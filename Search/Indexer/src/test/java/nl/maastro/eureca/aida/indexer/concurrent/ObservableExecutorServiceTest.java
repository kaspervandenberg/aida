// © Maastro, 2013
package nl.maastro.eureca.aida.indexer.concurrent;

import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.DummyExecutorObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import nl.maastro.eureca.aida.indexer.ZylabDocument;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.ActionSequence;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.Blocking;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.ConcurrentTestContext;
import nl.maastro.eureca.aida.indexer.testdata.ActionSequences;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import org.junit.After;
import static org.junit.Assume.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static nl.maastro.eureca.aida.indexer.concurrencyTestUtils.DummyExecutorObserver.receivedAnyEvent;
import static nl.maastro.eureca.aida.indexer.concurrencyTestUtils.ExecutorEvent.withValue;
import nl.maastro.eureca.aida.indexer.util.CompletionObserverCollectionFactory;
import nl.maastro.eureca.aida.indexer.util.ObserverCollectionFactory;
import org.junit.Test;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
@RunWith(Theories.class)
public class ObservableExecutorServiceTest implements ConcurrentTestContext<String> {

	@DataPoints
	public static ActionSequence<String>[] SEQUENCES = ActionSequences.values();

	private static final int N_TEST_TASKS = 3;
	private List<Blocking<String>> testTasks;
	private DummyExecutorObserver<String> observer;
	private ConcurrentMap<Integer, Future<String>> submittedTasks;
	private ExecutorService executor;  
	private ObserverCollectionFactory<CompletionObserver<?>, ObservableExecutorService> factory;
	
	private ObservableExecutorService testee;
	private final ActionSequence<String> sequence;

	public ObservableExecutorServiceTest(ActionSequence<String> sequence_) {
		this.sequence = sequence_;
	}

	@Before
	public void setup() throws Exception {
		testTasks = createTasks();
		observer = new DummyExecutorObserver<>();
		submittedTasks = new ConcurrentHashMap<>(N_TEST_TASKS);

		executor = Executors.newSingleThreadExecutor();
		factory = new CompletionObserverCollectionFactory();
		
		testee = new ObservableExecutorService(factory, executor);
	}

	@After
	public void teardown() {
		executor.shutdown();
	}
	
	@Test
	public void testScheduleTask() {
		assumeTrue(sequence.willStart(0));

		sequence.execute(this);
		
		blockUntilTasksFinished();
		assertThat(testTasks.get(0).currentState(), anyOf(
				is(Blocking.State.CALLED), is(Blocking.State.RUNNING), is(Blocking.State.FINISHED), is(Blocking.State.TERMINATED)));
	}

	@Test
	public void testFuture() {
		try {
			assumeTrue(sequence.willStart(0));
	
			sequence.execute(this);
		
			blockUntilTasksFinished();
			submittedTasks.get(0).get(2, TimeUnit.SECONDS);
		} catch (ExecutionException | InterruptedException | TimeoutException ex) {
			ex.printStackTrace();
		}
	}

	@Test
	public void testObserveTask() {
		try {
		assumeTrue(sequence.willFinish(0));
		
		sequence.execute(this);

		blockUntilTasksFinished();
		assertThat(observer, receivedAnyEvent(withValue(createResultFor(0))));
		} catch (Throwable ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
	
	@Override
	public Blocking<? extends String> getTask(int i) {
		return testTasks.get(i);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void submit(int taskId, Callable<? extends String> task) {
		Future<String> submittedTask = testee.subscribeAndSubmit(observer, (Callable<String>)task);
		submittedTasks.put(taskId, submittedTask);
	}
	
	private List<Blocking<String>> createTasks() {
		List<Blocking<String>> result = new ArrayList<>(N_TEST_TASKS);
		for (int i = 0; i < N_TEST_TASKS; i++) {
			result.add(createTask(i));
		}
		return result;
	}

	private Blocking<String> createTask(int i) {
		return Blocking.createDummy(createResultFor(i));
	}

	private String createResultFor(int i) {
		return String.format("task %d called", i);
	}

	private void blockUntilTasksFinished() {
		for (Map.Entry<Integer, Future<String>> entry : submittedTasks.entrySet()) {
			if(sequence.willFinish(entry.getKey())) {
				waitForTaskToFinish(entry.getValue());
			}
		}
	}

	private void waitForTaskToFinish(Future<?> task) {
		try {
			task.get(2000, TimeUnit.MILLISECONDS);
		} catch (ExecutionException | InterruptedException | TimeoutException ex) {
			ex.printStackTrace();
			assumeNoException(ex);
		}
	}
	
}
