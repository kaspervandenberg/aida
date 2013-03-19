// © Maastro, 2013
package nl.maastro.eureca.aida.indexer.tika.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Parse Zylab XmlFields files.
 * 
 * <p>Store the contents of the XmlFields file in the 
 * {@link org.apache.tika.metadata.Metadata}; use a delegate 
 * {@link org.apache.tika.metadata.Parser} to parse 
 * the file the metadata is about.</p>
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ZylabMetadataXml extends AbstractParser {
	public static class ReferencedDocumentNotFound extends TikaException {

		public ReferencedDocumentNotFound(String msg) {
			super(msg);
		}

		public ReferencedDocumentNotFound(String msg, Throwable cause) {
			super(msg, cause);
		}
	}

	public static class InvallidParseContext extends TikaException {

		public InvallidParseContext(String msg) {
			super(msg);
		}

		public InvallidParseContext(String msg, Throwable cause) {
			super(msg, cause);
		}
		
	}
	
	/**
	 * {@link #refPath} and {@link #refName} identify the file this metadata is 
	 * about.
	 * 
	 * <p>As found in the {@code path} and {@code name} attributes of the 
	 * {@code <document>}-tag.
	 */
	public static class FileRef {
		public final String refPath;
		public final String refName;

		public FileRef(String refPath_, String refName_) {
			this.refPath = refPath_;
			this.refName = refName_;
		}
	}

	/**
	 * Supplied by the client of {@link ZylabMetadataXml} to resolve 
	 * {@link FileRef}s to the {@link Path}s of the file to open.
	 * 
	 * <p>The aida vocabulary server can run on a different platform then the
	 * Zylab server, the Zylab server uses local paths to refer to the file the
	 * metadata is about.</p>
	 */
	public interface FileRefResolver {
		/**
		 * Convert {@code reference} to the {@link java.net.URL} where the file can be 
		 * found.
		 * 
		 * <p>{@link ZylabMetadataXml} will use the returned {@link java.net.URL}
		 * to open an {@link java.io.InputStream}
		 * 
		 * @param reference	{@link FileRef} found when parsing zylab XmlFields 
		 * 		files.
		 * 
		 * @return {@link java.net.URL} that allows reading its contents.
		 */
		public URL resolve(final FileRef reference);
	}

	/**
	 * Tagging interface to allow specifying the {@link org.apache.tika.parser.Parser}
	 * via the {@link org.apache.tika.parser.ParseContext}
	 */
	public interface ContentsParser extends Parser { }

	/**
	 * Tika {@link org.apache.tika.metadata.Parser} to parse the file the 
	 * metadata is about.
	 */
	private final ContentsParser defaultContentsParser;

	/**
	 * Used to convert Zylab's local path–name-reference to a file that AIDA 
	 * can access.
	 */
	private final FileRefResolver defaultResolver;
	
	private static final Set<MediaType> SUPPORTED_TYPE =
			Collections.singleton(MediaType.application("zylabMetadata+xml"));

	/**
	 * Create a parser using {@link org.apache.tika.parser.DefaultParser} to 
	 * parse the file the metadata is about (unless an other is specified in 
	 * the call to {@link #parse(java.io.InputStream, org.xml.sax.ContentHandler,
	 * org.apache.tika.metadata.Metadata, org.apache.tika.parser.ParseContext)}).
	 * 
	 * <p>A {@link FileRefResolver} must be specified using 
	 * {@link org.apache.tika.parser.ParseContext} or {@link 
	 * #parse(java.io.InputStream, org.xml.sax.ContentHandler, 
	 * org.apache.tika.metadata.Metadata, org.apache.tika.parser.ParseContext) }
	 * will throw a {@link InvallidParseContext}-exception.</p>
	 */
	public ZylabMetadataXml() {
		defaultContentsParser = (ContentsParser)new org.apache.tika.parser.DefaultParser(); 
		defaultResolver = null;
	}

	/**
	 * Create a parser using {@code contentsParser_} <i>by default</i> to parse 
	 * the file the metadata is about.
	 * 
	 * <p><i>Note: the caller of {@link #parse(java.io.InputStream, 
	 * org.xml.sax.ContentHandler, org.apache.tika.metadata.Metadata, 
	 * org.apache.tika.parser.ParseContext)} can specify a different 
	 * {@link ContentsParser} and/or {@link FileRefResolver} in the 
	 * {@link org.apache.tika.parser.ParseContext}.</i></p>
	 * 
	 * @param contentsParser_	<ul><li>a {@link org.apache.tika.parser.Parser},
	 * 		this parser is used to parse the file the metadata is about; or</li>
	 * 		<li>{@code null}, use {@link org.apache.tika.parser.DefaultParser}
	 * 		</li></uL>
	 * @param resolver_			<ul><li>a {@link FileRefResolver} to resolve
	 * 		Zylab's reference to subject document to an {@link java.net.URL}; 
	 * 		or</li>
	 * 		<li>{@code null}, the {@code FileRefResolver} must be specified in
	 * 		the {@code ParseContext}.</li></ul>
	 */
	public ZylabMetadataXml(Parser contentsParser_, FileRefResolver resolver_) {
		this.defaultContentsParser = (ContentsParser)contentsParser_;
		defaultResolver = resolver_;
	}

	/**
	 * @param context	not used
	 * @return 	always return {@link #SUPPORTED_TYPE}
	 */
	@Override
	public Set<MediaType> getSupportedTypes(ParseContext context) {
		return SUPPORTED_TYPE;
	}

	@Override
	public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context) throws IOException, SAXException, TikaException {
		MetadataHandler metadataHandler = new MetadataHandler(metadata);
		
		// Parse metadata
		XMLReader reader = XMLReaderFactory.createXMLReader();
		reader.setContentHandler(metadataHandler);
		InputSource iSource = new InputSource(stream);
		reader.parse(iSource);
		stream.close();

		FileRefResolver resolver = context.get(FileRefResolver.class, defaultResolver);
		if(resolver != null) {
			URL aboutDoc = resolver.resolve(metadataHandler.getAboutDocument());
			if(aboutDoc != null) {
				try (InputStream aboutDocStream = aboutDoc.openStream()) {
					// Parse document this metadata is about
					Parser contentsParser = context.get(ContentsParser.class, defaultContentsParser);
					contentsParser.parse(aboutDocStream, handler, metadata, context);
				}
			} else {
				String msg = String.format("Document metadata file %s is about not found (ref: %s, %s)",
						metadata.get(Metadata.RESOURCE_NAME_KEY),
						metadataHandler.getAboutDocument().refPath,
						metadataHandler.getAboutDocument().refName);
				throw new ReferencedDocumentNotFound(msg);
			}
		} else {
			String msg = String.format(
					"Specify FileRefResolver in parse context when parsing %s (parsed file %s)",
					SUPPORTED_TYPE.iterator().next().toString(),
					metadata.get(Metadata.RESOURCE_NAME_KEY));
			throw new InvallidParseContext(msg);
		}
	}
	
}
/* vim: set shiftwidth=4 tabstop=4 noexpandtab fo=ctwan ai : */
