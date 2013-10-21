// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.Concept;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.data.ActualResultLists;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.data.ActualResults;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
@RunWith(Theories.class)
public class ExpectedPreviousResultsTest {
	@DataPoints
	public static ActualResultLists[] ALL_RESULTS = ActualResultLists.values();
	
	@Mock private Concept searchedConcept; 
	
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
	public void testCreateFromPrevious_(ActualResultLists data, ActualResultLists other) {
		List<SearchResult> combinedData = combineAndShuffle(data, other);
		
		ExpectedResults testee = ExpectedPreviousResults.create(searchedConcept, combinedData);
		
		for (ActualResults item : data.items) {
			assertTrue(testee.isAsExpected(item.result));
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