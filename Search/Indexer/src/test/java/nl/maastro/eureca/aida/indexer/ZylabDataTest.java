// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import nl.maastro.eureca.aida.indexer.tika.parser.ZylabMetadataXml;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.*;
import static nl.maastro.eureca.aida.indexer.LunceneMatchers.*;
import org.apache.commons.io.FilenameUtils;

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

		public ParseData createDataParser(ZylabData container) {
			return new ParseData(container, getUrl());
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

		public ParseZylabMetadata createMetadataParser(ZylabData container, ReferenceResolver refernceResolver) {
			return new ParseZylabMetadata(container, getUrl(), refernceResolver);
		}
	}
	@DataPoints
	public final static MetadataResources[] METADATA_RESOURCES = MetadataResources.values();

	@DataPoints
	public final static FieldsToIndex[] fields() {
		ZylabData.getFieldSourceEntries(ZylabData.DocumentParts.DATA);	// Using a method call to ensure ZylabData.Fields is initialised
		FieldsToIndex[] result = new FieldsToIndex[4];
		result[0] = FieldsToIndex.CONTENT;
		result[1] = FieldsToIndex.KEYWORD;
		result[2] = FieldsToIndex.TITLE;
		result[3] = FieldsToIndex.ID;
		return result;
	}
	

	protected ZylabData testee;
	protected ParseData dataParser;
	protected ParseZylabMetadata metadataParser;
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
		
		testee = new ZylabData();
		dataParser = data.createDataParser(testee);
		metadataParser = metadata.createMetadataParser(testee, referenceResolver);
	}

	@Theory
	public void testMergeNoExceptions() throws Exception {
		dataParser.call();
		metadataParser.call();
	}


	@Theory
	public void testMergeExpectedFieldId() {
		try {
			dataParser.call();
			metadataParser.call();
		} catch (Exception ex) {
			assumeNoException(ex);
		}
		
		assertThat("has ID", testee.getFields(), hasItem(fieldNamed(FieldsToIndex.ID.fieldName)));
	}

	@Theory
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
	public void testMergeExpectedFields(FieldsToIndex field) {
		assumeThat(FilenameUtils.getExtension(data.resource).toLowerCase(), not("txt"));
		try {
			dataParser.call();
			metadataParser.call();
		} catch (Exception ex) {
			assumeNoException(ex);
		}
		
		assertThat(testee.getFields(), hasItem(fieldNamed(field.fieldName)));
	}

	@Theory
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