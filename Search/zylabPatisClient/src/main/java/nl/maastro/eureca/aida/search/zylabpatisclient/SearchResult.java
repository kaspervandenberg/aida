// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import com.google.gson.Gson;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.TopDocs;

/**
 * Results that {@link ZylabPatisClient} returns
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class SearchResult {
	/**
	 * The relevant parts of the response from SearcherWS.
	 */
	private static class SearcherWSResults {
		int hits;
	}
	
	private static transient Gson gsonParserInstance;
	private static transient SearchResult NO_RESULT;
	public final PatisNumber patient;
	public final int nHits;
	public final Map<String, Set<String>> snippets;

	
//	private final Map<DocumentId, Map<SemanticModifier, Set<Snippet>>> snippets;

	private SearchResult(PatisNumber patient_, int nHits_) {
		this.patient = patient_;
		this.nHits = nHits_;
		this.snippets = Collections.<String, Set<String>>emptyMap();
	}

	private SearchResult(PatisNumber patient_, int nHits_, 
			Map<String, Set<String>> snippets_) {
		this.patient = patient_;
		this.nHits = nHits_;
		this.snippets = snippets_;
	}

	/**
	 * 
	 * @return 
	 */
	public static SearchResult NO_RESULT() {
		if (NO_RESULT == null) {
			NO_RESULT = new SearchResult(null, -1);
		}
		return NO_RESULT;
	}

	/**
	 * Create a SearchResult from a call to 
	 * {@link org.apache.lucene.search.IndexSearcher#search(org.apache.lucene.search.Query, int)}
	 * 
	 * @param patient_
	 * @param hits
	 * @param snippets_
	 * @return 
	 */
	public static SearchResult create(final PatisNumber patient_, 
			final TopDocs hits, Map<String, Set<String>> snippets_) {
		return new SearchResult(patient_, hits.totalHits, snippets_);
	}

	public static SearchResult create(final PatisNumber patient_, final String json) {
		SearcherWSResults result = getGsonParser().fromJson(json, SearcherWSResults.class);
		return new SearchResult(patient_, result.hits);
	}

	public Set<DocumentId> getMatchingDocuments() {
		HashSet<DocumentId> result = new HashSet<>(snippets.size());
		for (String docId : snippets.keySet()) {
			result.add(new DocumentId(docId));
		}
		return result;
	}

	public Set<Snippet> getSnippets(DocumentId docId) {
		Set<String> snippetsByDoc = snippets.get(docId.value);
		HashSet<Snippet> result = new HashSet<>(snippetsByDoc.size());
		for (String snippetString : snippetsByDoc) {
			result.add(new Snippet(snippetString));
		}
		return result;
	}

	public Set<Classification> getClassification(DocumentId docId) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private static Gson getGsonParser() {
		if (gsonParserInstance == null) {
			gsonParserInstance = new Gson();
		}
		return gsonParserInstance;
	}
	
}
