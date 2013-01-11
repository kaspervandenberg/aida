/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.science.wsdtf.Tests;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.uva.science.wsdtf.utilities.Constants;
import nl.uva.science.wsdtf.utilities.StreamConfig;
import org.apache.axis.AxisEngine;

/**
 *
 * @author skoulouz
 */
public class TestConf {

    public void createConfig() {
        URI server;
        try {
            server = new URI("tcp://" + getIP() + ":8199");
            StreamConfig conf = getConf(server, "TCP");
            System.out.println("Configurations:\n Max Buffer Size:"+Constants.MAX_BUFFER_SIZE+
                "\n Protocol:"+conf.translateProtocol("TCP")+
                "\n Host name:"+conf.host+
                "\n Encryption:"+conf.ecnryptData+
                "\n Authenticate:"+conf.authenticate+
                "\n Port:"+conf.port+
                "\n Path:"+conf.path+
                "\n file name"+conf.fileName+
                "\n LogLevel: "+Constants.LOG_LEVEL+
                "\n Display Speed: "+Constants.displaySpeed+
                "\n Save Logs: "+ Constants.saveLogs+
                "\n File Requested: "+ conf.fileRequest);
            
        } catch (URISyntaxException ex) {
            Logger.getLogger(TestConf.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private StreamConfig getConf(URI source, String protocol) {
        StreamConfig conf = new StreamConfig();
        conf.displaySpeed = false;
        conf.protocol = protocol;

        if (protocol.equals("HTTP")) {
            conf.fileRequest = source.getPath();
            conf.host = "http://" + source.getHost();
            conf.port = source.getPort();
        } else {
            conf.host = source.getHost();
            conf.port = 8199;
        }

        conf.loggLevel = "WARNING";

        conf.maxBufferSize = 8388608;
        conf.ecnryptData = false;
        conf.authenticate = false;
        conf.displaySpeed = false;
        conf.saveLogs = false;

        return conf;
    }

    public void crateConfFile(){
        nl.uva.science.wsdtf.utilities.Initilize init = new nl.uva.science.wsdtf.utilities.Initilize();
        init.createConfigFile("/tmp/testConf.xml");
    }
    
    private String getIP() {
        /*  URL addr = null;
        try {
        addr = new URL((String) AxisEngine.getCurrentMessageContext().getProperty("transport.url"));
        } catch (MalformedURLException ex) {
        ex.printStackTrace();
        }
        return addr.getHost();*/
        String hostname = null;
        try {
            InetAddress addr = InetAddress.getLocalHost();

            // Get IP Address
            byte[] ipAddr = addr.getAddress();

            // Get hostname
            hostname = addr.getHostName();
        } catch (UnknownHostException e) {
        }
        return hostname;

    }
}
