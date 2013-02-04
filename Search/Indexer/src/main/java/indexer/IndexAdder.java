package indexer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.Analyzer;

/**
 *
 * @author Machiel Jansen, Edgar Meij
 */
public class IndexAdder {
  private final ConfigurationHandler cfg;
  private Document doc;
  
  /** logger for Commons logging. */
  private transient Logger log =
    Logger.getLogger("IndexAdder.class.getName()");
  
  /** Creates a new instance of IndexAdder using a configfile */  
  public IndexAdder(String configfile) {
    this(new ConfigurationHandler(configfile));
  }
  
  /** Creates a new instance of IndexAdder using a ConfigurationHandler*/
  public IndexAdder(ConfigurationHandler cfg) {   
    this.cfg = cfg;
    doc = new Document();
  }
    
  /**
   *  Add the field to a document
   *
   * @param DocumentType  the Document type to use
   * @param name          the field name
   * @param value         the value
   * 
   * @todo Find quicker way of accessing configuration options
   */          
  public void addFieldToDocument(String DocumentType, String name, String value) {
    
    // Check the defined fields in the config file
    String[] fields = cfg.getFields(DocumentType);
    
    for (int x=0; x<fields.length; x++) {
      if (fields[x].equalsIgnoreCase(name)){
        Field.Store store = cfg.getFieldStoreValue(DocumentType, name);
        Field.Index index = cfg.getFieldIndexValue(DocumentType, name);
        Field.TermVector tv = cfg.getTermVectorValue(DocumentType, name);      
        doc.add(new Field(name, value, store, index, tv));
      }
    }
  }
  
  /**
   *  Add the document to an index
   *
   * @param DocumentType  the Document type to use
   * @param iw            The IndexWriter to use
   */ 
  public void writeDocument(String DocumentType, IndexWriter iw) {
    try {
      AnalyzerFactory af = new AnalyzerFactory(cfg);
      String doctype = cfg.getDocumentAnalyzer(DocumentType);
      Analyzer analyzer = af.getAnalyzer(doctype);
      iw.addDocument(doc, analyzer);
      
    } catch(IOException ex) {
      if (log.isLoggable(Level.SEVERE))
        log.severe(ex.toString());
    }
    doc = new Document();
  }
}
