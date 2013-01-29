package nl.uva.science.wsdtf.io;


import java.io.*;

import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
//import java.util.ArrayList;
//import java.util.concurrent.BlockingQueue;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import nl.uva.science.wsdtf.utilities.Constants;

/**
 * <description> This class provides connections with the <code>StreamClientImpl</code>
 * or <code>StreamServerImpl</code> client. This class should only provide <code>byte[]</code> to its stream (in or out)
 * @author S. Koulouzis
 * @version alpha, March 21, 2008, 1:46 PM
 *
 */

public class Connector{
    
    private static Logger logger = null;
    
    private byte[] data;
    
    private volatile Object currentData;
        
    private ByteArrayOutputStream baos1;
    
    //private ByteArrayInputStream bais;
    
    //private PipedWriter pipeWriter;
    
    //private PipedReader pipeReader;
    
    //private PipedOutputStream pouts;
    
    //private PipedInputStream pinc;
    
    private BufferedOutputStream bufferOut;
    
    private BufferedInputStream bufferIn;
    
    //private boolean breakXML=false;
    //private String delimiter;
    //private int breakInterval;
    //private int prevTag = -1;
    //private int count = 0;
    //private String tag;
    //private String newTag;
    private boolean startCounting;
    //private int bytesFead=0;
    /**
     * Creates a new instance of Connector for senfing data to the <code>StreamServer</code>
     */
    public Connector(OutputStream serverOut) {
        bufferOut = new BufferedOutputStream(serverOut,Constants.MAX_BUFFER_SIZE);
        data = new byte[Constants.MAX_BUFFER_SIZE];
        initLogger();
    }
 
     /**
     * Creates a new instance of Connector for getting data from the <code>StreamClient</code>
     */
    public Connector(PipedInputStream clientIn) {
        bufferIn = new  BufferedInputStream(clientIn,Constants.MAX_BUFFER_SIZE);
        data = new byte[Constants.MAX_BUFFER_SIZE];
        initLogger();
    }    
        
    /**
     * Reads bytes from a file and puts them in the <code>OutputStream</code>.
     * The file location is specfed by the <code>currentData</code>
     */
    private void sendFile(){
        FileInputStream fis = null;
        InputStream someIn = null;
        int len = 0;
        try {
            if(currentData instanceof java.lang.String ){
                fis = new FileInputStream( (String)currentData );
            }else if (currentData instanceof java.io.File){
                fis = new FileInputStream( (File)currentData );
            }

            while ((len = fis.read(data)) != -1) {
                bufferOut.write(data,0,len);
            }
            bufferOut.flush();
            fis.close();
        } catch (NullPointerException ex) {
            logger.log(Level.SEVERE, "Uncaught exception", ex);
        }catch(IOException ex){
            logger.log(Level.SEVERE, "Uncaught exception", ex);
        }
    }
    
    
    /**
     * Saves incoming bytes to a file
     */
    private void storeFile(){
        FileOutputStream fos = null;
        //long startTime = System.currentTimeMillis();
        int len = 0;
        try {
            if(currentData instanceof java.lang.String ){
                fos = new FileOutputStream((String)currentData);
                logger.finest("Saving incoming bytes to: " + (String)currentData);
            }else if (currentData instanceof java.io.File){
                fos = new FileOutputStream((File)currentData);
                logger.finest("Saving incoming bytes to: " + (File)currentData);
            }
            
            while ((len = bufferIn.read(data)) != -1) {
                fos.write(data,0,len);
            }
            fos.flush();
            fos.close();
            
        } catch (Exception ex) {
            ex.printStackTrace();
//            if (ex.getMessage().equals("Pipe broken")) {
//                logger.fine("End of incoming stream");
//            } else {
//                logger.log(Level.SEVERE, "Uncaught exception", ex);
//            }
        }
    }
    
    
    /**
     * Save incoming bytes to a file.
     * @param path the file path.
     */
    public void save(String path) {
        currentData = path;
        storeFile();
    }
    
    /**
     * Save incoming bytes to a file.
     * @param path the file path.
     */
    public void save(File file) {
        currentData = file;
        storeFile();
    }
    
    /**
     * Gets bytes from a file and sends them to the Server for streaming
     * @param path the file path.
     *
     */
    public void streamFile(String path){
        currentData = path;
        sendFile();
    }
    
    
    /**
     * Gets bytes from a file and sends them to the Server for streaming
     * @param path the file path.
     *
     */
    public void streamFile(File file){
        currentData = file;
        sendFile();
    }
    
    
    public BufferedInputStream getBufferedInputStream() {
        return bufferIn;
    }
    
    public BufferedOutputStream getOutputStream() {
        return bufferOut;
    }
    
    
    /**
     *Initializes this classes longer
     */
    private void initLogger(){
        try {
            boolean ok = false;
            if (!(new File("logs/")).exists()) {
                ok = (new File("logs/")).mkdir();
            } else {
                ok = true;
            }
            logger = Logger.getLogger(Connector.class.getName());
            logger.setUseParentHandlers(false);
            logger.setUseParentHandlers(true);
            logger.setLevel(Constants.LOG_LEVEL);
            
            if (ok && Constants.saveLogs) {
                FileHandler handler = new FileHandler("logs/Connector.log");
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
    
    /**
     *Close streams with client/server
     */
    public void END(){
        try {
            if(bufferOut!=null){
                bufferOut.flush();    
                bufferOut.close();
            }
            if(bufferIn!=null){
                bufferIn.close();
            }
           
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
        
//    public PipedReader getPipeReader() {
//        return pipeReader;
//        //return reader;
//    }
//    
//    public PipedWriter getPipeWriter() {
//        return pipeWriter;
//        //return writer;
//    }
    
    public ByteArrayOutputStream getBaos1() {
        return baos1;
    }
    
    public boolean isStartCounting() {
        return startCounting;
    }  
}
