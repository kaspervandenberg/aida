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
	private final QueryNode parsetree_representation;
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
	private final SpanQuery luceneObject_representation;
	private final QName id;

	public LexicalPattern(final QName id_, final QueryNode parseTree_, final org.apache.lucene.search.spans.SpanQuery luceneObject_) {
		this.id = id_;
		this.parsetree_representation = parseTree_;
		this.luceneObject_representation = luceneObject_;
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
	public QueryNode getParsetree_representation() {
		return parsetree_representation;
	}

	@Override
	public SpanQuery getLuceneObject_representation() {
		return luceneObject_representation;
	}

	@Override
	public org.apache.lucene.search.Query getRepresentation() {
		return this.getLuceneObject_representation();
	}
}
