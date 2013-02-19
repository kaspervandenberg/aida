/*
 * getWordnetSynWS.java
 *
 * Created on March 23, 2006, 5:23 PM
 *
 */

package org.vle.aid.lucene.tools;

import java.lang.NullPointerException;

import java.io.*;
import java.io.File;
import java.util.logging.Logger;
import org.apache.lucene.analysis.util.WordlistLoader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class getWordnetSynWS {
    
    /** logger for Commons logging. */
    private transient Logger log =
	 Logger.getLogger("getWordnetSynWS.class.getName()"); 
    
	private static final int MAX_DOCS = 1000;
	
    private String indexLocation;
    private String stopwords;
    private IndexReader reader;
    private IndexSearcher searcher;
    private Analyzer analyzer;
    private TopDocs hits;
    
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
        
        File location = new File(new File(indexLocation,"wordnet"),"lucene-wordnet-index");
        
        try {        
			Directory luceneIndexDir = FSDirectory.open(location);
			if (!DirectoryReader.indexExists(luceneIndexDir)) {
                log.severe("No Wordnet index found: " + location);
            } else {
                log.fine("Wordnet index found: " + location);
                reader = DirectoryReader.open(luceneIndexDir);
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
                analyzer = new StandardAnalyzer(Version.LUCENE_41);

            } else {
                // No 1.5 on Mac os x...
                // HashSet <String> stopSet = new HashSet <String> ();
                CharArraySet stopSet = WordlistLoader.getWordSet(new FileReader(stopwordsFile), Version.LUCENE_41);
                analyzer = new StandardAnalyzer(Version.LUCENE_41, stopSet);                                       
            }

            QueryParser parser = new QueryParser(Version.LUCENE_41, "word", analyzer);
            Query queryTerms = parser.parse(term);
            hits = searcher.search(queryTerms, MAX_DOCS);      
      
            if (hits.totalHits == 0) {
                log.info("No synonyms found for term '" + term + "'.");
                return null;
            } else {
                return searcher.doc(hits.scoreDocs[0].doc).getValues("syn");
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
