/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrent;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.Blocking;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.DummyExecutorObserver;
import nl.maastro.eureca.aida.indexer.util.CompletionObserverCollectionFactory;
import nl.maastro.eureca.aida.indexer.util.ObserverCollection;
import nl.maastro.eureca.aida.indexer.util.ObserverCollectionFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.InOrder;
import static org.mockito.Mockito.*;

/**
 *
 * @author kasper
 */
public class ObservableExecutorServiceTestMulti {
	private final static int WAIT_TIME_LIMIT = 1000;
	private final static int N_RUNS = 10;
	
	@Test
	public void testScheduleReleaseBlockingTaskOnce() {
		Blocking<String> task = Blocking.createDummy("task1-scheduleReleaseBlocking");
		DummyExecutorObserver<String> observer = new DummyExecutorObserver<>();
		ObservableExecutorService testee = new ObservableExecutorService(new CompletionObserverCollectionFactory(), Executors.newSingleThreadExecutor());
		
		Future<String> future = scheduleAndRelease(testee, observer, task);
		waitUntilFinished(future);
	}
	
	/**
	 * Test identical to testScheduleReleaseBlockingTask1, since failures in ObservableExecutorServiceTest appear only when 
	 * multiple tests are run. 
	 */
	@Test
	public void testScheduleReleaseBlockingTaskTwice() {
		testScheduleReleaseBlockingTaskOnce();
		testScheduleReleaseBlockingTaskOnce();
	}

	@Test
	public void testReinitialise() {
		Blocking<String> task;
		DummyExecutorObserver<String> observer;
		ObservableExecutorService testee;
		
		for (int i = 0; i < N_RUNS; i++) {
			task = Blocking.createDummy("task2a-reinitialise");
			observer = new DummyExecutorObserver<>();
			testee = new ObservableExecutorService(new CompletionObserverCollectionFactory(), Executors.newSingleThreadExecutor());
			
			Future<String> futureResult = scheduleAndRelease(testee, observer, task);
			waitUntilFinished(futureResult);
		}
	}

	@Test
	public void testAddObserverBeforeSubmit() {
		Blocking<String> task;
		DummyExecutorObserver<String> observer;
		ObservableExecutorService testee;
		
		for (int i = 0; i < N_RUNS; i++) {
			task = Blocking.createDummy("task3-addBeforeSubmit");
			observer = new DummyExecutorObserver<>();
			ObserverCollectionFactory factory = mock(ObserverCollectionFactory.class);
			@SuppressWarnings("unchecked")
			ObserverCollection<CompletionObserver<String>, ObservableExecutorService> obsCollection = mock(ObserverCollection.class);
			when (factory.<String>createObserverSupport(any(ObservableExecutorService.class))) .thenReturn(obsCollection);
			ExecutorService executor = mock(ExecutorService.class);
			
			testee = new ObservableExecutorService(factory, executor);
			testee.subscribeAndSubmit(observer, task);
			
			InOrder inOrder = inOrder(obsCollection, executor);
			inOrder.verify(obsCollection) .addAll(any(Collection.class));
			inOrder.verify(executor) .execute(any(Runnable.class));
			inOrder.verifyNoMoreInteractions();
		}
	}

	@Test
	public void testTaskSeesObserver() {
//		fail("Not yet implemented");
	}

	private static <T> Future<T> scheduleAndRelease(ObservableExecutorService testee, 
			CompletionObserver<T> observer, Blocking<T> task) {
		Future<T> result = testee.subscribeAndSubmit(observer, task);
		task.enableStart();
		task.enableEnd();
		return result;
	}

	private static void waitUntilFinished(Future<?> future) {
		try {
			future.get(WAIT_TIME_LIMIT, TimeUnit.MILLISECONDS);
		} catch (ExecutionException | InterruptedException | TimeoutException ex) {
			ex.printStackTrace();
			fail("Exception while waiting for future");
		}
	}
}
