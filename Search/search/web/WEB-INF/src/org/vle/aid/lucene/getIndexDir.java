/*
 * getIndexDir.java
 *
 * Created on March 28, 2006, 5:59 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.vle.aid.lucene;

import java.util.logging.Logger;

/**
 *
 * @author emeij
 */
public class getIndexDir {
    
    /** logger for Commons logging. */
    private static transient Logger log =
	 Logger.getLogger("getIndexDir.class.getName()");    
    
    public static String getIndexDir() {
        
        log.fine("Checking for env. var. INDEXDIR");
        String indexDir = System.getenv("INDEXDIR");
        
        if (indexDir == null) {
            log.severe("***INDEXDIR not found!!!***");
            indexDir = "";
        } else {
            log.info("Found INDEXDIR: " + indexDir);
        }        
        
        return indexDir;
    }
    
}
