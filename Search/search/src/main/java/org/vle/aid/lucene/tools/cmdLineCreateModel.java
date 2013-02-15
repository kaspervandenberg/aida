/*
 * cmdLineCreateModel.java
 *
 * Created on March 27, 2006, 7:23 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package org.vle.aid.lucene.tools;

import com.aliasi.lm.NGramProcessLM;
import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Streams;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;

import java.io.*;
import java.io.IOException;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author edgar
 */
public class cmdLineCreateModel {

  private static final int NGRAM_LENGTH = 5;
  static final File DATA = new File("data/train");
  private static IndexReader reader = null;
  static final double MATCH_WEIGHT = -0.0;
  static final double DELETE_WEIGHT = -4.0;
  static final double INSERT_WEIGHT = -1.0;
  static final double SUBSTITUTE_WEIGHT = -2.0;
  static final double TRANSPOSE_WEIGHT = -2.0;

  public static void main(String[] args) {

    Date start = new Date();
    System.out.println("Started: " + start.toString());

    File LUCENE_INDEX = new File(args[0]);
    if (LUCENE_INDEX == null || !LUCENE_INDEX.exists() || !LUCENE_INDEX.canRead() || !reader.indexExists(LUCENE_INDEX)) {
      throw new IllegalArgumentException("can't read index dir " + LUCENE_INDEX);
    }

    String field = args[1];

    File MODEL_FILE_DIR = new File(LUCENE_INDEX.toString() +
            System.getProperty("file.separator") +
            "spellCheck");

    // true if created
    if (MODEL_FILE_DIR.mkdir()) {
      System.out.println(MODEL_FILE_DIR.toString() + " succesfully created");
    } else {
      System.out.println(MODEL_FILE_DIR.toString() + " exists");
    }

    File MODEL_FILE = new File(MODEL_FILE_DIR.toString().concat(System.getProperty("file.separator") + "SpellCheck.model"));


    // chekc if modelfile exists. if so, read LM form that file to construct a new NGramProcessLM 
    // Not possible

    try {
      reader = IndexReader.open(LUCENE_INDEX);
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("     CONFIGURATION:");
    System.out.println("     Model File: " + MODEL_FILE);
    System.out.println("     N-gram Length: " + NGRAM_LENGTH);

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

      System.out.println("Started creating Language Model for: " + LUCENE_INDEX);
      System.out.println("Writing model to file=" + MODEL_FILE);

      //for (int i = 0; i < reader.numDocs(); i++) {
      // For speed's sake:
      for (int i = 0; i < 50000; i++) {

        if (i % 5 == 0) {
          System.out.println("currently: " + i + " of 50000");
        }
        //System.out.println("currently: " + i + " of " + reader.numDocs());

        // Sanity check
        if (!reader.isDeleted(i)) {

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
            if (!field_name.equalsIgnoreCase(field)) {
              continue;
            }

            sc.train(field_value);
            sc.pruneTokens(2);
            sc.pruneLM(2);
          }
        }
      }

      writeModel(sc, MODEL_FILE);
      reader.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
    Date end = new Date();
    System.out.print((end.getTime() - start.getTime()) / 1000);
    System.out.println(" seconds.");
    System.out.println("Ended: " + end.toString());
    System.out.println("Done.");

  }

  private static void writeModel(TrainSpellChecker sc, File MODEL_FILE)
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
