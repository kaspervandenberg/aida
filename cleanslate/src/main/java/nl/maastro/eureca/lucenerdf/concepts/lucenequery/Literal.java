// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucenerdf.concepts.lucenequery;

import dataflow.quals.Pure;

/**
 * A literal string uses as part of a {@link QueryExpression}.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface Literal extends QueryExpression {
	/**
	 * @return	the value of this literal 
	 */
	@Pure
	public String getValue();


	/**
	 * Visitor pattern: call the {@code visitor}'s {@link 
	 * QueryVisitor#visitLiteral}-method.
	 *
	 * @param <T>	type of result that {@code visitor} returns, is opaque
	 *		to {@code Query}.
	 * @return		the result of the {@code visit_…}-method.
	 */
	@Override
	public <T> T accept(QueryVisitor<T> visitor);


	/**
	 * {@code Literal}s have no subexpressions.
	 * 
	 * @see QueryExpression#subexpressions() 
	 *
	 * @return an empty {@link Iterable}
	 */
	@Override
	@Pure
	public Iterable<? extends QueryExpression> subexpressions();


	/**
	 * @code {Literal}s have no variables.
	 * 
	 * @see QueryExpression#variables() 
	 *
	 * @return an empty {@link java.util.Map}.
	 */
	@Override
	@Pure
	public java.util.Map<
				nl.maastro.eureca.lucenerdf.concepts.auxiliary.Identifier,
				? extends nl.maastro.eureca.lucenerdf.concepts.lucenequery.binding.Variable>
			variables();


	/**
	 * @code {Literal}s have no variables.
	 * 
	 * @see QueryExpression#directVariables() 
	 *
	 * @return an empty {@link java.util.Map}.
	 */
	@Override
	@Pure
	public java.util.Map<
				nl.maastro.eureca.lucenerdf.concepts.auxiliary.Identifier,
				nl.maastro.eureca.lucenerdf.concepts.lucenequery.binding.Variable>
			directVariables();
}

/* vim:set tabstop=4 shiftwidth=4 autoindent spell spelllang=en_gb : */

