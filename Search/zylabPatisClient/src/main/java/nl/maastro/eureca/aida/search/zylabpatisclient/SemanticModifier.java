// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.DynamicAdapter;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.IdentityAdapter;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.LuceneObject;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.ParseTree;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.QueryAdapterBuilder;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.StringQuery;

/**
 * Represents qualifiers that modify a concept.  For example the Dutch word 
 * "geen", meaning none, modifies the concept metastasis; when the lexical
 * pattern "metastase" is found the patient is excluded from clinical trials,
 * but when "geen" is found near "metastase" the patient might be eligible
 * for a clinical trial.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface SemanticModifier {
	/**
	 * Return a builder that applies this {@code SemanticModifier} to 
	 * {@code apdated}.  The builder's
	 * {@link QueryAdapterBuilder#adapt(nl.maastro.eureca.aida.search.zylabpatisclient.query.Query) }-method
	 * returns a {@link Query} for the semantically modified  concept
	 * (i.e. {@code adapted}).
	 * 
	 * @param <TIn>	the type of {@link Query} that the requested adapter must 
	 * 		accepts as type of input parameter in its 
	 * 		{@link QueryAdapterBuilder#adapt(nl.maastro.eureca.aida.search.zylabpatisclient.query.Query) }-method.
	 * 
	 * @param <TOut>	the type of {@link Query} return that the requested
	 * 		adapter's {@link QueryAdapterBuilder#adapt(nl.maastro.eureca.aida.search.zylabpatisclient.query.Query) }
	 * 		must return.
	 * @param inClass	the type of {@link Query} that the requested adapter must 
	 * 		accepts as type of input parameter in its 
	 * 		{@link QueryAdapterBuilder#adapt(nl.maastro.eureca.aida.search.zylabpatisclient.query.Query) }-method.
	 * 		Equivalent to the generic parameter {@code <TIn>}.
	 * @param outClass	the type of {@link Query} return that the requested
	 * 		adapter's {@link QueryAdapterBuilder#adapt(nl.maastro.eureca.aida.search.zylabpatisclient.query.Query) }
	 * 		must return.  Equivalent to the generic parameter {@coe <TOut>}.
	 * 
	 * @return	a {@link QueryAdapterBuilder}{@code <Tin, TOut>} specialisation 
	 * 		of the generic {@code QueryAdapterBuilder}.  Each call to the
	 * 		adapterbuilder's {@code adapt}-method will return a wrapper that
	 * 		semantically modifies {@code adapted}. 
	 * 
	 * @throws IllegalArgumentException		when the {@code SemanticModifier}
	 * 		cannot convert queries of type {@code <TIn>} to {@code <TOut>};
	 * 		use {@link #getSupportedTypes()} to retieve when the 
	 * 		{@code SemanticModifier} will not throw {@code IllegalArgumentException}.
	 */
	public <TIn extends Query, TOut extends Query> QueryAdapterBuilder<TIn, TOut>
			getAdapterBuilder(Class<TIn> inClass, Class<TOut> outClass)
			throws IllegalArgumentException; 
	
	/**
	 * Retrieve which conversions the {@code SemanticModifier} supports.  When
	 * <em>inClass</em>–<em>outClass</em> is in the returned map,
	 * {@link #getAdapterBuilder(java.lang.Class, java.lang.Class) 
	 * getAdapterBuilder(inClass, outClass)} will not throw an
	 * {@link IllegalArgumentException}. 
	 * 
	 * When a {@code SemanticModifier} does not support a given conversion, use a
	 * {@link DynamicAdapter} to convert its in- and output to the required types.
	 * 
	 * @return	a map of {@code inClass} to a set of {@code outClass} containing
	 * 		the conversions that this {@code SemanticModifier} can perform.
	 */
	public Map<Class<? extends Query>, Set<Class<? extends Query>>> getSupportedTypes();

	/**
	 * Retrieve the {@link Classification} that patients or documents receive 
	 * when documents a concept modified with this {@code SemanticModifier}
	 * matches a document.
	 */
	public ConceptFoundStatus getClassification();
	
	public enum Constants implements SemanticModifier {
		/**
		 * The {@code NULL_MODIFIER} indicates that a concept is not modified.
		 */
		NULL_MODIFIER(ConceptFoundStatus.FOUND),

		/**
		 * The {@code UNKNOWN_MODIFIER} indicates that the {@link SemanticModifier}
		 * for the {@code SearchResult} was not specified.
		 */
		UNKNOWN_MODIFIER(ConceptFoundStatus.FOUND_CONCEPT_UNKNOWN);

		private static final Map<Class<? extends Query>, Set<Class<? extends Query>>>
				supportedConversions; 
		static {
			Map<Class<? extends Query>, Set<Class<? extends Query>>> tmp = 
					new HashMap<>(3);
	
			tmp.put(StringQuery.class, Collections.<Class<? extends Query>>singleton(StringQuery.class));
			tmp.put(ParseTree.class, Collections.<Class<? extends Query>>singleton(ParseTree.class));
			tmp.put(LuceneObject.class, Collections.<Class<? extends Query>>singleton(LuceneObject.class));
			
			supportedConversions = Collections.unmodifiableMap(tmp);
		}

		private final ConceptFoundStatus classification;
		
		private Constants(final ConceptFoundStatus classification_) {
			this.classification = classification_;
		}
			
		@Override
		@SuppressWarnings("unchecked")
		public <TIn extends Query, TOut extends Query> QueryAdapterBuilder<TIn, TOut>
				getAdapterBuilder(Class<TIn> inClass, Class<TOut> outClass)
				throws IllegalArgumentException {
			if(outClass.isAssignableFrom(inClass)) {
				return (QueryAdapterBuilder<TIn, TOut>) new IdentityAdapter<TIn>();
			}
			throw new IllegalArgumentException(
					"NULL MODIFIER does not support query type conversion.");
		}

		@Override
		public Map<Class<? extends Query>, Set<Class<? extends Query>>> getSupportedTypes() {
			return Collections.unmodifiableMap(supportedConversions);
		}

		@Override
		public ConceptFoundStatus getClassification() {
			return classification;
		}
		
	}
}
