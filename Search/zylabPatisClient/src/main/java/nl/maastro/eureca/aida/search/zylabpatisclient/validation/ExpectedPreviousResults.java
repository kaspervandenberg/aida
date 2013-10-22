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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.Concept;
import nl.maastro.eureca.aida.search.zylabpatisclient.DummySearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;

/**
 * Expect the results as produced by earlier Lucene invokations.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ExpectedPreviousResults implements ExpectedResults {
	private static final EligibilityClassification DEFAULT_CLASSIFICATION = EligibilityClassification.UNKNOWN;
	private static final EligibilityClassification CONFLICTING_CLASSIFICATION = EligibilityClassification.UNKNOWN;
	
	private static class Builder {
		private final Map<PatisNumber, Set<Set<EligibilityClassification>>> itemsToInsert = new ConcurrentHashMap<>();

		public ExpectedPreviousResults build(Concept about) {
			return new ExpectedPreviousResults(about, Collections.unmodifiableMap(itemsToInsert));
		}
		
		public Builder addAll(Iterable<SearchResult> previousResults) {
			for (SearchResult item : previousResults) {
				add(item);
			}
			return this;
		}

		public Builder addMap(Map<String, ? extends Set<? extends Set<String>>> items) {
			for (Map.Entry<String, ? extends Set<? extends Set<String>>> entry : items.entrySet()) {
				PatisNumber targetKey = PatisNumber.create(entry.getKey());
				Set<Set<EligibilityClassification>> toAdd = convert(entry.getValue());
				Set<Set<EligibilityClassification>> targetValues = getOrCreateClassifications(targetKey);
				targetValues.addAll(toAdd);
			}
			return this;
		}
		
		public Builder makeAllUnmodifiable() {
			for (PatisNumber key : itemsToInsert.keySet()) {
				makeUnmodifiable(key);
			}
			return this;
		}

		private static Set<Set<EligibilityClassification>> convert(Set<? extends Set<String>> items) {
			Set<Set<EligibilityClassification>> result = new HashSet<>(items.size());
			for (Set<String> innerSet : items) {
				result.add(convertInner(innerSet));
			}
			return result;
		}

		private static Set<EligibilityClassification> convertInner(Set<String> items) {
			Set<EligibilityClassification> result = EnumSet.noneOf(EligibilityClassification.class);
			for (String id : items) {
				result.add(EligibilityClassification.valueOf(id));
			}
			return result;
		}
		
		private void add(SearchResult previousResult) {
			PatisNumber key = previousResult.getPatient();
			Set<EligibilityClassification> expectedClassifications = 
					Collections.unmodifiableSet(previousResult.getClassification());
			
			Set<Set<EligibilityClassification>> target = getOrCreateClassifications(key);
			target.add(expectedClassifications);
		}

		private Set<Set<EligibilityClassification>> getOrCreateClassifications(PatisNumber patient) {
			if(!itemsToInsert.containsKey(patient)) {
				itemsToInsert.put(patient, new HashSet<Set<EligibilityClassification>>());
			}
			return itemsToInsert.get(patient);
		}

		private void makeUnmodifiable(PatisNumber key) {
			Set<Set<EligibilityClassification>> values = itemsToInsert.get(key);
			itemsToInsert.put(key, Collections.unmodifiableSet(values));
		}
	}
	
	private final Concept about;
	private final Map<PatisNumber, Set<Set<EligibilityClassification>>> expected;

	private ExpectedPreviousResults(Concept about_, Map<PatisNumber, Set<Set<EligibilityClassification>>> expected_) {
		this.about = about_;
		this.expected = expected_;
	}
	
	public static ExpectedPreviousResults create(Concept about, Iterable<SearchResult> previousResults) {
		return new Builder()
				.addAll(previousResults)
				.makeAllUnmodifiable()
				.build(about);
	}

	public static ExpectedPreviousResults read(Concept about, Reader input) {
		JsonObject obj_root = parse(input);
		Builder b = new Builder();
		
		b.addMap(readClassifications(obj_root));
		b.makeAllUnmodifiable();
		return b.build(about);
	}

	private static JsonObject parse(Reader input) {
		JsonParser parser = new JsonParser();
		JsonElement el_root = parser.parse(input);
		JsonObject obj_root = el_root.getAsJsonObject();
		return obj_root;
	}

	private static Map<String, Set<Set<String>>> readClassifications(JsonObject root) {
		if(root.has("expected")) {
			try {
				JsonElement entries = root.get("expected");
				Type mapType = new TypeToken<HashMap<String, HashSet<HashSet<String>>>>() {}.getType();
				Map<String, Set<Set<String>>> result = new Gson().fromJson(entries, mapType);
				return result;
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
				throw ex;
			}
		} else {
			throw new NoSuchElementException("no 'expected'-field in json");
		}
	}

	public void writeAsJson(Appendable out) {
		Type mapType = new TypeToken<HashMap<PatisNumber, HashSet<HashSet<EligibilityClassification>>>>() {}.getType();
		Gson gson = new Gson();
		
		JsonObject root = new JsonObject();
		root.add("expected", gson.toJsonTree(expected, mapType));
		
		gson.toJson(root, out);
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
		Set<EligibilityClassification> actualClassification = actual.getClassification();

		if(isInDefined(actualPatient)) {
			return expected.get(actualPatient).contains(actualClassification);
		} else {
			return false;
		}
	}

	@Override
	public boolean containsExpected(SearchResult actual) {
		PatisNumber actualPatient = actual.getPatient();
		Set<EligibilityClassification> actualClassification = actual.getClassification();

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
	public EligibilityClassification getClassification(PatisNumber patient) {
		if(isInDefined(patient)) {
			Set<Set<EligibilityClassification>> expectedClassifications = expected.get(patient);
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
			Set<Set<EligibilityClassification>> allClassifications = expected.get(patient);
			Set<EligibilityClassification> classification = mergeSets(allClassifications);
			return new DummySearchResult(patient, classification, 0);
		} else {
			return DummySearchResult.Creators.valueOf(DEFAULT_CLASSIFICATION).create(patient);
		}
	}

	@Override
	public Iterable<SearchResult> createAllExpectedResults() {
		return new ExpectedResultsToSearchResultsConvertor(this);
	}

	private static boolean  containsSubset(Set<Set<EligibilityClassification>> expectedSets, 
			Set<EligibilityClassification> actualClassification) {
		for (Set<EligibilityClassification> expectedResult : expectedSets) {
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

	private static Set<EligibilityClassification> mergeSets(Iterable<? extends Set<EligibilityClassification>> sets) {
		Set<EligibilityClassification> result = EnumSet.noneOf(EligibilityClassification.class);
		for (Set<EligibilityClassification> innerSet : sets) {
			result.addAll(innerSet);
		}
		return result;
	}
}
