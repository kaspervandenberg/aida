// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import java.util.HashMap;
import java.util.Map;
import net.kaspervandenberg.apps.common.util.cache.Cache;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;

/**
 * 
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class N3IdTranslator {
	
	
	private final Model rdfModel;
	private final transient Cache<Map<String, Namespace>> namespacePrefixes = new Cache<Map<String, Namespace>>() {
		@Override
		protected Map<String, Namespace> calc() {
			return createPrefixLookup();
		}
	};

	public N3IdTranslator(final Model rdfModel_) {
		this.rdfModel = rdfModel_;
	}
	

	public boolean containsPrefixForUri(String uri)
	{
		return namespacePrefixes.get().containsKey(uri);
	}

	public Namespace getNamespaceByUri(String uri) {
		return namespacePrefixes.get().get(uri);
	}
	
	private Map<String, Namespace> createPrefixLookup()
	{
		Map<String, Namespace> result = new HashMap<>();
		for (Namespace namespace : rdfModel.getNamespaces())
		{
			result.put(namespace.getName(), namespace);
		}
		return result;
	}
}
