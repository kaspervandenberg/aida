// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
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
public class ParseData implements Callable<ZylabData> {
	private final URL dataLocation;
	private final ZylabData data;
	
	public ParseData(ZylabData existingData, URL dataLocation_) {
		this.data = existingData;
		this.dataLocation = dataLocation_;
	}

	@Override
	public ZylabData call() throws Exception {
		Metadata tikaMetadata = initMetadata(dataLocation);
		storeContent(tikaMetadata);
		storeMetadata(tikaMetadata);

		return data;
	}

	public void storeContent(Metadata tikaMetadata) throws IOException {
		Tika tika = new Tika();
		try {
			String content = tika.parseToString(dataLocation.openStream(), tikaMetadata);
			if(ZylabData.hasFieldSource(DocumentParts.DATA, FieldsToIndex.CONTENT)) {
				data.setField(FieldsToIndex.CONTENT, content);
			}
		} catch (TikaException ex) {
			Logger.getLogger(ParseData.class.getName()).log(Level.SEVERE, 
					String.format("Cannot parse %s", dataLocation), ex);
		}

	}

	public void storeMetadata(Metadata tikaMetadata) {
		for (Map.Entry<FieldsToIndex, Property> entry : ZylabData.getFieldSourceEntries(DocumentParts.DATA, Property.class)) {
			Property fieldSource = entry.getValue();
			String value = tikaMetadata.get(fieldSource);
			if(value != null) {
				data.setField(entry.getKey(), value);
			}
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
			Logger.getLogger(ParseData.class.getName()).log(Level.WARNING,
					String.format("Unable to open a connection to %s, therefore no content-type header can be read", url), ex);
		}

		try {
			String contentEncoding = url.openConnection().getContentEncoding();
			if(contentEncoding != null) {
				result.add(Metadata.CONTENT_ENCODING, contentEncoding);
			}
		} catch (IOException ex) {
			Logger.getLogger(ParseData.class.getName()).log(Level.WARNING,
					String.format("Unable to open a connection to %s, therefore no content-type header can be read", url), ex);
		}

		return result;
	}
}
