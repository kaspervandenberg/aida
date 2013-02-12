package org.apache.lucene.aid;
/*
 * LuceneFilter.java
 *
 * Created on July 14, 2006, 4:35 PM
 *
 */

import org.apache.lucene.search.Filter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.Term;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.BitSet;

/**
 * Class to store and load Lucene filters, which can be used to filter search results. 
 * 
 * @author Edgar Meij
 */
public class LuceneFilter extends Filter {
	
	static final long serialVersionUID = 0122334;
    
    private String[] IDs;
    private String field;
    
    /** Creates a new instance of LuceneFilter */
    public LuceneFilter(String[] IDs, String field) {
        this.IDs = IDs;
        this.field = field;
    }
    
    /** Creates a new instance of LuceneFilter, using default field "id" */
    public LuceneFilter(String[] IDs) {
        this.IDs = IDs;
        this.field = "id";
    }

    /** Creates a new empty instance of LuceneFilter */
    public LuceneFilter() {
        this.IDs = null;
        this.field = "id";
    }    
    
    /** Sets new IDs to the filter **/
    public void setField(String field) {
        this.field = field;
    }        
    
    /** Sets new IDs to the filter **/
    public void setFilter(String[] IDs) {
        this.IDs = IDs;
    }    
    
    /** Gets IDs of the filter **/
    public String[] getFilter() {
        return this.IDs;
    }        
    
    /** Stores the current filter on disk **/
    public void storeFilter(String indexDir, String fileName) throws IOException {
        storeFilter(new File(indexDir, fileName), this.IDs);
    }
    
    /** Stores a filter on disk **/
    public void storeFilter(String indexDir, String fileName, String[] IDs) throws IOException {
        storeFilter(new File(indexDir, fileName), IDs);
    }    
    
    /** Stores this filter on disk **/
    public void storeFilter(File fileName, String[] IDs) throws IOException {
        FileWriter outFile = new FileWriter(fileName);
        PrintWriter fileOutput = new PrintWriter(outFile);
        
        for (int i=0; i<IDs.length; i++) {
            fileOutput.println(IDs[i]);
        }
        
        fileOutput.close();
        outFile.close();
    }
    
    /** Loads a filter from disk **/    
    public void loadFilter(String indexDir, String fileName) throws IOException {
        loadFilter(new File(indexDir, fileName));
    }
    
    /** Loads a filter from disk **/    
    public void loadFilter(String fileName) throws IOException {
        loadFilter(new File(fileName));
    }    
    
    /** Loads a filter from disk **/    
    public void loadFilter(File fileName, String field) throws IOException {
        this.setField(field);
        this.loadFilter(fileName);
    }
    
    /** Loads a filter from disk **/    
    public void loadFilter(String indexDir, String fileName, String field) throws IOException {
        this.setField(field);
        this.loadFilter(indexDir, fileName);
    }    
    
    /** Loads a filter from disk **/    
    public void loadFilter(File fileName) throws IOException {
        
        FileReader freader = null;
        LineNumberReader lnr = null;     
        Vector<String> idList = new Vector<String>();
                
        try {
            freader = new FileReader(fileName);
            lnr = new LineNumberReader(freader);
            String word = null;
            while ((word = lnr.readLine()) != null) {
                if (word.length() > 0) {
                    // Store found Strings in a Vector
                    idList.add(word);
                }
            }
        }
        
        finally {
            if (lnr != null)
                lnr.close();
            if (freader != null)
                freader.close();
        }
        
        this.setFilter( (String[]) idList.toArray(new String[0]));
    }    
    
    public BitSet bits(IndexReader reader) throws IOException {
        
        BitSet bits = new BitSet(reader.maxDoc());
        String[] IDs = this.IDs;
        int[] docs = new int[1];
        int[] freqs = new int[1];
        
        if (IDs != null) {
            for (int i=0; i<IDs.length; i++) {
                String ID = IDs[i];

                if (ID != null) {
                    TermDocs termDocs = 
                        reader.termDocs(new Term(field, ID));

                    int count = termDocs.read(docs, freqs);
                    if (count == 1) {
                        bits.set(docs[0]);
                    }

                }
            }
        } else {
            bits.set(0,reader.maxDoc());
        }
        
        return bits;
        
    }
    
    public static void main (String[] args) {
        
        LuceneFilter ft = new LuceneFilter(new String[] {"1", "3"}, "PMID");
        
        try {
            ft.storeFilter(args[0], args[1]);
            System.out.println(args[0] + System.getProperty("file.separator") + args[1] + " succesfully created");
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            System.err.println("Define directory and file");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        
        
    }
    
}
