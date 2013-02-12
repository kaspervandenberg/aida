/*
 * getSynonymsWS.java
 *
 * Provides synonym extraction and lookup functionality
 * TODO: Check wheter Lucene doc id has already been processed
 * TODO: Select Wordnet index
 * TODO: Implement allDigits()
 * TODO:
 *
 */
package org.vle.aid.lucene.tools;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.IOException;
import java.io.File;
import java.util.*;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.*;
import java.util.Collection;
import java.util.ArrayList;
import java.text.StringCharacterIterator;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 *
 * @author Edgar Meij, 2006
 */
public class getSynonymsWS {

  /** logger for Commons logging. */
  private transient Logger log =
          Logger.getLogger("synonym.class.getName()");
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
  File location;
  String indexLocation = System.getenv("INDEXDIR") +
          System.getProperty("file.separator");
  RecordManager recman;
  HTree hashtable;
  RecordManager Lucenerecman;
  HTree Lucenehashtable;
  IndexReader reader;
  String key;
  int alreadyStored;
  int newAcronyms;

  /**
   * Opens index at String location
   *
   * @param location location to open
   * @return boolean successful
   */
  private boolean openIndex(File location) {

    if (indexLocation == null) {
      log.severe("***INDEXDIR not found!!!***");
      indexLocation = "";
    } else {
      log.fine("Found INDEXDIR: " + indexLocation);
    }

    if (!reader.indexExists(location)) {
      log.severe("No index: " + location.toString());
      return false;
    } else {
      log.fine("Index found: " + location.toString());
      try {
        reader = IndexReader.open(location);
      } catch (IOException e) {
        log.severe(e.toString());
        return false;
      }

      return true;
    }
  }

  /**
   * Opens JDBM at location String index/synonyms/acro
   *
   * @param index
   * @return
   */
  private void openJDBM(String index)
          throws IOException {

    location = new File(indexLocation.concat(index).concat(System.getProperty("file.separator") + "synonyms"));

    // true if created
    if (location.mkdir()) {
      log.fine(location.toString() + " succesfully created");
    } else {
      log.fine(location.toString() + " exists");
    }

    // create or open index collection manager
    Properties props = new Properties();
    // props.setProperty("fileName", "");

    recman = RecordManagerFactory.createRecordManager(location.toString() +
            System.getProperty("file.separator") + "acro", props);

    // create or load index synonyms (hashtable)
    long recid = recman.getNamedObject("syn");
    if (recid != 0) {
      log.fine("Reloading existing list, index: " + index);
      hashtable = HTree.load(recman, recid);
    } else {
      log.fine("Creating new list, index: " + index);
      hashtable = HTree.createInstance(recman);
      recman.setNamedObject("syn", hashtable.getRecid());
    }

  }

  /**
   * Opens JDBM to keep track of already scanned doc's
   * at location String index/synonyms/doc_list
   *
   * @param index index to use
   * @return
   */
  private void openJDBMdocID(String index)
          throws IOException {

    location = new File(indexLocation.concat(index).concat(System.getProperty("file.separator") + "synonyms"));

    // true if created
    if (location.mkdir()) {
      log.fine(location.toString() + " succesfully created");
    } else {
      log.fine(location.toString() + " exists");
    }

    Properties props = new Properties();
    Lucenerecman = RecordManagerFactory.createRecordManager(location.toString() +
            System.getProperty("file.separator") + "doc_id", props);

    // create or load index synonyms (hashtable)
    long recid = Lucenerecman.getNamedObject("doc_id");
    if (recid != 0) {
      log.fine("Reloading existing list, index: " + index);
      Lucenehashtable = HTree.load(Lucenerecman, recid);
    } else {
      log.fine("Creating new list, index: " + index);
      Lucenehashtable = HTree.createInstance(Lucenerecman);
      Lucenerecman.setNamedObject("doc_id", Lucenehashtable.getRecid());
    }

  }

