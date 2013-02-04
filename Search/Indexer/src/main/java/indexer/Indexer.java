package indexer;

import java.io.*;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Iterator;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import javax.xml.transform.TransformerFactoryConfigurationError;

import nl.uva.science.wsdtf.utilities.*;
import nl.uva.science.wsdtf.io.Connector;
import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.apache.axis.attachments.AttachmentPart;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.client.async.AsyncCall;
import org.apache.axis.client.async.IAsyncResult;
import org.w3c.dom.Document;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Machiel Jansen, Edgar Meij
 */
public class Indexer {
    private static final int breakInterval = 5*1024*1024;
    private Initilize init;
    
    public static final int SWA=100;
    public static final int SOAP=200;
    public static final int TCP=500;
    
    private static long size = 0;
        
    /** Creates a new instance of Indexer */
    public Indexer() {
    }
    
    /** logger for Commons logging. */
    private static transient Logger log =
            Logger.getLogger(Indexer.class.getName());
    
    /**
     * Index file(s) on the server into an index on the server using the settings in a string
     *
     * @param config      String containing the indexconfig xml
     * @param extension   Extension of the data (to associate the filetype)
     * @param data        data to be indexed on the server
     * @return            status string
     */
    public String indexFromRemote(String config, String data, String extension) {
        
        String configfile = Utilities.createTemporaryFile(config, "xml");
        String datafile = Utilities.createTemporaryFile(data, extension);
        
        String status = indexFromCFG(configfile, null, datafile);
        
        new File(configfile).delete();
        new File(datafile).delete();
        
        return status;
    }

  /**
  * Add files to index on a server
  * 
  * @param indexName   name of the index
  * @param filename    filename to use for the data
  * @param filedata    data to be indexed on the server
  * @return            status string
  */
  public String addToIndex(String filedata, String filename, String indexName) {
    return addToIndexWithConfig(filedata, filename, indexName, Utilities.createDefaultConfigFile());
  }

