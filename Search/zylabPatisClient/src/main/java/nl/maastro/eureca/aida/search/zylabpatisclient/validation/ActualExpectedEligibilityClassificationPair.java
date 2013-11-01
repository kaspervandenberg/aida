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
class ActualExpectedEligibilityClassificationPair {
	private final static List<ActualExpectedEligibilityClassificationPair> IDENTITY_PAIRS;
	static {
		List<ActualExpectedEligibilityClassificationPair> result = new ArrayList<>(ConceptFoundStatus.values().length);
		for (ConceptFoundStatus classification : ConceptFoundStatus.values()) {
			result.add(new ActualExpectedEligibilityClassificationPair(classification, classification));
		}

		IDENTITY_PAIRS = Collections.unmodifiableList(result);
	}
	
	public final ConceptFoundStatus actual;
	public final ConceptFoundStatus expected;

	public ActualExpectedEligibilityClassificationPair(ConceptFoundStatus actual_, ConceptFoundStatus expected_) {
		this.actual = actual_;
		this.expected = expected_;
	}
	
	public static ActualExpectedEligibilityClassificationPair c(ConceptFoundStatus actual_, ConceptFoundStatus expected_) {
		return new ActualExpectedEligibilityClassificationPair(actual_, expected_);
	}

	public static List<ActualExpectedEligibilityClassificationPair> identityPairs() {
		return Collections.unmodifiableList(IDENTITY_PAIRS);
	}

	public static List<ActualExpectedEligibilityClassificationPair> empty() {
		return Collections.emptyList();
	}
}
