// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucenerdf.concepts.lucenequery.binding;

import dataflow.quals.Pure;
import java.util.Map;
import java.util.Set;
import nl.maastro.eureca.lucenerdf.concepts.auxiliary.Identifier;
import nl.maastro.eureca.lucenerdf.concepts.lucenequery.QueryExpression;
import nl.maastro.eureca.lucenerdf.concepts.lucenequery.QueryVisitor;

/**
 * Describes a part of a 
 * {@link nl.maastro.eureca.lucenerdf.concepts.lucenequery.QueryExpression}}
 * that can vary.
 */
public interface Variable extends QueryExpression {
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
	 * @see Variable#getValue() 
	 */
	public static class UnboundVariableException extends IllegalStateException {
		private static final long serialVersionUID = 1L;
		public UnboundVariableException() {
			super();
		}

		public UnboundVariableException(String message) {
			super(message);
		}

		public UnboundVariableException(String message, Throwable cause) {
			super(message, cause);
		}

		public UnboundVariableException(Throwable cause) {
			super(cause);
		}
	};

	
	/**
	 * Some variables will not support all subtypes of {@link QueryExpression};
	 * {@code IllegalValueTypeException} indicates .
	 */
	public static class IllegalValueTypeException extends  IllegalArgumentException {
		private static final long serialVersionUID = 1L;
		public IllegalValueTypeException() {
		}

		public IllegalValueTypeException(String s) {
			super(s);
		}

		public IllegalValueTypeException(String message, Throwable cause) {
			super(message, cause);
		}

		public IllegalValueTypeException(Throwable cause) {
			super(cause);
		}
	};

	
	/**
	 * Bind this {@code Variable}'s value to {@code newValue}.
	 * 
	 * <p>Calling {@code set()} multiple times is allowed.  {@link #getValue()}
	 * will return the value set most recently.</p>
	 * 
	 * @throws IllegalValueTypeException
	 * 		when ∄ C∊{@link #getAcceptedTypes()} : {@code newValue instanceof C}
	 */
	public void set(QueryExpression newValue)
			throws IllegalValueTypeException;

	
	/**
	 * @return	the value most recently {@link 
	 * 		#set(nl.maastro.eureca.lucenerdf.concepts.lucenequery.QueryExpression)}
	 * 		the returned value will be an instance of one of the types returned
	 * 		by {@link #getAcceptedTypes()}
	 * 
	 * @throws UnboundVariableException
	 * 		if and only if {@link #state()} == {@link State#UNBOUND}
	 */
	public QueryExpression getValue()
			throws UnboundVariableException;
	
	
	/**
	 * @return	whether this {@code Variable} is bound to a value. 
	 */
	public State state();


	/**
	 * @return	the types of {@link QueryExpression} that {@link #set} accepts
	 * 		and as a consequence {@link #getValue()} may return.
	 */
	public Set<Class<? extends QueryExpression>> getAcceptedTypes();
	

	/**
	 * Visitor pattern: call the {@code visitor}'s {@link 
	 * QueryVisitor#visitVariable}-method.
	 *
	 * @param <T>	type of result that {@code visitor} returns, is opaque
	 *		to {@code Query}.
	 * @return		the result of the {@code visitVariable()}-method.
	 */
	@Override
	public <T> T accept(QueryVisitor<T> visitor);


	/**
	 * Access the bound value (same as {@link #getValue()}.
	 * 
	 * @see QueryExpression#subexpressions() 
	 *
	 * @return either
	 * 		<ul><li>a singleton containing {@link #getValue()}; or</li>
	 * 		<li>an empty iterable.</li></ul>
	 */
	@Override
	public Iterable<? extends QueryExpression> subexpressions();


	/**
	 * Sub{@link Variable}s that {@link #getValue()} has.
	 *
	 * <p>the returned {@code Map} will <em>not</em> contain
	 * {@code this}.</p>
	 * 
	 * @see QueryExpression#variables()
	 */
	@Override
	public Map<Identifier, ? extends Variable> variables();


	/**
	 * @return always empty.
	 * 
	 * @see QueryExpression#directVariables() 
	 */
	@Override
	public Map<Identifier, ? extends Variable> directVariables();


}

/* vim:set tabstop=4 shiftwidth=4 autoindent spell spelllang=en_gb : */


