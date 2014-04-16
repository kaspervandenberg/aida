/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import org.openrdf.model.BNode;

/**
 *
 * @author kasper
 */
class BNodeTranslator implements Translator<BNode> {

	@Override
	public boolean isWellFormed(final String id) {
		return N3SyntaxPatterns.NODE_ID.matches(id);
	}

	@Override
	public String getId(final BNode bnode) {
		return String.format("_:%s", bnode.getID());
	}

	@Override
	public boolean matches(BNode val, String targetId) {
		String valId = getId(val);
		return valId.equals(targetId);
	}
	
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

