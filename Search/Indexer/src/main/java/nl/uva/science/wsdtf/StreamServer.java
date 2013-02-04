package nl.uva.science.wsdtf;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.io.*;

//import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
//import java.util.concurrent.BlockingQueue;

import nl.uva.science.wsdtf.protocols.Connection;
//import nl.uva.science.wsdtf.protocols.JStyxConnection;
import nl.uva.science.wsdtf.protocols.RTSPConnection;
import nl.uva.science.wsdtf.protocols.TCPConnection;
import nl.uva.science.wsdtf.protocols.UDPConnection;
import nl.uva.science.wsdtf.utilities.Constants;
import nl.uva.science.wsdtf.utilities.Options;

import org.globus.gsi.gssapi.auth.Authorization;
import org.ietf.jgss.GSSCredential;

/**
 * <description> This server provides to subclasses a constructor 
 * for communication between the  * server and the application, so 
 * that the server may take from the producer the data generated
</description>
 *
 * @author S. Koulouzis, E. Angelou
 * @version alpha, 22/08/07
 */
public abstract class StreamServer implements Runnable {

    /** The connection.Array of the connections the server holds */
    protected Connection[] connection;
    /** The options that initilize the server and its connections. */
    protected Options options;
    /** The credentials. */
    protected GSSCredential credentials = null;
    /** The authorization. */
    protected Authorization authorization = null;
    /** The buffer. */
    protected byte[] buffer;
    /** The die. */
    protected boolean die = false;
    /** The port. */
    protected int[] port = new int[Constants.CONNECTION_COUNT];
    /** The initial port. */
    private int initialPort;
        /** The stream to read data from the connector*/
    protected InputStream in;
    protected static Logger logger = null;

    /**
     * Instantiates a new stream server.
     *
     * @param opt
     *            the opt
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public StreamServer(Options opt) throws IOException {
        initLogger();
        logger.info("Starting");
        options = opt;
        if (opt.protocol == Constants.UDP || opt.protocol == Constants.RTSP) {
            if (Constants.MAX_BUFFER_SIZE > Constants.Kbyte * 63) {
                Constants.setMaxBufferSize(Constants.Kbyte * 63);
                logger.config("The UDP or RTSP protocol cannot set a buffer size grater than 63 Kbytes. The buffer size is set to: 63 kbytes");
            }
        }
        if (Constants.MAX_BUFFER_SIZE >= Constants.Kbyte * 63) {
            logger.fine("Some protocols cannot set the buffer size grater than 63 kbytes");
        }
        buffer = new byte[Constants.MAX_BUFFER_SIZE];
        credentials = options.getCredential();
        setAuthorization(options.getAuthorization());
        connection = new Connection[Constants.CONNECTION_COUNT];
        initialPort = options.port;
        this.in = this.options.in;
    }

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
     * Sets the authorization.
     *
     * @param auth
     *            the new authorization
     */
    public void setAuthorization(Authorization auth) {
        authorization = auth;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public abstract void run();

    /**
     * Stop the server, close the connections etc.
     */
    public abstract void stop();

    public abstract void kill();

    /**
     * Gets the connection.
     *
     * @return the connections
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected Connection[] getConnection() throws IOException {
        // System.out.println("Server geting new Connection ");
        initPort();
        switch (options.getProtocol()) {
            case Constants.TCP:
                for (int i = 0; i < Constants.CONNECTION_COUNT; i++) {
                    options.port = port[i];
                    connection[i] = new TCPConnection(options);
                    connection[i].setName("TCP@" + options.port);
                    logger.finest("Getting new TCP connection @ port" + port[i]);
                }
                return connection;
            case Constants.UDP:
                for (int i = 0; i < Constants.CONNECTION_COUNT; i++) {
                    options.port = port[i];
                    connection[i] = new UDPConnection(options);
                    connection[i].setName("UDP@" + options.port);
                    logger.finest("Getting new UDP connection @ port" + port[i]);
                }
                return connection;
            case Constants.RTSP:
                for (int i = 0; i < Constants.CONNECTION_COUNT; i++) {
                    options.port = port[i];
                    connection[i] = new RTSPConnection(options);
                    connection[i].setName("RTSP@" + options.port);
                    logger.finest("Getting new RTSP connection @ port" + port[i]);
                }
                return connection;
            case Constants.STYX:
//			if (Constants.CONNECTION_COUNT > 1) {
//				logger
//						.warning("JStyx does not support multithreading one connection will be opened ");
//			}
////			connection[0] = new JStyxConnection(options);
//			connection[0].setName("STYX@" + options.port);
//			logger.finest("Getting new STYX connection @ port" + options.port);
                logger.severe("There is no STYX server implementation");
                return null;
            case Constants.GridFTP:
                logger.severe("There is no GridFTP server implementation");
                return null;

            case Constants.HTTP:
                logger.severe("There is no HTTP server implementation");
                return null;
            default:
                for (int i = 0; i < Constants.CONNECTION_COUNT; i++) {
                    connection[i] = new TCPConnection(options);
                    options.port = port[i];
                    connection[i].setName("TCP@" + options.port);
                    logger.finest("Getting new TCP connection @ port" + port[i]);
                }
                return connection;
        }
    }

    /**
     * Gets the host name, so it may be returned to the WS.
     *
     * @return the host name
     */
    public String getHostName() {
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Uncaught exception", e);
        // e.printStackTrace();
        }
        return hostName;
    }

    /**
     * Initializes this classes longer.
     */
    private static void initLogger() {
        try {
            boolean ok = false;
            if (!(new File("logs/")).exists()) {
                ok = (new File("logs/")).mkdir();
            } else {
                ok = true;
            }
            logger = Logger.getLogger(StreamServer.class.getName());
            logger.setUseParentHandlers(false);
            logger.setUseParentHandlers(true);
            logger.setLevel(Constants.LOG_LEVEL);

            if (ok && Constants.saveLogs) {
                FileHandler handler = new FileHandler("logs/StreamServer.log");
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
