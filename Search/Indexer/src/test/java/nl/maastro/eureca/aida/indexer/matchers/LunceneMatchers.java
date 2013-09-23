// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.matchers;

import org.apache.lucene.index.IndexableField;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * A collection of {@link org.hamcrest.Matcher}s to test Lucene fields and documents
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class LunceneMatchers {

	/**
	 *
	 * @param targetValue the value of targetValue
	 */
	public static Matcher<IndexableField> fieldValue(final String targetValue) {
		return new TypeSafeDiagnosingMatcher<IndexableField>() {
			@Override
			protected boolean matchesSafely(IndexableField item, Description mismatchDescription) {
				return targetValue.equals(item.stringValue());
			}

			@Override
			public void describeTo(Description description) {
				description.appendText(String.format("field with value %s", targetValue));
			}
		};
	}

	/**
	 *
	 * @param targetName the value of targetName
	 */
	public static Matcher<IndexableField> fieldNamed(final String targetName) {
		return new TypeSafeDiagnosingMatcher<IndexableField>() {
			@Override
			protected boolean matchesSafely(IndexableField item, Description mismatchDescription) {
				return targetName.equals(item.name());
			}

			@Override
			public void describeTo(Description description) {
				description.appendText(String.format("field named %s", targetName));
			}
		};
	}
	
}
