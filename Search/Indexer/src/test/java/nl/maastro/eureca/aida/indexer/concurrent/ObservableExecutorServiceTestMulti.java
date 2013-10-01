/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.Blocking;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.DummyExecutorObserver;
import org.junit.Test;
import static org.junit.Assert.*;

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
		ObservableExecutorService testee = new ObservableExecutorService(Executors.newSingleThreadExecutor());
		
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
			testee = new ObservableExecutorService(Executors.newSingleThreadExecutor());
			
			Future<String> futureResult = scheduleAndRelease(testee, observer, task);
			waitUntilFinished(futureResult);
		}
	}

	private static <T> Future<T> scheduleAndRelease(ObservableExecutorService testee, 
			ObservableExecutorService.CompletionObserver<T> observer, Blocking<T> task) {
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
