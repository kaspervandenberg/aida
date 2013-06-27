package nl.maastro.eureca.aida.search.zylabpatisclient.query;

import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.SemanticModifier;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.ClassMap;

/**
 * Adapt any {@link Query} ({@link StringQuery}, {@link ParseTree}, or 
 * {@link LuceneObject} to any other type of {@Query}.
 * 
 * @author Kasper vanden Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DynamicAdapter {
	
	@SuppressWarnings("serial")  
	private class Table extends ClassMap<Query, ClassMap<Query,  
				QueryAdapterBuilder<? extends Query, ? extends Query>>> {

		public Table() {
			super(RetrievalStrategies.SUPERCLASS);
		}
		
		@SuppressWarnings("unchecked")
		public <TIn extends Query, TOut extends Query> 
				QueryAdapterBuilder<TIn, TOut> put(
					Class<TOut> key1,
					Class<TIn> key2,
					QueryAdapterBuilder<TIn, TOut> newValue_) {
			if (!this.containsKey(key1)) {
				this.put(key1, new ClassMap<Query, QueryAdapterBuilder<? extends Query, ? extends Query>>(RetrievalStrategies.SUBCLASS));
			}
			QueryAdapterBuilder<? extends Query, ? extends Query> result =
					this.getStrict(key1).put(key2, newValue_);
			
			/*
			 * Up–down cast to convert QAB<?, ?> to QAB<TIn, TOut> 
			 * (see http://stackoverflow.com/q/7502243/814206 and
			 * http://stackoverflow.com/a/7505867/814206
			 */
			return (QueryAdapterBuilder<TIn, TOut>)(Object)result;
		}

		@SuppressWarnings("unchecked")
		public <TIn extends Query, TOut extends Query> QueryAdapterBuilder<TIn, TOut> get(Class<TOut> key1, Class<TIn> key2) {
			QueryAdapterBuilder<? extends Query, ? extends Query> result = this.get(key1).get(key2);

			/*
			 * Up–down cast to convert QAB<?, ?> to QAB<TIn, TOut> 
			 * (see http://stackoverflow.com/q/7502243/814206 and
			 * http://stackoverflow.com/a/7505867/814206
			 */
			return (QueryAdapterBuilder<TIn, TOut>)(Object)result;
		}
	}

	private final Table registedBuilders;
	
	public DynamicAdapter() {
		registedBuilders = new Table();
		
		registedBuilders.put(StringQuery.class, StringQuery.class,
				new IdentityAdapter<StringQuery>());
		registedBuilders.put(ParseTree.class, ParseTree.class,
				new IdentityAdapter<ParseTree>());
		registedBuilders.put(LuceneObject.class, LuceneObject.class,
				new IdentityAdapter<LuceneObject>());
		
		registedBuilders.put(LuceneObject.class, ParseTree.class,
				new ParseTreeToObjectAdapter.Builder());
		registedBuilders.put(ParseTree.class, StringQuery.class, 
				new StringToParseTreeAdapter.Builder());

		registedBuilders.put(LuceneObject.class, StringQuery.class, 
				new ChainedAdapter<>(
					new StringToParseTreeAdapter.Builder(),
					new ParseTreeToObjectAdapter.Builder()));
	}

	public <TIn extends Query, TOut extends Query> 
			QueryAdapterBuilder<TIn, TOut>
			register(Class<TOut> toType, Class<TIn> fromType, QueryAdapterBuilder<TIn, TOut> newValue_) {
		return this.registedBuilders.put(toType, fromType, newValue_);
	}

	public <TAdapted extends Query> TAdapted adapt(
			final Class<TAdapted> toType, final Query adapted) {
		return adapted.accept(new Query.Visitor<TAdapted>() {
			@Override
			public TAdapted visit(LuceneObject element) {
				return registedBuilders.get(toType, LuceneObject.class).adapt(element);
			}

			@Override
			public TAdapted visit(StringQuery element) {
				return registedBuilders.get(toType, StringQuery.class).adapt(element);
			}

			@Override
			public TAdapted visit(ParseTree element) {
				return registedBuilders.get(toType, ParseTree.class).adapt(element);
			}
		});
	}

	public Query applyModifier(Query concept, final SemanticModifier modifier) {
		Class<? extends Query> conceptClass = concept.getClass();
		// TODO Check Why ClassMap does not match: answer enum and DualRepresentation query and not StringQuery, LuceneObject, or ParseTree
		ClassMap<Query, Set<Class<? extends Query>>> supportedConversions =
				new ClassMap<>(ClassMap.RetrievalStrategies.SUBCLASS, modifier.getSupportedTypes());
		if(supportedConversions.containsKey(conceptClass)) {
			Class<? extends Query> inputType = supportedConversions.getMatchingStoredKey(conceptClass);
			Class<? extends Query> outputType;
			ClassMap<Query, Void> targetTypes = ClassMap.createClassToVoidMap(
					ClassMap.RetrievalStrategies.SUPERCLASS, supportedConversions.get(conceptClass));
			
			if(targetTypes.containsKey(concept.getClass())) {
				outputType = targetTypes.getMatchingStoredKey(conceptClass);
			} else {
				if(targetTypes.isEmpty()) {
					throw new IllegalStateException(String.format(
							"Modifier supported conversions structure is not well formed: %s exist as key but is mapped to an empty set.",
							inputType.getName()));
				}
				outputType = targetTypes.keySet().iterator().next();
			}
			QueryAdapterBuilder<? extends Query, ? extends Query> modifierAdapter = 
					modifier.getAdapterBuilder(inputType, outputType);
			/*
			 * Use capture helper method, see http://stackoverflow.com/a/17302484/814206
			 */
			return applyModifier(inputType, outputType, modifier, concept);
			
		} else {
			throw new UnsupportedOperationException(String.format(
					"Not yet implemented concept class %s, modifier supports: %s",
					concept.getClass().getName(), modifier.getSupportedTypes().toString()));
		}
	}

	private static <TIn extends Query, TOut extends Query> TOut applyModifier(
			Class<TIn> inputType, Class<TOut> outputType,
			SemanticModifier modifier, Query argument) {
		TIn castArgument = inputType.cast(argument);
		QueryAdapterBuilder<TIn, TOut> adapterBuilder =
				modifier.getAdapterBuilder(inputType, outputType);
		return adapterBuilder.adapt(castArgument);
	}
}
