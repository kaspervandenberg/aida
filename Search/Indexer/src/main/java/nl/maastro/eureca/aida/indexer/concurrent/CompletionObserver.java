/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrent;

import java.util.concurrent.Future;

/**
 * Observer pattern: interface required of observers.
 *
 * @param <T>
 */
public interface CompletionObserver<T> {

	public void taskFinished(ObservableExecutorService source, Future<T> task);
	
}
