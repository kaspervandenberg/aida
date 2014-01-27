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
	private static class Query_Type_Determinator implements Query.Visitor<Class<? extends Query>> {
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
	
	
	private final Query unmodified_query;
	private final SemanticModifier modifier;
	private final ClassTable<Query, Query, Void> supported_adaptations;
	private final Class<? extends Query> original_type;
	

	public ModifierApplier(final SemanticModifier modifier_, final Query unmodified_query_)
	{
		this.unmodified_query = unmodified_query_;
		this.modifier = modifier_;
		this.supported_adaptations = get_supported_adaptations(modifier);
		this.original_type = determine_original_type(unmodified_query);
	}

	
	public Query get_modified_query()
	{
		if (is_type_preserving_supported()) {
			return type_preserving_modify();
		} else {
			Class<? extends Query> target_type = select_arbitrary_supported_type();
			return type_changing_modify(target_type);
		}	
	}


	private static ClassTable<Query, Query, Void> get_supported_adaptations(final SemanticModifier modifier)
	{
		Map<Class<? extends Query>, Set<Class<? extends Query>>> map_supported = modifier.getSupportedTypes();
		
		@SuppressWarnings("unchecked")	// Up–down-cast to convert 'Class<…>' into '? extends Class<…>' 
										// should work and not break anything and is required
										// to call 'create_source_to_target_void_table' with 'map_supported'.
		Map<? extends Class<? extends Query>, Set<? extends Class<? extends Query>>> generic_map_supported = 
				(Map<? extends Class<? extends Query>, Set<? extends Class<? extends Query>>>)(Object)map_supported;
		
		return ClassTable.<Query, Query>create_source_to_target_void_table(generic_map_supported);
	}

	@EnsuresNonNull("original_type")
	private static Class<? extends Query> determine_original_type(final Query unmodified_query) {
		Query_Type_Determinator determinator = new Query_Type_Determinator();
		return unmodified_query.accept(determinator);
	}


	private boolean is_type_preserving_supported()
	{
		return supported_adaptations.containsKeyPair(original_type, original_type);
	}
	
	
	private Class<? extends Query> select_arbitrary_supported_type()
	{
		if(!supported_adaptations.containsKey(original_type)) {
			throw new IllegalStateException(String.format(
					"Modifier, %s, cannot modify queries of type %s.",
					modifier, original_type));
		}
		
		@NonNull ClassMap<Query, ?> targets_map = supported_adaptations.get(original_type);
		@SuppressWarnings("nullness")	// TODO Unsure why the nullness checker gives a warning here
		Set<? extends Class<? extends Query>> supported_targets = targets_map.keySet();
		if (supported_targets.isEmpty()) {
			throw new IllegalStateException(String.format(
					"Supported types of modifier, %s, is not well formed: it contains an empty set of targets for %s.",
					modifier, original_type));
		}

		Class <? extends Query> first_target_type = supported_targets.iterator().next();
		return first_target_type;
	}

	private Query type_preserving_modify()
	{
		return apply_modifier(original_type, original_type);
	}


	private Query type_changing_modify (Class<? extends Query> target_type)
	{
		return apply_modifier(original_type, target_type);
	}


	private Query apply_modifier(
			Class<? extends Query> input_type,
			Class<? extends Query> result_type)
	{
		Class<? extends Query> stored_input_type = supported_adaptations.getMatchingStoredKey(input_type);
		Class<? extends Query> stored_result_type = supported_adaptations.getMatchingStoredKey(input_type, result_type);
		return apply_stored_modifier(stored_input_type, stored_result_type);
	}

	
	private <T_Original extends Query, T_Target extends Query>
	T_Target apply_stored_modifier (
			Class<T_Original> input_type,
			Class<T_Target> result_type)
	{
		QueryAdapterBuilder<T_Original, T_Target> modifying_adapter =
				modifier.getAdapterBuilder(input_type, result_type);
		T_Original typed_query = input_type.cast(unmodified_query);
		T_Target result = modifying_adapter.adapt(typed_query);

		return result;
	}
}
