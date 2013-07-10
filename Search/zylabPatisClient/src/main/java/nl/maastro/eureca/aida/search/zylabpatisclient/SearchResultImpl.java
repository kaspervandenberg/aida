// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;
import com.google.gson.Gson;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Results that {@link ZylabPatisClient} returns
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class SearchResultImpl implements SearchResult {
	private static class DummyEligible extends SearchResultImpl {
		public DummyEligible(PatisNumber patient) {
			super(patient, 0);
		}

		@Override
		public Set<EligibilityClassification> getClassification() {
			return Collections.singleton(EligibilityClassification.ELIGIBLE);
		}
	}

	private static class DummyNotEligible extends SearchResultImpl {
		public DummyNotEligible(PatisNumber patient) {
			super(patient, 1);
		}

		@Override
		public Set<EligibilityClassification> getClassification() {
			return Collections.singleton(EligibilityClassification.NOT_ELIGIBLE);
		}
	}
	
	private static class DummyUnknown extends SearchResultImpl {
		public DummyUnknown(PatisNumber patient) {
			super(patient, 0);
		}

		@Override
		public Set<EligibilityClassification> getClassification() {
			return Collections.singleton(EligibilityClassification.UNKNOWN);
		}
	}
			
	/**
	 * The relevant parts of the response from SearcherWS.
	 */
	private static class SearcherWSResults {
		int hits;
	}
	
	private static transient Gson gsonParserInstance;
	private static transient SearchResultImpl NO_RESULT;
	private final PatisNumber patient;
	private final int totalHits;
	
	private final Map<DocumentId, ResultDocument> matchingDocs;

	private SearchResultImpl(PatisNumber patient_, int nHits_) {
		this.patient = patient_;
		this.totalHits = nHits_;
		this.matchingDocs = Collections.emptyMap();
	}

	private SearchResultImpl(PatisNumber patient_, int nHits_, 
			Map<DocumentId, ResultDocument> docs_) {
		this.patient = patient_;
		this.totalHits = nHits_;
		this.matchingDocs = docs_;
	}

	public SearchResultImpl(PatisNumber patient) {
		this(patient, 0, Collections.<ResultDocument>emptyList());
	}
	
	public SearchResultImpl(PatisNumber patient_, int nHits_, 
			Collection<ResultDocument> matchingDocs_) {
		this.patient = patient_;
		this.totalHits = nHits_;
		this.matchingDocs = new HashMap<>();
		for (ResultDocument doc : matchingDocs_) {
			this.matchingDocs.put(doc.getId(), doc);
		}
		
	}

	public SearchResultImpl(SearchResult other) {
		this.patient = other.getPatient();
		this.totalHits = other.getTotalHits();
		Set<DocumentId> otherDocs = other.getMatchingDocumentIds();
		this.matchingDocs = new HashMap<>(otherDocs.size());
		for (DocumentId docId : otherDocs) {
			this.matchingDocs.put(docId, new ResultDocument(other.getDoc(docId)));
		}
	}

	/**
	 * 
	 * @return 
	 */
	public static SearchResultImpl NO_RESULT() {
		if (NO_RESULT == null) {
			NO_RESULT = new SearchResultImpl(null, -1);
		}
		return NO_RESULT;
	}


	public static SearchResultImpl create(final PatisNumber patient_, final String json) {
		SearcherWSResults result = getGsonParser().fromJson(json, SearcherWSResults.class);
		return new SearchResultImpl(patient_, result.hits);
	}

	public static SearchResultImpl create(final PatisNumber patient_, final boolean found) {
		return new SearchResultImpl(patient_, found ? 1 : 0);
	}

	public static SearchResultImpl createDummyEligible(final PatisNumber patient_) {
		return new DummyEligible(patient_);
	}

	public static SearchResultImpl createDummyNotEliligle(final PatisNumber patient_) {
		return new DummyNotEligible(patient_);
	}

	public static SearchResultImpl createDummyUnknown(final PatisNumber patient_) {
		return new DummyUnknown(patient_);
	}
	public static SearchResultImpl combine(SearchResult... parts) {
		int totalHits = 0;
		Set<PatisNumber> patients = new HashSet<>(1);
		Map<DocumentId, List<ResultDocument>> docsToCombine =
				new HashMap<>();
		for (SearchResult src : parts) {
			patients.add(src.getPatient());
			if(patients.size() != 1) {
				throw new IllegalArgumentException(
						"Cannot combine search results of different patients: " +
						patients.toString());
			}

			totalHits += src.getTotalHits();
			
			for (ResultDocument doc : src.getMatchingDocuments()) {
				DocumentId id = doc.getId();
				if(!docsToCombine.containsKey(id)) {
					docsToCombine.put(id, new LinkedList<ResultDocument>());
				}
				docsToCombine.get(id).add(doc);
			}
		}
		Map<DocumentId, ResultDocument> mergedDocs = new HashMap<>(docsToCombine.size());
		for (Map.Entry<DocumentId, List<ResultDocument>> entry : docsToCombine.entrySet()) {
			mergedDocs.put(entry.getKey(), ResultDocument.combine(entry.getValue()));
		}
		
		return new SearchResultImpl(patients.iterator().next(),
				totalHits, mergedDocs);
	}

	/**
	 * @return the patient
	 */
	@Override
	public PatisNumber getPatient() {
		return patient;
	}

	/**
	 * @return the total number of matches over all documents in the index.
	 * 		{@code nHits} can be greater than the sum of the hits in 
	 * 		{@link #getMatchingDocuments()} c.f. 
	 * 		{@link org.apache.lucene.search.TopDocs#totalHits}.
	 * 		{@link #remove(nl.maastro.eureca.aida.search.zylabpatisclient.ResultDocument) 
	 * 		removing} a document from this {@code SearchResult} will not update
	 * 	I	{@code nHits}
	 */
	@Override
	public int getTotalHits() {
		return totalHits;
	}

	@Override
	public Set<DocumentId> getMatchingDocumentIds() {
		return Collections.unmodifiableSet(matchingDocs.keySet());
	}

	@Override
	public ResultDocument getDoc(DocumentId id) {
		return matchingDocs.get(id);
	}
	
	@Override
	public Collection<ResultDocument> getMatchingDocuments() {
		return Collections.unmodifiableCollection(matchingDocs.values());
	}

	@Override
	public Set<EligibilityClassification> getClassification() {
		Set<EligibilityClassification> result = new HashSet<>();
		for (ResultDocument doc : getMatchingDocuments()) {
			result.addAll(doc.getClassifiers());
		}
		if(result.isEmpty()) {
			if(getTotalHits() > 0) {
				result.add(EligibilityClassification.NOT_ELIGIBLE);
			} else {
				result.add(EligibilityClassification.ELIGIBLE);
			}
		}
		return result;
	}

	@Override
	public void add(ResultDocument doc) {
		matchingDocs.put(doc.getId(), doc);
	}

	@Override
	public void remove(ResultDocument doc) {
		matchingDocs.remove(doc.getId());
	}

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
	@Override
	public Map<DocumentId, Set<Snippet>> assignUnknownTo(
			SemanticModifier modifier) {
		Map<DocumentId, Set<Snippet>> changed = new HashMap<>();
		for (Map.Entry<DocumentId, ResultDocument> entry : matchingDocs.entrySet()) {
			changed.put(entry.getKey(), entry.getValue().reclassify(
					SemanticModifier.Constants.UNKNOWN_MODIFIER, modifier));
		}
		return changed;
	}
	
	private static Gson getGsonParser() {
		if (gsonParserInstance == null) {
			gsonParserInstance = new Gson();
		}
		return gsonParserInstance;
	}
	
}
