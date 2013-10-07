/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

import java.util.concurrent.Future;
import nl.maastro.eureca.aida.indexer.concurrent.CompletionObserver;
import nl.maastro.eureca.aida.indexer.concurrent.ObservableExecutorService;

/**
 *
 * @author kasper
 */
public class DummyExecutorObserver<T> extends DummyObserverBase<ExecutorEvent<T>> implements CompletionObserver<T> {

	@Override
	public void taskFinished(ObservableExecutorService source, Future<T> task) {
		ExecutorEvent<T> receivedEvent = ExecutorEvent.register(source, task);
		add(receivedEvent);
	}
	
}
