// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.FieldInfo;

/**
 * Types of fields with different functionality.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public enum FieldFunctionality {
	DEFAULT_FIELD_SETTINGS(new FieldType() {{
		setIndexed(true);
		setStored(true);
		setTokenized(true);
		setStoreTermVectors(true);
		setStoreTermVectorOffsets(true);
		setStoreTermVectorPositions(true);
		setStoreTermVectorPayloads(false);
		setOmitNorms(false);
		setIndexOptions(
			FieldInfo.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		setDocValueType(null);
	
		freeze();
	}}),

	/**
	 * The field's value is indexed and stored as a single token; it is not
	 * analyzed.  Use for identifiers, filenames and hierarchical classification (in
	 * combination with prefix wildcard queries).
	 */
	SINGLE_TOKEN(new FieldType(DEFAULT_FIELD_SETTINGS.getFieldType()) {{
		setTokenized(false);
		setStoreTermVectors(false);
		freeze();
	}}),
	
	/**
	 * The field's value is analysed, indexed, and stored including positions of the terms relative to other terms and character
	 * positions.  Use for large portions of text.
	 */
	TEXT(DEFAULT_FIELD_SETTINGS.getFieldType()),
	
	/**
	 * Field that contains a Date; the field is indexed and stored using Lucene's numeric buckets to quickly find dates within
	 * a range.
	 *
	 */
	DATE(new FieldType(SINGLE_TOKEN.getFieldType()) {{
		setNumericType(FieldType.NumericType.LONG);
		setNumericPrecisionStep(8);
	}}),
	
	/**
	 * Metadata from the application's pespective (metadata from the user's perspective should be stored as {@link #SINGLE_TOKEN},
	 * {@link #TEXT}, or {@link #DATE}) are values used internally by the Indexer (and possibly searcher); metadata is stored
	 * but not indexed, meaning that searching for metadata is not possible.
	 */
	METADATA(new FieldType(DEFAULT_FIELD_SETTINGS.getFieldType()) {{
		setIndexed(false);
	}}),
	
	/**
	 * A date stored as metadata uses the same representation as {@link #DATE}, but is not indexed and therefore cannot be
	 * searched on.
	 */
	METADATA_DATE(new FieldType(METADATA.getFieldType()) {{
		setNumericType(FieldType.NumericType.LONG);
		setNumericPrecisionStep(8);
	}});
	 
	private final FieldType fieldType;

	private FieldFunctionality(final FieldType type) {
		this.fieldType = type;
	}

	public FieldType getFieldType() {
		return fieldType;
	}
	
}
