// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Create and submit task to an ExecutorService.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class TaskSubmitter <TInput, TResult> 
{
	/**
	 * Interface for generating tasks based on an input.
	 * 
	 * @param <TInput>	the type of input argument
	 * @param <TResult>		the result type of the generated task
	 */
	public interface TaskGenerator<TInput, TResult>
	{
		/**
		 * Use {@code arg} to generate a callable.
		 * @param input	this generator can use it for creating the task.
		 * 
		 * @return		a callable that {@link TaskSubmittingTask} will
		 * 			{@link ExecutorService#submit(java.util.concurrent.Callable)
		 * 			submit} to {@link TaskSubmitter#target}.
		 */
		public Callable<TResult> generate(TInput input);
	}

	
	/**
	 * The {@link ExecutorService} to which to {@link 
	 * ExecutorService#submit(java.util.concurrent.Callable) submit} the
	 * {@link TaskGenerator#generate(java.lang.Object) generated} tasks.
	 */
	private final ExecutorService target;


	/**
	 * Functor that generates tasks.
	 */
	private final TaskGenerator<TInput, TResult> taskGenerator;


	/**
	 * Constructor
	 * 
	 * @param target_ 	{@link ExecutorService} to which to {@link 
	 * 				ExecutorService#submit(java.util.concurrent.Callable) 
	 * 				submit} the {@link TaskGenerator#generate(java.lang.Object)
	 * 				generated} tasks.
	 * 
	 * @param generator_	functor that generates tasks. 
	 */
	public TaskSubmitter(ExecutorService target_, TaskGenerator<TInput, TResult> generator_) {
		this.target = target_;
		this.taskGenerator = generator_;
	}


	/**
	 * <nl><li>Use {@link #taskGenerator} to {@link 
	 * 		TaskGenerator#generate(java.lang.Object) generate} a task; and</li>
	 * <li>{@link ExecutorService#submit(java.util.concurrent.Callable) submit}
	 * 		this task to {@link #target}.</li></nl>
	 * 
	 * @param input		supplied to {@code TaskGenerator.generate}
	 * 
	 * @return 		The future that {@code target} returns.
	 */
	public Future<TResult> createAndSubmit(TInput input) {
		Callable<TResult> freshTask = taskGenerator.generate(input);
		return target.submit(freshTask);
	}
};
