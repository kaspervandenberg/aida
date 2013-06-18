// © Maastro, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import nl.maastro.eureca.aida.search.zylabpatisclient.query.DualRepresentationQuery;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.QueryProvider;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LuceneObject;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.ParseTree;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.ParseTreeToObjectAdapter;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.QueryAdapterBuilder;
//import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.StringQuery;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.GroupQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ProximityQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessor;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiTermQuery;
//import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

/**
 * Contains preconstructed queries to search for oncological concepts
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class PreconstructedQueries {
	private enum LexicalPatterns implements nl.maastro.eureca.aida.search.zylabpatisclient.query.Query,
			DualRepresentationQuery {
		METASTASIS_NL("metastase"),
		
		STAGE_NL("stadium"),
		FOUR_ROMAN("IV", false),
		FOUR_DIGIT("4", false),
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
		ANY_SIGNS(OrQueryNode.class, SIGNS_NL1, SIGNS_NL2)
		;
		
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
			this(term, false);
		}	

		private LexicalPatterns(final String term, boolean fuzzy) {
			if(fuzzy) {
				parsetree_representation = new FuzzyQueryNode(DEFAULT_FIELD, term, 2.0f, 0, term.length());
				luceneObject_representation = new SpanMultiTermQueryWrapper<>(
						new FuzzyQuery(new Term(DEFAULT_FIELD, term), 2));
			} else {
				parsetree_representation = new FieldQueryNode(DEFAULT_FIELD, term, 0, term.length());
				luceneObject_representation = new SpanTermQuery(new Term(DEFAULT_FIELD, term));
			}
		}

		private LexicalPatterns(final int distance, final LexicalPatterns... pats) {
			
			List<QueryNode> requiredNodes = new ArrayList<>(pats.length);
			for (QueryNode node : containedNodes(pats)) {
				requiredNodes.add(new ModifierQueryNode(
						node, ModifierQueryNode.Modifier.MOD_REQ));
			}
			parsetree_representation = new GroupQueryNode(
					new ProximityQueryNode(
					requiredNodes, DEFAULT_FIELD,
					ProximityQueryNode.Type.NUMBER, distance, false));
			
			luceneObject_representation = new SpanNearQuery(
					containedSpans(pats), distance, false);
		}

		private LexicalPatterns(final Class<OrQueryNode> dummy, final LexicalPatterns... pats) {
			parsetree_representation = new GroupQueryNode(new OrQueryNode(containedNodes(pats)));
			luceneObject_representation = new SpanOrQuery(containedSpans(pats));
		}
		
		@Override
		public <T> T accept(Visitor<T> visitor) {
			return VISITABLE_DELEGATE.accept(this, visitor);
		}

		@Override
		public QName getName() {
			if (id == null) {
				try {
					id = createQName(LexicalPatterns.this.name().toLowerCase());
				} catch (URISyntaxException ex) {
					throw new Error(ex);
				}
			}
			return id;
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

	private enum Concepts implements nl.maastro.eureca.aida.search.zylabpatisclient.query.Query,
			DualRepresentationQuery {
		METASTASIS(LexicalPatterns.METASTASIS_NL, 
				LexicalPatterns.ANY_STAGE4, LexicalPatterns.ANY_UITZAAI)
		;
			
		private Concepts(final LexicalPatterns... pats) {
			parsetree_representation = new OrQueryNode(LexicalPatterns.containedNodes(pats));
			luceneObject_representation = new SpanOrQuery(LexicalPatterns.containedSpans(pats));
		}
			
		private final QueryNode parsetree_representation;
		private final SpanQuery luceneObject_representation;
		private transient QName id = null;


		@Override
		public <T> T accept(Visitor<T> visitor) {
			return VISITABLE_DELEGATE.accept(this, visitor);
		}

		@Override
		public QName getName() {
			if (id == null) {
				try {
					id = createQName(Concepts.this.name().toLowerCase());
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

	private enum SemanticModifiers  {
		NEGATED(4, LexicalPatterns.ANY_NEGATION),
		SUSPICION(4, LexicalPatterns.ANY_SIGNS),
		NEGATED_SUSPICION(6, LexicalPatterns.ANY_SIGNS, LexicalPatterns.ANY_NEGATION);

		private final LexicalPatterns[] modifierPatterns;
		private final int distance;

		private SemanticModifiers(int distance_, LexicalPatterns... pats) {
			distance = distance_;
			modifierPatterns = pats;
		}
		
		private QueryNode compose(QueryNode query) {
			List<QueryNode> modifyingNodes = LexicalPatterns.containedNodes(modifierPatterns);
			List<QueryNode> nodes = new ArrayList<>(2);
			nodes.add(new OrQueryNode(modifyingNodes));
			nodes.add(new ModifierQueryNode(query, ModifierQueryNode.Modifier.MOD_REQ));
			
			return new ProximityQueryNode(nodes, DEFAULT_FIELD, ProximityQueryNode.Type.NUMBER, distance, false);
		}

		private SpanQuery compose(org.apache.lucene.search.Query query) {
			SpanQuery modifyingNodes = new SpanOrQuery(LexicalPatterns.containedSpans(modifierPatterns));
			SpanQuery tmp;
			if (query instanceof SpanQuery) {
				tmp = (SpanQuery)query;
			} else if (query instanceof MultiTermQuery) {
				tmp = new SpanMultiTermQueryWrapper<>((MultiTermQuery)query); 
			} else {
				throw new ClassCastException(String.format(
						"Cannot convert obect of type %s to SpanQuery.",
						query.getClass().getName()));
			}
			
			return new SpanNearQuery(new SpanQuery[] {
						modifyingNodes,
						tmp },
					distance, false);
		}

		public QueryNodeProcessor getProcessor() {
			return new QueryNodeProcessor() {
				private QueryConfigHandler ignoredConfigHandler = null;
				
				@Override
				public QueryNode process(QueryNode queryTree) throws QueryNodeException {
					return compose(queryTree);
				}

				@Override
				public void setQueryConfigHandler(QueryConfigHandler queryConfigHandler) {
					ignoredConfigHandler = queryConfigHandler;
				}

				@Override
				public QueryConfigHandler getQueryConfigHandler() {
					return ignoredConfigHandler;
				}
			};
		}
		
		public QueryAdapterBuilder<LuceneObject, LuceneObject> getAdapter_luceneObject() {
			return new QueryAdapterBuilder<LuceneObject, LuceneObject>() {
				@Override
				public LuceneObject adapt(final LuceneObject adapted) {
					return new LuceneObject() {
						@Override
						public org.apache.lucene.search.Query getRepresentation() {
							return compose(adapted.getRepresentation());
						}

						@Override
						public QName getName() {
							return adapted.getName();
						}
					};
				}
			};
		}

		public QueryAdapterBuilder<ParseTree, ParseTree> getAdapter_parseTree() {
			return new QueryAdapterBuilder<ParseTree, ParseTree>() {
				@Override
				public ParseTree adapt(final ParseTree adapted) {
					return new ParseTree() {

						@Override
						public QueryNode getRepresentation() {
							return compose(adapted.getRepresentation());
						}

						@Override
						public QName getName() {
							return adapted.getName();
						}
					};
				}
			};
		}

		public QueryAdapterBuilder<Query, Query> getAdapter_dynamic() {
			return new QueryAdapterBuilder<Query, Query>() {
				@Override
				public Query adapt(final Query adapted) {
					return adapted.accept(new Query.Visitor<Query>() {
						@Override
						public Query visit(LuceneObject element) {
							return getAdapter_luceneObject().adapt(element);
						}

						@Override
						public Query visit(StringQuery element) {
							throw new Error(new IllegalArgumentException(
									"Cannot modify StringQueries."));
						}

						@Override
						public Query visit(ParseTree element) {
							return getAdapter_parseTree().adapt(element);
						}
					});
				}
			};
		}
	}
	
	/**
	 * Build an {@link QName} from {@link #PREFIX}, the namespace URI
	 * {@link #getNamespaceUri() } and this local part.  {@code createQName()}
	 * uses {@link QName#QName(java.lang.String, java.lang.String)}
	 * to build the requested {@code QName}.
	 * 
	 * @param localpart	the local part of the QName to create
	 * 
	 * @return	the constructed {@link QName}
	 * 
	 * @throws URISyntaxException when the constructed URI has an syntax 
	 * 		error. 
	 */
	private static QName createQName(String localpart) 
			throws URISyntaxException {
		return new QName(getNamespaceUri().toString(), localpart, PREFIX);
	}

	/**
	 * QName local parts that prefixed with {@link PreconstructedQueries#getNamespaceUri()}
	 * form the {@link QName}s that identify the {@link Query}s in 
	 * {@link #storedPredicates}.
	 */
	public enum LocalParts {
		METASTASIS("metastasis", Concepts.METASTASIS, null),
		HINTS_METASTASIS("hints_metastasis", Concepts.METASTASIS, 
				SemanticModifiers.SUSPICION),
		NO_METASTASIS("no_metastasis", Concepts.METASTASIS, SemanticModifiers.NEGATED),
		NO_HINTS_METASTASIS("no_hints_metastasis", Concepts.METASTASIS, 
				SemanticModifiers.NEGATED_SUSPICION);

		private final String value;
		private final Concepts concept;
		private final SemanticModifiers modifier;
		private transient QName id = null;
		private transient nl.maastro.eureca.aida.search.zylabpatisclient.query.Query query;

		private LocalParts(final String value_, final Concepts concept_, 
				final SemanticModifiers modifier_) {
			value = value_;
			concept = concept_;
			modifier = modifier_;
		}

		/**
		 * Return the {@link QName} composed from this {@code LocalPart} and 
		 * {@link #getNamespaceUri()}.
		 * 
		 * @see #createQName(java.lang.String) 
		 * 
		 * @return	the {@link QName} to identify a preconstructed query with
		 */
		public QName getID() {
			if(id == null) {
				try {
					id = createQName(value);
				} catch (URISyntaxException ex) {
					throw new Error("URISyntaxException in hardcoded URI", ex);
				}
			}
			return id;
		}
			
		public nl.maastro.eureca.aida.search.zylabpatisclient.query.Query getQuery() {
			if(query == null) {
				if(modifier == null) {
					query = concept;
				} else {
					query = modifier.getAdapter_dynamic().adapt(concept);
				}
			}
			return query;
		}
	}

	/**
	 * Lucene DEFAULT_FIELD to use for the {@link SearchTerms}.
	 */
	private static final String DEFAULT_FIELD = "content";

	private static final DualRepresentationQuery.Visitable VISITABLE_DELEGATE =
			DualRepresentationQuery.Visitable.AS_LUCENE_OBJECT;

	/**
	 * {@link Term#text()text-part} of the {@link Term}s used in preconstructed 
	 * queries.
	 * 
	 * 
	 */
	private enum SearchTerms {
		METASTASIS_EN("metastasis"),
		METASTASIS_NL("metastase"),
		STAGE_EN("stage"),
		STAGE_NL("stadium"),
		FOUR_ROMAN("IV"),
		FOUR_DIGIT("4"),
		EXTENSIVE("extensive"),
		DISEASE("disease"),
		UITZAAIING("uitzaaiing"),
		GEEN("geen"),
		AANWIJZING("aanwijzing")
		;

		private final Term value;
		
		/**
		 * Create a SearchTerm for {2code value_} in the {@link #DEFAULT_FIELD}.
		 * 
		 * @param value_	the text part of the {@link Term} to search for.
		 */
		private SearchTerms(final String value_) {
			value = new Term(DEFAULT_FIELD, value_);
		}

		/**
		 * @return the {@link Term} to search for
		 */
		public Term getTerm() {
			return value;
		}

		/**
		 * Create a {@link FuzzyQuery} from this {@code SearchTerm}; that is a 
		 * {@link Query} that allows minor variations of this search term (for 
		 * example those caused by OCR).
		 * 
		 * @return the {@link FuzzyQuery#FuzzyQuery(org.apache.lucene.index.Term) constructed}
		 * 		{@link FuzzyQuery}
		 */
		public FuzzyQuery getFuzzyQuery() {
			return new FuzzyQuery(getTerm());
		}

		/**
		 * Create a 'fuzzy span' for this {@code SearchTerm}.  Fuzzy means that 
		 * minor variations of this {@code SearchTerm} match. {@link SpanQuery 
		 * Spans} allow construction of queries where parts are near other parts.
		 * 
		 * @return	this {@code SearchTerm} as a {@link #getFuzzyQuery()} 
		 * {@link SpanMultiTermQueryWrapper#SpanMultiTermQueryWrapper(org.apache.lucene.search.MultiTermQuery) wrapped}
		 * into a {@link SpanQuery}
		 */
		public SpanMultiTermQueryWrapper<FuzzyQuery> getFuzzySpan() {
			return new SpanMultiTermQueryWrapper<>(getFuzzyQuery());
		}

		/**
		 * Create a span that exactly matches this {@code SearchTerm}.  The 
		 * {@link SpanQuery} is usable a part of a span, which allows matching
		 * when parts are found near each other.
		 * 
		 * @return	a {@link SpanTermQuery} of this {@code SearchTerm} 
		 */
		public SpanTermQuery getExactSpan() {
			return new SpanTermQuery(getTerm());
		}
	}

	public static class Provider implements QueryProvider {
		@Override
		public Collection<QName> getQueryIds() {
			return PreconstructedQueries.instance().getIds();
		}


		@Override
		public boolean provides(QName id) {
			return PreconstructedQueries.instance().getIds().contains(id);
		}

		@Override
		@Deprecated
		public boolean hasString(QName id) {
			return false;
		}

		@Override
		@Deprecated
		public boolean hasObject(QName id) {
			return PreconstructedQueries.instance().getIds().contains(id);
		}

		@Override
		@Deprecated
		public String getAsString(QName id) throws NoSuchElementException {
			throw new NoSuchElementException("PreconstructedQueries only provides Lucene Query objects");
		}

		@Override
		@Deprecated
		public org.apache.lucene.search.Query getAsObject(QName id) throws NoSuchElementException {
			nl.maastro.eureca.aida.search.zylabpatisclient.query.Query q =
					PreconstructedQueries.instance().getQuery(id);
			return ADAPTER_BUILDER.adapt(q.accept(new nl.maastro.eureca.aida.search.zylabpatisclient.query.Query.Visitor<ParseTree>(){
				@Override
				public ParseTree visit(LuceneObject element) {
					throw new IllegalStateException("Expected parse tree.");
				}

				@Override
				public ParseTree visit(StringQuery element) {
					throw new IllegalStateException("Expected parse tree.");
				}

				@Override
				public ParseTree visit(ParseTree element) {
					return element;
				}
			 })).getRepresentation();
		}
		@Override
		public nl.maastro.eureca.aida.search.zylabpatisclient.query.Query get(QName id) {
			return PreconstructedQueries.instance().storedPredicates.get(id).getQuery();
		}
	}
			
	private static final String SEARCH_PROPERTY_RESOURCE = "/search.properties";
	private static final String SERVLET_URI_PROP = "nl.maastro.eureca.aida.search.zylabpatisclient.servletUri";
	private static final String DEFAULT_SERVLET_URI = "http://vocab.maastro.nl/zylabpatis";
	private static final String PREFIX = "pcq";
	private static final ParseTreeToObjectAdapter.Builder ADAPTER_BUILDER =
			new ParseTreeToObjectAdapter.Builder();
	
	private static URI servletUri = null;
	
	/**
	 * Queries to search for patients that match certain predicates.
	 * 
	 */
	// TODO Move to SearcherWS and provide interface to access stored queries
	private final Map<QName, LocalParts> storedPredicates;

	/**
	 * Singleton instance, use {@link #instance()} to access
	 */
	private static PreconstructedQueries instance;

	/**
	 * Singleton, use {@link #instance()} to retrieve the sole instance.
	 */
	private PreconstructedQueries() {
		Map<QName, LocalParts> tmp = new HashMap<>();
		for (LocalParts part : LocalParts.values()) {
			tmp.put(part.getID(), part);
		}
		storedPredicates = Collections.unmodifiableMap(tmp);
	}

	/**
	 * Access this singleton
	 * 
	 * @return the sole instance of {@code PreconstructedQueries}
	 */
	public static PreconstructedQueries instance() {
		if(instance == null) {
			instance = new PreconstructedQueries();
		}
		return instance;
	}

	private nl.maastro.eureca.aida.search.zylabpatisclient.query.Query
			getQuery(final QName key) {
		return storedPredicates.get(key).getQuery();
	}

	private nl.maastro.eureca.aida.search.zylabpatisclient.query.Query
			getQuery(final LocalParts keyFragment) {
		return getQuery(keyFragment.getID());
	}
	
	public Collection<QName> getIds() {
		return Collections.unmodifiableSet(storedPredicates.keySet());
	}
	
	private static URI getNamespaceUri() {
		if(servletUri == null) {
			InputStream propertyFile = PreconstructedQueries.class.getResourceAsStream(SEARCH_PROPERTY_RESOURCE);
			Properties props = new Properties();
			try {
				props.load(propertyFile);
				String s_uri = props.getProperty(SERVLET_URI_PROP, DEFAULT_SERVLET_URI);
				servletUri = new URI(s_uri);
				
			} catch (IOException | URISyntaxException ex) {
				throw new Error(ex);
			}
		}
		return servletUri;
	}
}
