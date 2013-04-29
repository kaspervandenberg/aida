// © Maastro Clinics, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

/**
 * Interface for the wrappers {@link WebserviceSearcher} and 
 * {@link LocalLuceneSearcher} allowing {@link ZylabPatisClient} to search via
 * the SearcherWS webservice and via a process local Lucene instance.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface Searcher {

	SearchResult searchFor(final String query, final PatisNumber patient) throws QueryNodeException;

	SearchResult searchFor(final Query query, final PatisNumber patient);

	Iterable<SearchResult> searchForAll(final String query, final Iterable<PatisNumber> patients);

	Iterable<SearchResult> searchForAll(final Query query, final Iterable<PatisNumber> patients);
	
}
