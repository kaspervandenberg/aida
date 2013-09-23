// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import nl.maastro.eureca.aida.indexer.tika.parser.ZylabMetadataXml;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.Before;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static nl.maastro.eureca.aida.indexer.matchers.LunceneMatchers.*;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ParseZylabMetadataTest {
	private static final String METADATA_RESOURCE = "/datadir/{C2212583-3E6D-4AB2-8F80-2C8934833CAB}.xml";
	private static final String REF_PATH = "D:\\ZyIMAGE Data\\Index Data\\EMD\\txt\\2012\\52\\00000000\\";
	private static final String REF_FILE = "50003BX4.TXT"; 
	private static final String ID = "{C2212583-3E6D-4AB2-8F80-2C8934833CAB}"; 
	private static final String EXP_PATIS_NR = "12345";
			
	
	private ZylabData data;
	private ParseZylabMetadata testee;
	private URL metadataUrl;
	private URL dataUrl;
	@Mock private ReferenceResolver referenceResolver;
	
	public ParseZylabMetadataTest() {
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		data = new ZylabData();


		try {
			metadataUrl = ParseZylabMetadataTest.class.getResource(METADATA_RESOURCE);
			dataUrl = new URL("http://unexisting.localhost/data/data.doc");
//			when (referenceResolver.resolve(new ZylabMetadataXml.FileRef(REF_PATH, REF_FILE))).thenReturn(dataUrl);
			when (referenceResolver.resolve(Mockito.any(ZylabMetadataXml.FileRef.class))).thenReturn(dataUrl);
		} catch (MalformedURLException | URISyntaxException ex) {
			assumeNoException(ex);
		}

		testee = new ParseZylabMetadata(data, metadataUrl, referenceResolver);
	}

	@Test
	public void testCall() throws Exception {
		testee.call();
	}

	@Test
	public void testIdParsed() throws Exception {
		testee.call();

		assertThat("has field id", data.getFields(), hasItem(fieldNamed(FieldsToIndex.ID.fieldName)));
	}

	@Test
	public void testHasPatisNumberParsed() throws Exception {
		testee.call();

		assertThat("has field PatisNr", data.getFields(), hasItem(fieldNamed(FieldsToIndex.PATISNUMMER.fieldName)));
	}

	@Test
	public void testPatisNumberParsed() throws Exception {
		testee.call();

		assertThat("has correct PatisNr", data.getFields(), hasItem(allOf(
				fieldNamed(FieldsToIndex.PATISNUMMER.fieldName),
				fieldValue(ParseZylabMetadataTest.EXP_PATIS_NR))));
	}

	@Test
	public void testHasDataUrl() throws Exception {
		testee.call();

		assertThat("Data URL as expected", data.getDataUrl(), is(dataUrl));
	}
}