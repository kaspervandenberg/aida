/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

/**
 *
 * @author kasper
 */
class ResourceTranslator implements Translator<Resource> {
	private final Translator<URI> uriTrans;
	private final Translator<BNode> bnodeTrans;

	public ResourceTranslator(final Translator<URI> uriTrans_, final Translator<BNode> bnodeTrans_) {
		this.uriTrans = uriTrans_;
		this.bnodeTrans = bnodeTrans_;
	}

	@Override
	public boolean isWellFormed(final String id) {
		return uriTrans.isWellFormed(id) || bnodeTrans.isWellFormed(id);
	}

	@Override
	public String getId(final Resource res) {
		if (res instanceof URI) {
			return uriTrans.getId((URI) res);
		} else if (res instanceof BNode) {
			return bnodeTrans.getId((BNode) res);
		} else {
			throw new Error(new IllegalArgumentException(String.format("Un expected type, %s, of resource %s", res.getClass(), res)));
		}
	}

	@Override
	public boolean matches(Resource res, String id) {
		if (res instanceof URI) {
			return uriTrans.matches((URI) res, id);
		} else if (res instanceof BNode) {
			return bnodeTrans.matches((BNode) res, id);
		} else {
			return false;
		}
	}
	
}
