// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import nl.maastro.eureca.aida.indexer.mocked.Resolver;
import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DocumentParseTaskSynchronizerTest {
	private static class MockedFuture {
		@Mock public Future<ZylabData> future;
		public boolean finished;

		public MockedFuture() {
			this.finished = false;
		}
		
		public void setup() {
			MockitoAnnotations.initMocks(this);

			when (future.isCancelled()) .thenReturn(false);
			when (future.isDone()) .thenAnswer(new Answer<Boolean>() {
				@Override
				public Boolean answer(InvocationOnMock invocation) throws Throwable {
					return finished;
				} });
		}
	}
	
	private static final String DATA_RESOURCE = "/referenced-data/txt/2012/52/00000000/50003BX4.TXT";
	
	@Mock private ExecutorService executor;
	private MockedFuture parseMetadataFuture;
	private MockedFuture parseDataFuture;
	private Resolver mockedResolver;
	private URL metadataLocation;
	private URL dataLocation;
	private DocumentParseTaskSynchronizer testee;
	
	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockedResolver = new Resolver();
		metadataLocation = mockedResolver.getMetadataURL();
		dataLocation = DocumentParseTaskSynchronizerTest.class.getResource(DATA_RESOURCE);
		
		parseMetadataFuture = createMockedTaskFuture(ParseZylabMetadataTask.class);
		parseDataFuture = createMockedTaskFuture(ParseDataTask.class);
		when (executor.submit(any(Runnable.class))) .thenReturn(mock(Future.class));

		testee = new DocumentParseTaskSynchronizer(executor, mockedResolver.getResolver());
	}

	@Test
	public void testArriveMetadataFile() {
		testee.arrive(metadataLocation);
		
		verify (executor).submit(isA(ParseZylabMetadataTask.class));
	}

	@Test
	public void testArriveDataFile() {
		testee.arrive(dataLocation);

		verify (executor).submit(isA(ParseDataTask.class));
	}

	@Test
	public void testArriveDataBeforeMetadata() {
		testee.arrive(dataLocation);
		testee.arrive(metadataLocation);
		
		verify (executor).submit(isA(ParseDataTask.class));
		verify (executor).submit(isA(ParseZylabMetadataTask.class));
	}
	
	@Test
	public void testArriveMetadataBeforeData() {
		testee.arrive(metadataLocation);
		testee.arrive(dataLocation);
		
		verify (executor).submit(isA(ParseDataTask.class));
		verify (executor).submit(isA(ParseZylabMetadataTask.class));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testFinishDataBeforeMetadata() {
		testee.arrive(metadataLocation);
		testee.arrive(dataLocation);
		parseDataFuture.finished = true;
		testee.finish(dataLocation);
		parseMetadataFuture.finished = true;
		testee.finish(metadataLocation);

		verify (executor).submit(isA(IndexTask.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFinishMetadataBeforeData() {
		testee.arrive(metadataLocation);
		testee.arrive(dataLocation);
		parseMetadataFuture.finished = true;
		testee.finish(metadataLocation);
		parseDataFuture.finished = true;
		testee.finish(dataLocation);

		verify (executor).submit(isA(IndexTask.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFinishBothBeforeEventsSent_submitIndexTaskOnce() {
		testee.arrive(metadataLocation);
		testee.arrive(dataLocation);
		parseMetadataFuture.finished = true;
		parseDataFuture.finished = true;
		testee.finish(metadataLocation);
		testee.finish(dataLocation);

		verify (executor, atMost(1)).submit(isA(IndexTask.class));
	}

	@Test
	public void testArriveAndFinishOnlyData_noIndexTaskSubmitted() {
		testee.arrive(dataLocation);
		parseDataFuture.finished = true;
		testee.finish(dataLocation);
		
		verify (executor, never()).submit(isA(IndexTask.class));
	}

	@Test
	public void testArriveAndFinishOnlyMetadata_noIndexTaskSubmitted() {
		testee.arrive(dataLocation);
		parseDataFuture.finished = true;
		testee.finish(dataLocation);
		
		verify (executor, never()).submit(isA(IndexTask.class));
	}

	private MockedFuture createMockedTaskFuture(Class<? extends Callable<ZylabData>> submittedClass) {
		MockedFuture mocked = new MockedFuture();
		mocked.setup();
		when (executor.submit(isA(submittedClass))) .thenReturn(mocked.future);
		return mocked;
	}
}