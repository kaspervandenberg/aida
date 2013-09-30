// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

import java.util.concurrent.Callable;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface ConcurrentTestContext<T> {
	public Blocking<? extends T> getTask(int taskId);
	public void submit(int taskId, Callable<? extends T> task);
}
