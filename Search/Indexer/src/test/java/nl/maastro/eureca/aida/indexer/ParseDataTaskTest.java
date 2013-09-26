// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.*;
import static nl.maastro.eureca.aida.indexer.matchers.LunceneMatchers.*;
import nl.maastro.eureca.aida.indexer.FieldsToIndex;
import org.apache.commons.io.FilenameUtils;
import org.junit.experimental.theories.Theory;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
@RunWith(Theories.class)
public class ParseDataTaskTest {
	private static final String DATA_ZYLAB_RESOURCE = "/referenced-data/txt/2012/52/00000000/50003BX4.TXT";
	private static final String DATA_WORD_RESOURCE = "/datadir/Grant Update 1-2.doc";
	private final String resource;
	
	private ZylabData data;
	private URL dataUrl;
	private ParseDataTask testee;
	
	@DataPoints
	public final static String[] RESOURCES = { DATA_ZYLAB_RESOURCE, DATA_WORD_RESOURCE}; 

	@DataPoints
	public final static FieldsToIndex[] fields() {
		Set<Map.Entry<FieldsToIndex, Object>> fieldSourceEntries = ZylabData.getFieldSourceEntries(DocumentParts.DATA);
		FieldsToIndex[] result = new FieldsToIndex[fieldSourceEntries.size()];
		
		int i = 0;
		for (Map.Entry<FieldsToIndex, Object> entry : fieldSourceEntries) {
			result[i] = entry.getKey();
			i++;
		}
		return result;
	}

	
	public ParseDataTaskTest(String resource_) {
		resource = resource_;
	}

	@Before
	public void setup() {
		data = new ZylabData();
		dataUrl = ParseDataTaskTest.class.getResource(resource);

		testee = new ParseDataTask(data, dataUrl);
	}

	@Test
	public void testCall() throws Exception {
		testee.call();
	}

	@Test
	public void testHasContent() throws Exception {
		testee.call();
		assertThat("has content field", data.getFields(), hasItem(fieldNamed(FieldsToIndex.CONTENT.fieldName)));
	}

	@Test
	public void testHasTitle() throws Exception {
		assumeThat("is not plain txt", FilenameUtils.getExtension(resource).toLowerCase(), not("txt"));
		testee.call();
		assertThat("has title field", data.getFields(), hasItem(fieldNamed(FieldsToIndex.TITLE.fieldName)));
	}

	@Theory
	public void testHasField(FieldsToIndex field) throws Exception {
		assumeThat("is not plain txt", FilenameUtils.getExtension(resource).toLowerCase(), not("txt"));
		testee.call();
		assertThat("has field", data.getFields(), hasItem(fieldNamed(field.fieldName)));
	}
}