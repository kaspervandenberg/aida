// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.util;

import checkers.nullness.quals.EnsuresNonNullIf;
import checkers.nullness.quals.Nullable;
/*>>> import checkers.nullness.quals.NonNull;*/;
import dataflow.quals.Pure;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A {@link ClassMap} containing {@link ClassMap}s.
 * 
 * This is useful for defining conversions.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
@SuppressWarnings("serial")
public class ClassTable<TOuterKeyClass extends /*@NonNull*/ Object, TInnerKeyClass extends /*@NonNull*/Object, TValue>
		extends ClassMap<TOuterKeyClass, ClassMap<TInnerKeyClass, TValue>> {

	private final ClassMap.RetrievalStrategies innerRetrievalStrategy;
	
	
	/**
	 * Create an empty {@code ClassTable} that uses {@code outerStrategy} to
	 * lookup the first class and {@code innerStrategy} on the second class.
	 * 
	 * @param outerStrategy	matching strategy used on outer request 
	 * 			arguments to stored classes.
	 * @param innerStrategy 	matching strategy used on innner request
	 * 			arguments to stored classes.
	 */
	public ClassTable(
			ClassMap.RetrievalStrategies outerStrategy,
			ClassMap.RetrievalStrategies innerStrategy)
	{
		super(outerStrategy);
		innerRetrievalStrategy = innerStrategy;
	}


	/**
	 * Convert and copy {@code m} to a {@code ClassTable}.
	 * 
	 * @param outerStrategy	matching strategy used on outer request 
	 * 			arguments to stored classes.
	 * @param innerStrategy 	matching strategy used on innner request
	 * 			arguments to stored classes.
	 * @param m		{@link Map} of {@link Map} to copy the contents from. 
	 */
	public ClassTable(
			ClassMap.RetrievalStrategies outerStrategy,
			ClassMap.RetrievalStrategies innerStrategy,
			Map<? extends Class<? extends TOuterKeyClass>,
					Map<? extends Class<? extends TInnerKeyClass>, ? extends TValue>> m)
	{
		super(outerStrategy);
		innerRetrievalStrategy = innerStrategy;
		
		for (Map.Entry<? extends Class<? extends TOuterKeyClass>,
				Map<? extends Class<? extends TInnerKeyClass>, ? extends TValue>> entry : m.entrySet()) {
			ClassMap<TInnerKeyClass, TValue> inner = new ClassMap<>(
					innerRetrievalStrategy,
					entry.getValue());
			super.put(entry.getKey(), inner);
		}
	}


	private ClassTable(
			ClassMap.RetrievalStrategies outerStrategy,
			ClassMap.RetrievalStrategies innerStrategy,
			Map<? extends Class<? extends TOuterKeyClass>,
					Set<? extends Class<? extends TInnerKeyClass>>> m,
			Class<TValue> voidClass)
	{
		super(outerStrategy);

		if (!Void.TYPE.isAssignableFrom(voidClass)) {
			throw new Error("This constructor only works for ClassTable<?, ?, Void>.");
		}
		
		innerRetrievalStrategy = innerStrategy;
		
		for (Map.Entry<? extends Class<? extends TOuterKeyClass>, 
				Set<? extends Class<? extends TInnerKeyClass>>> entry : m.entrySet()) {
			ClassMap<TInnerKeyClass, Void> inner = createClassToVoidMap(
					innerRetrievalStrategy,
					entry.getValue());
			@SuppressWarnings("unchecked")
			ClassMap<TInnerKeyClass, TValue> innerTvalueMap = (ClassMap<TInnerKeyClass, TValue>)inner;
			super.put(entry.getKey(), innerTvalueMap);
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
	 * isAssignableFrom}{@code (getArgument)}).
	 * For the target any stored class that is a subclass of the argument
	 * class in {@code get} will match (i.e. {@code 
	 * getArgument.isAssignableFrom(stored)}).
	 * 
	 * @param <TSourceClass>		the type of the classes used as source
	 * @param <TTargetClass>		the type of the classes used as target
	 * @param <TValue>				the type stored in this table.
	 * 
	 * @return	an empty {@link ClassTable} 
	 */
	public static <TSourceClass, TTargetClass, TValue>
			ClassTable<TSourceClass, TTargetClass, TValue>
			createSourceToTarget()
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
	 * isAssignableFrom}{@code (getArgument)}).
	 * For the target any stored class that is a subclass of the argument
	 * class in {@code get} will match (i.e. {@code 
	 * getArgument.isAssignableFrom(stored)}).
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
	 * Create a {@link ClassTable} containing {@link Void} values, comparable 
	 * to a {@link Map} of {@link Set}s, the lookup starts with the
	 * input class (the outer map) and has the resulting class as second
	 * argument (inner maps).
	 * 
	 * Use operations such as {@link #containsKeyPair(java.lang.Class, 
	 * java.lang.Class)}, {@link #containsKey(java.lang.Class) }, and 
	 * {@link #getMatchingStoredKey(java.lang.Class)}.
	 * 
	 * @param <TSourceClass>	the type of the classes used as source
	 * @param <TTargetClass>	the type of the classes used as target
	 * @param <TValue>			the type stored in this table.
	 * @param m					{@link Map} of {@link Set} to copy the 
	 * 							contents from. 
	 * 
	 * @return	{@link ClassTable} filled with (a copy of) {@code m}.
	 */
	public static <TSourceClass, TTargetClass>
			ClassTable<TSourceClass, TTargetClass, Void>
			createSourceToTargetVoidTable(
				Map<? extends Class<? extends TSourceClass>,
						Set<? extends Class<? extends TTargetClass>>> m)
	{
		return new ClassTable<>(
				RetrievalStrategies.SUBCLASS,		// RetrievalStrategies terminology is reversed from this: given stored, match SUBCLASS requests
				RetrievalStrategies.SUPERCLASS,		//  ""
				m,
				Void.TYPE);
	}
	

	/**
	 * Create a {@link ClassTable} use to lookup conversions starting with
	 * the resulting class (the outer map) and the source class as second
	 * argument (inner maps).
	 * 
	 * For the target any stored class that is a subclass of the argument
	 * class in {@code get} will match (i.e. {@code 
	 * getArgument.isAssignableFrom(stored)}).
	 * For the source any stored class that is a superclass of the argument
	 * class in {@link #get} will match (i.e. {@code stored.}{@link 
	 * Class#isAssignableFrom(java.lang.Class) 
	 * isAssignableFrom}{@code (getArgument)}).
	 * 
	 * NOTE: arguments {@code <TTargetClass>} and {@code <TSourceClass>}
	 * 		are reversed compared to {@link #createSourceToTarget}.
	 * 
	 * @param <TTargetClass>		the type of the classes used as target
	 * @param <TSourceClass>		the type of the classes used as source
	 * @param <TValue>				the type stored in this table.
	 * 
	 * @return	an empty {@link ClassTable} 
	 */
	public static <TTargetClass, TSourceClass, TValue>
			ClassTable<TTargetClass, TSourceClass, TValue>
			createTargetToSource()
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
	 * getArgument.isAssignableFrom(stored)}).
	 * For the source any stored class that is a superclass of the argument
	 * class in {@link #get} will match (i.e. {@code stored.}{@link 
	 * Class#isAssignableFrom(java.lang.Class) 
	 * isAssignableFrom}{@code (getArgument)}).
	 * 
	 * NOTE: arguments {@code <TTargetClass>} and {@code <TSourceClass>}
	 * 		are reversed compared to {@link #createSourceToTarget}.
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
			createTargetToSource(
				Map<? extends Class<? extends TTargetClass>,
					Map<? extends Class<? extends TSourceClass>, ? extends TValue>> m)
	{
		return new ClassTable<>(
				RetrievalStrategies.SUPERCLASS,		// RetrievalStrategies terminology is reversed from this: given stored, match SUPERCLASS requests
				RetrievalStrategies.SUBCLASS,		//  ""
				m);
	}
	

	/**
	 * Create a {@link ClassTable} containing {@link Void} values, comparable 
	 * to a {@link Map} of {@link Set}s, the lookup starts with the
	 * input class (the outer map) and has the resulting class as second
	 * argument (inner maps).
	 * 
	 * Use operations such as {@link #containsKeyPair(java.lang.Class, 
	 * java.lang.Class)}, {@link #containsKey(java.lang.Class) }, and 
	 * {@link #getMatchingStoredKey(java.lang.Class)}.
	 * 
	 * @param <TSourceClass>	the type of the classes used as source
	 * @param <TTargetClass>	the type of the classes used as target
	 * @param <TValue>			the type stored in this table.
	 * @param m					{@link Map} of {@link Set} to copy the 
	 * 							contents from. 
	 * 
	 * @return	{@link ClassTable} filled with (a copy of) {@code m}.
	 */
	public static <TTargetClass, TSourceClass>
			ClassTable<TTargetClass, TSourceClass, Void>
			createTargetToSourceVoidTable(
				Map<? extends Class<? extends TTargetClass>,
						Set<? extends Class<? extends TSourceClass>>> m)
	{
		return new ClassTable<>(
				RetrievalStrategies.SUPERCLASS,		// RetrievalStrategies terminology is reversed from this: given stored, match SUPERCLASS requests
				RetrievalStrategies.SUBCLASS,		//  ""
				m,
				Void.TYPE);
	}
	

	public @Nullable TValue put(
			Class<TOuterKeyClass> key1,
			Class<TInnerKeyClass> key2,
			TValue newValue_)
	{
		if (!this.containsKeyStrict(key1)) {
			this.put(key1, new ClassMap<TInnerKeyClass, TValue>(innerRetrievalStrategy));
		}
		@SuppressWarnings("nullness")
		TValue result = this.getStrict(key1).put(key2, newValue_);
		return result;
	}

	
	@Pure
	@SuppressWarnings("nullness")
	@EnsuresNonNullIf(expression={"get(#1, #2)", "get(#1)", "get(#1).get(#2)"}, result=true)
	public boolean containsKeyPair(
			Class<? extends TOuterKeyClass> key1,
			Class<? extends TInnerKeyClass> key2)
	{
		return containsKeyPair_impl(key1, key2);
	}


	@Pure
	private boolean containsKeyPair_impl(
			Class<? extends TOuterKeyClass> key1,
			Class<? extends TInnerKeyClass> key2)
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


	
	/**
	 * 
	 * @param reqKey1		the outer class of the request
	 * @param reqKey2		the inner class of the request; the result is
	 * 		related to this class.
	 * 
	 * @return	the key stored for ({@code reqKey1}, {@code reqKey2}).  The 
	 * 		returned class is related to {@code reqKey2} via 
	 * 		{@code innerRetrievalStrategy}.
	 */
	@Pure
	public Class<? extends TInnerKeyClass> getMatchingStoredKey(
			final Class<? extends TOuterKeyClass> reqKey1,
			final Class<? extends TInnerKeyClass> reqKey2)
	{
		@Nullable ClassMap<TInnerKeyClass, TValue> inner_map = get(reqKey1);
		if (inner_map == null) {
			throw new NoSuchElementException(String.format(
					"(%s, %s) not stored in this map",
					reqKey1.getName(),
					reqKey2.getName()));
		}
		return inner_map.getMatchingStoredKey(reqKey2);
	}
	


}
