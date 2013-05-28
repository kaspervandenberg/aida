// © Maastro Clinics, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import com.google.gson.Gson;
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

	private SearchResult(PatisNumber patient_, int nHits_) {
		this.patient = patient_;
		this.nHits = nHits_;
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

	public static SearchResult create(final PatisNumber patient_, final TopDocs hits) {
		return new SearchResult(patient_, hits.totalHits);
	}

	public static SearchResult create(final PatisNumber patient_, final String json) {
		SearcherWSResults result = getGsonParser().fromJson(json, SearcherWSResults.class);
		return new SearchResult(patient_, result.hits);
	}

	private static Gson getGsonParser() {
		if (gsonParserInstance == null) {
			gsonParserInstance = new Gson();
		}
		return gsonParserInstance;
	}
	
}
