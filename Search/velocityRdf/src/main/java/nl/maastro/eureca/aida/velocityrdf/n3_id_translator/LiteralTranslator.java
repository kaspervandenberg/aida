/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import org.openrdf.model.Literal;

/**
 *
 * @author kasper
 */
class LiteralTranslator implements Translator<Literal> {

	@Override
	public boolean isWellFormed(final String id) {
		return N3SyntaxPatterns.STRING.matches(id);
	}

	@Override
	public String getId(final Literal lit) {
		return lit.toString();
	}

	@Override
	public boolean matches(Literal val, String id) {
		return id.equals(val.toString());
	}
	
}
