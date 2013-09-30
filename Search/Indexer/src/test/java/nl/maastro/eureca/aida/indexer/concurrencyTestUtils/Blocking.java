/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author kasper
 */
public class Blocking<T> implements Callable<T> {
	public enum State {
		INITIAL,
		CALLED,
		RUNNING,
		FINISHED,
		TERMINATED
	}
	
	private final Callable<T> delegate;
	private final Semaphore start = new Semaphore(0);
	private final Semaphore end = new Semaphore(0);
	private final AtomicBoolean startEnabled = new AtomicBoolean(false);
	private final AtomicBoolean endEnabled = new AtomicBoolean(false);
	private final ReadWriteLock stateModificationLock = new ReentrantReadWriteLock();
	private State state = State.INITIAL;

	public Blocking(Callable<T> delegate_) {
		this.delegate = delegate_;
	}

	public static <T> Blocking<T> wrap(Callable<T> task) {
		return new Blocking<>(task);
	}

	public static <T> Blocking<T> createDummy(final T result) {
		final Callable<T> nop = new Callable<T>() {
			@Override
			public T call() throws Exception {
				return result;
			}
		};
		return new Blocking<>(nop);
	}

	@Override
	public T call() throws Exception {
				progressTo(State.CALLED);
		start.acquire();
				progressTo(State.RUNNING);
		T result = delegate.call();
				progressTo(State.FINISHED);
		end.acquire();
				progressTo(State.TERMINATED);
		return result;
	}
	
	public void enableStart() {
		startEnabled.set(true);
		start.release();
	}

	public void enableEnd() {
		endEnabled.set(true);
		end.release();
	}

	public State currentState() {
		try {
			stateModificationLock.readLock().lock();
			return state;
		} finally {
			stateModificationLock.readLock().unlock();
		}
	}

	public boolean canStart() {
		return startEnabled.get();
	}

	public boolean  canEnd() {
		return endEnabled.get();
	}

	private void progressTo(State next) {
		try {
			stateModificationLock.writeLock().lock();
			state = next;
		} finally {
			stateModificationLock.writeLock().unlock();
		}
	}
}
