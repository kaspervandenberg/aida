// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import static org.hamcrest.Matchers.*;
import org.mockito.MockitoAnnotations;
import static nl.maastro.eureca.aida.indexer.matchers.LunceneMatchers.*;
import nl.maastro.eureca.aida.indexer.mocked.Resolver;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ParseZylabMetadataTaskTest {
	private static final String ID = "{C2212583-3E6D-4AB2-8F80-2C8934833CAB}"; 
	private static final String EXP_PATIS_NR = "12345";
			
	
	private ZylabData data;
	private ParseZylabMetadataTask testee;
	private Resolver mockedResolver;
	private ReferenceResolver referenceResolver;
	
	public ParseZylabMetadataTaskTest() {
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		data = new ZylabData();

		mockedResolver = new Resolver();
		referenceResolver = mockedResolver.getResolver();

		testee = new ParseZylabMetadataTask(data, mockedResolver.getMetadataURL(), referenceResolver);
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
				fieldValue(ParseZylabMetadataTaskTest.EXP_PATIS_NR))));
	}

	@Test
	public void testHasDataUrl() throws Exception {
		testee.call();

		assertThat("Data URL as expected", data.getDataUrl(), is(mockedResolver.getDataUrl()));
	}
}