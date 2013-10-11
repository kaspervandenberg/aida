// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation.matchers;

import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class SearchResultDelegatingToPatisNumberMatcher extends TypeSafeDiagnosingMatcher<SearchResult> {
	private final Matcher<? super PatisNumber> delegate;

	public SearchResultDelegatingToPatisNumberMatcher(Matcher<? super PatisNumber> delegate_) {
		this.delegate = delegate_;
	}

	@Override
	protected boolean matchesSafely(SearchResult item, Description mismatchDescription) {
		mismatchDescription.appendText("result's patisnumber");
		delegate.describeMismatch(item.getPatient(), mismatchDescription);

		return delegate.matches(item.getPatient());
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("having patisnumber ");
		description.appendDescriptionOf(delegate);
	}
}
