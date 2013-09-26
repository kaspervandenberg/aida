// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.testdata;

import nl.maastro.eureca.aida.indexer.FieldsToIndex;
import nl.maastro.eureca.aida.indexer.matchers.LuceneMatchers;
import org.apache.lucene.index.IndexableField;
import org.hamcrest.Matcher;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class KnownFieldAnyValue implements Term {
	private final FieldsToIndex field;

	public KnownFieldAnyValue(FieldsToIndex field_) {
		this.field = field_;
	}

	@Override
	public Matcher<IndexableField> hasValue() {
		return org.hamcrest.Matchers.any(IndexableField.class);
	}

	@Override
	public Matcher<IndexableField> hasName() {
		final String fieldName = field.createField("").name();
		return LuceneMatchers.fieldNamed(fieldName);
	}

	
}
