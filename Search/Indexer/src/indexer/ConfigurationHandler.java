package indexer;

import indexer.config.ConfigType;
import indexer.config.DocType;
import indexer.config.FieldType;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
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

  private ConfigType CT;
  private final String CONFIGURATIONFILE;
  private Hashtable<String, String> ht;
  
  /** logger for Commons logging. */
    private transient Logger log =
      Logger.getLogger("ConfigurationHandler.class.getName()");

  /** Creates a new instance of ConfigurationHandler */
  public ConfigurationHandler(String configurationFile) {
    
    if (! new File(configurationFile).exists())
      throw new RuntimeException(configurationFile + " not found");
    
    CONFIGURATIONFILE = configurationFile;
    unMarshallIt();
    // create a Hashtable for the extentions to doctypes mapping
    // and fill it;
    ht = new Hashtable<String, String>();
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