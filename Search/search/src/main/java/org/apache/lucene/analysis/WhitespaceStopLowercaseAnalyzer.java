package org.apache.lucene.analysis;
import java.io.Reader;
import org.apache.lucene.analysis.WordlistLoader; 

/** An Analyzer that uses WhitespaceTokenizer. */

import java.io.File;
import java.io.IOException;
import java.util.Set;
                                                                              
/** Filters LetterTokenizer with LowerCaseFilter and StopFilter. */

public final class WhitespaceStopLowercaseAnalyzer extends Analyzer {
 private Set<String> stopWords;

 /** An array containing some common English words that are not usually useful
   for searching. */
 public static final String[] ENGLISH_STOP_WORDS = {
   "a", "an", "and", "are", "as", "at", "be", "but", "by",
   "for", "if", "in", "into", "is", "it",
   "no", "not", "of", "on", "or", "s", "such",
   "t", "that", "the", "their", "then", "there", "these",
   "they", "this", "to", "was", "will", "with"
 };

 /** Builds an analyzer which removes words in ENGLISH_STOP_WORDS. */
 public WhitespaceStopLowercaseAnalyzer() {
   stopWords = StopFilter.makeStopSet(ENGLISH_STOP_WORDS);
 }

 /** Builds an analyzer with the stop words from the given set.
  */
 public WhitespaceStopLowercaseAnalyzer(Set<String> stopWords) {
   this.stopWords = stopWords;
 }

 /** Builds an analyzer which removes words in the provided array. */
 public WhitespaceStopLowercaseAnalyzer(String[] stopWords) {
   this.stopWords = StopFilter.makeStopSet(stopWords);
 }
 
 /** Builds an analyzer with the stop words from the given file.
  * @see WordlistLoader#getWordSet(File)
  */
 public WhitespaceStopLowercaseAnalyzer(File stopwordsFile) throws IOException {
   stopWords = WordlistLoader.getWordSet(stopwordsFile);
 }

 /** Filters LowerCaseTokenizer with StopFilter. */
 public TokenStream tokenStream(String fieldName, Reader reader) {
    TokenStream result = new WhitespaceTokenizer(reader);
    result = new LowerCaseFilter(result);
    result = new StopFilter(result, stopWords);
    return result;
 }
}