/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

/**
 *
 * @author kasper
 */
public class IndexController {
	private static abstract class CancellableTask extends Thread {
		private final AtomicBoolean canceled = new AtomicBoolean(false);

		public void cancel() {
			canceled.set(true);
			this.interrupt();
		}

		public boolean isCancelled() {
			return canceled.get();
		}
	}
	
	private static abstract class WatchTask extends CancellableTask {
		private final Path watchedPath;
		private final WatchService service; 
		private final WatchKey key;

		public WatchTask(Path watchedPath_) throws IOException {
			this.watchedPath = watchedPath_;
			this.service = watchedPath.getFileSystem().newWatchService();
			this.key = this.watchedPath.register(
					service,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY);
		}
		
		protected abstract void createIndexTask(Path file); 
		
		@Override
		public void run() {
			while (!isCancelled()) {
				try {
					WatchKey k = service.take();
					for (WatchEvent<?> obj_watchEvent : k.pollEvents()) {
						if(obj_watchEvent.kind() == StandardWatchEventKinds.OVERFLOW) {
							continue;
						}
						
						@SuppressWarnings("unchecked")
						WatchEvent<Path> watchEvent = (WatchEvent<Path>)obj_watchEvent;
						Path fullpath = watchedPath.resolve(watchEvent.context());
						createIndexTask(fullpath);
					}
					k.reset();
				} catch (InterruptedException ex) {
					// Stop watching
					key.cancel();
					break;
				}
			}
		}
	}
	
	private static transient final Logger LOG = Logger.getLogger(IndexController.class.getName());
	private final static long REALTIME_SEARCHER_MAX_AGE = 10000;
	private final ExecutorCompletionService<ZylabData> tasks;
	private final ConcurrentMap<URL, ZylabData> documents;
	private final IndexWriter indexWriter;
	
	private IndexSearcher indexSearcher = null;
	private long indexSearcherCreationTime = 0;
	
	private Object realtimeSearcher = new Object() {

		public IndexSearcher get() throws IOException {
		}
	};

	public void onTaskComplete(ZylabData data) {
		if(data.getDataUrl() == null) {
			throw new Error(new NullPointerException(
					"Expecting data.dataUrl to be non-null."));
		}
		ZylabData storedData = documents.putIfAbsent(data.getDataUrl(), data);
		if(storedData != null) {
			storedData.merge(data);
		}
		
		if(storedData.getTasks().size() == ZylabData.DocumentParts.values().length) {
			boolean allComplete = true;
			boolean anyCanceled = false;
			
			for (Map.Entry<ZylabData.DocumentParts, FutureTask<?>> entry : storedData.getTasks()) {
				allComplete &= entry.getValue().isDone();
				anyCanceled |= entry.getValue().isCancelled();
			}

			if(allComplete && !anyCanceled) {
				indexDocument(storedData);
				documents.remove(storedData.getDataUrl(), storedData);
			}
		}
	}

	public void indexDocument(ZylabData data) {
		data.freeze();
		try {
			Term docId = data.getId();
			if(docId != null) {
				indexWriter.updateDocument(data.getId(), data.getFields());
			} else {
				LOG.log(Level.WARNING,
						String.format(
							"Adding document without ID to index (URL: %s)",
							data.getDataUrl()));
				indexWriter.addDocument(data.getFields());
			}
		} catch (IOException ex) {
			LOG.log(Level.SEVERE,
					String.format("Cannot add %s to index", data.getDataUrl()), ex);
		}
	}

	public boolean isIndexRecent(URL file) {
		try {
			IndexSearcher s = getRealTimeSearcher();
			TopDocs results = s.search(new TermQuery(urlToMetadataField(file)), 1);
			if(results.totalHits == 0) {
				return false;
			}
			Document doc = s.doc(results.scoreDocs[0].doc);
			doc.
			
		} catch (IOException ex) {
			LOG.log(Level.WARNING,
					String.format(
					"Unable to read index to determine whether %s should be updated", file));
		}
		return false;
	}

	private IndexSearcher getRealTimeSearcher() throws IOException {
		long now = new Date().getTime();
		if(indexSearcher == null || (now > (indexSearcherCreationTime + REALTIME_SEARCHER_MAX_AGE))) {
			DirectoryReader d = DirectoryReader.open(indexWriter, false);
			indexSearcher = new IndexSearcher(d);
			indexSearcherCreationTime = new Date().getTime();
		}
		return indexSearcher;
	}

	private Term urlToMetadataField(URL file) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	private Date lastModified(ZylabData.DocumentParts part, Document doc) {
		
		doc.getField(null)
	}
}
