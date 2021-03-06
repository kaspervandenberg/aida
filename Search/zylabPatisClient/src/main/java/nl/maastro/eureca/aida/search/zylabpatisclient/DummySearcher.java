// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.util.Map;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;
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
	private final Map<PatisNumber, ConceptFoundStatus> results;

	public DummySearcher(Map<PatisNumber, ConceptFoundStatus> results) {
		super("", 0);
		this.results = results;
	}

	@Override
	public SearchResult searchFor(final Query query, final Iterable<SemanticModifier> modifiers, final PatisNumber patient) {
		if (results.containsKey(patient) && results.get(patient) == ConceptFoundStatus.FOUND) {
			return DummySearchResult.Creators.NOT_ELIGIBLE.create(patient);
		} else if (results.containsKey(patient) && results.get(patient) == ConceptFoundStatus.NOT_FOUND) {
			return DummySearchResult.Creators.ELIGIBLE.create(patient);
		} else {
			return DummySearchResult.Creators.UNKNOWN.create(patient);
		}
	}
	
}
