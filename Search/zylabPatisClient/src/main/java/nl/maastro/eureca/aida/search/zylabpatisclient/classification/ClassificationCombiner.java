// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.classification;

/*>>>import checkers.nullness.quals.Nullable;*/
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.DocumentId;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.ResultDocument;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.SemanticModifier;
import nl.maastro.eureca.aida.search.zylabpatisclient.Gender;
import nl.maastro.eureca.aida.search.zylabpatisclient.Snippet;

/**
 * Combines the classifications of {@link ResultDocument}s into the 
 * {@link SearchResult}'s 
 * {@link SearchResult#getClassification() classification}.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public abstract class ClassificationCombiner implements Rule {
	private class SearchResultWrapper implements SearchResult {
		private final SearchResult delegate;

		public SearchResultWrapper(SearchResult delegate_) {
			this.delegate = delegate_;
		}

		@Override
		public PatisNumber getPatient() {
			return delegate.getPatient();
		}

		@Override
		public /*>>>@Nullable*/ Date getPatientBirthDate() {
			return delegate.getPatientBirthDate();
		}

		@Override
		public Gender getPatientGender() {
			return delegate.getPatientGender();
		}

		@Override
		public int getTotalHits() {
			return delegate.getTotalHits();
		}

		@Override
		public Set<DocumentId> getMatchingDocumentIds() {
			return delegate.getMatchingDocumentIds();
		}

		@Override
		public ResultDocument getDoc(DocumentId id) {
			return delegate.getDoc(id);
		}

		@Override
		public Collection<ResultDocument> getMatchingDocuments() {
			return delegate.getMatchingDocuments();
		}

		@Override
		public Set<ConceptFoundStatus> getClassification() {
			return ClassificationCombiner.this.getClassification(delegate);
		}

		@Override
		public void add(ResultDocument doc) {
			throw new UnsupportedOperationException("SearchResult cannot be modifier.");
		}

		@Override
		public void remove(ResultDocument doc) {
			throw new UnsupportedOperationException("SearchResult cannot be modifier.");
		}

		@Override
		public Map<DocumentId, Set<Snippet>> assignUnknownTo(SemanticModifier modifier) {
			throw new UnsupportedOperationException("SearchResult cannot be modifier.");
		}
	}

	protected abstract Set<ConceptFoundStatus> getClassification(SearchResult base);

	@Override
	public SearchResult apply(SearchResult searchResult) throws Inapplicable {
		return new SearchResultWrapper(searchResult);
	}
	
}
