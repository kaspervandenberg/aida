// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import nl.maastro.eureca.aida.search.zylabpatisclient.validation.data.ActualResultLists;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.data.ActualResults;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.Concept;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ResultComparison.Qualifications;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.hamcrest.Matchers.*;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import static nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus.*;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.mockito.Mockito;

/**
 *
 * @author kasper
 */
@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
@RunWith(Theories.class)
public class ResultComparisonTest {
	private enum Expected {
		ELIGIBLE_1("1", NOT_FOUND),
		ELIGIBLE_2("2", NOT_FOUND),
		NOT_ELIGIBLE_3("3", FOUND),
		NOT_ELIGIBLE_4("4", FOUND),
		UNCERTAIN_5("5", UNCERTAIN),
		UNCERTAIN_6("6", UNCERTAIN);

		private final PatisNumber patis;
		private final ConceptFoundStatus expectedClassification;
		
		private Expected(String patientID, ConceptFoundStatus classification) {
			this.patis = PatisNumber.create(patientID);
			this.expectedClassification = classification;
		}
	}
	
	private enum ExpectedResultsCreators {
		EMPTY(),
		SINGLE_ELIGIBLE(Expected.ELIGIBLE_1),
		TWO_ELIGIBLE(Expected.ELIGIBLE_1, Expected.ELIGIBLE_2),
		SINGLE_NOT_ELIGIBLE(Expected.NOT_ELIGIBLE_3),
		TWO_NOT_ELIGIBLE(Expected.NOT_ELIGIBLE_3, Expected.NOT_ELIGIBLE_4),
		SINGLE_UNCERTAIN(Expected.UNCERTAIN_5),
		TWO_UNCERTAIN(Expected.UNCERTAIN_6),
		ONE_EACH(Expected.ELIGIBLE_1, Expected.NOT_ELIGIBLE_3, Expected.UNCERTAIN_5),
		ALL(Expected.values());

		private final Expected[] items;

		private ExpectedResultsCreators(Expected... items_) {
			this.items = items_;
		}
		
		public ExpectedResults create(ResultComparisonTest context) {
			Map<PatisNumber, ConceptFoundStatus> contents = new HashMap<>(items.length);
			for (Expected item : items) {
				contents.put(item.patis, item.expectedClassification);
			}
			return ExpectedResultsMap.createIndependentCopy(context.searchedConcept, contents);
		}
	}

	private static class CombinedSearchResults {
		Set<ActualResultLists> counted;
		Set<ActualResultLists> ignored;

		public CombinedSearchResults(EnumSet<ActualResultLists> counted_, EnumSet<ActualResultLists> ignored_) {
			this.counted = counted_;
			this.ignored = ignored_;
		}
	}
	
	@DataPoints
	public static final Qualifications[] ALL_QUALIFICATIONS = ResultComparison.Qualifications.values();

	@DataPoints
	public static final  ExpectedResultsCreators[] ALL_EXPECTED_RESULTS_CREATORS = ExpectedResultsCreators.values();

	@DataPoints
	public static final ActualResultLists[] ALL_SEARCH_RESULT_LISTS = ActualResultLists.values();

