/* © Maastro, 2012
 */
package org.vle.aid.lucene;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.vle.aid.ResultType;
import static org.vle.aid.lucene.IndexedDocuments.builder;
import org.vle.aid.lucene.IndexedDocuments.Documents;
import org.vle.aid.lucene.IndexedDocuments.Fields;
import org.vle.aid.lucene.IndexedDocuments.FieldContents;
import org.vle.aid.lucene.IndexedDocuments.Queries;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Test whether {@link SearcherWS} returns the expected results.  See also
 * {@link SearcherWSTest_Basic} which contains legacy tests.
 * 
 * @author Kasper van den Berg <kasper@kaspervandenberg.net> 
 */
@RunWith(Theories.class)
public class SearcherWSTest {
	private final static int TEST_MAXDOCS = 1000;
	private final static String INDEXDIR_ENV = "INDEXDIR";
	private File fIndex;
	private FSDirectory index;
	private final IndexedDocuments storedDocs;
	private final Queries query;
	private SearcherWS searcher;
	private Matchers matchers;
	private Matcher<Queries> matchesAnyStoredDocument;

	/**
	 * Interesting configurations to test {@link SearcherWS} with.
	 *
	 * Add any configuration of {@link Documents}, {@link Fields}, and 
	 * {@link FieldContents} you like to test {@code SearcherWS} with.  The 
	 * tests should succeed with arbitrary configurations.  Extend the 
	 * enumerations {@code Documents}, {@code Fields}, and {@code FieldContents}
	 * with new items if needed.
	 */
	@DataPoints
	static public IndexedDocuments docStoreConfigs[] = {
		builder("empty").build(),
		builder("one_doc_one_field").of(Documents.D1).with(Fields.F1).value(FieldContents.V1).build(),
		builder("two_word_term").of(Documents.D1).with(Fields.F1).value(FieldContents.V3).build()
	};

	/**
	 * All items of {@link Queries}.  Extend {@code Queries} with new items to
	 * test {@link SearcherWS} with additional queries.
	 */
	@DataPoints
	static public Queries allQueries[] = Queries.values();
	
	public SearcherWSTest(final IndexedDocuments storedDocs_,
			final Queries query_) {
		storedDocs = storedDocs_;
		query = query_;
	}

	@Before
	public void setUp() {
		try {
			fIndex = assertCreateTmpPath();
			index = FSDirectory.open(fIndex);
			storedDocs.setupIndex(index);
			matchers = new Matchers(storedDocs);
			
			searcher = new SearcherWS();
			searcher.setIndexLocation(fIndex);
			matchesAnyStoredDocument =
					matchers.allDocsMatcher(query.field, Queries.MatchStrategy.ANY);
		} catch (IOException ex) {
			String msg = String.format(
					"Unable to open index %s in path %s",
					fIndex.getName(),
					fIndex.getParent().toString());
			throw new RuntimeException(msg, ex);
		}
	}

	@After
	public void tearDown() {
		deleteIfExists(fIndex);
	}

	/**
	 * Test recall of {@link SearcherWS#_search(Directory,
	 * org.apache.lucene.search.Query, int)}-method, of class {@link SearcherWS}.
	 * 
	 * If a document matches a query, {@code SearcherWS} should return that 
	 * document.
	 * 
	 * @throws IOException	when {@link SearcherWS#_search(org.apache.lucene.search.Query) }
	 * 		throws it
	 */
	@Theory
	public void testUnderscoreSearch_recall_queryTermInDocs_resultsContainDoc()
			throws IOException {
		assumeThat(query, matchesAnyStoredDocument);

		Query q = query.createTermQuery();
		TopDocs topDocs = searcher._search(q);
		Set<Documents> result = IndexedDocuments.toDocumentSet(index, topDocs);
		
		assertThat(result, matchers.containsAllExpectedResultsOf(Set.class, query));
	}

	/**
	 * Test precision of {@link SearcherWS#_search(Directory, Query, int).
	 *
	 * If a document does not match a query {@code SearcherWS} should not return
	 * that document.
	 * 
	 * @throws IOException	when {@link SearcherWS#_search(org.apache.lucene.search.Query) }
	 * 		throws it
	 */
	@Theory
	public void testUnderscoreSearch_precision_queryTermNotInDocs_resultsNotContainDoc()
			throws IOException {
		Query q= query.createTermQuery();
		TopDocs topDocs = searcher._search(q);
		Set<Documents> result = IndexedDocuments.toDocumentSet(index, topDocs);
		
		assertThat(result, matchers.containsNoUnexpectedResultsOf(Set.class, query));
	}

