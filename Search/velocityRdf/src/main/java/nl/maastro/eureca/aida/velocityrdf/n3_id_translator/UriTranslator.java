// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import org.openrdf.model.URI;

/**
 *
 * @author kasper
 */
class UriTranslator implements Translator<URI> {
	private final NamespaceContainer namespaces;
	private final Translator<URI> qnameTrans;
	private final Translator<URI> fullUriTrans;

	public UriTranslator(
			final NamespaceContainer namespaces_,
			final Translator<URI> qnameTrans_,
			final Translator<URI> fullUriTrans_)
	{
		this.namespaces = namespaces_;
		this.qnameTrans = qnameTrans_;
		this.fullUriTrans = fullUriTrans_;
	}

	@Override
	public boolean isWellFormed(final String id) {
		return qnameTrans.isWellFormed(id) || fullUriTrans.isWellFormed(id);
	}

	@Override
	public String getId(final URI uri) {
		String namespace = uri.getNamespace();
		if (namespaces.containsPrefixForUri(namespace)) {
			return qnameTrans.getId(uri);
		} else {
			return fullUriTrans.getId(uri);
		}
	}

	@Override
	public boolean matches(final URI val, final String id) {
		return fullUriTrans.matches(val, id);
	}
	
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

