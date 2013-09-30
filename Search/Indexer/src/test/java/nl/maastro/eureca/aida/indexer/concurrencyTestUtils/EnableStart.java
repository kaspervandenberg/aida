/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.ConcurrentTestContext;

/**
 *
 * @author kasper
 */
public class EnableStart<T> implements Action<T> {
	final int taskNr;

	public EnableStart(int taskNr_) {
		this.taskNr = taskNr_;
	}

	@Override
	public void execute(ConcurrentTestContext<T> context) {
		context.getTask(taskNr).enableStart();
	}

	@Override
	public boolean appliesTo(int target) {
		return this.taskNr == target;
	}
	
}
