package indexer;

import org.apache.lucene.index.IndexWriter;
import java.io.File;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author Edgar Meij
 * @todo Should we check the config file to see whether the doctype is defined? And, if not, skip indexing?
 */
public class TxtHandler extends DocHandler {
  private final String TYPE= "txt";
  private final ConfigurationHandler cfg;
  private final IndexAdder ia;
  private final ConfigurationHandler.FieldTypeCache fields;
  private List<String> fieldList;
  //private boolean indexDocs = true;
  
  /** Creates a new instance of TextHandler */
  public TxtHandler(ConfigurationHandler config) {
    cfg = config;
    ia = new IndexAdder(cfg);
	fields = cfg.getFields(TYPE);
  }
  
  public String[] getFieldNames(){
    return new String[] { "content", "id", "path", "url", "summary"};
  }
  
  public void addDocumentToIndex(IndexWriter writer, File file)  
          throws DocumentHandlerException {
    try {
      String text = Utilities.loadTextFile(file);
      ia.addFieldToDocument(TYPE, "id", file.getName());
      ia.addFieldToDocument(TYPE, "content", Utilities.cleanString(text));
      ia.addFieldToDocument(TYPE, "path", file.getPath());
      ia.addFieldToDocument(TYPE, "url", file.toURI().toString());
      
      //int summarySize = Math.min( text.length(), 1000 );
      //String summary = text.substring( 0, summarySize );
      
      // Add the summary as an UnIndexed field, so that it is stored and returned
      // with hit documents for display.
      //ia.addFieldToDocument(TYPE, "summary", summary );
      
      ia.writeDocument(TYPE, writer);
    } catch(Exception e) {
      throw new DocumentHandlerException("Cannot extract text from txt file: " 
              + file.getName() + " ('" + e.getMessage() + "')", e);
    }
  }

}
