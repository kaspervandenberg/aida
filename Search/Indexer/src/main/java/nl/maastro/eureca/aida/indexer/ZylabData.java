// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.indexer;

import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import nl.maastro.eureca.aida.indexer.tika.parser.MetadataHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaCoreProperties;

/**
 * Store the data found and when parsing the parts of Zylab documents an store the data used to coordinate this parsing.
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
	
	/**
	 * Types of fieldswith different functionality.
	 */
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
		
		/**
		 * The field's value is analysed, indexed, and stored including positions of the terms relative to other terms and character
		 * positions.  Use for large portions of text. 
		 */
		TEXT(DEFAULT_FIELD_SETTINGS),

		/**
		 * Field that contains a Date; the field is indexed and stored using Lucene's numeric buckets to quickly find dates within
		 * a range.
		 *
		 */
		DATE(new FieldType(SINGLE_TOKEN.getFieldType()) {{ 
			setNumericType(NumericType.LONG);
			setNumericPrecisionStep(8);
		}}),
		
		/**
		 * Metadata from the application's pespective (metadata from the user's perspective should be stored as {@link #SINGLE_TOKEN}, 
		 * {@link #TEXT}, or {@link #DATE}) are values used internally by the Indexer (and possibly searcher); metadata is stored 
		 * but not indexed, meaning that searching for metadata is not possible.
		 */
		METADATA(new FieldType(DEFAULT_FIELD_SETTINGS) {{
			setIndexed(false);
		}}),

		/**
		 * A date stored as metadata uses the same representation as {@link #DATE}, but is not indexed and therefore cannot be
		 * searched on. 
		 */
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
	
	/**
	 * Expected fields with an application defined purpose.  ZylabData supports adding other fields as well.
	 */
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

	/**
	 * ZylabData is spread over two files.
	 */
	public enum DocumentParts {
		DATA,
		METADATA
	}

	/**
	 * A callable that has access to {@code ZylabDate}
	 */
	public abstract class DataTask<T> implements Callable<T> {
		protected ZylabData getData() {
			return ZylabData.this;
		}
	}
	
	/**
	 * For each {@link DocumentParts} the fields that the part is expected to provide.
	 * 
	 * Optionally per field an object that specifies the field's value's origin.  To centralise information about which fields are 
	 * stored and their sources, this data is provided here.  Thereby intentionally violating encapsulatinon and decoupling 
	 * principles: i.e. {@code ZylabData} ignores these objects while other parts of Indexer depend on them:
	 * <ul><li>{@link ParseData#storeMetadata(org.apache.tika.metadata.Metadata)} inserts all fields of part 
	 * 		{@code DATA} that have a {@link Property}-value, Tika's predefined properties are in {@link TikaCoreProperties}.</li>
	 * <li>{@link ParseZylabMetadata#ParseZylabMetadata(nl.maastro.eureca.aida.indexer.ZylabData, 
	 * 		java.net.URL, nl.maastro.eureca.aida.indexer.tika.parser.ReferenceResolver) ParseZylabMetadata.ParseZylabMetadata(…)}
	 * 		constructs the attributes to store from entries in {@code METADATA} that have a 
	 * 		{@link nl.maastro.eureca.aida.indexer.tika.parser.MetadataHandler.XmlAttributes}-value.</li></ul>
	 */
	@SuppressWarnings("serial")
	public static final Map<DocumentParts, Map<Fields, Object>> FIELD_SOURCES =
			Collections.unmodifiableMap(new EnumMap<DocumentParts, Map<Fields, Object>>(DocumentParts.class) {{
				put(DocumentParts.DATA, Collections.unmodifiableMap(
						new EnumMap<Fields, Object>(Fields.class) {{
							put(Fields.CONTENT, null);
							put(Fields.TITLE, TikaCoreProperties.TITLE);
							put(Fields.KEYWORD, TikaCoreProperties.KEYWORDS);
						}}));
				put(DocumentParts.METADATA, Collections.unmodifiableMap(
						new EnumMap<Fields, Object>(Fields.class) {{
							put(Fields.ID, MetadataHandler.XmlAttributes.ATTR_GUID);
							put(Fields.KEYWORD, MetadataHandler.XmlAttributes.ATTR_KEY);
						}}));
			}});

	/**
	 * Fields to contain when a {@link DocumentParts} was last modified.
	 * 
	 * @see #getLastModified(nl.maastro.eureca.aida.indexer.ZylabData.DocumentParts) 
	 */
	@SuppressWarnings("serial")
	private static final Map<DocumentParts, Fields> MODICATION_DATES =
			Collections.unmodifiableMap(new EnumMap<DocumentParts, Fields>(DocumentParts.class) {{
				put(DocumentParts.DATA, Fields.DATA_LAST_MODIFIED);
				put(DocumentParts.METADATA, Fields.METADATA_LAST_MODIFIED);
			}});

	/**
	 * Fields that contain the location of a part (on different servers).
	 * 
	 * @see #getPartPointedTo(java.net.URL) 
	 */
	@SuppressWarnings("serial")
	private static final Map<DocumentParts, Set<Fields>> URLS =
			Collections.unmodifiableMap(new EnumMap<DocumentParts, Set<Fields>>(DocumentParts.class) {{
				put(DocumentParts.DATA, Collections.unmodifiableSet(EnumSet.of(
						Fields.BCK_DATA_URL, Fields.ZYLAB_DATA_URL)));
				put(DocumentParts.METADATA, Collections.unmodifiableSet(EnumSet.of(
						Fields.BCK_METADATA_URL, Fields.ZYLAB_METADATA_URL)));
			}});

	private URL dataURL = null;
	private final Document luceneDoc;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private boolean frozen = false;
	private final EnumMap<DocumentParts, Future<?>> parsetasks = 
			new EnumMap<>(DocumentParts.class);

	public ZylabData() {
		luceneDoc = new Document();
	}

	public ZylabData(Document doc_) {
		luceneDoc = doc_;
	}

	public static boolean hasFieldSource(DocumentParts part, Fields field) {
		if(FIELD_SOURCES.containsKey(part)) {
			return FIELD_SOURCES.get(part).containsKey(field);
		} else {
			return false;
		}
	}

	/**
	 * Access {@link #FIELD_SOURCES}
	 */
	public static Set<Map.Entry<Fields, Object>> getFieldSourceEntries(
			DocumentParts part) {
		if(FIELD_SOURCES.containsKey(part)) {
			return FIELD_SOURCES.get(part).entrySet();
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * Access {@link #FIELD_SOURCES} and cast the result to the expected type
	 */
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

	public Date getLastModified(DocumentParts part) {
		if(luceneDoc != null) {
			IndexableField f = luceneDoc.getField(MODICATION_DATES.get(part).fieldName);
			Number value = f.numericValue();
			if(value != null) {
				return new Date(value.longValue());
			}
		}
		return null;
	}

	public DocumentParts getPartPointedTo(URL target) {
		EnumMap<DocumentParts, Set<String>> urls = new EnumMap<>(DocumentParts.class);
		for (Map.Entry<DocumentParts, Set<Fields>> urlEntry : URLS.entrySet()) {
			Set<String> values = new HashSet<>(urlEntry.getValue().size());
			for (Fields field : urlEntry.getValue()) {
				String value = luceneDoc.get(field.fieldName);
				if(value != null) {
					values.add(FilenameUtils.getFullPath(value) + FilenameUtils.getName(value));
				}
			}
			if(!values.isEmpty()) {
				urls.put(urlEntry.getKey(), values);
			}
		}
		return selectdocumentPart(target.getPath(), urls);
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

	public Future<?> setParseData(DocumentParts part, Future<?> task) {
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

	public Set<Map.Entry<DocumentParts, Future<?>>> getTasks() {
		lock.readLock().lock();
		try {
			return Collections.unmodifiableSet(this.parsetasks.entrySet());
		} finally {
			lock.readLock().unlock();
		}
	}

	public boolean isAllTasksFinished() {
		if(getTasks().size() == DocumentParts.values().length) {
			boolean allComplete = true;
			boolean anyCanceled = false;
			
			for (Map.Entry<ZylabData.DocumentParts, Future<?>> entry : getTasks()) {
				allComplete &= entry.getValue().isDone();
				anyCanceled |= entry.getValue().isCancelled();
			}

			return allComplete && !anyCanceled;
		}
		return false;

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
			for (Map.Entry<DocumentParts, Future<?>> entry : other.getTasks()) {
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

	private static DocumentParts selectdocumentPart(
			String target, EnumMap<DocumentParts, Set<String>> partValues) {
		EnumMap<DocumentParts,Set<String>> matching = new EnumMap<>(DocumentParts.class);
		String targetLastPart = FilenameUtils.getName(target);
		for (Map.Entry<ZylabData.DocumentParts, Set<String>> entry : partValues.entrySet()) {
			for (String partValue : entry.getValue()) {
				if(targetLastPart.equals(FilenameUtils.getName(partValue))) {
					if(!matching.containsKey(entry.getKey())) {
						matching.put(entry.getKey(), 
								new HashSet<String>(entry.getValue().size()));
					}
					matching.get(entry.getKey()).add(FilenameUtils.getPathNoEndSeparator(partValue));
				}
			}
		}
		
		if(matching.size() == 1) {
			return matching.keySet().iterator().next();
		} else if (matching.size() == 0) {
			return null;
		} else {
			String targetPrefix = FilenameUtils.getPathNoEndSeparator(target);
			if (!targetPrefix.isEmpty()) {
				return selectdocumentPart(targetPrefix, matching);
			} else {
				return null;
			}
		}
	}
}
