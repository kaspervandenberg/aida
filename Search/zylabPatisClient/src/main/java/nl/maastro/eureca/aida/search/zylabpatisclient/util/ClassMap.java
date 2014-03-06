// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.util;

import checkers.nullness.quals.EnsuresNonNullIf;
import checkers.nullness.quals.Nullable;
/*>>> import checkers.nullness.quals.NonNull; */
/*>>> import checkers.nullness.quals.KeyFor; */
import dataflow.quals.Pure;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Allows searching using the sub- and superclass- relation between 
 * {@link Class}-object to find the {@code <TValue>}-object the matches.
 * 
 * <p><em>NOTE: Contrary to most maps, the {@code ClassMap}'s 
 * {@link #get(java.lang.Object)}-method is not efficient; {@code get(…)} 
 * executes in O(n), all classes are searched until one that matches is found.
 * </em></p>
 * 
 * <p><em>NOTE: {@code ClassMap} returns a value of an arbitrary matching class
 * key.  It does not return the most specific match nor the matching element
 * that was stored first or most recent.</em></p>
 * 
 * TODO Store the entries hierarchically following the class hierarchy.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenbewrg.net>
 */
@SuppressWarnings("serial")
public class ClassMap<TKeyClass extends /*@NonNull*/Object, TValue> extends HashMap<Class<? extends TKeyClass>, TValue> {

	/**
	 * Strategies that define which arguments to {@link #get(java.lang.Object)}
	 * match a stored key.
	 */
	public enum RetrievalStrategies {
		/**
		 * Matches when {@code request} is a subclass of the {@code stored} key.
		 */
		SUBCLASS {
			@Override
			public boolean matches(Class<?> request, Class<?> stored) {
				return stored.isAssignableFrom(request);
			}
		},
		/**
		 * Matches when {@code request} is a superclass of the {@code stored} key.
		 */
		SUPERCLASS {
			@Override
			public boolean matches(Class<?> request, Class<?> stored) {
				return request.isAssignableFrom(stored);
			}
		};

		public abstract boolean matches(Class<?> request, Class<?> stored);
	}
	private final RetrievalStrategies strategy;

	/**
	 * Create a {@code ClassMap} that uses {@code strategy} to match requests to
	 * stored keys.  In general, use {@link RetrievalStrategies#SUBCLASS}
	 * when the {@code ClassMap} contains objects that use the key class as a
	 * parameter to one of more methods; this allows calling the method(s) of
	 * the stored value with object of the request key type as parameter.
	 * And, use {@link RetrievalStrategies#SUPERCLASS} when the {@code ClassMap}
	 * contains objects that use key class as return type for one or more 
	 * methods; this allows calling the methods of the stored object and 
	 * assigning the results to objects of the request key type.
	 * 
	 * @param strategy_	the matching strategy to use for matching requested
	 * 		types to stored key types
	 */
	public ClassMap(RetrievalStrategies strategy_) {
		this.strategy = strategy_;
	}

	public ClassMap(RetrievalStrategies strategy_, 
			Map<? extends Class<? extends TKeyClass>, ? extends TValue> m) {
		super(m);
		this.strategy = strategy_;
	}

	/**
	 * Create a {@code Class}–{@code Void}
	 * @param <TKey>
	 * @param strategy_
	 * @param items_
	 * @return 
	 */
	@Pure
	public static <TKey extends /*@NonNull*/Object> ClassMap<TKey, Void> createClassToVoidMap(RetrievalStrategies strategy_,
			Set<? extends Class<? extends TKey>> items_) {
		HashMap<Class<? extends TKey>, Void> itemMap = new HashMap<>(items_.size());
		for (Class<? extends TKey> element : items_) {
			itemMap.put(element, null);
		}
		return new ClassMap<>(strategy_, itemMap);
	}

	@Pure
	public static <TKey extends /*@NonNull*/Object> Set<? extends Class<? extends TKey>> createClassSet(
			RetrievalStrategies strategy_,
			Set<? extends Class<? extends TKey>> items_)
	{
		ClassMap<TKey, Void> classMapWithoutValues = createClassToVoidMap(strategy_, items_);
		Set</*@KeyFor("classMapWithoutValues")*/Class<? extends TKey>> result = classMapWithoutValues.keySet();
		return result;
	}

	@Pure
	@Override
	@SuppressWarnings("nullness")
	@EnsuresNonNullIf(expression={"get(#1)", "getMatchingStoredKey(#1)", "lookup(#1)"}, result=true)
	public boolean containsKey(final @Nullable Object obj_request)
	{
		return containsKey_impl(obj_request);
	}
	
	
	@Pure
	private boolean containsKey_impl(final @Nullable Object obj_request)
	{
		if (!(obj_request instanceof Class)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		Class<? extends TKeyClass> request = (Class) obj_request;
		if (containsKey(request)) {
			return true;
		} else {
			return false;
		}
	}


	@Pure
	@SuppressWarnings("nullness")
	@EnsuresNonNullIf(expression = {"get(#1)", "getMatchingStoredKey(#1)", "lookup(#1)"}, result = true)
	public boolean containsKey(final @Nullable Class<? extends TKeyClass> request) {
		return containsKey_impl(request);
	}


	private boolean containsKey_impl(final @Nullable Class<? extends TKeyClass> request) {
		if (request != null) {
			Class<? extends TKeyClass> storedKey = lookup(request);
			if(storedKey != null) {
				return true;
			}
		}
		return false;
	}

	@Pure
	@SuppressWarnings("nullness")
	@EnsuresNonNullIf(expression = {"get(#1)", "getMatchingStoredKey(#1)", "getStrict(#1)", "lookup(#1)"}, result = true)
	public boolean containsKeyStrict(final @Nullable Class<? extends TKeyClass> key) {
		return super.containsKey(key);
	}

	@Pure
	public Class<? extends TKeyClass> getMatchingStoredKey(final Class<? extends TKeyClass> request) {
		Class<? extends TKeyClass> result = lookup(request);
		if(result != null) {
			return result;
		} else {
			throw new NoSuchElementException(String.format(
					"%s not stored in this map",
					request.getName()));
		}
	}

	@Pure
	@Override
	@SuppressWarnings("nullness")	// Kludge: checker framework requires 
				// signature of overriden HashMap.get to be
				//		<TValue extends @Nullable Object> @NonNull TValue get(…)
				// however,
				//		<TValue extends @NonNull Object> @Nullable TValue get(…)
				// describes ClassMap better: a map that does not contain null
				// values and with get(…)-methods that follow Map's semantics,
				// i.e. return null when requesting a value of a key that the
				// map does not contain.
	public @Nullable TValue get(final @Nullable Object o_request) {
		if (!(o_request instanceof Class)) {
			return (TValue) null;
		}
		@SuppressWarnings("unchecked")
		Class<? extends TKeyClass> request = (Class) o_request;
		return get(request);
	}

	@Pure
	public @Nullable TValue get(final @Nullable Class<? extends TKeyClass> request) {
		if (request == null) {
			return (TValue) null;
		}
		return getStrict(getMatchingStoredKey(request));
	}

	@Pure
	public @Nullable TValue getStrict(final @Nullable Class<? extends TKeyClass> key) {
		return super.get(key);
	}
	
	@Pure
	private @Nullable Class<? extends TKeyClass> lookup(final Class<? extends TKeyClass> request) {
		for (Class<? extends TKeyClass> stored : this.keySet()) {
			if (strategy.matches(request, stored)) {
				return stored;
			}
		}
		return null;
	}
}
