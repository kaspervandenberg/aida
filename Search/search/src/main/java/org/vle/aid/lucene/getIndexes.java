/*
 * getIndexes.java
 *
 * Created on March 25, 2006, 11:09 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.vle.aid.lucene;

import java.io.IOException;
import java.util.logging.Level;
import org.apache.lucene.index.*;

import java.util.*;
import java.util.logging.Logger;
import java.io.File;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author edgar
 */
public class getIndexes {
    
    private final String defaultIndexLocation = System.getenv("INDEXDIR") + 
            System.getProperty("file.separator");
    
    /** logger for Commons logging. */
    private transient Logger log =
	 Logger.getLogger("getIndexes.class.getName()"); 
    
    private String[] indexes = null;
    
  public String[] listIndexes(String indexLocation) {          
    if (indexLocation == null)
      indexLocation = defaultIndexLocation;

     Collection  <String> resultList = new ArrayList <String> ();

    IndexReader reader = null;
    File indexDir = new File(indexLocation);
    File listIndexes[] = indexDir.listFiles();

    for (int k = 0; k < listIndexes.length; ++k) {
			try {
				Directory indexDirectory = FSDirectory.open(listIndexes[k]);	
				if (DirectoryReader.indexExists(indexDirectory))
				  resultList.add(listIndexes[k].getName());
			} catch (IOException ex) {
				Logger.getLogger(getIndexes.class.getName()).log(
						Level.SEVERE,
						String.format("Unable to open index %s", listIndexes[k]),
						ex);
			}
    }

    return resultList.toArray(new String[0]);
  }
    
  public String listIndexesJason(String indexLocation) {
    
    String[] indexArray = listIndexes(indexLocation);
    String json = "{indexes:[";
    
    for (int i=0; i < indexArray.length; i++) {
      if (indexArray[i].equalsIgnoreCase("") || indexArray[i] == null ) {
        log.info("Strange index: " + indexArray[i]);
      } else {
        json += "{index:'"+indexArray[i]+"'},";
      }
    }
    
    if (json.length() != 10)
      json = json.substring(0,json.length()-1) + "]}";

    return json;
  }
    
    public String[] listDefaultIndexes() {
        return listIndexes(defaultIndexLocation);
    }
}
