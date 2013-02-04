/*
 * TestGFTP.java
 *
 * Created on June 15, 2008, 6:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nl.uva.science.wsdtf.Tests;

import nl.uva.science.wsdtf.utilities.Initilize;

/**
 *
 * @author alogo
 */
public class TestGFTP {
    private Initilize init;
    private String confFilePath;
    
    /** Creates a new instance of TestGFTP */
    public TestGFTP(String confiFile) {
        confFilePath = confiFile;
    }
    
    public void getFIle(String file){
        init.getConf().fileRequest = file;
    }
}
