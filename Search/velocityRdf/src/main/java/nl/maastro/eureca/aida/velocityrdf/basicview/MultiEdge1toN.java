// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.velocityrdf.basicview;

import java.util.ArrayList;
import java.util.Set;
import nl.maastro.eureca.aida.velocityrdf.n3_id_translator.N3IdTranslator;
import org.apache.commons.collections4.Transformer;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;


/**
 * Represents an edge with a single source and mutliple destinations.
 * 
 * <p>For example, a {@code MultiEdge1toN} can represent the following RDF 
 * statements:</p>
 * <ul>	<li>{@code :s1 :p1 :o1.}</li>
 * 		<li>{@code :s1 :p1 :o2.}</li>
 * 		<li>{@code :s1 :p1 "literal".}</li>
 * </ul><p>
 * {@link #getSubject()} would return a {@link Node} representing {@code s1}.
 * {@link #getPredicate()} would return a string {@code ":p1"}.
 * {@link #getObjectNodes()} would return a set of {@code Nodes}: {@code &#123;:o1, 
 * :o2&#125;}.  And, {@link #getObjectValues()} would return a set of strings:
 * {@code &#123;"literal"&#125;}.</p>
 * 
 * <p>See {@link MultiEdgeNto1} for the converse.</p>
 *
 * @author Kasper van den Berg &lt;kasper.vandenberg@maastro.nl&gt; &lt;kasper@kaspervandenberg.net&gt;
 */
public class MultiEdge1toN {
	private final AbstractMultiEdge delegate;
	
	public MultiEdge1toN(
			final Model rdfModel_,
			final Resource viewedSubject_,
			final URI viewedPredicate_,
			final N3IdTranslator identifierGenerator_)
	{
		this.delegate = new AbstractMultiEdge(
				rdfModel_, viewedSubject_, viewedPredicate_, identifierGenerator_)
		{
			@Override
			protected Set<? extends Value> selectNodesMultiEnd()
			{
				return super.selectObjects();
			}
		};
	}
	

	static Transformer<URI, MultiEdge1toN> multiEdgeCreator(
			final Model model_,
			final N3IdTranslator identifierGenerator_,
			final Resource subject_)
	{
		return new Transformer<URI, MultiEdge1toN>()
		{
			@Override
			public MultiEdge1toN transform(URI input)
			{
				return new MultiEdge1toN(model_, subject_, input, identifierGenerator_);
			}
		};
	}

	
	public Node getSubject()
	{
		return delegate.getSingleEnd();
	}
	

	public String getPredicate() {
		return delegate.getPredicate();
	}
	

	public ArrayList<Node> getObjectNodes()
	{
		return delegate.getMultiEndNodes();
	}

	
	public ArrayList<String> getObjectValues()
	{
		return delegate.getMultiEndValues();
	}
}


/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

