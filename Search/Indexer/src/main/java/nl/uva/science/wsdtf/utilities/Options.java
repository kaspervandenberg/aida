/*
 */
package nl.uva.science.wsdtf.utilities;

/**
 * 
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;
import org.globus.gsi.gssapi.auth.Authorization;
import org.ietf.jgss.GSSCredential;



/**
 * The Class Options. This class is used as an argument in almost 
 * every other class. It contains data members, 
 * such as the protocol the client and the server 
 * will use. Specific protocol configurations also 
 * go here. Every implementation of this class 
 * must provide a URI, for specifying the server 
 * location, and the port that client and server 
 * will communicate on. This class also provide a 
 * vector for storing URIs, that specify file 
 * locations, something that can be used by the 
 * producing WS to publish file locations to the 
 * consuming WS.
 *@author S. Koulouzis
 */
public class Options {
	
	/** The protocol. */
	public int protocol;
	
	/** The server location. */
	public String hostname;
	
	/** The port, used by client and server. */
	public int port;
	
	/** Configuration file location for use with the StyxServer */
	public String configFile;
	/** Username and passwd for the StyxClient */
	public String username;
	public String passwd;
	
	public SocketAddress socketAddress=null;
	
	public boolean isServer;
		
	public GSSCredential credentials = null;
	
	public byte[] Bytecredentials = null;
	
	public Authorization authorization = null;
		
	public int concurentConnections;
	public String fileRequest;
	
	public String pathToSave;
        
        public boolean keepStreamAlive;
	/**
	 * When set to 1 the server is whaiting for each packet to reach the client, 2 checks every two pacets e.t.c.
	 */
	public int udpSpeed = 1;
	
	public boolean encryptData=false;
        
        public boolean authentication = true;
	
	public boolean liveData=false;
        
        public InputStream in;
        public OutputStream out;
	
	/**
	 * Instantiates a new options with only the protocol and the port
	 * 
	 * @param protocol the protocol
	 * @param port the port
	 */
	public Options(int protocol,int port){
		this.protocol = protocol;
		this.port = port;
	}
	
	/**
	 * Instantiates a new options with the protocol the port and the server 
	 * location. The server location is used to inform the client by its WS where to find the server
	 * 
	 * @param protocol the protocol
	 * @param port the port
	 * @param serverLocation the server location
	 */
	public Options(int protocol,SocketAddress socketAddress){
		this.protocol = protocol;
		this.socketAddress = socketAddress;
	}
	
	/**
	 * Gets the protocol.
	 * 
	 * @return the protocol
	 */
	public int getProtocol(){
		return this.protocol;
	}
	
	/**
	 * Gets the port.
	 * 
	 * @return the port
	 */
	public int getPort(){
		return this.port;
	}
		
	/**
	 * Gets the server location.
	 * 
	 * @return the server location
	 */
	public SocketAddress getSocketAddress(){
		return this.socketAddress; 
	}
	
	public boolean isServer(){
		return this.isServer;
	}
	
	public GSSCredential getCredential(){
		return credentials;
	}
	
	public Authorization getAuthorization(){
		return this.authorization;
	}
		
	public String getHostName(){
		return this.hostname;
	}
	
	public String getConfigFilename(){
		return configFile;
	}
	
	public String getUsername(){
		return username;
	}
	
	public String getPasswd(){
		return passwd;
	}
		
}
