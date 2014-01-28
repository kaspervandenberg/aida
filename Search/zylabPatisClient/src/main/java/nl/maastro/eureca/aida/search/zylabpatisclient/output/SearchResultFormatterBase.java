// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.output;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.MonotonicNonNull;
import checkers.nullness.quals.NonNull;
import checkers.nullness.quals.Nullable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResultTable;

/**
 * @author Kasper van den Berg <kasper@kaspervandenberg.net> <kasper.vandenberg@maastro.nl>
 */
public abstract class SearchResultFormatterBase implements SearchResultFormatter {
	private static SearchResultFormatter DEFAULT_NOP_SNIPPET_WRITER = new SearchResultFormatterBase() {
		@Override
		public void write(Appendable out, SearchResult result) throws IOException {
			// Do nothing
		}
	};
	
	private @MonotonicNonNull SearchResultFormatter snippetDelegate = null;

	public SearchResultFormatter getSnippetStrategy() {
		if (snippetDelegate != null) {
			return snippetDelegate;
		} else {
			return DEFAULT_NOP_SNIPPET_WRITER;
		}
	}

	@EnsuresNonNull("snippetDelegate")
	public void setShowSnippetsStrategy(SearchResultFormatter delegate_) {
		snippetDelegate = delegate_;
	}
	@SuppressWarnings("serial")
	protected static class Table extends LinkedHashMap<PatisNumber, LinkedHashMap<String, /*@Nullable*/SearchResult>> {
		
	}
	
	@Override
	public void writeList(Appendable out, Iterable<SearchResult> results) throws IOException {
		for (SearchResult item : results) {
			write(out, item);
			out.append("\n");
		}
	}

	@Override
	public void writeTable(Appendable out, SearchResultTable data) throws IOException {
		for (PatisNumber row : data.getPatients()) {
			writeTableRow(out, data, row);
		}
	}

	protected void writeTableRow(Appendable out, SearchResultTable data, PatisNumber row) throws IOException {
		for (String col : data.getColumnNames()) {
			writeTableCell(out, data, row, col);
		}
	}

	protected void writeTableCell(Appendable out, SearchResultTable data, PatisNumber row, String col) throws IOException {
		write(out, data.getCell(row, col));
	}

	private static Table rotate(LinkedHashMap<String, Iterable<SearchResult>> perColData) {
		Table table = new Table();
		List<String> columns = new LinkedList<>(perColData.keySet());
		LinkedHashMap<String, /*@Nullable*/SearchResult> emptyRow = 
				new LinkedHashMap<>(columns.size());
		for (String col : columns) {
			emptyRow.put(col, null);
		}

		// TODO simplify: make intent explicit
		for (String col : columns) {
			if(perColData.containsKey(col)) {
				for (SearchResult result : perColData.get(col)) {
					if(!table.containsKey(result.getPatient())) {
						table.put(result.getPatient(), new LinkedHashMap<String, /*@Nullable*/SearchResult>(emptyRow));
					}
					@SuppressWarnings("nullness")
					@NonNull LinkedHashMap<String, SearchResult> row = table.get(result.getPatient());
					row.put(col, result);
				}
			}
		}
		return table;

	}
}
