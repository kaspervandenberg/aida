// © Maastro Clinics, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Use Lucene's {@link IndexSearcher} from within this process allowing complex
 * queries constructed with {@link Query}.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class LocalLuceneSearcher extends SearcherBase {
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
		log.logp(Level.FINE, LocalLuceneSearcher.class.getName(), "searchFor(S,P)", String.format("Entering (patient: %s, query: %s)", patient.value, query.toString()));
		String composedQuery = patient.compose(query);
		try {
			Query q = parser.parse(composedQuery, getDefaultField());
			TopDocs result = searcher.search(q, getMaxResults());
		log.logp(Level.FINE, LocalLuceneSearcher.class.getName(), "searchFor(S,P)", String.format("Leaving succesful (patient: %s, query: %s)", patient.value, query.toString()));
			return SearchResult.create(patient, result, getFragments(q, result));
		} catch (IOException ex) {
		log.logp(Level.FINE, LocalLuceneSearcher.class.getName(), "searchFor(S,P)", String.format("Exception (patient: %s, query: %s)", patient.value, query.toString()));
			log.log(Level.WARNING, String.format("IOException when querying local Lucene instance"), ex);
			return SearchResult.NO_RESULT();
		}
	}

	@Override
	public SearchResult searchFor(final Query query, final PatisNumber patient) {
		
		log.logp(Level.FINE, LocalLuceneSearcher.class.getName(), "searchFor(Q,P)", String.format("Entering (patient: %s, query: %s)", patient.value, query.toString()));
		try {
			Query q = patient.compose(query);
			TopDocs result = searcher.search(q, getMaxResults());
		log.logp(Level.FINE, LocalLuceneSearcher.class.getName(), "searchFor(Q,P)", String.format("Leaving succesful (patient: %s, query: %s)", patient.value, query.toString()));
			return SearchResult.create(patient, result, getFragments(q, result));
		} catch (IOException ex) {
		log.logp(Level.FINE, LocalLuceneSearcher.class.getName(), "searchFor(Q,P)", String.format("Exception (patient: %s, query: %s)", patient.value, query.toString()));
			log.log(Level.WARNING, String.format("IOException when querying local Lucene instance"), ex);
			return SearchResult.NO_RESULT();
		}
	}

	private Map<String, Set<String>> getFragments(Query query, TopDocs results) {
		log.logp(Level.FINE, LocalLuceneSearcher.class.getName(), "getFragments(Q, TD)", String.format("Entering (query: %s, results: %s)", query.toString(), results.toString()));
		try {
			QueryScorer scorer = new QueryScorer(query);
			Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<span class=\"searchHit\">", "</span>"), scorer);

			Map<String, Set<String>> fragments = 
					new HashMap<>(results.scoreDocs.length);
			
			for(int i = 0; i < results.scoreDocs.length; i++) {
				Document doc = searcher.doc(results.scoreDocs[i].doc);
				for (IndexableField indexableField : doc.getFields()) {
					log.log(Level.FINER, "#{0} field: {1}, value: {2}",
							new Object[] {
							Integer.toString(i),
							indexableField.name(),
							indexableField.stringValue()});
				}
				IndexableField content = doc.getField("content");
				
				TokenStream stream = content.tokenStream(new StandardAnalyzer(Version.LUCENE_41));
				try {
					Set<String> docFragments = new HashSet<>(
							Arrays.asList(
							highlighter.getBestFragments(
								stream, content.stringValue(), 5)));
					String id = doc.get("id");
					if(id == null) {
						id = "unknown_id?"+ UUID.randomUUID().toString();
					}
					fragments.put(id, docFragments);
				} catch (InvalidTokenOffsetsException ex) {
					throw new Error(ex);
				}
			}
					
		log.logp(Level.FINE, LocalLuceneSearcher.class.getName(), "getFragments(Q, TD)", String.format("Leaving (succesful) (query: %s, results: %s)", query.toString(), results.toString()));
			return fragments;
		} catch (IOException ex) {
		log.logp(Level.FINE, LocalLuceneSearcher.class.getName(), "getFragments(Q, TD)", String.format("Exception (query: %s, results: %s)", query.toString(), results.toString()));
			log.log(Level.WARNING, String.format("IOException when querying local Lucene instance"), ex);
			return Collections.emptyMap();
		}
	}

	@Override
	public boolean supportsStringQueries() {
		return true;
	}

	@Override
	public boolean supportsLuceneQueryObjects() {
		return true;
	}
	
}
