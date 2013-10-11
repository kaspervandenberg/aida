package nl.maastro.eureca.aida.search.zylabpatisclient.validation.matchers;

import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedResults;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 *
 */
public class SearchResultsAsExpectedMatcher extends TypeSafeDiagnosingMatcher<SearchResult> {
	private final ExpectedResults expectation;

	public SearchResultsAsExpectedMatcher(ExpectedResults expectation_) {
		this.expectation = expectation_;
	}

	@Override
	protected boolean matchesSafely(SearchResult item, Description mismatchDescription) {
		appendMismatchDescription(item, mismatchDescription);
		return expectation.isAsExpected(item);
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("expected classification of ");
		description.appendValue(expectation.getDefinedPatients());
	}

	private void appendMismatchDescription(SearchResult item, Description mismatchDescription) {
		if(isExpectationDefined(item)) {
			appendOtherClassificationExpected(item, mismatchDescription);
		} else {
			appendNoExpectedClassification(item, mismatchDescription);
		}
	}

	private boolean isExpectationDefined(SearchResult item) {
		return expectation.isInDefined(item.getPatient());
	}

	private void appendOtherClassificationExpected(SearchResult item, Description mismatchDescription) {
		mismatchDescription.appendValue(item);
		mismatchDescription.appendText(" classified as ");
		mismatchDescription.appendValue(item.getClassification());
		mismatchDescription.appendText(" expected ");
		mismatchDescription.appendValue(expectation.getClassification(item.getPatient()));
	}

	private void appendNoExpectedClassification(SearchResult item, Description mismatchDescription) {
		mismatchDescription.appendText("No expected classification defined for ");
		mismatchDescription.appendValue(item);
	}
}
