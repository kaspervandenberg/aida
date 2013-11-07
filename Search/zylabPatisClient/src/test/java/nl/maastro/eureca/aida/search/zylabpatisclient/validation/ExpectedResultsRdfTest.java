// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.Concept;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.rdfUtil.ClosableRepositoryConnection;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.rdfUtil.ClosableRepositoryConnectionFactory;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import org.junit.Before;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import static org.mockito.Mockito.when;
import static org.junit.Assume.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import org.junit.experimental.theories.Theory;
import org.mockito.MockitoAnnotations;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.rio.turtle.TurtleParserFactory;
/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
@RunWith(Theories.class)
@SuppressWarnings("serialize")
public class ExpectedResultsRdfTest {
	public enum DataStoreContents {
		EMPTY("", Collections.<PatisNumber, ConceptFoundStatus>emptyMap()),
		SINGLE_PATIENT_SINGLE_RESULT(
				TEST_PREFIX + ":" + EXISTING_CONCEPT_LOCAL_PART + " exp:expectsResults " + TEST_PREFIX + ":" + EXISTING_EXPECTATION_ID + " ."
				+ TEST_PREFIX + ":" + EXISTING_EXPECTATION_ID + " exp:patient _:p1 ."
				+ "_:p1 exp:patis \"12345\" ."
				+ "_:p1 exp:result exp:found .",
			new HashMap<PatisNumber, ConceptFoundStatus>() {{
				put(PatisNumber.create("12345"), ConceptFoundStatus.FOUND);
			}}),
//		SINGLE_PATIENT_NO_RESULTS(),

		;

		private final String n3Contents;
		private final Map<PatisNumber, ConceptFoundStatus> definedStatuses;
		
		
		private DataStoreContents(String n3Contents_, Map<PatisNumber, ConceptFoundStatus> definedStatuses_) {
			this.n3Contents = buildN3(n3Contents_);
			this.definedStatuses = definedStatuses_;
		}

		public void addToStore(RepositoryConnection connection) {
			InputStream s = IOUtils.toInputStream(n3Contents);
			try {
				connection.add(s, n3Contents, RDFFormat.N3);
			} catch (IOException | RDFParseException | RepositoryException ex) {
				throw new Error(ex);
			}
		}

		public Set<PatisNumber> getDefinedPatients() {
			return definedStatuses.keySet();
		}

		private static String buildN3(String n3Contents_) {
			StringBuilder str = new StringBuilder();
			appendN3Namespaces(str);
			str.append(n3Contents_);
			return str.toString();
		}
		
		private static void appendN3Namespaces(StringBuilder builder) {
			builder.append(n3NamespacePrefix(
							Config.PropertyKeys.RDF_VALIDATION_PREFIX,
							Config.PropertyKeys.RDF_VALIDATION_URI));
			builder.append(n3NamespacePrefix(
							Config.PropertyKeys.PRECONSTRUCTED_PREFIX,
							Config.PropertyKeys.PRECONSTRUCTED_URI));
			builder.append(n3NamespacePrefix(TEST_PREFIX, TEST_URI));
			builder.append("\n");
		}

		private static String n3NamespacePrefix(Config.PropertyKeys prefix, Config.PropertyKeys uri) {
			return n3NamespacePrefix(prefix.getValue(), uri.getValue());
		}

		private static String n3NamespacePrefix(String prefix, String uri) {
			String result = String.format("@prefix %s: <%s>.\n", prefix, uri);
			return result;
		}
	}

//	@DataPoint
//	public static final DataStoreContents EMPTY = DataStoreContents.EMPTY;

	@DataPoint
	public static final DataStoreContents SINGLE_PATIENT_SINGLE_RESULT = DataStoreContents.SINGLE_PATIENT_SINGLE_RESULT;

	public enum Patients {
		EXISTING_PATIENT("12345");

		private final PatisNumber number;

		private Patients(String number_) {
			this.number = PatisNumber.create(number_);
		}

		public PatisNumber getPatisNumber() {
			return number;
		}
	}
	@DataPoint
	public static final Patients EXISTING_PATIENT = Patients.EXISTING_PATIENT;
	
	private final static String TEST_URI = "http://clinisearch.ad.maastro.nl/zylabpatis/test/";
	private final static String TEST_PREFIX = "test";
	private final static String EXISTING_CONCEPT_LOCAL_PART = "existingConcept";
	private final static String EXISTING_EXPECTATION_ID = "existingExpectation";
	private final static QName EXPECTATION_ID = new QName(TEST_URI, EXISTING_EXPECTATION_ID, TEST_PREFIX);

	private final DataStoreContents contents;
	private Repository repo;
	@Mock private Concept concept;
	private ExpectedResultsRdf testee;

	public ExpectedResultsRdfTest(DataStoreContents contents_) {
		this.contents = contents_;
	}
	
	@Before
	public void setup() throws RepositoryException {
		MockitoAnnotations.initMocks(this);
		mockConceptQname();
		
		repo = new SailRepository(new MemoryStore());
		repo.initialize();
		try (ClosableRepositoryConnection connection = 
				ClosableRepositoryConnectionFactory.decorate(repo.getConnection())) {
			contents.addToStore(connection);

			testee = new ExpectedResultsRdf(repo, concept, EXPECTATION_ID);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	@After
	public void tearDown() throws RepositoryException {
		repo.shutDown();
	}
	
	@Test
	public void testTrivialSparqlAsk() throws RepositoryException, MalformedQueryException, 
			QueryEvaluationException {
		try {
			assumeThat(contents.getDefinedPatients(), not(empty()));
		
			try (ClosableRepositoryConnection connection = 
					ClosableRepositoryConnectionFactory.decorate(repo.getConnection())) {
				BooleanQuery query = connection.prepareBooleanQuery(QueryLanguage.SPARQL, "ASK {?s ?p ?o}");
				assertTrue(query.evaluate());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		
	}
	
	@Theory
	public void testPatientInDefined(Patients patient) {
		try {
			assumeThat(contents.getDefinedPatients(), hasItem(patient.getPatisNumber()));
		
			assertTrue(testee.isInDefined(patient.getPatisNumber()));
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	private void mockConceptQname() {
		when (concept.getName()) .thenReturn(new QName(TEST_URI, EXISTING_CONCEPT_LOCAL_PART, TEST_PREFIX));
	}
}