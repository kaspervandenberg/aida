/*
 * getFields.java
 *
 * Created on February 8, 2006, 3:06 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.vle.aid.lucene;


import org.apache.lucene.index.*;
import java.util.*;
import java.io.*;
import java.util.logging.Logger;
import java.io.IOException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author emeij
 */

public class getFields { 
    
    /** logger for Commons logging. */
    private static transient Logger log =
	 Logger.getLogger("getFields.class.getName()"); 
    
    private static String[] idxFields = null;
	private static int numDocs = -1;

  public String listFieldsJason(String index) {
		
    String json = "{fields:[";
    String indexLocation = System.getenv("INDEXDIR");

    if (indexLocation == null) { 
      log.severe("***INDEXDIR not found!!!***");
      indexLocation = "";
      return null;
    }
        
    String[] fields = getFieldsArray(indexLocation + System.getProperty("file.separator") + index);
    
    for (int i=0; i < fields.length; i++) {
      if (fields[i].equalsIgnoreCase("") || fields[i] == null ) {
        log.info("Strange field: " + fields[i]);
      } else {
        json += "{field:'"+fields[i]+"'},";
      }
    }
    
    if (fields.length > 0) 
      json = json.substring(0,json.length()-1) + "]}";
    else 
      json += "]}";
    
    return json;
  }        
        
  public String[] listFields(final java.lang.String index) {
		
    String indexLocation = System.getenv("INDEXDIR");
    log.fine("Checking for env. var. INDEXDIR");

    if (indexLocation == null) {
      log.severe("***INDEXDIR not found!!!***");
      indexLocation = "";
      return null;
    } else {
      log.fine("Found INDEXDIR: " + indexLocation);
    }
        
        return getFieldsArray(indexLocation + System.getProperty("file.separator") + index);
  }
	
	public static void main (String[] args) {
		try {
			String[] fields = getFieldsArray(args[0]);
			System.out.println(" - Fields: ");
			for (int i=0;i<fields.length;i++) {
				System.out.println(fields[i]);
			}
			System.out.println(" - Docs: " + numDocs);
			System.out.println();
        } catch (ArrayIndexOutOfBoundsException aoe) {
			System.err.println("Define location of indexdir");
        } 			
	}
	
	private static String[] getFieldsArray(String indexLocation) {
		
		// No 1.5 on Mac os x...
		//TreeSet <String> fields = new TreeSet <String> ();
		TreeSet fields = new TreeSet ();
		IndexReader reader = null;
		
		try {
			Directory indexDir = FSDirectory.open(new File(indexLocation));
			reader = DirectoryReader.open(indexDir);	
			// No 1.5 on Mac os x...
			// Collection <String> fieldsCollection = reader.getFieldNames();
			FieldInfos fieldInfos = MultiFields.getMergedFieldInfos(reader);	
			
			numDocs = reader.numDocs();
			reader.close();
			
			log.fine("Index " + indexLocation + 
					" has got " + fieldInfos.size() + " fields");
			
			// Iterate over found fields
			java.util.Iterator<FieldInfo> it = fieldInfos.iterator();
			
			while (it.hasNext()) {
				FieldInfo fld = it.next();
				fields.add(fld.name);
			}
			
        } catch (ArrayIndexOutOfBoundsException aoe) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            aoe.printStackTrace(pw);
			
            log.severe(sw.toString());
            pw.close();             
        } catch(IOException e) {
            log.info("IOError: " + e.getMessage());
        }	
		
		return (String[]) fields.toArray(new String[0]);
	}
    
}
