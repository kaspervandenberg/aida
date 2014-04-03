// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import net.kaspervandenberg.apps.common.util.cache.Cache;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * 
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class N3IdTranslator {
	
	
	private final Model rdfModel;
	private final TranslatorRepository translators;
	
	private final transient Cache<Map<String, Namespace>> namespacePrefixes = new Cache<Map<String, Namespace>>() {
		@Override
		protected Map<String, Namespace> calc() {
			return createPrefixLookup();
		}
	};

	private final transient Map<String, Statement> statementIds = new HashMap<>();
	private final transient Map<String, Value> valueIds = new HashMap<>();

	public N3IdTranslator(final Model rdfModel_) {
		this.rdfModel = rdfModel_;
		this.translators = new TranslatorRepository(this);
	}


	public String getId(Statement statement)
	{
		String id = translators.getStatementTranslator().getId(statement);
		statementIds.put(id, statement);
		return id;
	}


	public String getId(Value value)
	{
		String id = translators.getValueTranslator().getId(value);
		valueIds.put(id, value);
		return id;
	}


	public Statement getStatement(String id)
	{
		if (statementIds.containsKey(id))
		{
			return statementIds.get(id);
		}
		else
		{
			Statement result = findStatement(id);
			statementIds.put(id, result);
			return result;
		}
	}


	public Value getValue(String id)
	{
		if (valueIds.containsKey(id))
		{
			return valueIds.get(id);
		}
		else
		{
			Value result = findValue(id);
			valueIds.put(id, result);
			return result;
		}
	}
	
	
	public URI getUri(String id)
	{
		if (!translators.getUriTranslator().isWellFormed(id))
		{
			throw new IllegalArgumentException(String.format(
					"%s is not an identifier for an URI",
					id));
		}
		
		Value value = getValue(id);
		if (value instanceof URI)
		{
			return (URI)value;	
		}
		else
		{
			throw new IllegalStateException(String.format(
					"Element identified by %s is a %s not a URI.",
					id, value.getClass()));
		}
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


	private Statement findStatement(String targetId) throws NoSuchElementException
	{
		for (Statement s : rdfModel)
		{
			if (translators.getStatementTranslator().matches(s, targetId))
			{
				return s;
			}
		}
		throw new NoSuchElementException(String.format(
				"Model does not contain a statement matching identifier %s",
				targetId));
	}


	private Value findValue(String targetId) throws NoSuchElementException
	{
		for (Resource subj : rdfModel.subjects())
		{
			if (translators.getResourceTranslator().matches(subj, targetId))
			{
				return subj;
			}
		}
		
		for (Value obj : rdfModel.objects())
		{
			if (translators.getValueTranslator().matches(obj, targetId))
			{
				return obj;
			}
		}

		for (URI pred : rdfModel.predicates())
		{
			if (translators.getUriTranslator().matches(pred, targetId))
			{
				return pred;
			}
		}

		throw new NoSuchElementException(String.format(
				"Model does not contain a subject, object, or predicate "
				+ "matching %s",
				targetId));
	}
}
