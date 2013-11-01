// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import nl.maastro.eureca.aida.search.zylabpatisclient.Concept;
import nl.maastro.eureca.aida.search.zylabpatisclient.DummySearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;

/**
 * Expect the results as produced by earlier Lucene invokations.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ExpectedPreviousResults implements ExpectedResults {
	private static final ConceptFoundStatus DEFAULT_CLASSIFICATION = ConceptFoundStatus.FOUND_CONCEPT_UNKNOWN;
	private static final ConceptFoundStatus CONFLICTING_CLASSIFICATION = ConceptFoundStatus.FOUND_CONCEPT_UNKNOWN;
	
	private static class Builder {
		private final Map<PatisNumber, Set<Set<ConceptFoundStatus>>> itemsToInsert = new ConcurrentHashMap<>();
		private Date searchDate = new Date();

		public ExpectedPreviousResults build(Concept about) {
			return new ExpectedPreviousResults(about, searchDate, Collections.unmodifiableMap(itemsToInsert));
		}
		
		public Builder addAll(Iterable<SearchResult> previousResults) {
			for (SearchResult item : previousResults) {
				add(item);
			}
			return this;
		}

		public Builder parseJson(Reader input) {
			JsonObject jsonRoot = parse(input);
			readDate(jsonRoot);
			readClassifications(jsonRoot);

			return this;
		}

		public Builder setSearchDateToNow() {
			searchDate = new Date();
			return this;
		}

		public Builder setSearchDate(Date newValue) {
			searchDate = new Date(newValue.getTime());
			return this;
		}
				

		public Builder makeAllUnmodifiable() {
			for (PatisNumber key : itemsToInsert.keySet()) {
				makeUnmodifiable(key);
			}
			return this;
		}
		
		private static JsonObject parse(Reader input) {
			JsonParser parser = new JsonParser();
			JsonElement el_root = parser.parse(input);
			JsonObject obj_root = el_root.getAsJsonObject();
			return obj_root;
		}

		private void add(SearchResult previousResult) {
			PatisNumber key = previousResult.getPatient();
			Set<ConceptFoundStatus> expectedClassifications = 
					Collections.unmodifiableSet(previousResult.getClassification());
			
			Set<Set<ConceptFoundStatus>> target = getOrCreateClassifications(key);
			target.add(expectedClassifications);
		}

		private Set<Set<ConceptFoundStatus>> getOrCreateClassifications(PatisNumber patient) {
			if(!itemsToInsert.containsKey(patient)) {
				itemsToInsert.put(patient, new HashSet<Set<ConceptFoundStatus>>());
			}
			return itemsToInsert.get(patient);
		}

		private void readDate(JsonObject root) {
			if(root.has("searchDate")) {
				JsonElement json_searchDate = root.get("searchDate");
				Date value = new Gson().fromJson(json_searchDate, Date.class);
				setSearchDate(value);
			} else {
				setSearchDateToNow();
			}
		}

		private void readClassifications(JsonObject root) {
			if(root.has("expected")) {
				JsonObject entries = root.getAsJsonObject("expected");
				for (Map.Entry<String, JsonElement> mapping : entries.entrySet()) {
					readMapping(mapping);
				}
			} else {
				throw new NoSuchElementException("no 'expected'-field in json");
			}
		}
		
		private void readMapping(Map.Entry<String, JsonElement> jsonEntry) {
			PatisNumber key = PatisNumber.create(jsonEntry.getKey());
			Set<Set<ConceptFoundStatus>> target = getOrCreateClassifications(key);
			JsonArray setsToAdd = jsonEntry.getValue().getAsJsonArray();
			for (JsonElement innerSet : setsToAdd) {
				readClassificationSet(target, innerSet);
			}
		}

		private static void readClassificationSet(Set<Set<ConceptFoundStatus>> target, JsonElement innerSet) {
			Set<ConceptFoundStatus> classificationSet = EnumSet.noneOf(ConceptFoundStatus.class);
			JsonArray ids = innerSet.getAsJsonArray();
			for (JsonElement json_id : ids) {
				String str_id = json_id.getAsString();
				ConceptFoundStatus classification = ConceptFoundStatus.valueOf(str_id);
				classificationSet.add(classification);
			}
			target.add(Collections.unmodifiableSet(classificationSet));
		}
				

		private void makeUnmodifiable(PatisNumber key) {
			Set<Set<ConceptFoundStatus>> values = itemsToInsert.get(key);
			itemsToInsert.put(key, Collections.unmodifiableSet(values));
		}
	}
	
	private final Concept about;
	private final Date searchDate;
	private final Map<PatisNumber, Set<Set<ConceptFoundStatus>>> expected;

	private ExpectedPreviousResults(Concept about_, Date searchDate_, 
			Map<PatisNumber, Set<Set<ConceptFoundStatus>>> expected_) {
		this.about = about_;
		this.searchDate = new Date(searchDate_.getTime());
		this.expected = expected_;
	}
	
	public static ExpectedPreviousResults create(Concept about, Iterable<SearchResult> previousResults) {
		return new Builder()
				.addAll(previousResults)
				.setSearchDateToNow()
				.makeAllUnmodifiable()
				.build(about);
	}

	public static ExpectedPreviousResults read(Concept about, Reader input) {
		Builder b = new Builder();
		
		b.parseJson(input);
		b.makeAllUnmodifiable();
		return b.build(about);
	}

	public void writeAsJson(Appendable out) {
		Type mapType = new TypeToken<HashMap<PatisNumber, HashSet<HashSet<ConceptFoundStatus>>>>() {}.getType();
		Gson gson = new Gson();
		
		JsonObject root = new JsonObject();
		root.add("searchDate", gson.toJsonTree(searchDate));
		root.add("expected", gson.toJsonTree(expected, mapType));
		
		gson.toJson(root, out);
	}

	@Override
	public String getTitle() {
		String result = String.format("Results for %1$s (on %2$ta %2$tF %2$tR)", 
				about.getName().getLocalPart(), searchDate);
		return result;
	}
	
	@Override
	public Concept getAboutConcept() {
		return about;
	}

	@Override
	public Collection<PatisNumber> getDefinedPatients() {
		return expected.keySet();
	}

	@Override
	public boolean isAsExpected(SearchResult actual) {
		PatisNumber actualPatient = actual.getPatient();
		Set<ConceptFoundStatus> actualClassification = actual.getClassification();

		if(isInDefined(actualPatient)) {
			return expected.get(actualPatient).contains(actualClassification);
		} else {
			return false;
		}
	}

	@Override
	public boolean containsExpected(SearchResult actual) {
		PatisNumber actualPatient = actual.getPatient();
		Set<ConceptFoundStatus> actualClassification = actual.getClassification();

		if(isInDefined(actualPatient)) {
			return containsSubset(expected.get(actualPatient), actualClassification);
		} else {
			return false;
		}
	}

	@Override
	public boolean isInDefined(PatisNumber patient) {
		return expected.containsKey(patient);
	}

	@Override
	public ConceptFoundStatus getClassification(PatisNumber patient) {
		if(isInDefined(patient)) {
			Set<Set<ConceptFoundStatus>> expectedClassifications = expected.get(patient);
			if(isSingleton2(expectedClassifications)) {
				return getSingletonValue2(expectedClassifications);
			} else {
				return CONFLICTING_CLASSIFICATION;
			}
		} else {
			return DEFAULT_CLASSIFICATION;
		}
	}

	@Override
	public SearchResult createExpectedResult(PatisNumber patient) {
		if(isInDefined(patient)) {
			Set<Set<ConceptFoundStatus>> allClassifications = expected.get(patient);
			Set<ConceptFoundStatus> classification = mergeSets(allClassifications);
			return new DummySearchResult(patient, classification, 0);
		} else {
			return DummySearchResult.Creators.valueOf(DEFAULT_CLASSIFICATION).create(patient);
		}
	}

	@Override
	public Iterable<SearchResult> createAllExpectedResults() {
		return new ExpectedResultsToSearchResultsConvertor(this);
	}

	private static boolean  containsSubset(Set<Set<ConceptFoundStatus>> expectedSets, 
			Set<ConceptFoundStatus> actualClassification) {
		for (Set<ConceptFoundStatus> expectedResult : expectedSets) {
			if(actualClassification.containsAll(expectedResult)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isSingleton(Set<?> set) {
		return set.size() == 1;
	}

	private static <TInnerSet extends Set<?>> boolean isSingleton2(Set<TInnerSet> set) {
		if (isSingleton(set)) {
			Set<?> inner = getSingletonValue(set);
			return isSingleton(inner);
		} else {
			return false;
		}
	}

	private static <T> T getSingletonValue(Set<T> set) {
		if(isSingleton(set)) {
			return set.iterator().next();
		} else {
			throw new IllegalArgumentException("set must be a singleton");
		}
	}

	private static <T> T getSingletonValue2(Set<? extends Set<T>> set) {
		if (isSingleton2(set)) {
			Set<T> inner = getSingletonValue(set);
			return getSingletonValue(inner);
		} else {
			throw new IllegalArgumentException("set must be a singleton of singleton");
		}
	}

	private static Set<ConceptFoundStatus> mergeSets(Iterable<? extends Set<ConceptFoundStatus>> sets) {
		Set<ConceptFoundStatus> result = EnumSet.noneOf(ConceptFoundStatus.class);
		for (Set<ConceptFoundStatus> innerSet : sets) {
			result.addAll(innerSet);
		}
		return result;
	}
}
