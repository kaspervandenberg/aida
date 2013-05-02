// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.WeakHashMap;
import org.apache.lucene.search.Query;

/**
 * Use all registered {@link QueryProvider}s to resolve query IDs to queries.
 * 
 * <p><i>NOTE: Although {@code QueryResolver} implements all methods that
 * {@link QueryProvider} defines, it deliberately does <em>not</em> implement
 * the interface to avoid infinite recursion.</i></p>
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class QueryResolver {
	private final ServiceLoader<QueryProvider> queryProviders;
	private final WeakHashMap<URI, QueryProvider> queryCache = new WeakHashMap<>();
	private Set<URI> queryIDs = null;

	public QueryResolver() {
		queryProviders = ServiceLoader.load(QueryProvider.class);
	}

	/**
	 * Retrieve all available query IDs that the combined {@link QueryProvider}s
	 * provide.
	 * 
	 * @see QueryProvider#getQueryIds() 
	 * 
	 * @return	an immutable {@link Set} of {@link URI}s identifying provided
	 * 		queries.
	 */
	public Collection<URI> getQueryIds() {
		if(queryIDs == null) {
			Set<URI> tmp = new HashSet<>();
			Iterator<QueryProvider> iter = queryProviders.iterator();
			while(iter.hasNext()) {
				tmp.addAll(iter.next().getQueryIds());
			}
			queryIDs = Collections.unmodifiableSet(tmp);
		}
		return queryIDs;
	}

	/**
	 * Forward to {@link QueryProvider#hasString(java.net.URI)} of the
	 * {@link QueryProvider provider} that provides {@code id}.
	 * 
	 * @see QueryProvider#hasString(java.net.URI) 
	 * 
	 * @param id	the URI that identifies the query
	 * 
	 * @return	<ul><li>{@code true}, this provider can provide the query in
	 * 			a string format; or</li>
	 * 		<li>{@code false}, this provider can not provide the query in a 
	 * 			string format, i.e. the provider has the query only as
	 * 			{@link org.apache.lucene.search.Query} or the query is unknown
	 * 			to this provider.</li></ul>
	 */
	public boolean hasString(URI id) {
		try {
			return findProvider(id).hasString(id);
		} catch (NoSuchElementException ex) {
			return false;
		}
	}

	/**
	 * Forward to {@link QueryProvider#hasObject(java.net.URI)} of the
	 * {@link QueryProvider provider} that provides {@code id}.
	 * 
	 * @see QueryProvider#hasObject(java.net.URI) 
	 * 
	 * @param id	the URI that identifies the query
	 * 
	 * @return	<ul><li>{@code true}, this provider can provide the query as a
	 * 			Lucene {@code Query}-object; or</li>
	 * 		<li>{@code false}, this provides can not provide the query as a 
	 * 			{@code Query}-object, i.e. the provider has the query only in
	 * 			String format or the query is unknown to this provider.
	 * 		</li></ul>
	 */
	public boolean hasObject(URI id) {
		try {
			return findProvider(id).hasObject(id);
		} catch (NoSuchElementException ex) {
			return false;
		}
	}

	/**
	 * Forward to {@link QueryProvider#getAsString(java.net.URI)} of the 
	 * {@link QueryProvider provider} that provides {@code id}.
	 * 
	 * @see QueryProvider#getAsString(java.net.URI) 
	 * 
	 * @param id	the URI that identifies the query
	 * 
	 * @return	the string representation of the query, in a format that 
	 * 		{@link org.vle.aid.lucene.SearcherWS} accepts.
	 * 
	 * @throws NoSuchElementException	when the {@link QueryProvider} of 
	 * 		{@code id} can not provide the query represented as string; or
	 * 		when no provider provides {@code id}.
	 */
	public String getAsString(final URI id) throws NoSuchElementException {
		return findProvider(id).getAsString(id);
	}

	/**
	 * Forward to {@link QueryProvider#getAsObject(java.net.URI)} of the
	 * {@link QueryProvider provider} that provides {@code id}.
	 * 
	 * @see QueryProvider#getAsObject(java.net.URI) 
	 * 
	 * @param id	the URI that identifies the query
	 * 
	 * @return	the {@link org.apache.lucene.search.Query} that corresponds to
	 * 		{@code id}
	 * 
	 * @throws NoSuchElementException	when the {@link QueryProvider} of 
	 * 		{@code id} can not provide the query as a {@code Query}-object; or
	 * 		when no provider provides(id).
	 */
	public Query getAsObject(final URI id) throws NoSuchElementException {
		return findProvider(id).getAsObject(id);
	}

	/**
	 * Retrieve the {@link QueryProvider} that provides the query identified by
	 * {@code id}.
	 * 
	 * @param id	identifier of the query
	 * 
	 * @return		the {@link QueryProvider} that provides {@code id}
	 * 
	 * @throws NoSuchElementException 	when no registered {@link QueryProvider}
	 * 		provides a query identified by {@code id}.
	 */
	private QueryProvider findProvider(final URI id) throws NoSuchElementException {
		if(queryCache.containsKey(id)) {
			return queryCache.get(id);
		} else {
			Iterator<QueryProvider> iter = queryProviders.iterator();
			while(iter.hasNext()) {
				QueryProvider provider = iter.next();
				if(provider.getQueryIds().contains(id)) {
					queryCache.put(id, provider);
					return provider;
				}
			}
			throw new NoSuchElementException(String.format(
					"Query identified by %s not found"));
		}
	}
}
