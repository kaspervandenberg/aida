/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package indexer;

import java.util.Iterator;
import java.util.List;
//import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author emeij
 */
public class ConfigurationHandlerTest {
  
  ConfigurationHandler cfg = null;
    
    public ConfigurationHandlerTest() {
        this.cfg = new ConfigurationHandler("indexconfig.xml");
    }            

  /**
   * Test of getName method, of class ConfigurationHandler.
   */
	@Test
  public void testGetName() {
    //System.out.println("getName");
    String expResult = "My_index";
    String result = cfg.getName();
    assertEquals(expResult, result);
  }

  /**
   * Test of OverWrite method, of class ConfigurationHandler.
   */
	@Test
  public void testOverWrite() {
    //System.out.println("OverWrite");
    
    boolean expResult = true;
    boolean result = cfg.OverWrite();
    assertEquals(expResult, result);
  }

  /**
   * Test of getCreator method, of class ConfigurationHandler.
   */
	@Test
  public void testGetCreator() {
    //System.out.println("getCreator");
    
    String expResult = "unknown";
    String result = cfg.getCreator();
    assertEquals(expResult, result);
  }

  /**
   * Test of getDataPath method, of class ConfigurationHandler.
   */
	@Test
  public void testGetDataPath() {
    //System.out.println("getDataPath");
    
    String expResult = "datadir";
    String result = cfg.getDataPath();
    assertEquals(expResult, result);
  }

  /**
   * Test of getMergeFactor method, of class ConfigurationHandler.
   */
	@Test
  public void testGetMergeFactor() {
    //System.out.println("getMergeFactor");
    
    int expResult = 300;
    int result = cfg.getMergeFactor();
    assertEquals(expResult, result);
  }

  /**
   * Test of getMaxBufferedDocs method, of class ConfigurationHandler.
   */
	@Test
  public void testGetMaxBufferedDocs() {
    //System.out.println("getMaxBufferedDocs");
    
    int expResult = 30;
    int result = cfg.getMaxBufferedDocs();
    assertEquals(expResult, result);
  }

  /**
   * Test of getGlobalAnalyzer method, of class ConfigurationHandler.
   */
	@Test
  public void testGetGlobalAnalyzer() {
    //System.out.println("getGlobalAnalyzer");
    
    String expResult = "STANDARD";
    String result = cfg.getGlobalAnalyzer();
    assertEquals(expResult, result);
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
  public List<String> getDocumentExtensions(String Doc) {
    //System.out.println("getDocumentExtensions");
    
    return cfg.getDocumentExtensions(Doc);
  }

  /**
   * Test of getDocumentTypes method, of class ConfigurationHandler.
   */
  @Test
  public void testGetDocumentTypes() {
    //System.out.println("getDocumentTypes");
    
    List<String> result = cfg.getDocumentTypes();

    Iterator<String> it = result.iterator();
    
    while (it.hasNext()) {
      String doc = it.next();
      
      List<String> extensions = getDocumentExtensions(doc);
      // loop over extensions
      Iterator<String> extit = extensions.iterator();
      while (extit.hasNext()) {
        testGetDocType(extit.next(), doc);
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
