// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.Concept;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.query.Query;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.data.ActualResultLists;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.data.ActualResults;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.search.spans.SpanQuery;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
@RunWith(Theories.class)
public class ExpectedPreviousResultsTest {
	@DataPoints
	public static ActualResultLists[] ALL_RESULTS = ActualResultLists.values();
	
	private Concept searchedConcept = new Concept() {

		@Override
		public <T> T accept(Query.Visitor<T> visitor) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public QName getName() {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public QueryNode getParsetreeRepresentation() {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public SpanQuery getLuceneObjectRepresentation() {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public org.apache.lucene.search.Query getRepresentation() {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
	}; 
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Theory
	public void testCreateFromPrevious_hasAllPatients(ActualResultLists data) {
		Set<PatisNumber> s = patientsInTestdata(data);
		PatisNumber[] expectedPatients = s.toArray(new PatisNumber[s.size()]);
		
		ExpectedResults testee = ExpectedPreviousResults.create(searchedConcept, data.result);
		
		assertThat(testee.getDefinedPatients(), containsInAnyOrder(expectedPatients));
	}

	@Theory
	public void testCreateFromPrevious_allResultsAsExpected(ActualResultLists data, ActualResultLists other) {
		List<SearchResult> combinedData = combineAndShuffle(data, other);
		
		ExpectedResults testee = ExpectedPreviousResults.create(searchedConcept, combinedData);
		
		for (ActualResults item : data.items) {
			assertTrue(testee.isAsExpected(item.result));
		}
	}

	@Theory
	public void testWriteRead_hasAllPatients(ActualResultLists data) {
		Set<PatisNumber> s = patientsInTestdata(data);
		PatisNumber[] expectedPatients = s.toArray(new PatisNumber[s.size()]);
		ExpectedPreviousResults toWrite = ExpectedPreviousResults.create(searchedConcept, data.result);
		StringWriter store = new StringWriter();

		toWrite.writeAsJson(store);
		StringReader reader = new StringReader(store.toString());
		ExpectedPreviousResults readResults = ExpectedPreviousResults.read(searchedConcept, reader);
		
		assertThat(readResults.getDefinedPatients(), containsInAnyOrder(expectedPatients));
	}

	@Theory
	public void testWriteRead_allResultsAsExpected(ActualResultLists data, ActualResultLists other) {
		List<SearchResult> combinedData = combineAndShuffle(data, other);
		ExpectedPreviousResults toWrite = ExpectedPreviousResults.create(searchedConcept, combinedData);
		StringWriter store = new StringWriter();

		toWrite.writeAsJson(store);
		StringReader reader = new StringReader(store.toString());
		ExpectedPreviousResults readResults = ExpectedPreviousResults.read(searchedConcept, reader);
		
		for (ActualResults item : data.items) {
			assertTrue(readResults.isAsExpected(item.result));
		}
	}

	private Set<PatisNumber> patientsInTestdata(ActualResultLists data) {
		Set<PatisNumber> patients = new HashSet<>();
		for (ActualResults item : data.items) {
			patients.add(item.result.getPatient());
		}
		return patients;
	}

	private List<SearchResult> combineAndShuffle(ActualResultLists data, ActualResultLists other) {
		List<SearchResult> combined = new ArrayList<>(data.result.size() + other.result.size());
		combined.addAll(data.result);
		combined.addAll(other.result);
		Collections.shuffle(combined);
		return combined;
	}
}