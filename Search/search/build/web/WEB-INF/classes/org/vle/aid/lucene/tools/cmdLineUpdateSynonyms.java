/*
 * cmdLineUpdateSynonyms.java
 *
 * Created on March 27, 2006, 9:20 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package org.vle.aid.lucene.tools;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

import java.io.IOException;
import java.io.File;
import java.util.*;
import java.util.Properties;
import java.util.regex.*;
import java.text.StringCharacterIterator;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import java.util.Date;

/**
 *
 * @author edgar
 */
public class cmdLineUpdateSynonyms {

  private static final String ONLY_TEXT_REGEX = "[a-z,A-Z,0-9,-]+";
  private static final String ACRONYM_REGEX = "\\s+\\(([a-z,A-Z,0-9,-]{2,})\\)\\s+";
  private static final String SENTENCE_REGEX = "(\\.\\w+)";
  private static final String WHITESPACE_REGEX = "\\s+";
  private static final Pattern onlyTextPattern = Pattern.compile(ONLY_TEXT_REGEX);
  private static final Pattern acronymPattern = Pattern.compile(ACRONYM_REGEX);
  private static final Pattern sentencePattern = Pattern.compile(SENTENCE_REGEX);
  private static final Pattern whitespacePattern = Pattern.compile(WHITESPACE_REGEX);
  private static Matcher acronymMatcher;
  private static Matcher textMatcher;
  private static Matcher whitespaceMatcher;
  static File location;
  static RecordManager recman;
  static HTree hashtable;
  static RecordManager Lucenerecman;
  static HTree Lucenehashtable;
  static IndexReader reader = null;
  static String key;
  static int alreadyStored;
  static int newAcronyms;

