// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;

/**
 * Compare {@link SearchResult}s with {@link ExpectedResults}.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ResultComparison {
	private enum MatchTypes {
		STRICT__SINGLETONS_ONLY,
		WEAK__EXPECTED_AND_OTHERS,
		DIFFERING__CONTAINS_NO_EXPECTED;

		public static MatchTypes valueOf(ResultComparison context, SearchResult result) {
			ExpectedResults expectations = context.getExpected();
			if(expectations.isAsExpected(result)) {
				if(result.getClassification().size() == 1) {
					return STRICT__SINGLETONS_ONLY;
				} else {
					return WEAK__EXPECTED_AND_OTHERS;
				}
			} else {
				return DIFFERING__CONTAINS_NO_EXPECTED;
			}
		}
	}
	
	public enum Qualifications {
		/**
		 * The {@link SearchResult}s whose {@link SearchResult#getClassification()}-set is an singletonset containing
		 * {@link ExpectedResults#getClassification}.
		 */
		@SuppressWarnings("serial")
		ACTUAL_MATCHING_EXPECTED(
				new EnumMap<MatchTypes, List<ActualExpectedEligibilityClassificationPair>>(MatchTypes.class) {{
				put(
						MatchTypes.STRICT__SINGLETONS_ONLY,
						ActualExpectedEligibilityClassificationPair.identityPairs()); }}),
		
		/**
		 * The {@link SearchResult}s whose {@link SearchResult#getClassification()}-set contains 
		 * {@link ExpectedResults#getClassification} and some other element.
		 */
		@SuppressWarnings("serial")
		ACTUAL_CONTAINIG_EXPECTED_AND_OTHERS(
				new EnumMap<MatchTypes, List<ActualExpectedEligibilityClassificationPair>>(MatchTypes.class) {{
				put(
						MatchTypes.WEAK__EXPECTED_AND_OTHERS,
						ActualExpectedEligibilityClassificationPair.identityPairs()); }}),

		/**
		 * The {@link SearchResult}s whose {@link SearchResult#getClassification()}-set does NOT contain
		 * {@link ExpectedResults#getClassification}. 
		 */
		@SuppressWarnings("serial")
		ACTUAL_DIFFERING_FROM_EXPECTED(
				new EnumMap<MatchTypes, List<ActualExpectedEligibilityClassificationPair>>(MatchTypes.class) {{
				put(
						MatchTypes.DIFFERING__CONTAINS_NO_EXPECTED,
						ActualExpectedEligibilityClassificationPair.identityPairs()); }}),
		
		/**
		 * The {@link PatisNumber}s in {@link ExpectedResults#getDefinedPatients()} not found in the actual 
		 * results.
		 */
		MISSING_ACTUAL_RESULTS() {
			@Override
			protected int count(ResultComparison context) {
				return context.getMissingResults().size();
			}

			@Override
			protected List<SearchResult> collect(ResultComparison context) {
				Set<PatisNumber> missing = context.getMissingResults();
				List<SearchResult> result = new ArrayList<>(missing.size());
				ExpectedResults expected = context.getExpected();
				
				for (PatisNumber missingPatient : missing) {
					SearchResult dummy = expected.createExpectedResult(missingPatient);
					result.add(dummy);
				}

				return result;
			}
		},
		
		/**
		 * The {@link PatisNumber} in the search results for which no expected results are defined.
		 */
		EXTRA_ACTUAL_RESULTS() {
			@Override
			protected int count(ResultComparison context) {
				return context.getUnexpectedResults().size();
			}

			@Override
			protected List<SearchResult> collect(ResultComparison context) {
				return context.getUnexpectedResults();
			}
		};

		private final EnumMap<MatchTypes, List<ActualExpectedEligibilityClassificationPair>> coordinates;
		
		private Qualifications() {
			this.coordinates = new EnumMap<>(MatchTypes.class);
		}
		
		private Qualifications(Map<MatchTypes, List<ActualExpectedEligibilityClassificationPair>> coordinates_) {
			this.coordinates = new EnumMap<>(coordinates_);
		}
		
		protected int count(ResultComparison context) {
			int sum = 0;
			for (Map.Entry<MatchTypes, List<ActualExpectedEligibilityClassificationPair>> entry : coordinates.entrySet()) {
				ComparisonTable table = context.getCounters(entry.getKey());
				int count = table.countAll(entry.getValue());
				sum += count;
			}

			return sum;
		}

		protected List<SearchResult> collect(ResultComparison context) {
			List<SearchResult> result = new LinkedList<>();
			for (Map.Entry<MatchTypes, List<ActualExpectedEligibilityClassificationPair>> entry : coordinates.entrySet()) {
				ComparisonTable table = context.getCounters(entry.getKey());
				List<SearchResult> collected = table.collect(entry.getValue());
				result.addAll(collected);
			}

			return result;
		}
	}
	
	private final ExpectedResults expected;
	private final Iterable<SearchResult> actual;
	private final Set<PatisNumber> notSeenInResults;
	private final List<SearchResult> unexpectedResults;
	private final EnumMap<MatchTypes, ComparisonTable> counters;

	public static ResultComparison compare(Iterable<SearchResult> actual, ExpectedResults expected) {
		ResultComparison result = new ResultComparison(expected, actual);
		result.countAllActualItems();

		return result;
	}
	
	private ResultComparison(ExpectedResults expected_, Iterable<SearchResult> actual_) {
		this.expected = expected_;
		this.actual = actual_;
		this.notSeenInResults = new HashSet<>(expected.getDefinedPatients());
		this.unexpectedResults = new LinkedList<>();
		this.counters = createEmptyCounterTables();
	}
	
	public int getCount(Qualifications qualification) {
		return qualification.count(this);
	}

	public List<SearchResult> getResults(Qualifications qualification) {
		return qualification.collect(this);
	}
	
	private static EnumMap<MatchTypes, ComparisonTable> createEmptyCounterTables() {
		EnumMap<MatchTypes, ComparisonTable> result = new EnumMap<>(MatchTypes.class);
		for (MatchTypes matchType : MatchTypes.values()) {
			result.put(matchType, new ComparisonTable());
		}
		
		return result;	
	}

	private void countAllActualItems() {
		for (SearchResult searchResult : actual) {
			countResult(searchResult);
		}
	}

	private void countResult(SearchResult searchResult) {
		PatisNumber patient = searchResult.getPatient();
		markAsSeen(patient);
		if(expected.isInDefined(patient)) {
			countDefinedResult(patient, searchResult);
		} else {
			unexpectedResults.add(searchResult);
		}
	}

	private void markAsSeen(PatisNumber patient) {
		notSeenInResults.remove(patient);
	}
	

	private void countDefinedResult(PatisNumber patient, SearchResult searchResult) {
		MatchTypes matchType = MatchTypes.valueOf(this, searchResult);
		ComparisonTable table = getCounters(matchType);
		EligibilityClassification expectClassification = expected.getClassification(patient);
		insertInto(table, expectClassification, searchResult);
	}

	private void insertInto(ComparisonTable table, EligibilityClassification expectedClassification, SearchResult value) {
		Set<EligibilityClassification> targetRows = actualClassificationsUnionExpected(value, expectedClassification);
		
		for (EligibilityClassification row : targetRows) {
			table.put(new ActualExpectedEligibilityClassificationPair(row, expectedClassification), value);
		}
	}

	private Set<EligibilityClassification> actualClassificationsUnionExpected(SearchResult searchResult, EligibilityClassification expectedClassification) {
		Set<EligibilityClassification> result = EnumSet.copyOf(searchResult.getClassification());
		result.add(expectedClassification);
		
		return result;
	}
	
	private ExpectedResults getExpected() {
		return expected;
	}
	
	private ComparisonTable getCounters(MatchTypes matchType) {
		return counters.get(matchType);
	}
	
	private Set<PatisNumber> getMissingResults() {
		return Collections.unmodifiableSet(notSeenInResults);
	}

	private List<SearchResult> getUnexpectedResults() {
		return Collections.unmodifiableList(unexpectedResults);
	}
}
