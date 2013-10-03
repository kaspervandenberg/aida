// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import nl.maastro.eureca.aida.indexer.ZylabDocument;
import nl.maastro.eureca.aida.indexer.concurrent.CompletionObserver;
import nl.maastro.eureca.aida.indexer.concurrent.ObservableExecutorService;
import org.hamcrest.Matcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.Mockito.*;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class MockedFuture {
	@Mock
	public Future<ZylabDocument> future;
	public final List<CompletionObserver<ZylabDocument>> observers;
	private boolean finished;
	private ObservableExecutorService source;
	private Answer<Future<ZylabDocument>> answer;

	public MockedFuture() {
		this.observers = new ArrayList<>();
		this.finished = false;
	}

	public void setup() {
		MockitoAnnotations.initMocks(this);
		answer = createStoreObserverAnswer(observers, future);
		Mockito.when(future.isCancelled()).thenReturn(false);
		Mockito.when(future.isDone()).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				return finished;
			}
		});
	}

	public void mockExecutorCalls(ObservableExecutorService executor, 
			Matcher<CompletionObserver<ZylabDocument>> conditionOnObserver,
			Matcher<? extends Callable<ZylabDocument>> conditionOnCallable) {
		source = executor;
		when (executor.subscribeAndSubmit(argThat(conditionOnObserver), argThat(conditionOnCallable))) .thenAnswer(answer);
	}

	public void finish() {
		finished = true;
		for (CompletionObserver<ZylabDocument> observer : observers) {
			observer.taskFinished(source, future);
		}
	}
	
	private static <T> Answer<T> createStoreObserverAnswer(
			final List<CompletionObserver<ZylabDocument>> observerRef, final T result) {
		return new Answer<T>() {
			@Override
			@SuppressWarnings("unchecked")
			public T answer(InvocationOnMock invocation) throws Throwable {
				Object obj_observer = invocation.getArguments()[0];
				if(obj_observer instanceof CompletionObserver) {
					observerRef.add((CompletionObserver<ZylabDocument>)obj_observer);
				} else {
					throw new IllegalArgumentException("First argument is not a CompletionObserver");
				}
				return result;
			}
		};
	}
	
}
