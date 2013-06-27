// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.classification;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.DocumentId;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.SemanticModifier;
import nl.maastro.eureca.aida.search.zylabpatisclient.Snippet;

/**
 * One {@link SemanticModifier} overrides the {@link EligibilityClassification}
 * of an other.  If both {@code SemanticModifier}'s 
 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.query.Query}s match,
 * filter the overriden query's results.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class Overrides implements Rule {
	private final SemanticModifier overrider;
	private final SemanticModifier overridden;
	
	public Overrides(final SemanticModifier overrider_,
			final SemanticModifier overridden_) {
		this.overrider = overrider_;
		this.overridden = overridden_;
	} 

	/**
	 * @return <ul><li>{@code true}, {@code searchResult} contains at least
	 * 			one document that matches both {@link #overridden} and
	 * 			{@link #overrider}, therefore this rule can be applied; or</li>
	 * 		<li>{@code false}, {@code searchResult} does not contain any
	 * 			document that matches both {@link #overridden} and
	 * 			{@link #overrider}, therefore applying this rule will fail.</li>
	 * 		</ul>
	 */
	@Override
	public boolean isApplicable(SearchResult searchResult) {
		for (DocumentId docId : searchResult.getMatchingDocuments()) {
			if(isApplicable(searchResult, docId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Remove {@link #overridden} from the {@link SearchResult}s for each 
	 * document that matches both {@link #overrider} and {@code overridden}.
	 * 
	 * @param searchResult	the {@link SearchResult} to filter
	 * @return	a fresh {@link SearchResult} that does not contain 
	 * 		{@link nl.maastro.eureca.aida.search.zylabpatisclient.Snippet}s for
	 * 		{@link #overridden}.
	 * 
	 * @throws nl.maastro.eureca.aida.search.zylabpatisclient.classification.Rule.Inapplicable 
	 * 		when this rule is not applicable
	 */
	@Override
	public SearchResult apply(SearchResult searchResult) throws Inapplicable {
		if(!isApplicable(searchResult)) {
			throw new Rule.Inapplicable(String.format(
					"SearchResult %s contains no document matching %s and %s.",
					searchResult.toString(), overrider.toString(), overridden.toString()));
		}
		
		Map<DocumentId, Map<SemanticModifier, Set<Snippet>>> filteredResults =
				new HashMap<>(searchResult.getMatchingDocuments().size());
		for (DocumentId docId : searchResult.getMatchingDocuments()) {
			Set<SemanticModifier> perDocModifiers = searchResult.getModifiers(docId);
			Map<SemanticModifier, Set<Snippet>> filteredPerDocSnippets =
					new HashMap<>(perDocModifiers.size());
			for (SemanticModifier semanticModifier : perDocModifiers) {
				filteredPerDocSnippets.put(semanticModifier,
						searchResult.getSnippets(docId, semanticModifier));
			}
			if (isApplicable(searchResult, docId)) {
				filteredPerDocSnippets.remove(overridden);
			}
			filteredResults.put(docId, filteredPerDocSnippets);
		}

		return SearchResult.create(searchResult.patient, searchResult.nHits,
				filteredResults);
	}

	/**
	 * The rule can be applied to {@code docId} in {@code searchResult}. 
	 */
	private boolean isApplicable(SearchResult searchResult, DocumentId docId) {
		Set<SemanticModifier> perDocModifiers = searchResult.getModifiers(docId);
		return (perDocModifiers.contains(overrider) &&
				perDocModifiers.contains(overridden));
	}
	
}
