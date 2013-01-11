package org.vle.aid.medline;

import com.aliasi.medline.MedlineCitation;
import com.aliasi.medline.MedlineHandler;
import com.aliasi.medline.MedlineParser;
import com.aliasi.util.Files;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import java.util.Date;
import java.text.DateFormat;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class takes a folder and index as input and updates the index
 * according to the update files found in the folder.
 *
 * @author emeij
 */
public class IndexUpdater {

  static String INDEX_DIR;
  static final MedlineParser PARSER = new MedlineParser(true);
  static BufferedCitationIndexer indexer;
  
  private static final String date = DateFormat.getDateInstance(DateFormat.SHORT).format(new Date());

  public static void main(String[] args)
          throws IOException, SAXException {
    
    INDEX_DIR = args[0];
    indexer = new BufferedCitationIndexer(INDEX_DIR);
    //File stopwordFile = new File(args[1]);
    // not used

    for (int i = 1; i < args.length; ++i) { 
      process(args[i]);
    }
    
    System.err.println(" optimizing index");
    optimize();
    System.err.println(" done");

    /*
    for (int i = 1; i < args.length; ++i) {
      System.err.println("Indexing file=" + args[i]);
      if (args[i].endsWith(".xml")) {
        indexXML(indexer, new File(args[i]));
      } else {
        indexGZip(indexer, new File(args[i]));
      }
    }
    */
  }
  
  static void optimize() throws IOException {
    IndexWriter indexWriter = new IndexWriter(INDEX_DIR, new StandardAnalyzer(), false);
    indexWriter.optimize();
    indexWriter.close();
  }
  
  static void process(String file)
          throws IOException, SAXException {

    if (new File(file).isDirectory()) {
      System.err.println("Entering directory: " + file + '\n');
      File dir = new File(file);
      String[] listing = dir.list();
      Arrays.sort(listing);
      for (int f = 0; f < listing.length; f++) {
        File tmp = new File(dir, listing[f]);
        process(tmp.getPath()); // recurse
      }

    } else {
      if (file.endsWith(".xml")) {
        indexXML(indexer, new File(file));
      } else if (file.endsWith(".gz")) {
        indexGZip(indexer, new File(file));
      } else {
        String msg = "Unknown file extension. File=" + file;
        //throw new IllegalArgumentException(msg);
      }
    }
  }

  static void indexXML(BufferedCitationIndexer indexer, File file)
          throws IOException, SAXException {
    System.err.println("Updating file=" + file.getCanonicalPath());
    indexer.open();
    String url = Files.fileToURLName(file);
    InputSource inSource = new InputSource(url);
    PARSER.parse(inSource, indexer);
    indexer.commit();
  }

  static void indexGZip(BufferedCitationIndexer indexer,
          File file)
          throws IOException, SAXException {

    System.err.println("Updating file=" + file.getCanonicalPath());
    FileInputStream fileIn = null;
    GZIPInputStream gzipIn = null;
    InputStreamReader inReader = null;
    BufferedReader bufReader = null;
    InputSource inSource = null;
    try {
      indexer.open();
      fileIn = new FileInputStream(file);
      gzipIn = new GZIPInputStream(fileIn);
      inReader = new InputStreamReader(gzipIn, Strings.UTF8);
      bufReader = new BufferedReader(inReader);
      inSource = new InputSource(bufReader);
      inSource.setSystemId(Files.fileToURLName(file));
      PARSER.parse(inSource, indexer);
      indexer.commit();
    } finally {
      Streams.closeReader(bufReader);
      Streams.closeReader(inReader);
      Streams.closeInputStream(gzipIn);
      Streams.closeInputStream(fileIn);
    }
  }

  static class BufferedCitationIndexer implements MedlineHandler {

    Directory mIndexDir;
    Set mDeletedIds = new HashSet();
    Set mAddedDocs = new HashSet();
    String path;
    private long mStartTime = System.currentTimeMillis();
    
    public BufferedCitationIndexer(String path) {
      this.path = path;
    }
    
    public void open() throws IOException {
      mDeletedIds = new HashSet();
      mAddedDocs = new HashSet();
      System.err.println(elapsedTime() + " Starting ");
    }

    public void delete(String pmid) {
      //System.err.println("Scheduling Delete. PMID=" + pmid);
      mDeletedIds.add(pmid);
    }

    public void handle(MedlineCitation citation) {
      
      //System.err.println("Scheduling Add=" + citation.pmid());

      // create Lucene doc and add content fields
      Document doc = IndexBaseline.citationToDocument(citation);
      if (doc != null) {
        String pmid = citation.pmid();
        mDeletedIds.add(pmid);
        
        doc.add(new Field("modified", date, Field.Store.YES, Field.Index.UN_TOKENIZED));
        mAddedDocs.add(doc);
      }
    }

    public void commit() throws IOException {
      
      int numDeleted = 0;
      IndexWriter mIndexWriter;
      IndexReader mIndexReader;
      //IndexSearcher mIndexSearcher;
      
      mIndexDir = FSDirectory.getDirectory(path, false);
      mIndexReader = IndexReader.open(mIndexDir);
      //mIndexSearcher = new IndexSearcher(mIndexReader);
      
      System.err.println(elapsedTime() + " Deleting " + mDeletedIds.size() + " PMIDS");
      
      // remove docs buffered to delete
      Iterator it = mDeletedIds.iterator();
      while (it.hasNext()) {
        String pmid = (String) it.next();
        Term idTerm = new Term("PMID", pmid);
        
        try {
          if (mIndexReader.docFreq(idTerm) > 0) {
              numDeleted = 
                      mIndexReader.deleteDocuments(idTerm);
              //System.err.println("Deleted PMID=" + pmid + " # matches=" + numDeleted);
          }
        } catch (IOException e) {
          System.err.println("  Could not find term=" + idTerm);
          //System.err.println("  Ignoring update to PMID=" + pmid);
          //return;
        }
      }
      
      mDeletedIds.clear();
      
      try {
        mIndexReader.close();
      } catch (IOException e) {
        System.err.println("  Error closing index: " + e.getMessage());
      }
      
      System.err.println(elapsedTime() + " Adding " + mAddedDocs.size() + " PMIDS");
      
      Analyzer analyzer = new StandardAnalyzer();
      mIndexWriter = new IndexWriter(mIndexDir, analyzer, false);
      Iterator it2 = mAddedDocs.iterator();
      
      while (it2.hasNext()) {
        Document doc = (Document) it2.next();
        
        try {
          mIndexWriter.addDocument(doc);
        } catch (IOException e) {
          System.err.println("  Error adding doc: " + e.getMessage());
        }
        
        it2.remove();
      }
      
      mIndexWriter.close();
      System.err.println(elapsedTime() + " Done");
    }
    
    private String elapsedTime() {
        return Strings.msToString(System.currentTimeMillis()-mStartTime);
    }
  }
}
