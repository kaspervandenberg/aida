// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import nl.maastro.eureca.aida.indexer.CancelableTask;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class CancelableTaskTest {
	private final long TESTED_SLEEP = 20;
	private final long TESTER_SLEEP = 50;
	
	private CancelableTask tested;
	private AtomicInteger changeableValue;
	private AtomicBoolean interruptedFlag;
	
	public CancelableTaskTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
		changeableValue = new AtomicInteger(0);
		interruptedFlag = new AtomicBoolean(false);
		
		tested = createTask();
	}
	
	@After
	public void tearDown() {
		tested.cancel();
		try {
			tested.join();
		} catch (InterruptedException ex) {
			
		}
		tested = null;
		
	}
	
	@Test
	public void testIsCanceled() {
		tested.cancel();
		assertThat("canceled", tested.isCancelled(), is(true));
	}

	@Test
	public void testThreadRuns() {
		try {
			assumeThat(changeableValue.get(), is(0));
			tested.start();
				Thread.sleep(TESTER_SLEEP);
				assertThat("thread changed value", changeableValue.getAndSet(0), not(0));
		} catch(InterruptedException ex) {
			assumeNoException(ex);
		}
	}

	@Test
	public void testThreadStops() {
		try {
			assumeThat(changeableValue.get(), is(0));
			tested.start();
			Thread.sleep(TESTER_SLEEP);
			
			tested.cancel();
			Thread.sleep(TESTER_SLEEP);
			changeableValue.set(0);
			for (int i = 0; i < 10; i++) {
				Thread.sleep(500);
				assertThat("thread not changeing value", changeableValue.getAndSet(0), is(0));
			}
		} catch(InterruptedException ex) {
			assumeNoException(ex);
		}
	}

	@Test
	public void testThreadInterrupted() {
		final int N_ATTEMPTS = 10;
		int nInterrupted = 0;
		try {
			for (int i = 0; i < 10; i++) {
				interruptedFlag.set(false);
				CancelableTask tested = createTask();
				tested.start();
				Thread.sleep(TESTER_SLEEP);
				tested.cancel();
				tested.join();
				if(interruptedFlag.get()) {
					nInterrupted++;
				}
					
			}
			assertThat(String.format("At least one of %s interrupted", N_ATTEMPTS),
					nInterrupted, greaterThanOrEqualTo(1));
		} catch (InterruptedException ex) {
			assumeNoException(ex);
		}	
	}

	private CancelableTask createTask() {
		return new CancelableTask() {
			@Override
			public void run() {
				while (!isCancelled()) {
					try {
							changeableValue.set(1);
							sleep(TESTED_SLEEP);
					} catch (InterruptedException ex) {
						interruptedFlag.set(true);
						interrupted();
					}
				}
			} };	
	}
}