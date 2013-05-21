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
	/**
	 * What {@link nl.maastro.eureca.aida.search.zylabpatisclient.QueryProvider.QueryRepresentation}
	 * does the searcher support?
	 */
	public enum SearcherCapability {
		/**
		 * The Searcher supports queries with
		 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.QueryProvider.QueryRepresentation#STRING}-representation.
		 */
		STRING,

		/**
		 * The Searcher supports queries with
		 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.QueryProvider.QueryRepresentation#OBJECT}-representation.
		 */
		OBJECT,

		/**
		 * The Searcher supports queries with any representation:
		 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.QueryProvider.QueryRepresentation#STRING}-,
		 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.QueryProvider.QueryRepresentation#OBJECT}-,
		 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.QueryProvider.QueryRepresentation#BOTH}-, and
		 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.QueryProvider.QueryRepresentation#MIXED}-representation.
		 */
		BOTH,

		/**
		 * The Searcher does <em>NOT</em> support any query representation.
		 * Searchers not supporting any query representation are broken!
		 */
		NONE;

		public static SearcherCapability determineCapability(Searcher s) {
			if(s.supportsLuceneQueryObjects() && s.supportsStringQueries()) {
				return BOTH;
			} else if (s.supportsLuceneQueryObjects()) {
				return OBJECT;
			} else if (s.supportsStringQueries()) {
				return STRING;
			} else {
				return NONE;
			}
		}
	}


	SearchResult searchFor(final String query, final PatisNumber patient) throws QueryNodeException;

	SearchResult searchFor(final Query query, final PatisNumber patient);

	Iterable<SearchResult> searchForAll(final String query, final Iterable<PatisNumber> patients);

	Iterable<SearchResult> searchForAll(final Query query, final Iterable<PatisNumber> patients);

	boolean supportsStringQueries();

	boolean supportsLuceneQueryObjects();
	
}
