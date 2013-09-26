// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer.testdata;

import nl.maastro.eureca.aida.indexer.FieldsToIndex;
import org.apache.lucene.index.IndexableField;
import org.hamcrest.Matcher;

/**
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public enum Fields implements Term {
	ID(new KnownFieldValuePair(FieldsToIndex.ID, "{C2212583-3E6D-4AB2-8F80-2C8934833CAB}")),
	PATIS_NUMBER(new KnownFieldValuePair(FieldsToIndex.PATISNUMMER, "12345")),
	ANY_ID(new KnownFieldAnyValue(FieldsToIndex.ID)),
	ANY_CONTENT(new KnownFieldAnyValue(FieldsToIndex.CONTENT)),
	ANY_KEYWORD(new KnownFieldAnyValue(FieldsToIndex.KEYWORD)),
	ANY_TITLE(new KnownFieldAnyValue(FieldsToIndex.TITLE))
	;

	final Term delegate;

	private Fields(Term delegate_) {
		this.delegate = delegate_;
	}

	@Override
	public Matcher<IndexableField> hasValue() {
		return delegate.hasValue();
	}

	@Override
	public Matcher<IndexableField> hasName() {
		return delegate.hasName();
	}
	
}
