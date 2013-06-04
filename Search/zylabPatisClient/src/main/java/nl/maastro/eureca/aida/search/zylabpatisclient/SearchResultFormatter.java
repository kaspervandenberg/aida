// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.io.IOException;

/**
 * @author Kasper van den Berg <kasper@kaspervandenberg.net> <kasper.vandenberg@maastro.nl>
 */
public interface SearchResultFormatter {
	public void write(Appendable out, SearchResult result)throws IOException;
	public void writeAll(Appendable out, Iterable<SearchResult> results) throws IOException;	
}
