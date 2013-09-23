// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DocumentParseTaskSynchronizer {
	private final ExecutorService executor;
	private final ReferenceResolver referenceResolver;
	private final ZylabData data;
	private final Map<DocumentParts, Future<ZylabData>> parseTasks;
	private final Queue<ZylabData> dataToIndex;
	private boolean dataQueued;

	DocumentParseTaskSynchronizer(ExecutorService executor_, ReferenceResolver referenceResolver_, Queue<ZylabData> dataToIndex_) {
		this.executor = executor_;
		this.referenceResolver = referenceResolver_;
		this.data = new ZylabData();
		this.parseTasks = new EnumMap<>(DocumentParts.class);
		this.dataToIndex = dataToIndex_;
		this.dataQueued = false;
	}

	public synchronized void arrive(URL location) {
		submitTask(location);
	}

	public synchronized void finish(URL dataLocation) {
		if (allPartsFinished()) {
			queueIndexData();
		}
	}

	private void submitTask(URL location) {
		DocumentParts part = new DocumentPartTypeDetector().determinePartOf(location);
		Future<ZylabData> submittedTask = submitTaskForNewPart(part, location);
		parseTasks.put(part, submittedTask);
	}

	private Future<ZylabData> submitTaskForNewPart(DocumentParts part, URL location) {
		if (!parseTasks.containsKey(part)) {
			Callable<ZylabData> task = createTaskForPart(part, location);
			return executor.submit(task);
		} else {
			throw new IllegalStateException(String.format("task for part %s already exists", part.name()));
		}
	}
	
	private Callable<ZylabData> createTaskForPart(DocumentParts part, URL location) {
		switch (part) {
			case METADATA:
				return createMetadataTask(location);
			case DATA:
				return createDataTask(location);
			default:
				throw new IllegalArgumentException("URL points to unknown document part");
		}
	}

	private Callable<ZylabData> createMetadataTask(URL location) {
		return new ParseZylabMetadataTask(data, location, referenceResolver);
	}

	private Callable<ZylabData> createDataTask(URL location) {
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
