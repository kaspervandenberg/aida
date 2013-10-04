// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.util;

import nl.maastro.eureca.aida.indexer.concurrent.CompletionObserver;
import nl.maastro.eureca.aida.indexer.concurrent.ObservableExecutorService;

/**
 * Create {@link ObserverCollection}s as used by 
 * {@link ObservableExecutorService} and {@link ObservableTask}
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 *
 */
public interface ObserverCollectionFactory<TObserver, TSource> {
	public ObserverCollection<TObserver, TSource> createObserverSupport(TSource source); 
		
}
