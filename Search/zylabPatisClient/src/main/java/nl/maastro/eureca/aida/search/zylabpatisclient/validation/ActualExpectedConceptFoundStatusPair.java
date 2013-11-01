// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
class ActualExpectedConceptFoundStatusPair {
	private final static List<ActualExpectedConceptFoundStatusPair> IDENTITY_PAIRS;
	static {
		List<ActualExpectedConceptFoundStatusPair> result = new ArrayList<>(ConceptFoundStatus.values().length);
		for (ConceptFoundStatus classification : ConceptFoundStatus.values()) {
			result.add(new ActualExpectedConceptFoundStatusPair(classification, classification));
		}

		IDENTITY_PAIRS = Collections.unmodifiableList(result);
	}
	
	public final ConceptFoundStatus actual;
	public final ConceptFoundStatus expected;

	public ActualExpectedConceptFoundStatusPair(ConceptFoundStatus actual_, ConceptFoundStatus expected_) {
		this.actual = actual_;
		this.expected = expected_;
	}
	
	public static ActualExpectedConceptFoundStatusPair c(ConceptFoundStatus actual_, ConceptFoundStatus expected_) {
		return new ActualExpectedConceptFoundStatusPair(actual_, expected_);
	}

	public static List<ActualExpectedConceptFoundStatusPair> identityPairs() {
		return Collections.unmodifiableList(IDENTITY_PAIRS);
	}

	public static List<ActualExpectedConceptFoundStatusPair> empty() {
		return Collections.emptyList();
	}
}
