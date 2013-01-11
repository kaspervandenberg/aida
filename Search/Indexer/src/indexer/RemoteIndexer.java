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
    
    private IndexAdder adder;
    private ConfigurationHandler cfg;
    private String BASE;
    private String extention;
    private AnalyzerFactory af;
    private IndexWriter writer;
    private String indexdir;
    
    /** Creates a new instance of RemoteIndexer */
    public RemoteIndexer(String configFile) {
        cfg = new ConfigurationHandler(configFile);
        adder = new IndexAdder(cfg);
        BASE = Utilities.getINDEXDIR();
        String name = "RemoteIndex";
        indexdir = BASE + name;
        
        af = new AnalyzerFactory(cfg);
        boolean overwrite = cfg.OverWrite();
        if (overwrite == false) {
            if (! new File(indexdir).exists())
                overwrite = true;
        }
        
        try {
            writer = new IndexWriter(indexdir, af.getGlobalAnalyzer(), overwrite);
            writer.setUseCompoundFile(true);
            writer.setMergeFactor(cfg.getMergeFactor());
            writer.setMaxBufferedDocs(cfg.getMaxBufferedDocs());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
        writer.flush();
        writer.close();
        try {
            writer = new IndexWriter(indexdir, af.getGlobalAnalyzer(), false);
            writer.setUseCompoundFile(true);
            writer.setMergeFactor(cfg.getMergeFactor());
            writer.setMaxBufferedDocs(cfg.getMaxBufferedDocs());
        } catch (IOException ex) {
            ex.printStackTrace();
        }        
    }
}
