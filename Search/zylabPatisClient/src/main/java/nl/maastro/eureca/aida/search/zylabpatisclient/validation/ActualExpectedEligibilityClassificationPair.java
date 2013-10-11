// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
class ActualExpectedEligibilityClassificationPair {
	private final static List<ActualExpectedEligibilityClassificationPair> IDENTITY_PAIRS;
	static {
		List<ActualExpectedEligibilityClassificationPair> result = new ArrayList<>(EligibilityClassification.values().length);
		for (EligibilityClassification classification : EligibilityClassification.values()) {
			result.add(new ActualExpectedEligibilityClassificationPair(classification, classification));
		}

		IDENTITY_PAIRS = Collections.unmodifiableList(result);
	}
	
	public final EligibilityClassification actual;
	public final EligibilityClassification expected;

	public ActualExpectedEligibilityClassificationPair(EligibilityClassification actual_, EligibilityClassification expected_) {
		this.actual = actual_;
		this.expected = expected_;
	}
	
	public static ActualExpectedEligibilityClassificationPair c(EligibilityClassification actual_, EligibilityClassification expected_) {
		return new ActualExpectedEligibilityClassificationPair(actual_, expected_);
	}

	public static List<ActualExpectedEligibilityClassificationPair> identityPairs() {
		return Collections.unmodifiableList(IDENTITY_PAIRS);
	}

	public static List<ActualExpectedEligibilityClassificationPair> empty() {
		return Collections.emptyList();
	}
}
