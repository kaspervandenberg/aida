// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import nl.maastro.eureca.aida.indexer.tika.parser.ZylabMetadataXml;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class DocumentPartTypeDetector {
	@SuppressWarnings("serial")
	private static final Map<MediaType, DocumentParts> TYPE_TO_PART = Collections.unmodifiableMap(new HashMap<MediaType, DocumentParts>() {{
		put (ZylabMetadataXml.ZYLAB_METADATA, DocumentParts.METADATA);
	}});
	
	private static final DocumentParts DEFAULT_PART = DocumentParts.DATA;
	
	public DocumentParts determinePartOf(URL location) {
		MediaType mediaType = detectMediatype(location);
		return documentPartOrDefault(mediaType);
	}

	private MediaType detectMediatype(URL location) {
		try {
			Tika detectorFacade = new Tika();
			String detectedType = detectorFacade.detect(location);
			return MediaType.parse(detectedType);
		} catch (IOException ex) {
			throw new Error(ex);
		}
	}

	private DocumentParts documentPartOrDefault(MediaType mediaType) {
		if(TYPE_TO_PART.containsKey(mediaType)) {
			return TYPE_TO_PART.get(mediaType);
		} else {
			return DEFAULT_PART;
		}
	}

}
