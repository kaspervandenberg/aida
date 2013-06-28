// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;
import com.google.gson.Gson;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.search.TopDocs;

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
	
	private final Map<DocumentId, Map<SemanticModifier, Set<Snippet>>> snippets;

	private SearchResult(PatisNumber patient_, int nHits_) {
		this.patient = patient_;
		this.nHits = nHits_;
		this.snippets = Collections.<DocumentId, Map<SemanticModifier, Set<Snippet>>>emptyMap();
	}

	private SearchResult(PatisNumber patient_, int nHits_, 
			Map<DocumentId, Map<SemanticModifier, Set<Snippet>>> snippets_) {
		this.patient = patient_;
		this.nHits = nHits_;
		this.snippets = snippets_;
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

	/**
	 * Create a SearchResult from a call to 
	 * {@link org.apache.lucene.search.IndexSearcher#search(org.apache.lucene.search.Query, int)}
	 * 
	 * @param patient_	the {@link PatisNumber} of the patient about whon these
	 * 		{@code SearchResults} are.
	 * @param hits	the {@link org.apache.lucene.search.TopDocs} as returned by
	 * 		{@link org.apache.lucene.search.IndexSearcher#search(org.apache.lucene.search.Query, int)}
	 * @param snippets_		a map of document id to the set of matching snippets.
	 * 		All {@code Snippets} are assigned to 
	 * 		{@link nl.maastro.eureca.aida.search.zylabpatisclient.SemanticModifier.Constants#UNKNOWN_MODIFIER}.
	 * 		Use {@link #assignUnknownTo(nl.maastro.eureca.aida.search.zylabpatisclient.SemanticModifier) }
	 * 		to assign an other {@link SemanticModifier} to the created 
	 * 		{@code SearchResults}.
	 * 
	 * @return	a fresh {@link SearchResult} 
	 */
	public static SearchResult create(final PatisNumber patient_, 
			final TopDocs hits, Map<String, Set<String>> snippets_) {
		return create(patient_, SemanticModifier.Constants.UNKNOWN_MODIFIER, hits, snippets_);
	}

	public static SearchResult create(final PatisNumber patient_,
			final SemanticModifier modifier_, final TopDocs hits,
			final Map<String, Set<String>> snippets_) {
		Map<DocumentId, Map<SemanticModifier, Set<Snippet>>> tmpSnippets =
				new HashMap<>(snippets_.size());
		for (Map.Entry<String, Set<String>> entry : snippets_.entrySet()) {
			DocumentId docId = new DocumentId(entry.getKey());
			Set<Snippet> perDocSnippets = new HashSet<>(entry.getValue().size());
			for (String snippetText : entry.getValue()) {
				perDocSnippets.add(new Snippet(snippetText));
			}
			tmpSnippets.put(docId,
					Collections.<SemanticModifier, Set<Snippet>>singletonMap(
						modifier_, perDocSnippets));
		}
		return new SearchResult(patient_, hits.totalHits, tmpSnippets);
		
	}
	
	public static SearchResult create(final PatisNumber patient_,
			final int nHits, 
			final Map<DocumentId, Map<SemanticModifier, Set<Snippet>>> snippets_) {
		return new SearchResult(patient_, nHits, snippets_);
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
		Map<DocumentId, Map<SemanticModifier, Set<Snippet>>> combinedSnippets =
				new HashMap<>();
		for (int i = 0; i < parts.length; i++) {
			SearchResult part = parts[i];
			patients.add(part.patient);
			if(patients.size() != 1) {
				throw new IllegalArgumentException(
						"Cannot combine search results of different patients: " +
						patients.toString());
			}
			totalHits += part.nHits;
			for (DocumentId docId : part.getMatchingDocuments()) {
				if (!combinedSnippets.containsKey(docId)) {
					combinedSnippets.put(docId, new HashMap<SemanticModifier, Set<Snippet>>());
				}
				for (SemanticModifier semMod : part.getModifiers(docId)) {
					if (!combinedSnippets.get(docId).containsKey(semMod)) {
						combinedSnippets.get(docId).put(semMod, new HashSet<Snippet>());
					}
					combinedSnippets.get(docId).get(semMod).addAll(part.getSnippets(docId, semMod));
				}
			}
		}
		return new SearchResult(patients.iterator().next(),
				totalHits, combinedSnippets);
	}

	public Set<DocumentId> getMatchingDocuments() {
		return Collections.unmodifiableSet(snippets.keySet());
	}

	public Set<SemanticModifier> getModifiers(DocumentId docId) {
		return Collections.unmodifiableSet(snippets.get(docId).keySet());
	}

	public Set<Snippet> getSnippets(DocumentId docId, SemanticModifier modifier) {
		return Collections.unmodifiableSet(snippets.get(docId).get(modifier));
	}
	
	public Set<Snippet> getSnippets(DocumentId docId) {
		Map<SemanticModifier, Set<Snippet>> snippetsByDoc = snippets.get(docId);
		HashSet<Snippet> result = new HashSet<>(snippetsByDoc.size());
		for (Set<Snippet> snipets : snippetsByDoc.values()) {
			result.addAll(snipets);
		}
		return result;
	}

	public Set<EligibilityClassification> getClassification() {
		Set<EligibilityClassification> result = new HashSet<>();
		for (DocumentId docId : snippets.keySet()) {
			result.addAll(getClassification(docId));
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
	
	public Set<EligibilityClassification> getClassification(DocumentId docId) {
		Set<SemanticModifier> modifiers = snippets.get(docId).keySet();
		Set<EligibilityClassification> result = new HashSet<>(modifiers.size());
		for (SemanticModifier semMod : modifiers) {
			result.add(semMod.getClassification());
		}
		return result;
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
		for (Map.Entry<DocumentId, Map<SemanticModifier, Set<Snippet>>> entry : snippets.entrySet()) {
			if (entry.getValue().containsKey(SemanticModifier.Constants.UNKNOWN_MODIFIER)) {
				Set<Snippet> unknownSnippets = entry.getValue().get(SemanticModifier.Constants.UNKNOWN_MODIFIER);
				if(!unknownSnippets.isEmpty()) {
					if(!entry.getValue().containsKey(modifier)) {
						entry.getValue().put(modifier, new HashSet<Snippet>(unknownSnippets.size()));
					}
					entry.getValue().get(modifier).addAll(unknownSnippets);
					changed.put(entry.getKey(), unknownSnippets);
				}
			}
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
