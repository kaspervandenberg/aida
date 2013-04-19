// © Maastro Clinics, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.io.File;
import java.rmi.RemoteException;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.maastro.vocab.axis.services.SearcherWS.SearcherWS;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

/**
 * Wrapped arround the SearcherWS webservice that allows searching on 
 * Patisnumber.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
class WebserviceSearcher extends SearcherBase {
	private static final Logger log = Logger.getLogger(WebserviceSearcher.class.getName());
	private final nl.maastro.vocab.axis.services.SearcherWS.SearcherWS service;
	private final String index;

	public WebserviceSearcher(SearcherWS service_, String index_, final File index, String defaultField_, int maxResults_, final ForkJoinPool taskPool_) {
		super(defaultField_, maxResults_, taskPool_);
		this.service = service_;
		this.index = index_;
	}

	@Override
	public SearchResult searchFor(String query, PatisNumber patient) throws QueryNodeException {
		String constructedQuery = patient.compose(query);
		try {
			String resultJson = service.searchJason(index, constructedQuery, "0", getDefaultField(), String.valueOf(getMaxResults()));
			return SearchResult.create(patient, resultJson);
		} catch (RemoteException ex) {
			log.log(Level.SEVERE, String.format("RPC Error when calling searchJason on the webservice"), ex);
			return SearchResult.NO_RESULT();
		}
	}

	@Override
	public SearchResult searchFor(Query query, PatisNumber patient) {
		throw new UnsupportedOperationException("Webservice only supports String queries.");
	}
	
}
