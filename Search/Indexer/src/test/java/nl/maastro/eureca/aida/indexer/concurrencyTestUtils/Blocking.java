/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

/**
 *
 * @author kasper
 */
public class Blocking<T> implements Callable<T> {
	private final Callable<T> delegate;
	private final Semaphore start = new Semaphore(0);
	private final Semaphore end = new Semaphore(0);

	public Blocking(Callable<T> delegate_) {
		this.delegate = delegate_;
	}

	@Override
	public T call() throws Exception {
		start.acquire();
		T result = delegate.call();
		end.acquire();
		return result;
	}
	
	public void enableStart() {
		start.release();
	}

	public void enableEnd() {
		end.release();
	}
}
