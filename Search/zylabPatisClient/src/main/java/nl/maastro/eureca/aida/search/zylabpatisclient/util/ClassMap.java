// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.util;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

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

	

	@Override
	public TValue get(Object o_request) {
		if (!(o_request instanceof Class)) {
			throw new NoSuchElementException(String.format("No elements of type %s stored.", o_request.getClass().getName()));
		}
		Class<?> request = (Class) o_request;
		for (Class<?> stored : this.keySet()) {
			if (strategy.matches(request, stored)) {
				return getStrict(stored);
			}
		}
		throw new NoSuchElementException(String.format("%s not stored (or any of its parents) in this map", request.getName()));
	}

	public TValue getStrict(Object o_key) {
		if (!(o_key instanceof Class)) {
			throw new NoSuchElementException(String.format("No elements of type %s stored.", o_key.getClass().getName()));
		}
		Class<?> key = (Class) o_key;
		return super.get(key);
	}
	
}
