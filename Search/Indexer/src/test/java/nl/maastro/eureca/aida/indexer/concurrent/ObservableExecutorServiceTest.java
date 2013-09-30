// © Maastro, 2013
package nl.maastro.eureca.aida.indexer.concurrent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.ActionSequence;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.Blocking;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.ConcurrentTestContext;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.DurationMeasurement;
import nl.maastro.eureca.aida.indexer.testdata.ActionSequences;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import org.junit.After;
import static org.junit.Assume.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static nl.maastro.eureca.aida.indexer.concurrent.ObservableExecutorServiceTest.DummyObserver.receivedAnyEvent;
import static nl.maastro.eureca.aida.indexer.concurrent.ObservableExecutorServiceTest.DummyObserver.Event.withValue;
import org.junit.Test;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
@RunWith(Theories.class)
public class ObservableExecutorServiceTest implements ConcurrentTestContext<String> {
	static class DummyObserver implements ObservableExecutorService.Observer<String> {
		static class Event {
			public ObservableExecutorService<String> source;
			public Future<String> task;
			public String value;
			public DurationMeasurement getDuration;

			public Event(ObservableExecutorService<String> source_, Future<String> task_, String value_, DurationMeasurement getDuration_) {
				this.source = source_;
				this.task = task_;
				this.value = value_;
				this.getDuration = getDuration_;
			}


			public static Event register(ObservableExecutorService<String> source, Future<String> task) {
				try {
					DurationMeasurement stopwatch = new DurationMeasurement();
					stopwatch.start();
					String value = task.get();
					stopwatch.stop();
					return new Event(source, task, value, stopwatch);
				} catch(ExecutionException | InterruptedException ex) {
					assumeNoException(ex);
					throw new Error("Should not reach this line");
				}
			}

			public static Matcher<Event> withValue(final String expected) {
				return new TypeSafeDiagnosingMatcher<Event>() {
					@Override
					protected boolean matchesSafely(Event item, Description mismatchDescription) {
						return item.value.equals(expected);
					}

					@Override
					public void describeTo(Description description) {
						description.appendText("with value ");
						description.appendValue(expected);
					}
				};
			}
		}

		private List<Event> eventsReceived = new LinkedList<>();
		private ReadWriteLock eventLock = new ReentrantReadWriteLock();
		
		@Override
		public void taskFinished(ObservableExecutorService<String> source, Future<String> task) {
			Event receivedEvent = Event.register(source, task);
			add(receivedEvent);
		}

		public static Matcher<DummyObserver> receivedAnyEvent(final Matcher<Event> inner) {
			return new TypeSafeDiagnosingMatcher<DummyObserver>() {

				@Override
				protected boolean matchesSafely(DummyObserver item, Description mismatchDescription) {
					return item.applyMatcher(inner);
				}

				@Override
				public void describeTo(Description description) {
					description.appendText("received any event ").appendDescriptionOf(inner);
				}
			};
		}
		
		public List<Event> getEventsReceived() {
			try {
				eventLock.readLock().lock();
				return new ArrayList<>(eventsReceived);
			} finally {
				eventLock.readLock().unlock();
			}
		}
		
		private void add(Event event) {
			try {
				eventLock.writeLock().lock();
				eventsReceived.add(event);
			} finally {
				eventLock.writeLock().unlock();
			}
		}

		private boolean applyMatcher(Matcher<Event> inner) {
			try {
				eventLock.readLock().lock();
				return Matchers.hasItem(inner).matches(eventsReceived);
			} finally {
				eventLock.readLock().unlock();
			}
		}
	}

	@DataPoints
	public static ActionSequence<String>[] SEQUENCES = ActionSequences.values();

	private static final int N_TEST_TASKS = 3;
	private List<Blocking<String>> testTasks;
	private DummyObserver observer;
	private ConcurrentMap<Integer, Future<String>> submittedTasks;
	private ExecutorService executor;  
	
	private ObservableExecutorService<String> testee;
	private final ActionSequence<String> sequence;

	public ObservableExecutorServiceTest(ActionSequence<String> sequence_) {
		this.sequence = sequence_;
	}

	@Before
	public void setup() throws Exception {
		testTasks = createTasks();
		observer = new DummyObserver();
		submittedTasks = new ConcurrentHashMap<>(N_TEST_TASKS);

		executor = Executors.newSingleThreadExecutor();
		
		testee = new ObservableExecutorService<>(executor);
	}

	@After
	public void teardown() {
		executor.shutdown();
	}
	
	@Test
	public void testScheduleTask() {
		assumeTrue(sequence.willStart(0));

		sequence.execute(this);
		
		blockUntilTasksFinished();
		assertThat(testTasks.get(0).currentState(), anyOf(
				is(Blocking.State.CALLED), is(Blocking.State.RUNNING), is(Blocking.State.FINISHED), is(Blocking.State.TERMINATED)));
	}

	@Test
	public void testFuture() throws InterruptedException, ExecutionException, TimeoutException {
		assumeTrue(sequence.willStart(0));

		sequence.execute(this);
		
		blockUntilTasksFinished();
		submittedTasks.get(0).get(2, TimeUnit.SECONDS);
	}

	@Test
	public void testObserveTask() {
		assumeTrue(sequence.willFinish(0));
		
		sequence.execute(this);

		blockUntilTasksFinished();
		assertThat(observer, receivedAnyEvent(withValue(createResultFor(0))));
	}
	
	@Override
	public Blocking<? extends String> getTask(int i) {
		return testTasks.get(i);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void submit(int taskId, Callable<? extends String> task) {
		Future<String> submittedTask = testee.submit(observer, (Callable<String>)task);
		submittedTasks.put(taskId, submittedTask);
	}
	
	private List<Blocking<String>> createTasks() {
		List<Blocking<String>> result = new ArrayList<>(N_TEST_TASKS);
		for (int i = 0; i < N_TEST_TASKS; i++) {
			result.add(createTask(i));
		}
		return result;
	}

	private Blocking<String> createTask(int i) {
		return Blocking.createDummy(createResultFor(i));
	}

	private String createResultFor(int i) {
		return String.format("task %d called", i);
	}

	private void blockUntilTasksFinished() {
		for (Map.Entry<Integer, Future<String>> entry : submittedTasks.entrySet()) {
			if(sequence.willFinish(entry.getKey())) {
				waitForTaskToFinish(entry.getValue());
			}
		}
	}

	private void waitForTaskToFinish(Future<?> task) {
		try {
			task.get(2000, TimeUnit.MILLISECONDS);
		} catch (ExecutionException | InterruptedException | TimeoutException ex) {
			assumeNoException(ex);
		}
	}
	
}
