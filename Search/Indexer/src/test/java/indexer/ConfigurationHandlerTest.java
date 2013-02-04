/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package indexer;

import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 *
 * @author emeij
 */
public class ConfigurationHandlerTest extends TestCase {
  
  ConfigurationHandler cfg = null;
    
    public ConfigurationHandlerTest(String testName) {
        super(testName);
        this.cfg = new ConfigurationHandler("indexconfig.xml");
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

  /**
   * Test of getName method, of class ConfigurationHandler.
   */
  public void testGetName() {
    //System.out.println("getName");
    String expResult = "My_index";
    String result = cfg.getName();
    assertEquals(expResult, result);
  }

  /**
   * Test of OverWrite method, of class ConfigurationHandler.
   */
  public void testOverWrite() {
    //System.out.println("OverWrite");
    
    boolean expResult = true;
    boolean result = cfg.OverWrite();
    assertEquals(expResult, result);
  }

  /**
   * Test of getCreator method, of class ConfigurationHandler.
   */
  public void testGetCreator() {
    //System.out.println("getCreator");
    
    String expResult = "unknown";
    String result = cfg.getCreator();
    assertEquals(expResult, result);
  }

  /**
   * Test of getDataPath method, of class ConfigurationHandler.
   */
  public void testGetDataPath() {
    //System.out.println("getDataPath");
    
    String expResult = "datadir";
    String result = cfg.getDataPath();
    assertEquals(expResult, result);
  }

  /**
   * Test of getMergeFactor method, of class ConfigurationHandler.
   */
  public void testGetMergeFactor() {
    //System.out.println("getMergeFactor");
    
    int expResult = 300;
    int result = cfg.getMergeFactor();
    assertEquals(expResult, result);
  }

  /**
   * Test of getMaxBufferedDocs method, of class ConfigurationHandler.
   */
  public void testGetMaxBufferedDocs() {
    //System.out.println("getMaxBufferedDocs");
    
    int expResult = 30;
    int result = cfg.getMaxBufferedDocs();
    assertEquals(expResult, result);
  }

  /**
   * Test of getFields method, of class ConfigurationHandler.
   */
  public String[] testGetFields(String Doc) {
    //System.out.println("getFields");
    
    return cfg.getFields(Doc);
    //assertEquals(expResult, result);
  }

  /**
   * Test of getGlobalAnalyzer method, of class ConfigurationHandler.
   */
  public void testGetGlobalAnalyzer() {
    //System.out.println("getGlobalAnalyzer");
    
    String expResult = "STANDARD";
    String result = cfg.getGlobalAnalyzer();
    assertEquals(expResult, result);
  }

  /**
   * Test of getFieldIndexValue method, of class ConfigurationHandler.
   */
  public String testGetFieldIndexValue(String Doc, String FieldName) {
    ////System.out.println("getFieldIndexValue");
    
    Index result = cfg.getFieldIndexValue(Doc, FieldName);
    return "FIV of " + Doc + ", " + FieldName + ": " + result.toString();
    //assertEquals(expResult, result);
  }

  /**
   * Test of getFieldStoreValue method, of class ConfigurationHandler.
   */
  public String testGetFieldStoreValue(String Doc, String FieldName) {
    ////System.out.println("getFieldStoreValue");
    
    Store result = cfg.getFieldStoreValue(Doc, FieldName);
     return "FSV of " + Doc + ", " + FieldName + ": " + result.toString();
    //assertEquals(expResult, result);
  }

  /**
   * Test of getTermVectorValue method, of class ConfigurationHandler.
   */
  public String testGetTermVectorValue(String Doc, String FieldName) {
    ////System.out.println("getTermVectorValue");
    
    TermVector result = cfg.getTermVectorValue(Doc, FieldName);
    return "TV of " + Doc + ", " + FieldName + ": " + result.toString();
    //assertEquals(expResult, result);
  }

  /**
   * Test of getFieldDescription method, of class ConfigurationHandler.
   */
  public String testGetFieldDescription(String Doc, String FieldName) {
    ////System.out.println("getFieldDescription");
    
    String result = cfg.getFieldDescription(Doc, FieldName);
    return "Description of " + Doc + ", " + FieldName + ": " + result;
    //assertEquals(expResult, result);
  }

  /**
   * Test of getDocumentAnalyzer method, of class ConfigurationHandler.
   */
  public void testGetDocumentAnalyzer(String Doc, String expResult) {
    //System.out.println("getDocumentAnalyzer");

    String result = cfg.getDocumentAnalyzer(Doc);
    assertEquals(expResult, result);
  }

  /**
   * Test of getDocumentExtensions method, of class ConfigurationHandler.
   */
  public List<String> testGetDocumentExtensions(String Doc) {
    //System.out.println("getDocumentExtensions");
    
    return cfg.getDocumentExtensions(Doc);
  }

  /**
   * Test of getDocumentTypes method, of class ConfigurationHandler.
   */
  public void testGetDocumentTypes() {
    //System.out.println("getDocumentTypes");
    
    List<String> result = cfg.getDocumentTypes();

    Iterator<String> it = result.iterator();
    
    while (it.hasNext()) {
      String doc = it.next();
      
      List<String> extensions = testGetDocumentExtensions(doc);
      // loop over extensions
      Iterator<String> extit = extensions.iterator();
      while (extit.hasNext()) {
        testGetDocType(extit.next(), doc);
      }
      
      String[] f = testGetFields(doc);
      // loop over fields
      for (String i : f) {
        testGetFieldDescription(doc, i);
        testGetTermVectorValue(doc, i);
        testGetFieldStoreValue(doc, i);
        testGetFieldIndexValue(doc, i);
      }
    }

  }

  /**
   * Test of getDocType method, of class ConfigurationHandler.
   */
  public void testGetDocType(String extension, String expResult) {
    //System.out.println("getDocType");

    String result = cfg.getDocType(extension);
    assertEquals(expResult, result);
  }

}
