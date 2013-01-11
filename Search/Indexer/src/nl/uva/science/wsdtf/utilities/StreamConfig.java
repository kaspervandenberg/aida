/*
 * StreamConfig.java
 *
 * Created on March 11, 2008, 3:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nl.uva.science.wsdtf.utilities;

import javax.xml.bind.annotation.*;

import java.net.SocketAddress;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * A configuration class to set the connections parameters 
 * @author S. Koulouz
 */
@XmlRootElement
public class StreamConfig {
    
    @XmlElement
    public String protocol;
    
    @XmlElement
    public String host;
    
    @XmlElement
    public String username;
    
    @XmlElement
    public String path;
    
    @XmlElement
    public String password;
    
    @XmlElement
    public String applicationName;
    
    @XmlElement
    public String fileName;
    
    @XmlElement
    public String fileRequest;
    
    @XmlElement
    public String loggLevel;
    
    @XmlElement
    public int port;
    
    @XmlElement
    public int maxBufferSize;
    
    @XmlElement
    public int numOfStreams;
        
    @XmlElement
    public boolean ecnryptData;
    
    @XmlElement
    public boolean authenticate;
        
    @XmlElement
    public boolean keepStreamAlive;
    
    @XmlElement
    public boolean displaySpeed;
    
    @XmlElement
    public boolean saveLogs;
    
    
    
    /** Creates a new instance of StreamConfig */
    public StreamConfig() {
    }
    
    
    public int translateProtocol(String val){
        if(val.equalsIgnoreCase("GridFTP")){
            return Constants.GridFTP;
        }
        if(val.equalsIgnoreCase("jStyx") || val.equalsIgnoreCase("STYX")){
            return Constants.STYX;
        }
        if(val.equalsIgnoreCase("RTSP")){
            return Constants.RTSP;
        }
        if(val.equalsIgnoreCase("UDP")){
            return Constants.UDP;
        }
        if(val.equalsIgnoreCase("TCP")){
            return Constants.TCP;
        }
        if(val.equalsIgnoreCase("HTTP")){
            return Constants.HTTP;
        }
        return -999;
    }
    
    
    public String translateProtocol(int val){
        String str=null;
        switch (val) {
            case Constants.TCP:
                str = "TCP";
                break;
            case Constants.UDP:
                str = "UDP";
                break;
            case Constants.RTSP:
                str = "RTSP";
                break;
            case Constants.STYX:
                str = "STYX";
                break;
            case Constants.GridFTP:
                str = "GridFTP";
                break;
            case Constants.HTTP:
                str = "HTTP";
                break;
            default:
                str = "TCP";
                break;
        }
        return str;
    }
    
}
