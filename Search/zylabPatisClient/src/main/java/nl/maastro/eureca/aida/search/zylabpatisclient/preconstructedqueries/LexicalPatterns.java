/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.DualRepresentationQuery;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LuceneObject;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.GroupQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ProximityQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

/**
 *
 * @author kasper
 */
enum LexicalPatterns implements Query, DualRepresentationQuery, LuceneObject {
	METASTASIS_NL("*metastase*"),
	
	STAGE_NL("stadium"),
	FOUR_ROMAN("IV"),
	FOUR_DIGIT("4"),
	STAGE_IV_NL(2, STAGE_NL, FOUR_ROMAN),
	STAGE_4_NL(2, STAGE_NL, FOUR_DIGIT),
	ANY_STAGE4(OrQueryNode.class, STAGE_IV_NL, STAGE_4_NL),
	
	UITZAAI_NL("uitzaai"),
	UITGEZAAID_NL("uitgezaaid"),
	ANY_UITZAAI(OrQueryNode.class, UITZAAI_NL, UITGEZAAID_NL),
	
	NOT_NL1("geen"),
	NOT_NL2("niet"),
	ANY_NEGATION(OrQueryNode.class, NOT_NL1, NOT_NL2),
	
	SIGNS_NL1("aanwijzing"),
	SIGNS_NL2("teken"),
	MOGELIJK("mogelijk"),
	SUSPECT("suspect"),
	BEELD("beeld"),
	PAS("pas"),
	PASSEN("passen"),
	BEELD_PAST(3,BEELD, PAS),
	BEELD_PASSEN(3, BEELD, PASSEN),
	VERDENKING("verdenking"),
	ANY_SIGNS(OrQueryNode.class, SIGNS_NL1, SIGNS_NL2, VERDENKING, SUSPECT, BEELD_PAST, BEELD_PASSEN, MOGELIJK);
	
	private enum Type {
		NORMAL,
		FUZZY,
		WILDCARD
	}
	
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
	private static final float EDIT_DISTANCE = 2.0f;
	private transient QName id = null;

	static List<QueryNode> containedNodes(final LexicalPatterns... pats) {
		final List<QueryNode> nodes = new ArrayList<>(pats.length);
		for (LexicalPatterns p : pats) {
			nodes.add(p.getParsetree_representation());
		}
		return nodes;
	}

	static SpanQuery[] containedSpans(final LexicalPatterns... pats) {
		final SpanQuery[] result = new SpanQuery[pats.length];
		for (int i = 0; i < pats.length; i++) {
			result[i] = pats[i].getLuceneObject_representation();
		}
		return result;
	}

	private LexicalPatterns(final String term) {
		this(term,
				term.contains("*") ?
					Type.WILDCARD :
					( term.contains("~") ?
						Type.FUZZY :
						Type.NORMAL));
	}

	private LexicalPatterns(final String term, Type type) {
		switch (type) {
			case NORMAL:
				parsetree_representation = new FieldQueryNode(PreconstructedQueries.instance().getDefaultField(), term, 0, term.length());
				luceneObject_representation = new SpanTermQuery(new Term(PreconstructedQueries.instance().getDefaultField(), term));
				break;
			case FUZZY:
				parsetree_representation = new FuzzyQueryNode(PreconstructedQueries.instance().getDefaultField(), term, EDIT_DISTANCE, 0, term.length());
				luceneObject_representation = new SpanMultiTermQueryWrapper<>(new FuzzyQuery(new Term(PreconstructedQueries.instance().getDefaultField(), term), 2));
				break;
			case WILDCARD:
				parsetree_representation = new WildcardQueryNode(PreconstructedQueries.instance().getDefaultField(), term, 0, term.length());
				luceneObject_representation = new SpanMultiTermQueryWrapper<>(new WildcardQuery(new Term(PreconstructedQueries.instance().getDefaultField(), term)));
				break;
			default:
				throw new Error(new IllegalArgumentException("Unexpected enum value for Type"));
		}
	}

	private LexicalPatterns(final int distance, final LexicalPatterns... pats) {
		List<QueryNode> requiredNodes = new ArrayList<>(pats.length);
		for (QueryNode node : containedNodes(pats)) {
			requiredNodes.add(new ModifierQueryNode(node, ModifierQueryNode.Modifier.MOD_REQ));
		}
		parsetree_representation = new GroupQueryNode(new ProximityQueryNode(requiredNodes, PreconstructedQueries.instance().getDefaultField(), ProximityQueryNode.Type.NUMBER, distance, false));
		luceneObject_representation = new SpanNearQuery(containedSpans(pats), distance, false);
	}

	private LexicalPatterns(final Class<OrQueryNode> dummy, final LexicalPatterns... pats) {
		parsetree_representation = new GroupQueryNode(new OrQueryNode(containedNodes(pats)));
		luceneObject_representation = new SpanOrQuery(containedSpans(pats));
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return PreconstructedQueries.instance().getVisitableDelegate().accept(this, visitor);
	}

	@Override
	public QName getName() {
		if (id == null) {
			try {
				id = PreconstructedQueries.instance().createQName(LexicalPatterns.this.name().toLowerCase());
			} catch (URISyntaxException ex) {
				throw new Error(ex);
			}
		}
		return id;
	}

	@Override
	public org.apache.lucene.search.Query getRepresentation() {
		return getLuceneObject_representation();
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
