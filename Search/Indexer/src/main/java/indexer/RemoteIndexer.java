/*
 * RemoteIndexer.java
 *
 * Created on March 4, 2008, 10:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package indexer;

import java.io.IOException;
import java.io.File;
import org.apache.lucene.index.IndexWriter;

/**
 *
 * @author alogo
 */
public class RemoteIndexer {
    
    private final IndexAdder adder;
    private final ConfigurationHandler cfg;
	private final String INDEXNAME = "RemoteIndex";
    private IndexWriter writer;
    
    /** Creates a new instance of RemoteIndexer */
    public RemoteIndexer(String configFile) {
        cfg = new ConfigurationHandler(configFile);
        adder = new IndexAdder(cfg);
        
		writer = new IndexWriterUtil(cfg, INDEXNAME).getIndexWriter();
    }
        
    
    public void addFiled(String extention,String field, String value){
        adder.addFieldToDocument(extention, field,value);
    }
    
    public void addContent(String extention, String content){
        adder.addFieldToDocument(extention, "content",content);
    }
    
    public void addDocToIndex(){
        adder.writeDocument("pdf",writer);
    }
    
    
    public void closeWriter() throws IOException{
//        System.out.println("Index has "+writer.docCount()+" docs");
//            writer.optimize();
        writer.commit();
        writer.close();
		writer = new IndexWriterUtil(cfg, INDEXNAME).getIndexWriter();
    }
}
