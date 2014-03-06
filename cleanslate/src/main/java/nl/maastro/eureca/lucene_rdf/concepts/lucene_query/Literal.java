// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucene_rdf.concepts.lucene_query;

import dataflow.quals.Pure;

/**
 * A literal string uses as part of a {@link Query_Expression}.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface Literal extends Query_Expression {
	/**
	 * @return	the value of this literal 
	 */
	@Pure
	public String get_value();


	/**
	 * Visitor pattern: call the {@code visitor}'s {@link 
	 * Query_Visitor#visit_literal}-method.
	 *
	 * @param <T>	type of result that {@code visitor} returns, is opaque
	 *		to {@code Query}.
	 * @return		the result of the {@code visit_…}-method.
	 */
	@Override
	public <T> T accept(Query_Visitor<T> visitor);


	/**
	 * {@code Literal}s have no subexpressions.
	 * 
	 * @see Query_Expression#subexpressions() 
	 *
	 * @return an empty {@link Iterable}
	 */
	@Override
	@Pure
	public Iterable<Query_Expression> subexpressions();


	/**
	 * @code {Literal}s have no variables.
	 * 
	 * @see Query_Expression#variables() 
	 *
	 * @return an empty {@link java.util.Map}.
	 */
	@Override
	@Pure
	public java.util.Map<
				nl.maastro.eureca.lucene_rdf.concepts.auxiliary.Identifier,
				nl.maastro.eureca.lucene_rdf.concepts.lucene_query.binding.Variable>
			variables();


	/**
	 * @code {Literal}s have no variables.
	 * 
	 * @see Query_Expression#direct_variables() 
	 *
	 * @return an empty {@link java.util.Map}.
	 */
	@Override
	@Pure
	public java.util.Map<
				nl.maastro.eureca.lucene_rdf.concepts.auxiliary.Identifier,
				nl.maastro.eureca.lucene_rdf.concepts.lucene_query.binding.Variable>
			direct_variables();
}