  public static void main(String[] args) {

    Date start = new Date();
    System.out.println("Started: " + start.toString());

    File index = new File(args[0]);
    if (index == null || !index.exists() || !index.canRead() || !reader.indexExists(index)) {
      throw new IllegalArgumentException("can't read index dir " + index);
    }

    try {
      reader = IndexReader.open(index);
      openJDBM(index.toString());
      openJDBMdocID(index.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("Starting processing " + index);
    try {
      for (int i = 0; i < reader.numDocs(); i++) {

        if (i % 5 == 0) {
          System.out.println("currently: " + i + " of " + reader.numDocs());
        }

        // Sanity check
        if (!reader.isDeleted(i)) {

          String done = (String) Lucenehashtable.get(String.valueOf(i));

          if (done == null) {

            Document doc = reader.document(i);
            List<Field> fields = doc.getFields();

            for (Iterator<Field> it = fields.iterator(); it.hasNext();) {
              Field f = it.next();
              String field_name = f.name();
              String field_value = f.stringValue();

              // skip empty names or values. should never happen. 
              if (field_name == null) {
                continue;
              }
              if (field_value == null) {
                continue;
              }
              if (field_name.equals("")) {
                continue;
              }
              if (field_value.equals("")) {
                continue;
              }

              extractSynonyms(field_value);

              // Store that this doc has already been done
              Lucenehashtable.put(String.valueOf(i), "true");
              Lucenerecman.commit();
            }
          } else {
            if (i % 5 == 0) {
              System.out.println(i + " already scanned");
            }
          }
        }
      }

      reader.close();
      recman.close();
      Lucenerecman.close();

    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("Done. Added " + newAcronyms / 2 + " new synonyms. " +
            alreadyStored / 2 + " were already stored for index: " + index);

    Date end = new Date();
    System.out.print((end.getTime() - start.getTime()) / 1000);
    System.out.println(" seconds.");
    System.out.println("Ended: " + end.toString());
    System.out.println("Done.");

  }

  /**
   * Opens JDBM at location String index/synonyms/acro
   *
   * @param 
   * @return 
   */
  private static void openJDBM(String indexDir)
          throws IOException {

    location = new File(indexDir.concat(System.getProperty("file.separator") + "synonyms"));

    // true if created
    if (location.mkdir()) {
      System.out.println(location.toString() + " succesfully created");
    } else {
      System.out.println(location.toString() + " exists");
    }

    // create or open index collection manager
    Properties props = new Properties();
    // props.setProperty("fileName", "");

    recman = RecordManagerFactory.createRecordManager(location.toString() +
            System.getProperty("file.separator") + "acro", props);

    // create or load index synonyms (hashtable)
    long recid = recman.getNamedObject("syn");
    if (recid != 0) {
      System.out.println("Reloading existing list, index: " + indexDir);
      hashtable = HTree.load(recman, recid);
    } else {
      System.out.println("Creating new list, index: " + indexDir);
      hashtable = HTree.createInstance(recman);
      recman.setNamedObject("syn", hashtable.getRecid());
    }

  }

  /**
   * Opens JDBM to keep track of already scanned doc's 
   * at location String index/synonyms/doc_list
   *
   * @param String index
   * @return 
   */
  private static void openJDBMdocID(String index)
          throws IOException {

    location = new File(index.concat(System.getProperty("file.separator") + "synonyms"));

    // true if created
    if (location.mkdir()) {
      System.out.println(location.toString() + " succesfully created");
    } else {
      System.out.println(location.toString() + " exists");
    }

    Properties props = new Properties();
    Lucenerecman = RecordManagerFactory.createRecordManager(location.toString() +
            System.getProperty("file.separator") + "doc_id", props);

    // create or load index synonyms (hashtable)
    long recid = Lucenerecman.getNamedObject("doc_id");
    if (recid != 0) {
      System.out.println("Reloading existing list, index: " + index);
      Lucenehashtable = HTree.load(Lucenerecman, recid);
    } else {
      System.out.println("Creating new list, index: " + index);
      Lucenehashtable = HTree.createInstance(Lucenerecman);
      Lucenerecman.setNamedObject("doc_id", Lucenehashtable.getRecid());
    }

  }

  /**
   * Extracts acronyms from the input String and stores them using storeSynonym
   *
   * @param String input
   * @return 
   */
  private static void extractSynonyms(String input)
          throws IOException {

    String[] sentence = sentencePattern.split(input);

    for (int j = 0; j < sentence.length; j++) {

      String[] item = acronymPattern.split(sentence[j]);
      acronymMatcher = acronymPattern.matcher(sentence[j]);

      int k = 0;

      while (acronymMatcher.find()) {
        String acronym = acronymMatcher.group();
        String trueAcronym = "";
        textMatcher = onlyTextPattern.matcher(acronym);

        if (textMatcher.find()) {

          trueAcronym = textMatcher.group();
          whitespaceMatcher = whitespacePattern.matcher(item[k]);
          String definition = whitespaceMatcher.replaceAll(" ");

          if (checkSynonym(trueAcronym, definition)) {
            storeSynonym(trueAcronym.toLowerCase(), definition.toLowerCase());
            storeSynonym(definition.toLowerCase(), trueAcronym.toLowerCase());
          }

        } else {
          System.out.println("Difficulty extracting acronym: " + acronym);
        }

        k++;
      } // Next acronym in this sentence
    } // Next sentence
  }

  /**
   * 
   * @param 
   * @return 
   */
  private static void storeSynonym(String acronym, String definition)
          throws IOException {

    String[] lookupSynonym = (String[]) hashtable.get(acronym);
    boolean foundIt = false;

    if (lookupSynonym == null) {
      System.out.println("No synonyms found, creating new entry for: " + definition);
      String[] toStore = {definition};
      hashtable.put(acronym, toStore);
      recman.commit();
      newAcronyms++;
    } else {
      // Check for duplicates
      for (int m = 0; m < lookupSynonym.length; m++) {
        if (lookupSynonym[m].equals(definition)) {
          foundIt = true;
          break;
        }
      }

      if (foundIt) {
        System.out.println("\"" + definition + "\" - \"" + acronym + "\" already stored or duplicates.");
        alreadyStored++;
      } else {
        System.out.println("Appending entry: \"" + definition + "\" - \"" + acronym + "\"");
        String[] toStore = new String[lookupSynonym.length + 1];
        System.arraycopy(lookupSynonym, 0, toStore, 0, lookupSynonym.length);
        toStore[lookupSynonym.length] = definition;
        hashtable.put(acronym, toStore);
        recman.commit();
        newAcronyms++;
      }

    }
  }

  /**
   * The actual acronym extraction algorithm
   *
   * @param 
   * @return 
   */
  private static boolean checkSynonym(String acronym, String definition) {

    // Very naive, should perform more thorough checks.
    // And extract proper definitions

    if (definition.length() > 0 && acronym.length() > 0 && definition.length() < 30) {
      // log.info("1st letter ACRO: " + trueAcronym.substring(0,1));
      // log.info("1st letter DEF: " + definition.substring(0,1));
      if (definition.substring(0, 1).equals(acronym.substring(0, 1))) {
        return true;
      // log.info("Found: \"" + definition + "\" - \"" + trueAcronym + "\"");
      }
    }

    return false;
  }

  /**
   * Deletes some special characters from String, for pretty-print nice XML
   * 
   * @param 
   * @return 
   */
  private static String stringTermFilter(String fullSearchString) {

    final StringBuffer result = new StringBuffer();
    final StringCharacterIterator iterator =
            new StringCharacterIterator(fullSearchString);

    char character = iterator.current();
    char previousChar = ' ';

    while (character != StringCharacterIterator.DONE) {

      if (character == '<') {
        result.append("");
      } else if (character == '>') {
        result.append("");
      } else if (character == '\"') {
        result.append("");
      } else if (character == '\\') {
        result.append(" ");
      } else if (character == '/') {
        result.append(" ");
      } else if (character == '&') {
        result.append("");
      } else if (character == '!') {
        result.append("");
      } else if (character == '-') {
        if (previousChar == ' ') {
          result.append("");
        } else {
          result.append(character);
        }
      } else if (character == '*') {
        result.append("");
      } else if (character == '%') {
        result.append("");
      } else if (character == '#') {
        result.append("");
      } else if (character == '@') {
        result.append("");
      } else if (character == '(') {
        result.append("");
      } else if (character == ')') {
        result.append("");
      } else if (character == '"') {
        result.append("");
      } else if (character == '.') {
        result.append("");
      } else {
        //the char is not a special one
        //add it to the result as is
        result.append(character);
      }
      previousChar = character;
      character = iterator.next();
    }

    return result.toString();
  }
}
