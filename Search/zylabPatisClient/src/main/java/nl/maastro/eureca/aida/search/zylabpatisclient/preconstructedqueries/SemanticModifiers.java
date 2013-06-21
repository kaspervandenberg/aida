/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.SemanticModifier;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LuceneObject;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.ParseTree;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.QueryAdapterBuilder;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.StringQuery;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.ClassMap;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ProximityQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessor;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;

/**
 *
 * @author kasper
 */
 enum SemanticModifiers implements SemanticModifier {
	NEGATED(4, LexicalPatterns.ANY_NEGATION), SUSPICION(4, LexicalPatterns.ANY_SIGNS, LexicalPatterns.QUESTION), NEGATED_SUSPICION(6, LexicalPatterns.ANY_SIGNS, LexicalPatterns.ANY_NEGATION), QUESTION(2, LexicalPatterns.QUESTION);
	private final LexicalPatterns[] modifierPatterns;
	private final int distance;
	private final ClassMap<Query, ClassMap<Query, QueryAdapterBuilder<? extends Query, ? extends Query>>> adapters;

	private SemanticModifiers(int distance_, LexicalPatterns... pats) {
		distance = distance_;
		modifierPatterns = pats;
		adapters = new ClassMap<>(ClassMap.RetrievalStrategies.SUBCLASS);
		adapters.put(LuceneObject.class, new ClassMap<>(ClassMap.RetrievalStrategies.SUPERCLASS, Collections.<Class<? extends Query>, QueryAdapterBuilder<?, ?>>singletonMap(LuceneObject.class, getAdapter_luceneObject())));
		adapters.put(ParseTree.class, new ClassMap<>(ClassMap.RetrievalStrategies.SUPERCLASS, Collections.<Class<? extends Query>, QueryAdapterBuilder<?, ?>>singletonMap(ParseTree.class, getAdapter_parseTree())));
	}

	private QueryNode compose(QueryNode query) {
		List<QueryNode> modifyingNodes = LexicalPatterns.containedNodes(modifierPatterns);
		List<QueryNode> nodes = new ArrayList<>(2);
		nodes.add(new OrQueryNode(modifyingNodes));
		nodes.add(new ModifierQueryNode(query, ModifierQueryNode.Modifier.MOD_REQ));
		return new ProximityQueryNode(nodes, PreconstructedQueries.instance().getDefaultField(), ProximityQueryNode.Type.NUMBER, distance, false);
	}

	private SpanQuery compose(org.apache.lucene.search.Query query) {
		SpanQuery modifyingNodes = new SpanOrQuery(LexicalPatterns.containedSpans(modifierPatterns));
		SpanQuery tmp;
		if (query instanceof SpanQuery) {
			tmp = (SpanQuery) query;
		} else if (query instanceof MultiTermQuery) {
			tmp = new SpanMultiTermQueryWrapper<>((MultiTermQuery) query);
		} else {
			throw new ClassCastException(String.format("Cannot convert obect of type %s to SpanQuery.", query.getClass().getName()));
		}
		return new SpanNearQuery(new SpanQuery[]{modifyingNodes, tmp}, distance, false);
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

	@Override
    @SuppressWarnings(value = "unchecked")
	public <TIn extends Query, TOut extends Query> QueryAdapterBuilder<TIn, TOut> getAdapterBuilder(Class<TIn> inClass, Class<TOut> outClass) throws IllegalArgumentException {
		return (QueryAdapterBuilder<TIn, TOut>) (adapters.get(inClass).get(outClass));
	}

	@Override
	public Map<Class<? extends Query>, Set<Class<? extends Query>>> getSupportedTypes() {
		Map<Class<? extends Query>, Set<Class<? extends Query>>> result = new HashMap<>();
		for (Class<? extends Query> inClass : adapters.keySet()) {
			result.put(inClass, adapters.get(inClass).keySet());
		}
		return result;
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
						throw new Error(new IllegalArgumentException("Cannot modify StringQueries."));
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
