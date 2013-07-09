// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

import java.util.ArrayList;
import java.util.List;
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
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class LexicalPatternBuilder {
	public static class LexicalPattern implements Query, DualRepresentationQuery, LuceneObject {
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

		public LexicalPattern(final QName id_, final QueryNode parseTree_,
				final org.apache.lucene.search.spans.SpanQuery luceneObject_) {
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

	private static class SubPatternCollection {
		public ArrayList<QueryNode> nodes;
		public ArrayList<SpanQuery> luceneObjects;

		public SubPatternCollection(Iterable<LexicalPattern> patterns) {
			nodes = new ArrayList<>();
			luceneObjects = new ArrayList<>();
			
			for (LexicalPatternBuilder.LexicalPattern pat : patterns) {
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

	private static class XmlParser extends DefaultHandler {
		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
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
