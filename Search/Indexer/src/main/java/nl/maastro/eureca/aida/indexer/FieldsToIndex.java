// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;

/**
 * Expected fields with an application defined purpose.  ZylabData supports adding other fields as well.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public enum FieldsToIndex {
	ID(FieldFunctionality.SINGLE_TOKEN),
	TITLE(FieldFunctionality.TEXT),
	PATISNUMMER(FieldFunctionality.SINGLE_TOKEN),
	CONTENT(FieldFunctionality.TEXT),
	ZYLAB_DATA_URL(FieldFunctionality.METADATA),
	ZYLAB_METADATA_URL(FieldFunctionality.METADATA),
	BCK_DATA_URL(FieldFunctionality.METADATA),
	BCK_METADATA_URL(FieldFunctionality.METADATA),
	DOCUMENT_DATE(FieldFunctionality.DATE),
	KEYWORD(FieldFunctionality.TEXT),
	
	DATA_LAST_MODIFIED(FieldFunctionality.METADATA_DATE) {
		@Override
		public Field createField(String value) {
			throw new IllegalArgumentException(String.format("Use createField(Date) to create %s-fields", this.name()));
		}

		@Override
		public Field createField(Date value) {
			return new LongField(fieldName, value.getTime(), type);
		}
	},
	
	METADATA_LAST_MODIFIED(FieldFunctionality.METADATA_DATE) {
		@Override
		public Field createField(String value) {
			throw new IllegalArgumentException(String.format("Use createField(Date) to create %s-fields", this.name()));
		}

		@Override
		public Field createField(Date value) {
			return new LongField(fieldName, value.getTime(), type);
		}
	};
	
	protected final String fieldName;
	protected final FieldType type;
	private static final Map<String, FieldsToIndex> fieldsByName = new HashMap<>();
	static {
		for (FieldsToIndex field : FieldsToIndex.values()) {
			fieldsByName.put(field.fieldName, field);
		}
	}

	/**
	 * Construct a field to retrieve from the Zylab document.
	 * 
	 * @param fieldName_	the name under which this field is stored in the Lucene index
	 * @param usage			a {@link FieldFunctionality} item that specifies the intended use of the field.
	 */
	private FieldsToIndex(String fieldName_, FieldFunctionality usage) {
		this.fieldName = fieldName_;
		this.type = usage.getFieldType();
	}

	/**
	 * Construct a field –with a default name– to retrieve from the Zylab document.
	 * 
	 * @param usage			a {@link FieldFunctionality} item that specifies the intended use of the field.
	 */
	private FieldsToIndex(FieldFunctionality usage) {
		this.fieldName = this.name().toLowerCase();
		this.type = usage.getFieldType();
	}

	/**
	 * Use the {@link #type useage specifications} to create a Lucene {@link Field} having {@code value} that can be added to a
	 * {@link org.apache.lucene.document.Document}.
	 * 
	 * <i>NOTE: Do not use this method to create date or numeric fields; use {@link #createField(java.util.Date)} instead.</i>
	 * 
	 * @param value		the text value of the field
	 * 
	 * @return	a fresh Lucene {@link Field}. 
	 */
	public Field createField(String value) {
		return new Field(fieldName, value, type);
	}

	/**
	 * Create a Lucene {@link Field} with {@code value}.
	 * 
	 * @param value
	 * 
	 * @return	a fresh Lucene {@link Field}.
	 * 
	 * @throws IllegalStateException	when the expected useage of this field is not to index dates.
	 */
	public Field createField(Date value) {
		throw new IllegalStateException("Field does not supports dates");
	}

	/**
	 * Create a Lucene {@link Field} named {@code fieldName} containing {@code value}.
	 * <ul><li>when {@code FieldsToIndex} has an item named {@code fieldName}, use the {@link #type useage specifications} to
	 * 		create {@code Field}; or</li>
	 * <li>when {@code fieldName} is unknown, use {@link FieldFunctionality#DEFAULT_FIELD_SETTINGS} to create a field.</li></ul>
	 * @param fieldName
	 * @param value
	 * @return 
	 */
	public static Field createField(String fieldName, String value) {
		if (fieldsByName.containsKey(fieldName)) {
			return fieldsByName.get(fieldName).createField(value);
		} else {
			return new Field(fieldName, value, FieldFunctionality.DEFAULT_FIELD_SETTINGS.getFieldType());
		}
	}
}
