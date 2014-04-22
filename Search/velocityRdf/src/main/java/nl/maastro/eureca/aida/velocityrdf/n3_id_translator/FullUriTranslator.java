// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import org.openrdf.model.URI;

/**
 * Translate {@link URI}s into an uri enclosed in '&lt;' and '&gt;'.
 * For example. {@code http://example.org/ns#bar} becomes
 * {@code "<http://example.org/ns#bar>"}.  Compare with {@link
 * QNameTranslator} which abreviates namespaces to prefixes.
 *
 * @see <a href="http://www.w3.org/TeamSubmission/turtle/#terms">Turle §RDF
 * Term</a>
 * @see QNameTranslator
 * @see UriTranslator
 * 
 *
 * @author Kasper van den Berg &lt;kasper.vandenberg@maastro.nl&gt; &lt;kasper@kaspervandenberg.net&gt;
 */
class FullUriTranslator implements Translator<URI> {

	@Override
	public boolean isWellFormed(final String id) {
		return N3SyntaxPatterns.URI_REF.matches(id);
	}

	@Override
	public String getId(final URI uri) {
		return String.format("<%s%s>", uri.getNamespace(), uri.getLocalName());
	}

	@Override
	public boolean matches(URI val, String targetId) {
		String valueId = getId(val);
		return valueId.equals(targetId);
	}
	
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

