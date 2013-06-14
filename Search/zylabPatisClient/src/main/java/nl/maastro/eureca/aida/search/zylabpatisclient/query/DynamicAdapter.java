package nl.maastro.eureca.aida.search.zylabpatisclient.query;

import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * Adapt any {@link Query} ({@link StringQuery}, {@link ParseTree}, or 
 * {@link LuceneObject} to any other type of {@Query}.
 * 
 * @author Kasper vanden Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DynamicAdapter {

	private static class ClassMap<TValue> extends HashMap<Class<?>, TValue> {
		public enum RetrievalStrategies {
			SUBCLASS {
				@Override
				public boolean matches(Class<?> request, Class<?> stored) {
					return stored.isAssignableFrom(request);
				} },
			
			SUPERCLASS {
				@Override
				public boolean matches(Class<?> request, Class<?> stored) {
					return request.isAssignableFrom(stored);
				} };

			public abstract boolean matches(Class<?> request, Class<?> stored);
		}
		
		private final RetrievalStrategies strategy;

		public ClassMap(RetrievalStrategies strategy_) {
			this.strategy = strategy_;
		}
		
		@Override
		public TValue get(Object o_request) {
			if(!(o_request instanceof Class)) {
				throw new NoSuchElementException(
						String.format("No elements of type %s stored.",
						o_request.getClass().getName()));
			}
			Class<?> request = (Class)o_request;
			
			for (Class<?> stored : this.keySet()) {
				if(strategy.matches(request, stored)) {
					return getStrict(stored);
				}
			}

			throw new NoSuchElementException(String.format(
					"%s not stored (or any of its parents) in this map",
					request.getName()));
		}

		public TValue getStrict(Object o_key) {
			if(!(o_key instanceof Class)) {
				throw new NoSuchElementException(
						String.format("No elements of type %s stored.",
						o_key.getClass().getName()));
			}
			Class<?> key = (Class)o_key;
			return super.get(key);
		}
	}
	
	private class Table extends ClassMap<ClassMap< 
				QueryAdapterBuilder<? extends Query, ? extends Query>>> {

		public Table() {
			super(RetrievalStrategies.SUPERCLASS);
		}
		
		public <TIn extends Query, TOut extends Query> 
				QueryAdapterBuilder<TIn, TOut> put(
					Class<TOut> key1,
					Class<TIn> key2,
					QueryAdapterBuilder<TIn, TOut> newValue_) {
			if (!this.containsKey(key1)) {
				this.put(key1, new ClassMap<QueryAdapterBuilder<? extends Query, ? extends Query>>(RetrievalStrategies.SUBCLASS));
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
