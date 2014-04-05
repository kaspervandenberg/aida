// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.velocityrdf.basicview;

import nl.maastro.eureca.aida.velocityrdf.n3_id_translator.N3IdTranslator;
import org.apache.commons.collections4.Transformer;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class Edge {
	private final Model rdfModel;
	private final Statement viewed;
	private final N3IdTranslator identifierGenerator;

	public Edge(
			final Model rdfModel_,
			final Statement viewed_,
			final N3IdTranslator identifierGenerator_)
	{
		this.rdfModel = rdfModel_;
		this.viewed = viewed_;
		this.identifierGenerator = identifierGenerator_;
	}


	static Transformer<Statement, Edge> edgeCreator(
			final Model rdfModel_,
			final N3IdTranslator identifierGenerator_)
	{
		return new Transformer<Statement, Edge>()
		{
			@Override
			public Edge transform(Statement input)
			{
				return new Edge(rdfModel_, input, identifierGenerator_);
			}
		};
	}


	public Node getSubject()
	{
		return new Node(rdfModel, viewed.getSubject(), identifierGenerator);
	}


	public String getPredicate()
	{
		return identifierGenerator.getId(viewed.getPredicate());
	}


	public Node getObjectNode()
	{
		Value object = viewed.getObject();
		if (object instanceof Resource)
		{
			return new Node(rdfModel, (Resource)object, identifierGenerator);
		}
		else
		{
			throw new IllegalStateException(String.format(
					"Trying to access %s as a node, while it is a value (class %s) "
					+ "(accessed via statement %s)",
					identifierGenerator.getId(object),
					object.getClass(),
					identifierGenerator.getId(viewed)));
		}
	}


	public String getObjectValue()
	{
		Value object = viewed.getObject();
		if (object instanceof Literal)
		{
			return identifierGenerator.getId(object);
		}
		else
		{
			throw new IllegalStateException(String.format(
					"Trying to access %s as a value, while it is a node (class %s) "
					+ "(accessed via statement %s)",
					identifierGenerator.getId(object),
					object.getClass(),
					identifierGenerator.getId(viewed)));
		}
	}

	
}
