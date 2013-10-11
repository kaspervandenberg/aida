package nl.maastro.eureca.aida.search.zylabpatisclient.validation.matchers;

import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedResults;
import org.hamcrest.Matcher;
import static org.hamcrest.Matchers.*;

public class SearchResultsMatchers {
	public static Matcher<SearchResult> asExpectedBy(final ExpectedResults expectation) {
		return new SearchResultsAsExpectedMatcher(expectation);
	}	

	public static Matcher<PatisNumber> withExpectationsDefinedBy(final ExpectedResults expectation) {
		return isIn(expectation.getDefinedPatients());
	}

	public static Matcher<SearchResult> withPatisNumer(final Matcher<? super PatisNumber> inner) {
		return new SearchResultDelegatingToPatisNumberMatcher(inner);
	}
	
	public static Matcher<SearchResult> asExpectedOrUndefined(final ExpectedResults expectation) {
		return anyOf(asExpectedBy(expectation),
				not(withPatisNumer(withExpectationsDefinedBy(expectation))));
	}

	public static Matcher<? super Iterable<SearchResult>> containsAllExpectedResultsFrom(final ExpectedResults expectation) {
		return allOf(new HasAllExpectedResults(expectation));
	}
}
