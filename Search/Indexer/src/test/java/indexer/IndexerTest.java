package indexer;

import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;

/**
 *
 * @author emeij
 */
public class IndexerTest extends TestCase {
    
  boolean deleteAfterwards = false;
  String indexdir = "testindex";
  File index;
  Indexer instance;
  IndexWriterUtil iwUtil;
  
    public IndexerTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        index = new File(Utilities.getINDEXDIR() + indexdir);
        Utilities.deleteDir(index);
		instance = new Indexer();
		iwUtil = new IndexWriterUtil(new ConfigurationHandler("indexconfig.xml"), indexdir);
    }

    @Override
    protected void tearDown() throws Exception {
		iwUtil.closeIndexWriter();
        super.tearDown();
    }
    
  /**
   * Test of index method, of class Indexer.
   */
  public void testIndexMSword() throws IOException {
    String dataPath = "datadir" + File.separator + "Grant Update 1-2.doc";
    System.out.println(instance.index(indexdir, dataPath));
    assertEquals("In index", 1, iwUtil.getDocsInIndexCount());
    assertEquals("In cache", 1, Utilities.getDocsInCache(indexdir));
  }
  
  /**
   * Test of index method, of class Indexer.
   */
  public void testIndexPDF() throws IOException {
    String dataPath = "datadir" + File.separator + "dp1LSAintro.pdf";
    System.out.println(instance.index(indexdir, dataPath));
    assertEquals("In index", 1, iwUtil.getDocsInIndexCount());
    assertEquals("In cache", 1, Utilities.getDocsInCache(indexdir));
  }
  
  /**
   * Test of index method, of class Indexer.
   */
  public void testIndexTxt() throws IOException {
    String dataPath = "datadir" + File.separator + "wonder.txt";
    System.out.println(instance.index(indexdir, dataPath));
    assertEquals("In index", 1, iwUtil.getDocsInIndexCount());
    assertEquals("In cache", 1, Utilities.getDocsInCache(indexdir));
  }
  
  /**
   * Test of index method, of class Indexer.
   */
  public void testIndexMedline() throws IOException {
    String dataPath = "datadir" + File.separator + "100.med";
    System.out.println(instance.index(indexdir, dataPath));
    assertEquals("In index", 143, iwUtil.getDocsInIndexCount());
    assertEquals("In cache", 1, Utilities.getDocsInCache(indexdir));
  }
  
  public void testIndexAll() throws IOException {
    String dataPath = "datadir" + File.separator;
    System.out.println(instance.index(indexdir, dataPath));
    assertEquals("In index", 186, iwUtil.getDocsInIndexCount());
    
    if (deleteAfterwards)
      assertEquals(Utilities.deleteDir(new File(indexdir)), true);
    
  }
  
  public void testIndexAll(boolean deleteAfterwards) throws IOException {
    this.deleteAfterwards = deleteAfterwards;
    testIndexAll();
  }

}
