// © Maastro Clinics, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Use Lucene's {@link IndexSearcher} from within this process allowing complex
 * queries constructed with {@link Query}.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
class LocalLuceneSearcher extends SearcherBase {
	private static final Logger log = Logger.getLogger(LocalLuceneSearcher.class.getName());
	private static final StandardQueryParser parser = new StandardQueryParser();
	private final IndexSearcher searcher;

	public LocalLuceneSearcher(final File index, String defaultField_, int maxResults_, final ForkJoinPool taskPool_) throws IOException {
		super(defaultField_, maxResults_, taskPool_);
		Directory indexDir = FSDirectory.open(index);
		searcher = new IndexSearcher(DirectoryReader.open(indexDir), taskPool_);
	}

	@Override
	public SearchResult searchFor(final String query, final PatisNumber patient) throws QueryNodeException {
		String composedQuery = patient.compose(query);
		try {
			Query q = parser.parse(composedQuery, getDefaultField());
			TopDocs result = searcher.search(q, getMaxResults());
			return SearchResult.create(patient, result);
		} catch (IOException ex) {
			log.log(Level.WARNING, String.format("IOException when querying local Lucene instance"), ex);
			return SearchResult.NO_RESULT();
		}
	}

	@Override
	public SearchResult searchFor(final Query query, final PatisNumber patient) {
		try {
			Query q = patient.compose(query);
			TopDocs result = searcher.search(q, getMaxResults());
			return SearchResult.create(patient, result);
		} catch (IOException ex) {
			log.log(Level.WARNING, String.format("IOException when querying local Lucene instance"), ex);
			return SearchResult.NO_RESULT();
		}
	}
	
}
