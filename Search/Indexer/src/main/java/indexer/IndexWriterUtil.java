/*
 *  © Maastro 2013
 */
package indexer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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
public class IndexWriterUtil implements AutoCloseable {

	private static final transient Logger log =
			Logger.getLogger(IndexWriterUtil.class.getCanonicalName());

	/**
	 * Configuration that specifies the index dir.
	 */
	private final ConfigurationHandler config;

	/**
	 * File system directory where index is stored.
	 */
	private final FSDirectory indexdir;

	/**
	 * Used to add {@link org.apache.lucene.document.Document}s to the index.
	 */
	private IndexWriter indexWriter = null;

	/**
	 * Used to count the number of documents in the index
	 */
	private DirectoryReader dirReader = null;

	public IndexWriterUtil(final ConfigurationHandler config_) {
		this(config_, config_.getName());
	}
	
	public IndexWriterUtil(final ConfigurationHandler config_, 
			final String indexName) {
		config = config_;
		indexdir = initIndexDir(indexName);
	}

	@Override
	public void close() throws IOException {
		closeIndexWriter();
	}
	
	/**
	 * @return an (possibly shared) {@link IndexWriter}
	 */
	public IndexWriter getIndexWriter() {
		if(indexWriter == null) {
			indexWriter = createIndexWriter();
		}
		return indexWriter;
	}

	/**
	 * Close the {@link IndexWriter}.
	 * 
	 * Creating an index writer is expensive, prefer {@link IndexWriter#commit()}.
	 * Closing the {@code IndexWriter} is required to prevent loss of data.
	 * Call {@code closeIndexWriter()} when finished writing to the index.
	 */
	public void closeIndexWriter() {
		if(indexWriter != null) {
			try {
				if(dirReader == null || !dirReader.isCurrent()) {
					dirReader = DirectoryReader.open(indexWriter, true);
				}
			} catch (IOException ex) {
				String msg = String.format(
						"Error creating a DirectoryReader for index %s",
						getIndexdir().getName());
				log.log(Level.WARNING, msg, ex);
			}
			try {
				indexWriter.close();
			} catch (IOException ex) {
				String msg = String.format(
						"Error closing index %s, reattempt to close it.",
						getIndexdir().getName());
				log.log(Level.SEVERE, msg, ex);

				try {
					indexWriter.close();
				} catch (IOException ex2) {
					String msg2 = String.format(
							"Error closing index %s (second attempt), release locks data might be lost.",
							getIndexdir().getName());
					log.log(Level.SEVERE, msg2, ex2);
				} finally {
					try {
						if(IndexWriter.isLocked(indexdir)) {
							IndexWriter.unlock(indexdir);
						}
					} catch (IOException ex3) {
						String msg3 = String.format(
								"Error closing index %s, releasing lock.",
								getIndexdir().getName());
						log.log(Level.SEVERE, msg3, ex3);
					}
				}
			}
		}
		indexWriter = null;
	}
	
	public File getIndexdir() {
		return indexdir.getDirectory();
	}
	
	public File getCacheDir() {
		File cachedir = new File(indexdir.getDirectory(), "cache");
		assertIsDirectory(cachedir);
		return cachedir;
	}

	public void handleOutOfMememoryError(OutOfMemoryError ex) {
		log.log(Level.WARNING, "IndexWriterUtil attempt to recover from OutOfMemoryError", ex);
		closeIndexWriter();
	}

	public int getDocsInIndexCount() throws IOException {
		while(dirReader == null || !dirReader.isCurrent()) {
			if(indexWriter != null) {
				dirReader = DirectoryReader.open(indexWriter, true);
			} else {
				dirReader = DirectoryReader.open(indexdir);
			}
		}
		return dirReader.numDocs();
	}

	public void copyToCache(Path src, String targetName) {
		File dest = new File(getCacheDir(), targetName);
		try {
			Utilities.copy(src.toFile(), dest);
		} catch (IOException ex) {
			String msg = String.format(
					"Unable to copy %s to cache %s",
					src.toString(),
					dest.getPath());
			log.log(Level.SEVERE, msg, ex);
		}
		
	}
	
	public void copyToCache(Path file) {
		copyToCache(file, file.getFileName().toString());
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

	private IndexWriter createIndexWriter() {
		AnalyzerFactory af = new AnalyzerFactory(config);
		
		IndexWriterConfig iwconfig = new IndexWriterConfig(Version.LUCENE_41, af.getGlobalAnalyzer());
		// TODO Using defeault merge policy, check whether this is adequate
		// below is the lucene 2.1 settings to merge policy converted to 4.1
		// LogByteSizeMergePolicy mergePolicy = new LogByteSizeMergePolicy();
		// mergePolicy.setUseCompoundFile(true);
		// mergePolicy.setMergeFactor(cfg.getMergeFactor());
		// config.setMergePolicy(mergePolicy);
//		iwconfig.setOpenMode(
//				config.OverWrite() ?
//					IndexWriterConfig.OpenMode.CREATE : 
//					IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		iwconfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
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
