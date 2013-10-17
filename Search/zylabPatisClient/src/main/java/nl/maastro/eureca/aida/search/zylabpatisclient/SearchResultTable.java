// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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
			return this.concept.getName().getLocalPart();
		}

		@Override
		public SearchResult getCell(PatisNumber patient) {
			return results.get(patient);
		}
	}

	private class ValidationColumn implements Column {
		private final ExpectedResults expectedResults;

		public ValidationColumn(ExpectedResults expectedResults_) {
			this.expectedResults = expectedResults_;
		}

		@Override
		public String getName() {
			return expectedResults.getAboutConcept().getName().getLocalPart() + VALIDATION_COLUMN_NAME_SUFFIX;
		}

		@Override
		public SearchResult getCell(PatisNumber patient) {
			return expectedResults.createExpectedResult(patient);
		}
	}

	private static final String VALIDATION_COLUMN_NAME_SUFFIX = "-expected";
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

	public boolean containsColumn(String columnName) {
		return columns.containsKey(columnName);
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

	public void addAll(Collection<PatisNumber> patients) {
		rows.addAll(patients);
	}
}
