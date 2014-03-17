// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucenerdf.concepts.lucenequery;

/**
 * Query a single token in a field in a lucene document.
 *
 * <p>Lucene normally views documents as a collection of fields that each
 * contain a sequence of tokens.  Tokens roughly correspond to words of 
 * the documents.  {@code QueryTerm} represents searching
 * for a single token in a single field.</p>
 *
 * <p>Since {@code QueryTerm} represents searching a <em>single</em> token
 * in a <em>single</em> field {@link #getToken()} and {@link #getField()}
 * should return only {@link Literal}, {@link PatternQuery}, and
 * {@link nl.maastro.eureca.lucenerdf.concepts.lucenequery.binding.Variable}.
 * Use 
 * </p>
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface QueryTerm extends QueryExpression {
	/**
	 * 
	 */
	public QueryExpression getToken();
	public QueryExpression getField();
}

/* vim:set tabstop=4 shiftwidth=4 autoindent spell spelllang=en_gb : */
