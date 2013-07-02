// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

/**
 * A textual representation of a lucene query.  The SearcherWS webservice can
 * process these queries, Lucene's queryparsers (from {@link org.apache.lucene.queryparser})
 * can parse it.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface StringQuery extends Query {

	/**
	 * @return the String that represents this {@code Query}
	 */
	String getRepresentation();
	
}
