// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucenerdf.concepts.lucenequery;

/**
 * Represent querying a single token (similar to a word) as a subordinate part
 * of a {@link QueryExpression}.
 *
 * <p>Lucene indexes documents as sets of fields with each field being a 
 * sequence of tokens.  {@code TokenExpression}s match these tokens that
 * Lucene indexes.  {@code TokenExpression} must be 
 * {@link QueryExpression#subexpressions()} of {@link Query Queries} that 
 * specify the field to search in.</p>
 *
 * @author Kasper van den Berg <kasper@kaspervandenberg.net> <kasper.vandenberg@maastro.nl>
 */
public interface TokenExpression extends QueryExpression {
	/**
	 * @return {@link Iterable} containing {@code TokenExpression}s that are
	 * 		part of this {@code TokenExpression}.  The returned 
	 *		{@code Iterable} follows the restrictions specified in 
	 		{@link QueryExpression#subexpressions()}

	 * @see QueryExpression#subexpressions()
	 */
	@Override
	public Iterable<? extends TokenExpression> subexpressions();
}

/* vim:set tabstop=4 shiftwidth=4 autoindent spell spelllang=en_gb : */

