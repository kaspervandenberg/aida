// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.MockedFuture;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
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
	
	@Mock private ExecutorService executor;
	private MockedFuture parseMetadataFuture;
	private MockedFuture parseDataFuture;
	private Resolver mockedResolver;
	private URL metadataLocation;
	private URL dataLocation;
	private ConcurrentLinkedQueue<ZylabData> dataToIndex;
	private DocumentParseTaskSynchronizer testee;
	
	@Before
	@SuppressWarnings("unchecked")
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

		assertThat(dataToIndex.size(), org.hamcrest.Matchers.greaterThanOrEqualTo(1));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFinishMetadataBeforeData_queueData() {
		testee.arrive(metadataLocation);
		testee.arrive(dataLocation);
		parseMetadataFuture.finished = true;
		testee.finish(metadataLocation);
		parseDataFuture.finished = true;
		testee.finish(dataLocation);

		assertThat(dataToIndex.size(), org.hamcrest.Matchers.greaterThanOrEqualTo(1));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFinishBothBeforeEventsSent_queueData() {
		testee.arrive(metadataLocation);
		testee.arrive(dataLocation);
		parseMetadataFuture.finished = true;
		parseDataFuture.finished = true;
		testee.finish(metadataLocation);
		testee.finish(dataLocation);

		assertThat(dataToIndex.size(), org.hamcrest.Matchers.equalTo(1));
	}

	@Test
	public void testArriveAndFinishOnlyData_doNotQueueData() {
		testee.arrive(dataLocation);
		parseDataFuture.finished = true;
		testee.finish(dataLocation);
		
		assertTrue(dataToIndex.isEmpty());
	}

	@Test
	public void testArriveAndFinishOnlyMetadata_doNotQueueData() {
		testee.arrive(dataLocation);
		parseDataFuture.finished = true;
		testee.finish(dataLocation);
		
		assertTrue(dataToIndex.isEmpty());
	}

	private MockedFuture createMockedTaskFuture(Class<? extends Callable<ZylabData>> submittedClass) {
		MockedFuture mocked = new MockedFuture();
		mocked.setup();
		when (executor.submit(isA(submittedClass))) .thenReturn(mocked.future);
		return mocked;
	}
}