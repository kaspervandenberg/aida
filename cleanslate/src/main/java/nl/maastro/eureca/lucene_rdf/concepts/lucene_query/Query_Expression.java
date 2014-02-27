// © Kasper van den Berg, 2014

package nl.maastro.eureca.lucene_rdf.concepts.lucene_query;

import nl.maastro.eureca.lucene_rdf.concepts.auxiliary.Identifier;
import nl.maastro.eureca.lucene_rdf.concepts.lucene_query.binding.Variable;
import nl.maastro.eureca.lucene_rdf.concepts.lucene_query.binding.Binding;

/**
 * Top level interface representing queries in Lucene.
 */
public interface Query_Expression {
	/**
	 * Visitor pattern: call the {@code visitor}'s method that corresponds
	 * to this class.
	 *
	 * @param <T>	type of result that {@code visitor} returns, is opaque
	 *		to {@code Query}.
	 * @return		the result of the {@code visit_…}-metohd.
	 */
	public <T> T accept(Query_Visitor<T> visitor);


	/**
	 * Access the subexpressions that this {@code Query_Expression} contains.
	 *
	 * The returned {@link Iterable}'s {@link java.util,Iterator} must support
	 * {@link java.util.Iterator#hasNext()} and {@link java.util.Iterator#next()}.
	 * {@link java.util.Iterator#remove()} is optional a may throw
	 * {@link UnsupportedException}.
	 *
	 * When this {@code Query_Expression} contains no subexpressions, the
	 * {@code Iterable} must return an empty iterator (e.g. 
	 * {@link java.util.Collections#emptyIterator()}).
	 *
	 * The {@code Iterable} must only iterate over the direct subexpressions;
	 * it must NOT return transitive subexpression (i.e subexpressions of 
	 * subexpressions).
	 *
	 * @return an {@link Iterable} that complies with the three specifications
	 *		stated above.
	 */
	public Iterable<Query_Expression> subexpressions();


	/**
	 * 
	 */
	public Map<Identifier, Variable> variables();

}

/* vim:set tabstop=4 shiftwidth=4 autoindent : */

