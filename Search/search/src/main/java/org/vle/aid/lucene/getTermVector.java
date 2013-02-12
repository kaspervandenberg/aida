/*
 * getTermVector.java
 *
 * Created on February 9, 2006, 11:04 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.vle.aid.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;

import java.io.File;

import java.util.*;
import java.util.Iterator;
import java.util.logging.Logger;


/**
 *
 * @author emeij
 */
public class getTermVector {
    
    /** Creates a new instance of getTermVector */
    public getTermVector() {
    }
    
    /** logger for Commons logging. */
    private transient Logger log =
	 Logger.getLogger("getTermVector.class.getName()");  
    
    private IndexReader reader = null;
    private String[] terms;
    private int[] freqs;
    
    // TODO: implement getTermPositions(int index)
    
    /**
     * Get all terms from Lucenefield field, with document_ID UI (currently PMID)
     * for a given index.
     * @param UI
     * @param field
     * @param index
     * @return Array of terms
    */
    public String[] getTerms(String UI, String field, String index) {

        // TODO: get ID field from convention or config file
        String UIfield = "PMID";
        TermFreqVector tv = null;
        
        log.fine("Checking for env. var. INDEXDIR");
        String indexLocation = System.getenv("INDEXDIR");
        
        if (indexLocation == null) {
            log.severe("***INDEXDIR not found!!!***");
            indexLocation = "";
        } else {
            log.fine("Found INDEXDIR: " + indexLocation);
        }
        
        try {
            // Open index
            // TODO: get index params from config file
            
            reader = IndexReader.open(indexLocation + System.getProperty("file.separator") + index);
            IndexSearcher searcher = new IndexSearcher(reader);
            
            // TODO: Get analyzer type from index configfile
            Analyzer analyzer = new StandardAnalyzer(new File(indexLocation + System.getProperty("file.separator") + "stopwords.txt"));
            
            
            QueryParser parser = new QueryParser(UIfield, analyzer);
            Query queryTerm = parser.parse(UI);
            
            Hits result = searcher.search(queryTerm);
            searcher.close();
            
            // Check whether given UI exists in index
            if (result.length() == 0) {
                log.info("No documents with UI: " + UI + " in index " + index);
                reader.close();
                return null;
            } else {
                
                int doc_id = result.id(0);
                tv  = reader.getTermFreqVector(doc_id, field);
                terms = new String[tv.size()];
                terms = tv.getTerms();
                
                for (int k = 0; k < terms.length; ++k) {
                    // Debugging:
                    log.info("TERM: " + terms[k]);
                }                

                reader.close();
                return terms;
            }
            
            
            
        } catch (Exception e) {
            log.severe("getTermVector: " + e.getMessage());
        }
        
        // Shouldn't get here
        return null;
    }
     
    /**
     * Get all term frequencies from Lucenefield field, with document_ID UI (currently PMID)
     * for a given index.
     * @param UI
     * @param field
     * @param index
     * @return Array of termfrequencies
    */
    public int[] getTermFrequencies(String UI, String field, String index) {

        // TODO: get ID field from convention or config file
        String UIfield = "PMID";
        TermFreqVector tv = null;
        
        log.fine("Checking for env. var. INDEXDIR");
        String indexLocation = System.getenv("INDEXDIR");
        
        if (indexLocation == null) {
            log.severe("***INDEXDIR not found!!!***");
            indexLocation = "";
        } else {
            log.fine("Found INDEXDIR: " + indexLocation);
        }
        
        try {
            // Open index
            // TODO: get index params from config file
            reader = IndexReader.open(indexLocation + System.getProperty("file.separator") + index);
            IndexSearcher searcher = new IndexSearcher(reader);
            
            // TODO: Get analyzer type from index configfile
            Analyzer analyzer = new StandardAnalyzer(new File(indexLocation + System.getProperty("file.separator") + "stopwords.txt"));
            
            QueryParser parser = new QueryParser(UIfield, analyzer);
            Query queryTerm = parser.parse(UI);
            
            Hits result = searcher.search(queryTerm);
            searcher.close();
            
            // Check whether given UI exists in index
            if (result.length() == 0) {
                log.info("No documents with UI: " + UI + " in index " + index);
                reader.close();
                return null;
            } else {
                
                int doc_id = result.id(0);
                tv  = reader.getTermFreqVector(doc_id, field);
                freqs = new int[tv.size()];
                freqs = tv.getTermFrequencies();
                
                for (int k = 0; k < freqs.length; ++k) {
                    // Debugging:
                    log.info("TERMFREQ: " + freqs[k]);
                }                

                reader.close();
                return freqs;
            }
            
            
            
        } catch (Exception e) {
            log.severe("getTermVector: " + e.getMessage());
        }
        
        // Shouldn't get here
        return null;
    }     

    /*
    public Object showTV(String index) {

        ir = IndexReader.open(index);
        Integer DocId = 1; //(Integer) getProperty(table, "docNum");
        
        if (DocId == null) {
            log.severe("Missing Doc. Id. Index: " + index);
            return 0;
        }
        
        try {
            String fName = (String) getProperty(row, "fName");
            TermFreqVector tfv = ir.getTermFreqVector(DocId.intValue(), fName);
            if (tfv == null) {
                showStatus("Term Vector not available.");
            return;
        }
            
      Object dialog = addComponent(null, "/xml/vector.xml", null, null);
      setString(find(dialog, "fld"), "text", fName);
      Object vTable = find(dialog, "vTable");
      IntPair[] tvs = new IntPair[tfv.size()];
      String[] terms = tfv.getTerms();
      int[] freqs = tfv.getTermFrequencies();
      for (int i = 0; i < terms.length; i++) {
        IntPair ip = new IntPair(freqs[i], terms[i]);
        tvs[i] = ip;
      }
      Arrays.sort(tvs, new IntPair.PairComparator(false, true));
      for (int i = 0; i < tvs.length; i++) {
        Object r = create("row");
        add(vTable, r);
        Object cell = create("cell");
        setString(cell, "text", String.valueOf(tvs[i].cnt));
        add(r, cell);
        cell = create("cell");
        setString(cell, "text", tvs[i].text);
        add(r, cell);
      }
      add(dialog);
    } catch (Exception e) {
      e.printStackTrace();
      showStatus(e.getMessage());
    }
  }    
*/
    
}
