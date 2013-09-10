// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import nl.maastro.eureca.aida.indexer.tika.parser.ZylabMetadataXml;
import nl.maastro.eureca.aida.indexer.tika.parser.ZylabMetadataXml.FileRef;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ParseZylabMetadata extends ZylabData.DataTask<Void> {
	private final URL metadataURL;
	private final ReferenceResolver filerefResolver;
	
	public ParseZylabMetadata(
			ZylabData context,
			URL metadataURL_,
			ReferenceResolver filerefResolver_) {
		context.super();
		this.metadataURL = metadataURL_;
		this.filerefResolver = filerefResolver_;
	}

	@Override
	public Void call() throws Exception {
		MetadataHandler metadataHandler = new MetadataHandler(getData());
		
		// Parse metadata
		XMLReader reader = XMLReaderFactory.createXMLReader();
		reader.setContentHandler(metadataHandler);
		try (InputStream metadataStream = metadataURL.openStream()) {
			InputSource iSource = new InputSource(metadataStream);
			reader.parse(iSource);
		}

		try {
			FileRef ref_aboutDoc = metadataHandler.getAboutDocument();
			URL aboutDoc = filerefResolver.resolve(ref_aboutDoc);
			getData().initDataUrl(aboutDoc);
		} catch (URISyntaxException ex) {
			String msg = String.format("Document metadata file %s is about not found (ref: %s, %s)",
					metadataURL.toString(),
					metadataHandler.getAboutDocument().refPath,
					metadataHandler.getAboutDocument().refName);
			throw new ZylabMetadataXml.ReferencedDocumentNotFound(msg, ex);
		}

		return null;
	}
	
}
