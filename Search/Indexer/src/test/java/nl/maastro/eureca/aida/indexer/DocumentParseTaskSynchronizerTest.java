// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;
import nl.maastro.eureca.aida.indexer.mocked.Resolver;
import org.junit.Test;
import org.junit.Before;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
//import static org.hamcrest.Matchers.*;
		

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DocumentParseTaskSynchronizerTest {
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
		dataLocation = mockedResolver.getDataUrl();
		dataToIndex = new ConcurrentLinkedQueue<>();
		
		testee = new DocumentParseTaskSynchronizer(mockedResolver.getResolver(), dataToIndex);
	}

	@Test
	public void testFinishDataBeforeMetadata_queData() {
		testee.arrive(metadataLocation);
		testee.arrive(dataLocation);

		assertThat(dataToIndex.size(), org.hamcrest.Matchers.greaterThanOrEqualTo(1));
	}

	@Test
	public void testFinishMetadataBeforeData_queueData() {
		testee.arrive(metadataLocation);
		testee.arrive(dataLocation);

		assertThat(dataToIndex.size(), org.hamcrest.Matchers.greaterThanOrEqualTo(1));
	}

	@Test
	public void testArriveAndFinishOnlyData_doNotQueueData() {
		testee.arrive(dataLocation);
		
		assertTrue(dataToIndex.isEmpty());
	}

	@Test
	public void testArriveAndFinishOnlyMetadata_doNotQueueData() {
		testee.arrive(dataLocation);
		
		assertTrue(dataToIndex.isEmpty());
	}
}
