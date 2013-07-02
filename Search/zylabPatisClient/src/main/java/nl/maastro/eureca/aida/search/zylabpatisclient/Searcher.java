// © Maastro Clinics, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;

/**
 * Interface for the wrappers {@link WebserviceSearcher} and 
 * {@link LocalLuceneSearcher} allowing {@link ZylabPatisClient} to search via
 * the SearcherWS webservice and via a process local Lucene instance.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface Searcher {
	SearchResult searchFor(final Query query, 
			final Iterable<SemanticModifier> modifiers,
			final PatisNumber patient);

	Iterable<SearchResult> searchForAll(final Query query,
			final Iterable<SemanticModifier> modifiers,
			final Iterable<PatisNumber> patients);
}
