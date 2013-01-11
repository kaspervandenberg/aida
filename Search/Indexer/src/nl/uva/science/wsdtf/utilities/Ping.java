/*
 * Ping.java
 *
 * Created on May 10, 2008, 3:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package nl.uva.science.wsdtf.utilities;

/**
 *
 * @author S. Koulouzis
 */
public class Ping {
    
    /** Creates a new instance of Ping */
    public  Ping(String ip) {
        String pingResult = "";
        
        String pingCmd = "ping -c 2" + ip;
        
        try {
            Runtime r = Runtime.getRuntime();
            Process p = r.exec(pingCmd);
            
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
                pingResult += inputLine;
            }
            in.close();
            
        }//try
        catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }
    
}
