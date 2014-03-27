// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

/*>>>import checkers.nullness.quals.Nullable;*/
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;

/**
 * Search result uses as expected results.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DummySearchResult implements SearchResult {
	/**
	 * Use {@link #valueOf(nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification) };
	 * {@link #create(nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber) } to create a {@code DummySearchResult} for
	 * {@code classification} as expected classification for {@code patient}.
	 */
	public enum Creators {
		NOT_ELIGIBLE(ConceptFoundStatus.FOUND, 1),
		UNCERTAIN(ConceptFoundStatus.UNCERTAIN, 1),
		ELIGIBLE(ConceptFoundStatus.NOT_FOUND, 0),
		UNKNOWN(ConceptFoundStatus.FOUND_CONCEPT_UNKNOWN, 1);

		private static final Map<ConceptFoundStatus, Creators> classificationToCreator;
		static {
			EnumMap<ConceptFoundStatus, Creators> result = new EnumMap<>(ConceptFoundStatus.class);
			result.put(ConceptFoundStatus.FOUND, NOT_ELIGIBLE);
			result.put(ConceptFoundStatus.UNCERTAIN, UNCERTAIN);
			result.put(ConceptFoundStatus.NOT_FOUND, ELIGIBLE);
			result.put(ConceptFoundStatus.FOUND_CONCEPT_UNKNOWN, UNKNOWN);
			classificationToCreator = Collections.unmodifiableMap(result);
		}
		private final ConceptFoundStatus classification;
		private final int hitCount;

		private Creators(ConceptFoundStatus classification_, int hitCount_) {
			this.classification = classification_;
			this.hitCount = hitCount_;
		}

		public static Creators valueOf(ConceptFoundStatus classification) {
			Creators result = classificationToCreator.get(classification);
			if (result != null) {
				return result;
			} else {
				throw new Error(String.format(
						"classificationToCreator does not contain value for %s",
						classification));
			}
		}

		public DummySearchResult create(PatisNumber patient) {
			return new DummySearchResult(patient, classification, hitCount);
		}
	}

	private final PatisNumber patient;
	private final int totalHits;
	private final Set<ConceptFoundStatus> classification;

	public DummySearchResult(PatisNumber patient_, ConceptFoundStatus classification_, int hitCount) {
		this.patient = patient_;
		this.totalHits = hitCount;
		this.classification = Collections.singleton(classification_);
	}

	public DummySearchResult(PatisNumber patient_, Set<ConceptFoundStatus> classification_, int hitCount) {
		this.patient = patient_;
		this.totalHits = hitCount;
		this.classification = classification_;
	}

	@Override
	public Set<ConceptFoundStatus> getClassification() {
		return Collections.unmodifiableSet(classification);
	}
	

	@Override
	public PatisNumber getPatient() {
		return patient;
	}

	@Override
	public /*>>>@Nullable*/ Date getPatientBirthDate() {
		return null;
	}

	@Override
	public Gender getPatientGender() {
		return Gender.UNKNOWN;
	}

	@Override
	public int getTotalHits() {
		return totalHits;
	}

	@Override
	public Set<DocumentId> getMatchingDocumentIds() {
		return Collections.emptySet();
	}

	@Override
	public ResultDocument getDoc(DocumentId id) {
		throw new NoSuchElementException("DummySearchResult has no document");
	}

	@Override
	public Collection<ResultDocument> getMatchingDocuments() {
		return Collections.emptySet();
	}

	@Override
	public void add(ResultDocument doc) {
		throw new UnsupportedOperationException("Not supported; Cannot modify DummySearchResult.");
	}

	@Override
	public void remove(ResultDocument doc) {
		throw new UnsupportedOperationException("Not supported; Cannot modify DummySearchResult.");
	}

	@Override
	public Map<DocumentId, Set<Snippet>> assignUnknownTo(SemanticModifier modifier) {
		return Collections.emptyMap();
	}
}
