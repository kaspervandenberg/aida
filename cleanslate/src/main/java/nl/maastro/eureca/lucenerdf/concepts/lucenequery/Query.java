// © Kasper van den Berg, 2014
package nl.maastro.eureca.lucenerdf.concepts.lucenequery;

/**
 * A {@link QueryExpression} that can be submitted to Lucene.
 *
 * <p>{@code Query} complements {@link TokenExpression}: whereas
 * {@code TokenExpressions} must be contained in other {@code QueryExpressions},
 * while {@code Queries} model concepts that can be the root of a
 * {@code QueryExpression} tree.</p>
 */
public interface Query extends QueryExpression {
}

/* vim:set tabstop=4 shiftwidth=4 autoindent spell spelllang=en_gb : */


