// © Maastro Clinic, 2014
package nl.maastro.eureca.aida.velocityrdf.n3_id_translator;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

class Matchers<T> {
	private final Translator<T> testee;

	public Matchers(Translator<T> testee_)
	{
		this.testee = testee_;
	}

	
	public Matcher<T> matchesIdentifier(final String id)
	{
		return new TypeSafeMatcher<T>() {
			@Override
			protected boolean matchesSafely(T item) {
				return testee.matches(item, id);
			}

			@Override
			public void describeTo(Description description) {
				description.appendValue(testee);
				description.appendText(".matches identifier ");
				description.appendValue(id);
			}
		};
	}


	public Matcher<String> isWellformedIdentifier()
	{
		return new TypeSafeMatcher<String>() {
			@Override
			protected boolean matchesSafely(String item) {
				return testee.isWellFormed(item);
			}

			@Override
			public void describeTo(Description description) {
				description.appendValue(testee);
				description.appendText(".isWellFormed (identifier) ");
			}
		};
	}

}

/* vim:set tabstop=4 shiftwidth=4 autoindent textwidth=80 : */

