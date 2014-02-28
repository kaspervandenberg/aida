// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.MonotonicNonNull;
import java.util.LinkedHashMap;
import nl.maastro.eureca.aida.search.zylabpatisclient.DummySearcher;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.Searcher;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class Patients {
	private static @MonotonicNonNull Patients instance = null;

	private final LinkedHashMap<PatisNumber, ConceptFoundStatus> expectedMetastasis;

	private Patients() {
		expectedMetastasis = initExpectedMetastasis();
	}

	@EnsuresNonNull("instance")
	public static Patients instance() {
		if(instance == null) {
			instance = new Patients();
		}
		return instance;
	}
	
	public LinkedHashMap<PatisNumber, ConceptFoundStatus> getExpectedMetastasis() {
		return expectedMetastasis;
	}

	public Searcher getDummySearcher() {
		return new DummySearcher(getExpectedMetastasis());
	}

	private static LinkedHashMap<PatisNumber, ConceptFoundStatus> initExpectedMetastasis() {
		// Dummy list of patients; reading a list of patisnumbers is not yet in API
		LinkedHashMap<PatisNumber, ConceptFoundStatus> result = new LinkedHashMap<>();
		result.put(PatisNumber.create("71358"), ConceptFoundStatus.NOT_FOUND);// Exp 0
		result.put(PatisNumber.create("71314"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71415"), ConceptFoundStatus.NOT_FOUND); // Exp 0
		result.put(PatisNumber.create("71539"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71586"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("70924"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71785"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71438"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71375"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71448"), ConceptFoundStatus.NOT_FOUND);
		
		result.put(PatisNumber.create("71681"), ConceptFoundStatus.FOUND); // Exp 1
		result.put(PatisNumber.create("71692"), ConceptFoundStatus.FOUND);
		result.put(PatisNumber.create("71757"), ConceptFoundStatus.FOUND);
		result.put(PatisNumber.create("70986"), ConceptFoundStatus.FOUND);
		result.put(PatisNumber.create("46467"), ConceptFoundStatus.FOUND);
		
		result.put(PatisNumber.create("71441"), ConceptFoundStatus.FOUND);
		result.put(PatisNumber.create("71121"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71089"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("70657"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("70979"), ConceptFoundStatus.NOT_FOUND);

		result.put(PatisNumber.create("71367"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71369"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71118"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71363"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("70933"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71105"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71190"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("70946"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71074"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("70996"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71422"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71193"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71454"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71169"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71739"), ConceptFoundStatus.NOT_FOUND);
		result.put(PatisNumber.create("71464"), ConceptFoundStatus.NOT_FOUND);
		return result;

	}
}
