package nl.uva.science.wsdtf;

import java.io.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;
//import java.util.concurrent.BlockingQueue;

import nl.uva.science.wsdtf.utilities.Options;
import nl.uva.science.wsdtf.utilities.Constants;
import nl.uva.science.wsdtf.protocols.Connection;
import nl.uva.science.wsdtf.protocols.GridFTPConnection;
import nl.uva.science.wsdtf.protocols.HTTPConnection;
//import nl.uva.science.wsdtf.protocols.JStyxConnection;
import nl.uva.science.wsdtf.protocols.RTSPConnection;
import nl.uva.science.wsdtf.protocols.TCPConnection;
import nl.uva.science.wsdtf.protocols.UDPConnection;

import org.globus.gsi.gssapi.auth.Authorization;
import org.globus.gsi.gssapi.auth.SelfAuthorization;

import org.ietf.jgss.GSSCredential;

/**
 * <description> This client provides to subclasses a constructor for
 * authorizing the local user, and giving means of communication between the
 * client and an application, so that the client may give to the consumer the
 * data received. </description>
 *
 * @author S. Koulouzis, E. Angelou
 * @version alpha, 22/08/07
 */
public abstract class StreamClient implements Runnable {
    
    /** The connections. Array of the connections the client holds */
    protected Connection[] connection;
    
    /** The options, that initilize the client and its connections */
    protected Options options;
    
    /** The credentials. */
    protected GSSCredential credentials = null;
    
    /** The authorization. */
    protected Authorization authorization = null;
    
    /** The buffer. */
    protected byte[] buffer;
    
    /** The logger. */
    protected static Logger logger;
    
    /** The die. */
    protected boolean die = false;
        
    /** The port. */
    protected int[] port = new int[Constants.CONNECTION_COUNT];
    
    /** The initial port. */
    private int initialPort;
    
    /** The data received. */
    protected int datareceived;
    /** The stream to write data to the connector*/
    protected BufferedOutputStream out;
    
    /** The done. */
    boolean done = false;
    
    /**
     * Using the <code>Options</code> class initialize the connection, credentials and the
     * stram that provides communication with the application.
     *
     * @param opt
     *            the options
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public StreamClient(Options opt) throws IOException {
        initLogger();
        if(opt.protocol==Constants.UDP || opt.protocol==Constants.RTSP){
            if(Constants.MAX_BUFFER_SIZE > Constants.Kbyte*63){
                Constants.setMaxBufferSize(Constants.Kbyte*63);
                logger.config("The UDP or RTSP protocol cannot set a buffer size grater than 63 Kbytes. The buffer size is set to: 63 kbytes" );
            }
        }
        if(Constants.MAX_BUFFER_SIZE >Constants.Kbyte*63){
            logger.fine("Some protocols cannot set the buffer size grater than 63 kbytes");
        }
        buffer = new byte[Constants.MAX_BUFFER_SIZE];
        options = opt;
        this.credentials = options.getCredential();
        setAuthorization(SelfAuthorization.getInstance());
        
        connection = new Connection[Constants.CONNECTION_COUNT];
        initialPort = options.port;
        this.die = opt.keepStreamAlive;
        out = new BufferedOutputStream(this.options.out,Constants.MAX_BUFFER_SIZE);
    }
    
    /**
     * Mapping the authenticated user to a local user on the system. Using the
     * proxy the distinguished name of the authenticated user is mpped with the
     * local user name throug the grid map file
     *
     * @param auth
     *            the auth
     */
    private void setAuthorization(Authorization auth) {
        authorization = auth;
    }
    
    /**
     * Starts the thread.
     */
    public abstract void run();
    
    /**
     * Stop the client, close the connections etc.
     */
    public abstract void stop();
    
    /**
     * Inits the port.
     */
    private void initPort() {
        logger.finer("Initializing ports");
        options.port = initialPort;
        int startPort = options.port;
        port[0] = startPort++;
        for (int i = 1; i < Constants.CONNECTION_COUNT; i++) {
            port[i] = startPort++;
        }
    }
    
    /**
     * Gets the connection, depending on the protocol specifed in the
     * <code>Options</code>
     *
     * @return the connections
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected Connection[] getConnection() throws IOException {
        initPort();
        switch (options.getProtocol()) {
            case Constants.TCP:
                for (int i = 0; i < Constants.CONNECTION_COUNT; i++) {
                    options.port = port[i];
                    connection[i] = new TCPConnection(options);
                    connection[i].setName("TCP@" + options.port);
                    logger.finest("Getting new TCP connection @ port"+port[i]);
                }
                return connection;
            case Constants.UDP:
                for (int i = 0; i < Constants.CONNECTION_COUNT; i++) {
                    options.port = port[i];
                    connection[i] = new UDPConnection(options);
                    connection[i].setName("UDP@" + options.port);
                    logger.finest("Getting new UDP connection @ port"+port[i]);
                }
                return connection;
            case Constants.RTSP:
                for (int i = 0; i < Constants.CONNECTION_COUNT; i++) {
                    options.port = port[i];
                    connection[i] = new RTSPConnection(options);
                    connection[i].setName("RTSP@" + options.port);
                    logger.finest("Getting new RTSP connection @ port"+port[i]);
                }
                return connection;
            case Constants.STYX:
//                for (int i = 0; i < Constants.CONNECTION_COUNT; i++) {
//                    options.port = port[i];
//				connection[i] = new JStyxConnection(options);
//                    connection[i].setName("STYX@" + options.port);
//                    logger.finest("Getting new STYX connection @ port"+port[i]);
//                }
                logger.severe("There is no STYX client implementation");
                return null;
            case Constants.GridFTP:
                connection[0] = new GridFTPConnection(options);
                logger.finest("Getting new GFtp connection @ port"+options.port);
                return connection;
                
            case Constants.HTTP:
                connection[0] = new HTTPConnection(options);
                logger.finest("Getting new http connection @ port"+options.port);
                return connection;
            default:
                for (int i = 0; i < Constants.CONNECTION_COUNT; i++) {
                    connection[i] = new TCPConnection(options);
                    options.port = port[i];
                    connection[i].setName("TCP@" + options.port);
                    logger.finest("Getting new TCP connection @ port"+port[i]);
                }
                return connection;
        }
    }
    
    /** Initializes this classes longer */
    private static void initLogger(){
        try {
            boolean ok = false;
            if (!(new File("logs/"))
            .exists()) {
                ok = (new File("logs/"))
                .mkdir();
            } else {
                ok = true;
            }
            logger = Logger.getLogger(StreamClient.class.getName());
            logger.setUseParentHandlers(false);
            logger.setUseParentHandlers(true);
            logger.setLevel(Constants.LOG_LEVEL);
            
            if (ok && Constants.saveLogs) {
                FileHandler handler = new FileHandler("logs/StreamClient.log");
                handler.setFormatter(new SimpleFormatter());
                handler.setLevel(Constants.LOG_LEVEL);
                logger.addHandler(handler);
            }
            
            ConsoleHandler console = new ConsoleHandler();
            console.setLevel(Constants.LOG_LEVEL);
            logger.addHandler(console);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        
}
