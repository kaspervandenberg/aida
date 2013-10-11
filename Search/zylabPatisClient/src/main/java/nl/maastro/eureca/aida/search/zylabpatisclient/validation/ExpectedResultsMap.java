// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.Concept;
import nl.maastro.eureca.aida.search.zylabpatisclient.DummySearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;


/**
 * Basic {@link PatisNumber patient}–{@link EligibilityClassification expected classification}-map for a single concept.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ExpectedResultsMap implements ExpectedResults {
	private static final EligibilityClassification DEFAULT_CLASSIFICATION = EligibilityClassification.UNKNOWN;
	private final Concept about;
	private final Map<PatisNumber, EligibilityClassification> classifications;

	private ExpectedResultsMap(Concept about_) {
		this.about = about_;
		this.classifications = new HashMap<>();
	}

	private ExpectedResultsMap(Concept about_, Map<PatisNumber, EligibilityClassification> classifications_) {
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
	public static ExpectedResultsMap createWrapper(Concept about, Map<PatisNumber, EligibilityClassification> classifications) {
		return new ExpectedResultsMap(about, classifications);
	}

	/**
	 * @return	 a fresh {@code ExpectedResultsMap} initialised to {@code classifications} and insulated from it: future changes in
	 * 		{@code classifications} have no effect on {@code this}, neither do changes in {@code this} affect {@code classifications}.
	 * 		
	 */	
	public static ExpectedResultsMap createIndependentCopy(Concept about, Map<PatisNumber, EligibilityClassification> classifications) {
		Map<PatisNumber, EligibilityClassification> copy = new HashMap<>(classifications);
		return new ExpectedResultsMap(about, copy);
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
		Set<EligibilityClassification> actual = searchResult.getClassification();

		EligibilityClassification expected = getClassification(patient);
		return compareActualAndExpected(actual, expected);
	}

	@Override
	public boolean isInDefined(PatisNumber patient) {
		return classifications.containsKey(patient);
	}

	@Override
	public EligibilityClassification getClassification(PatisNumber patient) {
		if (isInDefined(patient)) {
			return classifications.get(patient);
		} else {
			return DEFAULT_CLASSIFICATION;
		}
	}

	@Override
	public SearchResult createExpectedResult(PatisNumber patient) {
		EligibilityClassification expected = getClassification(patient);
		return DummySearchResult.Creators.valueOf(expected).create(patient);
	}
	
	private boolean compareActualAndExpected(Set<EligibilityClassification> actual, EligibilityClassification expected) {
		return (actual.size() == 1) && actual.contains(expected);
	}

}
