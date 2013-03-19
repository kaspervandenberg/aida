package indexer;

import java.io.File;
import java.io.IOException;
//import junit.framework.TestCase;
import org.apache.lucene.index.IndexReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author emeij
 */
public class WebServiceTest {
    
  boolean deleteAfterwards = true;
  
  /**
   * Test of indexFromRemote method, of class Indexer.
   */
	@Test
  public void testIndexFromRemote() {
    /*
          try {
            String endpoint = "http://localhost:8080/axis/services/SearcherWS";

            Service service = new Service();
            Call call = (Call) service.createCall();

            call.setTargetEndpointAddress(endpoint);
            call.setOperationName("searchJason"); 

            String res = (String) call.invoke( new Object[] {
                
            } ); 

          } catch (Exception e) {
            e.printStackTrace();
          }
    
    System.out.println("indexFromRemote");
    String config = "";
    String data = "";
    String extension = "";
    Indexer instance = new Indexer();
    String expResult = "";
    String result = instance.indexFromRemote(config, data, extension);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  */
  }

}
