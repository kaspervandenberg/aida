// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Callable;
import nl.maastro.eureca.aida.indexer.tika.parser.ZylabMetadataXml;
import nl.maastro.eureca.aida.indexer.tika.parser.ZylabMetadataXml.FileRef;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ParseZylabMetadataTask implements Callable<ZylabData>  {
	private final URL metadataLocation;
	private final ReferenceResolver filerefResolver;
	private final ZylabData data;
	
	public ParseZylabMetadataTask(
			ZylabData existingData,
			URL metadataURL_,
			ReferenceResolver filerefResolver_) {
		this.metadataLocation = metadataURL_;
		this.filerefResolver = filerefResolver_;
		this.data = existingData;
	}

	@Override
	public ZylabData call() throws Exception {
		MetadataHandler metadataHandler = new MetadataHandler(data);
		
		// Parse metadata
		XMLReader reader = XMLReaderFactory.createXMLReader();
		reader.setContentHandler(metadataHandler);
		try (InputStream metadataStream = metadataLocation.openStream()) {
			InputSource iSource = new InputSource(metadataStream);
			reader.parse(iSource);
		}

		try {
			FileRef ref_aboutDoc = metadataHandler.getAboutDocument();
			URL aboutDoc = filerefResolver.resolve(ref_aboutDoc);
			data.initDataUrl(aboutDoc);
		} catch (URISyntaxException ex) {
			String msg = String.format("Document metadata file %s is about not found (ref: %s, %s)",
					metadataLocation.toString(),
					metadataHandler.getAboutDocument().refPath,
					metadataHandler.getAboutDocument().refName);
			throw new ZylabMetadataXml.ReferencedDocumentNotFound(msg, ex);
		}

		return null;
	}
	
}
