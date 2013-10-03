// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import nl.maastro.eureca.aida.indexer.concurrent.CompletionObserver;
import nl.maastro.eureca.aida.indexer.concurrent.ObservableExecutorService;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DocumentParseTaskSynchronizer {
	private final ObservableExecutorService executor;
	private final ReferenceResolver referenceResolver;
	private final ZylabDocument data;
	private final Map<DocumentParts, Future<ZylabDocument>> parseTasks;
	private final CompletionObserver<ZylabDocument> observer = new CompletionObserver<ZylabDocument>() {
		@Override
		public void taskFinished(ObservableExecutorService source, Future<ZylabDocument> task) {
			if(allPartsFinished()) {
				queueIndexData();
			}
		}
	};
	private final Queue<ZylabDocument> dataToIndex;
	private boolean dataQueued;

	DocumentParseTaskSynchronizer(ObservableExecutorService executor_, ReferenceResolver referenceResolver_, Queue<ZylabDocument> dataToIndex_) {
		this.executor = executor_;
		this.referenceResolver = referenceResolver_;
		this.data = new ZylabDocumentImpl();
		this.parseTasks = new EnumMap<>(DocumentParts.class);
		this.dataToIndex = dataToIndex_;
		this.dataQueued = false;
	}

	public synchronized void arrive(URL location) {
		submitTask(location);
	}

	private void submitTask(URL location) {
		DocumentParts part = new DocumentPartTypeDetector().determinePartOf(location);
		Future<ZylabDocument> submittedTask = submitTaskForNewPart(part, location);
		parseTasks.put(part, submittedTask);
	}

	private Future<ZylabDocument> submitTaskForNewPart(DocumentParts part, URL location) {
		if (!parseTasks.containsKey(part)) {
			Callable<ZylabDocument> task = createTaskForPart(part, location);
			return executor.subscribeAndSubmit(observer, task);
		} else {
			throw new IllegalStateException(String.format("task for part %s already exists", part.name()));
		}
	}
	
	private Callable<ZylabDocument> createTaskForPart(DocumentParts part, URL location) {
		switch (part) {
			case METADATA:
				return createMetadataTask(location);
			case DATA:
				return createDataTask(location);
			default:
				throw new IllegalArgumentException("URL points to unknown document part");
		}
	}

	private Callable<ZylabDocument> createMetadataTask(URL location) {
		return new ParseZylabMetadataTask(data, location, referenceResolver);
	}

	private Callable<ZylabDocument> createDataTask(URL location) {
		return new ParseDataTask(data, location);
	}

	private boolean allPartsFinished() {
		boolean result = true;
		for (DocumentParts part : DocumentParts.values()) {
			result &= isPartFinished(part);
		}
		return result;
	}

	private boolean isPartFinished(DocumentParts part) {
		if(parseTasks.containsKey(part)) {
			Future<?> task = parseTasks.get(part);
			return task.isDone() && !task.isCancelled();
		} else {
			return false;
		}
	}

	private synchronized void queueIndexData() {
		if(!dataQueued) {
			dataToIndex.add(data);
			dataQueued = true;
		}
	}

}
