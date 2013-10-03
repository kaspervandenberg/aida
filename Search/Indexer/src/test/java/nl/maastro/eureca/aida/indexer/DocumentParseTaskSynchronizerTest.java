// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.MockedFuture;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import nl.maastro.eureca.aida.indexer.concurrent.CompletionObserver;
import nl.maastro.eureca.aida.indexer.concurrent.ObservableExecutorService;
import nl.maastro.eureca.aida.indexer.mocked.Resolver;
import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
//import static org.hamcrest.Matchers.*;
		

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DocumentParseTaskSynchronizerTest {
	
	private static final String DATA_RESOURCE = "/referenced-data/txt/2012/52/00000000/50003BX4.TXT";
	
	@Mock private ObservableExecutorService executor;
	private MockedFuture parseMetadataFuture;
	private MockedFuture parseDataFuture;
	private Resolver mockedResolver;
	private URL metadataLocation;
	private URL dataLocation;
	private ConcurrentLinkedQueue<ZylabDocument> dataToIndex;
	private DocumentParseTaskSynchronizer testee;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockedResolver = new Resolver();
		metadataLocation = mockedResolver.getMetadataURL();
		dataLocation = DocumentParseTaskSynchronizerTest.class.getResource(DATA_RESOURCE);
		dataToIndex = new ConcurrentLinkedQueue<>();
		
		parseMetadataFuture = createMockedTaskFuture(ParseZylabMetadataTask.class);
		parseDataFuture = createMockedTaskFuture(ParseDataTask.class);

		testee = new DocumentParseTaskSynchronizer(executor, mockedResolver.getResolver(), dataToIndex);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testArriveMetadataFile() {
		testee.arrive(metadataLocation);
		
		verify (executor).subscribeAndSubmit(isA(CompletionObserver.class), isA(ParseZylabMetadataTask.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testArriveDataFile() {
		testee.arrive(dataLocation);

		verify (executor).subscribeAndSubmit(isA(CompletionObserver.class), isA(ParseDataTask.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testArriveDataBeforeMetadata() {
		testee.arrive(dataLocation);
		testee.arrive(metadataLocation);
		
		verify (executor).subscribeAndSubmit(isA(CompletionObserver.class), isA(ParseDataTask.class));
		verify (executor).subscribeAndSubmit(isA(CompletionObserver.class), isA(ParseZylabMetadataTask.class));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testArriveMetadataBeforeData() {
		testee.arrive(metadataLocation);
		testee.arrive(dataLocation);
		
		verify (executor).subscribeAndSubmit(isA(CompletionObserver.class), isA(ParseDataTask.class));
		verify (executor).subscribeAndSubmit(isA(CompletionObserver.class), isA(ParseZylabMetadataTask.class));
	}

	@Test
	public void testFinishDataBeforeMetadata_queData() {
		testee.arrive(metadataLocation);
		testee.arrive(dataLocation);
		parseDataFuture.finish();
		parseMetadataFuture.finish();

		assertThat(dataToIndex.size(), org.hamcrest.Matchers.greaterThanOrEqualTo(1));
	}

	@Test
	public void testFinishMetadataBeforeData_queueData() {
		testee.arrive(metadataLocation);
		testee.arrive(dataLocation);
		parseMetadataFuture.finish();
		parseDataFuture.finish();

		assertThat(dataToIndex.size(), org.hamcrest.Matchers.greaterThanOrEqualTo(1));
	}

	@Test
	public void testArriveAndFinishOnlyData_doNotQueueData() {
		testee.arrive(dataLocation);
		parseDataFuture.finish();
		
		assertTrue(dataToIndex.isEmpty());
	}

	@Test
	public void testArriveAndFinishOnlyMetadata_doNotQueueData() {
		testee.arrive(dataLocation);
		parseDataFuture.finish();
		
		assertTrue(dataToIndex.isEmpty());
	}

	@SuppressWarnings("unchecked")
	private MockedFuture createMockedTaskFuture(Class<? extends Callable<ZylabDocument>> submittedClass) {
		MockedFuture mocked = new MockedFuture();
		mocked.setup();
		mocked.mockExecutorCalls(executor, 
				org.hamcrest.Matchers.any((Class<CompletionObserver<ZylabDocument>>)(Object)CompletionObserver.class),
				org.hamcrest.Matchers.any(submittedClass));
		
		return mocked;
	}

}