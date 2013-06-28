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
public class SearchResult {
	/**
	 * The relevant parts of the response from SearcherWS.
	 */
	private static class SearcherWSResults {
		int hits;
	}
	
	private static transient Gson gsonParserInstance;
	private static transient SearchResult NO_RESULT;
	public final PatisNumber patient;
	public final int nHits;
	
	private final Map<DocumentId, ResultDocument> matchingDocs;

	private SearchResult(PatisNumber patient_, int nHits_) {
		this.patient = patient_;
		this.nHits = nHits_;
		this.matchingDocs = Collections.emptyMap();
	}

	private SearchResult(PatisNumber patient_, int nHits_, 
			Map<DocumentId, ResultDocument> docs_) {
		this.patient = patient_;
		this.nHits = nHits_;
		this.matchingDocs = docs_;
	}

	public SearchResult(PatisNumber patient) {
		this(patient, 0, Collections.<ResultDocument>emptyList());
	}
	
	public SearchResult(PatisNumber patient_, int nHits_, 
			Collection<ResultDocument> matchingDocs_) {
		this.patient = patient_;
		this.nHits = nHits_;
		this.matchingDocs = new HashMap<>();
		for (ResultDocument doc : matchingDocs_) {
			this.matchingDocs.put(doc.getId(), doc);
		}
		
	}

	public SearchResult(SearchResult other) {
		this.patient = other.patient;
		this.nHits = other.nHits;
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
	public static SearchResult NO_RESULT() {
		if (NO_RESULT == null) {
			NO_RESULT = new SearchResult(null, -1);
		}
		return NO_RESULT;
	}


	public static SearchResult create(final PatisNumber patient_, final String json) {
		SearcherWSResults result = getGsonParser().fromJson(json, SearcherWSResults.class);
		return new SearchResult(patient_, result.hits);
	}

	public static SearchResult create(final PatisNumber patient_, final boolean found) {
		return new SearchResult(patient_, found ? 1 : 0);
	}

	public static SearchResult combine(SearchResult... parts) {
		int totalHits = 0;
		Set<PatisNumber> patients = new HashSet<>(1);
		Map<DocumentId, List<ResultDocument>> docsToCombine =
				new HashMap<>();
		for (SearchResult src : parts) {
			patients.add(src.patient);
			if(patients.size() != 1) {
				throw new IllegalArgumentException(
						"Cannot combine search results of different patients: " +
						patients.toString());
			}

			totalHits += src.nHits;
			
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
		
		return new SearchResult(patients.iterator().next(),
				totalHits, mergedDocs);
	}

	public Set<DocumentId> getMatchingDocumentIds() {
		return Collections.unmodifiableSet(matchingDocs.keySet());
	}

	public ResultDocument getDoc(DocumentId id) {
		return matchingDocs.get(id);
	}
	
	public Collection<ResultDocument> getMatchingDocuments() {
		return Collections.unmodifiableCollection(matchingDocs.values());
	}

	public Set<EligibilityClassification> getClassification() {
		Set<EligibilityClassification> result = new HashSet<>();
		for (ResultDocument doc : getMatchingDocuments()) {
			result.addAll(doc.getClassifiers());
		}
		if(result.isEmpty()) {
			if(nHits > 0) {
				result.add(EligibilityClassification.NOT_ELIGIBLE);
			} else {
				result.add(EligibilityClassification.NO_EXCLUSION_CRITERION_FOUND);
			}
		}
		return result;
	}

	public void add(ResultDocument doc) {
		matchingDocs.put(doc.getId(), doc);
	}

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
