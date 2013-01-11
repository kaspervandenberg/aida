/*
 * createLegalSpansJDBM.java
 *
 * Created on July 15, 2006, 12:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

import java.io.IOException;
import java.io.File;
import java.util.HashMap;
import java.util.Properties;
import java.io.FileReader;
import java.io.LineNumberReader;

/**
 *
 * @author edgar
 */
public class createLegalSpansJDBM { 
    
    /** Creates a new instance of createLegalSpansJDBM */
    public createLegalSpansJDBM() {
    }
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        
        FileReader          freader = null;
        LineNumberReader    lnr = null;        
        String              dirName = null;
        String              inputFile = null;
        RecordManager       recman = null;
        HTree               hashtable = null;  
        String              currentPMID = "0";
        
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-d")) {
                i++;
                if (i >= args.length)
                        throw new IllegalArgumentException("Specify targetdir after -d");
                dirName = args[i];
            } else if (args[i].equals("-i")) {
                i++;
                if (i >= args.length)
                        throw new IllegalArgumentException("Specify inputfile after -i");
                inputFile = args[i];
            } else {
                throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }
        
        if (dirName == null) 
            throw new IllegalArgumentException("Must specify targetdir after -d");
        if (inputFile == null) 
            throw new IllegalArgumentException("Must specify inputFile after -i");        

        File location = new File(dirName);
        
        // true if created
        if (location.mkdir()) {
            System.err.println(dirName + " succesfully created");
        } else {
            System.err.println(dirName + " exists");
        }

        // create or open index collection manager
        Properties props = new Properties();
        // props.setProperty("fileName", "");
        
        recman = RecordManagerFactory.createRecordManager(location.toString() +
               System.getProperty("file.separator") + "legalspans", props);
        
        // create or load index synonyms (hashtable)
        long recid = recman.getNamedObject("PMID");
        if ( recid != 0 ) {
            System.err.println("File exists, delete it first");
            System.exit(0);
            //hashtable = HTree.load( recman, recid );
        } else {
            System.err.println("Creating new JDBM");
            hashtable = HTree.createInstance( recman );
            recman.setNamedObject( "PMID", hashtable.getRecid() );
        }
        
        try {
            freader = new FileReader(new File(inputFile));
            lnr = new LineNumberReader(freader);
            String word = null;
            while ((word = lnr.readLine()) != null) {
                if (word.length() > 0) {
                    String[] terms = word.split("[\t +]");
                    HashMap map = null;
                    if (terms.length != 3) {
                        System.err.println(" - Suspicious line: [" + word + "] length: " + terms.length);
                    } else {
                        if (currentPMID.equalsIgnoreCase(terms[0])) {
                            //System.err.println("   - Adding to " + terms[0]);
                            map = (HashMap) hashtable.get(currentPMID);            
                        } else {
                            System.err.println(" - Processing " + terms[0]);
                            currentPMID = terms[0];
                            map = new HashMap();
                        }
                        
                        if (map == null) {
                            System.err.println(" - Something went wrong with: " + currentPMID);
                        } else {
                            map.put(terms[1], terms[2]);
                            hashtable.put(currentPMID, map);
                            recman.commit();
                            map.clear();                        
                        }
                    }
                }
            }
        }
        
        finally {
            if (lnr != null)
                lnr.close();
            if (freader != null)
                freader.close();
            if (recman != null)
                recman.close();
            System.err.println(" - Done");
        }        
        
    }
    
}
