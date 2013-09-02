// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import nl.maastro.eureca.aida.indexer.tika.parser.MetadataHandler;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ZylabData {
	public final static FieldType DEFAULT_FIELD_SETTINGS = new FieldType()
		{{
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
		}};
	
	public enum FieldFunctionality {
		/**
		 * The field's value is indexed and stored as a single token; it is not
		 * analyzed.  Use for identifiers, filenames and hierarchical classification (in
		 * combination with prefix wildcard queries).
		 */
		SINGLE_TOKEN(new FieldType(DEFAULT_FIELD_SETTINGS) {{
			setTokenized(false);
			setStoreTermVectors(false);
			freeze();
		}}),
		
		TEXT(DEFAULT_FIELD_SETTINGS),

		DATE(new FieldType(SINGLE_TOKEN.getFieldType()) {{ 
			setNumericType(NumericType.LONG);
			setNumericPrecisionStep(8);
		}}),
		
		METADATA(new FieldType(DEFAULT_FIELD_SETTINGS) {{
			setIndexed(false);
		}}),

		METADATA_DATE(new FieldType(METADATA.getFieldType()) {{ 
			setNumericType(NumericType.LONG);
			setNumericPrecisionStep(8);
		}}),
		
		;
		
		private final FieldType fieldType;

		private FieldFunctionality(final FieldType type) {
			this.fieldType = type;
		}
		
		public FieldType getFieldType() {
			return fieldType;
		}
	}
	
	public enum Fields {
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
				throw new IllegalArgumentException(
						String.format("Use createField(Date) to create %s-fields", this.name()));
			}

			@Override
			public Field createField(Date value) {
				return new LongField(fieldName, value.getTime(), type);
			} },
		
		METADATA_LAST_MODIFIED(FieldFunctionality.METADATA_DATE) {
			@Override
			public Field createField(String value) {
				throw new IllegalArgumentException(
						String.format("Use createField(Date) to create %s-fields", this.name()));
			}

			@Override
			public Field createField(Date value) {
				return new LongField(fieldName, value.getTime(), type);
			} };

		protected final String fieldName;
		protected final FieldType type;

		private static final Map<String, Fields> fieldsByName = new HashMap<>();
		static {
			for (Fields field : values()) {
				fieldsByName.put(field.fieldName, field);
			}
		}

		private Fields(String fieldName_, FieldFunctionality usage) {
			this.fieldName = fieldName_;
			this.type = usage.getFieldType();
		}
		
		private Fields(FieldFunctionality usage) {
			this.fieldName = this.name().toLowerCase();
			this.type = usage.getFieldType();
		}

		public Field createField(String value) {
			return new Field(fieldName, value, type);
		};

		public Field createField(Date value) {
			throw new IllegalStateException("Field does not supports dates");
		}

		public static Field createField(String fieldName, String value) {
			if(fieldsByName.containsKey(fieldName)) {
				return fieldsByName.get(fieldName).createField(value);
			} else {
				return new Field(fieldName, value, DEFAULT_FIELD_SETTINGS);
			}
		}
	}

	public enum DocumentParts {
		DATA,
		METADATA
	}

	public abstract class DataTask<T> implements Callable<T> {
		protected ZylabData getData() {
			return ZylabData.this;
		}
	}
	
	@SuppressWarnings("serial")
	public static final Map<DocumentParts, Map<Fields, Object>> FIELD_SOURCES =
			Collections.unmodifiableMap(new HashMap<DocumentParts, Map<Fields, Object>>() {{
					put(DocumentParts.DATA, Collections.unmodifiableMap(
							new HashMap<Fields, Object>() {{
								put(Fields.CONTENT, null);
								put(Fields.TITLE, null);
							}}));
					put(DocumentParts.METADATA, Collections.unmodifiableMap(
							new HashMap<Fields, Object>() {{
								put(Fields.ID, MetadataHandler.XmlAttributes.ATTR_GUID);
								put(Fields.KEYWORD, MetadataHandler.XmlAttributes.ATTR_KEY);
							}}));
			}});

	private URL dataURL = null;
	private final Document luceneDoc = new Document();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private boolean frozen = false;
	private final EnumMap<DocumentParts, FutureTask<?>> parsetasks = 
			new EnumMap<>(DocumentParts.class);

	public static boolean hasFieldSource(DocumentParts part, Fields field) {
		if(FIELD_SOURCES.containsKey(part)) {
			return FIELD_SOURCES.get(part).containsKey(field);
		} else {
			return false;
		}
	}

	public static Set<Map.Entry<Fields, Object>> getFieldSourceEntries(
			DocumentParts part) {
		if(FIELD_SOURCES.containsKey(part)) {
			return FIELD_SOURCES.get(part).entrySet();
		} else {
			return Collections.emptySet();
		}
	}

	public static <T> Set<Map.Entry<Fields, T>> getFieldSourceEntries(
			DocumentParts part, Class<T> filter) {
		Set<Map.Entry<Fields, Object>> src = getFieldSourceEntries(part);
		Set<Map.Entry<Fields, T>> result = new HashSet<>(src.size());
		for (Map.Entry<Fields, Object> obj_entry : src) {
			if(filter.isInstance(obj_entry.getValue())) {
				HashMap.SimpleImmutableEntry<Fields, T> entry =
						new AbstractMap.SimpleImmutableEntry<>(
						obj_entry.getKey(),
						filter.cast(obj_entry.getValue()));
				result.add(entry);
			}
		}
		return result;
	}
	
	public URL getDataUrl() {
		return dataURL;
	}

	public Term getId() {
		this.lock.readLock().lock();
		try {
			return new Term(Fields.ID.fieldName, luceneDoc.get(Fields.ID.fieldName));
		} finally {
			this.lock.readLock().unlock();
		}
	}

	public String getLastModified(DocumentParts part) {
		
	}

	public void merge(ZylabData other) 
			throws IllegalArgumentException {
		try {
			if(other.getDataUrl() != null) {
				initDataUrl(other.getDataUrl());
			}
		} catch (IllegalStateException ex) {
			throw new IllegalArgumentException("Cannot merge: DataURLs differ", ex);
		}

		insertAllFields(other);
		insertAllTasks(other);
	}

	public void initDataUrl(final URL value) throws IllegalStateException {
		if(dataURL == null || dataURL.equals(value)) {
			dataURL = value;
		} else {
			throw new IllegalStateException("Set dataURL once.");
		}
	}

	public List<IndexableField> getFields() {
		this.lock.readLock().lock();
		try {
			return new ArrayList<>(luceneDoc.getFields());
		} finally {
			this.lock.readLock().unlock();
		}
	}

	public void setField(String fieldName, String value) {
		this.lock.writeLock().lock();
		try {
			if(!frozen) {
				luceneDoc.add(Fields.createField(fieldName, value));
			} else {
				throw new IllegalStateException("Document is frozen.");
			}
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	public void setField(Fields field, String value) {
		this.lock.writeLock().lock();
		try {
			if(!frozen) {
				luceneDoc.add(field.createField(value));
			} else {
				throw new IllegalStateException("Document is frozen.");
			}
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	public void setField(Fields field, Date value) {
		this.lock.writeLock().lock();
		try {
			if(!frozen) {
				luceneDoc.add(field.createField(value));
			} else {
				throw new IllegalStateException("Document is frozen.");
			}
		} finally {
			this.lock.writeLock().unlock();
		}
	}
	
	public void freeze() {
		this.lock.writeLock().lock();
		try {
			frozen = true;
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	public FutureTask<?> setParseData(DocumentParts part, FutureTask<?> task) {
		lock.writeLock().lock();
		try {
			return this.parsetasks.put(part, task);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void cancelParsing(DocumentParts part) {
		lock.readLock().lock();
		try {
			if(this.parsetasks.containsKey(part)) {
				this.parsetasks.get(part).cancel(false);
			}
		} finally {
			lock.readLock().unlock();
		}
	}

	public Set<Map.Entry<DocumentParts, FutureTask<?>>> getTasks() {
		lock.readLock().lock();
		try {
			return Collections.unmodifiableSet(this.parsetasks.entrySet());
		} finally {
			lock.readLock().unlock();
		}
	}
	
	private void insertAllFields(ZylabData other) {
		lock.writeLock().lock();
		try {
			for (IndexableField field : other.getFields()) {
				luceneDoc.add(field);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	private void insertAllTasks(ZylabData other) {
		lock.writeLock().lock();
		try {
			for (Map.Entry<DocumentParts, FutureTask<?>> entry : other.getTasks()) {
				if(this.parsetasks.containsKey(entry.getKey())) {
					throw new IllegalStateException(
							"Cannot merge parse tasks for duplicate document parts");
				}
				this.parsetasks.put(entry.getKey(), entry.getValue());
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

}
