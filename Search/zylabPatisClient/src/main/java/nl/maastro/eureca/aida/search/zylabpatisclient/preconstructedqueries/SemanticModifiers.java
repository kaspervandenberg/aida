/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;
import nl.maastro.eureca.aida.search.zylabpatisclient.SemanticModifier;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LuceneObject;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LuceneObjectBase;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.ParseTree;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.ParseTreeBase;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.QueryAdapterBuilder;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.ClassMap;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.QNameUtil;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ProximityQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;

import static nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries.LexicalPatterns.*;

/**
 *
 * @author kasper
 */
public enum SemanticModifiers {
	NEGATED_PREFIX(2, EligibilityClassification.UNCERTAIN,
			new LexicalPatterns[] {ANY_NEGATION},
			new LexicalPatterns[] {}),
	SUSPICION_PREFIX(4, EligibilityClassification.UNCERTAIN,
			new LexicalPatterns[] {SIGNS_NL1,
				SIGNS_NL2,
				MOGELIJK,
				MISSCHIEN,
				KUNNEN_ALL,
				ZOU},
			new LexicalPatterns[] { }),
	SUSPICION_ANY_ORDER(4, EligibilityClassification.UNCERTAIN,
			SUSPECT,
			BEELD_PAST,
			KUNNEN_ZIJN,
			VRAAG,
			INDERDAAD_DE_VRAAG),
//	SUSPICION(4, EligibilityClassification.UNCERTAIN,
//			ANY_SIGNS),
	;

	private enum ModifierForm {
		PREFIX {
			@Override
			public SpanQuery[] composeArray(Config config, SpanQuery query, SpanQuery[] prefixModifierPatterns, SpanQuery[] suffixModifierPatterns) {
				SpanQuery prefix = toSpanQuery("Expecting prefix modifiers.", prefixModifierPatterns);
				expectEmpty("Expecting no suffix modifiers.", suffixModifierPatterns);
				return new SpanQuery[] {prefix, query};
			}

			@Override
			public void addPrefix(List<QueryNode> target, List<QueryNode> items) {
				add("Expecting prefix modifiers.", target, items);
			}

			@Override
			public void addSuffix(List<QueryNode> target, List<QueryNode> items) {
				expectEmpty("Expecting no suffix modifiers.", items);
			}
		},
		SUFFIX {
			@Override
			public SpanQuery[] composeArray(Config config, SpanQuery query, SpanQuery[] prefixModifierPatterns, SpanQuery[] suffixModifierPatterns) {
				expectEmpty(
						"Expecting no prefix modifiers.",
						prefixModifierPatterns);
				SpanQuery suffix = toSpanQuery(
						"Expecting suffix modifiers.",
						suffixModifierPatterns);
				return new SpanQuery[] {query, suffix};
			}

			@Override
			public void addPrefix(List<QueryNode> target, List<QueryNode> items) {
				expectEmpty("Expecting no prefix modifiers.", items);
			}

			@Override
			public void addSuffix(List<QueryNode> target, List<QueryNode> items) {
				add("Expecting suffix modifiers.", target, items);
			}
		},
		BOTH {
			@Override
			public SpanQuery[] composeArray(Config config,
					SpanQuery query,
					SpanQuery[] prefixModifierPatterns,
					SpanQuery[] suffixModifierPatterns) {
				SpanQuery prefix = toSpanQuery(
						"Expecting prefix modifiers.",
						prefixModifierPatterns);
				SpanQuery suffix = toSpanQuery(
						"Expecting suffix modifiers.",
						suffixModifierPatterns);
				return new SpanQuery[] {prefix, query, suffix};
			}

			@Override
			public void addPrefix(List<QueryNode> target, List<QueryNode> items) {
				add("Expecting prefix modifiers.", target, items);
			}

			@Override
			public void addSuffix(List<QueryNode> target, List<QueryNode> items) {
				add("Expecting suffix modifiers.", target, items);
			}
			
		},
		EMPTY {
			@Override
			public SpanQuery[] composeArray(Config config,
					SpanQuery query,
					SpanQuery[] prefixModifierPatterns,
					SpanQuery[] suffixModifierPatterns) {
				expectEmpty(
						"Expecting no prefix modifiers",
						prefixModifierPatterns);
				expectEmpty(
						"Expecting no suffix modifiers",
						suffixModifierPatterns);

				return new SpanQuery[] {query};
			}

			@Override
			public void addPrefix(List<QueryNode> target, List<QueryNode> items) {
				expectEmpty("Expecting no prefix modifiers", items);
			}

			@Override
			public void addSuffix(List<QueryNode> target, List<QueryNode> items) {
				expectEmpty("Expecting no suffix modifiers", items);
			}
		};

