// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

/**
 * A query in a representation that can be executed by Lucene.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public abstract class LuceneObjectBase implements LuceneObject {

	/**
	 * Visitor pattern; calls {@link nl.maastro.eureca.aida.search.zylabpatisclient.query.Query.Visitor#visit(LuceneObject)}.
	 */
	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visit(this);
	}
}