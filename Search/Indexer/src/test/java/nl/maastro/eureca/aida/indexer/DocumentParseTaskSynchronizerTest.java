// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import nl.maastro.eureca.aida.indexer.mocked.Resolver;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DocumentParseTaskSynchronizerTest {
	private static final String DATA_RESOURCE = "/referenced-data/txt/2012/52/00000000/50003BX4.TXT";
	
	@Mock private ExecutorService executor;
	private Resolver mockedResolver;
	private URL metadataLocation;
	private URL dataLocation;
	private DocumentParseTaskSynchronizer testee;
	
	public DocumentParseTaskSynchronizerTest() {
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockedResolver = new Resolver();
		metadataLocation = mockedResolver.getMetadataURL();
		dataLocation = DocumentParseTaskSynchronizerTest.class.getResource(DATA_RESOURCE);
		
		testee = new DocumentParseTaskSynchronizer(executor, mockedResolver.getResolver());
	}

	@Test
	public void testArriveMetadataFile() {
		testee.arrive(metadataLocation);
		
		verify (executor).submit(isA(ParseZylabMetadata.class));
	}

	@Test
	public void testArriveDataFile() {
		testee.arrive(dataLocation);

		verify (executor).submit(isA(ParseData.class));
	}

	@Test
	public void testArriveDataBeforeMetadata() {
		testee.arrive(dataLocation);
		testee.arrive(metadataLocation);
		
		verify (executor).submit(isA(ParseData.class));
		verify (executor).submit(isA(ParseZylabMetadata.class));
	}
	
	@Test
	public void testArriveMetadataBeforeData() {
		testee.arrive(metadataLocation);
		testee.arrive(dataLocation);
		
		verify (executor).submit(isA(ParseData.class));
		verify (executor).submit(isA(ParseZylabMetadata.class));
	}
}