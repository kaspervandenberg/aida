// © Maastro Clinc, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries;

import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.DualRepresentationQuery;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;

/**
 * Preconstructed concepts
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
enum Concepts implements nl.maastro.eureca.aida.search.zylabpatisclient.query.Query, DualRepresentationQuery {
	METASTASIS(LexicalPatterns.METASTASIS_NL, LexicalPatterns.ANY_STAGE4, LexicalPatterns.ANY_UITZAAI);

	private Concepts(final LexicalPatterns... pats) {
		parsetree_representation = new OrQueryNode(LexicalPatterns.containedNodes(pats));
		luceneObject_representation = new SpanOrQuery(LexicalPatterns.containedSpans(pats));
	}
	private final QueryNode parsetree_representation;
	private final SpanQuery luceneObject_representation;
	private transient QName id = null;

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return PreconstructedQueries.instance().getVisitableDelegate().accept(this, visitor);
	}

	@Override
	public QName getName() {
		if (id == null) {
			try {
				id = PreconstructedQueries.instance().createQName(Concepts.this.name().toLowerCase());
			} catch (URISyntaxException ex) {
				throw new Error(ex);
			}
		}
		return id;
	}

	public QueryNode getRepresentation() {
		return parsetree_representation;
	}

	/**
	 * @return the parsetree_representation
	 */
	@Override
	public QueryNode getParsetree_representation() {
		return parsetree_representation;
	}

	/**
	 * @return the luceneObject_representation
	 */
	@Override
	public SpanQuery getLuceneObject_representation() {
		return luceneObject_representation;
	}
	
}
