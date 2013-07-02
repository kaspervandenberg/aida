// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.output;

import java.io.IOException;
import java.util.LinkedHashMap;
import nl.maastro.eureca.aida.search.zylabpatisclient.SearchResult;

/**
 * @author Kasper van den Berg <kasper@kaspervandenberg.net> <kasper.vandenberg@maastro.nl>
 */
public interface SearchResultFormatter {
	public void write(Appendable out, SearchResult result)throws IOException;
	public void writeList(Appendable out, Iterable<SearchResult> results) throws IOException;	
	public void writeTable(Appendable out, LinkedHashMap<String, Iterable<SearchResult>> results) throws IOException;
}
