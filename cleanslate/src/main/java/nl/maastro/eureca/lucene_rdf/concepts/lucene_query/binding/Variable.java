// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucene_rdf.concepts.lucene_query.binding;

import dataflow.quals.Pure;
import java.util.Map;
import java.util.Set;
import nl.maastro.eureca.lucene_rdf.concepts.auxiliary.Identifier;
import nl.maastro.eureca.lucene_rdf.concepts.lucene_query.Query_Expression;
import nl.maastro.eureca.lucene_rdf.concepts.lucene_query.Query_Visitor;

/**
 * Describes a part of a 
 * {@link nl.maastro.eureca.lucene_rdf.concepts.lucene_query.Query_Expression}}
 * that can vary.
 */
public interface Variable extends Query_Expression {
	public enum State {
		/**
		 * This {@code Variable} was {@link Variable#set }
		 * to a value.
		 */
		BOUND,

		/**
		 * This {@code Variable} is not {@link Variable#set} to a value.
		 */
		UNBOUND
	};

	
	/**
	 * Indicates that this {@code Variable} being {@link State#UNBOUND} caused,
	 * a method to failed.
	 * 
	 * @see Variable#get_value() 
	 */
	public static class Unbound_Variable_Exception extends IllegalStateException {
		private static final long serialVersionUID = 1L;
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

	
	/**
	 * Some variables will not support all subtypes of {@link Query_Expression};
	 * {@code Illegal_Value_Type_Exception} indicates .
	 */
	public static class Illegal_Value_Type_Exception extends  IllegalArgumentException {
		private static final long serialVersionUID = 1L;
		public Illegal_Value_Type_Exception() {
		}

		public Illegal_Value_Type_Exception(String s) {
			super(s);
		}

		public Illegal_Value_Type_Exception(String message, Throwable cause) {
			super(message, cause);
		}

		public Illegal_Value_Type_Exception(Throwable cause) {
			super(cause);
		}
	};

	
	/**
	 * Bind this {@code Variable}'s value to {@code new_value}.
	 * 
	 * <p>Calling {@code set()} multiple times is allowed.  {@link #get()}
	 * will return the value set most recently.</p>
	 * 
	 * @throws nl.maastro.eureca.lucene_rdf.concepts.lucene_query.binding.Variable.Illegal_Value_Type_Exception
	 * 		when ∄ C∊{@link #get_accepted_types()} : {@code new_value instanceof C}
	 */
	public void set(Query_Expression new_value)
			throws Illegal_Value_Type_Exception;

	
	/**
	 * @return	the value most recently {@link 
	 * 		#set(nl.maastro.eureca.lucene_rdf.concepts.lucene_query.Query_Expression)}
	 * 		the returned value will be an instance of one of the types returned
	 * 		by {@link #get_accepted_types()}
	 * 
	 * @throws nl.maastro.eureca.lucene_rdf.concepts.lucene_query.binding.Variable.Unbound_Variable_Exception
	 * 		if and only if {@link #state()} == {@link State#UNBOUND}
	 */
	public Query_Expression get_value()
			throws Unbound_Variable_Exception;
	
	
	/**
	 * @return	whether this {@code Variable} is bound to a value. 
	 */
	public State state();


	/**
	 * @return	the types of {@link Query_Expression} that {@link #set} accepts
	 * 		and as a consequence {@link #get()} may return.
	 */
	public Set<Class<? extends Query_Expression>> get_accepted_types();
	

	/**
	 * Visitor pattern: call the {@code visitor}'s {@link 
	 * Query_Visitor#visit_variable}-method.
	 *
	 * @param <T>	type of result that {@code visitor} returns, is opaque
	 *		to {@code Query}.
	 * @return		the result of the {@code visit_variable()}-method.
	 */
	@Override
	public <T> T accept(Query_Visitor<T> visitor);


	/**
	 * Access the bound value (same as {@link #get_value()}.
	 * 
	 * @see Query_Expression#subexpressions() 
	 *
	 * @return either
	 * 		<ul><li>a singleton containing {@link #get_value()}; or</li>
	 * 		<li>an empty iterable.</li></ul>
	 */
	@Override
	public Iterable<Query_Expression> subexpressions();


	/**
	 * Sub{@link Variable}s that {@link #get_value()} has.
	 *
	 * <p>the returned {@code Map} will <em>not</em> contain
	 * {@code this}.</p>
	 * 
	 * @see Query_Expression#variables()
	 */
	@Override
	public Map<Identifier, Variable> variables();


	/**
	 * @return always empty.
	 * 
	 * @see Query_Expression#direct_variables() 
	 */
	@Override
	public Map<Identifier, Variable> direct_variables();


}

/* vim:set tabstop=4 shiftwidth=4 autoindent : */


