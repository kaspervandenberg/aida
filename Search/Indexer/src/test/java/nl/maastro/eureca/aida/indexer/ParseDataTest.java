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
import static nl.maastro.eureca.aida.indexer.LunceneMatchers.*;
import nl.maastro.eureca.aida.indexer.FieldsToIndex;
import org.apache.commons.io.FilenameUtils;
import org.junit.experimental.theories.Theory;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
@RunWith(Theories.class)
public class ParseDataTest {
	private static final String DATA_ZYLAB_RESOURCE = "/referenced-data/txt/2012/52/00000000/50003BX4.TXT";
	private static final String DATA_WORD_RESOURCE = "/datadir/Grant Update 1-2.doc";
	private final String resource;
	
	private ZylabData data;
	private URL dataUrl;
	private ParseData testee;
	
	@DataPoints
	public final static String[] RESOURCES = { DATA_ZYLAB_RESOURCE, DATA_WORD_RESOURCE}; 

	@DataPoints
	public final static FieldsToIndex[] fields() {
//		System.out.println("fields() called");
		Set<Map.Entry<FieldsToIndex, Object>> fieldSourceEntries = ZylabData.getFieldSourceEntries(ZylabData.DocumentParts.DATA);
//		System.out.println("entries: " + fieldSourceEntries);
		FieldsToIndex[] result = new FieldsToIndex[fieldSourceEntries.size()];
		
		int i = 0;
		for (Map.Entry<FieldsToIndex, Object> entry : fieldSourceEntries) {
//			System.out.println("\t entry: " + entry.getKey().fieldName);
			result[i] = entry.getKey();
			i++;
		}
//		System.out.println("result: " + result);
		return result;
	}

	
	public ParseDataTest(String resource_) {
		resource = resource_;
	}

	@Before
	public void setup() {
		data = new ZylabData();
		dataUrl = ParseDataTest.class.getResource(resource);

		testee = new ParseData(data, dataUrl);
	}

	@Theory
	public void testCall() throws Exception {
		testee.call();
	}

	@Theory
	public void testHasContent() throws Exception {
		testee.call();
		assertThat("has content field", data.getFields(), hasItem(fieldNamed(FieldsToIndex.CONTENT.fieldName)));
	}

	@Theory
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