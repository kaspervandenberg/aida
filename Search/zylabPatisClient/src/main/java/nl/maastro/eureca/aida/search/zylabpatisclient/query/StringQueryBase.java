// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

/**
 * A textual representation of a lucene query.  The SearcherWS webservice can
 * process these queries, Lucene's queryparsers (from {@link org.apache.lucene.queryparser})
 * can parse it.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public abstract class StringQueryBase implements StringQuery {

	/**
	 * Visitor pattern; calls {@link nl.maastro.eureca.aida.search.zylabpatisclient.query.Query.Visitor#visit(StringQuery)}.
	 */
	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visit(this);
	}
}
