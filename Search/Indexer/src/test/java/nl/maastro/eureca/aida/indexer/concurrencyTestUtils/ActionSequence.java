/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;


/**
 *
 * @author kasper
 */
public interface ActionSequence<T> {

	void execute(ConcurrentTestContext<T> context);

	boolean willFinish(int taskId);

	boolean willSchedule(int taskId);

	boolean willStart(int taskId);
	
}
