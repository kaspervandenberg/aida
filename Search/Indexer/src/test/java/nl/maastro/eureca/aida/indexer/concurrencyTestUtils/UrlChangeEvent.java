/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

import java.net.URL;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 *
 * @author kasper
 */
public class UrlChangeEvent<T> {
	private final T source;
	private final URL before;
	private final URL after;

	public UrlChangeEvent(T source_, URL before_, URL after_) {
		this.source = source_;
		this.before = before_;
		this.after = after_;
	}

	public static <T> UrlChangeEvent<T> register(T source, URL before, URL after) {
		return new UrlChangeEvent<>(source, before, after);
	}

	public static <T> Matcher<UrlChangeEvent<T>> setTo(final URL expected) {
		return new TypeSafeDiagnosingMatcher<UrlChangeEvent<T>>() {
			@Override
			protected boolean matchesSafely(UrlChangeEvent<T> item, Description mismatchDescription) {
				return item.getAfter().equals(expected);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("set to ");
				description.appendValue(expected);
			}
			
		};
	}

	public URL getAfter() {
		return after;
	}

	public URL getBefore() {
		return before;
	}
}
