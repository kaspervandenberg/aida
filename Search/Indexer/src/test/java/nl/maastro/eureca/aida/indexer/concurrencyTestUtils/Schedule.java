/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

/**
 *
 * @author kasper
 */
public class Schedule<T> implements Action<T> {
	final int taskNr;

	public Schedule(int taskNr_) {
		this.taskNr = taskNr_;
	}

	@Override
	public void execute(ConcurrentTestContext<T> context) {
		context.submit(taskNr, context.getTask(taskNr));
	}

	@Override
	public boolean appliesTo(int target) {
		return this.taskNr == target;
	}
	
}
