package nl.uva.science.wsdtf;


//import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
//import java.util.ArrayList;

import nl.uva.science.wsdtf.utilities.Constants;
import nl.uva.science.wsdtf.utilities.Options;

/**
 * <description> StreamServer implementation. The options specify what kind of a
 * connection the server will open to communicate with the client.
 * </description>
 *
 * @author S. Koulouzis, E. Angelou
 * @version alpha, 22/08/07
 */
public class StreamServerImpl extends StreamServer  {
    
    /**
     * Instantiates a new stream server impl.
     *
     * @param opt
     *            the options
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public StreamServerImpl(Options opt) throws IOException {
        super(opt);
    }
    
    /**
     * Starts sending data to the client
     * @Override
     */
    public void run() {
        while (!die) {
            try {
                connection = getConnection();
                if (Constants.CONNECTION_COUNT >= 2) {
//                    if(!die){
//                        for (int i = 0; i < Constants.CONNECTION_COUNT; i++) {
//                            connection[i].setPool(pool);
//                            connection[i] = connection[i].SecureConnection(options);
//                            connection[i].start();
//                        }
//                    }
//                    for (int j = 0; j < Constants.CONNECTION_COUNT; j++) {
//                        try {
//                            logger.info("waiting for connections to end");
//                            connection[j].join();
//                        } catch (InterruptedException e) {
//                            logger.log(Level.SEVERE, "Uncaught exception", e);
//                        }
//                    }
//                    logger.info("connections are done");
                } else {
                    if(!die){
                        logger.info("waiting for connections");
                        connection[0] = connection[0].SecureConnection(options);
                        logger.info("start streaming");
                        stream();
                        logger.info("connections are done");
                    }
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Uncaught exception, killing server", ex);
                kill();
            }
            die = !options.keepStreamAlive;
        }
        logger.info("Server is dead");
    }
    
    /**
     * Stream, by using a single connection object
     */
    private void stream() {
        int bytesSent = 0;
        int len = 0;
        long start = System.currentTimeMillis();
        try {
            while (connection[0] != null && connection[0].isConnected() && ((len = in.read(buffer)) != -1)) {
                connection[0].write(buffer, 0, len);
                bytesSent = bytesSent + buffer.length;
                if(bytesSent%(1024*100)==0){
                    System.runFinalization();
                    System.gc();
                    if(Constants.displaySpeed){
                        System.out.println("So far transmitted: "+bytesSent+" bytes. Speed: "+
                                (bytesSent/1024.0) / ((System.currentTimeMillis() - start)/1000.0) +" k/sec ");
                    }else{
                        logger.finest("So far transmitted: "+bytesSent+" bytes. Speed: "+
                                (bytesSent/1024.0) / ((System.currentTimeMillis() - start)/1000.0) +" k/sec ");
                    }
                }
            }
        } catch (Exception ex) {
            long end = System.currentTimeMillis();
            if(Constants.displaySpeed){
                System.out.println("stoping by Exception, so far sent: "+
                        bytesSent+" bytes in "+(end-start)+ " msec "+ex);
            }else{
                logger.finer("stoping by Exception, so far sent: "+
                        bytesSent+" bytes in "+(end-start)+ " msec "+ex);
            }
        }finally{
            long end = System.currentTimeMillis();
            if(Constants.displaySpeed){
                System.out.println("stoping by break, so far sent: "+bytesSent+" bytes in "
                        +(end-start)+ " msec. Total speed: "+ (bytesSent/1024.0)/((end-start)/1000.0)+" kb/s" );
            }else{
                logger.finer("stoping by break, so far sent: "+bytesSent+" bytes in "
                        +(end-start)+ " msec. Total speed: "+ (bytesSent/1024.0)/((end-start)/1000.0)+" kb/s" );
            }
            stop();
        }
        
    }
    
    /**
     * Stop the server, close all open connectios etc.
     *
     * @Override
     */
    public void stop() {
        for (int i = 0; i < Constants.CONNECTION_COUNT; i++) {
            if (connection[i] != null) {
                connection[i].closeConnection();
            }
        }
        try {
            in.close();
            System.runFinalization();
            System.gc();
        } catch (Throwable e) {
            logger.severe(e.toString());
        }
    }
    
    /**
     * Kill this server.
     */
    public void kill() {
        logger.finest("killing this server");
        die = true;
        stop();
        try {
            if (credentials != null) {
                credentials.dispose();
            }
        }catch (Throwable ex) {
            logger.log(Level.WARNING, "Uncaught exception", ex);
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
