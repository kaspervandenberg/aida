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

	public static class Unbound_Variable_Exception extends IllegalStateException {
		public Unbound_Variable_Exception() {
			super();
		}

		public Unbound_Variable_Exception(String message) {
			super(message);
		}

		public Unbound_Variable_Exception(String message, Throwable cause) {
			super(message, cause);
		}

		public Unbound_Variable_Exception(Throwable cause) {
			super(cause);
		}
	};

	public void set(Object new_value);
	public State state();
	public Object get_value();
}

/* vim:set tabstop=4 shiftwidth=4 autoindent : */


