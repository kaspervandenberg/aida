/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer;

import org.apache.lucene.document.FieldType;

/**
 * Types of fieldswith different functionality.
 */
public enum FieldFunctionality {
	/**
	 * The field's value is indexed and stored as a single token; it is not
	 * analyzed.  Use for identifiers, filenames and hierarchical classification (in
	 * combination with prefix wildcard queries).
	 */
	SINGLE_TOKEN(new FieldType(ZylabData.DEFAULT_FIELD_SETTINGS) {
		{
			setTokenized(false);
			setStoreTermVectors(false);
			freeze();
		}
	}), /**
	 * The field's value is analysed, indexed, and stored including positions of the terms relative to other terms and character
	 * positions.  Use for large portions of text.
	 */ TEXT(ZylabData.DEFAULT_FIELD_SETTINGS), /**
	 * Field that contains a Date; the field is indexed and stored using Lucene's numeric buckets to quickly find dates within
	 * a range.
	 *
	 */ DATE(new FieldType(SINGLE_TOKEN.getFieldType()) {
		{
			setNumericType(FieldType.NumericType.LONG);
			setNumericPrecisionStep(8);
		}
	}), /**
	 * Metadata from the application's pespective (metadata from the user's perspective should be stored as {@link #SINGLE_TOKEN},
	 * {@link #TEXT}, or {@link #DATE}) are values used internally by the Indexer (and possibly searcher); metadata is stored
	 * but not indexed, meaning that searching for metadata is not possible.
	 */ METADATA(new FieldType(ZylabData.DEFAULT_FIELD_SETTINGS) {
		{
			setIndexed(false);
		}
	}), /**
	 * A date stored as metadata uses the same representation as {@link #DATE}, but is not indexed and therefore cannot be
	 * searched on.
	 */ METADATA_DATE(new FieldType(METADATA.getFieldType()) {
		{
			setNumericType(FieldType.NumericType.LONG);
			setNumericPrecisionStep(8);
		}
	});
	private final FieldType fieldType;

	private FieldFunctionality(final FieldType type) {
		this.fieldType = type;
	}

	public FieldType getFieldType() {
		return fieldType;
	}
	
}
