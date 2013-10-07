// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.DummyDataChangeObserver;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.DummyObserverBase;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.UrlChangeEvent;
import nl.maastro.eureca.aida.indexer.mocked.Resolver;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.Before;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
		

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DocumentParseTaskSynchronizerTest {
	private Resolver mockedResolver;
	private URL metadataLocation;
	private URL dataLocation;
	private BlockingQueue<ZylabDocument> dataToIndex;
	private DummyDataChangeObserver<DocumentParseTaskSynchronizer> observer;
	private DocumentParseTaskSynchronizer testee;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockedResolver = new Resolver();
		metadataLocation = mockedResolver.getMetadataURL();
		dataLocation = mockedResolver.getDataUrl();
		dataToIndex = new LinkedBlockingQueue<>();
		observer = new DummyDataChangeObserver<>();
		
		testee = new DocumentParseTaskSynchronizer(mockedResolver.getResolver(), dataToIndex);
	}

	@Test
	public void testFinishDataBeforeMetadata_queueData() {
		testee.arrive(metadataLocation);
		testee.arrive(dataLocation);

		assertThat(dataToIndex.size(), org.hamcrest.Matchers.greaterThanOrEqualTo(1));
	}

	@Test
	public void testFinishMetadataBeforeData_queueData() {
		testee.arrive(metadataLocation);
		testee.arrive(dataLocation);

		assertThat(dataToIndex.size(), org.hamcrest.Matchers.greaterThanOrEqualTo(1));
	}

	@Test
	public void testArriveAndFinishOnlyData_doNotQueueData() {
		testee.arrive(dataLocation);
		
		assertTrue(dataToIndex.isEmpty());
	}

	@Test
	public void testArriveAndFinishOnlyMetadata_doNotQueueData() {
		testee.arrive(dataLocation);
		
		assertTrue(dataToIndex.isEmpty());
	}

	@Test
	public void testQueuedDocumentDataLocation() throws InterruptedException {
		testee.arrive(metadataLocation);
		testee.arrive(dataLocation);

		ZylabDocument item = dataToIndex.poll(2000, TimeUnit.MILLISECONDS);
		assertNotNull(item);
		assertThat(item.getDataUrl(), anyOf(is(mockedResolver.getDataUrl()), is(dataLocation)));
	}
	
	@Test
	public void testObserveDataUrl() {
		testee.subscribe(observer);
		testee.arrive(metadataLocation);

		System.out.println("URLs:");
		for (UrlChangeEvent<DocumentParseTaskSynchronizer> urlChangeEvent : observer.getEventsReceived()) {
			System.out.printf("%s -> %s\n", urlChangeEvent.getBefore(), urlChangeEvent.getAfter());
		}
		
		Matcher<UrlChangeEvent<DocumentParseTaskSynchronizer>> setToResolvedUrl = UrlChangeEvent.setTo(mockedResolver.getDataUrl());
		Matcher<DummyObserverBase<UrlChangeEvent<DocumentParseTaskSynchronizer>>> receiveEventChangedToResolvedUrl = DummyObserverBase.receivedAnyEvent(setToResolvedUrl);
		assertThat(observer, receiveEventChangedToResolvedUrl);
	}
}