		public static ModifierForm selectForm(LexicalPatterns[] prefix,
				LexicalPatterns[] suffix) {
			if(!isEmpty(prefix)) {
				if(isEmpty(suffix)) {
					return PREFIX;
				} else {
					return  BOTH;
				}
			} else {
				if(!isEmpty(suffix)) {
					return SUFFIX;
				} else {
					return EMPTY;
				}
			}
		}

		public abstract SpanQuery[] composeArray(
				Config config,
				SpanQuery query,
				SpanQuery[] prefixModifierPatterns,
				SpanQuery[] suffixModifierPatterns);

		public abstract void addPrefix(List<QueryNode> target, List<QueryNode> items); 
		
		public abstract void addSuffix(List<QueryNode> target, List<QueryNode> items); 

		protected void add(String emptyMsg, List<QueryNode> target, List<QueryNode> items) {
			if(items.isEmpty()) {
				throw new IllegalStateException(emptyMsg);
			}
			target.add(new OrQueryNode(items));
		}

		protected void expectEmpty(String nonEmptyMsg, List<QueryNode> items) {
			if(!items.isEmpty()) {
				throw new IllegalStateException(nonEmptyMsg);
			}
		}

		protected void expectEmpty(String nonEmptyMsg, SpanQuery items[]) {
			if(!isEmpty(items)) {
				throw new IllegalStateException(nonEmptyMsg);
			}
		}

		protected SpanQuery toSpanQuery(String emptyMsg, SpanQuery items[]) {
			if(isEmpty(items)) {
				throw new IllegalStateException(emptyMsg);
			}
			return new SpanOrQuery(items);
		}
		
		private static <T> boolean isEmpty(T[] items) {
			return items.length == 0;
		}
	}
	
	private final LexicalPatterns[] prefixModifierPatterns;
	private final LexicalPatterns[] suffixModifierPatterns;
	private final int distance;
	private final boolean inOrder;
	private final EligibilityClassification classification;
	private final Map<Config, SemanticModifier> instances =
			new HashMap<>();

	private class Impl implements SemanticModifier {
		private final Config config;
		private final ClassMap<Query, ClassMap<Query, QueryAdapterBuilder<? extends Query, ? extends Query>>> adapters;

