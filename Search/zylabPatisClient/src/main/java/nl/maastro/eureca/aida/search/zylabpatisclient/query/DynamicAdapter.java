package nl.maastro.eureca.aida.search.zylabpatisclient.query;

import checkers.nullness.quals.NonNull;
import checkers.nullness.quals.Nullable;
import java.util.NoSuchElementException;
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
	private class Table extends ClassMap</*@NonNull*/Query, ClassMap</*@NonNull*/Query,  
				QueryAdapterBuilder<? extends /*@NonNull*/Query, ? extends /*@NonNull*/Query>>> {

		public Table() {
			super(RetrievalStrategies.SUPERCLASS);
		}

		@Nullable
		public <TIn extends /*@NonNull*/Query, TOut extends /*@NonNull*/Query> 
				QueryAdapterBuilder<? extends /*@NonNull*/Query, ? extends /*@NonNull*/Query> put(
					Class<TOut> key1,
					Class<TIn> key2,
					QueryAdapterBuilder<TIn, TOut> newValue_) {
			if (!super.containsKey(key1)) {
				this.put(key1, new ClassMap<Query, QueryAdapterBuilder<? extends Query, ? extends Query>>(RetrievalStrategies.SUBCLASS));
			}
			@SuppressWarnings("nullness")
			@NonNull ClassMap<Query, QueryAdapterBuilder<? extends Query, ? extends Query>> innerMap = this.getStrict(key1);
			@Nullable QueryAdapterBuilder<? extends Query, ? extends Query> previous = innerMap.put(key2, newValue_);
			
			/*
			 * Up–down cast to convert QAB<?, ?> to QAB<TIn, TOut> 
			 * (see http://stackoverflow.com/q/7502243/814206 and
			 * http://stackoverflow.com/a/7505867/814206
			 */
			return previous;
		}
		
		@SuppressWarnings("unchecked")
		public <TIn extends Query, TOut extends Query> QueryAdapterBuilder<TIn, TOut> get(Class<TOut> key1, Class<TIn> key2) {
			ClassMap</*@NonNull*/Query, 
					QueryAdapterBuilder<? extends /*@NonNull*/Query, ? extends /*@NonNull*/Query>> innerMap = getStrict(key1);

			if (innerMap != null) {
				QueryAdapterBuilder<? extends /*@NonNull*/Query, ? extends /*@NonNull*/Query> result = innerMap.get(key2);

				if(result != null) {
					/*
					 * Up–down cast to convert QAB<?, ?> to QAB<TIn, TOut> 
					 * (see http://stackoverflow.com/q/7502243/814206 and
					 * http://stackoverflow.com/a/7505867/814206
					 */
					return (QueryAdapterBuilder<TIn, TOut>)(Object)result;
				}
			}

			throw new NoSuchElementException(String.format("No adapter for (%s, %s) stored.",
					key1, key2));
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


	@Nullable
	public <TIn extends Query, TOut extends Query> 
			QueryAdapterBuilder<? extends Query, ? extends Query>
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
		ModifierApplier applier = new ModifierApplier(modifier, concept);
		return applier.getModifiedQuery();
	}
}
