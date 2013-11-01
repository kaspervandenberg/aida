// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import nl.maastro.eureca.aida.search.zylabpatisclient.DummySearchResult;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.Concept;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;


/**
 * Basic {@link PatisNumber patient}–{@link EligibilityClassification expected classification}-map for a single concept.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ExpectedResultsMap implements ExpectedResults {
	private static final ConceptFoundStatus DEFAULT_CLASSIFICATION = ConceptFoundStatus.FOUND_CONCEPT_UNKNOWN;
	private final Concept about;
	private final Map<PatisNumber, ConceptFoundStatus> classifications;

	private ExpectedResultsMap(Concept about_) {
		this.about = about_;
		this.classifications = new HashMap<>();
	}

	private ExpectedResultsMap(Concept about_, Map<PatisNumber, ConceptFoundStatus> classifications_) {
		this.about = about_;
		this.classifications = classifications_;
	}
	
	public static ExpectedResultsMap createEmpty(Concept about) {
		return new ExpectedResultsMap(about);
	}
	
	/**
	 * @return	a wrapper arround {@code classifications}: changes in {@code classifications} cause changes in {@code this} and vice
	 * 		verse.
	 * 
	 * @see #createIndependentCopy(nl.maastro.eureca.aida.search.zylabpatisclient.Concept, java.util.Map) createIndependentCopy
	 */
	public static ExpectedResultsMap createWrapper(Concept about, Map<PatisNumber, ConceptFoundStatus> classifications) {
		return new ExpectedResultsMap(about, classifications);
	}

	/**
	 * @return	 a fresh {@code ExpectedResultsMap} initialised to {@code classifications} and insulated from it: future changes in
	 * 		{@code classifications} have no effect on {@code this}, neither do changes in {@code this} affect {@code classifications}.
	 * 		
	 */	
	public static ExpectedResultsMap createIndependentCopy(Concept about, Map<PatisNumber, ConceptFoundStatus> classifications) {
		Map<PatisNumber, ConceptFoundStatus> copy = new HashMap<>(classifications);
		return new ExpectedResultsMap(about, copy);
	}

	@Override
	public String getTitle() {
		String result = String.format("Expected results for %s", 
				about.getName().getLocalPart());
		return result;
	}
	
	@Override
	public Concept getAboutConcept() {
		return about;
	}

	@Override
	public Collection<PatisNumber> getDefinedPatients() {
		return Collections.unmodifiableSet(classifications.keySet());
	}

	@Override
	public boolean isAsExpected(SearchResult searchResult) {
		PatisNumber patient = searchResult.getPatient();
		Set<ConceptFoundStatus> actual = searchResult.getClassification();

		ConceptFoundStatus expected = getClassification(patient);
		return compareStrictlyActualAndExpected(actual, expected);
	}

	@Override
	public boolean containsExpected(SearchResult searchResult) {
		PatisNumber patient = searchResult.getPatient();
		Set<ConceptFoundStatus> actual = searchResult.getClassification();

		ConceptFoundStatus expected = getClassification(patient);
		return actual.contains(expected);
	}

	@Override
	public boolean isInDefined(PatisNumber patient) {
		return classifications.containsKey(patient);
	}

	@Override
	public ConceptFoundStatus getClassification(PatisNumber patient) {
		if (isInDefined(patient)) {
			return classifications.get(patient);
		} else {
			return DEFAULT_CLASSIFICATION;
		}
	}

	@Override
	public SearchResult createExpectedResult(PatisNumber patient) {
		ConceptFoundStatus expected = getClassification(patient);
		return DummySearchResult.Creators.valueOf(expected).create(patient);
	}

	@Override
	public Iterable<SearchResult> createAllExpectedResults() {
		return new ExpectedResultsToSearchResultsConvertor(this);
	}
	
	private boolean compareStrictlyActualAndExpected(Set<ConceptFoundStatus> actual, ConceptFoundStatus expected) {
		return (actual.size() == 1) && actual.contains(expected);
	}

}
