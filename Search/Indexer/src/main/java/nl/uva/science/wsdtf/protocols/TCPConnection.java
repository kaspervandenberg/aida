package nl.uva.science.wsdtf.protocols;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

import org.globus.net.ServerSocketFactory;
import org.globus.net.SocketFactory;

import org.globus.gsi.gssapi.net.GssSocket;
import org.globus.gsi.gssapi.net.GssSocketFactory;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSContext;

import org.gridforum.jgss.ExtendedGSSManager;
import org.gridforum.jgss.ExtendedGSSCredential;

import nl.uva.science.wsdtf.utilities.Options;
import nl.uva.science.wsdtf.utilities.Constants;
import nl.uva.science.wsdtf.utilities.Security;

import org.globus.gsi.gssapi.auth.NoAuthorization;

/**
 * An implementation of a TCP connection. 
 * 
 *
 * @author S. Koulouzis
 * 
 */
public class TCPConnection extends Connection {
    
    private ServerSocket _server;
    
    private Socket _client;
    
    private Socket socket;
    
    private BufferedInputStream inStream;
    
    private BufferedOutputStream outStream;
    
//        private ByteArrayOutputStream baos;
    
    // private ExtendedGSSContext context;
    
    private GSSContext context;
    
    // private MessageProp prop;
    
    private byte[] head = new byte[1];
    
    private ByteArrayOutputStream baos;
    
    /**
     *Creates a new instance of a TCP connection.
     * @param options
     */
    public TCPConnection(Options options) throws IOException {
        setLogger();
        buffer = new byte[Constants.MAX_BUFFER_SIZE];
        this.options = options;
        if (this.options.isServer()) {
            _server = ServerSocketFactory.getDefault().createServerSocket(
                    options.getPort());
            options.port = _server.getLocalPort();
            
        } else {
            this._client = SocketFactory.getDefault().createSocket(this.options.getHostName(), options.getPort());
        }
        baos = new ByteArrayOutputStream();
    }
    
