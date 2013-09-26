// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.Before;
import static org.hamcrest.Matchers.*;
import org.mockito.MockitoAnnotations;
import static nl.maastro.eureca.aida.indexer.matchers.LuceneMatchers.*;
import nl.maastro.eureca.aida.indexer.mocked.Resolver;
import nl.maastro.eureca.aida.indexer.testdata.Fields;
import nl.maastro.eureca.aida.indexer.testdata.Term;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
@RunWith(Theories.class)
public class ParseZylabMetadataTaskTest {

	@DataPoints
	public static Term[] TESTED_FIELDS = {
		Fields.ID, Fields.PATIS_NUMBER
	};
	
	private ZylabDocument data;
	private ParseZylabMetadataTask testee;
	private Resolver mockedResolver;
	private ReferenceResolver referenceResolver;
	
	public ParseZylabMetadataTaskTest() {
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		data = new ZylabDocumentImpl();

		mockedResolver = new Resolver();
		referenceResolver = mockedResolver.getResolver();

		testee = new ParseZylabMetadataTask(data, mockedResolver.getMetadataURL(), referenceResolver);
	}

	@Test
	public void testCall() throws Exception {
		testee.call();
	}

	@Theory
	public void testHasField(Term field) throws Exception {
		testee.call();

		assertThat("has field", data.getFields(), hasItem(fieldNamed(field)));
	}

	@Theory
	public void testHasFieldWithCorrectValue(Term field) throws Exception {
		testee.call();

		assertThat("has correct field–value", data.getFields(), hasItem(allOf(
				fieldNamed(field),
				fieldValue(field))));
	}

	@Test
	public void testHasDataUrl() throws Exception {
		testee.call();

		assertThat("Data URL as expected", data.getDataUrl(), is(mockedResolver.getDataUrl()));
	}
}
