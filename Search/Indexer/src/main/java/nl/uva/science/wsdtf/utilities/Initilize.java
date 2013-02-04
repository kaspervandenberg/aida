package nl.uva.science.wsdtf.utilities;

import java.io.*;
import javax.xml.bind.JAXBException;

import org.globus.gsi.gssapi.auth.Authorization;
import org.globus.gsi.gssapi.auth.SelfAuthorization;

import nl.uva.science.wsdtf.StreamClientImpl;
import nl.uva.science.wsdtf.StreamServerImpl;
import nl.uva.science.wsdtf.io.*;

import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


public class Initilize {
    
    private Authorization authorization;
    
    private int protocol;
    
    private String hostname;
    
    private int port;
    
    private StreamServerImpl server;
    
    private StreamClientImpl client;
    
    private Thread threadClient, threadser;
    
    private boolean encryptData;
    private boolean authentication=true;
    
    private Options Serveropt;
    private Options Clientopt;
    
    private static StreamConfig conf;
    
    private String userName;
    
    private String password;
    
    protected static Logger logger = null;
    
    private Connector conn;
    
    private PipedInputStream serverIn;
    private PipedOutputStream serverOut;
    
    private PipedInputStream clientIn;
    private PipedOutputStream clientOut;
        
    /**
     *Use this constuctr to iniilize steaming by an xml configration file
     *@param filePath the locaton of the configuration file
     */
    public Initilize(String filePath) {
        authorization = SelfAuthorization.getInstance();
        initLogger();
        configureFromFile(filePath);
        initPipes();
    }
    
        /**
     *Use this constuctr to iniilize steaming by an <code>StreamConfig</code> class 
     *@param the <code>StreamConfig</code> 
     */
    public Initilize(StreamConfig conf) {
        authorization = SelfAuthorization.getInstance();
        initLogger();
        setConf(conf);
        configure();
        initPipes();
    }
    
    /**
     * Empty constructor 
     */
    public Initilize() {
    }
    
    /**
     Initializes piped steams , for communication between the client/server, and the <code>Connector</code>
     */
    private void initPipes(){
        try {
            //for java 1.6
            //serverIn = new PipedInputStream(Constants.MAX_BUFFER_SIZE);
            serverIn = new PipedInputStream();
            serverOut = new PipedOutputStream(serverIn);
            //for java 1.6
            //clientIn = new PipedInputStream(Constants.MAX_BUFFER_SIZE);
            clientIn = new PipedInputStream();
            clientOut = new PipedOutputStream(clientIn);
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Uncaught exception", e);
            System.exit(1);
        }
    }
    
    /**
     * Set channel encryption 
     * @param val
     */
    public void setEncryption(boolean val){
        encryptData = val;
        logger.fine("Encrypt cahnnel: "+val);
    }
    
    /**
     * Sets channel authentication 
     * @param val
     */
    public void setAuthentication(boolean val){
        if(!val){
            encryptData = val;
            logger.fine("Encrypt cahnnel: "+val);
        }
        authentication = val;
        logger.fine("Authenticate other side: "+val);
    }
    
    /**
     * Initializes a client. Options, server location etc.
     * @throws java.io.IOException
     */
    public void client() throws IOException{
//		try {
        Clientopt = new Options(getProtocol(), port);
        
        Clientopt.hostname = hostname;
        if(this.authentication){
            Clientopt.authorization = authorization;
            Clientopt.credentials = Security.loadProxyCredential(null);
        }
        Clientopt.isServer = false;
        Clientopt.username = this.userName;
        Clientopt.passwd = this.password;
        Clientopt.encryptData = encryptData;
        Clientopt.authentication = this.authentication;
        Clientopt.fileRequest =  getConf().fileRequest;
        Clientopt.out = this.clientOut;
        
        client = new StreamClientImpl(Clientopt);
        threadClient = new Thread(getClient());
        
        threadClient.setName("Client");
        
        logger.finer("Client options are set");
        
    }
    
