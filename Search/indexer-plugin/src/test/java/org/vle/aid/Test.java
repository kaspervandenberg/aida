package org.vle.aid;

import java.util.Iterator;
import java.util.List;

import indexer.ConfigurationHandler;
import indexer.DocHandler;
import indexer.HandlerFactory;
import indexer.Indexer;
import junit.framework.*;

public class Test extends TestCase {
	
  public static void main (String[] args) {
    junit.textui.TestRunner.run (suite());
  }
  
  public static junit.framework.Test suite() {
    return new TestSuite(Test.class);
  }

	public void testIndexer () {
		
		String resultString = "";
		
		try {
			String configfile = "indexconfig.xml";
	    ConfigurationHandler ch = new ConfigurationHandler(configfile);
	    Indexer i = new Indexer();
	    
	    List<String> docTypes = ch.getDocumentTypes();	    
	    HandlerFactory hf = new HandlerFactory(ch);
	    
	    // Print all *possible* fieldtypes
	    for(Iterator it = docTypes.iterator(); it.hasNext();) {
	    	String docType = (String) it.next();
	    	System.out.print("-- Possible fields for " + docType + "\n--- ");
	    	
	    	DocHandler handler = hf.getHandler(docType);
	    	String[] fields = handler.getFieldNames();
	    	for (int x=0; x<fields.length; x++) {
	    		System.out.print(fields[x] + " ");
	    	}
	    	System.out.println();
	    }
	    
	    System.out.println("-- Using Global Analyzer: " + ch.getGlobalAnalyzer());
	    System.out.println("-- Using Medline Analyzer: " + ch.getDocumentAnalyzer("medline"));
	    System.out.println("-- Defined document types in config file: " + docTypes);
	    
	    // Print all *defined* fieldtypes
	    for(Iterator it = docTypes.iterator(); it.hasNext();) {
	    	String docType = (String) it.next();
	    	System.out.print("-- Defined fields for " + docType + " in config file: \n--- ");
	    	
	    	String[] fields = ch.getFields(docType);
	    	for (int x=0; x<fields.length; x++) {
	    		System.out.print(fields[x] + " ");
	    	}
	    	System.out.println();
	    }
	    
	    
			resultString = i.indexFromCFG(configfile, null, null);//"machiel.jansen", "jacq357", "medline/100.med");
			//String resultString = i.indexSRBData("machiel.jansen", "jacq357", "medline/100.med");
			
			System.out.println(resultString);
		} catch(Exception e) {
			System.out.println("Error in execute! " + e);
		}
		
		assertEquals(resultString, "Indexing finished");
	}
}
