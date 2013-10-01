// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.concurrent;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.maastro.eureca.aida.indexer.util.ObserverCollection;
import nl.maastro.eureca.aida.indexer.util.StaleReferenceFilter;

/**
 * ExecutorService that notifies the registered {@link Observers} when a submitted task completes.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 * 
 */
public class ObservableExecutorService extends AbstractExecutorService implements ExecutorService {
	/**
	 * Observer pattern: interface required of observers.
	 * 
	 * @param <T> 
	 */
	public interface CompletionObserver<T> {
		public void taskFinished(ObservableExecutorService source, Future<T> task);
	}

	private class Task<T> extends FutureTask<T> {
		private final ObserverCollection<CompletionObserver<T>, ObservableExecutorService> observers;

		@SuppressWarnings("unchecked")
		public Task(Runnable inner, T result) {
			super(inner, result);
			this.observers = createObserverSupport();
		}

		public Task(Callable<T> inner) {
			super(inner);
			this.observers = createObserverSupport();
		}

		public Task(Collection<CompletionObserver<T>> initialObservers, Runnable inner, T result) {
			super(inner, result);
			this.observers = createObserverSupport();
			observers.addAll(initialObservers);
		}

		public Task(Collection<CompletionObserver<T>> initialObservers, Callable<T> inner) {
			super(inner);
			this.observers = createObserverSupport();
			observers.addAll(initialObservers);
		}

		public boolean subscribe(CompletionObserver<T> observer) {
			return observers.add(observer);
		}

		public boolean unsubscribe(CompletionObserver<T> observer) {
			return observers.remove(observer);
		}

		@SuppressWarnings("unchecked")
		private ObserverCollection<CompletionObserver<T>, ObservableExecutorService> createObserverSupport() {
			return new ObserverCollection<>(ObservableExecutorService.this,
					(Class<CompletionObserver<T>>)(Object)CompletionObserver.class, OBSERVER_METHOD);
		}

		@Override
		protected void done() {
			observers.fireEvent(this);
		}
	}

	private static final Method OBSERVER_METHOD;
	static {
		try {
			OBSERVER_METHOD = CompletionObserver.class.getDeclaredMethod("taskFinished", ObservableExecutorService.class, Future.class);
		} catch (NoSuchMethodException | SecurityException ex) {
			throw new Error("Error initialising OBSERVER_METHOD", ex);
		}
	}

	private final ExecutorService delegate;
	
	public ObservableExecutorService(ExecutorService delegate_) {
		this.delegate = delegate_;
	}

	public <T> Future<T> subscribeAndSubmit(CompletionObserver<T> observer, Callable<T> task) {
		return subscribeAndSubmit(Collections.singleton(observer), task);
	}
	
	public <T> Future<T> subscribeAndSubmit(Collection<CompletionObserver<T>> observers, Callable<T> task) {
		Task<T> decoratedTask = new Task<>(observers, task);
		delegate.execute(decoratedTask);
		return decoratedTask;
	}
	
	
	public <T> boolean subscribe(CompletionObserver<T> observer, Future<T> task) {
		if(task instanceof Task) {
			return subscribeToTask(observer, (Task<T>)task);
		} else {
			return false;
		}
	}

	private <T> boolean subscribeToTask(CompletionObserver<T> observer, Task<T> task) {
		return task.subscribe(observer);
	}

	public <T> boolean unsubscribe(CompletionObserver<T> observer, Future<T> task) {
		if(task instanceof Task) {
			return unsubscribetoTask(observer, (Task<T>)task);
		} else {
			return false;
		}
	}
	
	private <T> boolean unsubscribetoTask(CompletionObserver<T> observer, Task<T> task) {
		return task.unsubscribe(observer);
	}
	
	@Override
	public void execute(Runnable command) {
		if(command instanceof Task) {
			delegate.execute(command);
		} else {
			delegate.execute(newTaskFor(command, null));
		}
	}
	
	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
		return new Task<>(runnable, value);
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		return new Task<>(callable);
	}

	@Override
	public void shutdown() {
		delegate.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return delegate.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return delegate.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return delegate.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return delegate.awaitTermination(timeout, unit);
	}
}
