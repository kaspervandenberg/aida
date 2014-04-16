/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;

/**
 *
 * @author kasper
 */
class ValueTranslator implements Translator<Value> {
	private final Translator<Resource> resTrans;
	private final Translator<Literal> litTrans;

	public ValueTranslator(final Translator<Resource> resTrans_, final Translator<Literal> litTrans_) {
		this.resTrans = resTrans_;
		this.litTrans = litTrans_;
	}

	@Override
	public boolean isWellFormed(final String id) {
		return resTrans.isWellFormed(id) || litTrans.isWellFormed(id);
	}

	@Override
	public String getId(final Value val) {
		if (val instanceof Resource) {
			return resTrans.getId((Resource) val);
		} else if (val instanceof Literal) {
			return litTrans.getId((Literal) val);
		} else {
			throw new Error(new IllegalArgumentException(String.format("Un expected type, %s, of value %s", val.getClass(), val)));
		}
	}

	@Override
	public boolean matches(final Value val, final String id) {
		if (val instanceof Resource) {
			return resTrans.matches((Resource) val, id);
		} else if (val instanceof Literal) {
			return litTrans.matches((Literal) val, id);
		} else {
			return false;
		}
	}
	
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