  /**
   * Iterates through all fields of all documents in the (Lucene) index.
   * Returns a String which contains the outcome of the process.
   *
   * @param index index to use
   * @return String
   */
  public String updateSynonyms(String index)
          throws IOException {

    if (!openIndex(new File(indexLocation.concat(index)))) {
      return null;
    }

    openJDBM(index);
    openJDBMdocID(index);

    log.info("Starting processing " + index);

    for (int i = 0; i < reader.numDocs(); i++) {
      // for (int i = 0; i < 10000; i++) {
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
        }
      }
    }

    reader.close();
    recman.close();
    Lucenerecman.close();

    return "Done. Added " + newAcronyms / 2 + " new synonyms. " +
            alreadyStored / 2 + " were already stored for index: " + index;
  }

  /**
   * Gives an Array of synonyms for a given term and index.
   *
   * @param index index to use
   * @param term term to search
   * @return Array of Strings containing synonyms.
   */
  public String[] getSynonyms(String index, String term) {

    if (term.equals("") || term == null) {
      return null;
    }

    if (!openIndex(new File(indexLocation.concat(index)))) {
      return null;
    }

    try {
      openJDBM(index);
      String[] content = (String[]) hashtable.get(term);
      if (content == null) {
        log.info("Nothing found for term: " + term + " and index: " + index);
      } else {
        log.info("Found " + content.length + " synonyms for term: " + term + " and index: " + index);
      }
      return content;
    } catch (IOException e) {
      String[] error = {e.toString()};
      log.severe(e.toString());
      return error;
    }
  }

  /**
   * Returns an array of Strings containing all stored synonyms for a given index.
   *
   * @param index index to use
   * @return Array of Strings containing all stored synonyms.
   */
  public String[] listSynonyms(String index) {

    // No 1.5 on Mac os x...
    //Collection <String> resultList = new ArrayList <String> ();
    Collection resultList = new ArrayList();

    log.info("Listing terms for index: " + index);

    if (!openIndex(new File(indexLocation.concat(index)))) {
      return null;
    }

    try {
      openJDBM(index);

      FastIterator iter = hashtable.keys();
      key = (String) iter.next();

      while (key != null) {
        resultList.add(key);
        key = (String) iter.next();
      }

      String[] arrayResultList = (String[]) resultList.toArray(new String[0]);

      return (String[]) resultList.toArray(new String[0]);

    } catch (IOException e) {
      String[] error = {e.toString()};
      return error;
    }

  }

  /**
   * Returns an XML document containing all stored synonyms for a given index.
   *
   * @param index
   * @return XML file containing all stored synonyms.
   */
  public String listSynonymsXML(String index)
          throws IOException {

    log.info("Prepping XML, index: " + index);
    // No 1.5 on Mac os x...
    // Collection <String>  resultList = new ArrayList <String> ();
    Collection resultList = new ArrayList();

    Calendar cal = Calendar.getInstance(TimeZone.getDefault());
    String DATE_FORMAT = "dd MMMMM yyyy, HH:mm:ss";
    java.text.SimpleDateFormat sdf =
            new java.text.SimpleDateFormat(DATE_FORMAT);
    sdf.setTimeZone(TimeZone.getDefault());

    String output = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<?xml-stylesheet href=\"listSynonymsXML.xsl\" type=\"text/xsl\"?>" +
            "\n<result " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            // "\txsi:schemaLocation=\"http://www.science.uva.nl/~emeij/\"" +
            // "aidLuceneResults.xsd\n" +
            "\txmlns=\"http://www.mgj.org/aid\"\n" +
            "\tindex=\"" + index + "\"\n" +
            "\tdate=\"" + sdf.format(cal.getTime()) + "\">\n";

    if (!openIndex(new File(indexLocation.concat(index)))) {
      return null;
    }

    openJDBM(index);

    FastIterator iter = hashtable.keys();
    key = (String) iter.next();

    while (key != null) {
      // resultList.add(key);
      String[] lookupSynonym = (String[]) hashtable.get(key);

      if (lookupSynonym.length > 0 && lookupSynonym != null && lookupSynonym[0] != null) {
        for (int j = 0; j < lookupSynonym.length; j++) {
          output += "\t<synonym term=\"" +
                  stringTermFilter(key) + "\">" +
                  stringTermFilter(lookupSynonym[j]) +
                  "</synonym>\n";
        }
      }
      key = (String) iter.next();
    }

    //String[] arrayResultList = (String[]) resultList.toArray(new String[0]);

    String[] arrayResultList = new String[resultList.size()];
    resultList.toArray(arrayResultList);
    output += "</result>\n";

    return output;

  }

  /**
   * Deletes a given term from the synonym list for a given index.
   * Returns true if deletion was succesful, false otherwise.
   *
   * @param index index to use
   * @param term term to delete
   * @return String deletion status
   */
  public String deleteSynonym(String index, String term)
          throws IOException {

    if (term.equals("") || term == null) {
      return "Fill in a term. ";
    }

    log.info("Deleting term \"" + term + "\" from index: " + index);

    String output = "";

    if (!openIndex(new File(indexLocation.concat(index)))) {
      return "Unable to open index: " + index;
    }

    openJDBM(index);

    // Check whether term exists for given index
    String[] lookupSynonym = (String[]) hashtable.get(term);

    if (lookupSynonym == null) {
      output += "No term found to delete: " + term;
    } else {

      hashtable.remove(term);
      recman.commit();
      output += "Deleted: " + term + ". ";

      for (int m = 0; m < lookupSynonym.length; m++) {

        log.info("Looking up: " + lookupSynonym[m]);
        String[] invertedSynonyms = (String[]) hashtable.get(lookupSynonym[m]);
        hashtable.remove(lookupSynonym[m]);
        recman.commit();
        output += "Deleted: " + lookupSynonym[m] + ". ";


        if (invertedSynonyms.length != 1) {
          for (int k = 0; k < invertedSynonyms.length; k++) {

            if (invertedSynonyms[k].equalsIgnoreCase(term)) {

              String[] tempArray = new String[invertedSynonyms.length - 1];

              System.arraycopy(invertedSynonyms, 0, tempArray, 0, k);

              System.arraycopy(invertedSynonyms, k + 1, tempArray, k,
                      invertedSynonyms.length - k - 1);

              hashtable.put(lookupSynonym[m], tempArray);
              recman.commit();
              break;
            }
          }
        } else { // 1 on 1 relation, must specify separately
          hashtable.remove(invertedSynonyms[0]);
          recman.commit();
        }
      }
    }

    recman.close();

    return output;
  }

  /**
   * Adds a given term to the synonym list for a given index.
   * Returns true if addition was succesful, false otherwise.
   *
   * @param index index to use
   * @param term term to add
   * @param synonym its synonym
   * @return String addition status
   */
  public String addSynonym(String index, String term, String synonym)
          throws IOException {

    if (term.equals("") || term == null) {
      return null;
    }
    if (synonym.equals("") || synonym == null) {
      return null;
    }

    log.info("Adding term \"" + term + "\", with synonym \"" +
            synonym + "\" to index: " + index);

    if (!openIndex(new File(indexLocation.concat(index)))) {
      return "Unable to open index " + index;
    }

    openJDBM(index);

    storeSynonym(term.toLowerCase(), synonym.toLowerCase());
    storeSynonym(synonym.toLowerCase(), term.toLowerCase());

    return "Done.";
  }

  /**
   * Extracts acronyms from the input String and stores them using storeSynonym
   *
   * @param input
   * @return
   */
  private void extractSynonyms(String input)
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
          log.info("Difficulty extracting acronym: " + acronym);
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
  private void storeSynonym(String acronym, String definition)
          throws IOException {

    String[] lookupSynonym = (String[]) hashtable.get(acronym);
    boolean foundIt = false;

    if (lookupSynonym == null) {
      log.info("No synonyms found, creating new entry for: " + definition);
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
        log.info("\"" + definition + "\" - \"" + acronym + "\" already stored or duplicates.");
        alreadyStored++;
      } else {
        log.info("Appending entry: \"" + definition + "\" - \"" + acronym + "\"");
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
  private boolean checkSynonym(String acronym, String definition) {

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
