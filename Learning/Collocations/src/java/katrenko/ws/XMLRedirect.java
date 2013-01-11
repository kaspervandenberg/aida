/*
 * XMLRedirect.java
 *
 * Created on March 23, 2006, 6:20 PM
 */
package katrenko.ws;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author software
 * @version
 */
public class XMLRedirect extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
     private static final String CONTENT_TYPE = "text/xml";
/**Initialize global variables*/
public void init(ServletConfig config) throws ServletException
{
super.init(config);
}

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
  
   
  
}
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        doGet(request,  response);
       // out.close();
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
     response.setContentType(CONTENT_TYPE);
     PrintWriter out = response.getWriter();
 //    response.getAttribute("output");
     String aString = (String)request.getAttribute("output");
       
   /* String contentType = req.getContentType();
    if (contentType == null) 
      return; */
 /*   BufferedReader br = new BufferedReader(request.getReader());

    String line = null;
    while ((line = br.readLine()) != null) {
      int index;
      out.println(line); 
    }
    br.close();*/
     
      out.println(aString);
      out.close();
}
    
}