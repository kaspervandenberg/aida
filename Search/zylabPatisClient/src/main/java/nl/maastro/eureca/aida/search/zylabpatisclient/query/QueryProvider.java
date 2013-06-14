// © Maastro, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

import java.util.Collection;
import java.util.NoSuchElementException;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;

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
	 * The types of representation that a provided query can have.
	 */
	@Deprecated
	public enum QueryRepresentation_Old {
		/**
		 * The Query(ies) is (are) <em>only</em> provided as String; that is  
		 * {@link #hasString(javax.xml.namespace.QName)} returns {@code true};
		 * and {@link #hasObject(javax.xml.namespace.QName)} returns 
		 * {@code false}.
		 */
		STRING(true, false),

		/**
		 * The Query(ies) is (are) <em>only</em> provided as a 
		 * {@link org.apache.lucene.search.Query Lucene Query-object}; that is
		 * {@link #hasString(javax.xml.namespace.QName)} returns {@code false); and
		 * {@link #hasObject(javax.xml.namespace.QName)} returns {@code true}. 
		 */
		OBJECT(false, true),

		/**
		 * The Query(ies) is (are) provided as <em>both</em> a String and a
		 * {@link org.apache.lucene.search.Query Lucene Query-object}; that is
		 * both {@link #hasString(javax.xml.namespace.QName)} and
		 * {@link #hasObject(javax.xml.namespace.QName)} return {@code true}. 
		 */
		BOTH(true, true),

		/**
		 * Some of the queries in the collection are provided as only String
		 * while others are only provided as a 
		 * {@link org.apache.lucene.search.Query Lucene Query-object}.  Some
		 * queries may be provided in both representations.
		 */
		MIXED(false, false),

		/**
		 * The query(ies) is (are) not known by the provider.
		 */
		UNKNOWN_QUERY(false, false);

		private final boolean asString;
		private final boolean asObject;
		
		private QueryRepresentation_Old(
				final boolean asString_, final boolean asObject_) {
			asString = asString_;
			asObject = asObject_;
		}

		public static QueryRepresentation_Old determineRepresentation(
				QueryProvider context, Iterable<QName> queries) {
			boolean allAsString = true;
			boolean allAsObject = true;

			for (QName q : queries) {
				QueryRepresentation_Old r = determineRepresentation(context, q);
				if(r.equals(UNKNOWN_QUERY)) {
					return UNKNOWN_QUERY;
				}
				allAsString &= r.asString;
				allAsObject &= r.asObject;
			}

			if(allAsString && allAsObject) {
				return BOTH;
			} else if (allAsObject) {
				return OBJECT;
			} else if (allAsString) {
				return STRING;
			} else {
				return MIXED;
			}
		}
		
		public static QueryRepresentation_Old determineRepresentation(
				QueryProvider context, QName query) {
			boolean asString = context.hasString(query);
			boolean asObject = context.hasObject(query);

			if(asString && asObject) {
				return BOTH;
			} else if (asObject) {
				return OBJECT;
			} else if (asString) {
				return STRING;
			} else {
				return UNKNOWN_QUERY;
			}
		}
	}


	/**
	 * Retrieve the ids of the queries that this provider can provide.
	 * 
	 * For each id ∈ returned list, either 
	 * {@link #hasString(javax.xml.namespace.QName)},
	 * {@link #hasObject(javax.xml.namespace.QName)}, or both methods must 
	 * return {@code true}.
	 * 
	 * @return a collection of {@link QName}s that identify all queries that this
	 * 		{@code QueryProvider} provides.
	 */
	public Collection<QName> getQueryIds();

	/**
	 * Retrieve whether this {@code QueryProvider} can provide the {@link Query}
	 * identified by {@code id}.
	 * 
	 * @param id	the {@link QName} by which the query is known
	 * @return	<ul><li>{@code true}, the provider can provide the query 
	 * 				identified by {@code id}; or </li>
	 * 			<li>{@code false}, the provider cannot provide the requested 
	 * 				query.</li></ul>
	 */
	public boolean provides(QName id);

	/**
	 * Retrieve the query identified by {@code id}.
	 * 
	 * @param id	the {@link QName} that identifies the query
	 * 
	 * @return	the query
	 * 
	 * @throws NoSuchElementException	when this {@code QueryProvider} can not
	 * 		provide the query.
	 */
	public Query get(QName id);
	
	/**
	 * Retrieve whether the provider has the query available as a string that
	 * can be send to {@link org.vle.aid.lucene.SearcherWS}.
	 * When {@code hasString} returns {@code true} for a given {@code id},
	 * {@link #getAsString(javax.xml.namespace.QName)} must return the string
	 * representation for the query.
	 * 
	 * @param id	the {@link QName} that identifies the query
	 * 
	 * @return	<ul><li>{@code true}, this provider can provide the query in
	 * 			a string format; or</li>
	 * 		<li>{@code false}, this provider can not provide the query in a 
	 * 			string format, i.e. the provider has the query only as
	 * 			{@link org.apache.lucene.search.Query} or the query is unknown
	 * 			to this provider.</li></ul>
	 */
	@Deprecated
	public boolean hasString(final QName id);

	/**
	 * Retrieve whether the provider has the query available as a Lucene 
	 * {@link org.apache.lucene.search.Query} object.
	 * When {@code hasObject} returns {@code true} for a given {@code id},
	 * {@link #getAsObject(javax.xml.namespace.QName)} must return the
	 * query as object.
	 * 
	 * @param id	the {@link QName} that identifies the query
	 * 
	 * @return	<ul><li>{@code true}, this provider can provide the query as a
	 * 			Lucene {@code Query}-object; or</li>
	 * 		<li>{@code false}, this provides can not provide the query as a 
	 * 			{@code Query}-object, i.e. the provider has the query only in
	 * 			String format or the query is unknown to this provider.
	 * 		</li></ul>
	 */
	@Deprecated
	public boolean hasObject(final QName id);

	/**
	 * Retrieve the string representation of the query identified by {@code id}.
	 * 
	 * 
	 * @param id	the {@link QName} that identifies the query
	 * 
	 * @return	the string representation of the query, in a format that 
	 * 		{@link org.vle.aid.lucene.SearcherWS} accepts.
	 * 
	 * @throws NoSuchElementException	when this {@code QueryProvider} can not
	 * 		provide the query (represented as string).
	 */
	@Deprecated
	public String getAsString(final QName id) throws NoSuchElementException;

	/**
	 * Retrieve the string representation of the query identified by {@code id}.
	 * 
	 * 
	 * @param id	the {@link QName} that identifies the query
	 * 
	 * @return	the {@link org.apache.lucene.search.Query} that corresponds to
	 * 		{@code id}
	 * 
	 * @throws NoSuchElementException	when this {@code QueryProvider} can not
	 * 		provide the query (as a {@code Query}-object).
	 */
	@Deprecated
	public org.apache.lucene.search.Query getAsObject(final QName id) throws NoSuchElementException;
}
