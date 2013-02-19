/*
 * Snippet.java
 *
 * Created on April 3, 2007, 9:38 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.lucene.tools;

import java.io.*;

import java.util.logging.Level;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.highlight.*;

import java.util.logging.Logger;
import org.apache.lucene.util.Version;

/**
 *
 * @author emeij
 */
public class Snippet {
  
  private IndexSearcher searcher;
  private Analyzer analyzer;
  private Query query;
  private String FIELD_NAME = "content";
  
  /** logger for Commons logging. */
  private transient Logger log =
       Logger.getLogger("Snippet.class.getName()");
  
  /** Creates a new instance of Snippet */
  public Snippet(String q, IndexSearcher s, Analyzer a) 
      throws IOException {
    this(q, s, a, "content");
  }
  
  public Snippet(String q, IndexSearcher s, Analyzer a, String field) 
      throws IOException {
    this.searcher = s;
    this.analyzer = a;
    this.FIELD_NAME = field;
    
    QueryParser parser = new QueryParser(Version.LUCENE_41, field, a);
    
    try {
      this.query = parser.parse(q);
    } catch (org.apache.lucene.queryparser.classic.ParseException ex) {
      ex.printStackTrace();
    }
    
    this.query = this.query.rewrite(searcher.getIndexReader()); //required to expand search terms
    
  }
  
  public Snippet(Query q, IndexSearcher s, Analyzer a, String field) 
      throws IOException {
    
    this.searcher = s;
    this.analyzer = a;
    this.FIELD_NAME = field;
    this.query = q;
  }

  public void setFIELD_NAME(String FIELD_NAME) {
    this.FIELD_NAME = FIELD_NAME;
  }
  
  
  public String getSnippet (String text) 
      throws IOException { 
  
    Highlighter highlighter = new Highlighter(new QueryScorer(query));
    TokenStream tokenStream = analyzer.tokenStream(FIELD_NAME, new StringReader(text));
    
    // Return 2 best fragments and seperate with a "..."
	try {
	    String result = highlighter.getBestFragments(tokenStream, text, 2, "...");
    	return result;
	} catch (InvalidTokenOffsetsException ex) {
		log.log(Level.WARNING, "Invallid token offset, when highlighting search results.", ex);
		return text;
	}
  }
}
