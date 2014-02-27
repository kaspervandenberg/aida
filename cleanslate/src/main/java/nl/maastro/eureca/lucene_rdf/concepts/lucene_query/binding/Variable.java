// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucene_rdf.concepts.lucene_query.binding;

/**
 * Describes a part of a 
 * {@link nl.maastro.eureca.lucene_rdf.concepts.lucene_query.Query_Expression}}
 * that can vary.
 */
public interface Variable {
	public enum State {
		BOUND,
		UNBOUND
	};

	public void set(Query_Expression new_value);
	public State state();
	public Query_Expression get_value();
}

/* vim:set tabstop=4 shiftwidth=4 autoindent : */


