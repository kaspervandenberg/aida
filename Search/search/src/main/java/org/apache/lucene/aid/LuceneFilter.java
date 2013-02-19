package org.apache.lucene.aid;
/*
 * LuceneFilter.java
 *
 * Created on July 14, 2006, 4:35 PM
 *
 */

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.index.Term;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.util.Bits;

/**
 * Class to store and load Lucene filters, which can be used to filter search results. 
 * 
 * @deprecated
 * Wrapper arround Lucene's {@link TermsFilter}; prefer using {@code TermsFilter}
 * directely.
 * 
 * @author Edgar Meij
 * @author Kasper van den Berg
 */
@Deprecated
public class LuceneFilter extends Filter {
	
	static final long serialVersionUID = 0122334;
    
    private String[] IDs;
    private String field;

	private TermsFilter wrapped;
    
    /** Creates a new instance of LuceneFilter */
    public LuceneFilter(String[] IDs, String field) {
        this.IDs = IDs;
        this.field = field;
		this.wrapped = buildTermsFilter(this.IDs, this.field);
    }
    
    /** Creates a new instance of LuceneFilter, using default field "id" */
    public LuceneFilter(String[] IDs) {
		this(IDs, "id");
    }

    /** Creates a new empty instance of LuceneFilter */
    public LuceneFilter() {
		this(null, "id");
    }    
    
	@Override
	public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) throws IOException {
		return this.wrapped.getDocIdSet(context, acceptDocs);
	}
    
    /** Sets new IDs to the filter **/
    public void setField(String field) {
        this.field = field;
		this.wrapped = buildTermsFilter(this.IDs, this.field);
    }        
    
    /** Sets new IDs to the filter **/
    public void setFilter(String[] IDs) {
        this.IDs = IDs;
		this.wrapped = buildTermsFilter(this.IDs, this.field);
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
        List<String> idList = new ArrayList<String>();
                
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
        
        this.setFilter( idList.toArray(new String[0]));
    }    
    
	/**
	 * Create a {@link TermsFilter} that matches any {@code field}–{@code v}-term
	 * foreach {@code v} ∈ {@code values[]}.
	 * 
	 * @param values array of 'text of word' as used in {@link Term#Term(String, String) }
	 * @param field name of a Field as used in {@link Term#Term(String, String) }. 
	 * 
	 * @return	a {@link TermsFilter} that matches any value ∈ {@code values[]}
	 */
	private static TermsFilter buildTermsFilter(String values[], String field) {
		List<Term> terms = new ArrayList<Term>(values.length);
		for (String id : values) {
			terms.add(new Term(field, id));
		}
		return new TermsFilter(terms);
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
