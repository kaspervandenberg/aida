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

