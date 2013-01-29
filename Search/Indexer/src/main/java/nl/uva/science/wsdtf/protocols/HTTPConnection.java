/*
 * HTTPConnection.java
 *
 * Created on May 23, 2008, 12:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nl.uva.science.wsdtf.protocols;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import nl.uva.science.wsdtf.utilities.Constants;
import nl.uva.science.wsdtf.utilities.Options;

/**
 * An implementation of an HTTP connection. 
 * 
 *
 * @author S. Koulouzis
 * 
 */
public class HTTPConnection  extends Connection{
    /**The server's URL*/
    private URL _server;
    /**The options*/
    private Options options;
    /**The input stream*/
    private InputStream in;
    /**The connection*/
    private HttpURLConnection httpConnection;
    
    private ByteArrayOutputStream baos;
    
    /** Creates a new instance of HTTPConnection */
    public HTTPConnection(Options options)  throws IOException {
        setLogger();
        this.options = options;
        buffer = new byte[Constants.MAX_BUFFER_SIZE];
        baos = new ByteArrayOutputStream();
        if (options.isServer()) {
            _server = new URL(options.hostname+":"+options.port+"/"+options.fileRequest);
//            logger.severe("This connection does not provide a http server implementation.");
//            throw new IOException("CONNECTION_EOS");
        }else{
            _server = new URL(options.hostname+":"+options.port+"/"+options.fileRequest);
        }
    }
    
    /**
     * This method is not implemented. This means that the HTTP 
     * connection can't stream data directly, only in the form of files 
     */
    public void write(byte b[], int off, int len)throws IOException{
        logger.severe("This method is not implemented");
        throw new IOException("CONNECTION_EOS");
    }
    
    /**
     * 
     * Reads data from the input stream, if there are no data left to read throws an IO Exception 
     * @return The data read.
     * @throws java.io.IOException
     */
    public byte[] read() throws IOException {
        int len=0;
        baos.reset();
        while ((len = in.read(buffer)) != -1) {
            if (len == -1 || len == 0) {
                // Use Exceptions as events
                throw new IOException("CONNECTION_EOS");
            }
            baos.write(buffer,0,len);
            return baos.toByteArray();
        }
        if (len == -1 || len == 0) {
            // Use Exceptions as events
            throw new IOException("CONNECTION_EOS");
        }
        return null;
    }
    
    /**
     * Secures HTTP connection. Not implemented
     * @param options the options 
     * @return the secured connection 
     * @throws java.io.IOException
     */
    public Connection SecureConnection(Options options)throws IOException {
        if(options.authentication){
            logger.severe("This connection does not provide a secure connection.. yet.");
            throw new IOException("CONNECTION_EOS");
        }else{
            httpConnection = (HttpURLConnection)_server.openConnection();
//            httpConnection.setChunkedStreamingMode(Constants.MAX_BUFFER_SIZE); 
            httpConnection.connect();
            logger.finest("connected to "+_server);
            in = httpConnection.getInputStream();
        }
        return this;
    }
    
    /**
     * Checks if connection is up.  There is no check at the moment if an http server is up
     * @return always true.
     */
    public boolean isConnected(){
        //an http server is always up.
        return true;
    }
    
    /**
     * closes this connection 
     */
    public void closeConnection(){
        httpConnection.disconnect();
    }
}
