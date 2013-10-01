/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import nl.maastro.eureca.aida.indexer.concurrent.ObservableExecutorService;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 *
 * @author kasper
 */
public class DummyExecutorObserver<T> implements ObservableExecutorService.CompletionObserver<T> {
	private List<ExecutorEvent<T>> eventsReceived = new LinkedList<>();
	private ReadWriteLock eventLock = new ReentrantReadWriteLock();

	@Override
	public void taskFinished(ObservableExecutorService source, Future<T> task) {
		ExecutorEvent<T> receivedEvent = ExecutorEvent.register(source, task);
		add(receivedEvent);
	}

	public static <T> Matcher<DummyExecutorObserver<T>> receivedAnyEvent(final Matcher<ExecutorEvent<T>> inner) {
		return new TypeSafeDiagnosingMatcher<DummyExecutorObserver<T>>() {
			@Override
			protected boolean matchesSafely(DummyExecutorObserver<T> item, Description mismatchDescription) {
				return item.applyMatcher(inner);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("received any event ").appendDescriptionOf(inner);
			}
		};
	}

	public List<ExecutorEvent<T>> getEventsReceived() {
		try {
			eventLock.readLock().lock();
			return new ArrayList<>(eventsReceived);
		} finally {
			eventLock.readLock().unlock();
		}
	}

	private void add(ExecutorEvent<T> event) {
		try {
			eventLock.writeLock().lock();
			eventsReceived.add(event);
		} finally {
			eventLock.writeLock().unlock();
		}
	}

	private boolean applyMatcher(Matcher<ExecutorEvent<T>> inner) {
		try {
			eventLock.readLock().lock();
			return Matchers.hasItem(inner).matches(eventsReceived);
		} finally {
			eventLock.readLock().unlock();
		}
	}
	
}
