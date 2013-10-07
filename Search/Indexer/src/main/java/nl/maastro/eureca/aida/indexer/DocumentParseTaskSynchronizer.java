// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URL;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import nl.maastro.eureca.aida.indexer.util.ObserverCollection;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DocumentParseTaskSynchronizer {
	private final ReferenceResolver referenceResolver;
	private final ZylabDocument data;
	private final Set<DocumentParts> parsingFinished;
	private final ObserverCollection<DataAssociationObserver<DocumentParseTaskSynchronizer>, DocumentParseTaskSynchronizer> observers;
	private final Queue<ZylabDocument> dataToIndex;
	private boolean dataQueued;
	private final DataAssociationObserver<ZylabDocument> dataAssociationChangeForwarder = new DataAssociationObserver<ZylabDocument>() {
		@Override
		public void dataAssociationChanged(ZylabDocument source, URL oldValue, URL currentValue) {
			if(!Objects.equals(oldValue, currentValue)) {
				observers.fireChangeEvent(oldValue, currentValue);
			}
		}
	};

	@SuppressWarnings("unchecked")
	DocumentParseTaskSynchronizer(ReferenceResolver referenceResolver_, Queue<ZylabDocument> dataToIndex_) {
		this.referenceResolver = referenceResolver_;
		this.data = new ZylabDocumentImpl();
		this.parsingFinished = EnumSet.noneOf(DocumentParts.class);
		this.observers = new ObserverCollection<>(
				this, (Class<DataAssociationObserver<DocumentParseTaskSynchronizer>>)(Object)DataAssociationObserver.class);
		data.subscribe(dataAssociationChangeForwarder);
		this.dataToIndex = dataToIndex_;
		this.dataQueued = false;
	}

	public synchronized void arrive(URL location) {
		executeTask(location);
	}

	private void executeTask(URL location) {
		DocumentParts part = new DocumentPartTypeDetector().determinePartOf(location);
		Callable<ZylabDocument> task = createTaskForPart(part, location);
		execute(task);
		finish(part);
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

	private void execute(Callable<ZylabDocument> task) {
		try {
			task.call();
		} catch (Exception ex) {
			throw new Error(ex);
		}
	}

	private void finish(DocumentParts part) {
		parsingFinished.add(part);
		if(isAllPartsFinished()) {
			queueIndexData();
		}
	}

	private boolean isAllPartsFinished() {
		boolean result = true;
		for (DocumentParts part : DocumentParts.values()) {
			result &= isPartFinished(part);
		}
		return result;
	}

	private boolean isPartFinished(DocumentParts part) {
		return parsingFinished.contains(part);
	}

	private synchronized void queueIndexData() {
		if(!dataQueued) {
			dataToIndex.add(data);
			dataQueued = true;
		}
	}

	void subscribe(DataAssociationObserver<DocumentParseTaskSynchronizer> observer) {
		observers.add(observer);
	}
}
