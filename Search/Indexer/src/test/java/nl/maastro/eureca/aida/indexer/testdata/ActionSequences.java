/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.testdata;

import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.ActionSequence;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.ActionSequenceImpl;

import static nl.maastro.eureca.aida.indexer.concurrencyTestUtils.ActionSequenceImpl.Builder;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.ConcurrentTestContext;

/**
 *
 * @author kasper
 */
public enum ActionSequences implements ActionSequence<String> {
	SINGLE_TASK(new Builder<String>() {{ schedule(0); enableStart(0); enableEnd(0); }}),
	TWO_TASKS_INTERLEAVED(new Builder<String>() {{ schedule(0); schedule(1); enableStart(1); enableStart(0); enableEnd(1); enableEnd(0); }})
	;
	
	private final ActionSequence<String> delegate;
	
	private ActionSequences(ActionSequenceImpl.Builder<String> builder) {
		this.delegate = builder.build();
	}

	@Override
	public void execute(ConcurrentTestContext<String> context) {
		delegate.execute(context);
	}

	@Override
	public boolean willFinish(int taskId) {
		return delegate.willFinish(taskId);
	}

	@Override
	public boolean willSchedule(int taskId) {
		return delegate.willSchedule(taskId);
	}

	@Override
	public boolean willStart(int taskId) {
		return delegate.willStart(taskId);
	}
}
