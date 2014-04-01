// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.velocityrdf;

import org.apache.velocity.context.AbstractContext;
import org.openrdf.model.Model;

/**
 * A {@link org.apache.velocity.context.Context} that uses a
 * {@link org.openrdf.model.Model} as the source of its data.
 * 
 * The context is read-only.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class RdfContext extends AbstractContext {
	private final Model rdfModel;
	
	public RdfContext(Model rdfModel_) {
		this.rdfModel = rdfModel_;
	}

	

	@Override
	public Object internalGet(String key) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Object internalPut(String key, Object value) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean internalContainsKey(Object key) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Object[] internalGetKeys() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Object internalRemove(Object key) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
