/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 *
 * @author kasper
 */
public class DummyObserverBase<TEvent> {
	private final ReadWriteLock eventLock = new ReentrantReadWriteLock();
	private final List<TEvent> eventsReceived = new LinkedList<>();

	public static <TEvent> Matcher<DummyObserverBase<TEvent>> receivedAnyEvent(final Matcher<TEvent> inner) {
		return new TypeSafeDiagnosingMatcher<DummyObserverBase<TEvent>>() {
			@Override
			protected boolean matchesSafely(DummyObserverBase<TEvent> item, Description mismatchDescription) {
				return item.applyMatcher(inner);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("received any event ").appendDescriptionOf(inner);
			}
		};
	}

	protected void add(TEvent event) {
		try {
			eventLock.writeLock().lock();
			eventsReceived.add(event);
		} finally {
			eventLock.writeLock().unlock();
		}
	}

	protected boolean applyMatcher(Matcher<TEvent> inner) {
		try {
			eventLock.readLock().lock();
			return Matchers.hasItem(inner).matches(eventsReceived);
		} finally {
			eventLock.readLock().unlock();
		}
	}

	public List<TEvent> getEventsReceived() {
		try {
			eventLock.readLock().lock();
			return new ArrayList<>(eventsReceived);
		} finally {
			eventLock.readLock().unlock();
		}
	}
	
}
