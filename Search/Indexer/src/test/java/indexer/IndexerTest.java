package indexer;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.maastro.eureca.aida.indexer.tika.parser.ZylabMetadataXml;
import nl.maastro.eureca.aida.indexer.tika.parser.ZylabMetadataXmlDetector;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.parser.AutoDetectParser;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.equalTo;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.BeforeClass;

/**
 *
 * @author emeij
 */
public class IndexerTest {
    
  boolean deleteAfterwards = false;
  String indexdir = "testindex";
  File index;
  Indexer instance;
  IndexWriterUtil iwUtil;
  
  	@BeforeClass
	public static void setUpClass() throws IOException {
		Logger.getLogger("").addHandler(new FileHandler("indexer-test.log"));
		Logger.getLogger(indexer.BaseIndexing.class.getName()).setLevel(Level.ALL);
	}
  
	@Before
    public void setUp() throws Exception {
        index = new File(Utilities.getINDEXDIR() + indexdir);
        Utilities.deleteDir(index);
		instance = new Indexer();
		iwUtil = new IndexWriterUtil(new ConfigurationHandler("indexconfig.xml"), indexdir);
    }

	@After
    public void tearDown() throws Exception {
		iwUtil.closeIndexWriter();
    }
    
  /**
   * Test of index method, of class Indexer.
   */
	@Test
  public void testIndexMSword() throws IOException {
    String dataPath = "datadir" + File.separator + "Grant Update 1-2.doc";
    System.out.println(instance.index(indexdir, dataPath));
    assertEquals("In index", 1, iwUtil.getDocsInIndexCount());
    assertEquals("In cache", 1, Utilities.getDocsInCache(indexdir));
  }
  
  /**
   * Test of index method, of class Indexer.
   */
	@Test
  public void testIndexPDF() throws IOException {
    String dataPath = "datadir" + File.separator + "dp1LSAintro.pdf";
    System.out.println(instance.index(indexdir, dataPath));
    assertEquals("In index", 1, iwUtil.getDocsInIndexCount());
    assertEquals("In cache", 1, Utilities.getDocsInCache(indexdir));
  }
  
  /**
   * Test of index method, of class Indexer.
   */
	@Test
  public void testIndexTxt() throws IOException {
    String dataPath = "datadir" + File.separator + "wonder.txt";
    System.out.println(instance.index(indexdir, dataPath));
    assertEquals("In index", 1, iwUtil.getDocsInIndexCount());
    assertEquals("In cache", 1, Utilities.getDocsInCache(indexdir));
  }
  
  /**
   * Test of index method, of class Indexer.
   */
  // TODO issue AIDA-8 medline files can consist of multiple documents
	@Test
  @Ignore
  public void testIndexMedline() throws IOException {
    String dataPath = "datadir" + File.separator + "100.med";
    System.out.println(instance.index(indexdir, dataPath));
    assertEquals("In index", 143, iwUtil.getDocsInIndexCount());
    assertEquals("In cache", 1, Utilities.getDocsInCache(indexdir));
  }

	@Test
	public void testParserRegistered() {
		AutoDetectParser p = new AutoDetectParser();
		assertThat(
				"Zylab parser not available",
				p.getParsers(),
				hasKey(ZylabMetadataXml.ZYLAB_METADATA));
	}

	@Test
	public void testDetectorRegistered() {
		DefaultDetector d = new DefaultDetector();
				
		assertThat(
				"Zylab detector not available",
				d.getDetectors(),
				hasItem(Matchers.<Detector>instanceOf(ZylabMetadataXmlDetector.class)));
	}

	@Test
	public void testIndexZylab() throws IOException {
		String dataPath = "datadir" + File.separator + "{C2212583-3E6D-4AB2-8F80-2C8934833CAB}.xml";
		System.out.println(instance.index(indexdir, dataPath));
		assertEquals("In index", 1, iwUtil.getDocsInIndexCount());
		assertEquals("In cache", 1, Utilities.getDocsInCache(indexdir));
	}

	@Test
  public void testIndexAll() throws IOException {
    String dataPath = "datadir" + File.separator;
    System.out.println(instance.index(indexdir, dataPath));
// TODO issue AIDA-8 medline files can consist of multiple documents
//    assertEquals("In index", 186, iwUtil.getDocsInIndexCount());
	assertEquals("In index: ", 6, iwUtil.getDocsInIndexCount());
    
    if (deleteAfterwards)
      assertEquals(Utilities.deleteDir(new File(indexdir)), true);
    
  }

	@Test
	public void testIndexContainsField() throws IOException {
		testIndexZylab();
		DirectoryReader dirReader = DirectoryReader.open(iwUtil.getIndexWriter(), false);
		assertEquals(dirReader.maxDoc(), 1);
//		assertThat(dirReader.document(0).getFields(), hasItem(
//				new FeatureMatcher<IndexableField, String>(equalTo("Patisnummer"), "Field key", "Field key") {
//			@Override
//			protected String featureValueOf(IndexableField actual) {
//				return actual.name();
//			} } ));
		System.out.println(String.format("maxdoc: %d", dirReader.maxDoc()));
		Document doc = dirReader.document(0);
		for(IndexableField field : doc.getFields()) {
			System.out.println(String.format("Field name: %s,\t value: %s", field.name(), field.stringValue()));
		}
	}
  
  public void testIndexAll(boolean deleteAfterwards) throws IOException {
    this.deleteAfterwards = deleteAfterwards;
    testIndexAll();
  }

}
