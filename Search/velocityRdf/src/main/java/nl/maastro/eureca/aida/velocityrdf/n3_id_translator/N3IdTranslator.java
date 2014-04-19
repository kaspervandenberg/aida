// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.MonotonicNonNull;
import checkers.nullness.quals.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
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
	private final NamespaceContainer namespaces;
	private @MonotonicNonNull TranslatorRepository translators;
	

	private final transient Map<String, Statement> statementIds = new HashMap<>();
	private final transient Map<String, Value> valueIds = new HashMap<>();

	public N3IdTranslator(final Model rdfModel_) {
		this.rdfModel = rdfModel_;
		this.namespaces = new NamespaceContainer(
				this.rdfModel.getNamespaces());
	}


	public org.apache.commons.collections4.Transformer<Value, String> valueIdGenerator()
	{
		return new org.apache.commons.collections4.Transformer<Value, String>() {
			@Override
			public String transform(Value input) {
				return getId(input);
			} };
	}


	public org.apache.commons.collections4.Transformer<Statement, String> statementIdGenerator()
	{
		return new org.apache.commons.collections4.Transformer<Statement, String>() {
			@Override
			public String transform(Statement input) {
				return getId(input);
			} };
	}


	public String getId(Statement statement)
	{
		String id = getTranslators().getStatementTranslator().getId(statement);
		statementIds.put(id, statement);
		return id;
	}


	public String getId(Value value)
	{
		String id = getTranslators().getValueTranslator().getId(value);
		valueIds.put(id, value);
		return id;
	}

	
	public boolean containsStatement(String id)
	{
		return statementIds.containsKey(id)
				|| (tryFindSubject(id) !=  null);
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
			return result;
		}
	}


	public boolean containsValue(String id)
	{
		return containsValue(Value.class, id);
	}

	
	public Value getValue(String id)
	{
		if (valueIds.containsKey(id))
		{
			return valueIds.get(id);
		}
		else
		{
			return findValue(id);
		}
	}


	public boolean containsUri(String targetId)
	{	
		return containsValue(URI.class, targetId);
	}
	
	
	public URI getUri(String id)
	{
		if (!getTranslators().getUriTranslator().isWellFormed(id))
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


	@EnsuresNonNull("translators")
	private TranslatorRepository getTranslators()
	{
		if (translators == null) 
		{
			throw new UnsupportedOperationException("Not yet implemented");
//			translators = new TranslatorRepository(this);
		}
		return translators;
	}

	private Statement findStatement(String targetId) throws NoSuchElementException
	{
		Statement result = tryFindStatement(targetId);
		if(result != null)
		{
			return result;
		}
		else
		{
			throw new NoSuchElementException(String.format(
					"Model does not contain a statement matching identifier %s",
					targetId));
		}
	}


	private Value findValue(String targetId) throws NoSuchElementException
	{
		Value result = tryFindValue(targetId);
		if (result != null)
		{
			return result;
		}
		else
		{
			throw new NoSuchElementException(String.format(
					"Model does not contain a subject, object, or predicate "
					+ "matching %s",
					targetId));
		}
	}


	private boolean containsValue(
			Class<? extends Value> requestedType,
			String targetId)
	{
		Value result;
		if (valueIds.containsKey(targetId))
		{
			result = valueIds.get(targetId);
		}
		else
		{
			result = tryFindValue(targetId);
		}
		
		return requestedType.isInstance(result);
	}
	

	private @Nullable Statement tryFindStatement(String targetId)
	{
		for (Statement s : rdfModel)
		{
			if (getTranslators().getStatementTranslator().matches(s, targetId))
			{
				statementIds.put(targetId, s);
				return s;
			}
		}
		return null;
	}


	private @Nullable Value tryFindValue(String targetId)
	{
		Resource subjResult = tryFindSubject(targetId);
		if (subjResult != null) {
			return subjResult;
		}

		Value objResult = tryFindObject(targetId);
		if (objResult != null) {
			return objResult;
		}

		URI predResult = tryFindPredicate(targetId);
		if (predResult != null) {
			return predResult;
		}
		
		return null;
	}


	private @Nullable Resource tryFindSubject(String targetId)
	{
		for (Resource subj : rdfModel.subjects())
		{
			if (getTranslators().getResourceTranslator().matches(subj, targetId))
			{
				valueIds.put(targetId, subj);
				return subj;
			}
		}
		return null;
	}


	private @Nullable Value tryFindObject(String targetId)
	{
		for (Value obj : rdfModel.objects())
		{
			if (getTranslators().getValueTranslator().matches(obj, targetId))
			{
				valueIds.put(targetId, obj);
				return obj;
			}
		}
		return null;
	}


	private @Nullable URI tryFindPredicate(String targetId)
	{
		for (URI pred : rdfModel.predicates())
		{
			if (getTranslators().getUriTranslator().matches(pred, targetId))
			{
				valueIds.put(targetId, pred);
				return pred;
			}
		}
		return null;
	}
	
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

