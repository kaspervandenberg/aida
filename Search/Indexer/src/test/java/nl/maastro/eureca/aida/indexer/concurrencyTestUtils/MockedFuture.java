// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

import java.util.concurrent.Future;
import nl.maastro.eureca.aida.indexer.ZylabData;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class MockedFuture {
	@Mock
	public Future<ZylabData> future;
	public boolean finished;

	public MockedFuture() {
		this.finished = false;
	}

	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(future.isCancelled()).thenReturn(false);
		Mockito.when(future.isDone()).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				return finished;
			}
		});
	}
	
}
