// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.hamcrest.Matchers.*;
import org.hamcrest.Matchers;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent.Kind;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.hamcrest.collection.IsEmptyCollection;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
@RunWith(Theories.class)
public class FilesystemWatchTaskTest {
	private class FinishTestee implements Answer<Void> {
		@Override
		public Void answer(InvocationOnMock invocation) throws Throwable {
			testeeFinished.release();
			waitForever.acquire();
			throw new IllegalStateException("Method should not reach this line");
		}
		
	}
	
	@DataPoints
	public static WatchEvent.Kind<?> eventKinds[] = new WatchEvent.Kind<?>[] {
			ENTRY_CREATE, ENTRY_MODIFY, OVERFLOW};

	@DataPoints
	public static WatchEvent.Kind<?> eventSequences[][] = new WatchEvent.Kind<?>[][] {
		{ ENTRY_CREATE },
		{ ENTRY_CREATE, ENTRY_CREATE },
		{ OVERFLOW, ENTRY_CREATE, ENTRY_MODIFY },
		{ },
		{ OVERFLOW, OVERFLOW }
	};

	private final static int SLEEP_TIME = 500;
	private static final int N_PATHS = 4;
	
//	private Mockery context;
	@Mock
	private	Path watchedPath;
	
	@Mock
	private FileSystem filesystem;

	@Mock
	private WatchService watchService;

	@Mock
	private WatchKey key;
	
	private List<WatchEvent<Object>> events;
	private List<Path> contextPaths;
	private List<Path> resolvedPaths;
	private FilesystemWatchTask testee;
	private ConcurrentLinkedDeque<Path> fileEvents;
	private Semaphore testeeFinished;
	private Semaphore waitForever;
	
	public FilesystemWatchTaskTest() {
	}
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		
		fileEvents = new ConcurrentLinkedDeque<>();
		events = new ArrayList<>();
		contextPaths = new ArrayList<>();
		resolvedPaths = new ArrayList<>();
		testeeFinished = new Semaphore(0);
		waitForever = new Semaphore(0);

		for (int i = 0; i < N_PATHS; i++) {
			@SuppressWarnings("unchecked")
			final WatchEvent<Object> event = mock(WatchEvent.class, String.format("event %d", i));
			final Path eventPath = mock(Path.class, String.format("context path %d", i));
			final Path resolvedPath = mock(Path.class, String.format("resolved path %d", i));
			
			events.add(i, event);
			contextPaths.add(i, eventPath);
			resolvedPaths.add(i, resolvedPath);
			
			when (event.context()). thenReturn(eventPath);
			when (watchedPath.resolve(eventPath)). thenReturn(resolvedPath);
		}
		
		when (watchedPath.getFileSystem()). thenReturn(filesystem);
		try {
			when (filesystem.newWatchService()). thenReturn(watchService);
			when (watchedPath.register(eq(watchService), org.mockito.Matchers.<Kind<?>>anyVararg())). thenReturn(key);
		} catch (IOException ex) {
			assumeNoException(ex);
		}

