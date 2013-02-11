package indexer;

import indexer.config.ConfigType;
import indexer.config.DocType;
import indexer.config.FieldType;
import indexer.config.IndexType;
import indexer.config.VectorType;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.lucene.document.Field;


/**
 *
 * @author Machiel Jansen, Edgar Meij
 */
public class ConfigurationHandler {
	/**
	 * Cache the fields {@link #document} contains.  Convert the field settings 
	 * from {@link ConfigurationHandler#CONFIGURATIONFILE} to Lucene's FieldType.
	 * 
	 * <i>NOTE: Contrary to {@link indexer.config.DocType#getField() } which is 
	 * a life view of the configured fields, {@code FieldTypeCache} reads these 
	 * fields only once.</i>
	 * 
	 * @see ConfigurationHandler#convertToLuceneFieldType(indexer.config.FieldType) 
	 */
	private static class FieldTypeCache  {
		/**
		 * {@link DocType} to whose fields this {@code FieldTypeCache} provides 
		 * access.
		 */
		private final DocType document;

		/**
		 * Mapping of {@link ConfigurationHandler#CONFIGURATIONFILE}{@code /<DocType>/<Field>/<Name}
		 * to (cached) Lucene {@link org.apache.lucene.document.FieldType}.
		 * 
		 * <p><i>REMARK: {@link java.lang.System#gc()} is free to remove entries; 
		 * {@link #get(java.lang.String)} will (re-) create them when needed.
		 * </i></p>
		 */
		private final WeakHashMap<String, org.apache.lucene.document.FieldType> fields =
				new WeakHashMap<String, org.apache.lucene.document.FieldType>();
		
		/**
		 * Mapping of {@link ConfigurationHandler#CONFIGURATIONFILE}{@code /<DocType>/<Field>/<Name>}
		 * to {@link indexer.config.FieldType} as read from {@link #config}.
		 * 
		 * <p>REMARK: Map is unmodifiable after it has been initialised.</p>
		 */
		private final Map<String, indexer.config.FieldType> fieldConfig;

		/**
		 * Empty {@code FieldTypeCache} which returns 
		 * {@link ConfigurationHandler#DEFAULT_FT} on each {@link #get(java.lang.String)}
		 * request.
		 */
		private static final FieldTypeCache EMPTY = new FieldTypeCache();

		/**
		 * Create an empty FieldTypeCache that {@link #get(java.lang.String) returns} 
		 * {@link ConfigurationHandler#DEFAULT_FT} for each fieldName supplied.
		 * 
		 * This constructor is private: for normal operation use 
		 * {@link #FieldTypeCache(indexer.config.DocType) } to instantiate a
		 * {@code FieldTypeCache}; or use {@link #createEmpty()} if returning 
		 * only the default {@link org.apache.lucene.document.FieldType} is what 
		 * you want.
		 */
		private FieldTypeCache() {
			document = null;
			fieldConfig = Collections.emptyMap();
		}
		
		/**
		 * Construct a {@code FieldTypeCache} that contains the Lucene 
		 * {@link org.apache.lucene.document.FieldType}s for {@code document_}.
		 * 
		 * @param document_ a {@link DocType} read from {@link #CONFIGURATIONFILE}.
		 */
		public FieldTypeCache(final DocType document_) {
			document = document_;
			fieldConfig = Collections.unmodifiableMap(initFieldConfig(document));
		}

		/**
		 * Create a {@link FieldTypeCache} that returns 
		 * {@link ConfigurationHandler#DEFAULT_FT} for each field name.
		 * 
		 * Normally use {@link #FieldTypeCache(indexer.config.DocType) } to 
		 * construct {@code FieldTypeCache} that contains all configured fields
		 * for a {@link indexer.config.DocType} <i>and</i> returns the default 
		 * field for any field that is not configured.
		 * 
		 * @return	a singleton {@link FieldTypeCache} whose {@link #get} always
		 * 		returns {@link ConfigurationHandler#DEFAULT_FT}.
		 */
		public static FieldTypeCache createEmpty() {
			return EMPTY;
		}

