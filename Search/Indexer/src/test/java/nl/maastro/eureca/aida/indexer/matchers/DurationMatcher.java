/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.matchers;

import java.util.concurrent.TimeUnit;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.DurationMeasurement;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 *
 * @author kasper
 */
public class DurationMatcher extends TypeSafeDiagnosingMatcher<DurationMeasurement> {
	private final long timeLimit;
	private final TimeUnit unit;

	public DurationMatcher(long timeLimit_, TimeUnit unit_) {
		this.timeLimit = timeLimit_;
		this.unit = unit_;
	}
	
	
	public static Matcher<DurationMeasurement> within(long timeLimit, TimeUnit unit) {
		return new DurationMatcher(timeLimit, unit);
	}

	@Override
	protected boolean matchesSafely(DurationMeasurement item, Description mismatchDescription) {
		return item.getDuration(unit) <= timeLimit;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("measured duration within limit of ")
				.appendValue(timeLimit) .appendValue(unit);
	}
}
