package nl.uva.science.wsdtf.protocols;

import java.io.File;
import java.io.IOException;
//import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
//import java.util.logging.SimpleFormatter;

import nl.uva.science.wsdtf.utilities.Constants;
import nl.uva.science.wsdtf.utilities.Options;

/**
 * The Class Connection, that server and client use to stream data. Underling
 * protocol implementations must at least provide a
 * <code>write(byte b[], int off, int len)</code> and a
 * <code>byte[] read()</code>. This will probably change, and instead 
 * of read and write a in/out stream would be required 
 *
 * @author S. Koulouzis, E. Angelou
 * @version alpha, 22/08/07
 */
public abstract class Connection extends Thread {
    
    /** The connected. */
    protected boolean connected = false;
    
    /** The secured. */
    protected boolean secured = false;
    
    /** The buffer. */
    protected byte[] buffer;
    
    /** The Bytebuffer. */
    protected int[] intbuffer = new int[1024];
    
    /** The pool. */
//	protected List<int[]> pool;
    
    /** The options. */
    protected Options options;
    
    /** The byte streamed. */
    protected int byteStreamed = 0;
    
    protected static Logger logger = null;
    
    /**
     * Read.
     *
     * @return the byte[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract byte[] read() throws IOException;
    
    /**
     * Write.
     *
     * @param b
     *            the byte array
     * @param off
     *            the offset
     * @param len
     *            the length
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract void write(byte b[], int off, int len) throws IOException;
    
    /**
     * Secure connection.
     *
     * @param options
     *            the options
     *
     * @return the connection
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract Connection SecureConnection(Options options)
    throws IOException;
    
    /**
     * Close connection.Depending on the protocol close streams and sockets
     */
    public abstract void closeConnection();
    
    /**
     * Checks if is connected.
     *
     * @return true, if is connected
     */
    public abstract boolean isConnected();
    
    /*
     * Sets the pool.
     *
     * @param pool
     *            the new pool
     */
//	public void setPool(List<int[]> pool) {
//		this.pool = pool;
//	}
    
    /*
     * Gets the pool.
     *
     * @return the pool
     */
//	public List getPool() {
//		return this.pool;
//	}
    
        /*
         * (non-Javadoc)
         *
         * @see java.lang.Thread#run()
         */
    public void run() {
        if (options.isServer) {
            send();
            logger.info(getName() + " Done snding");
            // System.out.println(getName() + " Done snding");
        }
        if (!options.isServer) {
            recive();
            logger.info(getName() + " Done reciving");
            // System.out.println(getName() + " Done reciving");
        }
    } // end run
    
    /**
     * Gets the buffer.
     *
     * @return the buffer
     */
//	public synchronized byte[] getBuffer() {
//		return buffer;
//	}
    
    /**
     * Gets the port.
     *
     * @return the port
     */
    public int getPort() {
        return options.port;
    }
    
    /**
     * Send. Not used
     */
    private void send() {
//		logger.info("start sending");
//		boolean done = false;
//		while (!done) {
//			synchronized (pool) {
//				while (pool.isEmpty()) {
//					try {
//						pool.wait();
//					} catch (InterruptedException ex) {
//						if (pool.isEmpty()) {
//							done = true;
//							logger.finer("No more data to send. Streamed "
//									+ byteStreamed + " bytes");
//							// System.out.println(this.getName()+"
//							// byteStreamed="+byteStreamed);
//							closeConnection();
//							break;
//						} else {
//							logger.log(Level.WARNING, "Uncaught exception", ex);
//							// ex.printStackTrace();
//						}
//					}
//				}
//				if (!done) {
//					intbuffer = (int[]) pool.remove(pool.size() - 1);
////					if (intbuffer.length != buffer.length) {
//						buffer = new byte[intbuffer.length];
////					}
//					for (int i = 0; i < intbuffer.length; i++) {
//						buffer[i] = (byte) intbuffer[i];
//					}
//					try {
//						byteStreamed = byteStreamed + buffer.length;
//						write(buffer, 0, buffer.length);
//					} catch (IOException e) {
//						logger.log(Level.SEVERE, "Uncaught exception", e);
//						e.printStackTrace();
//						// System.exit(1);
//					}
//				}
//			}
//		}
    }
    
    /**
     * Recive. Not used
     */
    private void recive() {
//		logger.info("start reciveing");
//		boolean done = false;
//		while (!done) {
//			try {
//				buffer = read();
//				if (buffer.length != intbuffer.length) {
//					intbuffer = new int[buffer.length];
//				}
//				for (int i = 0; i < buffer.length; i++) {
//					intbuffer[i] = buffer[i];
//				}
//				byteStreamed = byteStreamed + intbuffer.length;
//				synchronized (pool) {
//					pool.add(0, intbuffer);
//					pool.notify();
//				}
//			} catch (IOException e) {
//				if (e.getMessage().equals("CONNECTION_EOS")
//						|| e.getMessage().equals("Receive timed out")) {
//					// System.out.println(this.getName()+"
//					// byteStreamed="+byteStreamed);
//					done = true;
//					closeConnection();
//					break;
//				} else {
//					// System.err.println(this.getName());
//					logger.warning(e.toString());
//					// e.printStackTrace();
//					break;
//				}
//			}
//		}
//		synchronized (pool) {
//			pool.notify();
//		}
    }
    
    /**
     * Sets the logger.
     * 
     */
    protected static void setLogger() {
        
        boolean ok = false;
        if (!(new File("logs/")).exists()) {
            ok = (new File("logs/")).mkdir();
        } else {
            ok = true;
        }
        logger = Logger.getLogger(Connection.class.getName());
        logger.setUseParentHandlers(false);
        logger.setUseParentHandlers(true);
        logger.setLevel(Constants.LOG_LEVEL);
        try {
            
            if (ok && Constants.saveLogs) {
                FileHandler handler = new FileHandler("logs/"+Connection.class.getName()+".log");
                handler.setLevel(Constants.LOG_LEVEL);
                logger.addHandler(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(Constants.LOG_LEVEL);
        logger.addHandler(console);
        
    }
    
    public static void collectGarbage(){
        try {
            System.runFinalization();
            System.gc();
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Uncaught exception", ex);
        }
    }
}