		/**
		 * Retrieve the configured Lucene {@link org.apache.lucene.document.FieldType}
		 * for {@link #document}.{@code fieldName}.
		 * 
		 * When {@link #fields} does not contain an entry for {@code fieldName},
		 * use {@link ConfigurationHandler#convertToLuceneFieldType(indexer.config.FieldType)}
		 * to create it.
		 * 
		 * @param fieldName	the name of the field as configured in 
		 * 		{@link ConfigurationHandler#CONFIGURATIONFILE}{@code /<DocType>/<Field>/<Name>}.
		 * 
		 * @return <ul><li>the Lucene {@link org.apache.lucene.document.FieldType} as 
		 * 			configured for {@code fieldName}; or</li>
		 * 		<li>{@link ConfigurationHandler#DEFAULT_FT}, when {@link #document}
		 * 			does not contain a specific configuration for {@code fieldName}.
		 * 		</li></ul>
		 */
		public org.apache.lucene.document.FieldType get(final String fieldName) {
			if(fields.containsKey(fieldName)) {
				return fields.get(fieldName);
			} else if (fieldConfig.containsKey(fieldName)) {
				org.apache.lucene.document.FieldType luceneFieldType =
						ConfigurationHandler.convertToLuceneFieldType(
								fieldConfig.get(fieldName));
				fields.put(fieldName, luceneFieldType);
				return luceneFieldType;
			} else if (ALL_DEFAULT_FIELDS.containsKey(fieldName)) {
				return  ALL_DEFAULT_FIELDS.get(fieldName);
			} else {
				return ConfigurationHandler.DEFAULT_FT;
			}
		}
		
		/**
		 * Read all configured {@link indexer.config.FieldType}s from 
		 * {@code document} and put them in the returned map.
		 * 
		 * @param document	{@link indexer.config.DocType} whose fields to collect.
		 * 
		 * @return a {@link Map} that contains the 
		 * 		{@code <Field>/<Name>}–{@link indexer.config.FieldType}-pairs
		 * 		found in {@code document}.
		 */
		private static Map<String, indexer.config.FieldType> initFieldConfig(final DocType document) {
			Map<String, indexer.config.FieldType> result = new HashMap<String, FieldType>();
			for (FieldType fieldType : document.getField()) {
				result.put(fieldType.getName(), fieldType);
			}
			return result;
		}
	}

	private static class DocTypeCache {
		/**
		 * XML-object that contains the configuration of 
		 * {@link indexer.config.DocType}s.
		 */
		private final ConfigType config;
		
		/**
		 * Mapping of {@link ConfigurationHandler#CONFIGURATIONFILE}{@code /<DocType>/<FileType>}
		 * to (cached) {@link FieldTypeCache}.
		 * 
		 * <p><i>REMARK: {@link java.lang.System#gc()} is free to remove entries; 
		 * {@link #get(java.lang.String)} will (re-) create them when needed.
		 * </i></p>
		 */
		private final WeakHashMap<String, FieldTypeCache> documents =
				new WeakHashMap<String, FieldTypeCache>();

		/**
		 * Mapping of {@link ConfigurationHandler#CONFIGURATIONFILE}{@code /<DocType>/<FileType>}
		 * to {@link indexer.config.DocType} as read from {@link #config}.
		 * 
		 * <p>REMARK: Map is unmodifiable after it has been initialised.</p>
		 */
		private final Map<String, DocType> documentConfig;


		public DocTypeCache(final ConfigType config_) {
			config = config_;
			documentConfig = Collections.unmodifiableMap(initDocConfig(config_));
		}

		/**
		 * Retrieve the configured set of fields for the document type named
		 * {@code fileType}.
		 * 
		 * @param fileType	{@link ConfigurationHandler#CONFIGURATIONFILE}{@code /<DocType>/<FileType>}
		 * 		of the {@link indexer.config.DocType} whose fields to retrieve.
		 * 
		 * @return <ul><li>a {@link FieldTypeCache} for {@code fileType}; or</li>
		 * 		<li>{@link FieldTypeCache#createEmpty()}, when {@link #config}
		 * 		does not contain configuration for {@code fileType}.</li></ul>
		 */
		public FieldTypeCache get(final String fileType) {
			if(documents.containsKey(fileType)) {
				return documents.get(fileType);
			} else if (documentConfig.containsKey(fileType)) {
				FieldTypeCache ftCache = new FieldTypeCache(documentConfig.get(fileType));
				documents.put(fileType, ftCache);
				return ftCache;
			} else {
				return FieldTypeCache.createEmpty();
			}
		}