		public Impl(final Config config_) {
			this.config = config_;
			
			adapters = new ClassMap<>(ClassMap.RetrievalStrategies.SUBCLASS);
			adapters.put(LuceneObject.class,
				createSingletonMapping(LuceneObject.class, getAdapter_luceneObject()));
			adapters.put(ParseTree.class,
				createSingletonMapping(ParseTree.class, getAdapter_parseTree()));
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

		@Override
		public EligibilityClassification getClassification() {
			return classification;
		}
		
		public QueryNode compose(QueryNode query) {
			ModifierForm form = ModifierForm.selectForm(
					prefixModifierPatterns,
					suffixModifierPatterns);
			List<QueryNode> nodes = new ArrayList<>(3);
			
			form.addPrefix(nodes, LexicalPatterns.containedNodes(
					LexicalPatterns.toPatternIterable(
							config,
							Arrays.asList(prefixModifierPatterns))));
			nodes.add(new ModifierQueryNode(
					query,
					ModifierQueryNode.Modifier.MOD_REQ));
			form.addSuffix(nodes, LexicalPatterns.containedNodes(
					LexicalPatterns.toPatternIterable(
							config,
							Arrays.asList(suffixModifierPatterns))));
			
			return new ProximityQueryNode(
					nodes, 
					config.getDefaultField(), 
					ProximityQueryNode.Type.NUMBER, 
					distance, 
					inOrder);
		}

		public SpanQuery compose(org.apache.lucene.search.Query query) {
			ModifierForm form = ModifierForm.selectForm(
					prefixModifierPatterns,
					suffixModifierPatterns);
			
			SpanQuery tmp;
			if (query instanceof SpanQuery) {
				tmp = (SpanQuery) query;
			} else if (query instanceof MultiTermQuery) {
				tmp = new SpanMultiTermQueryWrapper<>((MultiTermQuery) query);
			} else {
				throw new ClassCastException(String.format("Cannot convert obect of type %s to SpanQuery.", query.getClass().getName()));
			}
			
			SpanQuery[] compound = form.composeArray(config, tmp,
					LexicalPatterns.containedSpans(
							LexicalPatterns.toPatternIterable(
									config,
									Arrays.asList(prefixModifierPatterns))),
					LexicalPatterns.containedSpans(
							LexicalPatterns.toPatternIterable(
									config,
									Arrays.asList(suffixModifierPatterns))));
			
			return new SpanNearQuery(compound, distance, inOrder);
		}

		private QueryAdapterBuilder<LuceneObject, LuceneObject> getAdapter_luceneObject() {
			return new QueryAdapterBuilder<LuceneObject, LuceneObject>() {
				@Override
				public LuceneObject adapt(final LuceneObject adapted) {
					return new LuceneObjectBase() {
						@Override
						public org.apache.lucene.search.Query getRepresentation() {
							return compose(adapted.getRepresentation());
						}

						@Override
						public QName getName() {
							return QNameUtil.instance().append(adapted.getName(),
									"-" + SemanticModifiers.this.name());
						}
					};
				}
			};
		}

		private QueryAdapterBuilder<ParseTree, ParseTree> getAdapter_parseTree() {
			return new QueryAdapterBuilder<ParseTree, ParseTree>() {
				@Override
				public ParseTree adapt(final ParseTree adapted) {
					return new ParseTreeBase() {
						@Override
						public QueryNode getRepresentation() {
							return compose(adapted.getRepresentation());
						}

						@Override
						public QName getName() {
							return QNameUtil.instance().append(adapted.getName(),
									"-" + SemanticModifiers.this.name());
						}
					};
				}
			};
		}

	}
	
	private SemanticModifiers(int distance_, EligibilityClassification classification_,
			LexicalPatterns... pats) {
		this.distance = distance_;
		this.inOrder = false;
		this.prefixModifierPatterns = pats;
		this.suffixModifierPatterns = new LexicalPatterns[0];
		this.classification = classification_;
	}

	private SemanticModifiers(int distance_,
			EligibilityClassification classification,
			LexicalPatterns[] prefixPatterns,
			LexicalPatterns[] suffixPatterns) {
		this.distance = distance_;
		this.inOrder = true; 
		this.prefixModifierPatterns = prefixPatterns;
		this.suffixModifierPatterns = suffixPatterns;
		this.classification = classification;
	}

	private static <T extends Query> ClassMap<Query, QueryAdapterBuilder<? extends Query, ? extends Query>>
			createSingletonMapping(
			Class<T> type, QueryAdapterBuilder<T, T> adapter) {
		return new ClassMap<Query, QueryAdapterBuilder<? extends Query, ? extends Query>>(ClassMap.RetrievalStrategies.SUPERCLASS,
				Collections.singletonMap(type, adapter));
	}

	public static Iterable<SemanticModifier> toModifierIterable(
			final Config config,
			final Iterable<SemanticModifiers> items) {
		return new Iterable<SemanticModifier>() {
			final Iterator<SemanticModifiers> delegate = items.iterator();
			@Override
			public Iterator<SemanticModifier> iterator() {
				return new Iterator<SemanticModifier>() {
					@Override
					public boolean hasNext() {
						return delegate.hasNext();
					}

					@Override
					public SemanticModifier next() {
						return delegate.next().getModifier(config);
					}

					@Override
					public void remove() {
						delegate.remove();
					}
				};
			}
		};
	}

	public SemanticModifier getModifier(final Config config) {
		if(!instances.containsKey(config)) {
			SemanticModifier semmod = new Impl(config);
			instances.put(config, semmod);
		}
		return instances.get(config);
	}
}
