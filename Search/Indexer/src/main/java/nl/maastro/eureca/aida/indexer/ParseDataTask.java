// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;

/**
 * Use Tika to parse the data of a Zylab document.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ParseDataTask implements Callable<ZylabData> {
	private final URL dataLocation;
	private ZylabData document;
	private final ReadWriteLock documentLock;
	
	public ParseDataTask(ZylabData existingData, URL dataLocation_) {
		this.document = existingData;
		this.dataLocation = dataLocation_;
		this.documentLock = new ReentrantReadWriteLock();
	}

	@Override
	public ZylabData call() throws Exception {
		Metadata tikaMetadata = initMetadata(dataLocation);
		storeContent(tikaMetadata);
		storeMetadata(tikaMetadata);

		try {
			documentLock.readLock().lock();
			return document;
		} finally {
			documentLock.readLock().unlock();
		}
	}

	public void storeContent(Metadata tikaMetadata) throws IOException {
		Tika tika = new Tika();
		try {
			String content = tika.parseToString(dataLocation.openStream(), tikaMetadata);
			if(ZylabData.hasFieldSource(DocumentParts.DATA, FieldsToIndex.CONTENT)) {
				addField(FieldsToIndex.CONTENT, content);
			}
		} catch (TikaException ex) {
			Logger.getLogger(ParseDataTask.class.getName()).log(Level.SEVERE, 
					String.format("Cannot parse %s", dataLocation), ex);
		}

	}

	public void storeMetadata(Metadata tikaMetadata) {
		for (Map.Entry<FieldsToIndex, Property> entry : ZylabData.getFieldSourceEntries(DocumentParts.DATA, Property.class)) {
			Property fieldSource = entry.getValue();
			String value = tikaMetadata.get(fieldSource);
			if(value != null) {
				addField(entry.getKey(), value);
			}
		}
	}

	public void switchTo(ZylabData freshDocument) {
		try {
			documentLock.writeLock().lock();
			
			ZylabData currentDocument = this.document;
			freshDocument.merge(currentDocument);
			this.document = freshDocument;
		} finally {
			documentLock.writeLock().unlock();
		}
	}
	
	private static Metadata initMetadata(URL url) {
		Metadata result = new Metadata();
		result.add(Metadata.RESOURCE_NAME_KEY, FilenameUtils.getName(url.getPath()));
		
		try {
			String contentType = url.openConnection().getContentType();
			if(contentType != null) {
				result.add(Metadata.CONTENT_TYPE, contentType);
			}
		} catch (IOException ex) {
			Logger.getLogger(ParseDataTask.class.getName()).log(Level.WARNING,
					String.format("Unable to open a connection to %s, therefore no content-type header can be read", url), ex);
		}

		try {
			String contentEncoding = url.openConnection().getContentEncoding();
			if(contentEncoding != null) {
				result.add(Metadata.CONTENT_ENCODING, contentEncoding);
			}
		} catch (IOException ex) {
			Logger.getLogger(ParseDataTask.class.getName()).log(Level.WARNING,
					String.format("Unable to open a connection to %s, therefore no content-type header can be read", url), ex);
		}

		return result;
	}

	private void addField(FieldsToIndex field, String value) {
		try {
			documentLock.readLock().lock();
			document.setField(field, value);
		} finally {
			documentLock.readLock().unlock();
		}
	}
}
