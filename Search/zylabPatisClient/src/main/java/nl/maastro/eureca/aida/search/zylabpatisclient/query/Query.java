// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

import javax.xml.namespace.QName;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface Query {
	/**
	 * Visitor pattern.  Visitors with query type specific operations must 
	 * implement this interface to receive the callback from this Query.
	 */
	public interface Visitor<T> {
		public T visit(LuceneObject element);
		public T visit(StringQuery element);
		public T visit(ParseTree element);
	}

	/**
	 * Visitor pattern; {@code accept} will call either
	 * <ul><li>{@link Visitor#visit(LuceneObject)},</li>
	 * 		<li>{@link Visitor#visit(ParseTree)}, or</li>
	 * 		<li>{@link Visitor#visit(StringQuery)}</li></ul>
	 * 
	 * @param visitor	the object to call back. 
	 */
	public <T> T accept(Visitor<T> visitor);

	/**
	 * @return the {@link QName} via which the query is provided.
	 */
	public QName getName();
}
