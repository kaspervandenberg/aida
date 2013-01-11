package indexer;

import org.apache.lucene.index.IndexWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Vector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.textmining.text.extraction.WordExtractor;

/**
 *
 * @author Machiel Jansen, Edgar Meij
 */
public class MswordHandler extends DocHandler {
  private final String TYPE= "msword";
  private ConfigurationHandler cfg;
  private IndexAdder ia;
  private List<String> fieldList;
  
  /** Creates a new instance of MSWordHandler */
  public MswordHandler(ConfigurationHandler cfg) {
    this.cfg = cfg;
    ia = new IndexAdder(cfg);
    this.fieldList = new Vector<String>();
    String[] fields = cfg.getFields(TYPE);
    for (int x=0; x<fields.length; x++) {
      fieldList.add(fields[x]);
    }
  }
  
  public String[] getFieldNames() {
    return new String[] { "id", "content", "path", "url", "summary"};
  }
  
  public void addDocumentToIndex(IndexWriter writer, File file) 
          throws DocumentHandlerException {
    try {
      String text = getDocument(file);
      // default fields
      ia.addFieldToDocument(TYPE, "id", file.getName());
      ia.addFieldToDocument(TYPE, "content", text);
      
      int summarySize = Math.min( text.length(), 1000 );
      String summary = text.substring( 0, summarySize );
      
      // Add the summary as an UnIndexed field, so that it is stored and returned
      // with hit documents for display.
      if (fieldList.contains("summary"))
        ia.addFieldToDocument(TYPE, "summary", summary );
      
      if (fieldList.contains("path"))
        ia.addFieldToDocument(TYPE, "path", file.getPath());
      ia.addFieldToDocument(TYPE, "url", file.toURL().toString());
      
      ia.writeDocument(TYPE, writer);
      
    } catch(Exception ex) {
      throw new DocumentHandlerException(ex.toString());
    }  
  }
  
  /**
   * Extract the text from a document
   *
   * @param   file        File to use
   * @return              Extracted text
   */
  private String getDocument(File file ) throws DocumentHandlerException {
    
    String bodyText = "";
    
    try { 
      bodyText = new WordExtractor().extractText(new BufferedInputStream(new FileInputStream(file)));
    } catch (Exception e) {
      throw new DocumentHandlerException("Cannot extract text from Word file: " 
              + file.getName() + " ('" + e.getMessage() + "')", e);
    }
    return Utilities.cleanString(bodyText); 
  }  
}
