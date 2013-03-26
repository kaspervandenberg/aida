// © Maastro, 2013
package nl.maastro.eureca.aida.indexer.tika.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Scanner;
import org.apache.tika.detect.Detector;
import org.apache.tika.detect.EncodingDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.txt.UniversalEncodingDetector;

/**
 * Detect Zylab metadata files that {@link ZylabMetadataXml} can parse.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ZylabMetadataXmlDetector implements Detector {
	private static final int MAX_READ = 200;
	private static final String ZYLAB_TAG = "<zylab>";
	private static final String ZYLAB_METADATA_DIRECTORY = 
			File.separator + "XmlFields" + File.separator;
	
	private WeakReference<EncodingDetector> encodingDetector = null;

	@Override
	public MediaType detect(InputStream input, Metadata metadata) throws IOException {
		if(input != null) {
			input.mark(MAX_READ + 4);
			try { 
				Charset cs = getEncodingDetector().detect(input, metadata);
				if(cs != null) {
					Scanner scanner = new Scanner(input, cs.name());
					String firstXmlTag = scanner.findWithinHorizon("<\\w.*\\w>", MAX_READ);
					
					if(firstXmlTag != null && firstXmlTag.contains(ZYLAB_TAG)) {
						return ZylabMetadataXml.ZYLAB_METADATA;
					}
				}
			} finally {
				input.reset();
			}
		}
		
		String path = metadata.get(TikaMetadataKeys.RESOURCE_NAME_KEY);
		if(path != null) {
			if(path.contains(ZYLAB_METADATA_DIRECTORY)) {
				return ZylabMetadataXml.ZYLAB_METADATA;
			}
		}

		/* Unknown mediatype, return application/octet-stream as specified in 
		 * Detector.
		 */
		return MediaType.OCTET_STREAM;
	}

	private EncodingDetector getEncodingDetector() {
		EncodingDetector detector = (encodingDetector != null) ?
				encodingDetector.get() : null;
		if(detector == null) {
			detector = new UniversalEncodingDetector();
			encodingDetector = new WeakReference<>(detector);
		}
		return detector;
	}
}
