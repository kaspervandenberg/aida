/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vle.aid.lucene;


import junit.framework.TestCase;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.xmlbeans.XmlOptions;
import org.vle.aid.ResultDocument;
import org.vle.aid.ResultType;

/**
 * See {@link SearcherWSTest} for more exhaustive tests of SearcherWS
 *
 * @author emeij
 */
public class SearcherWSTest_Basic extends TestCase {
    
    public SearcherWSTest_Basic(String testName) {
        super(testName);
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
   * Test of search method, of class SearcherWS.
   */
  public void testSearch() throws Exception {
     
    System.err.println("search");
    String index = "testindex";
    String queryString = "linear";
    String maxHits = "1";
    String defaultField = "content";
    SearcherWS instance = new SearcherWS();
    String result = instance.search(index, queryString, maxHits, defaultField);
    
    // Get the number of hits from the returned XML
    XmlOptions xmlOpts = new XmlOptions();
    xmlOpts.setCharacterEncoding("UTF-8");
    ResultDocument resultDoc = ResultDocument.Factory.parse(result, xmlOpts);    
    ResultType xmlResult = resultDoc.getResult();
    int totalHits = xmlResult.getTotal();
    
    assertEquals(totalHits, 3);
  }

  /**
   * Test of search method, of class SearcherWS.
   */
  public void testSearchWS() throws Exception {
     
    System.err.println("search webservice");
    String      endpoint = "http://localhost:8080/axis/services/";

    /* This doesn't work
    SimpleAxisServer s = new SimpleAxisServer();
    s.setServerSocket(new ServerSocket(port));
    s.start();
    //AdminClient ad = new AdminClient(true);
    AdminClient.setDefaultConfiguration(s.getMyConfig());
    AdminClient.main(new String[] { "deploy.wsdd" });
    AdminClient ad = new AdminClient();
    
    ad.setDefaultConfiguration(s.getMyConfig());
    ad.setTargetEndpointAddress(new URL(endpoint + "SearcherWS"));
    
    System.out.println(ad.getCall().getService().getServiceName());
    ad.process(new String[] { "-h", "localhost", "-p", "11111", "deploy.wsdd" });
    System.out.println(ad.list());
    */
    String index = "testindex";
    String queryString = "linear";
    String maxHits = "1";
    String defaultField = "content";
    
    Service     service  = new Service();
    Call        call     = (Call) service.createCall();
    
    call.setTargetEndpointAddress(endpoint + "SearcherWS");
    call.setOperationName("search");
                
    String result = 
        (String) call.invoke( new Object[] { 
                  index
                , queryString
                , maxHits
                , defaultField
        } );
    
    //String expResult = "";
    //System.out.println(result);
    //String result = instance.search(index, queryString, maxHits, defaultField);
    //s.stop();
    
    XmlOptions  xmlOpts = new XmlOptions();
    xmlOpts.setCharacterEncoding("UTF-8");
    ResultDocument resultDoc = ResultDocument.Factory.parse(result, xmlOpts);
    
    ResultType xmlResult = resultDoc.getResult();
    int totalHits = xmlResult.getTotal();
    
    assertEquals(totalHits, 3);
  }

}
