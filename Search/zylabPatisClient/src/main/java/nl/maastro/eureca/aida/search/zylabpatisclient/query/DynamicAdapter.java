package nl.maastro.eureca.aida.search.zylabpatisclient.query;

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
}
