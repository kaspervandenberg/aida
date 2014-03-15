// © Kasper van den Berg, 2014
/**
 * Concepts to model queries, to query Lucene with, as trees of {@link
 * nl.maastro.eureca.lucenerdf.concepts.lucenequery.QueryExpression}-objects.
 * The concepts follow the hierarchy and
 * provide the composition options as shown in the diagram below.
 * <img src="doc-files/hierarchy_and_composition.svg"
 *	alt="UML class diagram of QueryExpression hierarchy and composition."
 *	width="80%" />
 *
 * <p>The concepts follow two patterns:
 * <ul>	<li>composite (see [GoF p.163]); and</li>
 * 	<li>visitor (see [GoF p.331]).</li></ul>
 * {@code QueryExpression} defines {@link
 * nl.maastro.eureca.lucenerdf.concepts.lucenequery.QueryExpression#subexpressions()}
 * as the interface to access components; thereby defining that each
 * {@code QueryExpression} can act as a composite.  The derived interfaces 
 * refine this method.  One class is atomic (i.e. an exception and not
 * composite): {@link
 * nl.maastro.eureca.lucenerdf.concepts.lucenequery.Literal}.  Even this
 * interface implements {@code subexpressions()}, but will always return an
 * empty collection.<br />
 * {@link
 * nl.maastro.eureca.lucenerdf.concepts.lucenequery.QueryExpression#accept(
 * nl.maastro.eureca.lucenerdf.concepts.lucenequery.QueryVisitor)} and 
 * {@link nl.maastro.eureca.lucenerdf.concepts.lucenequery.QueryVisitor}
 * implement the visitor pattern.</p>
 *
 * <p>For completeness the diagram shows the classes
 * {@link nl.maastro.eureca.lucenerdf.concepts.lucenequery.binding.Variable},
 * {@code nl.maastro.eureca.lucenerdf.concepts.lucenequery.binding.QueryVariable},
 * and {@code
 * nl.maastro.eureca.lucenerdf.concepts.lucenequery.binding.TokenVariable}.
 * You can find these classes in the package
 * {@link nl.maastro.eureca.lucenerdf.concepts.lucenequery.binding}.</p>
 */
package nl.maastro.eureca.lucenerdf.concepts.lucenequery;

/* vim:set tabstop=4 shiftwidth=4 autoindent spell spelllang=en_gb : */

