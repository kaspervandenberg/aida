// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;

/**
 *
 * @author kasper
 */
class ComparisonTable {
	private final Map<ConceptFoundStatus, Map<ConceptFoundStatus, List<SearchResult>>> data;

	public ComparisonTable() {
		this.data = new EnumMap<>(ConceptFoundStatus.class);
	}

	public List<SearchResult> get(ActualExpectedConceptFoundStatusPair coordinate) {
		if (contains(coordinate)) {
			return Collections.unmodifiableList(getCell(coordinate));
		} else {
			return Collections.emptyList();
		}
	}

	public int count(ActualExpectedConceptFoundStatusPair coordinate) {
		if(contains(coordinate)) {
			return getCell(coordinate).size();
		} else {
			return 0;
		}
	}

	public void put(ActualExpectedConceptFoundStatusPair coordinate, SearchResult actual) {
		if (!contains(coordinate)) {
			init(coordinate);
		}
		getCell(coordinate).add(actual);
	}

	public List<SearchResult> collect(List<ActualExpectedConceptFoundStatusPair> coordinates) {
		List<SearchResult> collected = new LinkedList<>();
		for (ActualExpectedConceptFoundStatusPair coord : coordinates) {
			collected.addAll(get(coord));
		}

		return collected;
	}

	public int countAll(List<ActualExpectedConceptFoundStatusPair> coordinates) {
		int sum = 0;
		for (ActualExpectedConceptFoundStatusPair coord : coordinates) {
			sum += count(coord);
		}
		return sum;
	}

	private boolean contains(ActualExpectedConceptFoundStatusPair coordinate) {
		if (containsRow(coordinate)) {
			Map<ConceptFoundStatus, List<SearchResult>> row = getRow(coordinate);
			return containsCell(row, coordinate);
		} else {
			return false;
		}
	}

	private boolean containsRow(ActualExpectedConceptFoundStatusPair coordinate) {
		return data.containsKey(coordinate.actual);
	}

	private boolean containsCell(Map<ConceptFoundStatus, List<SearchResult>> row, ActualExpectedConceptFoundStatusPair coordinate) {
		return row.containsKey(coordinate.expected);
	}

	private List<SearchResult> getCell(ActualExpectedConceptFoundStatusPair coordinate) {
		Map<ConceptFoundStatus, List<SearchResult>> row = getRow(coordinate);
		return row.get(coordinate.expected);
	}

	private Map<ConceptFoundStatus, List<SearchResult>> getRow(ActualExpectedConceptFoundStatusPair coordinate) {
		return data.get(coordinate.actual);
	}

	private void init(ActualExpectedConceptFoundStatusPair coordinate) {
		if (!containsRow(coordinate)) {
			createRow(coordinate);
		}
		Map<ConceptFoundStatus, List<SearchResult>> row = getRow(coordinate);
		if (!containsCell(row, coordinate)) {
			createCell(row, coordinate);
		}
	}

	private void createRow(ActualExpectedConceptFoundStatusPair coordinate) {
		data.put(coordinate.actual, new EnumMap<ConceptFoundStatus, List<SearchResult>>(ConceptFoundStatus.class));
	}

	private void createCell(Map<ConceptFoundStatus, List<SearchResult>> row, ActualExpectedConceptFoundStatusPair coordinate) {
		row.put(coordinate.expected, new LinkedList<SearchResult>());
	}
	
}
