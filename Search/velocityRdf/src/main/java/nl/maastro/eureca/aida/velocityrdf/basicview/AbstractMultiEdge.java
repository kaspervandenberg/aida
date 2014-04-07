// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.velocityrdf.basicview;

/*>>>import checkers.nullness.quals.NonNull;*/
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import nl.maastro.eureca.aida.velocityrdf.n3_id_translator.N3IdTranslator;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.apache.commons.collections4.Transformer;

import static org.apache.commons.collections4.CollectionUtils.collect;
import static org.apache.commons.collections4.IteratorUtils.filteredIterator;
import static org.apache.commons.collections4.IteratorUtils.transformedIterator;
import static org.apache.commons.collections4.PredicateUtils.instanceofPredicate;

import static nl.maastro.eureca.aida.velocityrdf.basicview.ModelTransformerUtils.literalCreator;


/**
 * Common implementation for one-to-many relations.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
abstract class AbstractMultiEdge {
	private final Model rdfModel;
	private final Resource singleEnd;
	private final URI viewedPredicate;
	private final N3IdTranslator identifierGenerator;


	protected AbstractMultiEdge(
			final Model rdfModel_,
			final Resource singleEnd_,
			final URI viewedPredicate_,
			final N3IdTranslator identifierGenerator_)
	{
		this.rdfModel = rdfModel_;
		this.singleEnd = singleEnd_;
		this.viewedPredicate = viewedPredicate_;
		this.identifierGenerator = identifierGenerator_;
	}
	

	protected abstract Set<? extends Value> selectNodesMultiEnd();

	
	public String getPredicate()
	{
		return identifierGenerator.getId(viewedPredicate);
	}


	protected Set<Value> selectObjects()
	{
		Model filtered = rdfModel.filter(singleEnd, viewedPredicate, null);
		Set<Value> result = filtered.objects();
		return result;
	}


	protected Set<Resource> selectSubjects() {
		Model filtered = rdfModel.filter(null, viewedPredicate, singleEnd);
		Set<Resource> result = filtered.subjects();
		return result;
	}
			
	
	
	protected Node getSingleEnd()
	{
		return new Node(rdfModel, singleEnd, identifierGenerator);
	}
	
	protected ArrayList<Node> getMultiEndNodes()
	{
		Set<? extends Value> objects = selectNodesMultiEnd();
		ArrayList<Node> result = new ArrayList<>( collect(
				filter(objects.iterator(), Resource.class), 
				Node.nodeCreator(rdfModel, identifierGenerator)));
		
		return result;
	}


	protected ArrayList<String> getMultiEndValues()
	{
		Set<? extends Value> objects = selectNodesMultiEnd();
		ArrayList<String> result = new ArrayList<>( collect(
				filter(objects.iterator(), Literal.class),
				literalCreator(rdfModel, identifierGenerator)));

		return result;
	}

	
	private <TRdfValueType/*>>>extends @NonNull Object*/> Iterator<TRdfValueType> filter(
			final Iterator<? extends Value> sourceIter,
			final Class<TRdfValueType> typeToInclude)
	{
		Iterator<Value> toIncludeIter = filteredIterator(
				sourceIter,
				instanceofPredicate(typeToInclude));
		
		Iterator<TRdfValueType> castIter = transformedIterator(
				toIncludeIter,
				dynCastTransformer(typeToInclude));
		
		return castIter;
	}

	
	private <I/*>>>extends @NonNull Object*/, O/*>>>extends @NonNull Object*/>
			Transformer<I, O> dynCastTransformer(final Class<O> targetType)
	{
		return new Transformer<I, O>() {
			@Override
			public O transform(I input) {
				return targetType.cast(input);
			}
		};
	}
}
