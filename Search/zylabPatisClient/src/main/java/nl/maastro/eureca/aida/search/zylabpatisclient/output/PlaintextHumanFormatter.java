// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.output;

import java.io.IOException;
import java.util.LinkedHashMap;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;

/**
 *
 * @author Kasper van den Berg <kasper@kaspervandenberg.net> <kasper.vandenberg@maastro.nl>
 */
public class PlaintextHumanFormatter extends SearchResultFormatterBase {
	private static class FormatSnippets extends SearchResultFormatterBase {
		@Override
		public void write(Appendable out, SearchResult result) throws IOException {
			for (String docId : result.snippets.keySet()) {
				out.append(String.format("\tDocument: %s\n", docId));
				for (String snippet : result.snippets.get(docId)) {
					out.append(String.format("\t\t<snippet>%s</snippet>\n", snippet));
				}
			}
		}
	}

	private static class HideSnippets extends SearchResultFormatterBase {
		@Override
		public void write(Appendable out, SearchResult result) throws IOException {
			// Node code needed
		}
	}
	
	public enum ShowSnippetStrategy implements SearchResultFormatter {
		NEVER(new HideSnippets()),
		ALWAYS(new FormatSnippets());
		
		private final SearchResultFormatter delegate;

		private ShowSnippetStrategy(SearchResultFormatter delegate) {
			this.delegate = delegate;
		}

		@Override
		public void write(Appendable out, SearchResult result) throws IOException {
			delegate.write(out, result);
		}

		@Override
		public void writeList(Appendable out, Iterable<SearchResult> results) throws IOException {
			delegate.writeList(out, results);
		}

		@Override
		public void writeTable(Appendable out, LinkedHashMap<String, Iterable<SearchResult>> results) throws IOException {
			delegate.writeTable(out, results);
		}
	};
	public void setShowSnippetStrategy(ShowSnippetStrategy delegate_) {
		snippetDelegate = delegate_;
	}

	@Override
	public void write(Appendable out, SearchResult result) throws IOException {
		out.append(String.format("PatisNr: %s found in %d documents\nSnippets:\n",
				result.patient.value,
				result.nHits));
		getSnippetStrategy().write(out, result);
	}

}
