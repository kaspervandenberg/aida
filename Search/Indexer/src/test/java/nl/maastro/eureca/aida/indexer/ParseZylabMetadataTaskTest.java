// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import static nl.maastro.eureca.aida.indexer.matchers.LuceneMatchers.fieldNamed;
import static nl.maastro.eureca.aida.indexer.matchers.LuceneMatchers.fieldValue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import nl.maastro.eureca.aida.indexer.mocked.Resolver;
import nl.maastro.eureca.aida.indexer.testdata.Fields;
import nl.maastro.eureca.aida.indexer.testdata.Term;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

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
