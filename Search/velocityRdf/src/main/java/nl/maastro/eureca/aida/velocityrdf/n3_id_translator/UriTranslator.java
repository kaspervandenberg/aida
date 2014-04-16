/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import org.openrdf.model.URI;

/**
 *
 * @author kasper
 */
class UriTranslator implements Translator<URI> {
	private final Translator<URI> qnameTrans;
	private final Translator<URI> fullUriTrans;
	private final N3IdTranslator context;

	public UriTranslator(final Translator<URI> qnameTrans_, final Translator<URI> fullUriTrans_, final N3IdTranslator context) {
		this.context = context;
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
		if (context.containsPrefixForUri(namespace)) {
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

