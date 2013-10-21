// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;
import org.mockito.Mockito;

/**
 * Mocked {@link SearchResult} used to test {@link ResultComparison}.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public enum ActualResults {
	ACTUAL_1_SINGLETON_ELIGIBLE("1", EligibilityClassification.ELIGIBLE),
	ACTUAL_1_SINGLETON_NOT_ELIGIBLE("1", EligibilityClassification.NOT_ELIGIBLE),
	ACTUAL_1_SINGLETON_UNCERTAIN("1", EligibilityClassification.UNCERTAIN),
	ACTUAL_1_WEAKLY_AS_EXPECTED("1", EligibilityClassification.UNCERTAIN, EligibilityClassification.ELIGIBLE),
	ACTUAL_1_SET_DIFFERING1("1", EligibilityClassification.NOT_ELIGIBLE, EligibilityClassification.UNCERTAIN),
	ACTUAL_1_SET_DIFFERING2("1", EligibilityClassification.UNKNOWN, EligibilityClassification.NOT_ELIGIBLE),
	ACTUAL_2_SINGLETON_ELIGIBLE("2", EligibilityClassification.ELIGIBLE),
	ACTUAL_2_SINGLETON_NOT_ELIGIBLE("2", EligibilityClassification.NOT_ELIGIBLE),
	ACTUAL_2_SINGLETON_UNCERTAIN("2", EligibilityClassification.UNCERTAIN),
	ACTUAL_2_WEAKLY_AS_EXPECTED("2", EligibilityClassification.ELIGIBLE, EligibilityClassification.UNKNOWN), 
	ACTUAL_2_SET_DIFFERING1("2", EligibilityClassification.NOT_ELIGIBLE, EligibilityClassification.UNCERTAIN),
	ACTUAL_2_SET_DIFFERING2("2", EligibilityClassification.UNKNOWN, EligibilityClassification.NOT_ELIGIBLE),
	ACTUAL_3_SINGLETON_AS_EXPECTED("3", EligibilityClassification.NOT_ELIGIBLE),
	ACTUAL_3_SINGLTON_DIFFERING("3", EligibilityClassification.ELIGIBLE),
	ACTUAL_5_SINGLETON_AS_EXPECTED("5", EligibilityClassification.UNCERTAIN),
	ACTUAL_7_UNEXPECTED("7", EligibilityClassification.NOT_ELIGIBLE);
	
	private final PatisNumber patient;
	private final Set<EligibilityClassification> classifications;
	public final SearchResult result;

	private ActualResults(String patentID, EligibilityClassification... classification_) {
		this.patient = PatisNumber.create(patentID);
		this.classifications = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(classification_)));
		this.result = Mockito.mock(SearchResult.class);
		Mockito.when(result.getPatient()).thenReturn(patient);
		Mockito.when(result.getClassification()).thenReturn(classifications);
	}
	
}
