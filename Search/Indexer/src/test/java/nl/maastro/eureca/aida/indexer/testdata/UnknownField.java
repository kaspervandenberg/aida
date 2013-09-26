/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.testdata;

import nl.maastro.eureca.aida.indexer.matchers.LuceneMatchers;
import org.apache.lucene.index.IndexableField;
import org.hamcrest.Matcher;

/**
 *
 * @author kasper
 */
public class UnknownField implements Term {
	private final String name;
	private final String value;

	public UnknownField(String fieldName_, String value_) {
		this.name = fieldName_;
		this.value = value_;
	}
	
	@Override
	public Matcher<IndexableField> hasValue() {
		return LuceneMatchers.fieldValue(value);
	}

	@Override
	public Matcher<IndexableField> hasName() {
		return LuceneMatchers.fieldNamed(name);
	}
	
}
