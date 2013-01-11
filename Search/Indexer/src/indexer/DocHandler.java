package indexer;

import org.apache.lucene.index.IndexWriter;
import java.io.File;
import java.io.IOException;

/**
 * @author Machiel Jansen, Edgar Meij
 */
public abstract class DocHandler {
  
  /**
   * Adds a file to the index
   *
   * @param   writer      IndexWriter to use
   * @param   file        File to add
   */
  public abstract void addDocumentToIndex(IndexWriter writer, File file) 
          throws DocumentHandlerException;
  
  /**
   * Return all possible field names
   *
   * @return   Array of field names
   */
  public abstract String[] getFieldNames();
  
  /**
   * Adds a file to the index cache. 
   * This function should be called after each addDocumentToIndex call
   *
   * @param   file        File to add to the indexcache
   * @throws  DocumentHandlerException  if an error occurs
   */  
  public void copyDocumentToCache (File src, File dest) 
          throws DocumentHandlerException {
    try {
      Utilities.copy(src, dest);
    } catch (IOException e) {
      throw new DocumentHandlerException(e.toString());
    }
    
  }
  
}