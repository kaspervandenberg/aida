/*
 * termFinderWS.java
 *
 */

package org.vle.aid.lucene.tools;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexReader;

/**
 *
 * @author emeij
 */
public class termFinderWS {

    private String indexLocation;
    private IndexReader reader;
    private int defaultCount = 10;
    private int maxCount = 100;
    
    /** logger for Commons logging. */
    private transient Logger log =
	 Logger.getLogger(termFinderWS.class.getName());
	 
    public String getJSNres (String index, String field, String query, String start, String count) {
		if (count.equalsIgnoreCase("1"))
			return new String ("{result:[{'id':'id1','snippet':'snippet1','path':'path1'},{'id':'id2','snippet':'snippet2','path':'path2'}]}");
	    else 
			return new String ("{result:[{'id':'2id1','snippet':'2snippet1','path':'2path1'},{'id':'2id2','snippet':'2snippet2','path':'2path2'}]}");
	}
    
    public String[][] getTerms(String index, String field, String prefix, String count) 
            throws IOException {
    
        int intCount;
        String[][] result;
        int numberOfTerms;
        
        // Check value of maxHits
        try { intCount  = Integer.parseInt(count); }
            catch (NumberFormatException e) { intCount = -1; }
        
        if (intCount <= 0 || intCount > maxCount)
            intCount = defaultCount;
        
        result = new String[intCount][];
        
        log.fine("Checking for env. var. INDEXDIR");
        String indexLocation = System.getenv("INDEXDIR");
        
        if (indexLocation == null) {
            log.severe("***INDEXDIR not found!!!***");
            return null;
        } else {
            log.fine("Found INDEXDIR: " + indexLocation);
        }        
        
        reader = IndexReader.open(indexLocation + System.getProperty("file.separator") + index);
        TermEnum enumerator = reader.terms(new Term(field, prefix));
        
        try {
            String prefixText = prefix;
            String prefixField = field;
            int n = 0;
            do {
                Term term = enumerator.term();
                if (term != null && term.text().startsWith(prefix) && n<intCount) {
                    result[n] = new String[3];
                    result[n][0] = term.text();
                    result[n][1] = term.field();
                    result[n][2] = Integer.toString(enumerator.docFreq());
                    n++;
                } else {
                    break;
                }
            } while (enumerator.next());
        } finally {
          enumerator.close();
        }
    
        return result;
    }
}
