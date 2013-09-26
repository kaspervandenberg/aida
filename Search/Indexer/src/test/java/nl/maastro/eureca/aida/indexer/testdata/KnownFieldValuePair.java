// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.testdata;

import nl.maastro.eureca.aida.indexer.FieldsToIndex;
import nl.maastro.eureca.aida.indexer.matchers.LuceneMatchers;
import org.apache.lucene.index.IndexableField;
import org.hamcrest.Matcher;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class KnownFieldValuePair implements Term {
	private final FieldsToIndex field;
	private final String value;

	public KnownFieldValuePair(FieldsToIndex field_, String value_) {
		this.field = field_;
		this.value = value_;
	}

	@Override
	public Matcher<IndexableField> hasValue() {
		return LuceneMatchers.fieldValue(value);
	}

	@Override
	public Matcher<IndexableField> hasName() {
		final String fieldName = field.createField(value).name();
		return LuceneMatchers.fieldNamed(fieldName);
	}
	
}
