// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.velocityrdf.basicview;

import java.util.ArrayList;
import java.util.Set;
import nl.maastro.eureca.aida.velocityrdf.n3_id_translator.N3IdTranslator;
import org.apache.commons.collections4.Transformer;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

/**
 * Represent an edge with multiple sources and a single destination.
 * 
 * <p>For example, it can represent the following RDF statements:
 * <ul>	<li>{@code :s1 :p2 :o1.}</li>
 * 		<li>{@code :s2 :p2 :o1.}</li>
 * 		<li>{@code :s3 :p2 :o1.}</li>
 * </ul>
 * {@link #getSubjects()} would return a set of {@link Node}s containing
 * {@code {:s1, :s2, :s3}}.  {@link #getPredicate()} would return a string 
 * {@code ":p2"}.  And, {@link #getObject()} would return a {@code Node}
 * {@code :o1}.</p>
 * 
 * <p>See {@link MultiEdge1toN} for the converse.</p>
 *
 * @author Kasper van den Berg &lt;kasper.vandenberg@maastro.nl&gt; &lt;kasper@kaspervandenberg.net&gt;
 */
public class MultiEdgeNto1 {
	private final AbstractMultiEdge delegate;

	
	public MultiEdgeNto1(
			final Model rdfModel_,
			final Resource viewedObject_,
			final URI viewedPredicate_,
			final N3IdTranslator identifierGenerator_)
	{
		this.delegate = new AbstractMultiEdge(
				rdfModel_, viewedObject_, viewedPredicate_, identifierGenerator_)
		{
			@Override
			protected Set<Resource> selectNodesMultiEnd()
			{
				return super.selectSubjects();
			}
		};
	}

	
	static Transformer<URI, MultiEdgeNto1> multiEdgeCreator(
			final Model model_,
			final N3IdTranslator identifierGenerator_,
			final Resource object_)
	{
		return new Transformer<URI, MultiEdgeNto1>()
		{
			@Override
			public MultiEdgeNto1 transform(URI input)
			{
				return new MultiEdgeNto1(model_, object_, input, identifierGenerator_);
			}
		};
	}
	

	public ArrayList<Node> getSubjects()
	{
		return delegate.getMultiEndNodes();
	}


	public String getPredicate() {
		return delegate.getPredicate();
	}
	

	public Node getObject()
	{
		return delegate.getSingleEnd();
	}
}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

