// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

import javax.xml.namespace.QName;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.search.spans.SpanQuery;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class LexicalPattern implements Query, DualRepresentationQuery, LuceneObject {
	/**
	 * The {@link org.apache.lucene.queryparser.flexible.core.nodes.QueryNode}
	 * parsetree that corresponds to this {@code LexicalPattern}.
	 *
	 * @see nl.maastro.eureca.aida.search.zylabpatisclient.PreconstructedQueries.LexicalPatterns#luceneObject_representation
	 */
	private final QueryNode parsetreeRepresentation;
	/**
	 * The {@code LexicalPattern} as
	 * {@link org.apache.lucene.search.Query}-object that
	 * {@link org.apache.lucene.search.IndexSearcher} uses to search on.
	 *
	 * The current use of {@link org.apache.lucene.queryparser.flexible.core.builders.QueryBuilder#build(org.apache.lucene.queryparser.flexible.core.nodes.QueryNode)}
	 * does not convert {@link org.apache.lucene.queryparser.flexible.core.nodes.ProximityQueryNode}
	 * as intended, therefore {@code LexicalPatterns} store their
	 * representation in two variants:
	 * <ul><li>as a parse tree; and</li>
	 * 		<li>as a lucene.search.SpanQuery.</li></ul>
	 */
	private final SpanQuery luceneObjectRepresentation;
	private final QName id;

	public LexicalPattern(final QName id_, final QueryNode parseTree_, final org.apache.lucene.search.spans.SpanQuery luceneObject_) {
		this.id = id_;
		this.parsetreeRepresentation = parseTree_;
		this.luceneObjectRepresentation = luceneObject_;
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return DualRepresentationQuery.Visitable.AS_LUCENE_OBJECT.accept(this, visitor);
	}

	@Override
	public QName getName() {
		return id;
	}

	@Override
	public QueryNode getParsetreeRepresentation() {
		return parsetreeRepresentation;
	}

	@Override
	public SpanQuery getLuceneObjectRepresentation() {
		return luceneObjectRepresentation;
	}

	@Override
	public org.apache.lucene.search.Query getRepresentation() {
		return this.getLuceneObjectRepresentation();
	}
}
