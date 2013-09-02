// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
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
public class ParseData extends ZylabData.DataTask<Void> {
	private final URL data;
	
	public ParseData(ZylabData container, URL data_) {
		container.super();
		this.data = data_;
	}

	@Override
	public Void call() throws Exception {
		Metadata tikaMetadata = initMetadata(data);
		storeContent(tikaMetadata);
		storeMetadata(tikaMetadata);

		return null;
	}

	public void storeContent(Metadata tikaMetadata) throws IOException {
		Tika tika = new Tika();
		try {
			String content = tika.parseToString(data.openStream(), tikaMetadata);
			if(ZylabData.hasFieldSource(ZylabData.DocumentParts.DATA, ZylabData.Fields.CONTENT)) {
				getData().setField(ZylabData.Fields.CONTENT, content);
			}
		} catch (TikaException ex) {
			Logger.getLogger(ParseData.class.getName()).log(Level.SEVERE, 
					String.format("Cannot parse %s", data), ex);
		}

	}

	public void storeMetadata(Metadata tikaMetadata) {
		for (Map.Entry<ZylabData.Fields, Property> entry : ZylabData.getFieldSourceEntries(ZylabData.DocumentParts.DATA, Property.class)) {
			Property fieldSource = entry.getValue();
			String value = tikaMetadata.get(fieldSource);
			if(value != null) {
				getData().setField(entry.getKey(), value);
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
