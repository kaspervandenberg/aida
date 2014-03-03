// © Kasper van den Berg, 2014

package nl.maastro.eureca.lucene_rdf.concepts.lucene_query;

import nl.maastro.eureca.lucene_rdf.concepts.auxiliary.Identifier;
import nl.maastro.eureca.lucene_rdf.concepts.lucene_query.binding.Variable;
import nl.maastro.eureca.lucene_rdf.concepts.lucene_query.binding.Binding;

import java.util.Map;

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
	 * <p>The returned {@link Iterable}'s {@link java.util,Iterator} must support
	 * {@link java.util.Iterator#hasNext()} and {@link java.util.Iterator#next()}.
	 * {@link java.util.Iterator#remove()} is optional a may throw
	 * {@link UnsupportedException}.</p>
	 *
	 * <p>When this {@code Query_Expression} contains no subexpressions, the
	 * {@code Iterable} must return an empty iterator (e.g. 
	 * {@link java.util.Collections#emptyIterator()}).</p>
	 *
	 * <p>The {@code Iterable} must only iterate over the direct subexpressions;
	 * it must <em>NOT</em> return transitive subexpression (i.e subexpressions
	 * of subexpressions).</p>
	 *
	 * @return an {@link Iterable} that complies with the three specifications
	 *		stated above.
	 */
	public Iterable<Query_Expression> subexpressions();


	/**
	 * {@link Variable}s that this {@code Query_Expression} has.
	 *
	 * <p>The map of variables contains both the direct {@code Variables} of
	 * this {@code Query_Expression} <em>and</em> the variables of
	 * subexpressions.  Use {@link #direct_variables()} to retrieve only 
	 * the direct variables.  Prefer {@code variables()} to 
	 * {@code direct_variables()}.</p>
	 * 
	 * <p>This {@code Query_Expression} must ensure that identifiers of
	 * variables from subexpressions differ.  The {@code Query_Expression}
	 * can change the identifiers of variables of subexpressions.</p>
	 *
	 * <p>When this {@code Query_Expression} nor any of its subexpressions 
	 * have variables the map will be {@link java.util.Map#isEmpty() empty}.</p>
	 *
	 * <p>The returned map must support read only access; methods such as
	 * {@link java.util.Map#put(Object, Object)} and 
	 * {@link java.util.Map#remove(Object)} may throw a 
	 * {@link UnsupportedException}.</p>
	 *
	 * @return a {@link java.util.Map} that complies with the specifications
	 *		stated above.
	 */
	public Map<Identifier, Variable> variables();


	/**
	 * Only the {@link Variable}s that this {@code Query_Expression} directely
	 * contains, <em>not</em> {@code Variable} that subexpressions contain.
	 *
	 * @see #variables()
	 */
	public Map<Identifier, Variable> direct_variables();

}

/* vim:set tabstop=4 shiftwidth=4 autoindent : */

