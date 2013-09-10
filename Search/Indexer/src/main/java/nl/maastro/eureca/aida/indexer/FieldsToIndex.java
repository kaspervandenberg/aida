/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;

/**
 * Expected fields with an application defined purpose.  ZylabData supports adding other fields as well.
 */
public enum FieldsToIndex {
	ID(FieldFunctionality.SINGLE_TOKEN), TITLE(FieldFunctionality.TEXT), PATISNUMMER(FieldFunctionality.SINGLE_TOKEN), CONTENT(FieldFunctionality.TEXT), ZYLAB_DATA_URL(FieldFunctionality.METADATA), ZYLAB_METADATA_URL(FieldFunctionality.METADATA), BCK_DATA_URL(FieldFunctionality.METADATA), BCK_METADATA_URL(FieldFunctionality.METADATA), DOCUMENT_DATE(FieldFunctionality.DATE), KEYWORD(FieldFunctionality.TEXT), DATA_LAST_MODIFIED(FieldFunctionality.METADATA_DATE) {
		@Override
		public Field createField(String value) {
			throw new IllegalArgumentException(String.format("Use createField(Date) to create %s-fields", this.name()));
		}

		@Override
		public Field createField(Date value) {
			return new LongField(fieldName, value.getTime(), type);
		}
	}, METADATA_LAST_MODIFIED(FieldFunctionality.METADATA_DATE) {
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

	private FieldsToIndex(String fieldName_, FieldFunctionality usage) {
		this.fieldName = fieldName_;
		this.type = usage.getFieldType();
	}

	private FieldsToIndex(FieldFunctionality usage) {
		this.fieldName = this.name().toLowerCase();
		this.type = usage.getFieldType();
	}

	public Field createField(String value) {
		return new Field(fieldName, value, type);
	}

	public Field createField(Date value) {
		throw new IllegalStateException("Field does not supports dates");
	}

	public static Field createField(String fieldName, String value) {
		if (fieldsByName.containsKey(fieldName)) {
			return fieldsByName.get(fieldName).createField(value);
		} else {
			return new Field(fieldName, value, ZylabData.DEFAULT_FIELD_SETTINGS);
		}
	}
	
}
