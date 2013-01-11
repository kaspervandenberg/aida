/*
 * getWordnetSynWS.java
 *
 * Created on March 23, 2006, 5:23 PM
 *
 */

package org.vle.aid.lucene.tools;

import java.io.IOException;
import java.lang.NullPointerException;

import java.io.*;
import java.io.File;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Iterator;
import java.util.HashSet;
import org.apache.lucene.analysis.WordlistLoader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Searcher;

public class getWordnetSynWS {
    
    /** logger for Commons logging. */
    private transient Logger log =
	 Logger.getLogger("getWordnetSynWS.class.getName()"); 
    
    private String indexLocation;
    private String stopwords;
    private IndexReader reader;
    private IndexSearcher searcher;
    private Analyzer analyzer;
    private Hits hits;
    
    public String[] getWordnetSynonyms (String term) {
        
        if (term == null || term.equalsIgnoreCase("")) {
            log.info("Received an empty String");
            return null;
        }

        log.fine("Checking for env. var. INDEXDIR");
        indexLocation = System.getenv("INDEXDIR");
        
        if (indexLocation == null) {
            log.severe("***INDEXDIR not found!!!***");
            indexLocation = "";
        } else {
            log.fine("Found INDEXDIR: " + indexLocation);
        }
        
        String location = indexLocation + System.getProperty("file.separator") +
                "wordnet" + System.getProperty("file.separator") + "lucene-wordnet-index";
        
        try {        
            if (!reader.indexExists(location)) {
                log.severe("No Wordnet index found: " + location);
            } else {
                log.fine("Wordnet index found: " + location);
                reader = IndexReader.open(location);
            }    
        } catch (NullPointerException e) {
            log.severe("No Wordnet index found: " + location);
            log.severe(e.toString());
        } catch (IOException e) {
            log.severe(e.toString());
        }
        
        stopwords = indexLocation + System.getProperty("file.separator") + "stopwords.txt";
        searcher = new IndexSearcher(reader);  

        try {                
            File stopwordsFile = new File(stopwords);
            if (stopwordsFile == null || !stopwordsFile.exists() || !stopwordsFile.canRead()) {
                // throw new IllegalArgumentException("can't read stopword file " + stopwords);
                log.fine("Can't read default stopwords file: " + stopwords + ", safely ingnoring stopwords");
                analyzer = new StandardAnalyzer();

            } else {
                // No 1.5 on Mac os x...
                // HashSet <String> stopSet = new HashSet <String> ();
                HashSet stopSet = new HashSet();
                stopSet = WordlistLoader.getWordSet(stopwordsFile);


                String[] stopwords = new String[stopSet.size()];
                stopSet.toArray(stopwords);

                analyzer = new StandardAnalyzer(stopwords);                                       
            }

            QueryParser parser = new QueryParser("word", analyzer);
            Query queryTerms = parser.parse(term);
            hits = searcher.search(queryTerms);      
      
            if (hits.length() == 0) {
                log.info("No synonyms found for term '" + term + "'.");
                return null;
            } else {
                return hits.doc(0).getValues("syn");
            }
            
        } catch (ParseException e) {
            log.severe("Ignoring ParseException: " + e.toString());
        } catch(IOException e) {
            log.severe("IOException: " + e.toString());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.severe(sw.toString());
            pw.close();            
        }  
        
        return null;
    }
}