		/**
		 * Read all configured {@link indexer.config.DocType}s from 
		 * {@code config} and put them in the returned map.
		 * 
		 * @param config	{@link indexer.config.ConfigType} whose DocTypes to 
		 * 		collect.
		 * @return a {@link Map} that contains the 
		 * 		{@code <DocType>/<FileType>}–{@link indexer.config.DocType}-pairs
		 * 		found in {@code config}.
		 */
		private static Map<String, DocType> initDocConfig(final ConfigType config) {
			Map<String, DocType> result = new WeakHashMap<String, DocType>();

			for (DocType docType : config.getDocType()) {
				result.put(docType.getFileType(), docType);
			}
			
			return result;
		}
	}
	
	/**
	 * Default settings for Lucene {@link org.apache.lucene.document.FieldType}.
	 */
	private static final org.apache.lucene.document.FieldType DEFAULT_FT;
	static {
		DEFAULT_FT = new org.apache.lucene.document.FieldType();
		DEFAULT_FT.setTokenized(true);
		DEFAULT_FT.setStored(true);
		DEFAULT_FT.setStoreTermVectors(false);
		DEFAULT_FT.freeze();
	}

	/**
	 * Every document has the following Fields even if they are not configured.
	 * 
	 * @see FieldTypeCache#get(java.lang.String) 
	 */
	private enum DefaultFields {
		ID {
			@Override
			protected org.apache.lucene.document.FieldType initFieldType() {
				org.apache.lucene.document.FieldType idType = 
						new org.apache.lucene.document.FieldType(DEFAULT_FT);
				idType.setTokenized(false);
				idType.freeze();
				return idType;
			}
			
		},
		CONTENT {
			@Override
			protected org.apache.lucene.document.FieldType initFieldType() {
				return DEFAULT_FT;
			}
		};

		private final org.apache.lucene.document.FieldType type;
		
		private DefaultFields() {
			type = initFieldType();
		}
		
		public String getFieldName() {
			return this.name().toLowerCase();
		}
		
		public org.apache.lucene.document.FieldType getFieldType() {
			return type;
		}

		protected abstract org.apache.lucene.document.FieldType initFieldType(); 
	}
	
	private final static Map<String, org.apache.lucene.document.FieldType> ALL_DEFAULT_FIELDS;
	static {
		Map<String, org.apache.lucene.document.FieldType> result =
				new HashMap<String, org.apache.lucene.document.FieldType>();
		for (DefaultFields field : DefaultFields.values()) {
			result.put(field.getFieldName(), field.getFieldType());
		}
		ALL_DEFAULT_FIELDS = Collections.unmodifiableMap(result);
	}
	
	/* Set of VectorTypes for which lucene...FieldType.storeTermVectors should
	 * be set to true.
	 */
	private final static EnumSet<VectorType> STORE_TERM_VECTORS = 
			EnumSet.of(
				VectorType.YES, VectorType.WITH_OFFSETS, 
				VectorType.WITH_POSITIONS,
				VectorType.WITH_POSITIONS_OFFSETS);
	
	/* Set of VectorTypes for which lucene...FieldType.storeTermVectorOffsets
	 * should be set to true.
	 */
	private final static EnumSet<VectorType> STORE_TERM_VECTORS_OFFSETS = 
			EnumSet.of(
				VectorType.WITH_OFFSETS, VectorType.WITH_POSITIONS_OFFSETS);

	/* Set of VectorType for which lucene...FieldType.storeTermVectorPositions
	 * should be set to true.
	 */
	private final static EnumSet<VectorType> STORE_TERM_VECTORS_POSITIONS = 
			EnumSet.of(
				VectorType.WITH_POSITIONS, VectorType.WITH_POSITIONS_OFFSETS);

	private final static EnumSet<IndexType> INDEX =
			EnumSet.of(
				IndexType.TOKENIZED, IndexType.UNTOKENIZED, 
				IndexType.NO_NORMS);

	private final static EnumSet<IndexType> TOKENIZE =
			EnumSet.of(IndexType.TOKENIZED);

	private final static EnumSet<IndexType> OMITNORMS =
			EnumSet.of(IndexType.NO_NORMS);


  private ConfigType CT;
  private final String CONFIGURATIONFILE;
  private Map<String, String> ht;
  private final DocTypeCache documentTypes;
  
  /** logger for Commons logging. */
    private transient Logger log =
      Logger.getLogger("ConfigurationHandler.class.getName()");

