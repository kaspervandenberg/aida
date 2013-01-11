/**
 *
 * @author Machiel Jansen, Edgar Meij
 */
package indexer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.IndexWriter;

public class BaseIndexing {
  
  public String datadir;
  public String indexdir;
  public final File cachedir;
  private ConfigurationHandler cfg;
  private HandlerFactory hf;
  private String BASE;
  public int added = 0;
  public int failed = 0;
  
  /** logger for Commons logging. */
  private transient Logger log =
    Logger.getLogger("BaseIndexing.class.getName()");

  /** Creates a new instance of BaseIndexing
   * @param       configFile      path to the configurationfile
   * @param       name            Name of the index to use
   * @param       dataPath        path to the files
   */
  public BaseIndexing(String configFile, String name, String dataPath) {
    this(new ConfigurationHandler(configFile), name, dataPath);
  }
  
  public BaseIndexing(ConfigurationHandler cfg, String name, String dataPath) {
    
    this.cfg = cfg;
    
    BASE = Utilities.getINDEXDIR();
    hf = new HandlerFactory(cfg);

    if (name == null || name.length() == 0)
      name = cfg.getName();
    if (dataPath == null || dataPath.length() == 0)
      dataPath = cfg.getDataPath();

    indexdir = BASE + name;
    datadir = dataPath; 

    // test whether the index is locked
    if (new File(indexdir, "indexlock").exists())
      throw new RuntimeException("Index is locked.");

    if (log.isLoggable(Level.FINE)) {
      log.fine("Indexdir: " + indexdir);
      log.fine("Datadir: " + datadir);
    }
    
    cachedir = new File(indexdir, "cache");
    
  }

  /** Adds Documents to the index */
  public boolean addDocuments() throws IOException{

    AnalyzerFactory af = new AnalyzerFactory(cfg);

    // Switch to true if this is a fresh index
    boolean overwrite = cfg.OverWrite();
    if (overwrite == false) {
      if (! new File(indexdir).exists())
        overwrite = true;
    }

    IndexWriter writer = new IndexWriter(indexdir, af.getGlobalAnalyzer(), overwrite);
    writer.setUseCompoundFile(true);
    writer.setMergeFactor(cfg.getMergeFactor());
    writer.setMaxBufferedDocs(cfg.getMaxBufferedDocs());

    if (!cachedir.exists())
      if (!cachedir.mkdirs()) 
        throw new RuntimeException("Error creating cachedir " + cachedir.getAbsolutePath());

    indexDocs(writer, new File(datadir));
    writer.optimize();
    writer.close();    
    return true;
  }

  /** Does the actual indexing
   * @param       writer          IndexWriter to use
   * @param       file            File to index
   */
  private void indexDocs(IndexWriter writer, File file) {
    if (file.canRead()) {
      if (file.isDirectory()) {
        String[] files = file.list();
        // an IO error could occur
        if (files != null) {
          for (int i = 0; i < files.length; i++) {
            indexDocs(writer, new File(file, files[i]));
            //log.info("done");
          }  
        }  
      } else {  
        if (!file.getName().startsWith(".")) {
          
          if (log.isLoggable(Level.FINE))
            log.fine("Adding " + file);  
          
          DocHandler dh = hf.getHandler(file.getName());
          
          if (dh != null) {
            try {
              dh.addDocumentToIndex(writer, file);
              dh.copyDocumentToCache(file, new File(cachedir, file.getName()));
              added++;
            } catch (DocumentHandlerException e) {
              if (log.isLoggable(Level.SEVERE))
                log.severe("" + e.getMessage());
              failed++;
            }
          } else {
            failed++;
          }
        }
      }
    }
  }

}