		try {
			testee = createWatcher(watchedPath);
		} catch (IOException ex) {
			assumeNoException(ex);
		}
	}

	@Test
	public void testCreateWatcher() {
		try {
			when(watchService.take()).thenAnswer(nTimesKeyThenFinish(0));
			
			startAndFinishTestee();
			verify(watchService).take();

		} catch(InterruptedException ex) {
			assumeNoException(ex);
		}
	}
	
	@Test
	public void testNoEventsNoCreateIndexTask() {
		try {
			when(watchService.take()).thenAnswer(nTimesKeyThenFinish(0));
			
			startAndFinishTestee();
			assertThat("No events", fileEvents, empty());

		} catch(InterruptedException ex) {
			assumeNoException(ex);
		}
	}
	@Theory
	@SuppressWarnings({"serial", "unchecked"})
	public void testSingleEventsOneCreateIndexTask(final WatchEvent.Kind<Object> kind) {
		assumeThat(kind,
				Matchers.<Kind<?>>isOneOf(StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY));
		
		try {
			when (key.pollEvents()). thenAnswer(nextSingleEvent());
			when (events.get(0).kind()).  thenReturn(kind);
			when (watchService.take()). thenAnswer(nTimesKeyThenFinish(1));
			
			startAndFinishTestee();
			assertThat("createIndexTask(resolvePaths[0]) is called", fileEvents, containsInAnyOrder(resolvedPaths.get(0)));

		} catch(InterruptedException ex) {
			assumeNoException(ex);
		}
	}

	@Test
	@SuppressWarnings({"unchecked", "serial"})
	public void testSingleIgnoredEventsNotCreateIndexTask() {
		try {
			when (key.pollEvents()). thenAnswer(nextSingleEvent());
			when (events.get(0).kind()).  thenReturn(OVERFLOW);
			when (watchService.take()). thenAnswer(nTimesKeyThenFinish(1));
			
			startAndFinishTestee();
			assertThat("No events", fileEvents, IsEmptyCollection.empty());

		} catch(InterruptedException ex) {
			assumeNoException(ex);
		}
	}

	@Theory
	@SuppressWarnings({"serial", "unchecked"})
	public void testMultipleEventsCreateIndexTasks(Kind<?> eventSequence[]) {
//		final WatchEvent.Kind<?>[] eventSequence = new WatchEvent.Kind<?>[] {
//			ENTRY_CREATE, ENTRY_CREATE
//		};
		
		try {
			for (int i = 0; i < eventSequence.length; i++) {
				WatchEvent.Kind<?> kind = eventSequence[i];
				when (events.get(i).kind()). thenReturn((Kind<Object>)kind);
			}
			when (key.pollEvents()). thenAnswer(nextSingleEvent());
			when (watchService.take()). thenAnswer(nTimesKeyThenFinish(eventSequence.length));
			
			
			startAndFinishTestee();
			
			List<Path> expectedPaths = new LinkedList<>();
			for (int i = 0; i < eventSequence.length; i++) {
				WatchEvent.Kind<?> kind = eventSequence[i];
				if(kind.equals(ENTRY_CREATE) || kind.equals(ENTRY_MODIFY)) {
					expectedPaths.add(resolvedPaths.get(i));
				}
			}
			assertThat("createIndexTask called for expected path", fileEvents, containsInAnyOrder(expectedPaths.toArray(new Path[expectedPaths.size()])));

		} catch(InterruptedException ex) {
			assumeNoException(ex);
		}
	}

	private void startAndFinishTestee() {
		try {
			testee.start();
			testeeFinished.tryAcquire(SLEEP_TIME, TimeUnit.MILLISECONDS);
			testee.cancel();
			waitForever.release();
			testee.join();
		} catch (InterruptedException ex) {
			assumeNoException(ex);
		}
	}
	
	private FilesystemWatchTask createWatcher(Path path) throws IOException {
		return new FilesystemWatchTask(path) {
			@Override
			protected void createIndexTask(Path file) {
				fileEvents.add(file);
			}
		};
	}

	private Answer<List<WatchEvent<Object>>> nextSingleEvent() {
		return new Answer<List<WatchEvent<Object>>>() {
			Iterator<WatchEvent<Object>> iter = events.iterator();
			
			@Override
			public List<WatchEvent<Object>> answer(InvocationOnMock invocation) throws Throwable {
				return Collections.<WatchEvent<Object>>singletonList(iter.next());
			}
		};
	}

	private Answer<WatchKey> nTimesKeyThenFinish(final int nTimes_) {
		return new Answer<WatchKey>() {
			int nTimes = nTimes_;

			@Override
			public WatchKey answer(InvocationOnMock invocation) throws Throwable {
				if(nTimes > 0) {
					nTimes--;
					return key;
				} else {
					testeeFinished.release();
					waitForever.acquire();
					throw new IllegalStateException("Method should not reach this line");
				}
			}
		};
	}

}
