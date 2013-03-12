/**
 *
 * @author Machiel Jansen, Edgar Meij
 */
package indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

public class BaseIndexing {
  
  public File datadir;
  private IndexWriterUtil indexWriterUtil;
//  public FSDirectory indexdir;
//  public final File cachedir;
  private ConfigurationHandler cfg;
  private HandlerFactory hf;
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
    
    hf = new HandlerFactory(cfg);

	indexWriterUtil = (name == null || name.length() == 0) ?
			new IndexWriterUtil(cfg) :
			new IndexWriterUtil(cfg, name);
	
    datadir = (dataPath == null || dataPath.length() == 0) ?
			new File(cfg.getDataPath()) :
			new File(dataPath);
	
		if (log.isLoggable(Level.FINE)) {
		  log.fine("Indexdir: " + indexWriterUtil.getIndexdir());
		  log.fine("Datadir: " + datadir);
		}
		
  }

  /** Adds Documents to the index */
  public boolean addDocuments() throws IOException{
    IndexWriter writer = indexWriterUtil.createIndexWriter();

	assertIsDirectory(indexWriterUtil.getCacheDir());

    indexDocs(writer, datadir);
    writer.close();    
    return true;
  }

	/** Does the actual indexing
	 * @param       writer          IndexWriter to use
	 * @param       file            File to index
	 */
	private void indexDocs(IndexWriter writer, File file) {
		// TODO Integrate Tica config with 'indexconfig.xml'
		TikaConfig tc = TikaConfig.getDefaultConfig();
		  
		Tika ticaParser = new  Tika(); 
		Metadata metadata = new Metadata();
		try {
			// TODO Extract metadata and content in a single pass
			// TODO Extract summary
			ticaParser.parse(new FileInputStream(file), metadata);
			String content = ParseUtils.getStringContent(file, tc);
		} catch (/*TODO specify Exception*/ Exception ex) {
			// TODO Handle exeption
		}
	 
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
              dh.copyDocumentToCache(file, new File(indexWriterUtil.getCacheDir(), file.getName()));
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

  public File getIndexdir() {
	  return indexWriterUtil.getIndexdir();
  }

  private void assertIsDirectory(File f) {
	if(!f.exists()) {
		f.mkdirs();
	}
	
	if((!f.exists()) || (!f.isDirectory())) {
		throw new RuntimeException(String.format("Error creating directory %s", f.toString()));
	}
  }

}

/* vim: set shiftwidth=4 tabstop=4 noexpandtab fo=ctwan ai : */

