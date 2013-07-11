// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries;

import java.util.LinkedHashMap;
import nl.maastro.eureca.aida.search.zylabpatisclient.DummySearcher;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.Searcher;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class Patients {
	private static Patients instance = null;

	private final LinkedHashMap<PatisNumber, EligibilityClassification> expectedMetastasis;

	private Patients() {
		expectedMetastasis = initExpectedMetastasis();
	}

	public static Patients instance() {
		if(instance == null) {
			instance = new Patients();
		}
		return instance;
	}
	
	public LinkedHashMap<PatisNumber, EligibilityClassification> getExpectedMetastasis() {
		return expectedMetastasis;
	}

	public Searcher getDummySearcher() {
		return new DummySearcher(getExpectedMetastasis());
	}

	private static LinkedHashMap<PatisNumber, EligibilityClassification> initExpectedMetastasis() {
		// Dummy list of patients; reading a list of patisnumbers is not yet in API
		LinkedHashMap<PatisNumber, EligibilityClassification> result = new LinkedHashMap<>();
		result.put(PatisNumber.create("71358"), EligibilityClassification.ELIGIBLE);// Exp 0
		result.put(PatisNumber.create("71314"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71415"), EligibilityClassification.ELIGIBLE); // Exp 0
		result.put(PatisNumber.create("71539"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71586"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("70924"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71785"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71438"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71375"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71448"), EligibilityClassification.ELIGIBLE);
		
		result.put(PatisNumber.create("71681"), EligibilityClassification.NOT_ELIGIBLE); // Exp 1
		result.put(PatisNumber.create("71692"), EligibilityClassification.NOT_ELIGIBLE);
		result.put(PatisNumber.create("71757"), EligibilityClassification.NOT_ELIGIBLE);
		result.put(PatisNumber.create("70986"), EligibilityClassification.NOT_ELIGIBLE);
		result.put(PatisNumber.create("46467"), EligibilityClassification.NOT_ELIGIBLE);
		
		result.put(PatisNumber.create("71441"), EligibilityClassification.NOT_ELIGIBLE);
		result.put(PatisNumber.create("71121"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71089"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("70657"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("70979"), EligibilityClassification.ELIGIBLE);

		result.put(PatisNumber.create("71367"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71369"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71118"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71363"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("70933"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71105"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71190"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("70946"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71074"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("70996"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71422"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71193"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71454"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71169"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71739"), EligibilityClassification.ELIGIBLE);
		result.put(PatisNumber.create("71464"), EligibilityClassification.ELIGIBLE);
		return result;

	}
}