  /** Creates a new instance of ConfigurationHandler */
  public ConfigurationHandler(String configurationFile) {
    
    if (! new File(configurationFile).exists())
      throw new RuntimeException(configurationFile + " not found");
    
    CONFIGURATIONFILE = configurationFile;
    unMarshallIt();
	
	assert (CT != null);
	documentTypes = new DocTypeCache(CT);
	
    // create a Hashtable for the extentions to doctypes mapping
    // and fill it;
    ht = new HashMap<String, String>();
    getExtensionsToType();
  }

  private void unMarshallIt(){
    CT = null;
    try {

      JAXBContext jc = JAXBContext.newInstance("indexer.config", this.getClass().getClassLoader());
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      
      File cfg = new File(CONFIGURATIONFILE);
      
      if (cfg.exists() != true) 
        System.err.println("Config File does not exist!");

      JAXBElement<?> O = (JAXBElement<?>)unmarshaller.unmarshal(cfg);
      CT = (ConfigType) O.getValue();    
    } catch(JAXBException e ) {
      if (log.isLoggable(Level.SEVERE))
        log.severe(e.toString());
    }
  }

  /**
   * Returns the name of the ConfigType
   * @return              Name 
  */
  public String getName() {
    return CT.getName();
  }
  
  /**
   * Returns the name of the ConfigType
   * @return              Name 
  */
  public void setName(String name) {
    CT.setName(name);
  }
  
  /**
   * Returns the value of the overwrite switch
   * @return              true or false
  */
  public boolean OverWrite() {
    return CT.isIndexOverwrite().booleanValue();
  }
  
  /**
   * Sets the value of the overwrite switch
   * @return              true or false
  */
  public void SetOverWrite(boolean overwrite) {
    CT.setIndexOverwrite(overwrite);
  }

  /**
   * Gets the value of the creator property
   * @return              The value of the creator property
  */
  public String getCreator() {
    String result = CT.getCreator();
    if(result != null)
      return result;
    else
      return "unknown";
  }

  /**
   * Gets the value of the dataPath property.
   * @return              dataPath          
  */
  public String getDataPath() {
    String result = CT.getDataPath();
    if(result != null)
      return result;
    else
      return "data";
  }

  /**
   * Gets the value of the mergeFactor property.
   * @return              mergeFactor
  */
  public int getMergeFactor() {
    int result = CT.getMergeFactor().intValue();
    if(result != 0)
      return result;
    else
      /* DEFAULT VALUE */
    return 10;
  }
  
  /**
   * Gets the value of the maxBufferedDocs property.
   * @return              maxBufferedDocs        
  */
  public int getMaxBufferedDocs() {
    int result = CT.getMaxBufferedDocs().intValue();
    if(result != 0)
      return result;
    else
      /* DEFAULT VALUE */
      return 10;
  }

  /**
   * Gets all defined fields for a doctype
   *
   * @param    Doc        Document type
   * @return              name
   */
  @Deprecated
  public String[] getFields(String Doc) {
    
    DocType doc; 
    FieldType field; 

    List<FieldType> fields = null;
    List<DocType> docs = CT.getDocType();
    
    for(Iterator it = docs.iterator(); it.hasNext();) {
      doc = (DocType)it.next();
      if(doc.getFileType().equals(Doc)){
        fields = doc.getField();
        break;
      }
    }
    
    String[] result = new String[fields.size()+2];
    int cnt = 2;
    result[0] = "id";
    result[1] = "content";
    
    for(Iterator it = fields.iterator(); it.hasNext();) {
      field = (FieldType)it.next();
      result[cnt] = field.getName();
      cnt++;
    }
    
    return result;

  }
  
  /**
   * Gets the value of the GlobalAnalyzer property.
   * @return              GlobalAnalyzer
  */
  public String getGlobalAnalyzer() {
    String result = CT.getIndexAnalyzer();
    if(result != null)
      return result;
    else
      /* DEFAULT VALUE */
      return "STANDARD";
  }
  
  