    /**
     * Reads data from an input stream. If the connection is closed by the server, it throws an <code>IOException</code>
     * @return the bytes read.
     * @throws java.io.IOException
     */
    public byte[] read() throws IOException {
        int len=0;
        baos.reset();
        while ((len = inStream.read(buffer)) != -1) {
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
     * Writes a byte array in the output stream. If the connection is down throws an IOException
     * @param b the byte array to write 
     * @param off, the offset
     * @param len, the length 
     * @throws java.io.IOException
     */   
    public void write(byte b[], int off, int len) throws IOException {
        outStream.write(b, off, len);
        outStream.flush();
        byteStreamed = byteStreamed + len;
    }
    
    /**
     * if options have being set to authenticate it authenticates and/or encrypts the channel, by using a proxy certificate
     *
     *  1. Load proxy path from COG properties
     *  2. Read the proxy file
     *  3. Create a GSS  proxy credential
     *  4. Create a GSSContext
     *  5. Request Mutual Authentication
     *  6. Set send/receive buffer sizes
     * else just make the socket connection and set send/receive buffer sizes
     *@params options
     */
    public TCPConnection SecureConnection(Options options) throws IOException {
        if(options.authentication){
            ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
                    .getInstance();
            try {
                if (options.isServer()) {
                    
                                /*
                                 * Load GSS Credential: 1. Load proxy path from COG properties
                                 * 2. Read the proxy file 3. Create a GSS credential w/ proxy
                                 * data
                                 */
                    byte proxyBytes[] = Security.readBinFile(null);
                    
                    GSSCredential credential = manager.createCredential(proxyBytes,
                            ExtendedGSSCredential.IMPEXP_OPAQUE,
                            GSSCredential.DEFAULT_LIFETIME, null,
                            GSSCredential.INITIATE_AND_ACCEPT);
                    
                    // System.out.println("Server Credential: " +
                    // credential.getName());
                    //
                    // System.out.println("TCP @ "+_server.getLocalPort()+" is
                    // waiting for connections");
                    logger.info("TCP @ " + _server.getLocalPort()
                    + " is waiting for connections");
                    socket = _server.accept();
                    socket.setSendBufferSize(Constants.MAX_BUFFER_SIZE);
                    logger.info("TCP @ " + socket.getLocalPort()
                    + " is connected with "
                            + socket.getRemoteSocketAddress());                    
                                 /*
                                  * Create a GSSContext to receive the incoming request
                                  * from the client. Use null for the server credentials
                                  * passed in. This tells the underlying mechanism
                                  * to use whatever credentials it has available that
                                  * can be used to accept this connection.
                                  */
                    context = manager.createContext(credential);
                } else {
                    socket = this._client;
                    socket.setReceiveBufferSize(Constants.MAX_BUFFER_SIZE);
                    logger.info("TCP @ " + socket.getLocalPort()
                    + " is connected with "
                            + socket.getRemoteSocketAddress());                    
                                /*
                                 * Create a GSS Credential grid-proxy-init is required 1. Load
                                 * COG propertirs 2. Read proxy file 3. Obtain a GSSManager
                                 * instance 4. Create a GSSCredential w/ default values
                                 */
                    byte proxyBytes[] = Security.readBinFile(null);
                    
                    GSSCredential credential = manager.createCredential(proxyBytes,
                            ExtendedGSSCredential.IMPEXP_OPAQUE,
                            GSSCredential.DEFAULT_LIFETIME, null,
                            GSSCredential.INITIATE_AND_ACCEPT);
                    
                    // System.out.println("Client Credential: " +
                    // credential.getName()
                    // + " Remaining life time:" +
                    // credential.getRemainingLifetime());
                    logger.finer("Client Credential: " + credential.getName()
                    + " Remaining life time:"
                            + credential.getRemainingLifetime());
                    
                    context = manager.createContext(null, null, credential,
                            GSSContext.DEFAULT_LIFETIME);
                    
                    context.requestCredDeleg(false);
                    context.requestMutualAuth(true);
                }
                
                GssSocketFactory factory = GssSocketFactory.getDefault();
                
                GssSocket gsiSocket = (GssSocket) factory.createSocket(socket,
                        null, 0, context);
                
                gsiSocket.setWrapMode(gsiSocket.GSI_MODE);
                // Server or client socket
                gsiSocket.setUseClientMode(!options.isServer());
                gsiSocket.setAuthorization(NoAuthorization.getInstance());// options.getAuthorization());
                
                // if the data are not encrypted just authenticate the other side
                if (!options.encryptData) {
                    gsiSocket.startHandshake();
                } else {
                    socket = gsiSocket;
                }
                if (options.isServer()) {
                    outStream = new BufferedOutputStream(socket.getOutputStream(),
                            Constants.MAX_BUFFER_SIZE);
                } else {
                    inStream = new BufferedInputStream(socket.getInputStream(),
                            Constants.MAX_BUFFER_SIZE);
                }
                
                        /*
                         * If mutual authentication did not take place, then only the client
                         * was authenticated to the server. Otherwise, both client and
                         * server were authenticated to each other.
                         */
                if (context.getMutualAuthState()) {
                    // System.out.println("TCP @ "+socket.getLocalPort()+": Mutual
                    // authentication took place!");
                    // System.out.println("Client is " + context.getSrcName());
                    // System.out.println("Server is " + context.getTargName());
                    logger.finest("TCP @ " + socket.getLocalPort()
                    + ": Mutual authentication took place!");
                    logger.finest("Client is " + context.getSrcName());
                    logger.fine("Server is " + context.getTargName());
                    connected = true;
                    secured = true;
                } else {
                    secured = false;
                }
            } catch (GSSException ex) {
                secured = false;
                logger.log(Level.SEVERE, "Authentication Failed!!!", ex);
                System.exit(2);
                // logger.severe(ex.toString());
                // ex.printStackTrace();
            }
        }else{
            if (options.isServer()) {
                // System.out.println("Server Credential: " +
                // credential.getName());
                //
                // System.out.println("TCP @ "+_server.getLocalPort()+" is
                // waiting for connections");
                logger.info("TCP @ " + _server.getLocalPort()
                + " is waiting for connections");
                socket = _server.accept();
                socket.setSendBufferSize(Constants.MAX_BUFFER_SIZE);
                logger.info("TCP @ " + socket.getLocalPort()
                + " is connected with "
                        + socket.getRemoteSocketAddress());
                // System.out.println("TCP @ "+socket.getLocalPort()+" is
                // connected with"+socket.getRemoteSocketAddress());
            } else {
                socket = this._client;
                socket.setReceiveBufferSize(Constants.MAX_BUFFER_SIZE);
                logger.info("TCP @ " + socket.getLocalPort()
                + " is connected with "
                        + socket.getRemoteSocketAddress());
            }
            if (options.isServer()) {
                Constants.setMaxBufferSize(socket.getSendBufferSize());
                outStream = new BufferedOutputStream(socket.getOutputStream(),Constants.MAX_BUFFER_SIZE);
            } else {
                Constants.setMaxBufferSize(socket.getReceiveBufferSize());
                inStream = new BufferedInputStream(socket.getInputStream(),Constants.MAX_BUFFER_SIZE);
            }
            connected = true;
        }
        logger.config("Socket Receive Buffer Size is set to: "+socket.getReceiveBufferSize()+" bytes");
        logger.config("Socket Send Buffer Size is set to: "+socket.getSendBufferSize()+" bytes");
        socket.setTcpNoDelay(false);
//            collectGarbage();
        return this;
    }
    
    /**
     * Closes this connection.
     */
    public void closeConnection() {
        try {
            if (_client != null) {
                if (inStream != null) {
                    inStream.close();
                }
                _client.close();
            }
            if (_server != null) {
                if (outStream != null) {
                    // outStream.flush();
                    outStream.close();
                }
                _server.close();
                // System.out.println("TCP @"+socket.getLocalPort()+":
                // _server.isClosed()="+_server.isClosed());
            }
            if (socket != null) {
                socket.close();
            }
            // socket = null;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Uncaught exception", ex);
            // ex.printStackTrace();
        } finally {
            try {
                if (_server != null) {
                    if (outStream != null) {
                        outStream.close();
                    }
                    _server.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Uncaught exception", ex);
                ex.printStackTrace();
            }
        }
//                collectGarbage();
        connected = false;
        secured = false;
    }
    /**
     * Checks if connection is up
     * @return true if connection is up
     */
    public boolean isConnected() {
        boolean val = false;
                /*
                 * System.out.println("TCPConnection IN ISCONNECTED:--------------");
                 * System.out.println("TCPCnnection: address= " + address);
                 * System.out.println("socket:"+socket);
                 * System.out.println("connected:"+connected);
                 * System.out.println("secured:"+secured);
                 * System.out.println("socket.isConnected():"+socket.isConnected());
                 * System.out.println("socket.isBound():"+socket.isBound());
                 * System.out.println("socket.isClosed():"+socket.isClosed());
                 * System.out.println("socket.isInputShutdown():"+socket.isInputShutdown());
                 * System.out.println("socket.isOutputShutdown():"+socket.isOutputShutdown());
                 * System.out.println("socket.isBound():"+socket.isBound());
                 * System.out.println("socket.getInetAddress():"+socket.getInetAddress());
                 * System.out.println("socket.getLocalSocketAddress():"+socket.getLocalSocketAddress());
                 * System.out.println("socket.isBound():"+socket.isBound());
                 * System.out.println("TCPConnection IN ISCONNECTED:--------------");
                 */
        if(this.options.authentication){
            if (socket != null && !socket.isClosed() && connected && secured) {
                val = true;
            }
        }else{
            if (socket != null && !socket.isClosed() && connected){
                val = true;
            }
        }
        return val;
    }
    
        /*
         * private byte[] ecryptData(byte data[]){ byte[] wdata=null; try{
         * MessageProp msgProp = new MessageProp(1, false); GssSocket gsiSocket =
         * (GssSocket)socket; wdata = gsiSocket.getContext().wrap(data, 0,
         * data.length, msgProp); }catch(GSSException ex){ ex.printStackTrace(); }
         * return wdata; }
         *
         * private byte[] decryptData(byte data[]){ byte[] wdata=null; try{
         * MessageProp msgProp = new MessageProp(1, false); GssSocket gsiSocket =
         * (GssSocket)socket; wdata = gsiSocket.getContext().unwrap(data, 0,
         * data.length, msgProp); }catch(GSSException ex){ ex.printStackTrace(); }
         * return wdata; }
         */
}
