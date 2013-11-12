// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import java.util.Collection;
import nl.maastro.eureca.aida.search.zylabpatisclient.Concept;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;

/**
 * Results that a concept query should produce.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface ExpectedResults {
	/**
	 * @return	the title to use when displaying this concept. 
	 */
	public String getTitle();

	/**
	 * @return 	the {@link Concept} for which these are the expected results.
	 */
	public Concept getAboutConcept();
	
	/**
	 * @return the Set of Patients for whom expected results are defined
	 */
	public Iterable<PatisNumber> getDefinedPatients();

	/**
	 * Is {@code searchResult} as defined in {@code this} {@code ExpectedResults}?
	 */
	public boolean isAsExpected(SearchResult searchResult);

	/**
	 * Does {@code searchResult} contain the expected result for 
	 * {@code searchResult.}{@link SearchResult#getPatient()}?
	 */
	public boolean containsExpected(SearchResult searchResult);

	/**
	 * Is {@code patient} among the {@code PatisNumber}s for which {@code this} defines expected results?
	 */
	public boolean isInDefined(PatisNumber patient);

	/**
	 * @return	the expected {@link EligibilityClassification} of the {@code patient} for the concept {@link #getConcept()}
	 */
	public ConceptFoundStatus getClassification(PatisNumber patient);

	/**
	 * @return	a {@link SearchResult} containing {@code patient} classified as expected. 
	 */
	public SearchResult createExpectedResult(PatisNumber patient);

	/**
	 * @return	an {@link Iterable} containing each patient in {@link #getDefinedPatients()} classified as expected. 
	 */
	public Iterable<SearchResult> createAllExpectedResults();
}