	/**
	 * Test whether {@link SearcherWS#makeXML(org.apache.lucene.search.TopDocs, int) 
	 * returns well formed XML.
	 * 
	 * @throws SAXParseException	the test has failed, XML output is not well formed
	 * @throws IOException	<ul><li>when {@link SearcherWS#_search(org.apache.lucene.search.Query) } throws it; or</li>
	 * 						<li>when {@link SearcherWS#makeXML(org.apache.lucene.search.TopDocs, int) throws it</li></ul>
	 * @throws InterruptedException when writing XML was interrupted
	 * 
	 */
	@Theory
	public void testMakeXML_wellFormedXML() 
			throws IOException, InterruptedException, SAXParseException {
		Query q = query.createTermQuery();
		TopDocs topDocs = searcher._search(q);
		final ResultType xml = searcher.makeXML(topDocs, TEST_MAXDOCS);

		final PipedInputStream snk = new PipedInputStream();
		final PipedOutputStream src = new PipedOutputStream(snk);
		
		// Writing and parsing XML via pipe should be done simultaneously
		ExecutorService executor = Executors.newCachedThreadPool();
		Future<?> saveXml = executor.submit(new Runnable() {
			@Override
			public void run() {
				try  {
					xml.save(src);
					src.close();
				} catch (IOException ex) {
					throw new RuntimeException("Error saving XML", ex);
				}
			}
		});
		
		try {
			DocumentBuilder builder = XMLUtil.createXmlDocBuilder(false);
			builder.parse(snk);
		} catch (SAXParseException ex) {
			throw ex;
		} catch (SAXException ex) {
			throw new Error("Unexpected exception when parsing", ex);
		}
		snk.close();
		
		try {
			// Check for exceptions via Future.get().
			saveXml.get();
			executor.shutdown();
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			if(cause instanceof RuntimeException) {
				throw (RuntimeException)cause;
			} else  {
				throw new Error("Unexpected exception when writing XML", cause);
			}
		}
	}

	/**
	 * Test recall of {@link SearcherWS#makeXML(org.apache.lucene.search.TopDocs, int) }-method,
	 * of class {@link SearcherWS}.
	 * 
	 * If a document matches a query, {@code SearcherWS} should return that 
	 * document.
	 * 
	 * @throws IOException	<ul><li>when {@link SearcherWS#_search(org.apache.lucene.search.Query) }
	 * 		throws it; or</li>
	 * 		<li>when {@link SearcherWS#makeXML(org.apache.lucene.search.TopDocs, int)}
	 * 		throws it.</li></ul>
	 */
	@Theory
	public void testMakeXML_recall() throws IOException {
		assumeThat(query, matchesAnyStoredDocument);

		Query q = query.createTermQuery();
		TopDocs topDocs = searcher._search(q);
		ResultType result = searcher.makeXML(topDocs, TEST_MAXDOCS);
		Node xmlResult = XMLUtil.getInstance().toXmlNode(result);

		assertThat(xmlResult, matchers.containsAllExpectedResultsOf(Node.class, query));
	}

	/**
	 * Test precision of {@link SearcherWS#makeXML(org.apache.lucene.search.TopDocs, int).
	 *
	 * If a document does not match a query {@code SearcherWS} should not return
	 * that document.
	 * 
	 * @throws IOException	<ul><li>when {@link SearcherWS#_search(org.apache.lucene.search.Query) }
	 * 		throws it; or</li>
	 * 		<li>when {@link SearcherWS#makeXML(org.apache.lucene.search.TopDocs, int)}
	 * 		throws it.</li></ul>
	 */
	@Theory
	public void testMakeXML_precision() throws IOException {
		Query q = query.createTermQuery();
		TopDocs topDocs = searcher._search(q);
		ResultType result = searcher.makeXML(topDocs, TEST_MAXDOCS);
		Node xmlResult = XMLUtil.getInstance().toXmlNode(result);

		assertThat(xmlResult, matchers.containsNoUnexpectedResultsOf(Node.class, query));
	}

	@Theory
	public void testSearch_recall() throws SAXException, IOException {
		assumeThat(query, matchesAnyStoredDocument);
		assumeTrue(indexDirEnvValid());
		
		String queryStr = query.queryText;
		String resultStr = searcher.search(index.getDirectory().getName(),
				queryStr, Integer.valueOf(TEST_MAXDOCS).toString(), query.field.name());
		Node xmlResult = XMLUtil.getInstance().toXmlNode(resultStr);
		
		assertThat(xmlResult, matchers.containsAllExpectedResultsOf(Node.class, query));
	}

	@Theory
	public void testSearch_precision() throws SAXException, IOException {
		assumeTrue(indexDirEnvValid());
		
		String queryStr = query.queryText;
		String resultStr = searcher.search(index.getDirectory().getName(),
				queryStr, Integer.valueOf(TEST_MAXDOCS).toString(), query.field.name());
		Node xmlResult = XMLUtil.getInstance().toXmlNode(resultStr);
		
		assertThat(xmlResult, matchers.containsNoUnexpectedResultsOf(Node.class, query));
	}

	private static boolean indexDirEnvValid() {
		try {
			File indexDir = indexDirEnv();
			return true;
//			return indexDir.exists();
		} catch (InvalidPathException | IllegalStateException ex) {
			return false;
		}
	}

	private static File indexDirEnv() throws InvalidPathException {
		String s_indexDir = System.getenv(INDEXDIR_ENV);
		if (s_indexDir != null) {
			Path p_indexDir = FileSystems.getDefault().getPath(s_indexDir);
			return p_indexDir.toFile();
		}
		throw new IllegalStateException(String.format(
				"Environment var %s is not set.",INDEXDIR_ENV));
	}

	private static void deleteIfExists(File file) {
		if (file != null && file.exists()) {
			try {
				FileUtils.deleteDirectory(file);
			} catch (IOException ex) {
				Logger.getLogger(SearcherWSTest.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private static File assertCreateTmpPath() {
		File aidaIndexes = (indexDirEnvValid()) ? 
				indexDirEnv() :
				FileUtils.getTempDirectory();
		UUID uuid = UUID.randomUUID();
		StringBuilder indexName = new StringBuilder()
				.append("testIndex-")
				.append(uuid.toString())
				.append(".tmp");
		File result = new File(aidaIndexes, indexName.toString());
		return result;
	}

}

/* vim: set shiftwidth=4 tabstop=4 noexpandtab fo=ctwan ai : */
