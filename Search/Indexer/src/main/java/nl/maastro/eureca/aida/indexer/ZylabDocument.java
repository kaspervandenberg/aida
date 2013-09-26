// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URL;
import java.util.Date;
import java.util.List;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public interface ZylabDocument {

	public URL getDataUrl();

	public List<IndexableField> getFields();

	public Term getId();

	public void initDataUrl(final URL value) throws IllegalStateException;

	public void merge(nl.maastro.eureca.aida.indexer.ZylabDocument other) throws IllegalArgumentException;

	public void setField(String fieldName, String value);

	public void setField(FieldsToIndex field, String value);

	public void setField(FieldsToIndex field, Date value);
}
