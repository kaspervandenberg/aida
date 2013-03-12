package indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Logger;
import org.apache.lucene.index.IndexWriter;


/**
 *
 * @author Machiel Jansen, Edgar Meij
 */
public class MedlineHandler extends DocHandler {
  
  private StringBuffer storedString;
  private final String TYPE = "medline";
  private String State;
  private final IndexAdder adder;
  private IndexWriter iw;

  /** Creates a new MedlineHandler */
  public MedlineHandler(ConfigurationHandler config) {
    storedString = new StringBuffer(500);
    State = "NONE";   
    adder = new IndexAdder(config);
  }
  
 /**
 * Return all possible field names
 *
 * @return   Array of field names
 */
  public String[] getFieldNames(){
    return new String[] { "PMID", "AB", "TI", "AU", "FAU", "MH", "SO" };
  }
  
  /**
   * Adds a file to the index
   *
   * @param   writer      IndexWriter to use
   * @param   file        File to add
   */
  public void addDocumentToIndex(IndexWriter writer, File file) 
          throws DocumentHandlerException {
    
    
    try {
      
      iw = writer;
      BufferedReader in = new BufferedReader(new FileReader(file));
      String str;
      StringBuffer sb = new StringBuffer();
      String[] codes = getFieldNames();
      boolean FLAG = false;
      
      while ((str = in.readLine()) != null) {
        
        sb.append(str);
        FLAG = false;
        
        for(int i=0; i < codes.length; i++) {
          FLAG = structureCheck(codes[i], str);
          if (!FLAG)
            break;
        }
        
        if (str.startsWith("      ")) {
          changeS("BLANK", str);
        }

        // Added AllFields upto the sb.delete
        // TODO: check if new PMID, then write
        if (str.length() == 0) {
          try {
            adder.addFieldToDocument(TYPE, "content", sb.toString());
            adder.addFieldToDocument(TYPE, "path", file.getPath());
            adder.addFieldToDocument(TYPE, "url", file.toURI().toString());
            adder.writeDocument(TYPE, writer);
            sb.delete(0, sb.capacity());
            changeS("NONE", str);
          } catch(Exception ex) {
            System.err.println("HERE: " + ex.toString());
          }
        }
      }
      
      adder.addFieldToDocument("medline", "content", sb.toString());
      adder.writeDocument("medline", writer);
      sb.delete(0, sb.capacity());
      changeS("NONE", str);

      in.close();

      } catch (Exception e) {
        throw new DocumentHandlerException(e.toString());
      } finally {
        
      }
   }
   
  /** 
   * changestate to the code ("AB" or "TI") and remove it 
   * 
  */
  private boolean structureCheck(String code, String string) {
    if (string.startsWith(code))
      changeS(code, Utilities.splitLine(string));
    return true;
  }

  private boolean changeS(String newState, String line) {
    if (newState.equalsIgnoreCase("BLANK")) {
      storeString(line);
      return true;
    }
    
    if (State.equalsIgnoreCase("NONE") & !newState.equalsIgnoreCase("NONE")) {
      storeString(line);
      State = newState;
      return true;
    }
    
    if (!State.equalsIgnoreCase("NONE") & !newState.equalsIgnoreCase("NONE")) {
      addTheField(State);
      storeString(line);
      State = newState;
      return true;
    }
    
    if (newState.equalsIgnoreCase("NONE") & !State.equalsIgnoreCase("NONE")){
      try {
        adder.writeDocument("medline", iw);
        State = newState;
      } catch(Exception e) {
        System.err.println(e.getMessage());
      }
      return true;        
    }
      
    return true;
  }
   
  private void storeString(String s) {
    
    if (storedString.length() == 0) {
      storedString.append(s);
    } else {
      storedString.append("\n");
      storedString.append(s);
    }     
  }
  
  private void addTheField(String Code) {
    if (Code.length() != 0) {
      adder.addFieldToDocument("medline", Code, storedString.toString());
      storedString.delete(0, storedString.length());
      State = Code;
    }
  }
}