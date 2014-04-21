// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.basicview;

import nl.maastro.eureca.aida.velocityrdf.n3_id_translator.N3IdTranslator;
import org.apache.commons.collections4.Transformer;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

/**
 *
 * @author Kasper van den Berg &lt;kasper.vandenberg@maastro.nl&gt; &lt;kasper@kaspervandenberg.net&gt;
 */
public class ModelTransformerUtils {
	public static Transformer<Literal, String> literalCreator(
			final Model rdfModel_,
			final N3IdTranslator identifierGenerator_)
	{
		return new Transformer<Literal, String>()
		{
			@Override
			public String transform(Literal input)
			{
				return identifierGenerator_.getId(input);
			}
		};
	}


	public static Transformer<Statement, URI> predicateRetriever(
			final Model rdfModel_,
			final N3IdTranslator identifierGenerator_)
	{
		return new Transformer<Statement, URI>()
		{
			@Override
			public URI transform(Statement input)
			{
				return input.getPredicate();
			}
		};
	}
	
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

