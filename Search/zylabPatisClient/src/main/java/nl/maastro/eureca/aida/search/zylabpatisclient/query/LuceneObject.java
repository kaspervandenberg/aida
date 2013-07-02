// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

/**
 * A query in a representation that can be executed by Lucene.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface LuceneObject extends Query {

	/**
	 * @return the Lucene Query that represents this {@code Query}
	 */
	org.apache.lucene.search.Query getRepresentation();
	
}
