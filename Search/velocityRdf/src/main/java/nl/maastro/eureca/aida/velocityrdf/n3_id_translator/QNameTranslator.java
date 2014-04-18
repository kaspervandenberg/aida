// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

/**
 *
 * @author kasper
 */
class QNameTranslator implements Translator<URI> {
	private final N3IdTranslator context;

	QNameTranslator(final N3IdTranslator context) {
		this.context = context;
	}

	@Override
	public boolean isWellFormed(String id) {
		return N3SyntaxPatterns.NODE_ID.matches(id);
	}

	@Override
	public String getId(final URI uri) {
		String namespace = uri.getNamespace();
		if (context.containsPrefixForUri(namespace)) {
			Namespace ns = context.getNamespaceByUri(namespace);
			return getQnameId(ns, uri);
		} else {
			throw new Error(String.format("Attempt to create a QName for namespace %s without a defined prefix (URI: %s); " + "fix code to use FullUriTranslator instead.", namespace, uri));
		}
	}

	public String getQnameId(final Namespace ns, final URI uri) {
		return String.format("%s:%s", ns.getPrefix(), uri.getLocalName());
	}

	@Override
	public boolean matches(URI val, String id) {
		throw new UnsupportedOperationException("Not supported; use FullUriTranslator.matches() instead..");
	}
	
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

