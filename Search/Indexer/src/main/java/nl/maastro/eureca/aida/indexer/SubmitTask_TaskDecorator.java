// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Submit a task on completion of the decorated task.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class SubmitTask_TaskDecorator<TDelegate, TResult>
		implements Callable<Future<TResult>>
{
	/**
	 * The first task to call.
	 */
	private final Callable<TDelegate> delegate;


	/**
	 * The {@link TaskSubmitter} to use to {@link 
	 * TaskSubmitter#createAndSubmit(java.lang.Object) create and submit}
	 * the second task.
	 */
	private final TaskSubmitter<TDelegate, TResult> submitter;


	/**
	 * Constructor
	 * 
	 * @param delegate_		first task to call.
	 * @param submitter_	{@link TaskSubmitter} to use to {@link 
	 * 		TaskSubmitter#createAndSubmit(java.lang.Object) create and submit}
	 *		the second task.
	 */
	public SubmitTask_TaskDecorator(Callable<TDelegate> delegate_,
			TaskSubmitter<TDelegate, TResult> submitter_)
	{
		this.delegate = delegate_;
		this.submitter = submitter_;
	}
	
	@Override
	public Future<TResult> call() throws Exception
	{
		TDelegate intermediateResult = delegate.call();
		return submitter.createAndSubmit(intermediateResult);
	}
}
