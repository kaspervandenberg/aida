// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;

/**
 *
 * @author kasper
 */
class ComparisonTable {
	private final Map<EligibilityClassification, Map<EligibilityClassification, List<SearchResult>>> data;

	public ComparisonTable() {
		this.data = new EnumMap<>(EligibilityClassification.class);
	}

	public List<SearchResult> get(ActualExpectedEligibilityClassificationPair coordinate) {
		if (contains(coordinate)) {
			return Collections.unmodifiableList(getCell(coordinate));
		} else {
			return Collections.emptyList();
		}
	}

	public int count(ActualExpectedEligibilityClassificationPair coordinate) {
		if(contains(coordinate)) {
			return getCell(coordinate).size();
		} else {
			return 0;
		}
	}

	public void put(ActualExpectedEligibilityClassificationPair coordinate, SearchResult actual) {
		if (!contains(coordinate)) {
			init(coordinate);
		}
		getCell(coordinate).add(actual);
	}

	public List<SearchResult> collect(List<ActualExpectedEligibilityClassificationPair> coordinates) {
		List<SearchResult> collected = new LinkedList<>();
		for (ActualExpectedEligibilityClassificationPair coord : coordinates) {
			collected.addAll(get(coord));
		}

		return collected;
	}

	public int countAll(List<ActualExpectedEligibilityClassificationPair> coordinates) {
		int sum = 0;
		for (ActualExpectedEligibilityClassificationPair coord : coordinates) {
			sum += count(coord);
		}
		return sum;
	}

	private boolean contains(ActualExpectedEligibilityClassificationPair coordinate) {
		if (containsRow(coordinate)) {
			Map<EligibilityClassification, List<SearchResult>> row = getRow(coordinate);
			return containsCell(row, coordinate);
		} else {
			return false;
		}
	}

	private boolean containsRow(ActualExpectedEligibilityClassificationPair coordinate) {
		return data.containsKey(coordinate.actual);
	}

	private boolean containsCell(Map<EligibilityClassification, List<SearchResult>> row, ActualExpectedEligibilityClassificationPair coordinate) {
		return row.containsKey(coordinate.expected);
	}

	private List<SearchResult> getCell(ActualExpectedEligibilityClassificationPair coordinate) {
		Map<EligibilityClassification, List<SearchResult>> row = getRow(coordinate);
		return row.get(coordinate.expected);
	}

	private Map<EligibilityClassification, List<SearchResult>> getRow(ActualExpectedEligibilityClassificationPair coordinate) {
		return data.get(coordinate.actual);
	}

	private void init(ActualExpectedEligibilityClassificationPair coordinate) {
		if (!containsRow(coordinate)) {
			createRow(coordinate);
		}
		Map<EligibilityClassification, List<SearchResult>> row = getRow(coordinate);
		if (!containsCell(row, coordinate)) {
			createCell(row, coordinate);
		}
	}

	private void createRow(ActualExpectedEligibilityClassificationPair coordinate) {
		data.put(coordinate.actual, new EnumMap<EligibilityClassification, List<SearchResult>>(EligibilityClassification.class));
	}

	private void createCell(Map<EligibilityClassification, List<SearchResult>> row, ActualExpectedEligibilityClassificationPair coordinate) {
		row.put(coordinate.expected, new LinkedList<SearchResult>());
	}
	
}
