package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.ConcurrentTestContext;

/**
 *
 * @author kasper
 */
public interface Action<T> {

	public void execute(ConcurrentTestContext<T> context);

	public boolean appliesTo(int target);
	
}
