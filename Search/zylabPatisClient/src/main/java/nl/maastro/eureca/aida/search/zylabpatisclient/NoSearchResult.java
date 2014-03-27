// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

/*>>>import checkers.nullness.quals.Nullable;*/
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;

/**
 * Indactes that no result is available.  Used by {@link SearchResultImpl#NO_RESULT()}.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
class NoSearchResult implements SearchResult {

	public NoSearchResult() {
	}

	@Override
	public PatisNumber getPatient() {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public /*>>>@Nullable*/ Date getPatientBirthDate() {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public Gender getPatientGender() {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public int getTotalHits() {
		return 0;
	}

	@Override
	public Set<DocumentId> getMatchingDocumentIds() {
		return Collections.<DocumentId>emptySet();
	}

	@Override
	public ResultDocument getDoc(DocumentId id) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public Collection<ResultDocument> getMatchingDocuments() {
		return Collections.<ResultDocument>emptySet();
	}

	@Override
	public Set<ConceptFoundStatus> getClassification() {
		return Collections.singleton(ConceptFoundStatus.NOT_FOUND);
	}

	@Override
	public void add(ResultDocument doc) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public void remove(ResultDocument doc) {
		// ignored
	}

	@Override
	public Map<DocumentId, Set<Snippet>> assignUnknownTo(SemanticModifier modifier) {
		return Collections.<DocumentId, Set<Snippet>>emptyMap();
	}
	
}
