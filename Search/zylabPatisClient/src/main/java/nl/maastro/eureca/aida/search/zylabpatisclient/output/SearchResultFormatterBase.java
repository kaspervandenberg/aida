// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.output;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;

/**
 * @author Kasper van den Berg <kasper@kaspervandenberg.net> <kasper.vandenberg@maastro.nl>
 */
public abstract class SearchResultFormatterBase implements SearchResultFormatter {
	protected SearchResultFormatter snippetDelegate;

	public SearchResultFormatter getSnippetStrategy() {
		return snippetDelegate;
	}

	public void setShowSnippetsStrategy(SearchResultFormatter delegate_) {
		snippetDelegate = delegate_;
	}
	protected static class Table extends LinkedHashMap<PatisNumber, LinkedHashMap<String, SearchResult>> {
		
	}
	
	@Override
	public void writeList(Appendable out, Iterable<SearchResult> results) throws IOException {
		for (SearchResult item : results) {
			write(out, item);
			out.append("\n");
		}
	}

	@Override
	public void writeTable(Appendable out, LinkedHashMap<String, Iterable<SearchResult>> results) throws IOException {
		Table data = rotate(results);
		for (PatisNumber row : data.keySet()) {
			writeTableRow(out, data, row);
		}
	}

	protected void writeTableRow(Appendable out, Table data, PatisNumber row) throws IOException {
		for (String col : data.get(row).keySet()) {
			writeTableCell(out, data, row, col);
		}
	}

	protected void writeTableCell(Appendable out, Table data, PatisNumber row, String col) throws IOException {
		write(out, data.get(row).get(col));
	}

	private static Table rotate(LinkedHashMap<String, Iterable<SearchResult>> perColData) {
		Table table = new Table();
		List<String> columns = new LinkedList<>(perColData.keySet());
		LinkedHashMap<String, SearchResult> emptyRow = 
				new LinkedHashMap<>(columns.size());
		for (String col : columns) {
			emptyRow.put(col, null);
		}

		for (String col : columns) {
			for (SearchResult result : perColData.get(col)) {
				if(!table.containsKey(result.patient)) {
					table.put(result.patient, new LinkedHashMap<>(emptyRow));
				}
				table.get(result.patient).put(col, result);
			}
		}
		return table;

	}
}