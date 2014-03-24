// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucenerdf.concepts.lucenequery;

import nl.maastro.eureca.lucenerdf.concepts.lucenequery.binding.Variable;

/**
 * Interface that classes implement when the require knowing the specific type of
 * {@link QueryExpression}s they operate on.
 * 
 * @param <T>	type that {@code visit…}-methods return.
 *
 * @see "[GoF] Visitor pattern"
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface QueryVisitor<T> {
	public T visitDefault(QueryExpression visited);
	public T visitLiteral(Literal visited);
	public T visitVariable(Variable visited);
	public T visitTerm(TermQuery visited);
/*
	public T visitLiteral(Literal visited);
	public T visitBoolean(BooleanQuery visited);
	public T visitPattern(PatternQuery visited);
	public T visitSpan(SpanQuery visited);
	public T visitFuzzy(FuzzyQuery visited);
*/
}

/* vim:set tabstop=4 shiftwidth=4 autoindent spell spelllang=en_gb : */

