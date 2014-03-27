// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

/*>>>import checkers.nullness.quals.Nullable;*/
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;

/**
 * Results that {@link ZylabPatisClient} returns
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface SearchResult {

	/**
	 * @return the patient
	 */
	public PatisNumber getPatient();

	public /*>>>@Nullable*/ Date getPatientBirthDate();
	
	public Gender getPatientGender();

	/**
	 * @return the total number of matches over all documents in the index.
	 * 		{@code nHits} can be greater than the sum of the hits in
	 * 		{@link #getMatchingDocuments()} c.f.
	 * 		{@link org.apache.lucene.search.TopDocs#totalHits}.
	 * 		{@link #remove(nl.maastro.eureca.aida.search.zylabpatisclient.ResultDocument)
	 * 		removing} a document from this {@code SearchResult} will not update
	 * 	I	{@code nHits}
	 */
	public int getTotalHits();
	
	public Set<DocumentId> getMatchingDocumentIds();

	public ResultDocument getDoc(DocumentId id);
	
	public Collection<ResultDocument> getMatchingDocuments();

	public Set<ConceptFoundStatus> getClassification();

	public void add(ResultDocument doc);

	public void remove(ResultDocument doc);
	
	/**
	 * Assign {@code modifier} as {@link SemanticModifier} to all {@link Snippet}s
	 * that have {@link nl.maastro.eureca.aida.search.zylabpatisclient.SemanticModifier.Constants#UNKNOWN_MODIFIER}
	 * as value.
	 *
	 * @param modifier	the new {@link SemanticModifier} to assign
	 *
	 * @return  a {@link DocumentId}–set of {@link Snippet}-map of all
	 * 		{@code Snippets} that were assign to {@code modifier}
	 */
	public Map<DocumentId, Set<Snippet>> assignUnknownTo(
			SemanticModifier modifier);


}
