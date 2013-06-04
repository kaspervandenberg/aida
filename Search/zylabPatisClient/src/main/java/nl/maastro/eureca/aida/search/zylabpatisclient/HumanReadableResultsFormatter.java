// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.io.IOException;

/**
 *
 * @author Kasper van den Berg <kasper@kaspervandenberg.net> <kasper.vandenberg@maastro.nl>
 */
public class HumanReadableResultsFormatter implements SearchResultFormatter {

	@Override
	public void write(Appendable out, SearchResult result) throws IOException {
		out.append(String.format("PatisNr: %s found in %d documents\nSnippets:\n",
				result.patient.value,
				result.nHits));
		
		for (String docId : result.snippets.keySet()) {
			out.append(String.format("\tDocument: %s\n", docId));
			for (String snippet : result.snippets.get(docId)) {
				out.append(String.format("\t\t<snippet>%s</snippet>\n", snippet));
			}
		}
	}

	@Override
	public void writeAll(Appendable out, Iterable<SearchResult> results) throws IOException {
		for (SearchResult item : results) {
			write(out, item);
			out.append("\n");
		}
	}
	
}