	@SuppressWarnings("serial")
	private static final EnumMap<Qualifications, CombinedSearchResults> SEARCH_RESULT_LIST_CLASSES =
			new EnumMap<Qualifications, CombinedSearchResults>(Qualifications.class) {{
				put(Qualifications.ACTUAL_MATCHING_EXPECTED, new CombinedSearchResults(
						EnumSet.of(
							ActualResultLists.EMPTY,
							ActualResultLists.SINGLE_1_SINGLETON_EXPECTED,
							ActualResultLists.SINGLE_2_SINGLETON_EXPECTED,
							ActualResultLists.SINGLE_3_SINGLETON_EXPECTED,
							ActualResultLists.SINGLE_5_SINGLETON_EXPECTED,
							ActualResultLists.TWO_SINGLETON_SAME_CAT_1_EXPECTED_2_EXPECTED,
							ActualResultLists.TWO_SINGLETON_DIVERSE_CAT_1_EXPECTED_3_EXPECTED,
							ActualResultLists.FOUR_SINGLETON_1_EXPECTED_2_EXPECTED_3_EXPECTED_5_EXPECTED),
						EnumSet.of(
							ActualResultLists.EMPTY,
							ActualResultLists.SINGLE_1_SINGLETON_DIFFERING1,
							ActualResultLists.SINGLE_1_SINGLETON_DIFFERING2,
							ActualResultLists.SINGLE_1_SET_WEAK_EXPECTED,
							ActualResultLists.SINGLE_1_SET_WEAK_DIFFERING1,
							ActualResultLists.SINGLE_1_SET_WEAK_DIFFERING2,
							ActualResultLists.SINGLE_7_UNDEFINED,
							ActualResultLists.TWO_SINGLETON_1_DIFFERING_2_DIFFERING,
							ActualResultLists.TWO_SAME_CAT_SINGLETON_DIFFERING,
							ActualResultLists.TWO_SINGLETON_DIVERSE_CAT_DIFFERING)));
				put(Qualifications.ACTUAL_DIFFERING_FROM_EXPECTED, new CombinedSearchResults(
						EnumSet.of(
							ActualResultLists.EMPTY,
							ActualResultLists.SINGLE_1_SET_WEAK_DIFFERING1,
							ActualResultLists.SINGLE_1_SET_WEAK_DIFFERING2,
							ActualResultLists.SINGLE_1_SINGLETON_DIFFERING1,
							ActualResultLists.SINGLE_1_SINGLETON_DIFFERING2,
							ActualResultLists.THREE_MIXED_SET_SINGLETON_MIXED_CAT_DIFFERING,
							ActualResultLists.TWO_SAME_CAT_SINGLETON_DIFFERING,
							ActualResultLists.TWO_SINGLETON_1_DIFFERING_2_DIFFERING,
							ActualResultLists.TWO_SINGLETON_DIVERSE_CAT_DIFFERING),
						EnumSet.of(
							ActualResultLists.EMPTY,
							ActualResultLists.FOUR_SINGLETON_1_EXPECTED_2_EXPECTED_3_EXPECTED_5_EXPECTED,
							ActualResultLists.SINGLE_1_SINGLETON_EXPECTED,
							ActualResultLists.SINGLE_2_SINGLETON_EXPECTED,
							ActualResultLists.SINGLE_3_SINGLETON_EXPECTED,
							ActualResultLists.SINGLE_5_SINGLETON_EXPECTED,
							ActualResultLists.SINGLE_7_UNDEFINED,
							ActualResultLists.TWO_SINGLETON_DIVERSE_CAT_1_EXPECTED_3_EXPECTED,
							ActualResultLists.TWO_SINGLETON_SAME_CAT_1_EXPECTED_2_EXPECTED)));
				put(Qualifications.ACTUAL_CONTAINIG_EXPECTED_AND_OTHERS, new CombinedSearchResults(
						EnumSet.of(
							ActualResultLists.EMPTY,
							ActualResultLists.SINGLE_1_SET_WEAK_EXPECTED,
							ActualResultLists.SINGLE_2_SET_WEAK_EXPECTED,
							ActualResultLists.TWO_SAME_CAT_1_SET_WEAK_EXPECTED_2_SET_WEAK_EXPECTED),
						EnumSet.of(
							ActualResultLists.EMPTY,
							ActualResultLists.SINGLE_1_SINGLETON_EXPECTED,
							ActualResultLists.SINGLE_1_SINGLETON_DIFFERING1,
							ActualResultLists.SINGLE_1_SINGLETON_DIFFERING2,
							ActualResultLists.SINGLE_2_SINGLETON_EXPECTED,
							ActualResultLists.SINGLE_7_UNDEFINED,
							ActualResultLists.TWO_SINGLETON_SAME_CAT_1_EXPECTED_2_EXPECTED,
							ActualResultLists.TWO_SINGLETON_1_DIFFERING_2_DIFFERING,
							ActualResultLists.TWO_SINGLETON_DIVERSE_CAT_1_EXPECTED_3_EXPECTED
						)));
			}};

	@Mock private Concept searchedConcept; 
	
	private static final Set<Qualifications> DEFINED_QUALIFICATIONS;
	static {
		Set<Qualifications> result = new HashSet<>(Arrays.asList(ALL_QUALIFICATIONS));
		result.remove(ResultComparison.Qualifications.MISSING_ACTUAL_RESULTS);

		DEFINED_QUALIFICATIONS = Collections.unmodifiableSet(result);
	}
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Theory
	public void bothEmpty_any0(Qualifications qualifier) {
		ResultComparison testee = ResultComparison.compare(ActualResultLists.EMPTY.result, ExpectedResultsCreators.EMPTY.create(this));

		assertThat(testee.getCount(qualifier), equalTo(0));
	}

	@Theory
	public void bothEmpty_anyEmpty(Qualifications qualifier) {
		ResultComparison testee = ResultComparison.compare(ActualResultLists.EMPTY.result, ExpectedResultsCreators.EMPTY.create(this));

		assertThat(testee.getResults(qualifier), empty());
	}

	@Theory
	public void actualEmpty_anyDefined0(Qualifications qualifier, ExpectedResultsCreators expectedResultsCreator) {
		assumeThat(qualifier, isIn(DEFINED_QUALIFICATIONS));
		ResultComparison testee = ResultComparison.compare(ActualResultLists.EMPTY.result, expectedResultsCreator.create(this));

		assertThat(testee.getCount(qualifier), equalTo(0));
	}

