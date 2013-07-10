// © Maastro Clinics, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Common implementation for the wrappers … and … allowing {@link ZylabPatisClient} to 
 * search via the SearcherWS webservice and via a process local Lucene instance.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public abstract class SearcherBase implements Searcher {
	private final String defaultField;
	private final int maxResults;

	protected SearcherBase(final String defaultField_, final int maxResults_) {
		defaultField = defaultField_;
		maxResults = maxResults_;
	}

	@Override
	public Iterable<SearchResult> searchForAll(
			final nl.maastro.eureca.aida.search.zylabpatisclient.query.Query query,
			final Iterable<SemanticModifier> modifiers, 
			final Iterable<PatisNumber> patients) {
		Deque<SearchResult> result = new ConcurrentLinkedDeque<>();
		for (PatisNumber patient : patients) {
			result.add(searchFor(query, modifiers, patient));
		}
		return result;
	}

	protected String getDefaultField() {
		return defaultField;
	}

	protected int getMaxResults() {
		return maxResults;
	}
}
