// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import net.kaspervandenberg.apps.common.util.cache.MultiCache;
import nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedResults;

/**
 * 
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class SearchResultTable {
	private interface Column {
		public String getName();
		public SearchResult getCell(PatisNumber patient);
		public Iterable<SearchResult> getValues(Iterable<PatisNumber> patients);
	}

	private class SearchedColumn implements Column {
		private final Concept concept;
		private final Iterable<SemanticModifier> modifiersToApply;

		private final transient MultiCache<PatisNumber, SearchResult> results = new MultiCache<PatisNumber, SearchResult>() {
			@Override
			protected SearchResult calc(PatisNumber k) {
				return searcher.searchFor(concept, modifiersToApply, k);
			}
		};
		
		public SearchedColumn(Concept concept_, Iterable<SemanticModifier> modifiersToApply_) {
			this.concept = concept_;
			this.modifiersToApply = modifiersToApply_;
		}

		@Override
		public String getName() {
			return getNameFor(concept);
		}

		@Override
		public SearchResult getCell(PatisNumber patient) {
			return results.get(patient);
		}

		@Override
		public Iterable<SearchResult> getValues(Iterable<PatisNumber> patients) {
			List<SearchResult> resultList = new LinkedList<>();
			for (PatisNumber patient : patients) {
				resultList.add(getCell(patient));
			}
			return resultList;
		}
	}

	private class ValidationColumn implements Column {
		private final ExpectedResults expectedResults;

		public ValidationColumn(ExpectedResults expectedResults_) {
			this.expectedResults = expectedResults_;
		}

		@Override
		public String getName() {
			return getNameFor(expectedResults);
		}

		@Override
		public SearchResult getCell(PatisNumber patient) {
			return expectedResults.createExpectedResult(patient);
		}

		@Override
		public Iterable<SearchResult> getValues(Iterable<PatisNumber> patients) {
			List<SearchResult> resultList = new LinkedList<>();
			for (PatisNumber patient : patients) {
				resultList.add(getCell(patient));
			}
			return resultList;
		}
	}
	private final Searcher searcher;
	private final LinkedHashMap<String, Column> columns;
	private final LinkedHashSet<PatisNumber> rows;

	public SearchResultTable(Searcher searcher_) {
		this.searcher = searcher_;
		this.columns = new LinkedHashMap<>();
		this.rows = new LinkedHashSet<>();
	}

	public Iterable<String> getColumnNames() {
		return Collections.unmodifiableSet(columns.keySet());
	}

	public Iterable<PatisNumber> getPatients() {
		return Collections.unmodifiableSet(rows);
	}

	public SearchResult getCell(PatisNumber row, String column) {
		if(containsColumn(column) && containsPatient(row)) {
			return columns.get(column).getCell(row);
		} else {
			throw new NoSuchElementException(String.format("Table contains no cell (%s, %s)", row.getValue(), column));
		}
	}

	public SearchResult getCell(PatisNumber row, ExpectedResults column) {
		return getCell(row, getNameFor(column));
	}

	public SearchResult getCell(PatisNumber row, Concept column) {
		return getCell(row, getNameFor(column));
	}

	public boolean containsColumn(String columnName) {
		return columns.containsKey(columnName);
	}

	public boolean containsColumn(ExpectedResults expectedCol) {
		return containsColumn(getNameFor(expectedCol));
	}

	public boolean containsColumn(Concept conceptCol) {
		return containsColumn(getNameFor(conceptCol));
	}

	public Iterable<SearchResult> getColumn(String columnName) {
		return columns.get(columnName).getValues(getPatients());
	}

	public Iterable<SearchResult> getColumn(ExpectedResults expectedCol) {
		return getColumn(getNameFor(expectedCol));
	}

	public Iterable<SearchResult> getColumn(Concept conceptCol) {
		return getColumn(getNameFor(conceptCol));
	}

	public boolean containsPatient(PatisNumber patient) {
		return rows.contains(patient);
	}

	public void addExpectedResultsColumn(ExpectedResults item) {
		Column toAdd = new ValidationColumn(item);
		columns.put(toAdd.getName(), toAdd);
	}

	public void addConceptSearchColumn(Concept concept, Iterable<SemanticModifier> modifiersToApply) {
		Column toAdd = new SearchedColumn(concept, modifiersToApply);
		columns.put(toAdd.getName(), toAdd);
	}

	public void addAll(Iterable<PatisNumber> patients) {
		for (PatisNumber patient : patients) {
			rows.add(patient);
		}
	}

	private static String getNameFor(Concept concept) {
		return concept.getName().getLocalPart();
	}

	private static String getNameFor(ExpectedResults expected) {
		return expected.getTitle();
	}
}
