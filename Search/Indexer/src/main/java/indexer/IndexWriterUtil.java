/*
 *  © Maastro 2013
 */
package indexer;

import java.io.File;
import java.io.IOException;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

/**
 * Common code for BaseIndexing and RemoteIndexer.
 * 
 * TODO refactor BaseIndexing, RemoteIndexer, IndexAdder, and perhaps the 
 * DocHandlers.
 * 
 * @author Kasper van den Berg <kasper@kaspervandenberg.net>
 */
public class IndexWriterUtil {

	private static final transient Logger log =
			Logger.getLogger(IndexWriterUtil.class.getCanonicalName());

	/**
	 * Configuration that specifies the index dir.
	 */
	private final ConfigurationHandler config;

	private final FSDirectory indexdir;

	public IndexWriterUtil(final ConfigurationHandler config_) {
		this(config_, config_.getName());
	}
	
	public IndexWriterUtil(final ConfigurationHandler config_, 
			final String indexName) {
		config = config_;
		indexdir = initIndexDir(indexName);
	}

	public IndexWriter createIndexWriter() {
		AnalyzerFactory af = new AnalyzerFactory(config);
		
		IndexWriterConfig iwconfig = new IndexWriterConfig(Version.LUCENE_41, af.getGlobalAnalyzer());
		// TODO Using defeault merge policy, check whether this is adequate
		// below is the lucene 2.1 settings to merge policy converted to 4.1
		// LogByteSizeMergePolicy mergePolicy = new LogByteSizeMergePolicy();
		// mergePolicy.setUseCompoundFile(true);
		// mergePolicy.setMergeFactor(cfg.getMergeFactor());
		// config.setMergePolicy(mergePolicy);
		iwconfig.setOpenMode(
				config.OverWrite() ?
					IndexWriterConfig.OpenMode.CREATE : 
					IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		iwconfig.setMaxBufferedDocs(config.getMaxBufferedDocs());
		
		IndexWriter writer;
		try {
			writer = new IndexWriter(indexdir, iwconfig);
			return writer;
		} catch (IOException ex) {
			Logger.getLogger(IndexWriterUtil.class.getName()).log(Level.SEVERE, null, ex);
			throw new RuntimeException("Exception occured when creating IndexWriter", ex);
		}
	}

	public File getIndexdir() {
		return indexdir.getDirectory();
	}
	
	private FSDirectory initIndexDir(String indexName) {
		File base = new File(Utilities.getINDEXDIR());
		
		try {
			FSDirectory indexdir = FSDirectory.open(new File(base, indexName));
			assertCanLock(indexdir);

			if (log.isLoggable(Level.FINE)) {
			  log.fine("Indexdir: " + indexdir);
			}
			return indexdir;
			
		} catch (IOException ex) {
			throw new RuntimeException("IOException ", ex);
		}
	}

	public File getCacheDir() {
		File cachedir = new File(indexdir.getDirectory(), "cache");
		assertIsDirectory(cachedir);
		return cachedir;
	}

	private static void assertCanLock(Directory indexdir) throws RuntimeException {
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
	}

	private static void assertIsDirectory(File f) {
		if(!f.exists()) {
			f.mkdirs();
		}
	
		if((!f.exists()) || (!f.isDirectory())) {
			throw new RuntimeException(String.format("Error creating directory %s", f.toString()));
		}
  	}

}
