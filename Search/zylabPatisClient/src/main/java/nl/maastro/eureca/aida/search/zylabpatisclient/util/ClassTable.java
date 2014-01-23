// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.util;

import checkers.nullness.quals.EnsuresNonNullIf;
import checkers.nullness.quals.Nullable;
import dataflow.quals.Pure;
import java.util.Map;

/**
 * A {@link ClassMap} containing {@link ClassMap}s.
 * 
 * This is useful for defining conversions.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
@SuppressWarnings("serial")
public class ClassTable<TOuterKeyClass, TInnerKeyClass, TValue>
		extends ClassMap<TOuterKeyClass, ClassMap<TInnerKeyClass, TValue>> {

	private final ClassMap.RetrievalStrategies inner_retrieval_strategy;
	
	
	/**
	 * Create an empty {@code ClassTable} that uses {@code outer_strategy} to
	 * lookup the first class and {@code inner_strategy} on the second class.
	 * 
	 * @param outer_strategy	matching strategy used on outer request 
	 * 			arguments to stored classes.
	 * @param inner_strategy 	matching strategy used on innner request
	 * 			arguments to stored classes.
	 */
	public ClassTable(
			ClassMap.RetrievalStrategies outer_strategy,
			ClassMap.RetrievalStrategies inner_strategy)
	{
		super(outer_strategy);
		inner_retrieval_strategy = inner_strategy;
	}


	/**
	 * Convert and copy {@code m} to a {@code ClassTable}.
	 * 
	 * @param outer_strategy	matching strategy used on outer request 
	 * 			arguments to stored classes.
	 * @param inner_strategy 	matching strategy used on innner request
	 * 			arguments to stored classes.
	 * @param m		{@link Map} of {@link Map} to copy the contents from. 
	 */
	public ClassTable(
			ClassMap.RetrievalStrategies outer_strategy,
			ClassMap.RetrievalStrategies inner_strategy,
			Map<? extends Class<? extends TOuterKeyClass>,
					Map<? extends Class<? extends TInnerKeyClass>, ? extends TValue>> m)
	{
		super(outer_strategy);
		inner_retrieval_strategy = inner_strategy;
		
		for (Map.Entry<? extends Class<? extends TOuterKeyClass>,
				Map<? extends Class<? extends TInnerKeyClass>, ? extends TValue>> entry : m.entrySet()) {
			ClassMap<TInnerKeyClass, TValue> inner = new ClassMap<>(
					inner_retrieval_strategy,
					entry.getValue());
			super.put(entry.getKey(), inner);
		}
	}


	/**
	 * Create a {@link ClassTable} use to lookup conversions starting with
	 * the input class (the outer map) and the resulting class as second
	 * argument (inner maps).
	 * 
	 * For the source any stored class that is a superclass of the argument
	 * class in {@link #get} will match (i.e. {@code stored.}{@link 
	 * Class#isAssignableFrom(java.lang.Class) 
	 * isAssignableFrom}{@code (get_argument)}).
	 * For the target any stored class that is a subclass of the argument
	 * class in {@code get} will match (i.e. {@code 
	 * get_argument.isAssignableFrom(stored)}).
	 * 
	 * @param <TSourceClass>		the type of the classes used as source
	 * @param <TTargetClass>		the type of the classes used as target
	 * @param <TValue>				the type stored in this table.
	 * 
	 * @return	an empty {@link ClassTable} 
	 */
	public static <TSourceClass, TTargetClass, TValue>
			ClassTable<TSourceClass, TTargetClass, TValue>
			create_source_to_target()
	{
		return new ClassTable<>(
				RetrievalStrategies.SUBCLASS,		// RetrievalStrategies terminology is reversed from this: given stored, match SUBCLASS requests
				RetrievalStrategies.SUPERCLASS);		//  ""
	}
	

	/**
	 * Create a {@link ClassTable} use to lookup conversions starting with
	 * the input class (the outer map) and the resulting class as second
	 * argument (inner maps).
	 * 
	 * For the source any stored class that is a superclass of the argument
	 * class in {@link #get} will match (i.e. {@code stored.}{@link 
	 * Class#isAssignableFrom(java.lang.Class) 
	 * isAssignableFrom}{@code (get_argument)}).
	 * For the target any stored class that is a subclass of the argument
	 * class in {@code get} will match (i.e. {@code 
	 * get_argument.isAssignableFrom(stored)}).
	 * 
	 * @param <TSourceClass>	the type of the classes used as source
	 * @param <TTargetClass>	the type of the classes used as target
	 * @param <TValue>			the type stored in this table.
	 * @param m					{@link Map} of {@link Map} to copy the 
	 * 							contents from. 
	 * 
	 * @return	{@link ClassTable} filled with (a copy of) {@code m}.
	 */
	public static <TSourceClass, TTargetClass, TValue>
			ClassTable<TSourceClass, TTargetClass, TValue>
			create_source_to_target(
				Map<? extends Class<? extends TSourceClass>,
					Map<? extends Class<? extends TTargetClass>, ? extends TValue>> m)
	{
		return new ClassTable<>(
				RetrievalStrategies.SUBCLASS,		// RetrievalStrategies terminology is reversed from this: given stored, match SUBCLASS requests
				RetrievalStrategies.SUPERCLASS,		//  ""
				m);
	}
	

	/**
	 * Create a {@link ClassTable} use to lookup conversions starting with
	 * the resulting class (the outer map) and the source class as second
	 * argument (inner maps).
	 * 
	 * For the target any stored class that is a subclass of the argument
	 * class in {@code get} will match (i.e. {@code 
	 * get_argument.isAssignableFrom(stored)}).
	 * For the source any stored class that is a superclass of the argument
	 * class in {@link #get} will match (i.e. {@code stored.}{@link 
	 * Class#isAssignableFrom(java.lang.Class) 
	 * isAssignableFrom}{@code (get_argument)}).
	 * 
	 * NOTE: arguments {@code <TTargetClass>} and {@code <TSourceClass>}
	 * 		are reversed compared to {@link #create_source_to_target}.
	 * 
	 * @param <TTargetClass>		the type of the classes used as target
	 * @param <TSourceClass>		the type of the classes used as source
	 * @param <TValue>				the type stored in this table.
	 * 
	 * @return	an empty {@link ClassTable} 
	 */
	public static <TTargetClass, TSourceClass, TValue>
			ClassTable<TTargetClass, TSourceClass, TValue>
			create_target_to_source()
	{
		return new ClassTable<>(
				RetrievalStrategies.SUPERCLASS,		// RetrievalStrategies terminology is reversed from this: given stored, match SUPERCLASS requests
				RetrievalStrategies.SUBCLASS);		//  ""
	}
	

	/**
	 * Create a {@link ClassTable} use to lookup conversions starting with
	 * the resulting class (the outer map) and the source class as second
	 * argument (inner maps).
	 * 
	 * For the target any stored class that is a subclass of the argument
	 * class in {@code get} will match (i.e. {@code 
	 * get_argument.isAssignableFrom(stored)}).
	 * For the source any stored class that is a superclass of the argument
	 * class in {@link #get} will match (i.e. {@code stored.}{@link 
	 * Class#isAssignableFrom(java.lang.Class) 
	 * isAssignableFrom}{@code (get_argument)}).
	 * 
	 * NOTE: arguments {@code <TTargetClass>} and {@code <TSourceClass>}
	 * 		are reversed compared to {@link #create_source_to_target}.
	 * 
	 * @param <TTargetClass>	the type of the classes used as target
	 * @param <TSourceClass>	the type of the classes used as source
	 * @param <TValue>			the type stored in this table.
	 * @param m					{@link Map} of {@link Map} to copy the 
	 * 							contents from. 
	 * 
	 * @return	{@link ClassTable} filled with (a copy of) {@code m}.
	 */
	public static <TTargetClass, TSourceClass, TValue>
			ClassTable<TTargetClass, TSourceClass, TValue>
			create_target_to_source(
				Map<? extends Class<? extends TTargetClass>,
					Map<? extends Class<? extends TSourceClass>, ? extends TValue>> m)
	{
		return new ClassTable<>(
				RetrievalStrategies.SUPERCLASS,		// RetrievalStrategies terminology is reversed from this: given stored, match SUPERCLASS requests
				RetrievalStrategies.SUBCLASS,		//  ""
				m);
	}
	

	public @Nullable TValue put(
			Class<TOuterKeyClass> key1,
			Class<TInnerKeyClass> key2,
			TValue newValue_)
	{
		if (!this.containsKeyStrict(key1)) {
			this.put(key1, new ClassMap<TInnerKeyClass, TValue>(inner_retrieval_strategy));
		}
		@SuppressWarnings("nullness")
		TValue result = this.getStrict(key1).put(key2, newValue_);
		return result;
	}

	
	@Pure
	@SuppressWarnings("nullness")
	@EnsuresNonNullIf(expression={"get(#1, #2)", "get(#1)", "get(#1).get(#2)"}, result=true)
	public boolean containsKeyPair(
			Class<TOuterKeyClass> key1,
			Class<TInnerKeyClass> key2)
	{
		return containsKeyPair_impl(key1, key2);
	}


	@Pure
	private boolean containsKeyPair_impl(
			Class<TOuterKeyClass> key1,
			Class<TInnerKeyClass> key2)
	{
		if (this.containsKey(key1)) {
			return this.get(key1).containsKey(key2);
		} else {
			return false;
		}
	}
	
	
	@Pure
	public @Nullable TValue get(
			Class<TOuterKeyClass> key1,
			Class<TInnerKeyClass> key2)
	{
		if (this.containsKey(key1)) {
			return this.get(key1).get(key2);
		} else {
			return null;
		}
	}
}
