// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.EnsuresNonNullIf;
import checkers.nullness.quals.Nullable;
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
			@Nullable Column col = columns.get(column);
			assert (col != null) : "@AssumeAssertion(nullness)";
			return col.getCell(row);
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

	@SuppressWarnings("nullness")
	@EnsuresNonNullIf(expression={"columns.get(#1)"}, result=true)
	public boolean containsColumn(final String columnName) {
		return columns.containsKey(columnName);
	}
	
	public boolean containsColumn(final ExpectedResults expectedCol) {
		return containsColumn(getNameFor(expectedCol));
	}

	public boolean containsColumn(final Concept conceptCol) {
		return containsColumn(getNameFor(conceptCol));
	}

	public Iterable<SearchResult> getColumn(final String columnName) {
		if (containsColumn(columnName)) {
			return columns.get(columnName).getValues(getPatients());
		} else {
			throw new NoSuchElementException(String.format(
					"Table contains no coloumn named, %s",
					columnName));
		}
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

	@SuppressWarnings("nullness")
	@EnsuresNonNull({"columns.get(getNameFor(#1))"})
	public void addExpectedResultsColumn(final ExpectedResults item) {
		addExpectedResultsColumn_impl(item);
	}

	private void addExpectedResultsColumn_impl(final ExpectedResults item) {
		Column toAdd = new ValidationColumn(item);
		columns.put(toAdd.getName(), toAdd);
	}

	@SuppressWarnings("nullness")
	@EnsuresNonNull({"columns.get(getNameFor(#1))"})
	public void addConceptSearchColumn(final Concept concept, Iterable<SemanticModifier> modifiersToApply) {
		addConceptSearchColumn_impl(concept, modifiersToApply);
	}

	public void addConceptSearchColumn_impl(final Concept concept, Iterable<SemanticModifier> modifiersToApply) {
		Column toAdd = new SearchedColumn(concept, modifiersToApply);
		columns.put(toAdd.getName(), toAdd);
	}

	public void addAll(Collection<PatisNumber> patients) {
		rows.addAll(patients);
	}

	private static String getNameFor(Concept concept) {
		return concept.getName().getLocalPart();
	}

	private static String getNameFor(ExpectedResults expected) {
		return expected.getTitle();
	}
}
