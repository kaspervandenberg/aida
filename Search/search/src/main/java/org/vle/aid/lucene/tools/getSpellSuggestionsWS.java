/*
 * getSpellSuggestionsWS.java
 *
 * Provides synonym extraction and lookup functionality
 * TODO: Check wheter Lucene doc id has already been processed
 * TODO: field uit didyoumean functie DONE
 * TODO: searcher uit didyoumean functie DONE
 * TODO: create overloaded didyoumean, useful 4 debugging DONE
 *
 */
package org.vle.aid.lucene.tools;

import com.aliasi.lm.NGramProcessLM;
import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Streams;

import java.util.logging.Level;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

import java.io.*;
import java.io.IOException;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.Properties;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class getSpellSuggestionsWS {

  /** logger for Commons logging. */
  private transient Logger log =
          Logger.getLogger("spellCheck.class.getName()");
  private File location;
  private File modelFile;

  // Location where indexes are stored. Mind the trailing separator!
  String indexLocation = System.getenv("INDEXDIR") +
          System.getProperty("file.separator");
  IndexReader reader;
  CompiledSpellChecker compiledSC;

  // Just for DEBUG:
  static final StandardAnalyzer LUCENE_TOKENIZER = new StandardAnalyzer(Version.LUCENE_41);
  static final double MATCH_WEIGHT = -0.0;
  static final double DELETE_WEIGHT = -2.0;
  static final double INSERT_WEIGHT = -1.0;
  static final double SUBSTITUTE_WEIGHT = -2.0;
  static final double TRANSPOSE_WEIGHT = -2.0;
  private static final int NGRAM_LENGTH = 5;
  private static final int MAX_DOCS = 1000;
  RecordManager Lucenerecman;
  HTree Lucenehashtable;

  /**
   * Opens JDBM to keep track of already scanned doc's
   * at location String index/spellcheck/doc_list
   *
   * @param String index
   * @return
   */
  private void openJDBMdocID(String index)
          throws IOException {

    location = new File(indexLocation.concat(index).concat(System.getProperty("file.separator") + "spellCheck"));

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
   * Opens index at String location
   *
   * @param String location
   * @return boolean successful
   */
  private boolean openIndex(File location) {
		try {
			if (indexLocation == null) {
			  log.severe("***INDEXDIR not found!!!***");
			  indexLocation = "";
			} else {
			  log.fine("Found INDEXDIR: " + indexLocation);
			}

			Directory indexDir = FSDirectory.open(location);
			if (!DirectoryReader.indexExists(indexDir)) {
			  log.severe("No index: " + location.toString());
			  return false;
			} else {
			  log.fine("Index found: " + location.toString());
			  try {
				reader = DirectoryReader.open(indexDir);
			  } catch (IOException e) {
				log.severe(e.toString());
				return false;
			  }

			  return true;
			}
		} catch (IOException ex) {
			Logger.getLogger(getSpellSuggestionsWS.class.getName()).log(
					Level.SEVERE,
					String.format("Error opening index %s", location),
					ex);
			return false;
		}
  }

  /**
   * Iterates through all fields of all documents in the (Lucene) index
   * for a given field and generates a new Lucene index, and a compiled
   * Language Model file. Returns a String which contains the outcome of
   * the process.
   *
   * @param index index to use
   * @param field field to search
   * @return String
   */
  public String createModel(String index, String field) {

    if (!openIndex(new File(indexLocation.concat(index)))) {
      return null;
    }

    /* Not possible in the current implementation
    try {
    openJDBMdocID(index);
    } catch (IOException e) {
    log.severe("IOException: " + e);
    return null;
    }
     */

    location = new File(indexLocation.concat(index).concat(System.getProperty("file.separator") + "spellCheck"));
    modelFile = new File(location.toString().concat(System.getProperty("file.separator") + "SpellCheck.model"));

    // true if created
    if (location.mkdir()) {
      log.info(location.toString() + " succesfully created");
    } else {
      log.info(location.toString() + " exists");
    }

    log.info("N-gram Length: " + NGRAM_LENGTH);

    FixedWeightEditDistance fixedEdit =
            new FixedWeightEditDistance(MATCH_WEIGHT,
            DELETE_WEIGHT,
            INSERT_WEIGHT,
            SUBSTITUTE_WEIGHT,
            TRANSPOSE_WEIGHT);

    NGramProcessLM lm = new NGramProcessLM(NGRAM_LENGTH);
    TokenizerFactory tokenizerFactory = new IndoEuropeanTokenizerFactory();
    TrainSpellChecker sc = new TrainSpellChecker(lm, fixedEdit, tokenizerFactory);

    try {

      log.info("Started creating Language Model for: " + index);
      log.info("Writing model to file=" + modelFile);

      //for (int i = 0; i < reader.numDocs(); i++) {
      for (int i = 0; i < 100; i++) {
        if (i % 5 == 0) {
          log.info("currently: " + i + " of " + reader.numDocs());
        }

        // TODO: Check wheter Lucene doc id has already been processed
        // Not possible in the current implementation


        // Sanity check
		org.apache.lucene.util.Bits liveDocs = MultiFields.getLiveDocs(reader);
		if (liveDocs.get(i)) {

          // Not possible in the current implementation
          // String done = (String) Lucenehashtable.get(String.valueOf(i));

          if (true) {
            Document doc = reader.document(i);
            List<IndexableField> fields = doc.getFields();

            for (Iterator<IndexableField> it = fields.iterator(); it.hasNext();) {
              IndexableField f = it.next();
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
              if (!field_name.equalsIgnoreCase(field)) {
                continue;
              }

              sc.train(field_value);
              sc.pruneTokens(2);
              sc.pruneLM(2);
              writeModel(sc, modelFile);
            }

          // Store that this doc has already been done
          // Not possible in the current implementation
          // Lucenehashtable.put(String.valueOf(i), "true");
          // Lucenerecman.commit();
          }
        }
      }

      // Not possible in the current implementation
      // Lucenerecman.close();
      reader.close();

    } catch (IOException e) {
      log.severe(e.toString());
      return e.toString();
    } finally {

    }

    return "Done with index " + index;
  }

  public String didyoumean(String index, String term) {

    if (!openIndex(new File(indexLocation.concat(index)))) {
      return null;
    }

    location = new File(indexLocation.concat(index).concat(System.getProperty("file.separator") + "spellCheck"));
    modelFile = new File(location.toString().concat(System.getProperty("file.separator") + "SpellCheck.model"));

    // true if created
    if (modelFile.exists()) {
      log.fine("Reading model from file=" + modelFile);
    } else {
      log.severe(modelFile + " doesn't exist");
      return null;
    }

    String bestAlternative;

    try {
      // try to read compiled model from model file
      compiledSC = readModel(modelFile);

      // compute alternative spelling
      bestAlternative = compiledSC.didYouMean(term);

      if (bestAlternative.equals(term)) {
        log.info("No spelling correction found for term: '" + term + "' in " + modelFile);
        return null;
      } else {
        log.info("Found suggestion '" + bestAlternative + "' for term: '" + term + "' in " + modelFile);
      }

    } catch (Exception e) {
      log.severe(e.toString());
      return e.toString();
    }

    return bestAlternative;
  }

  public String didyoumeanDEBUG(String index, String term, String field) {

    if (!openIndex(new File(indexLocation.concat(index)))) {
      return null;
    }

    location = new File(indexLocation.concat(index).concat(System.getProperty("file.separator") + "spellCheck"));
    modelFile = new File(location.toString().concat(System.getProperty("file.separator") + "SpellCheck.model"));

    // true if created
    if (modelFile.exists()) {
      log.info("Reading model from file=" + modelFile);
    } else {
      log.severe(modelFile + " doesn't exist");
      return null;
    }

    IndexSearcher searcher = new IndexSearcher(reader);
    QueryParser parser = new QueryParser(Version.LUCENE_41, field, LUCENE_TOKENIZER);
    String bestAlternative;

    try {
      // try to read compiled model from model file
      compiledSC = readModel(modelFile);

      Query origQuery = parser.parse(term);
      TopDocs hits = searcher.search(origQuery, MAX_DOCS);
      log.info("Found " + hits.totalHits + " document(s) that matched query '" + term + "':");

      // compute alternative spelling
      bestAlternative = compiledSC.didYouMean(term);

      if (bestAlternative.equals(term)) {
        log.info(" No spelling correction found.");
        return "";
      } else {
        try {
          Query alternativeQuery = parser.parse(bestAlternative);
          TopDocs hits2 = searcher.search(alternativeQuery, NGRAM_LENGTH);
          log.info("Found " + hits2.totalHits + " document(s) matching best alt='" + bestAlternative + "':");

        } catch (ParseException e) {
          log.info("Best alternative not a valid query.");
          return bestAlternative;
        }
      }
    } catch (Exception e) {
      log.severe(e.toString());
      return "Error: " + e.toString();
    }

    return bestAlternative;
  }

  private void writeModel(TrainSpellChecker sc, File MODEL_FILE)
          throws IOException {

    //log.info("opening stream");
    // create object output stream from file
    FileOutputStream fileOut = new FileOutputStream(MODEL_FILE);
    BufferedOutputStream bufOut = new BufferedOutputStream(fileOut);
    ObjectOutputStream objOut = new ObjectOutputStream(bufOut);

    //log.info("calling compileTo");
    // write the spell checker to the file
    sc.compileTo(objOut);

    //log.info("closing");
    // close the resources
    Streams.closeOutputStream(objOut);
    Streams.closeOutputStream(bufOut);
    Streams.closeOutputStream(fileOut);
  }

  private static CompiledSpellChecker readModel(File file)
          throws ClassNotFoundException, IOException {

    // create object input stream from file
    FileInputStream fileIn = new FileInputStream(file);
    BufferedInputStream bufIn = new BufferedInputStream(fileIn);
    ObjectInputStream objIn = new ObjectInputStream(bufIn);

    // read the spell checker
    CompiledSpellChecker sc = (CompiledSpellChecker) objIn.readObject();

    // close the resources and return result
    Streams.closeInputStream(objIn);
    Streams.closeInputStream(bufIn);
    Streams.closeInputStream(fileIn);
    return sc;
  }
}
