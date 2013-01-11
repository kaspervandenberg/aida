/*
 * Tokenizer.java
 *
 */

package org.vle.aid.lucene.tools;

import java.util.*;
import java.io.*;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.index.Term;

public class Tokenizer {
  
  public static String[] queryToArray(String in) {
    
    HashSet<String> terms = new HashSet<String>();
    QueryParser qp = new QueryParser("field", new KeywordAnalyzer());
    
    try {
      //Query q = qp.parse(QueryParser.escape(in));
      Query q = qp.parse(in);
      
      //BooleanQuery bq = new BooleanQuery();
      //bq.add(q, BooleanClause.Occur.SHOULD);    
      //      for (BooleanClause t : bq.getClauses())
      //        System.out.println("==" + t.getQuery().toString("field"));
      //WeightedTerm[] qterms = QueryTermExtractor.getTerms(bq, true);
      
      TreeSet<Term> qset = new TreeSet<Term>();
      q.extractTerms(qset);
      for (Term qt : qset) {
        //System.out.println(qt.toString().replaceFirst("field:", ""));
        terms.add(qt.toString().replaceFirst("field:", ""));
      }
      
      /*
      String res = q.toString("field");

      String regex = "\"(.+?)\"";
      Pattern p = Pattern.compile(regex);
      Matcher m = p.matcher(res);
      
      while (m.find())
        terms.add(m.group().replaceAll("\"", ""));
      
      // remove from input string
      res = m.replaceAll("");
      
      for (String t : res.split(" "))
        if (t.length() > 0)
          terms.add(t);
      
      //q.extractTerms(terms);
      */
    } catch (ParseException ignored) { }
    
    return terms.toArray(new String[0]);
  }
  
    /**
     * utility to tokenize an input string into an Array of Strings
     */
    public static String[] wordsToArray(String in) {
        in = stripControlCharacters(in);
        return someWordsToArray(in, in.length() + 1);
    }

    /**
     * utility to tokenize an input string into an Array of Strings - with a maximum # of returned words
     */
    public static String[] someWordsToArray(String s2, int maxR) {
        s2 = stripControlCharacters(s2);
        
        // No 1.5 on Mac os x...
        //Vector <String> words = new Vector <String> ();
        Vector words = new Vector();
        
        String x;
        int count = 0;
        try {
            StreamTokenizer str_tok = new StreamTokenizer(new StringReader(s2));
            str_tok.whitespaceChars('"', '"');
            str_tok.whitespaceChars('\'', '\'');
            str_tok.whitespaceChars('/', '/');
            //str_tok.wordChars(':', ':');
            while (str_tok.nextToken() != StreamTokenizer.TT_EOF) {
                String s;
                switch (str_tok.ttype) {
                    case StreamTokenizer.TT_EOL:
                        s = ""; // we will ignore this
                        break;
                    case StreamTokenizer.TT_WORD:
                        s = str_tok.sval;
                        break;
                    case StreamTokenizer.TT_NUMBER:
                        s = "" + (int) str_tok.nval; // .toString(); // we will ignore this

                        break;
                    default :
                        s = String.valueOf((char) str_tok.ttype);
                }
                if (s.length() < 1)
                    continue;
                //if (s.indexOf("-") > -1) continue;
                //s = s.toLowerCase();
                if (s.endsWith(".")) {
                    // first check for abreviations like "N.J.":
                    int index = s.indexOf(".");
                    if (index < (s.length() - 1)) {
                        words.add(s);
                    } else {
                        words.add(s.substring(0, s.length() - 1));
                        words.add(".");
                    }
                } else if (s.endsWith(",")) {
                    x = s.substring(0, s.length() - 1);
                    if (x.length() > 0) words.add(x);
                    words.add(",");
                } else if (s.endsWith(";")) {
                    x = s.substring(0, s.length() - 1);
                    if (x.length() > 0) words.add(x);
                    words.add(";");
                } else if (s.endsWith("?")) {
                    x = s.substring(0, s.length() - 1);
                    if (x.length() > 0) words.add(x);
                    words.add("?");
                } else if (s.endsWith(":")) {
                    x = s.substring(0, s.length() - 1);
                    if (x.length() > 0) words.add(x);
                    words.add(":");
                } else {
                    words.add(s);
                }
                if (count++ > maxR) break;
            }
        } catch (Exception e) {
        }
        if (words.size() == 0) {
            return new String[0];
        }
        String[] r = new String[words.size()];
        for (int i = 0, size = words.size(); i < size; i++)
            r[i] = (String) words.elementAt(i);
        return r;
    }

    private static String stripControlCharacters(String s) {
        StringBuffer sb = new StringBuffer(s.length() + 1);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch > 256 || ch == '\n' || ch == '\t' || ch == '\r' || ch == 226) {
                sb.append(' ');
                continue;
            }
            //System.out.println(" ch: " + ch + " (int)ch: " + (int)ch + " Character.isISOControl(ch): " + Character.isISOControl(ch));
            if ((int) ch < 129)
                sb.append(ch);
            else
                sb.append(' ');
        }
        return sb.toString();
    }

    public static void main(String[] args) {
      
      
      String input = "The \"good boy\" \"sometimes good boy\"fetched \"bad boy\" \"theirh ball\" and then got a doggie cookie.  � � But the perception is otherwise because a lot of the publicity that we� � � ve gott";
      input="EZH2 +HDAC1 +SMC1 OR +\"Enhancer of Zeste Drosophila Homologue 2\"";
      String[] words = Tokenizer.wordsToArray(input);
      
      System.out.println(input);
      System.out.println();
      
      for (String w : words)
        System.out.println("++ " + w);
      
      words = Tokenizer.queryToArray(input);
        
      for (String w : words)
        System.out.println("-- " + w);      
      
    }

}

