// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucene_rdf.concepts.lucene_query;

/**
 * Interface that classes implement when the require knowing the specific type of
 * {@link Query_Expression}s they operate on.
 * 
 * @param <T>	type that {@code visit…}-methods return.
 *
 * @see [GoF] Visitor pattern
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface Query_Visitor<T> {
	public T visit_default(Query_Expression visited);
/*
	public T visit_term(Query_Term visited);
	public T visit_boolean(Boolean_Query visited);
	public T visit_pattern(Pattern_Query visited);
	public T visit_span(Span_Query visited);
	public T visit_fuzzy(Fuzzy_Query visited);
*/
}

/* vim:set tabstop=4 shiftwidth=4 autoindent : */


