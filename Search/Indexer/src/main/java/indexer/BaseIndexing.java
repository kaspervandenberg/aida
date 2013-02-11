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
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

public class BaseIndexing {
  
  public String datadir;
  public FSDirectory indexdir;
  public final File cachedir;
  private ConfigurationHandler cfg;
  private HandlerFactory hf;
  private File BASE;
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
    
    BASE = new File(Utilities.getINDEXDIR());
    hf = new HandlerFactory(cfg);

    if (name == null || name.length() == 0)
      name = cfg.getName();
    if (dataPath == null || dataPath.length() == 0)
      dataPath = cfg.getDataPath();

	try {
	    indexdir = FSDirectory.open(new File(BASE, name));
		
	    datadir = dataPath; 

		try {
			// test whether the index is locked
			new Lock.With(indexdir.makeLock("indexlock"), 1000) {
			  public Object doBody() {
				  // No code needed, just check acquiring lcok succeeds.
				  return null;
			  }
			}.run();
		} catch (LockObtainFailedException ex) {
			throw new RuntimeException("Index is locked.", ex);
		} catch (IOException ex) {
			throw new RuntimeException("IO exception, Index is locked", ex);
		}
		

		if (log.isLoggable(Level.FINE)) {
		  log.fine("Indexdir: " + indexdir);
		  log.fine("Datadir: " + datadir);
		}
		
		cachedir = new File(indexdir.getDirectory(), "cache");

	} catch (IOException ex) {
		throw new RuntimeException("IOException ", ex);
	}
  }

  /** Adds Documents to the index */
  public boolean addDocuments() throws IOException{

    AnalyzerFactory af = new AnalyzerFactory(cfg);

	IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_41, af.getGlobalAnalyzer());
	// TODO Using defeault merge policy, check whether this is adequate
	// below is the lucene 2.1 settings to merge policy converted to 4.1
	// LogByteSizeMergePolicy mergePolicy = new LogByteSizeMergePolicy();
	// mergePolicy.setUseCompoundFile(true);
	// mergePolicy.setMergeFactor(cfg.getMergeFactor());
	// config.setMergePolicy(mergePolicy);
	config.setOpenMode(
			cfg.OverWrite() ?
				IndexWriterConfig.OpenMode.CREATE : 
				IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
	config.setMaxBufferedDocs(cfg.getMaxBufferedDocs());
    IndexWriter writer = new IndexWriter(indexdir, config);

	assertIsDirectory(cachedir);

    indexDocs(writer, new File(datadir));
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

  private void assertIsDirectory(File f) {
	if(!f.exists()) {
		f.mkdirs();
	}
	
	if((!f.exists()) || (!f.isDirectory())) {
		throw new RuntimeException(String.format("Error creating directory %s", f.toString()));
	}
  }

}