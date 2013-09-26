// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.testdata;

import org.apache.lucene.index.IndexableField;
import org.hamcrest.Matcher;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface Term {
	public Matcher<IndexableField> hasValue();	
	public Matcher<IndexableField> hasName();	
	public IndexableField toIndexableField(); 
}
