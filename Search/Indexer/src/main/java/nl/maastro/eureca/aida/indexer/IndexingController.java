// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

/**
 * Control incremental indexing of multipart documents.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class IndexingController {
	
	private static transient final Logger LOG = Logger.getLogger(IndexingController.class.getName());
	private final static long REALTIME_SEARCHER_MAX_AGE = 10000;
	private ExecutorCompletionService<ZylabData> tasks;
	private ConcurrentMap<URL, ZylabData> documents;
	private IndexWriter indexWriter;
	
	private IndexSearcher indexSearcher = null;
	private long indexSearcherCreationTime = 0;
	
	public void onTaskComplete(ZylabData data) {
		ZylabData storedData = storeAndMerge(data);
		if(storedData.isAllTasksFinished()) {
			indexDocument(data);
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
			ZylabData zylab = new ZylabData(doc);
			Date indexedVersion = zylab.getLastModified(zylab.getPartPointedTo(file));
			if(indexedVersion == null) {
				return false;
			}
			Date fileDate = new Date(file.openConnection().getDate());
			return !indexedVersion.before(fileDate);
			
		} catch (IOException ex) {
			LOG.log(Level.WARNING,
					String.format(
					"Unable to read index to determine whether %s should be updated", file));
		}
		return false;
	}

	/**
	 * Store {@code data} in {@link #documents} {@link ZylabData#merge merging}
	 * it with any data already in {@code documents}.
	 * 
	 * @param data	the {@link ZylabData} to store
	 * @return	the merged data (as stored)
	 */
	private ZylabData storeAndMerge(ZylabData data) {
		if(data.getDataUrl() == null) {
			throw new Error(new NullPointerException(
					"Expecting data.dataUrl to be non-null."));
		}
		ZylabData storedData = documents.putIfAbsent(data.getDataUrl(), data);
		if(storedData != null) {
			storedData.merge(data);
		}
		return storedData;
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
}
