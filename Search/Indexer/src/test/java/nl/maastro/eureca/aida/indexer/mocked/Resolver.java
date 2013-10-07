// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.mocked;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import nl.maastro.eureca.aida.indexer.ReferenceResolver;
import nl.maastro.eureca.aida.indexer.tika.parser.ZylabMetadataXml;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.junit.Assume.*;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class Resolver {
	private static final String METADATA_RESOURCE = "/datadir/{C2212583-3E6D-4AB2-8F80-2C8934833CAB}.xml";
	private static final String DATA_RESOURCE =  "/referenced-data/txt/2012/52/00000000/50003BX4.TXT";
	private static final String REF_PATH = "D:\\ZyIMAGE Data\\Index Data\\EMD\\txt\\2012\\52\\00000000\\";
	private static final String REF_FILE = "50003BX4.TXT"; 
	
	private boolean isInitialised = false;
	@Mock private ReferenceResolver referenceResolver;
	private URL metadataUrl;
	private URL dataUrl;
	
	public Resolver() {
	}

	private void init() {
		try {
			metadataUrl = Resolver.class.getResource(METADATA_RESOURCE);
			dataUrl = Resolver.class.getResource(DATA_RESOURCE);
			
			MockitoAnnotations.initMocks(this);
			
			when (referenceResolver.resolve(Mockito.any(ZylabMetadataXml.FileRef.class))).thenReturn(dataUrl);
		} catch (URISyntaxException ex) {
			assumeNoException(ex);
		}
	}

	public ReferenceResolver getResolver() {
		initIfNeeded();
		return referenceResolver;
	}

	public URL getMetadataURL() {
		initIfNeeded();
		return metadataUrl;
	}

	public URL getDataUrl() {
		initIfNeeded();
		return dataUrl;
	}

	private void initIfNeeded() {
		if(!isInitialised) {
			init();
		}
	}
}
