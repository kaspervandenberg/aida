/*
 * termFinderWS.java
 *
 */

package org.vle.aid.lucene.tools;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.sandbox.queries.regex.JavaUtilRegexCapabilities;
import org.apache.lucene.sandbox.queries.regex.RegexTermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author emeij
 */
public class termFinderWS {

    private String indexLocation;
    private IndexReader reader;
    private int defaultCount = 10;
    private int maxCount = 100;
    
	private static final int MAX_DOCS = 1000;

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
        File indexLocation = new File(System.getenv("INDEXDIR"));
        
        if (indexLocation == null) {
            log.severe("***INDEXDIR not found!!!***");
            return null;
        } else {
            log.fine("Found INDEXDIR: " + indexLocation);
        }        
        
		
		Directory luceneIndexDir = FSDirectory.open(new File(indexLocation, index));
        reader = DirectoryReader.open(luceneIndexDir);
		
		Fields fields = MultiFields.getFields(reader);
		Terms terms = fields.terms(field);
		RegexTermsEnum termsWithPrefix = new RegexTermsEnum(
				terms.iterator(null),
				new Term(field, String.format("%s.*", prefix)),
				new JavaUtilRegexCapabilities()); 

		int n = 0;
		while (termsWithPrefix.next() != null) {
			BytesRef term = termsWithPrefix.term();
			if (term != null && term.utf8ToString().startsWith(prefix) && n<intCount) {
				result[n] = new String[3];
				result[n][0] = term.utf8ToString();
				result[n][1] = field;
				result[n][2] = Integer.toString(termsWithPrefix.docFreq());
				n++;
			} else {
				break;
			}
		}
			
        return result;
    }
}
