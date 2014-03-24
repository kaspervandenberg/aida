// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucenerdf.concepts.lucenequery;

import nl.maastro.eureca.lucenerdf.concepts.auxiliary.Identifier;
import nl.maastro.eureca.lucenerdf.concepts.lucenequery.binding.TokenVariable;
import java.util.Map;

/**
 * Query a single token in a field in a lucene document.
 *
 * <p>Lucene normally views documents as a collection of fields that each
 * contain a sequence of tokens.  Tokens roughly correspond to words of 
 * the documents.  {@code TermQuery} represents searching
 * for a single token in a single field.</p>
 *
 * <p>Since {@code TermQuery} represents searching a <em>single</em> token
 * in a <em>single</em> field {@link #getToken()} and {@link #getField()}
 * should return only {@link TokenExpression}s.
 * Use 
 * </p>
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface TermQuery extends Query {
	/**
	 * @return	the token to search for
	 */
	public TokenExpression getToken();


	/**
	 * @return	the field in which the {@code token} is searched
	 */
	public TokenExpression getField();


	/**
	 * Visitor pattern: call the {@code Visitor}'s 
	 * {@link QueryVisitor#visitTerm}-method.
	 *
	 * @inheritDoc
	 */
	@Override
	public <T> T accept(QueryVisitor<T> visitor);


	/**
	 * Access the {@link #getField() field} and {@link #getToken() token} that
	 * this {@code TermQuery} contains.
	 *
	 * @return	an {@link Iterable} that contains two {@link TokenExpression}s:
	 *		first, this {@code TermQuery}'s {@code field}; then, its
	 *		{@code token}.
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
	 */
	public Map<Identifier, ? extends TokenVariable> directVariables();
}

/* vim:set tabstop=4 shiftwidth=4 autoindent spell spelllang=en_gb : */

