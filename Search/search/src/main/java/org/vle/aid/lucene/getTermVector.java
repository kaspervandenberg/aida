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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.io.File;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


/**
 *
 * @author emeij
 */
public class getTermVector {
	private static final int MAX_RESULTS = 1000;
    
    /** Creates a new instance of getTermVector */
    public getTermVector() {
    }
    
    /** logger for Commons logging. */
    private transient Logger log =
	 Logger.getLogger("getTermVector.class.getName()");  
    
    private IndexReader reader = null;
//    private String[] terms;
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
			File indexLoc = new File(indexLocation, index);
            Directory indexDirectory = FSDirectory.open(indexLoc);
            reader = DirectoryReader.open(indexDirectory);
            IndexSearcher searcher = new IndexSearcher(reader);
            
            // TODO: Get analyzer type from index configfile
            Analyzer analyzer = new StandardAnalyzer(
					Version.LUCENE_41,
					new FileReader(new File(indexLocation, "stopwords.txt")));
            
            
            QueryParser parser = new QueryParser(Version.LUCENE_41, UIfield, analyzer);
            Query queryTerm = parser.parse(UI);
            
            TopDocs result = searcher.search(queryTerm, MAX_RESULTS);
            
            // Check whether given UI exists in index
            if (result.totalHits == 0) {
                log.info("No documents with UI: " + UI + " in index " + index);
                reader.close();
                return null;
            } else {
                
                int doc_id = result.scoreDocs[0].doc;
                Terms tv  = reader.getTermVector(doc_id, field);
				
                ArrayList<String> terms = new ArrayList<String>((int)tv.size());
				TermsEnum iTerms = tv.iterator(null);
                
				while(iTerms.next() != null) {
					terms.add(iTerms.term().utf8ToString());
                    log.info(String.format("TERM: %s", terms.get(terms.size() -1)));
				}

                reader.close();
                return terms.toArray(new String[terms.size()]);
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
        
        log.fine("Checking for env. var. INDEXDIR");
        File indexLocation = new File(System.getenv("INDEXDIR"));
        
        if (indexLocation == null || !indexLocation.exists() || !indexLocation.isDirectory()) {
            log.severe("***INDEXDIR not found!!!***");
            indexLocation = new File("");
        } else {
            log.fine("Found INDEXDIR: " + indexLocation);
        }
        
        try {
            // Open index
            // TODO: get index params from config file
			Directory indexDir = FSDirectory.open(new File(indexLocation, index));
            reader = DirectoryReader.open(indexDir);
            IndexSearcher searcher = new IndexSearcher(reader);
            
            // TODO: Get analyzer type from index configfile
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41, 
					new FileReader(new File(indexLocation, "stopwords.txt")));
            
            QueryParser parser = new QueryParser(Version.LUCENE_41, UIfield, analyzer);
            Query queryTerm = parser.parse(UI);
            
            TopDocs result = searcher.search(queryTerm, MAX_RESULTS);
            
            // Check whether given UI exists in index
            if (result.totalHits == 0) {
                log.info("No documents with UI: " + UI + " in index " + index);
                reader.close();
                return null;
            } else {
                
                int doc_id = result.scoreDocs[0].doc;
                Terms tv  = reader.getTermVector(doc_id, field);
                freqs = new int[tv.getDocCount()];
//                freqs = tv.getTermFrequencies();
				TermsEnum iter = tv.iterator(null);
                
                for (int k = 0; k < freqs.length && iter.next() != null; ++k) {
					freqs[k] = iter.docFreq();
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
