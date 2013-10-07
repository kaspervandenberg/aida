/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer;

import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.DummyDataChangeObserver;
import nl.maastro.eureca.aida.indexer.concurrent.ObservableExecutorService;
import nl.maastro.eureca.aida.indexer.mocked.Resolver;
import nl.maastro.eureca.aida.indexer.util.CompletionObserverCollectionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.mockito.MockitoAnnotations;
import static org.hamcrest.Matchers.*;
import static nl.maastro.eureca.aida.indexer.concurrencyTestUtils.DummyExecutorObserver.receivedAnyEvent;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.DummyObserverBase;
import nl.maastro.eureca.aida.indexer.concurrencyTestUtils.UrlChangeEvent;
import static nl.maastro.eureca.aida.indexer.concurrencyTestUtils.UrlChangeEvent.setTo;
import org.hamcrest.Matcher;

/**
 *
 * @author kasper
 */
public class DocumentParseTaskSynchronizerWithRealParsingTest {
	private static final String DATA_RESOURCE = "/referenced-data/txt/2012/52/00000000/50003BX4.TXT";
	
	private Resolver mockedResolver;
	private URL metadataLocation;
	private URL dataLocation;
	private BlockingQueue<ZylabDocument> dataToIndex;
	private DummyDataChangeObserver<DocumentParseTaskSynchronizer> observer;
	private ObservableExecutorService executor;
	private DocumentParseTaskSynchronizer testee;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockedResolver = new Resolver();
		metadataLocation = mockedResolver.getMetadataURL();
		dataLocation = DocumentParseTaskSynchronizerWithRealParsingTest.class.getResource(DATA_RESOURCE);
		dataToIndex = new LinkedBlockingQueue<>() ;
		observer = new DummyDataChangeObserver<>();
		executor = createObservableExecutorService();
		
		testee = new DocumentParseTaskSynchronizer(executor, mockedResolver.getResolver(), dataToIndex);
	}

	private static ObservableExecutorService createObservableExecutorService() {
		ExecutorService executor = Executors.newCachedThreadPool();
		CompletionObserverCollectionFactory factory = new CompletionObserverCollectionFactory();
		ObservableExecutorService result = new ObservableExecutorService(factory, executor);
		return result;
	}

	@Test
	public void testDocumentQueued() throws InterruptedException {
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

		try {
			executor.shutdown();
			executor.awaitTermination(2000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			assumeNoException(ex);
		}
		
		System.out.println("URLs:");
		for (UrlChangeEvent<DocumentParseTaskSynchronizer> urlChangeEvent : observer.getEventsReceived()) {
			System.out.printf("%s -> %s\n", urlChangeEvent.getBefore(), urlChangeEvent.getAfter());
		}
		
		Matcher<UrlChangeEvent<DocumentParseTaskSynchronizer>> setToResolvedUrl = setTo(mockedResolver.getDataUrl());
		Matcher<DummyObserverBase<UrlChangeEvent<DocumentParseTaskSynchronizer>>> receiveEventChangedToResolvedUrl = receivedAnyEvent(setToResolvedUrl);
		assertThat(observer, receiveEventChangedToResolvedUrl);
	}
}
