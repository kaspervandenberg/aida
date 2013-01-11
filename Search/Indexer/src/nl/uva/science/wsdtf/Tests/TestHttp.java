/*
 * TestHttp.java
 *
 * Created on May 23, 2008, 2:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nl.uva.science.wsdtf.Tests;

import java.io.IOException;
import java.net.URL;
import nl.uva.science.wsdtf.io.Connector;
import nl.uva.science.wsdtf.utilities.Initilize;

/**
 *
 * @author alogo
 */
public class TestHttp {
    private Initilize init;
    private String confFilePath;
    /** Creates a new instance of TestHttp */
    public TestHttp(String confiFile) {
        confFilePath = confiFile;
    }
    
    public void wget(){
        Connector conn = initTransfer(confFilePath);
        conn.save("/home/skoulouz/testTrans/client/wgetFile");
    }
    
    private Connector initTransfer(String confiFile){
        Connector conn = null;
        try {
            init = new Initilize(confiFile);
            init.setPort(init.getPort());
            init.client();
            conn = init.getConnector();
            init.startClient();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return conn;
    }
    
}