	@Theory
	public void actualEmpty_anyDefinedEmpty(Qualifications qualifier, ExpectedResultsCreators expectedResultsCreator) {
		assumeThat(qualifier, isIn(DEFINED_QUALIFICATIONS));
		ResultComparison testee = ResultComparison.compare(ActualResultLists.EMPTY.result, expectedResultsCreator.create(this));

		assertThat(testee.getResults(qualifier), empty());
	}

	@Theory
	public void actualEmpty_missingAllExpected(ExpectedResultsCreators expectedResultsCreator) {
		ResultComparison testee = ResultComparison.compare(ActualResultLists.EMPTY.result, expectedResultsCreator.create(this));

		for (Expected expectedItem : expectedResultsCreator.items) {
			assertThat(String.format("Missing %s", expectedItem.patis),
					testee.getResults(Qualifications.MISSING_ACTUAL_RESULTS),
					hasItem(Matchers.<SearchResult>hasProperty("patient", equalTo(expectedItem.patis))));
		}
	}

	@Theory
	public void combinedResultList_count(Qualifications qualifier, ActualResultLists countedItems, ActualResultLists otherItems) {
		List<SearchResult> actual = createSearchResults(qualifier, countedItems, otherItems);
		
		ResultComparison testee = ResultComparison.compare(actual, ExpectedResultsCreators.ALL.create(this));
		
		assertThat(testee.getCount(qualifier), equalTo(countedItems.result.size()));
	}

	@Theory
	public void combinedResultList_contains(Qualifications qualifier, ActualResultLists countedItems, ActualResultLists otherItems) {
		List<SearchResult> actual = createSearchResults(qualifier, countedItems, otherItems);

		ResultComparison testee = ResultComparison.compare(actual, ExpectedResultsCreators.ALL.create(this));

		for (ActualResults searchResult : countedItems.items) {
			assertThat(testee.getResults(qualifier), hasItem(searchResult.result));
		}
	}

	@Theory
	public void ignoredItems_notCounted(Qualifications qualifier, ActualResultLists ignoredItems) {
		List<SearchResult> searchResults = createIgnoredSearchResults(qualifier, ignoredItems);
		
		ResultComparison testee = ResultComparison.compare(searchResults, ExpectedResultsCreators.ALL.create(this));
		
		assertThat(testee.getCount(qualifier), equalTo(0));
	}

	@Theory
	public void ignoredItems_emptyResults(Qualifications qualifier, ActualResultLists ignoredItems) {
		List<SearchResult> searchResults = createIgnoredSearchResults(qualifier, ignoredItems);
		
		ResultComparison testee = ResultComparison.compare(searchResults, ExpectedResultsCreators.ALL.create(this));
		
		assertThat(testee.getResults(qualifier), empty());
	}

	@Ignore
	@Test
	public void setContainingExpected_notCounted1() {
		ResultComparison testee = ResultComparison.compare(ActualResultLists.SINGLE_1_SET_WEAK_EXPECTED.result, ExpectedResultsCreators.ALL.create(this));

		assertThat(testee.getCount(Qualifications.ACTUAL_DIFFERING_FROM_EXPECTED), is(equalTo(0)));
	}

	@Ignore
	@Test
	public void setContainingExpected_notCounted2() {
		ResultComparison testee = ResultComparison.compare(ActualResultLists.SINGLE_2_SET_WEAK_EXPECTED.result, ExpectedResultsCreators.ALL.create(this));

		assertThat(testee.getCount(Qualifications.ACTUAL_DIFFERING_FROM_EXPECTED), is(equalTo(0)));
	}

	private List<SearchResult> createSearchResults(Qualifications qualifier, ActualResultLists countedItems, ActualResultLists otherItems) {
		assumeThat(qualifier, isIn(SEARCH_RESULT_LIST_CLASSES.keySet()));
		assumeThat(countedItems, isIn(SEARCH_RESULT_LIST_CLASSES.get(qualifier).counted));
		assumeThat(otherItems, isIn(SEARCH_RESULT_LIST_CLASSES.get(qualifier).ignored));

		List<SearchResult> searchResults = new ArrayList<>(countedItems.result.size() + otherItems.result.size());
		searchResults.addAll(countedItems.result);
		searchResults.addAll(otherItems.result);
		Collections.shuffle(searchResults);

		return searchResults;
	}

	private List<SearchResult> createIgnoredSearchResults(Qualifications qualifier, ActualResultLists ignoredItems) {
		assumeThat(qualifier, isIn(SEARCH_RESULT_LIST_CLASSES.keySet()));
		assumeThat(ignoredItems, isIn(SEARCH_RESULT_LIST_CLASSES.get(qualifier).ignored));
		
		List<SearchResult> ignoredResults = new ArrayList<>(ignoredItems.result.size());
		ignoredResults.addAll(ignoredItems.result);
		Collections.shuffle(ignoredResults);
		
		return ignoredResults;
	}
}