// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DocumentParseTaskSynchronizer {
	private final ExecutorService executor;
	private final ReferenceResolver referenceResolver;
	private final ZylabData data;

	DocumentParseTaskSynchronizer(ExecutorService executor_, ReferenceResolver referenceResolver_) {
		this.executor = executor_;
		this.referenceResolver = referenceResolver_;
		this.data = new ZylabData();
	}

	public void arrive(URL location) {
		executor.submit(createTask(location));
	}

	private Callable<ZylabData> createTask(URL location) {
		DocumentParts part = new DocumentPartTypeDetector().determinePartOf(location);
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
		return new ParseZylabMetadata(data, location, referenceResolver);
	}

	private Callable<ZylabData> createDataTask(URL location) {
		return new ParseData(data, location);
	}

}
