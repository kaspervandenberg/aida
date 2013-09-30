/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.DurationMeasurement;
import nl.maastro.eureca.aida.indexer.matchers.DurationMatcher;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.hamcrest.Matcher;

/**
 *
 * @author kasper
 */
public class ObservableExecutorServiceTestBasic {
	static private final int MAX_TIME_MSEC = 1500;
	static private final int N_OBSERVATIONS = 1000;
	static private final int N_PARALLEL_TASKS = 50;
	
	@Mock private Callable<String> mockedTask;
	@Mock private ObservableExecutorService.Observer<String> mockedObserver;
	private Callable<String> task;
	private ObservableExecutorService.Observer<String> observer;

	private ObservableExecutorService<String> testee;
	
	private DurationMeasurement stopwatch;
	private AtomicInteger timesCalled;
	private AtomicInteger timesNotified;
	private Semaphore tasksFinished;
	private Semaphore startNext;

	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		MockitoAnnotations.initMocks(this);
		task = createTask();
		observer = createObserver();

		doAnswer(createOnTaskfinished()) 
				.when(mockedObserver) .taskFinished((ObservableExecutorService<String>)any(), (Future<String>)any());

		stopwatch = new DurationMeasurement();
		timesCalled = new AtomicInteger(0);
		timesNotified = new AtomicInteger(0);
		tasksFinished = new Semaphore(0);
		startNext = new Semaphore(N_PARALLEL_TASKS);
		 
		testee = new ObservableExecutorService<>(Executors.newCachedThreadPool());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testCompletionExecutor() {
		CompletionService<String> testedCompletionSerivce = new ExecutorCompletionService<>(Executors.newSingleThreadExecutor());

		testedCompletionSerivce.submit(mockedTask);

		try {
			Future<String> result = testedCompletionSerivce.poll(1000, TimeUnit.MILLISECONDS);
			assertThat(result, is(notNullValue()));
		} catch (InterruptedException ex) {
			assumeNoException(ex);
		}
	}

	@Test
	public void testSubmitCallable() {
		stopwatch.start();
		
		submit(N_OBSERVATIONS, task, observer);
		waitUntilAllFinished();

		stopwatch.stop();

		assertThat(timesCalled.get(), is(N_OBSERVATIONS));
		assertThat(timesNotified.get(), is(N_OBSERVATIONS));
		assertThat(stopwatch, withinTimeLimit());
	}
	
	@Test
	public void testSubmitMockedCallable() throws Exception {
		stopwatch.start();

		submit(N_OBSERVATIONS, mockedTask, observer);
		waitUntilAllFinished();
		
		stopwatch.stop();

		verify (mockedTask, atLeast(N_OBSERVATIONS)) .call();
		assertThat(timesNotified.get(), is(N_OBSERVATIONS));
		assertThat(stopwatch, withinTimeLimit());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testSubmitMockedCallableMockedObserver() throws Exception {
		stopwatch.start();

		submit(N_OBSERVATIONS, mockedTask, mockedObserver);
		waitUntilAllFinished();

		stopwatch.stop();
		
		verify (mockedTask, atLeast(N_OBSERVATIONS)) .call();
		verify (mockedObserver, atLeast(N_OBSERVATIONS)) .taskFinished(same(testee), (Future<String>)any());
		assertThat(stopwatch, withinTimeLimit());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testFuture() {
		Future<String> future = testee.submit(mockedObserver, mockedTask);
		
		try {
			future.get(MAX_TIME_MSEC, TimeUnit.MILLISECONDS);
		} catch (ExecutionException | TimeoutException | InterruptedException ex) {
			assumeNoException(ex);
		}
	}

	private Callable<String> createTask() {
		return new Callable<String>() {
			@Override
			public String call() throws Exception {
				timesCalled.incrementAndGet();
				return "test";
			} };
	}

	private ObservableExecutorService.Observer<String> createObserver() {
		return new ObservableExecutorService.Observer<String>() {
			@Override
			public void taskFinished(ObservableExecutorService<String> source, Future<String> task) {
				timesNotified.incrementAndGet();
				tasksFinished.release();
				startNext.release();
			} };
	}

	private Answer<Void> createOnTaskfinished() {
		return new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				timesNotified.incrementAndGet();
				tasksFinished.release();
				startNext.release();
				return null;
			} };
	}

	private void submit(int count, Callable<String> task, ObservableExecutorService.Observer<String> observer) {
		for (int i = 0; i < count; i++) {
			waitForAvailableThread();
			testee.submit(observer, task);
		}
	}

	private void waitForAvailableThread() {
		try {
			boolean haveAcquired = startNext.tryAcquire(MAX_TIME_MSEC, TimeUnit.MILLISECONDS);
			assumeTrue(String.format("thread available within %d miliseconds", MAX_TIME_MSEC), haveAcquired);
		} catch(InterruptedException ex) {
			assumeNoException("waiting for available thread", ex);
		}
	}

	private void waitUntilAllFinished() {
		try {
			boolean allFinished = tasksFinished.tryAcquire(N_OBSERVATIONS, MAX_TIME_MSEC, TimeUnit.MILLISECONDS);
			assumeTrue("all task finished permits acquired", allFinished);
		} catch (InterruptedException ex) {
			assumeNoException("waiting for all tasks to finish", ex);
		}
	}

	private Matcher<DurationMeasurement> withinTimeLimit() {
		return DurationMatcher.within(MAX_TIME_MSEC, TimeUnit.MILLISECONDS);
	}
}
