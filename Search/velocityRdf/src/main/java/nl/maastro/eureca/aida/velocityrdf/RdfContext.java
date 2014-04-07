// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.velocityrdf;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import nl.maastro.eureca.aida.velocityrdf.basicview.Edge;
import nl.maastro.eureca.aida.velocityrdf.basicview.Node;
import nl.maastro.eureca.aida.velocityrdf.n3_id_translator.N3IdTranslator;
import org.apache.velocity.context.AbstractContext;
import org.openrdf.model.Model;

import static org.apache.commons.collections4.CollectionUtils.collect;
import static org.apache.commons.collections4.IteratorUtils.filteredIterator;
import static org.apache.commons.collections4.PredicateUtils.instanceofPredicate;
import org.openrdf.model.URI;



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
	private final N3IdTranslator identifierGenerator;
	
	public RdfContext(Model rdfModel_)
	{
		this.rdfModel = rdfModel_;
		this.identifierGenerator = new N3IdTranslator(rdfModel_);
	}

	

	@Override
	public Object internalGet(String key)
	{
		if ( identifierGenerator.containsUri(key))
		{
			return new Node(rdfModel, identifierGenerator.getUri(key), identifierGenerator);
		}
		else if (identifierGenerator.containsStatement(key))
		{
			return new Edge(rdfModel, identifierGenerator.getStatement(key), identifierGenerator);
		}
		else
		{
			throw new NoSuchElementException(String.format(
					"Model contains no resource or statement identified by %s",
					key));
		}
	}

	
	@Override
	public Object internalPut(String key, Object value)
	{
		throw new UnsupportedOperationException("Not supported; modify model via RDF.");
	}

	
	@Override
	public boolean internalContainsKey(Object obj_key)
	{
		if (obj_key instanceof String) {
			String key = (String)obj_key;	
			return identifierGenerator.containsUri(key)
					|| identifierGenerator.containsStatement(key);
		}
		else
		{
			return false;
		}
	}

	
	@Override
	public Object[] internalGetKeys()
	{
		ArrayList<String> result = new ArrayList<>( collect(
				rdfModel.subjects(),
				identifierGenerator.valueIdGenerator()));
		result.addAll( collect(
				filteredIterator(
						rdfModel.objects().iterator(),
						instanceofPredicate(URI.class)),
				identifierGenerator.valueIdGenerator()));
		
		return result.toArray();
	}

	@Override
	public Object internalRemove(Object key) {
		throw new UnsupportedOperationException("Not supported; modify model via RDF.");
	}
}