  /**
   * Gets the value of the Index from Field of DocType
   * ....repetion of code here ... not very neat
   *
   * @param    Doc        Document type
   * @param    FieldName  Name of the field
   * @return              Field.Index
   */
  @Deprecated
  public Field.Index getFieldIndexValue(String Doc, String FieldName) {
    DocType doc; FieldType field; String result=null;
    List<FieldType> fields = null;
    List<DocType> docs = CT.getDocType();
    for(Iterator it = docs.iterator(); it.hasNext();) {
      doc = (DocType)it.next();
      if(doc.getFileType().equals(Doc)){
        fields = doc.getField();
        break;
      }
    }
    for(Iterator it = fields.iterator(); it.hasNext();) {
      field = (FieldType)it.next();
      if (field.getName().equals(FieldName)) {
        result = field.getIndex().value();
        break;
      }
    }
    if(result==null)
      /* DEFAULT VALUE */
      return Field.Index.TOKENIZED;
    else
      if(result.equalsIgnoreCase("UN_TOKENIZED"))
        return Field.Index.UN_TOKENIZED;
      if(result.equalsIgnoreCase("NO"))
        return Field.Index.NO;
      if(result.equalsIgnoreCase("NO_NORMS"))
        return Field.Index.NO_NORMS;
      if(result.equalsIgnoreCase("TOKENIZED"))
        return Field.Index.TOKENIZED;

      return Field.Index.TOKENIZED;
  }

  /**
   * Gets the value of the Store from Field of DocType
   * ....repetion of code here ... not very neat
   *
   * @param    Doc        Document type
   * @param    FieldName  Name of the field
   * @return              Field.Store
   */
  @Deprecated
  public Field.Store getFieldStoreValue(String Doc, String FieldName) {
    DocType doc; FieldType field; String result=null;
    List<FieldType> fields = null;
    List<DocType> docs = CT.getDocType();
    
    try {
      for(Iterator it = docs.iterator(); it.hasNext();) {
        doc = (DocType)it.next();
        if(doc.getFileType().equals(Doc)){
          fields = doc.getField();
          break;
        }
      }
      for(Iterator it = fields.iterator(); it.hasNext();) {
        field = (FieldType)it.next();
        if (field.getName().equals(FieldName)) {
          result = field.isStore().toString();
          break;
        }
      }
      
      if (result==null)
        /* DEFAULT VALUE */
        return Field.Store.YES;
      else
        if (result.equalsIgnoreCase("true"))
          return Field.Store.YES;
        else 
          return Field.Store.NO;
    } catch(Exception ex) {
      System.err.println(ex.toString());
      return Field.Store.YES;
    }
  }

  /**
   * Gets the value of the TermVector from Field of DocType
   * ....repetion of code here ... not very neat
   *
   * @param    Doc        Document type
   * @param    FieldName  Name of the field
   * @return              Field.TermVector
   */
  @Deprecated
  public Field.TermVector getTermVectorValue(String Doc, String FieldName) {
    DocType doc; 
    FieldType field; 
    String result = null;
    List<FieldType> fields = null;
    List<DocType> docs = CT.getDocType();
    
    for(Iterator it = docs.iterator(); it.hasNext();) {
      doc = (DocType)it.next();
      if(doc.getFileType().equals(Doc)) {
        fields = doc.getField();
        break;
      }
    }
    
    for(Iterator it = fields.iterator(); it.hasNext();) {
      field = (FieldType)it.next();
      if (field.getName().equals(FieldName)) {
        result = field.getTermvector().value();
        break;
      }
    }
    
    if(result==null)
      /* DEFAULT VALUE */
      return Field.TermVector.NO;
    else
      if(result.equalsIgnoreCase("NO"))
        return Field.TermVector.NO;
      if(result.equalsIgnoreCase("YES"))
        return Field.TermVector.YES;

      return Field.TermVector.NO;
}

  
  /**
   * Retrieve the Lucene {@link org.apache.lucene.document.FieldType} as
   * configured in {@link #CONFIGURATIONFILE}{@code /<DocType>[./<FileType>=={doc}]/<Field>[./<Name>=={field}]}.
   * 
   * <p><i>NOTE: the returned field is 
   * {@link org.apache.lucene.document.FieldType#freeze() frozen}.</i></p>
   * 
   * @param doc	value of {@code <FileType>} for the desired document.
   * @param field	value of {@code <Name>} for the desired field within the document.
   * 
   * @return <ul><li>a Lucene {@link org.apache.lucene.document.FieldType} 
   * 		set as configured; or</li>
   * `		<li>{@link #DEFAULT_FT}, when no specific configuration entry for 
   * 		the field exists.</li></ul>
   * 
   * @see #convertToLuceneFieldType(indexer.config.FieldType) 
   */
  public org.apache.lucene.document.FieldType getFieldType(String doc, String field) {
	return documentTypes.get(doc).get(field);
  }

