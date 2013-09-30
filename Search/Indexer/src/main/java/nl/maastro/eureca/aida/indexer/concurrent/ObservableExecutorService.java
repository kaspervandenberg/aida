package nl.maastro.eureca.aida.indexer.concurrent;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.maastro.eureca.aida.indexer.util.StaleReferenceFilter;

public class ObservableExecutorService<T> {
	public interface Observer<T> {
		public void taskFinished(ObservableExecutorService<T> source, Future<T> task);
	}
	
	private final CompletionService<T> delegate;
	private final ConcurrentMap<Future<T>, Collection<WeakReference<Observer<T>>>> observers;
	private final ReadWriteLock observerLock;
	private final Thread notifier;
	
	public ObservableExecutorService(ExecutorService delegate_) {
		this.delegate = new ExecutorCompletionService<>(delegate_);
		this.observers = new ConcurrentHashMap<>();
		this.observerLock = new ReentrantReadWriteLock();
		this.notifier = createEventFireThread();
	}
	
	public Future<T> submit(Observer<T> observer, Callable<T> task) {
		try {
			this.observerLock.writeLock().lock();
			Future<T> result = delegate.submit(task);
			addObserver(observer, result);
			return result;
		} finally {
			this.observerLock.writeLock().unlock();
		}
	}

	private Thread createEventFireThread() {
		Thread result = new Thread(new Runnable() {
			@Override
			public void run() {
				for(;;) {
					try {
						Future<T> finishedTask = delegate.take();
						notifyObservers(finishedTask);
					} catch (InterruptedException ex) {
						Logger.getLogger(ObservableExecutorService.class.getName()).log(Level.WARNING, "Received interrupt; ignoring it", ex);
					}
				}
			}
		}, "completedTaskWatcher-" + this.hashCode());
		result.setDaemon(true);
		return result;
	}

	private void notifyObservers(Future<T> future) {
		for (Observer<T> observer : new StaleReferenceFilter<>(getObservers(future), true)) {
			observer.taskFinished(this, future);
		}
	}

	private Collection<WeakReference<Observer<T>>> getObservers(Future<T> future) {
		try {
			this.observerLock.readLock().lock();
			observers.putIfAbsent(future, new ConcurrentLinkedDeque<WeakReference<Observer<T>>>());
			return observers.get(future);
		} finally {
			this.observerLock.readLock().unlock();
		}
	}

	private void addObserver(Observer<T> observer, Future<T> future) {
		getObservers(future).add(new WeakReference<>(observer));
		startNotifierIfNeeded();
	}

	private void startNotifierIfNeeded() {
		if(!notifier.isAlive()) {
			notifier.start();
		}
	}
}
