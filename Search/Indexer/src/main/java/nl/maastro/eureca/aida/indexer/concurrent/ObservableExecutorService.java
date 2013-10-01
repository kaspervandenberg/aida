// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.concurrent;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import nl.maastro.eureca.aida.indexer.util.ObserverCollection;
import nl.maastro.eureca.aida.indexer.util.ObserverCollectionFactory;

/**
 * ExecutorService that notifies the registered {@link Observers} when a submitted task completes.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 * 
 */
public class ObservableExecutorService extends AbstractExecutorService implements ExecutorService {

	private final ExecutorService delegate;
	private final ObserverCollectionFactory observerCollectionFactory;
	
	public ObservableExecutorService(ObserverCollectionFactory observerCollectionFactory_, ExecutorService delegate_) {
		this.delegate = delegate_;
		this.observerCollectionFactory = observerCollectionFactory_;
	}

	public <T> Future<T> subscribeAndSubmit(CompletionObserver<T> observer, Callable<T> task) {
		return subscribeAndSubmit(Collections.singleton(observer), task);
	}
	
	public <T> Future<T> subscribeAndSubmit(Collection<CompletionObserver<T>> observers, Callable<T> task) {
		ObservableTask<ObservableExecutorService, T> decoratedTask = newTaskFor(observers, task);
		delegate.execute(decoratedTask);
		return decoratedTask;
	}
	
	
	@SuppressWarnings("unchecked")
	public <T> boolean subscribe(CompletionObserver<T> observer, Future<T> task) {
		if(task instanceof ObservableTask) {
			return subscribeToTask(observer, (ObservableTask<ObservableExecutorService, T>)task);
		} else {
			return false;
		}
	}

	private <T> boolean subscribeToTask(CompletionObserver<T> observer, ObservableTask<ObservableExecutorService, T> task) {
		return task.subscribe(observer);
	}

	@SuppressWarnings("unchecked")
	public <T> boolean unsubscribe(CompletionObserver<T> observer, Future<T> task) {
		if(task instanceof ObservableTask) {
			return unsubscribetoTask(observer, (ObservableTask<ObservableExecutorService, T>)task);
		} else {
			return false;
		}
	}
	
	private <T> boolean unsubscribetoTask(CompletionObserver<T> observer, ObservableTask<ObservableExecutorService, T> task) {
		return task.unsubscribe(observer);
	}
	
	@Override
	public void execute(Runnable command) {
		if(command instanceof ObservableTask) {
			delegate.execute(command);
		} else {
			delegate.execute(newTaskFor(command, null));
		}
	}
	
	@Override
	protected <T> ObservableTask<ObservableExecutorService, T> newTaskFor(Runnable runnable, T value) {
		return newTaskFor(Collections.<CompletionObserver<T>>emptyList(), runnable, value);
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		return newTaskFor(Collections.<CompletionObserver<T>>emptyList(), callable);
	}

	private <T> ObservableTask<ObservableExecutorService, T> newTaskFor(
			Collection<CompletionObserver<T>> initialObservers, Runnable runnable, T value) {
		ObserverCollection<CompletionObserver<T>, ObservableExecutorService> observerSupport =
				createObserverSupport(initialObservers);
		return new ObservableTask<>(observerSupport, runnable, value);
	}

	private <T> ObservableTask<ObservableExecutorService, T> newTaskFor(
			Collection<CompletionObserver<T>> initialObservers, Callable<T> callable) {
		ObserverCollection<CompletionObserver<T>, ObservableExecutorService> observerSupport =
				createObserverSupport(initialObservers);
		return new ObservableTask<>(observerSupport, callable);
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

	@SuppressWarnings(value = "unchecked")
	private <T> ObserverCollection<CompletionObserver<T>, ObservableExecutorService> createObserverSupport(
			Collection<CompletionObserver<T>> initialObservers) {
		ObserverCollection<CompletionObserver<T>, ObservableExecutorService> result = observerCollectionFactory.<T>createObserverSupport(this);
		result.addAll(initialObservers);
		return result;
	}

}
