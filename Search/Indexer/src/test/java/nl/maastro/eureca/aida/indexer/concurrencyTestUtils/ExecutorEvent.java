/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import nl.maastro.eureca.aida.indexer.concurrent.ObservableExecutorService;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Assume;

/**
 *
 * @author kasper
 */
public class ExecutorEvent<T> {
	private ObservableExecutorService source;
	private Future<T> task;
	private T value;
	private DurationMeasurement getDuration;

	public ExecutorEvent(ObservableExecutorService source_, Future<T> task_, T value_, DurationMeasurement getDuration_) {
		this.source = source_;
		this.task = task_;
		this.value = value_;
		this.getDuration = getDuration_;
	}

	public static <T> ExecutorEvent<T> register(ObservableExecutorService source, Future<T> task) {
		try {
			DurationMeasurement stopwatch = new DurationMeasurement();
			stopwatch.start();
			T value = task.get();
			stopwatch.stop();
			return new ExecutorEvent<>(source, task, value, stopwatch);
		} catch (ExecutionException | InterruptedException ex) {
			Assume.assumeNoException(ex);
			throw new Error("Should not reach this line");
		}
	}

	public static <T> Matcher<ExecutorEvent<T>> withValue(final T expected) {
		return new TypeSafeDiagnosingMatcher<ExecutorEvent<T>>() {
			@Override
			protected boolean matchesSafely(ExecutorEvent<T> item, Description mismatchDescription) {
				return item.getValue().equals(expected);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("with value ");
				description.appendValue(expected);
			}
		};
	}
	
	private T getValue() {
		return value;
	}
	
}
