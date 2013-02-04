package nl.uva.science.wsdtf;

//import java.io.FileOutputStream;
import java.io.IOException;

import java.util.logging.Level;

import nl.uva.science.wsdtf.utilities.Constants;
import nl.uva.science.wsdtf.utilities.Options;

/**
 * <description> Client implementation. The <code>Options</code> specify what kind of a
 * connection the client will open to communicate with the server. The
 * connection must be secured (authenticate server, and secure communication),  if options require that
 * </description>
 *
 * @author S. Koulouzis, E. Angelou
 * @version alpha, 22/08/07
 */
public class StreamClientImpl extends StreamClient {
    
    public boolean gotEOS = false;
    
    /**
     * Starts  connection, the details of securing the connection
     * depend on the underlying protocol, and the options specified 
     *
     * @param opt
     *            the opt
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public StreamClientImpl(Options opt) throws IOException {
        super(opt);
    }
    
    /**
     * Starts the client. It receives data from the server and passes them to
     * an <code>out</code>
     *
     * @Override
     */
    public void run() {
        while (!die) {
            try {
                connection = getConnection();
                if (Constants.CONNECTION_COUNT >= 2) {
//                            for (int i = 0; i < Constants.CONNECTION_COUNT; i++) {
//                                connection[i] = connection[i].SecureConnection(options);
//                                connection[i].setPool(pool);
//                                connection[i].start();
//                            }
                } else {
                    connection[0] = connection[0].SecureConnection(options);
                    logger.info("Start receiving");
                    recive();
                    logger.info("Done receiving");
                }
                
            } catch (IOException ex) {
                if (ex.getMessage().equals("Connection refused")) {
                    logger.log(Level.SEVERE, "Killing Client. Server seems not to be listening @"+options.hostname+","+options.port, ex);
                    kill();
                    stop();
                    break;
                }
                if (ex.getMessage().equals("NO_MORE_DATA")) {
                    logger.finest("Connection has reached the end of the stream");
                    stop();
                    break;
                } else {
                    logger.log(Level.SEVERE, "Uncaught exception", ex);
                }
            }
            stop();
        }
    }
    
    /**
     * Recive data , from a single connection object
     */
    private void recive() throws IOException {
        logger.info("reciveing bytes");
        long start = System.currentTimeMillis();
        while (connection[0].isConnected()) {
            try {
                buffer = connection[0].read();
                out.write(buffer);
                out.flush();
                datareceived = datareceived + buffer.length;
                
                //try to clean up a bit
                if(datareceived%(1024*50)==0){
                    try {
                        System.runFinalization();
                        System.gc();
                    } catch (Throwable e) {
                        logger.severe(e.toString());
                    }
                    if(Constants.displaySpeed){
                        System.out.println("So far recived: "+
                                datareceived+" bytes. Speed: "+
                                (datareceived/1024.0) / ((System.currentTimeMillis() - start)/1000.0) +" k/sec ");
                    }else{
                        logger.finest("So far recived: "+datareceived+" bytes. Speed: "+
                                (datareceived/1024.0) / ((System.currentTimeMillis() - start)/1000.0) +" k/sec ");
                    }
                }
                
                //in any IOException stop the client
            } catch (Exception ex) {
                if (ex.getMessage().equals("CONNECTION_EOS")) {
                    long end = System.currentTimeMillis();
                    if(Constants.displaySpeed){
                        System.out.println("Connection has reached the end of the stream, after reciving "+
                                datareceived+" in "+(end-start)+" msec Total speed: "+
                                (datareceived/1024.0)/((end-start)/1000.0));
                    }else{
                        logger.finer("Connection has reached the end of the stream, after reciving "+
                                datareceived+" in "+(end-start)+" msec Total speed: "+
                                (datareceived/1024.0)/((end-start)/1000.0));
                    }
                    throw new IOException("NO_MORE_DATA");
                } else {
                    logger.log(Level.SEVERE, "Uncaught exception", ex);
                    break;
                }
            }
        }
        if(Constants.displaySpeed){
            System.out.println("Connection has reached the end of the stream, after reciving "+
                    datareceived+" in "+(System.currentTimeMillis()-start)+
                    " msec. Total speed: "+(datareceived/1024.0)/((System.currentTimeMillis()-start)/1000.0 )+
                    " kb/s");
        }else{
            logger.finer("Connection has reached the end of the stream, after reciving "+
                    datareceived+" in "+(System.currentTimeMillis()-start)+
                    " msec. Total speed: "+(datareceived/1024.0)/((System.currentTimeMillis()-start)/1000.0 )
                    + "kb/s");
        }
        stop();
    }
    
    /**
     * Stop the client. close all open connections and strems
     *
     * @Override
     */
    public void stop() {
        logger.finer("stoping");
        die = true;
        try {
            for (int i = 0; i < Constants.CONNECTION_COUNT; i++) {
                if (connection[i] != null) {
                    connection[i].closeConnection();
                    connection[i] = null;
                }
            }
            if(out!=null){
                out.flush();
                out.close();
                out=null;
            }
            
        } catch (Exception ex) {
            logger.log(Level.WARNING,"There was a problem while closing the connection", ex);
        }
    }
    
    /**
     * Kill.
     */
    public void kill() {
        logger.info("killing this client");
        die = true;
        for (int i = 0; i < Constants.CONNECTION_COUNT; i++) {
            if (connection[i] != null) {
                connection[i].closeConnection();
                connection[i] = null;
            }
        }
        try {
            if(out!=null){
                out.flush();
                out.close();
                out =null;
            }
            finalize();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Uncaught exception", ex);
            ex.printStackTrace();
        }
        // catch (GSSException ex) {
        // logger.log(Level.SEVERE, "Uncaught exception", ex);
        // ex.printStackTrace();
        // }
        catch (InterruptedException ex) {
            logger.log(Level.SEVERE, "Uncaught exception", ex);
            ex.printStackTrace();
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Uncaught exception", ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * Checks if is connected.
     *
     * @return true, if is connected
     */
    public boolean isConnected() {
        return connection[0].isConnected();
    }
}