  /**
   * Convert indexer.config.FieldType to Lucene's FieldType
   * 
   * @param src	indexer.config.FieldType to convert.
   * @return {@code src} converted to Lucene.
   */
  private static org.apache.lucene.document.FieldType convertToLuceneFieldType(
		  indexer.config.FieldType src) {
	org.apache.lucene.document.FieldType result =
			new org.apache.lucene.document.FieldType(DEFAULT_FT);

	result.setStored(src.isStore());

	result.setIndexed(INDEX.contains(src.getIndex()));
	result.setTokenized(TOKENIZE.contains(src.getIndex()));
	result.setOmitNorms(OMITNORMS.contains(src.getIndex()));
	
	result.setStoreTermVectors(STORE_TERM_VECTORS.contains(src.getTermvector())); 
	result.setStoreTermVectorOffsets(STORE_TERM_VECTORS_OFFSETS.contains(src.getTermvector()));
	result.setStoreTermVectorPositions(STORE_TERM_VECTORS_POSITIONS.contains(src.getTermvector()));

	result.freeze();
	return result;
  };
		  

  /**
   * Gets the value of the Description from Field of DocType
   * ....repetion of code here ... not very neat
   *
   * @param    Doc        Document type
   * @param    FieldName  Name of the field
   * @return              Description
   */
  public String getFieldDescription(String Doc, String FieldName) {
    DocType doc; 
    FieldType field; 
    String result=null;
    List<FieldType> fields = null;
    List<DocType> docs = CT.getDocType();
    
    for(Iterator it = docs.iterator(); it.hasNext();) {
      doc = (DocType)it.next();
      if (doc.getFileType().equals(Doc)) {
        fields = doc.getField();
        break;
      }
    }
    
    for(Iterator it = fields.iterator(); it.hasNext();) {
      field = (FieldType)it.next();
      if (field.getName().equals(FieldName)) {
        result = field.getDescription();
        break;
      }
    }
    
    if(result==null)
      /* DEFAULT VALUE */
      return "empty";
    else
      return result;
  }

  /**
   * Gets the value for the analayzer at documentType level
   *
   * @param    Doc        Document type
   * @return              Analyzer
   */
  public String getDocumentAnalyzer(String Doc) {
    DocType doc;  
    String result = null;
    List<DocType> docs = CT.getDocType();
    
    for(Iterator it = docs.iterator(); it.hasNext();) {
      doc = (DocType)it.next();      
      if (doc.getFileType().equals(Doc)) {
        result = doc.getDocTypeAnalyzer();
        break;
      }
    }
    
    if (result==null)
      return getGlobalAnalyzer();
    else
      return result; 
  }
  
  /**
   * Gets the value for the extension
   *
   * @param    Doc        Document type
   * @return              extension
   */
  public List<String> getDocumentExtensions(String Doc) {   
    DocType doc;  
    List<String> result=null;
    List<DocType> docs = CT.getDocType();
    
    for(Iterator it = docs.iterator(); it.hasNext();) {
      doc = (DocType)it.next();
      if (doc.getFileType().equals(Doc)) {
        result = doc.getFileExtension();
        break;
      }
    }
    
    if (result==null)
      return null;
    else
      return result; 
  }

  /**
   *  Return the list of document types in the config file
   *  This is used to dtermine the appropriate DocumentHandler for indexing
   *
   * @return              List of types
  */
  public List<String> getDocumentTypes() {
     DocType doc;  
     ArrayList<String> result = new ArrayList<String>();
     List<DocType> docs = CT.getDocType();
     
     for(Iterator it = docs.iterator(); it.hasNext();) {
       doc = (DocType)it.next();
       result.add(doc.getFileType());
     }
     
     return result; 
 }

  /**
   * Build the hashtable for lookup of documenttypes given the file extension
  */
  private void getExtensionsToType() {
     DocType doc;  
     List<DocType> docs = CT.getDocType();
     List<String>  exts;
     
     for(Iterator it = docs.iterator(); it.hasNext();) {
       doc = (DocType)it.next();
       exts = getDocumentExtensions(doc.getFileType());
       
       for(Iterator i = exts.iterator(); i.hasNext();) {
         ht.put((String)i.next(), doc.getFileType());
       }
     }
   }
  
  /**
   *  Gets document type for a particular extension
   *
   * @return              document type
  */
   public String getDocType(String extension) {
     if (extension == null || extension.length() == 0) return null;
     return ht.get(extension);
   }
}