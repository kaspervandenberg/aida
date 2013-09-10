// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for threads support cancelation; for example an infinite loop in a thread that runs until {@link #cancel() cancled}.
 * Derived classes should expect {@link InterruptedException} and should stop when {@link #isCancelled()} returns true.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public abstract class CancelableTask extends Thread {
	/**
	 * Flag set when {@link #cancel()} was called.
	 * 
	 * @see #isCancelled()
	 * @see #cancel()
	 */
	private final AtomicBoolean canceled = new AtomicBoolean(false);

	public CancelableTask() {
	}

	public CancelableTask(Runnable target) {
		super(target);
	}

	public CancelableTask(ThreadGroup group, Runnable target) {
		super(group, target);
	}

	public CancelableTask(String name) {
		super(name);
	}

	public CancelableTask(ThreadGroup group, String name) {
		super(group, name);
	}

	public CancelableTask(Runnable target, String name) {
		super(target, name);
	}

	public CancelableTask(ThreadGroup group, Runnable target, String name) {
		super(group, target, name);
	}

	public CancelableTask(ThreadGroup group, Runnable target, String name, long stackSize) {
		super(group, target, name, stackSize);
	}
	
	
	
	/**
	 * Indicate this thread should stop.  The thread receives an {@link InterruptedException} and {@link #isCancelled()} will 
	 * return true; the derived class is responsible for actually stopping.
	 */
	public void cancel() {
		canceled.set(true);
		this.interrupt();
	}

	/**
	 * Derived threads should regularly check {@code isCancelled} and stop when {@code isCancelled} returns {@code true}.
	 * 
	 * @return <ul><li>{@code true}, when the thread was canceled;</li>
	 * 			<li>{@code false}, when the thread is allowed to continue.</li></ul>
	 */
	public boolean isCancelled() {
		return canceled.get();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>{@code start()} allows restarting a Thread by setting {@link #isCancelled()} to false.</p>
	 */
	@Override
	public synchronized void start() {
		canceled.set(false);
		super.start();
	}
}