    /**
     * Starts a client thread.
     */
    public void startClient() {
        threadClient.start();
        logger.finer("Thread client started");
    }
    
    
    /**
     *  Initializes a server Options, server location etc.
     */
    public void server() {
        try {
            Serveropt = new Options(getProtocol(), port);
            if(this.authentication){
                Serveropt.authorization = authorization;
                Serveropt.credentials = Security.loadProxyCredential(null);
            }
            Serveropt.isServer = true;
            Serveropt.username = this.userName;
            Serveropt.passwd = this.password;
            Serveropt.encryptData = encryptData;
            Serveropt.authentication = this.authentication;
            Serveropt.keepStreamAlive = getConf().keepStreamAlive;
            Serveropt.in = this.serverIn;
            
            server = new StreamServerImpl(Serveropt);
            
            threadser = new Thread(server);
            
            threadser.setName("Server");
            Serveropt.hostname = server.getHostName();
            this.hostname = Serveropt.getHostName();
            
            logger.finer("Server options are set");
//			Serveropt.port = server.getPort();
//			this.port = Serveropt.getPort();
            // Wait for threads to end
            // threadser.join();
            // threadProducer.join();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Starts a server thread
     */
    public void startServer() {
        threadser.start();
        logger.finer("Thread server started");
    }
    
    /**
     * Returns the host name of the server.
     * @return the server name
     */
    public String getHostName() {
        return hostname;
    }
    
/**
 *  Sets the host name of the server.
 * @param hostname the host name 
 */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    /**
     * Kills the server thread. 
     */
    public void killServer(){
        server.kill();
        logger.fine("Killing server");
    }
    
    /**
     * Returns client or server threads.
     * @param name, two possible values: <code>Client</code> or <code>Server</code>
     * @return the thread
     */
    public Thread getThread(String name){
        if(name.equals("Client")){
            return threadClient;
        }
        if(name.equals("Server")){
            return threadser;
        }
        return null;
    }
    
    /**
     * Sets the port 
     * @param port
     */
    public void setPort(int port){
        this.port = port;
    }
    
    /**
     * Gets the port 
     * @return
     */
    public int getPort(){
        return this.port;
    }
    
    /**
     * Sets the path to save a file, if client, or the file to stream if server 
     * @param path
     */
    public void setPath(String path){
        if(Serveropt!=null){
            Serveropt.fileRequest=path;
            logger.fine("Server Options got file request: "+path);
        }
        if(Clientopt!=null){
            Clientopt.pathToSave = path;
            logger.fine("Client Options got file to save: "+path);
        }
    }
    
    /**
     * Returns the path, to save, or stream a file 
     * @return
     */
    public String getPath(){
        if(Serveropt!=null){
            return Serveropt.fileRequest;
        }
        if(Clientopt!=null){
            return Clientopt.pathToSave;
        }
        return null;
    }
    
    /**
     * Initilizes the logger for this class 
     */
    private static void initLogger(){
        try {
            boolean ok = false;
            if (!(new File("logs/")).exists()) {
                ok = (new File("logs/")).mkdir();
            } else {
                ok = true;
            }
            logger = Logger.getLogger(Initilize.class.getName());
            logger.setUseParentHandlers(false);
            logger.setUseParentHandlers(true);
            logger.setLevel(Constants.LOG_LEVEL);
            
            if (ok && Constants.saveLogs && new File("logs/").canWrite()) {
                FileHandler handler = new FileHandler("logs/Initilize.log");
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
     * Creates a defult XML config file 
     * @param path, where to save the file
     */
    public void createConfigFile(String path){
        File file = new File(path);
        conf = new StreamConfig();
        setConf(conf);
        JAXBContext context;
        Marshaller marshaller = null;
        {
            //pretty print XML
            //Only for java 1.6
            //marshaller.marshal(getConf(), file);
            FileOutputStream fos = null;
            try {
                context = JAXBContext.newInstance(nl.uva.science.wsdtf.utilities.StreamConfig.class);
                marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                fos = new FileOutputStream(file);
                marshaller.marshal(getConf(), fos);

                //logger.fine("Configuration file is created in " + path);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Initilize.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JAXBException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException ex) {
                    Logger.getLogger(Initilize.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    /**
     * Converts an <code>StreamConfig</code> to a string 
     * @param conf the <code>StreamConfig</code> 
     * @return, the string 
     */
    public String Config2String(StreamConfig conf){
        JAXBContext context;
        Marshaller marshaller = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String strConf=null;
        try {
            context = JAXBContext.newInstance(nl.uva.science.wsdtf.utilities.StreamConfig.class);
            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); //pretty print XML
            marshaller.marshal(conf, out);
            strConf = new String(out.toByteArray());
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
        return strConf;
    }
    

    /**
     * Loads an XML config file as an  <code>StreamConfig</code>  class
     * @param filePath, the file location.
     * @return the  <code>StreamConfig</code> 
     */
    public StreamConfig loadConfigFile(String filePath){
//            StreamConfig conf = null;
        try {
            JAXBContext jc = JAXBContext.newInstance(nl.uva.science.wsdtf.utilities.StreamConfig.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            File cfg = new File(filePath);
            
            if (cfg.exists() != true){
                logger.warning("Config File does not exist in "+filePath);
            }
            
            Unmarshaller u = jc.createUnmarshaller();
            setConf((StreamConfig)u.unmarshal(cfg));
            logger.fine("Configuration file loaed from "+filePath);
            
        } catch(JAXBException e ) {
            e.printStackTrace();
        }
        return getConf();
    }
    
    /**
     * Returns a string as a  <code>StreamConfig</code>  
     * @param strConf, the config as a string 
     * @return the  <code>StreamConfig</code> 
     */
    public StreamConfig String2Config(String strConf){
        StreamConfig conf = null;
        try {
            JAXBContext jc = JAXBContext.newInstance(nl.uva.science.wsdtf.utilities.StreamConfig.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            
            Unmarshaller u = jc.createUnmarshaller();
            ByteArrayInputStream in = new ByteArrayInputStream(strConf.getBytes());
            conf = (StreamConfig)u.unmarshal(in);
        } catch(JAXBException e ) {
            e.printStackTrace();
        }
        return conf;
    }
    
    /**
     *Loads a confguration xml file forom the specifed path
     *@param filePath the locaton of the configuration file
     */
    public void configureFromFile(String filePath){
        setConf(loadConfigFile(filePath));
        configure();
        logger.finest("Read configuration file from "+filePath);
        
    }
    
    /**
     * Configures a connection, accoarding to the <code>conf</code>
     */
    private void configure(){
        Constants.setMaxBufferSize(getConf().maxBufferSize);
        setProtocol(getConf().translateProtocol(getConf().protocol));
        setHostname(getConf().host);
        setEncryption(getConf().ecnryptData);
        setAuthentication(getConf().authenticate);
        setUserName(getConf().username);
        setPassword(getConf().password);
        setPort(getConf().port);
        setPath(getConf().path);
//            queue = new ArrayBlockingQueue<Object>(getConf().queueSize,true);
        Constants.setLogLevel(getConf().loggLevel);
        Constants.displaySpeed = getConf().displaySpeed;
        Constants.saveLogs = getConf().saveLogs;
        initLogger();
        logger.config("Configurations:\n Max Buffer Size:"+Constants.MAX_BUFFER_SIZE+
                "\n Protocol:"+getConf().translateProtocol(this.getProtocol())+
                "\n Host name:"+this.getHostName()+
                "\n Encryption:"+this.encryptData+
                "\n Authenticate:"+this.authentication+
                "\n Port:"+this.getPort()+
                "\n Path:"+this.getPath()+
                "\n file name"+getConf().fileName+
                "\n LogLevel: "+Constants.LOG_LEVEL+
                "\n Display Speed: "+Constants.displaySpeed+
                "\n Save Logs: "+ Constants.saveLogs+
                "\n File Requested: "+ getConf().fileRequest);
    }
    
    public int getProtocol() {
        return protocol;
    }
    
    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public static StreamConfig getConf() {
        return conf;
    }
    
    public static void setConf(StreamConfig aConf) {
        conf = aConf;
    }
    
    public Connector getConnector() {
        if(this.getThread("Client")!=null){
            conn = new Connector(clientIn);
            serverOut = null;
            serverOut =null;
        }else{
            conn = new Connector(serverOut);
            clientIn=null;
            clientOut=null;
        }
        return conn;
    }
    
    public StreamServerImpl getServer(){
        return server;
    }
    
    public StreamClientImpl getClient() {
        return client;
    }
}
