// © Kasper van den Berg, 2014

package nl.maastro.eureca.lucenerdf.concepts.lucenequery;

import nl.maastro.eureca.lucenerdf.concepts.auxiliary.Identifier;
import nl.maastro.eureca.lucenerdf.concepts.lucenequery.binding.Variable;

import java.util.Map;

/**
 * Top level interface representing queries in Lucene.
 */
public interface QueryExpression {
	/**
	 * Visitor pattern: call the {@code visitor}'s method that corresponds
	 * to this class.
	 *
	 * @param <T>	type of result that {@code visitor} returns, is opaque
	 *		to {@code Query}.
	 * @return		the result of the {@code visit_…}-method.
	 */
	public <T> T accept(QueryVisitor<T> visitor);


	/**
	 * Access the subexpressions that this {@code QueryExpression} contains.
	 *
	 * <p>The returned {@link Iterable}'s {@link java.util.Iterator} must support
	 * {@link java.util.Iterator#hasNext()} and {@link java.util.Iterator#next()}.
	 * {@link java.util.Iterator#remove()} is optional a may throw
	 * {@link UnsupportedOperationException}.</p>
	 *
	 * <p>When this {@code QueryExpression} contains no subexpressions, the
	 * {@code Iterable} must return an empty iterator (e.g. 
	 * {@link java.util.Collections#emptyIterator()}).</p>
	 *
	 * <p>The {@code Iterable} must only iterate over the direct subexpressions;
	 * it must <em>NOT</em> return transitive subexpression (i.e subexpressions
	 * of subexpressions).  The subexpressions should form an a-cyclic directed
	 * graph.</p>
	 *
	 * @return an {@link Iterable} that complies with the three specifications
	 *		stated above.
	 */
	public Iterable<QueryExpression> subexpressions();


	/**
	 * {@link Variable}s that this {@code QueryExpression} has.
	 *
	 * <p>The map of variables contains both the direct {@code Variables} of
	 * this {@code QueryExpression} <em>and</em> the variables of
	 * subexpressions.  Use {@link #directVariables()} to retrieve only 
	 * the direct variables.  Prefer {@code variables()} to 
	 * {@code directVariables()}.</p>
	 * 
	 * <p>This {@code QueryExpression} must ensure that identifiers of
	 * variables from subexpressions differ.  The {@code QueryExpression}
	 * can change the identifiers of variables of subexpressions.</p>
	 *
	 * <p>When this {@code QueryExpression} nor any of its subexpressions 
	 * have variables the map will be {@link java.util.Map#isEmpty() empty}.</p>
	 *
	 * <p>The returned map must support read only access; methods such as
	 * {@link java.util.Map#put(Object, Object)} and 
	 * {@link java.util.Map#remove(Object)} may throw a 
	 * {@link UnsupportedOperationException}.</p>
	 *
	 * @return a {@link java.util.Map} that complies with the specifications
	 *		stated above.
	 */
	public Map<Identifier, Variable> variables();


	/**
	 * @return Map with only the {@link Variable}s that this 
	 * 		{@code QueryExpression}	directely contains, <em>not</em>
	 * 		{@code Variable}s that subexpressions contain.
	 * 
	 * @see #variables()
	 */
	public Map<Identifier, Variable> directVariables();

}

/* vim:set tabstop=4 shiftwidth=4 autoindent spell spelllang=en_gb : */

