// © Maastro, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.net.URI;
import java.util.Collection;
import java.util.NoSuchElementException;
import org.apache.lucene.search.Query;

/**
 * Interface for classes that provide concept queries.  Register 
 * {@code QueryProvider}s via Java's Service Provider mechanism.  The API 
 * documentation of {@link java.util.ServiceLoader} describes how to register
 * a service provider.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface QueryProvider {
	/**
	 * Retrieve the ids of the queries that this provider can provide.
	 * 
	 * For each id ∈ returned list, either {@link #hasString(java.net.URI)},
	 * {@link #hasObject(java.net.URI)}, or both methods must return 
	 * {@code true}.
	 * 
	 * @return a collection of {@link URI}s that identify all queries that this
	 * 		{@code QueryProvider} provides.
	 */
	public Collection<URI> getQueryIds();

	/**
	 * Retrieve whether the provider has the query available as a string that
	 * can be send to {@link org.vle.aid.lucene.SearcherWS}.
	 * When {@code hasString} returns {@code true} for a given {@code id},
	 * {@link #getAsString(java.net.URI)} must return the string representation
	 * for the query.
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
	public boolean hasString(final URI id);

	/**
	 * Retrieve whether the provider has the query available as a Lucene 
	 * {@link org.apache.lucene.search.Query} object.
	 * When {@code hasObject} returns {@code true} for a given {@code id},
	 * {@link AbstractMethodError#getAsObject(java.net.URI)} must return the
	 * query as object.
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
	public boolean hasObject(final URI id);

	/**
	 * Retrieve the string representation of the query identified by {@code id}.
	 * 
	 * 
	 * @param id	the URI that identifies the query
	 * 
	 * @return	the string representation of the query, in a format that 
	 * 		{@link org.vle.aid.lucene.SearcherWS} accepts.
	 * 
	 * @throws NoSuchElementException	when this {@code QueryProvider} can not
	 * 		provide the query (represented as string).
	 */
	public String getAsString(final URI id) throws NoSuchElementException;

	/**
	 * Retrieve the string representation of the query identified by {@code id}.
	 * 
	 * 
	 * @param id	the URI that identifies the query
	 * 
	 * @return	the {@link org.apache.lucene.search.Query} that corresponds to
	 * 		{@code id}
	 * 
	 * @throws NoSuchElementException	when this {@code QueryProvider} can not
	 * 		provide the query (as a {@code Query}-object).
	 */
	public Query getAsObject(final URI id) throws NoSuchElementException;
}
