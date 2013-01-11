package org.vle.aid.medline;

import com.aliasi.medline.Abstract;
import com.aliasi.medline.Article;
import com.aliasi.medline.JournalIssue;
import com.aliasi.medline.MedlineCitation;
import com.aliasi.medline.MedlineHandler;
import com.aliasi.medline.MedlineParser;
import com.aliasi.medline.MeshHeading;
import com.aliasi.medline.PubDate;
import com.aliasi.medline.Topic;
import com.aliasi.medline.Author;
import com.aliasi.medline.Name;
import com.aliasi.util.Files;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.*;
import java.util.Vector;

import java.util.Date;
import java.text.DateFormat;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class IndexBaseline {

    static MedlineParser PARSER = new MedlineParser(true);
    static CitationIndexer indexer;
    static String[] stopwords;
    private static final String date = DateFormat.getDateInstance(DateFormat.SHORT).format(new Date());

    public IndexBaseline(File indexdir, File stopwordLocation, boolean freshindex) {

        if (stopwordLocation == null) {
            if (stopwordLocation.canRead()) {
                createStopwords(stopwordLocation);
            } else {
                Logger.getLogger(IndexBaseline.class.getName()).log(
                        Level.INFO, null, stopwordLocation.getAbsolutePath() + " not readable");
                stopwords = new String[0];
            }
        } else {
            stopwords = new String[0];
        }

        try {
            indexer = new CitationIndexer(indexdir, freshindex);
            PARSER = new MedlineParser(true);
            System.err.println("Index directory=" + indexdir.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(IndexBaseline.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void index(String[] fileLocations) {

        for (int i = 0; i < fileLocations.length; ++i) {
            try {
                process(fileLocations[i], indexer);
            } catch (IOException ex) {
                Logger.getLogger(IndexBaseline.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(IndexBaseline.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static void close() {
        try {
            indexer.close();
        } catch (IOException ex) {
            Logger.getLogger(IndexBaseline.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // Used for checking duplicates
    // When TRUE: An index already exists on the desired location
    // When FALSE: A fresh index is to be created
    // TODO: make interactive
    public static final boolean existingIndex = true;

    public static void main(String[] args) {

        try {

            if (args.length < 3) {
                throw new ArrayIndexOutOfBoundsException();
            }

            File indexDir = null;
            File stopwordFile = null;
            Vector<String> files = new Vector();

            boolean freshindex = false;

            if (args[0].equals("-f")) {
                freshindex = true;
                indexDir = new File(args[1]);
                stopwordFile = new File(args[2]);
                for (int i = 3; i < args.length; ++i) {
                    files.add(args[i]);
                }

            } else {
                indexDir = new File(args[0]);
                stopwordFile = new File(args[1]);

                for (int i = 2; i < args.length; ++i) {
                    files.add(args[i]);
                }
            }

            new IndexBaseline(indexDir, stopwordFile, freshindex);
            index(files.toArray(new String[0]));
            close();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Usage: Index indexdir stopwordfile [inputfiles]");
        }
    }

    static void process(String file, CitationIndexer indexer)
            throws IOException, SAXException {

        if (new File(file).isDirectory()) {
            System.err.println("Entering directory: " + file + '\n');
            File dir = new File(file);
            String[] listing = dir.list();
            for (int f = 0; f < listing.length; f++) {
                File tmp = new File(dir, listing[f]);
                process(tmp.getPath(), indexer); // recurse
            }

        } else {
            System.err.println("Indexing file=" + file + '\n');
            if (file.endsWith(".xml")) {
                indexXML(indexer, new File(file));
            } else if (file.endsWith(".gz")) {
                indexGZip(indexer, new File(file));
            } else {
                String msg = "Unknown file extension. File=" + file;
                throw new IllegalArgumentException(msg);
            }
        }
    }

    static void indexXML(CitationIndexer indexer, File file)
            throws IOException, SAXException {

        String url = Files.fileToURLName(file);
        InputSource inSource = new InputSource(url);
        PARSER.parse(inSource, indexer);
    }

    static void indexGZip(CitationIndexer indexer, File file)
            throws IOException, SAXException {

        FileInputStream fileIn = null;
        GZIPInputStream gzipIn = null;
        InputStreamReader inReader = null;
        BufferedReader bufReader = null;
        InputSource inSource = null;
        try {
            fileIn = new FileInputStream(file);
            gzipIn = new GZIPInputStream(fileIn);
            inReader = new InputStreamReader(gzipIn, Strings.UTF8);
            bufReader = new BufferedReader(inReader);
            inSource = new InputSource(bufReader);
            inSource.setSystemId(Files.fileToURLName(file));
            PARSER.parse(inSource, indexer);
        } finally {
            Streams.closeReader(bufReader);
            Streams.closeReader(inReader);
            Streams.closeInputStream(gzipIn);
            Streams.closeInputStream(fileIn);
        }
    }

    public static Document citationToDocument(MedlineCitation citation) {

        // Used if title or abstract is emtpy
        boolean doNotIndex = false;

        // Stores concatenation of title and abstract
        String content = "";
        String title = "";
        Article article = citation.article();
        Abstract abstr = article.abstrct();

        title += article.articleTitleText();
        content += " " + abstr.textWithoutTruncationMarker();

        // create Lucene doc and add content fields
        Document doc = new Document();
        doc.add(new Field("PMID", citation.pmid(), Field.Store.YES, Field.Index.UN_TOKENIZED));

        JournalIssue issue = article.journal().journalIssue();
        PubDate date = (issue != null) ? issue.pubDate() : article.book().pubDate();
        if (date.year() != null) {
            doc.add(new Field("year", date.year(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        }

        // Store Publication type
        String[] pubTypes = article.publicationTypes();
        for (int h = 0; h < pubTypes.length; ++h) {
            doc.add(new Field("PT", pubTypes[h], Field.Store.YES, Field.Index.UN_TOKENIZED));
        }

        if ("".equals(title) || title == null) {
            return null;
        }
        doc.add(new Field("title", title, Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.WITH_POSITIONS_OFFSETS));

        if ("".equals(content) || content == null) {
            return null;
        }
        doc.add(new Field("content", content, Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.WITH_POSITIONS_OFFSETS));

        // Authors
        Author[] al = article.authorList().authors();
        for (int i = 0; i < al.length; ++i) {
            Name n = al[i].name();
            if (n != null) {
                doc.add(new Field("author", n.fullName(), Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
            }
        }

        // MeSH
        MeshHeading[] headings = citation.meshHeadings();
        for (int i = 0; i < headings.length; ++i) {
            // There's always a descriptor for a heading:
            Topic descriptor = headings[i].descriptor();
            if (descriptor.isMajor()) {
                doc.add(new Field("mesh", descriptor.topic(), Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
            }
        }

        if (doNotIndex) {
            return null;
        } else {
            return doc;
        }
    }

    static class CitationIndexer implements MedlineHandler {

        IndexWriter mLuceneIndexer;
        int progress;
        // Analyzer analyzer = new SimpleAnalyzer();
        //Analyzer analyzer = new StandardAnalyzer(stopwords);
        Analyzer analyzer = new StandardAnalyzer();

        /**
         * 
         * @param indexDir
         * @param createFreshIndex setting it to true causes Lucene to create a fresh index in the specified directory.
         * @throws java.io.IOException
         */
        public CitationIndexer(File indexDir, boolean createFreshIndex) throws IOException {
            mLuceneIndexer = new IndexWriter(indexDir, analyzer, createFreshIndex);
            progress = 0;
        }

        public void handle(MedlineCitation citation) {

            Document doc = citationToDocument(citation);
            try {
                if (doc != null) {
                    System.err.println("     Adding PMID=" + citation.pmid());
                    doc.add(new Field("modified", date, Field.Store.YES, Field.Index.UN_TOKENIZED));
                    mLuceneIndexer.addDocument(doc);
                }
            } catch (IOException e) {
                System.err.println("EXCEPTION INDEXING CITATION: " + citation.pmid() + ". Error: " + e);
            }

        }

        public void close() throws IOException {
            System.err.println("Optimizing..");
            mLuceneIndexer.optimize();  // merges segments
            mLuceneIndexer.close();     // commits to disk
        }

        public void delete(String pmid) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Creates a String[] of stopwords from the specified file. Each stopword must
     * appear on a separate line. If there are any errors reading the file, the
     * returned list will be empty.
     * @param file the stopword file
     * @return a String[] containing the stopwords
     */
    public static void createStopwords(File file) {

        System.err.println("Creating stopwords from file " + file.getAbsolutePath());
        BufferedReader reader = null;

        Vector<String> words = new Vector();

        try {

            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line.trim());
            }

            stopwords = words.toArray(new String[0]);

        } catch (IOException ex) {
            Logger.getLogger(IndexBaseline.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.err.println("Array with stopwords created. Length: " + stopwords.length + " words");
    }

    public static boolean checkDuplicate(String[] Values, String check) {

        if (Values != null) {
            for (int k = 0; k < Values.length; ++k) {
                if (check.equalsIgnoreCase(Values[k])) {
                    return false;
                }
            }
        }

        return true;
    }
}
