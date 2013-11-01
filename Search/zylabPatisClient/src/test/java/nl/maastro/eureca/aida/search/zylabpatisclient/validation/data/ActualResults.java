// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;
import org.mockito.Mockito;

/**
 * Mocked {@link SearchResult} used to test {@link ResultComparison}.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public enum ActualResults {
	ACTUAL_1_SINGLETON_ELIGIBLE("1", ConceptFoundStatus.NOT_FOUND),
	ACTUAL_1_SINGLETON_NOT_ELIGIBLE("1", ConceptFoundStatus.FOUND),
	ACTUAL_1_SINGLETON_UNCERTAIN("1", ConceptFoundStatus.UNCERTAIN),
	ACTUAL_1_WEAKLY_AS_EXPECTED("1", ConceptFoundStatus.UNCERTAIN, ConceptFoundStatus.NOT_FOUND),
	ACTUAL_1_SET_DIFFERING1("1", ConceptFoundStatus.FOUND, ConceptFoundStatus.UNCERTAIN),
	ACTUAL_1_SET_DIFFERING2("1", ConceptFoundStatus.FOUND_CONCEPT_UNKNOWN, ConceptFoundStatus.FOUND),
	ACTUAL_2_SINGLETON_ELIGIBLE("2", ConceptFoundStatus.NOT_FOUND),
	ACTUAL_2_SINGLETON_NOT_ELIGIBLE("2", ConceptFoundStatus.FOUND),
	ACTUAL_2_SINGLETON_UNCERTAIN("2", ConceptFoundStatus.UNCERTAIN),
	ACTUAL_2_WEAKLY_AS_EXPECTED("2", ConceptFoundStatus.NOT_FOUND, ConceptFoundStatus.FOUND_CONCEPT_UNKNOWN), 
	ACTUAL_2_SET_DIFFERING1("2", ConceptFoundStatus.FOUND, ConceptFoundStatus.UNCERTAIN),
	ACTUAL_2_SET_DIFFERING2("2", ConceptFoundStatus.FOUND_CONCEPT_UNKNOWN, ConceptFoundStatus.FOUND),
	ACTUAL_3_SINGLETON_AS_EXPECTED("3", ConceptFoundStatus.FOUND),
	ACTUAL_3_SINGLTON_DIFFERING("3", ConceptFoundStatus.NOT_FOUND),
	ACTUAL_5_SINGLETON_AS_EXPECTED("5", ConceptFoundStatus.UNCERTAIN),
	ACTUAL_7_UNEXPECTED("7", ConceptFoundStatus.FOUND);
	
	private final PatisNumber patient;
	private final Set<ConceptFoundStatus> classifications;
	public final SearchResult result;

	private ActualResults(String patentID, ConceptFoundStatus... classification_) {
		this.patient = PatisNumber.create(patentID);
		this.classifications = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(classification_)));
		this.result = Mockito.mock(SearchResult.class);
		Mockito.when(result.getPatient()).thenReturn(patient);
		Mockito.when(result.getClassification()).thenReturn(classifications);
	}
	
}
