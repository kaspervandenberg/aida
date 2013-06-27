// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.util;

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
public class ClassMap<TKeyClass, TValue> extends HashMap<Class<? extends TKeyClass>, TValue> {

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

	public static <TKey> ClassMap<TKey, Void> createClassToVoidMap(RetrievalStrategies strategy_,
			Set<? extends Class<? extends TKey>> items_) {
		HashMap<Class<? extends TKey>, Void> itemMap = new HashMap<>(items_.size());
		for (Class<? extends TKey> element : items_) {
			itemMap.put(element, null);
		}
		return new ClassMap<>(strategy_, itemMap);
	}

	public static <TKey> Set<Class<? extends TKey>> createClassSet(
			RetrievalStrategies strategy_,
			Set<? extends Class<? extends TKey>> items_) {
		return createClassToVoidMap(strategy_, items_).keySet();
	}

	@Override
	public boolean containsKey(Object o_request) {
		if (!(o_request instanceof Class)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		Class<? extends TKeyClass> request = (Class) o_request;
		return containsKey(request);
	}

	public boolean containsKey(Class<? extends TKeyClass> request) {
		Class<? extends TKeyClass> storedKey = lookup(request);
		if(storedKey != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean containsKeyStrict(Class<? extends TKeyClass> key) {
		return super.containsKey(key);
	}

	public Class<? extends TKeyClass> getMatchingStoredKey(Class<? extends TKeyClass> request) {
		Class<? extends TKeyClass> result = lookup(request);
		if(result != null) {
			return result;
		} else {
			throw new NoSuchElementException(String.format(
					"%s not stored (or any of its parents) in this map",
					request.getName()));
		}
	}

	@Override
	public TValue get(Object o_request) {
		if (!(o_request instanceof Class)) {
			throw new NoSuchElementException(String.format("No elements of type %s stored.", o_request.getClass().getName()));
		}
		@SuppressWarnings("unchecked")
		Class<? extends TKeyClass> request = (Class) o_request;
		return get(request);
	}

	public TValue get(Class<? extends TKeyClass> request) {
		return getStrict(getMatchingStoredKey(request));
	}

	public TValue getStrict(Class<? extends TKeyClass> key) {
		return super.get(key);
	}
	
	private Class<? extends TKeyClass> lookup(Class<? extends TKeyClass> request) {
		for (Class<? extends TKeyClass> stored : this.keySet()) {
			if (strategy.matches(request, stored)) {
				return stored;
			}
		}
		return null;
	}
}