 /**
    * Add files to index on a server
    * 
    * @param indexName   name of the index
    * @param filename    filename to use for the data
    * @param filedata    data to be indexed on the server
    * @param config      String containing the indexconfig xml
    * @return            status string
  */
  public String addToIndexWithConfig(String filedata, String filename, String indexName, String config) {

    String configfile = Utilities.createTemporaryFile(config, "xml");
    ConfigurationHandler cfg = new ConfigurationHandler(configfile);
    cfg.SetOverWrite(false);
    cfg.setName(indexName);

    String datafile = Utilities.createTemporaryFile(filedata, filename, true);

    BaseIndexing BI = new BaseIndexing(cfg, null, datafile);
    boolean success = false;

    try {
      success = BI.addDocuments();
    } catch(IOException e) {
      if (log.isLoggable(Level.SEVERE))
        log.severe(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
      return "Indexing failed: " + e.getClass() + e.getMessage();
    }

    new File(datafile).delete();
    new File(configfile).delete();
    return success ? "added " + filename + " to the index " + indexName : "Indexing failed" ;
  }
    
  /**
  * Index file(s) on the server into an index on the server using the settings from a configfile
  * 
  * @param configfile  Path to configfile on the server
  * @param name        Name of the index to use/create
  * @param dataPath    Path to the files to be indexed on the server
  * @return            status string
  */
  public String indexFromCFG(String configfile, String name, String dataPath) {
       
    if (new File(configfile).exists()) {
      
      BaseIndexing BI = new BaseIndexing(configfile, name, dataPath);
      boolean success = false;

      try {
        success = BI.addDocuments();
      } catch (IOException e) {
        if (log.isLoggable(Level.SEVERE)) {
          log.severe(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
        return "Indexing failed: " + e.getClass() + e.getMessage();
      }
      
      if (success) 
        return "Indexing finished, added " 
                + BI.added + " files to " + BI.indexdir
                + " (was unable to index " + BI.failed + " files)"
                ;
      else 
        return "Indexing failed";
      
    } else {
      return "Configuration file not found";
    }  
  }
    
    /**
     * Index file(s) on the server into an index on the server using default settings
     *
     * @param name        Name of the index to use/create
     * @param dataPath    Path to the files to be indexed on the server
     * @return            status string
     */
    public String index(String name, String dataPath) {
        
        if((name.length()>100) || (dataPath.length()>100))
            return "No valid input";
        
        String configfile = "indexconfig.xml";
        
        return indexFromCFG(configfile, name, dataPath);
    }
    
    /**
     * Index file(s) from SRB into an index on the server using default settings
     *
     * @param userName    SRB username
     * @param password    SRB password
     * @param fromPath    Path to the files to be indexed on SRB
     * @return            status string
     */
    public String indexFromSRB(String userName, String password, String fromPath) {
        long start = System.currentTimeMillis();
        SRBhandler sh = new SRBhandler();
        sh.setAccount(userName, password);
        
        // make sure it's unique
        File temp = new File(userName);
        int cnt = 1;
        while (temp.exists()) {
            temp = new File(userName + cnt);
            cnt++;
        }
        
        String dataPath = sh.getSRBFile(fromPath, temp.getName());
        String path = userName + "/" + dataPath.substring(2);
        String status = index(userName, path);
        Utilities.deleteDir(temp);
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        log.info("Indexing time: "+elapsed+" msec");
        return(status);
    }

	/**
	 * Index file(s) from a Windows share (Samba or CIFS) into an index on the 
	 * server using {@code config} as settings. Analogue to {@link #indexFromSRB}.
	 * 
	 * @param fromPath	URL for the file or directory to index. 
	 * 		See {@link http://jcifs.samba.org/src/docs/api/jcifs/smb/SmbFile.html} 
	 * 		for detailed syntax.  SMB URLs have the following syntax: {@code 
	 * 		smb://[[[domain;]username[:password]@]server[:port]/[[share/[dir/]file]]][?[param=value[param2=value2[...]]]}
	 * 		Examples: {@code smb://usersnyc;miallen:mypass@angus/tmp/},
	 * 		{@code smb://Administrator:P%40ss@msmith1/c/WINDOWS/Desktop/foo.txt}, 
	 * 		{@code smb://angus.foo.net/d/jcifs/pipes.doc}, 
	 * 		{@code smb://192.168.1.15/ADMIN$/} and
	 * 		{@code smb://domain;username:password@server/share/path/to/file.txt}
	 * @param config	String containing the contents of indexconfig.xml
	 * 
	 * @return	Status string 
	 */
	public String indexFromSamba(String fromPath, String config) {

		throw new UnsupportedOperationException("Not yet implemented");
	}
    
     /**
     * Index file(s) from a TCP stream into an index on the server using settings included in the beginning of the stream
     *
     * @param streamingConfig    the TCP stream configurations, e.g. port
     * @param extension    not used
     * @return            indexed files
     */
    public String indexWithTCP(String streamingConfig,String extension){
        long start = System.currentTimeMillis();
        String streamConfigfile = Utilities.createTemporaryFile(streamingConfig, "xml");
        String files;
        try{
            files = handleStream( streamConfigfile );
        }finally{
            new File(streamConfigfile).delete();
        }
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        log.info("Recived "+ size +" bytes. \n"+
                "Indexing time: "+elapsed+" msec \n"
                +"Speed: "+( ( (size/1024.0)/((elapsed)/1000.0)) )+" kbyte/sec" );
        return files;
    }
    
     /**
     * Starts a TCP connection according to the <code>streamConfigfile</code> and handles incoming XML stream, 
     *
     * @param streamConfigfile    the TCP stream configurations, e.g. port
     * @param extension    not used
     * @return            indexed files 
     */
    private String handleStream(String streamConfigfile){
        Connector conn = initTransfer(streamConfigfile,0,false);
        BufferedInputStream in = conn.getBufferedInputStream();
        
        String indexedDocs=",";
        String currentDoc="";
        String data=null;
        String tmpFilePath = System.getProperty("java.io.tmpdir");
        
        if(! new File(tmpFilePath).exists() || !new File(tmpFilePath).canWrite()){
            tmpFilePath = System.getProperty("user.dir");
        }
        
        File tmpIndexConfFile = new File(tmpFilePath+"/tmpIndexConf.xml");
        FileWriter fosConf = null;
        
        RemoteIndexer indexer=null;
        
        int prevTag=-1;
        int dataParsed=0;
        long start=-999;
        int TOOMUCH = 47185920;
        
        XMLStreamReader reader=null;
        try {
            reader =   XMLInputFactory.newInstance().createXMLStreamReader(in);
            fosConf = new FileWriter(tmpIndexConfFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            //start parsing XML 
            while (reader.hasNext()) {
                Integer eventType;
                
                eventType = reader.next();
                
                //Start elements---------------
                if( eventType == XMLEvent.START_ELEMENT){
                    if(reader.getLocalName().equals("stream")){
                    }else if(reader.getLocalName().equals("index_config")){
                        prevTag = 0;
                    }else if(reader.getLocalName().equals("fileName") ){
                        prevTag = 1;
                    }else if(reader.getLocalName().equals("content")){
                        start = System.currentTimeMillis();
                        prevTag = 2;
                    }else if(reader.getLocalName().equals("chunk")){
                        prevTag = 3;
                    }else{
                        prevTag = 2000;
                    }
                }
                
                //Data-------------------------
                if( eventType == XMLEvent.CHARACTERS ){
                    data = reader.getText();
                    size = (int)(size + data.getBytes().length);
                    switch(prevTag){
                        //start saving the indexConfig files
                        case 0:
                            fosConf.write(data);
                            fosConf.flush();
                            break;
                            //Use the file name
                        case 1:
                            currentDoc = data;
                            break;
                            //star of content
                        case 2:
                            break;
                            //start of content chunk add it to the doc
                        case 3:
                            dataParsed = dataParsed + data.length();
                            size = size + data.getBytes().length;
                            indexer.addContent("pdf",data);
                            if(dataParsed>=TOOMUCH){
//                                System.out.println("  Too much data  Adding doc to index.. size of content: "+dataParsed);
                                indexer.addDocToIndex();
                                indexer.closeWriter();
                                dataParsed = 0;
                            }
                            break;
                        default :
//                            eventType = reader.next();
                            break;
                    }
                }
                
                //End elements---------------
                if( eventType == XMLEvent.END_ELEMENT){
                    //close the fosConf and instantiate the indexer
                    if(reader.getLocalName().equals("index_config")){
                        fosConf.flush();
                        fosConf.close();
                        //Instantiating the indexer
                        indexer = new RemoteIndexer(tmpIndexConfFile.getPath());
                    }
                    //nothing
//                    if(reader.getLocalName().equals("fileName")){
                    
//                    }
                    //save the doc to the index
                    if(reader.getLocalName().equals("content")){
//                        System.out.println("    Adding doc to index.. size of content: "+dataParsed);
                        indexer.addDocToIndex();
                        log.info("Indexing took "+(System.currentTimeMillis()-start));
                        start = System.currentTimeMillis();
                        dataParsed = 0;
                        indexedDocs = indexedDocs +","+ currentDoc;
                    }
                    //nothing
//                    if(reader.getLocalName().equals("chunk")){
//                    }
                    if(reader.getLocalName().equals("stream")){
                        prevTag = 1000;
                    }
                    
                    try {
                        System.runFinalization();
                        System.gc();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                if(eventType == XMLEvent.END_DOCUMENT){
                    prevTag = 1000;
                }
            }//end while
            
            indexer.closeWriter();
            tmpIndexConfFile.delete();
            
        } catch (Exception ex) {
//            System.err.println("PARSER ERROR: ");
//            System.err.println("Element was "+reader.getLocalName() );
//            System.err.println("Q size was "+conn.getQ().size() );
//            System.err.println("Prev tag was "+prevTag );
//            System.err.println("Input file "+infIle );
            ex.printStackTrace();
            init.getClient().kill();
            return null;
        }
//        new File(infIle).delete();
        return indexedDocs;
    }
     
    /**
     * Index file(s) from a SOAP message. You may call this method with an existing file name in the index, if you want to append content
     *
     * @param indexConfig    the index configuration as a String
     * @param extension    the content
     * @return            indexed files
     */    
    public String indexWithSOAP(String indexConfig,String content,String fileName){
        long start = System.currentTimeMillis();
        String indexedDocs=fileName;
        String tmpIndexConfPath = Utilities.createTemporaryFile(indexConfig,"xml");
        RemoteIndexer indexer = new RemoteIndexer(tmpIndexConfPath);
        size = (size + content.getBytes().length);
        indexer.addContent("pdf",content);
        indexer.addDocToIndex();
        try {
            indexer.closeWriter();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        new File(tmpIndexConfPath).delete();
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        String.valueOf(elapsed);
        log.info("Recived: "+ size +" bytes. \n" +
                "Indexing time: "+elapsed+" msec\n"
                +"Speed: "+( ( (size/1024.0)/((elapsed)/1000.0)) )+" kbyte/sec" );
        return indexedDocs;
    }
    

     /**
     * Index file(s) from a SOAP attachment. You may call this method with an existing file name in the index, if you want to append content
     *
     * @param indexConfig    the index configuration as a String
     * @return             indexed files
     */     
    public String indexWithSwA(String indexConfig){
        long start = System.currentTimeMillis();
        String indexedDocs="";
        Iterator it = MessageContext.getCurrentContext().getCurrentMessage().getAttachments();
        String content = "";
        String tmpIndexConfPath = Utilities.createTemporaryFile(indexConfig,"xml");
        RemoteIndexer indexer = new RemoteIndexer(tmpIndexConfPath);
        while(it.hasNext()){
            AttachmentPart ap = (AttachmentPart)it.next();
            DataHandler handler;
            try {
                handler = ap.getDataHandler();
                indexedDocs = indexedDocs +","+ ap.getContentId();
                content = content + (String)handler.getContent();
                size = (size + content.getBytes().length);
            } catch (SOAPException ex) {
                ex.printStackTrace();
            }  catch (IOException ex) {
                ex.printStackTrace();
            }
            ap.detachAttachmentFile();
            ap.dispose();
        }
        
        indexer.addContent("pdf",content);
        indexer.addDocToIndex();
        try {
            indexer.closeWriter();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        new File(tmpIndexConfPath).delete();
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        log.info("Recived: "+ size +" bytes. \n" +
                "Indexing time: "+elapsed+" msec\n" +
                "Speed: "+( ( (size/1024.0)/((elapsed)/1000.0)) )+" kbyte/sec" );
        return indexedDocs;
    }
    
    
    /**
     * Extracts content from a PDF file, and strams it in the service listening at <code>endPoint</code>
     *
     * @param streamingConfig    the stream configuration as a String
     * @param dir    the location of the PDF folder or file 
     * @param endPoint    the location where the service listening at.
     * @param method    what method should it be used for straming the contenet (HTTP,SOAP,etc.)
     * @param generateContent    used for testing. It generates a txt content.
     * @return            files stramed
     */    
    public String streamContent(String streamingConfig,String indexConfig,String dir,String endPoint,int method,boolean generateContent){
        if(!generateContent){
            return streamPDFContent(streamingConfig,indexConfig,dir,endPoint,method);
        }else{
            return streamLiveContent(streamingConfig,indexConfig,endPoint,dir,method);
        }
    }
    
     
    /**
     * Extracts content from a PDF file, and strams it in the service listening at <code>endPoint</code>
     *
     * @param streamingConfig    the stream configuration as a String
     * @param dir    the location of the PDF folder or file 
     * @param endPoint    the location where the service listening at.
     * @param method    what method should it be used for straming the contenet (HTTP,SOAP,etc.)
     * @return            files stramed
     */    
    
    private String streamPDFContent(String streamingConfig,String indexConfig,String dir,String endPoint,int method){
        long start = System.currentTimeMillis();
        String result =null;
        if(streamingConfig == null || streamingConfig.equals("") ){
            URI server=null;
            try {
                server = new URI("tcp://" + getIP() + ":8199");
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
            Initilize i = new Initilize();
            streamingConfig = i.Config2String(getConf(server,"TCP"));
        }
        
        log.info("Streaming contents from "+dir+" to "+endPoint);
        switch(method){
            case SOAP:
                result = streamPDFContentWithSOAP(indexConfig,endPoint,dir,false);
                break;
            case SWA:
                result = streamPDFContentWithSOAP(indexConfig,endPoint,dir,true);
                break;
            case TCP:
                result = streamPDFContentWithTCP(streamingConfig,indexConfig,endPoint,dir);
                break;
        }
        
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        log.info("Transfered: "+ size +" bytes\n" +
                "Time elapsed: "+elapsed +" mscec\n"+
                "Speed: "+( ( (size/1024.0)/((elapsed)/1000.0)) )+" kbyte/sec" );
        return result;
    }
    
    
    /**
     * Used for testing. Generates a content in simple txt 
     *
     * @param streamingConfig    the stream configuration as a String
     * @param indexConfig    the index configuration as a String 
     * @param endPoint    the location where the service listening at.
     * @param method    what method should it be used for straming the contenet (HTTP,SOAP,etc.)
     * @param generateContent    used for testing. It generates a txt content.
     * @return            files stramed
     */  
    private String streamLiveContent(String streamingConfig,String indexConfig,String endPoint,String dir,int method){
        long start = System.currentTimeMillis();
        String result =null;
        if(streamingConfig == null || streamingConfig.equals("") ){
            URI server=null;
            try {
                server = new URI("tcp://" + getIP() + ":8199");
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
            Initilize i = new Initilize();
            streamingConfig = i.Config2String(getConf(server,"TCP"));
        }
        
        log.info("Streaming contents to "+endPoint);
        switch(method){
            case SOAP:
                result = streamContentWithSOAP(indexConfig,endPoint,dir,false);
                break;
            case SWA:
                result = streamContentWithSOAP(indexConfig,endPoint,dir,true);
                break;
            case TCP:
                result = streamContentWithTCP(streamingConfig,indexConfig,dir,endPoint);
                break;
        }
        
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        log.info("Transfered: "+ size +" bytes\n" +
                "Time elapsed: "+elapsed +" mscec\n"+
                "Speed: "+( ( (size/1024.0)/((elapsed)/1000.0)) )+" kbyte/sec" );
        return result;
    }
    
    private String streamPDFContentWithSOAP(String indexConfig,String endPoint,String dir,boolean attach){
        File pdfDir = new File(dir);
        String[] fileNames=null;
        String indexedFiles = "";
        if(pdfDir.isDirectory()){
            fileNames = pdfDir.list();
        }else{
            fileNames = dir.split(",");
        }
        
        DocConverter converter;
        Thread t;
        File pdfFile;
        int len = fileNames.length;
        PipedReader pipeReader;
        Document XMLDoc=null;
        StringWriter sw = new StringWriter();
        boolean called = false;
        try {
            XMLDoc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    new InputSource(new StringReader(indexConfig)));
            Boolean overWrite = new Boolean(XMLDoc.getElementsByTagName("IndexOverwrite").item(0).getTextContent());
            
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (TransformerFactoryConfigurationError ex) {
            ex.printStackTrace();
        }
        
        
        long start = System.currentTimeMillis();
        char[] tmp = new char[1024];
        size = 0;
        for(int i=0;i<len;i++){
            pdfFile = new File(dir+"/"+fileNames[i]);
            if(!pdfFile.exists()){
                pdfFile = new File(fileNames[i]);
            }
            if(!pdfFile.isDirectory() && !pdfFile.getName().startsWith(".")){
                converter = new DocConverter();
                converter.setCurrentOpp(converter.extractTextFromPdf);
                pipeReader = converter.getPipeReader();
                converter.setFile(pdfFile.getPath());
                t = new Thread(converter);
                t.start();
                
                int cnt=0;
                
                String content="";
                try{
                    while ((cnt = pipeReader.read(tmp)) != -1) {
                        content = content + new String(tmp,0,cnt);
                        size = size + cnt;
                        if(content.getBytes().length >= (5*1024*1024)){
//                            if(called){
//                                XMLDoc.getElementsByTagName("IndexOverwrite").item(0).setTextContent("false");
//                                Transformer serializer = TransformerFactory.newInstance().newTransformer();
//                                serializer.transform(new DOMSource(XMLDoc), new StreamResult(sw));
//                                indexConfig = sw.toString();
//                            }
                            if(!attach){
                                Object[] arg = {indexConfig,content,pdfFile.getName()};
                                indexedFiles = indexedFiles +","+ call(arg,"indexWithSOAP",endPoint);
                            }else{
                                Object[] arg = {indexConfig};
                                indexedFiles = indexedFiles +","+callWA(arg,"indexWithSwA",endPoint,content,pdfFile.getName());
                            }
                            called = true;
                            content ="";
                        }
                    }
                    if(content.getBytes().length > 1 ){
//                        if(called){
//                            XMLDoc.getElementsByTagName("IndexOverwrite").item(0).setTextContent("false");
//                            Transformer serializer = TransformerFactory.newInstance().newTransformer();
//                            serializer.transform(new DOMSource(XMLDoc), new StreamResult(sw));
//                            indexConfig = sw.toString();
//                        }
                        if(!attach){
                            Object[] arg = {indexConfig,content,pdfFile.getName()};
                            indexedFiles = indexedFiles +","+call(arg,"indexWithSOAP",endPoint);
                        }else{
                            Object[] arg = {indexConfig};
                            indexedFiles = indexedFiles +","+callWA(arg,"indexWithSwA",endPoint,content,pdfFile.getName());
                        }
                        called = true;
                        content = "";
                    }
                    
                    t.join();
                    
                }catch(IOException ex){
                    ex.printStackTrace();
                }catch (InterruptedException ex) {
                    ex.printStackTrace();
                } catch (TransformerFactoryConfigurationError ex) {
                    ex.printStackTrace();
                }
//                catch (TransformerException ex) {
//                    ex.printStackTrace();
//                }catch (TransformerConfigurationException ex) {
//                    ex.printStackTrace();
//                }
                
            }
            
        }
        return indexedFiles;
    }
    
    private String streamPDFContentWithTCP(String streamingConfig,String indexConfig,String endPoint,String dir){
        String streamServerConfPath = Utilities.createTemporaryFile(streamingConfig, "xml");
        String indexedFiles = "";
        File pdfDir = new File(dir);
        String[] fileNames=null;
        PipedReader pipeReader;
        Thread t=null;
        String content=null;
        if(pdfDir.isDirectory()){
            fileNames = pdfDir.list();
        }else{
            fileNames = dir.split(",");
        }
        Connector conn = initTransfer(streamServerConfPath,0,true);
        
        
        while(!init.getThread("Server").getState().equals(Thread.State.RUNNABLE)){
            if(init.getThread("Server").getState().equals(Thread.State.TERMINATED)){
                init.killServer();
                return "Problem Starting Server";
            }
        }
        
        String delimiter = "chunk";
        boolean breakXML = true;
        DocConverter converter;
        File pdfFile;
        int len = fileNames.length;
        
        Object[] arg = {streamingConfig,"pdf"};
        asyncCall(arg,"indexWithTCP",endPoint);
        
        try {
            OutputStream out = conn.getOutputStream();
            String header = "<?xml version=\"1.0\"?>"+"<stream>"+"<index_config>"+"<![CDATA["+indexConfig+"]]>"+"</index_config>";
            String fileNode;
            String startContentNode="<content>"+"<"+delimiter+"><![CDATA[";
            String endContentNode= "]]></"+delimiter+">"+"</content>";
            String breakNode =  "]]></"+delimiter+">"+"<"+delimiter+"><![CDATA[";
            out.write(header.getBytes());
            size = 0;
            char[] tmp = new char[1048576];
            for(int i=0;i<len;i++){
                pdfFile = new File(dir+"/"+fileNames[i]);
                if(!pdfFile.exists()){
                    pdfFile = new File(fileNames[i]);
                }
                if(!pdfFile.isDirectory() && !pdfFile.getName().startsWith(".")){
                    indexedFiles = indexedFiles + ","+ pdfFile.getName();
                    fileNode ="<fileName>"+pdfFile.getName()+"</fileName>";
                    out.write(fileNode.getBytes());
                    out.write(startContentNode.getBytes());
                    converter = new DocConverter();
                    converter.setCurrentOpp(converter.extractTextFromPdf);
                    pipeReader = converter.getPipeReader();
                    converter.setFile(pdfFile.getPath());
                    t = new Thread(converter);
                    t.start();
                    
                    int cnt=0;
                    
                    while ((cnt = pipeReader.read(tmp)) != -1) {
                        size = size + cnt;
                        content = new String(tmp,0,cnt);
                        out.write(content.getBytes());
                        if(breakXML && size%breakInterval==0){
                            out.write(breakNode.getBytes());
                        }
                    }
                    out.write(endContentNode.getBytes());
                }
                t.join();
            }
            out.write("</stream>".getBytes());
            conn.END();
        }catch(Exception ex){
            ex.printStackTrace();
            init.killServer();
        }finally{
            new File(streamServerConfPath).delete();
        }
        return indexedFiles;
    }
    
    private Connector initTransfer(String confiFile,int inc,boolean server){
        Connector conn = null;
        try {
            init = new Initilize(confiFile);
            init.setPort(init.getPort()+inc);
            if(server){
                init.server();
            }else{
                init.client();
            }
            conn = init.getConnector();
            if(server){
                init.startServer();
            }else{
                init.startClient();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return conn;
    }
    
    private String streamContentWithSOAP(String indexConfig,String endPoint,String dir,boolean attach){
        File storeDir = new File(dir);
        Document XMLDoc=null;
        StringWriter sw = new StringWriter();
        String indexedFiles=null;
        boolean called = false;
        StringBuffer buffer = new StringBuffer();
        
        try {
            XMLDoc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    new InputSource(new StringReader(indexConfig)));
            Boolean overWrite = new Boolean(XMLDoc.getElementsByTagName("IndexOverwrite").item(0).getTextContent());
            
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (TransformerFactoryConfigurationError ex) {
            ex.printStackTrace();
        }
        size = 0;
        java.io.BufferedReader in =null;
        try {
            in = new java.io.BufferedReader(new java.io.FileReader("/tmp/len"));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        PrintWriter out=null;
        try {
            if(storeDir.exists()){
                out = new PrintWriter(new BufferedWriter(new FileWriter(dir + "/content.txt")));
            }
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        int len=0;
        try {
            len = Integer.parseInt(in.readLine());
            in.close();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        for(int i=0;i<len;i++){
            size = size + getHomer().getBytes().length;
            if(storeDir.exists() && out!=null){
                out.println(getHomer());
            }else{
                //the indexer can't handle too mach data
                if(size%(10*1048576)==0 && size>0){
                    if(!attach){
                        Object[] arg = {indexConfig,buffer.toString(),"liveData"};
                        indexedFiles = call(arg,"indexWithSOAP",endPoint);
                    }else{
                        Object[] arg = {indexConfig};
                        indexedFiles = callWA(arg,"indexWithSwA",endPoint,buffer.toString(),"liveData");
                    }
                    buffer.setLength(0);
                }else{
                    buffer.append(getHomer());
                }
            }
        }
        if(buffer.toString().length()>=1){
            if(!attach){
                Object[] arg = {indexConfig,buffer.toString(),"liveData"};
                indexedFiles = call(arg,"indexWithSOAP",endPoint);
            }else{
                Object[] arg = {indexConfig};
                indexedFiles = callWA(arg,"indexWithSwA",endPoint,buffer.toString(),"liveData");
            }
            buffer.setLength(0);
        }
        return indexedFiles;
    }
    
    
    private String streamContentWithTCP(String streamingConfig,String indexConfig,String dir,String endPoint){
        String streamServerConfPath=null;
        String indexedFiles = "";
        File stoerDir = new File(dir);
        String[] fileNames=null;
        PrintWriter out =null;
        Connector conn = null;
        java.io.BufferedReader in =null;
        try {
            in = new java.io.BufferedReader(new java.io.FileReader("/tmp/len"));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        if(stoerDir.exists()){
            try {
                out = new PrintWriter(new BufferedWriter(new FileWriter(dir+"/content.txt")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }else{
            streamServerConfPath = Utilities.createTemporaryFile(streamingConfig, "xml");
            long serStart = System.currentTimeMillis();
            conn = initTransfer(streamServerConfPath,0,true);
            while(!init.getThread("Server").getState().equals(Thread.State.RUNNABLE)){
                if(init.getThread("Server").getState().equals(Thread.State.TERMINATED)){
                    init.killServer();
                    return "Problem Starting Server";
                }
            }
            long serEnd = System.currentTimeMillis();
            log.info("Streaming Server stated in "+(serEnd-serStart)+" msec");
            Object[] arg = {streamingConfig,"pdf"};
            asyncCall(arg,"indexWithTCP",endPoint);
        }
        
        
        String delimiter = "chunk";
        String header = "<?xml version=\"1.0\"?>"+"<stream>"+"<index_config>"+"<![CDATA["+indexConfig+"]]>"+"</index_config>";
        String startContentNode = "<content>"+"<"+delimiter+"><![CDATA[";
        String breakNode = "]]></"+delimiter+">"+"<content>"+"<"+delimiter+"><![CDATA[";
        String endNode = "]]></"+delimiter+">"+"</content></stream>";
        try {
            
            size = 0;
            int len =  Integer.parseInt(in.readLine());
            in.close();
            for(int i=0;i<len;i++){
                size = size + getHomer().getBytes().length;
                if(out!=null){
                    out.println(getHomer());
                }else if(conn!=null){
                    if(i==0){
                        conn.getOutputStream().write(header.getBytes());
                        conn.getOutputStream().write("<fileName>liveData</fileName>".getBytes());
                        conn.getOutputStream().write(startContentNode.getBytes());
                    }
                    conn.getOutputStream().write(getHomer().getBytes());
                    if(size%breakInterval==0){
                        conn.getOutputStream().write(breakNode.getBytes());
                    }
                    if(i>=len-1){
                        conn.getOutputStream().write(endNode.getBytes());
                    }
                }
            }
            if(conn!=null){
                conn.END();
            }
        }catch(Exception ex){
            ex.printStackTrace();
            if(conn!=null){
                init.killServer();
            }
        }finally{
            if(streamServerConfPath!=null){
                new File(streamServerConfPath).delete();
            }
        }
        return indexedFiles;
    }
    /**
     * Use the file indexconfig.xml to configure!
     */
    public static void main(String[] args) throws Exception {
        
        String configfile = "indexconfig.xml";
        
        if (args != null) {
            if (args.length == 1) {
                configfile = args[0];
            } else {
                System.err.println("Too few/many arguments, usage:\njava -jar Indexer.jar <configfile>\n");
                System.exit(-1);
            }
        }
        
        ConfigurationHandler ch = new ConfigurationHandler(configfile);
        Indexer i = new Indexer();
        String logmsg = i.indexFromCFG(configfile, null, null);
        
        if (log.isLoggable(Level.FINE)) {
            //log.info(ch.getCreator());
            //log.info(ch.getMaxBufferedDocs());
            //log.info(ch.getMergeFactor());
            //log.info(ch.getDataPath());
            //log.fine("-- Using Global Analyzer: " + ch.getGlobalAnalyzer());
            //log.info(ch.getFieldIndexValue("medline", "AU"));
            //log.info(ch.getFieldStoreValue("medline", "AU"));
            //log.info(ch.getTermVectorValue("medline", "AU"));
            //log.info(ch.getFieldDescription("medline", "AU"));
            //log.fine("-- Using Medline Analyzer: " + ch.getDocumentAnalyzer("medline"));
            //log.info("Extension:medline " + ch.getDocType("med"));
            log.fine("-- Defined document types in config file: " + ch.getDocumentTypes());
            log.info(logmsg);
        }
        //System.err.println(i.indexSRBData("username", "password", "medline/100.med"));
    }
    
    private String asyncCall(Object[] args,String method,String endpoint){
        Service service=null;
        Call call = null;
        AsyncCall  aCall=null;
        String result=null;
        IAsyncResult asyncResult=null;
        try{
            service = new Service();
            call = (Call)service.createCall();
            call.setTargetEndpointAddress(new URL(endpoint + "IndexWS"));
            call.setOperationName(new QName(method));
            
            aCall = new AsyncCall(call);
            
            asyncResult = aCall.invoke(args);
        }catch (ServiceException ex){
            ex.printStackTrace();
        }catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        return (String)asyncResult.getResponse();
    }
    
    private String call(Object[] args,String method,String endpoint){
        Service service=null;
        Call call = null;
        String result=null;
        try{
            service = new Service();
            call = (Call)service.createCall();
            call.setTargetEndpointAddress(new URL(endpoint + "IndexWS"));
            call.setOperationName(new QName(method));
            result= (String)call.invoke(args);
        }catch (ServiceException ex){
            ex.printStackTrace();
        }catch (MalformedURLException ex) {
            ex.printStackTrace();
        }catch (RemoteException ex) {
            ex.printStackTrace();
        }
        
        return result;
    }
    
    private String callWA(Object[] args,String method,String endpoint,String attachment,String contentID){
        Service service=null;
        Call call = null;
        String result=null;
        try{
            service = new Service();
            call = (Call)service.createCall();
            call.setTargetEndpointAddress(new URL(endpoint + "IndexWS"));
            call.setOperationName(new QName(method));
            AttachmentPart ap= new AttachmentPart(new DataHandler(attachment, "text/plain" ));
            ap.setContentId(contentID);
            call.addAttachmentPart(ap);
            
            result = (String)call.invoke(args);
        }catch (ServiceException ex){
            ex.printStackTrace();
        }catch (MalformedURLException ex) {
            ex.printStackTrace();
        }catch (RemoteException ex) {
            ex.printStackTrace();
        }
        
        return result;
    }
    
    private StreamConfig getConf(URI source,String protocol){
        StreamConfig conf = new StreamConfig();
        conf.displaySpeed = false;
        conf.protocol = protocol;
        
        if(protocol.equals("HTTP")){
            conf.fileRequest = source.getPath();
            conf.host = "http://"+source.getHost();
            conf.port = source.getPort();
        }else{
            conf.host = source.getHost();
            conf.port = 8199;
        }
        
        conf.loggLevel = "WARNING";
        
        conf.maxBufferSize = 8388608;
        conf.ecnryptData=false;
        conf.authenticate=false;
        conf.displaySpeed=false;
        conf.saveLogs=false;
        
        return conf;
    }
    
    private String getIP(){
       /* URL addr=null;
        try {
            addr = new URL((String) AxisEngine.getCurrentMessageContext().getProperty("transport.url"));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        return addr.getHost();*/
        String hostname = null;
        try {
            InetAddress addr = InetAddress.getLocalHost();

            // Get IP Address
            byte[] ipAddr = addr.getAddress();

            // Get hostname
            hostname = addr.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostname;
    }
    
    private static String getHomer(){
        return"Sing, O goddess, the anger of Achilles son of Peleus, " +
                "that brought countless ills upon the Achaeans. Many a brave soul " +
                "did it send hurrying down to Hades, and many a hero did it yield " +
                "a prey to dogs and vultures, for so were the counsels of Jove " +
                "fulfilled from the day on which the son of Atreus, king of men, " +
                "and great Achilles, first fell out with one another.And which of " +
                "the gods was it that set them on to quarrel? It was the son of Jove " +
                "and Leto; for he was angry with the king and sent a pestilence upon the " +
                "host to plague the people, because the son of Atreus had dishonoured Chryses " +
                "his priest. Now Chryses had come to the ships of the Achaeans to free his " +
                "daughter, and had brought with him a great ransom: moreover he bore in his " +
                "hand the sceptre of Apollo wreathed with a suppliant’s wreath and he besought " +
                "the Achaeans, but most of all the two sons of Atreus, who were their chiefs. "+
                "Sons of Atreus, he cried, and all other Achaeans, may the gods who dwell in Olympus " +
                "grant you to sack the city of Priam.";
    }
}
