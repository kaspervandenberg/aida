// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.util;

import java.lang.reflect.Method;
import java.util.concurrent.Future;
import nl.maastro.eureca.aida.indexer.concurrent.CompletionObserver;
import nl.maastro.eureca.aida.indexer.concurrent.ObservableExecutorService;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DefaultObserverCollectionFactory implements ObserverCollectionFactory {
	private static final Method OBSERVER_METHOD;
	static {
		try {
			OBSERVER_METHOD = CompletionObserver.class.getDeclaredMethod("taskFinished", ObservableExecutorService.class, Future.class);
		} catch (NoSuchMethodException | SecurityException ex) {
			throw new Error("Error initialising OBSERVER_METHOD", ex);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> ObserverCollection<CompletionObserver<T>, ObservableExecutorService> createObserverSupport(ObservableExecutorService source) {
		return new ObserverCollection<>(source, (Class<CompletionObserver<T>>)(Object)CompletionObserver.class, OBSERVER_METHOD);
	}
	
}
