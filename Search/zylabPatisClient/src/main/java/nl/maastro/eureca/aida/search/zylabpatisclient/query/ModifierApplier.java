// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.query;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.NonNull;
/*>>> import checkers.nullness.quals.Nullable; */
import java.util.Map;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.SemanticModifier;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.ClassMap;
import nl.maastro.eureca.aida.search.zylabpatisclient.util.ClassTable;

/**
 * Applies a {@link SemanticModifier} to a {@link Query}.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ModifierApplier {
	private static class QueryTypeDeterminator implements Query.Visitor<Class<? extends Query>> {
		@Override
		public Class<? extends Query> visit(LuceneObject element) {
			return LuceneObject.class;
		}

		@Override
		public Class<? extends Query> visit(StringQuery element) {
			return  StringQuery.class;
		}

		@Override
		public Class<? extends Query> visit(ParseTree element) {
			return ParseTree.class;
		}
	};
	
	
	private final Query unmodifiedQuery;
	private final SemanticModifier modifier;
	private final ClassTable<Query, Query, Void> supportedAdaptations;
	private final Class<? extends Query> originalType;
	

	public ModifierApplier(final SemanticModifier modifier_, final Query unmodifiedQuery_)
	{
		this.unmodifiedQuery = unmodifiedQuery_;
		this.modifier = modifier_;
		this.supportedAdaptations = getSupportedAdaptations(modifier);
		this.originalType = determineOriginalType(unmodifiedQuery);
	}

	
	public Query getModifiedQuery()
	{
		if (isTypePreservingSupported()) {
			return typePreservingModify();
		} else {
			Class<? extends Query> targetType = selectArbitrarySupportedType();
			return typeChangingModify(targetType);
		}	
	}


	private static ClassTable<Query, Query, Void> getSupportedAdaptations(final SemanticModifier modifier)
	{
		Map<Class<? extends Query>, Set<Class<? extends Query>>> mapSupported = modifier.getSupportedTypes();
		
		@SuppressWarnings("unchecked")	// Up–down-cast to convert 'Class<…>' into '? extends Class<…>' 
										// should work and not break anything and is required
										// to call 'createSourceToTargetVoidTable' with 'mapSupported'.
		Map<? extends Class<? extends Query>, Set<? extends Class<? extends Query>>> genericMapSupported = 
				(Map<? extends Class<? extends Query>, Set<? extends Class<? extends Query>>>)(Object)mapSupported;
		
		return ClassTable.<Query, Query>createSourceToTargetVoidTable(genericMapSupported);
	}

	@EnsuresNonNull("originalType")
	private static Class<? extends Query> determineOriginalType(final Query unmodifiedQuery) {
		QueryTypeDeterminator determinator = new QueryTypeDeterminator();
		return unmodifiedQuery.accept(determinator);
	}


	private boolean isTypePreservingSupported()
	{
		return supportedAdaptations.containsKeyPair(originalType, originalType);
	}
	
	
	private Class<? extends Query> selectArbitrarySupportedType()
	{
		if(!supportedAdaptations.containsKey(originalType)) {
			throw new IllegalStateException(String.format(
					"Modifier, %s, cannot modify queries of type %s.",
					modifier, originalType));
		}
		
		@NonNull ClassMap<Query, ?> targetsMap = supportedAdaptations.get(originalType);
		@SuppressWarnings("nullness")	// TODO Unsure why the nullness checker gives a warning here
		Set<? extends Class<? extends Query>> supportedTargets = targetsMap.keySet();
		if (supportedTargets.isEmpty()) {
			throw new IllegalStateException(String.format(
					"Supported types of modifier, %s, is not well formed: it contains an empty set of targets for %s.",
					modifier, originalType));
		}

		Class <? extends Query> firstTargetType = supportedTargets.iterator().next();
		return firstTargetType;
	}

	private Query typePreservingModify()
	{
		return applyModifier(originalType, originalType);
	}


	private Query typeChangingModify (Class<? extends Query> targetType)
	{
		return applyModifier(originalType, targetType);
	}


	private Query applyModifier(
			Class<? extends Query> inputType,
			Class<? extends Query> resultType)
	{
		Class<? extends Query> storedInputType = supportedAdaptations.getMatchingStoredKey(inputType);
		Class<? extends Query> storedResultType = supportedAdaptations.getMatchingStoredKey(inputType, resultType);
		return applyStoredModifier(storedInputType, storedResultType);
	}

	
	private <T_Original extends Query, T_Target extends Query>
	T_Target applyStoredModifier (
			Class<T_Original> inputType,
			Class<T_Target> resultType)
	{
		QueryAdapterBuilder<T_Original, T_Target> modifyingAdapter =
				modifier.getAdapterBuilder(inputType, resultType);
		T_Original typedQuery = inputType.cast(unmodifiedQuery);
		T_Target result = modifyingAdapter.adapt(typedQuery);

		return result;
	}
}
