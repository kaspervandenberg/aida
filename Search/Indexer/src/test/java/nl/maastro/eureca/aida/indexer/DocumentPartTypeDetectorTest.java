// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URL;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DocumentPartTypeDetectorTest {
	private static final String METADATA_RESOURCE = "/datadir/{C2212583-3E6D-4AB2-8F80-2C8934833CAB}.xml";
	private static final String DATA_RESOURCE = "/referenced-data/txt/2012/52/00000000/50003BX4.TXT";
	
	
	private URL metadataLocation;
	private URL dataLocation;
	private DocumentPartTypeDetector testee;
	
	@Before
	public void setup() {
		metadataLocation = DocumentPartTypeDetectorTest.class.getResource(METADATA_RESOURCE);
		dataLocation = DocumentPartTypeDetectorTest.class.getResource(DATA_RESOURCE);
		testee = new DocumentPartTypeDetector();
	}
	
	@Test
	public void detectZylabXml() {
		assertThat(testee.determinePartOf(metadataLocation), is(DocumentParts.METADATA));
	}

	@Test
	public void detectZylabData() {
		assertThat(testee.determinePartOf(dataLocation), is(DocumentParts.DATA));
	}
}
