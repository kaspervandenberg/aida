// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

import java.util.ArrayList;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.PreconstructedQueries;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.GroupQueryNode;
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
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class LexicalPatternBuilder {

	private static class SubPatternCollection {
		public ArrayList<QueryNode> nodes;
		public ArrayList<SpanQuery> luceneObjects;

		public SubPatternCollection(Iterable<LexicalPattern> patterns) {
			nodes = new ArrayList<>();
			luceneObjects = new ArrayList<>();
			
			for (LexicalPattern pat : patterns) {
				luceneObjects.add(pat.getLuceneObject_representation());
				nodes.add(pat.getParsetree_representation());
			}
		}
		
		public SpanQuery[] getSpanQueries() {
			return luceneObjects.toArray(new SpanQuery[luceneObjects.size()]);
		}
	}
	
	private static LexicalPatternBuilder instance = null;
	private String defaultField = null;
	private float defalutFuzzyDistance = 1.0f;
	private int defaultSpanNearDistance = 2;

	private LexicalPatternBuilder() {
	}

	public static LexicalPatternBuilder instance() {
		if(instance == null) {
			instance = new LexicalPatternBuilder();
		}
		return instance;
	}

	public LexicalPattern term(QName id, String term) {
		return new LexicalPattern(id,
				new FieldQueryNode(getField(), term, 0, term.length()),
				new SpanTermQuery(new Term(getField(), term)));
	}

	public LexicalPattern fuzzy(QName id, String term, int distance) {
		return new LexicalPattern(id,
				new FuzzyQueryNode(getField(), term, distance, 0, term.length()),
				new SpanMultiTermQueryWrapper<>(new FuzzyQuery(new Term(getField(), term), distance)));
	}
	
	public LexicalPattern fuzzy(QName id, String term) {
		return fuzzy(id, term, (int) getFuzzyDistance());
	}

	public LexicalPattern wildcard(QName id, String term) {
		return new LexicalPattern(id,
				new WildcardQueryNode(getField(), term, 0, term.length()),
				new SpanMultiTermQueryWrapper<>(new WildcardQuery(new Term(getField(), term))));
	}

	public LexicalPattern auto(QName id, String termExpr) {
		if (termExpr.contains("*")) {
			return wildcard(id, termExpr);
		} else if (termExpr.contains("~")) {
			return fuzzy(id, termExpr);
		} else {
			return term(id, termExpr);
		}
	}

	public LexicalPattern near(QName id, int distance, Iterable<LexicalPattern> patterns) {
		SubPatternCollection pats = new SubPatternCollection(patterns);
		return new LexicalPattern(id,
				new GroupQueryNode(new ProximityQueryNode(pats.nodes, getField(), ProximityQueryNode.Type.NUMBER, distance, false)),
				new SpanNearQuery(pats.getSpanQueries(), distance, false));
	}

	public LexicalPattern near(QName id, Iterable<LexicalPattern> patterns) {
		return near(id, getSpanNearDistance(), patterns);
	}

	public LexicalPattern or(QName id, Iterable<LexicalPattern> patterns) {
		SubPatternCollection pats = new SubPatternCollection(patterns);
		return new LexicalPattern(id,
				new GroupQueryNode(new OrQueryNode(pats.nodes)),
				new SpanOrQuery(pats.getSpanQueries()));
	}
	
	public LexicalPatternBuilder setDefaultField(final String newValue_) {
		this.defaultField = newValue_;
		return this;
	}

	protected String getField() {
		if(defaultField != null) {
			return defaultField;
		} else {
			try {
				return Config.instance().getDefaultField();
			} catch (IllegalStateException ex) {
				return PreconstructedQueries.instance().getDefaultField();
			}
		}
	}

	public LexicalPatternBuilder setFuzzyEditDistance(final int newValue_) {
		this.defalutFuzzyDistance = newValue_;
		return this;
	}
	
	protected float getFuzzyDistance() {
		return defalutFuzzyDistance;
	}

	public LexicalPatternBuilder setSpanNearDistance(final int newValue_) {
		this.defaultSpanNearDistance = newValue_;
		return this;
	}

	protected int getSpanNearDistance() {
		return defaultSpanNearDistance;
	}

}
