/*
 *
 */
package nl.uva.science.wsdtf.Tests;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import nl.uva.science.wsdtf.utilities.Constants;
import nl.uva.science.wsdtf.utilities.Initilize;
import nl.uva.science.wsdtf.utilities.Ping;

/**
 * The Class Test.
 */
public class Test {
    
    /**
     * The main method.
     *
     * @param args
     *            the args
     */
    public static void main(String[] args) {
        
        if(!(new File("logs/")).exists()){
            if((new File("logs/")).mkdir()){
            }
        }
        
        
        Constants.logPath = "logs/";
        Initilize init=null;
        int protocol = Constants.TCP;
        Constants.setMaxBufferSize((int)Constants.MB*7);
        int port = 8199;
        boolean authenticate=false;
        
        if (args[0].equals("ping")) {
            Ping p = new Ping(args[1]);
        }
        if (args[0].equals("wget")) {
            TestHttp http = new TestHttp(args[1]);
            http.wget();
        }
        if(args[0].equals("GFtp")){
            TestGFTP t = new TestGFTP(args[1]);
            t.getFIle(args[2]);
        }
        
        if(args[0].equals("conf")){
            TestConf t = new TestConf();
            t.createConfig();
            
            t.crateConfFile();
        }
    }
    
    
    private static String[] getFilesName(String dir) {
        File[] files = new File(dir).listFiles();
        String[] paths = new String[files.length];
        for (int i = 0; i < paths.length; i++) {
            paths[i] = files[i].getPath();
        }
        return paths;
    }
    
}