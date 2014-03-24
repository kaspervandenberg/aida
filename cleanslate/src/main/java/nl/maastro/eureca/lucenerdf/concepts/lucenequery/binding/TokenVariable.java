// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucenerdf.concepts.lucenequery.binding;

import nl.maastro.eureca.lucenerdf.concepts.auxiliary.Identifier;
import nl.maastro.eureca.lucenerdf.concepts.lucenequery.QueryExpression;
import nl.maastro.eureca.lucenerdf.concepts.lucenequery.TokenExpression;
import java.util.Set;
import java.util.Map;

/**
 * Describes a {@link Variable} restricted to contain
 * {@link nl.maastro.eureca.lucenerdf.concepts.lucenequery.TokenExpression}.
 *
 * @author Kasper van den Berg <kasper@kaspervandenberg.net> <kasper.vandenberg@maastro.nl>
 */
public interface TokenVariable extends Variable, TokenExpression {
	/**
	 * @inheritDoc
	 *
	 * @param newValue	the {@link TokenExpression} to which to bind this 
	 *		{@code TokenVariable}.
	 *		<br /><em>NOTE: {@code TokenVariable} restricts accepted values to 
	 *		{@code TokenExpression}s, other types of {@link QueryExpression}
	 *		will cause an 
	 *		{@link nl.maastro.eureca.lucenerdf.concepts.lucenequery.binding.Variable.IllegalValueTypeException}.
	 */
	@Override
	public void set(QueryExpression newValue)
			throws IllegalValueTypeException;


	/**
	 * Bind this {@code Variable}'s value to {@code newValue}.
	 * 
	 * @see #set(nl.maastro.eureca.lucenerdf.concepts.lucenequery.QueryExpression)
	 */
	public void set(TokenExpression newValue)
			throws IllegalValueTypeException;


	/**
	 * @inheritDoc
	 */
	@Override
	public TokenExpression getValue()
			throws UnboundVariableException;


	/**
	 * Return the set of types to which this {@code TokenVariable} can be bound.
	 *
	 * Restricts {@link Variable#getAcceptedTypes()} to subtypes of 
	 * {@link TokenExpression}.
	 *
	 * @return	the types of {@link TokenExpression} that {@link #set} accepts
	 * 		and as a consequence {@link #getValue()} may return.
	 */
	@Override
	public Set<? extends Class<? extends TokenExpression>> getAcceptedTypes();


	/**
	 * @inheritDoc
	 */
	@Override
	public Iterable<? extends TokenExpression> subexpressions();
	

	/**
	 * @inheritDoc
	 */
	@Override
	public Map<Identifier, ? extends TokenVariable> variables();


	/**
	 * @inheritDoc
	 *
	 * @return always empty.
	 */
	@Override
	public Map<Identifier, ? extends TokenVariable> directVariables();
}


/* vim:set tabstop=4 shiftwidth=4 autoindent spell spelllang=en_gb : */

