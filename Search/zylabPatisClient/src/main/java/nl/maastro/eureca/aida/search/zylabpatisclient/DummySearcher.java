// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.util.Map;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;

/**
 * A {@link Searcher} that produces dummy {@link SearchResult}s based on
 * the {@link PatisNumber} being in a map of results.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DummySearcher extends SearcherBase {
	/**
	 * Map of {@link PatisNumber} to boolean:
	 * <ul><li>{@code true}, the concept is expected to be found in the documents 
	 * 			→ the patient is not eligible; or</li>
	 * 		<li>{@code false}, the patient's documents should not match the 
	 * 			concept → the patient is eligiblie for clinical trials.</li></ul>
	 */
	private final Map<PatisNumber, EligibilityClassification> results;

	public DummySearcher(Map<PatisNumber, EligibilityClassification> results) {
		super("", 0);
		this.results = results;
	}

	@Override
	public SearchResult searchFor(final Query query, final Iterable<SemanticModifier> modifiers, final PatisNumber patient) {
		if (results.containsKey(patient) && results.get(patient) == EligibilityClassification.NOT_ELIGIBLE) {
			return SearchResultImpl.createDummyNotEliligle(patient);
		} else if (results.containsKey(patient) && results.get(patient) == EligibilityClassification.ELIGIBLE) {
			return SearchResultImpl.createDummyEligible(patient);
		} else {
			return SearchResultImpl.createDummyUnknown(patient);
		}
	}
	
}
