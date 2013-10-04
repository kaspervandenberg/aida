/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrent;

import java.util.concurrent.Future;
import nl.maastro.eureca.aida.indexer.util.ObserverCollection;

/**
 * Observer pattern: interface required of observers.
 *
 * @param <T>
 */
@ObserverCollection.Observer
public interface CompletionObserver<T> {

	@ObserverCollection.NotifyMethod
	public void taskFinished(ObservableExecutorService source, Future<T> task);
	
}
