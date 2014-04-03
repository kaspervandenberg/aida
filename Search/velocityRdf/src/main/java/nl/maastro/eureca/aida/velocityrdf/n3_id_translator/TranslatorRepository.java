// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.MonotonicNonNull;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Contain a fixed set of {@link Translator}s for all parts of 
 * {@link org.openrdf.model.Model}.
 * 
 * Create the {@code Translators} and resolve the inter translator dependencies.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
class TranslatorRepository {
	private final N3IdTranslator context;

	@MonotonicNonNull private LiteralTranslator		litTrans = null;
	@MonotonicNonNull private QNameTranslator		qnameTrans = null;
	@MonotonicNonNull private FullUriTranslator		fullUriTrans = null;
	@MonotonicNonNull private UriTranslator			uriTrans = null;
	@MonotonicNonNull private BNodeTranslator		bnodeTrans = null;
	@MonotonicNonNull private ResourceTranslator	resourceTrans = null;
	@MonotonicNonNull private ValueTranslator		valueTrans = null;
	@MonotonicNonNull private StatementTranslator	statementTrans = null;

	
	public TranslatorRepository(final N3IdTranslator context_)
	{
		this.context = context_;
	}


	public Translator<?> getTranslator(Class<?> valueType)
	{
		if (Statement.class.isAssignableFrom(valueType)) {
			return getStatementTranslator();
		}
		if (URI.class.isAssignableFrom(valueType)) {
			return getUriTranslator();
		}
		if (BNode.class.isAssignableFrom(valueType)) {
			return getBNodeTranslator();
		}
		if (Literal.class.isAssignableFrom(valueType)) {
			return getLiteralTranslator();
		}
		if (Resource.class.isAssignableFrom(valueType)) {
			return getResourceTranslator();
		}
		if (Value.class.isAssignableFrom(valueType)) {
			return getValueTranslator();
		}
		throw new IllegalArgumentException(String.format(
				"No translator found for values of type %s",
				valueType));
	}


	@EnsuresNonNull("litTrans")
	public LiteralTranslator getLiteralTranslator()
	{
		if (litTrans == null) {
			litTrans = new LiteralTranslator();
		}

		return litTrans;
	}


	@EnsuresNonNull("qnameTrans")
	public QNameTranslator getQNameTranslator()
	{
		if (qnameTrans == null) {
			qnameTrans = new QNameTranslator(context);
		}

		return qnameTrans;
	}
	

	@EnsuresNonNull("fullUriTrans")
	public FullUriTranslator getFullUriTranslator()
	{
		if (fullUriTrans == null) {
			fullUriTrans = new FullUriTranslator();
		}

		return fullUriTrans;
	}


	@EnsuresNonNull("uriTrans")
	public UriTranslator getUriTranslator()
	{
		if (uriTrans == null) {
			uriTrans = new UriTranslator(
					getQNameTranslator(),
					getFullUriTranslator(),
					context);
		}

		return uriTrans;
	}


	@EnsuresNonNull("bnodeTrans")
	public BNodeTranslator getBNodeTranslator()
	{
		if (bnodeTrans == null) {
			bnodeTrans = new BNodeTranslator();
		}

		return bnodeTrans;
	}


	@EnsuresNonNull("resourceTrans")
	public ResourceTranslator getResourceTranslator()
	{
		if (resourceTrans == null) {
			resourceTrans = new ResourceTranslator(
					getUriTranslator(),
					getBNodeTranslator());
		}

		return resourceTrans;
	}


	@EnsuresNonNull("valueTrans")
	public ValueTranslator getValueTranslator()
	{
		if (valueTrans == null) {
			valueTrans = new ValueTranslator(
					getResourceTranslator(), 
					getLiteralTranslator());
		}

		return valueTrans;
	}


	@EnsuresNonNull("statementTrans")
	public StatementTranslator getStatementTranslator()
	{
		if (statementTrans == null) {
			statementTrans = new StatementTranslator(
					getResourceTranslator(),
					getUriTranslator(),
					getValueTranslator());
		}

		return statementTrans;
	}
}

/* vim:set tabstop=4 shiftwidth=4: */
