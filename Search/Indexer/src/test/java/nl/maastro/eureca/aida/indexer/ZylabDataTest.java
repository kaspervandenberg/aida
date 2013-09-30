// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URISyntaxException;
import java.net.URL;
import nl.maastro.eureca.aida.indexer.tika.parser.ZylabMetadataXml;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.*;
import static nl.maastro.eureca.aida.indexer.matchers.LuceneMatchers.*;
import nl.maastro.eureca.aida.indexer.testdata.Fields;
import nl.maastro.eureca.aida.indexer.testdata.Term;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

/**
 * @see ParseZylabMetadataTest
 * @see ParseDataTest
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
@RunWith(Theories.class)
public class ZylabDataTest {
	protected interface ResourceProvider {
		public URL getUrl();
	}
	
	protected enum DataResources implements ResourceProvider {
		ZYLAB("/referenced-data/txt/2012/52/00000000/50003BX4.TXT"),
		WORD("/datadir/Grant Update 1-2.doc");

		private final String resource;
		
		private DataResources(String resource_) {
			this.resource = resource_;
		}

		@Override
		public URL getUrl() {
			return DataResources.class.getResource(resource);
		}

		public ParseDataTask createDataParser(ZylabDocument container) {
			return new ParseDataTask(container, getUrl());
		}
	}
	@DataPoints
	public final static DataResources[] DATA_RESOURCES = DataResources.values();

	protected enum MetadataResources implements ResourceProvider {
		ZYLAB("/datadir/{C2212583-3E6D-4AB2-8F80-2C8934833CAB}.xml");
		
		private final String resource;
		
		private MetadataResources(String resource_) {
			this.resource = resource_;
		}

		@Override
		public URL getUrl() {
			return DataResources.class.getResource(resource);
		}

		public ParseZylabMetadataTask createMetadataParser(ZylabDocument container, ReferenceResolver refernceResolver) {
			return new ParseZylabMetadataTask(container, getUrl(), refernceResolver);
		}
	}
	@DataPoints
	public final static MetadataResources[] METADATA_RESOURCES = MetadataResources.values();

	@DataPoints
	public final static Term[] FIELDS = Fields.values();

	protected ZylabDocument testee;
	protected ParseDataTask dataParser;
	protected ParseZylabMetadataTask metadataParser;
	@Mock protected ReferenceResolver referenceResolver;
	
	protected final MetadataResources metadata;
	protected final DataResources data;
	
	public ZylabDataTest(MetadataResources metadata_, DataResources data_) {
		this.metadata = metadata_;
		this.data = data_;
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		try {
			when(referenceResolver.resolve(Mockito.any(ZylabMetadataXml.FileRef.class))).thenReturn(data.getUrl());
		} catch (URISyntaxException ex) {
			assumeNoException(ex);
		}
		
		testee = new ZylabDocumentImpl();
		dataParser = data.createDataParser(testee);
		metadataParser = metadata.createMetadataParser(testee, referenceResolver);
	}

	@Test
	public void testMergeNoExceptions() throws Exception {
		dataParser.call();
		metadataParser.call();
	}


	@Test
	public void testMergeExpectedFieldId() {
		try {
			dataParser.call();
			metadataParser.call();
		} catch (Exception ex) {
			assumeNoException(ex);
		}
		
		assertThat("has ID", testee.getFields(), hasItem(fieldNamed(FieldsToIndex.ID.fieldName)));
	}

	@Test
	public void testMergeExpectedFieldContent() {
		try {
			dataParser.call();
			metadataParser.call();
		} catch (Exception ex) {
			assumeNoException(ex);
		}
		
		assertThat("has content", testee.getFields(), hasItem(fieldNamed(FieldsToIndex.CONTENT.fieldName)));
	}
	
	@Theory
	public void testMergeExpectedFields(Term field) {
		assumeThat(FilenameUtils.getExtension(data.resource).toLowerCase(), not("txt"));
		try {
			dataParser.call();
			metadataParser.call();
		} catch (Exception ex) {
			assumeNoException(ex);
		}
		
		assertThat(testee.getFields(), hasItem(fieldNamed(field)));
	}

	@Test
	public void testDataUrl()  {
		try {
			dataParser.call();
			metadataParser.call();
		} catch (Exception ex) {
			assumeNoException(ex);
		}
		assertThat(testee.getDataUrl(), is(data.getUrl()));
	}



}