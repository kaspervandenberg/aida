// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import nl.maastro.eureca.aida.indexer.util.ObserverCollection;

/**
 * A {@link FutureTask} that notifiies its {@link CompletionObserver}s, when it is finished.
 * 
 * @see ObservableExecutorService
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ObservableTask<TEventSource, TResult> extends FutureTask<TResult> {
	private final ObserverCollection<CompletionObserver<TResult>, ?> observers;

	public ObservableTask(ObserverCollection<CompletionObserver<TResult>, ?> observers_, Runnable inner, TResult result) {
		super(inner, result);
		this.observers = observers_;
	}

	public ObservableTask(ObserverCollection<CompletionObserver<TResult>, ?> observers_, Callable<TResult> inner) {
		super(inner);
		this.observers = observers_;
	}

	public boolean subscribe(CompletionObserver<TResult> observer) {
		return observers.add(observer);
	}

	public boolean unsubscribe(CompletionObserver<TResult> observer) {
		return observers.remove(observer);
	}

	@Override
	protected void done() {
		observers.fireEvent(this);
	}
	
}
