/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.science.wsdtf.protocols;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.OutputStreamWriter;
import java.io.IOException;

/**
 *
 * @author alogo
 */
public class HTTPStreamer extends HttpServlet{
    
   public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            int len = req.getContentLength();
            byte[] input = new byte[len];
            ServletInputStream sin = req.getInputStream();
            
            OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream());
            
            int c, count = 0 ;
            while ((c = sin.read(input, count, input.length-count)) != -1) {
                count +=c;
                String inString = new String(input);
                writer.write(inString);
            }
            sin.close();
        
           
//            resp.setStatus(HttpServletResponse.SC_OK);
            
          
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }  
}
