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
import static nl.maastro.eureca.aida.indexer.matchers.LunceneMatchers.*;
import nl.maastro.eureca.aida.indexer.mocked.Resolver;
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
	private static final String EXPECTED_ID = "{C2212583-3E6D-4AB2-8F80-2C8934833CAB}"; 
	private static final String EXPECTED_PATIS_NR = "12345";
	@SuppressWarnings("serial")
	private static Map<FieldsToIndex, String> EXPECTED_VALUES = Collections.unmodifiableMap(
			new EnumMap<FieldsToIndex, String>(FieldsToIndex.class) {{
		put(FieldsToIndex.ID, ParseZylabMetadataTaskTest.EXPECTED_ID);
		put(FieldsToIndex.PATISNUMMER, ParseZylabMetadataTaskTest.EXPECTED_PATIS_NR);
	}});

	@DataPoints
	public static FieldsToIndex[] FIELDS = {
		FieldsToIndex.ID, FieldsToIndex.PATISNUMMER
	};
	
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

	@Theory
	public void testHasField(FieldsToIndex field) throws Exception {
		testee.call();

		assertThat(String.format("has field %s", field.name()), data.getFields(), hasItem(fieldNamed(field.fieldName)));
	}

	@Theory
	public void testHasFieldWithCorrectValue(FieldsToIndex field) throws Exception {
		assumeTrue(EXPECTED_VALUES.containsKey(field));
		
		testee.call();

		assertThat("has correct field–value", data.getFields(), hasItem(allOf(
				fieldNamed(field.fieldName),
				fieldValue(EXPECTED_VALUES.get(field)))));
	}

	@Test
	public void testHasDataUrl() throws Exception {
		testee.call();

		assertThat("Data URL as expected", data.getDataUrl(), is(mockedResolver.getDataUrl()));
	}
}