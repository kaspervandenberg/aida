/**
 *
 */
package nl.uva.science.wsdtf.protocols;

import java.io.*;


import java.io.IOException;
import java.io.BufferedInputStream;
import java.util.logging.Level;

import nl.uva.science.wsdtf.utilities.Options;
import nl.uva.science.wsdtf.utilities.Constants;
import nl.uva.science.wsdtf.utilities.Security;

import org.globus.common.CoGProperties;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;

import org.ietf.jgss.GSSCredential;

import org.globus.io.streams.GridFTPInputStream;


/**
 * @author E. Angelou
 *
 */
public class GridFTPConnection extends Connection {
    
    private long chunk = 0;
    
    private BufferedInputStream inStream = null;
    
    // private ByteArrayInputStream inStream = null;
    
    int r = 2048;
    
    byte[] buf;
    
    BufferedReader GridBufferedReader;
    
    GridFTPInputStream ftp;
    
    /**
     *
     * @param options
     */
    public GridFTPConnection(Options options) throws IOException {
        
        // init Frame
        super();
        setLogger();
        buf = new byte[Constants.MAX_BUFFER_SIZE]; // buffer used to store
        buffer = new byte[Constants.MAX_BUFFER_SIZE];
        // Check if the options is set to start a server (option not supported)
        if (options.isServer()) {
//			System.out.println("This connection does not provide a server implementation. Please use the default GridFTP server.");
            logger.warning("This connection does not provide a server implementation. Please use the default GridFTP server.");
            throw new IOException("CONNECTION_EOS");
        } else {
            // Load the credential and create a GridFTP stream from which to
            // read
            try {
                ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager  .getInstance();
                
                                /*
                                 * Create a GSS Credential grid-proxy-init is required 1. Load
                                 * COG propertirs 2. Read proxy file 3. Obtain a GSSManager
                                 * instance 4. Create a GSSCredential w/ default values
                                 */
                CoGProperties cog = CoGProperties.getDefault();
                byte proxyBytes[] = Security.readBinFile(cog.getProxyFile());
                
                logger.finest("Client: Loaded proxy file: "+ cog.getProxyFile());
                
                GSSCredential credential = manager.createCredential(proxyBytes,
                        ExtendedGSSCredential.IMPEXP_OPAQUE,
                        GSSCredential.DEFAULT_LIFETIME, null,
                        GSSCredential.INITIATE_AND_ACCEPT);
                
//				System.out.println("Client Credential: " + credential.getName()
//						+ " Remaining life time:"
//						+ credential.getRemainingLifetime());
                
                logger.finest("Client Credential: " + credential.getName()
                + " Remaining life time:"
                        + credential.getRemainingLifetime());
                
                // Create a GridFTP input stream from which to receive the file
                ftp = new GridFTPInputStream(credential, options.getHostName(),options.getPort(), options.fileRequest);
                
                inStream = new BufferedInputStream((InputStream) ftp,Constants.MAX_BUFFER_SIZE);
                
//				System.out.println("GridFTP @ " + options.getPort());
                logger.info("GridFTP @ " + options.getPort());
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Uncaught exception", ex);
//				ex.printStackTrace();
                ftp.close();
                throw new IOException("CONNECTION_EOS");
                
            }
        }
        
    }
    
    public byte[] read() throws IOException {
        try {
            // reset the buffer
            if (buffer.length != r){
                buffer = new byte[r];
            }
            
            r = ftp.read(buffer);
            
            //System.out.println("GOT::" + r);
            // One more chunk read...
            chunk++;
            
        } catch (Exception e) {
            // closeConnection();
            logger.fine("Nothing else to read\nCaught Exception:" + e.toString());
//			System.out.println("Nothing else to read\nCaught Exception:" + e);
            throw new IOException("CONNECTION_EOS");
            
        }
        
        return buffer;
    }
    
    public void write(byte data[], int off, int len) throws IOException {
        logger.severe("This method is not implemented");
        throw new IOException("CONNECTION_EOS");
        // not implemented
    }
    
    public Connection SecureConnection(Options options) {
        if(!options.authentication){
            logger.severe("GridFTP must use authenication, check your configurations");
            return null;
        }
        return this;
    }
    
    public void closeConnection() {
        try {
            
            // close the stream
            inStream.close();
            if (ftp.available()>0){
                ftp.close();
            }
            
            // System.exit(0);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Uncaught exception", ex);
//			ex.printStackTrace();
            
        }
    }
    
    public boolean isConnected() {
        // Bogus isConnected()...
        try {
            if (inStream.available() >= 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Uncaught exception", e);
//			e.printStackTrace();
            // throw new IOException("CONNECTION_EOS");
        }
        return false;
    }
}