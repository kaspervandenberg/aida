// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.basicview;

/*>>>import checkers.nullness.quals.NonNull;*/
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import nl.maastro.eureca.aida.velocityrdf.n3_id_translator.N3IdTranslator;
import org.apache.commons.collections4.Transformer;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import static org.apache.commons.collections4.CollectionUtils.collect;
import static nl.maastro.eureca.aida.velocityrdf.basicview.ModelTransformerUtils.predicateRetriever;
import org.openrdf.model.Statement;

/**
 * A RDF {@link org.openrdf.model.Resource} containing accessors to navigate
 * the RDF graph.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class Node {
	private final Model rdfModel;
	private final Resource viewedItem;
	private final N3IdTranslator identifierGenerator;

	public Node(
			final Model rdfModel_, 
			final Resource viewedItem_, 
			final N3IdTranslator identifierGenerator_)
	{
		this.rdfModel = rdfModel_;
		this.viewedItem = viewedItem_;
		this.identifierGenerator = identifierGenerator_;
	}

	
	static Transformer<Resource, Node> nodeCreator(
			final Model rdfModel_,
			final N3IdTranslator identifierGenerator_)
	{
		return new Transformer<Resource, Node>()
		{
			@Override
			public Node transform(Resource input)
			{
				return new Node(rdfModel_, input, identifierGenerator_);
			}
		};
	}


	public String getId()
	{
		return identifierGenerator.getId(viewedItem);
	}


	public ArrayList<Edge> getOutEdges()
	{
		ArrayList<Edge> result = getEdges(getSubjectModel());

		return result;
	}
	
	
	public ArrayList<MultiEdge1toN> getOutPreds()
	{
		Transformer<URI, MultiEdge1toN> transformer = 
				MultiEdge1toN.multiEdgeCreator(
					rdfModel, identifierGenerator, viewedItem);
		ArrayList<MultiEdge1toN> result = getPredicates(
				getSubjectModel(), transformer);
		
		return result;
	}


	public ArrayList<Edge> getInEdges()
	{
		ArrayList<Edge> result = getEdges(getObjectModel());

		return result;
	}


	public ArrayList<MultiEdgeNto1> getInPreds()
	{
		Transformer<URI, MultiEdgeNto1> transformer =
				MultiEdgeNto1.multiEdgeCreator(
					rdfModel, identifierGenerator, viewedItem);
		ArrayList<MultiEdgeNto1> result = getPredicates(
				getObjectModel(),
				transformer);

		return result;
	}


	private Model getSubjectModel()
	{
		return rdfModel.filter(viewedItem, null, null);
	}

	
	private Model getObjectModel()
	{
		return rdfModel.filter(null, null, viewedItem);
	}
	

	private <TMultiEdge/*>>>extends @NonNull Object*/> ArrayList<TMultiEdge> getPredicates(
			final Model targetPredicates,
			final Transformer<URI, TMultiEdge> multiEdgeCreator)
	{
		Set<URI> predicates = new HashSet<>( collect (
				targetPredicates,
				predicateRetriever(rdfModel, identifierGenerator)));
		ArrayList<TMultiEdge> result = new ArrayList<>( collect (
				predicates,
				multiEdgeCreator));

		return result;
	}


	private ArrayList<Edge> getEdges(
			final Model targetStatements)
	{
		Transformer<Statement, Edge> transformer =
				Edge.edgeCreator(rdfModel, identifierGenerator);
		ArrayList<Edge> result = new ArrayList<>( collect(
				targetStatements,
				transformer));

		return result;
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

