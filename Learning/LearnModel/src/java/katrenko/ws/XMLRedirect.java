/*
 * XMLRedirect.java
 *
 * Created on 31 March 2006, 18:27
 */

package katrenko.ws;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * XMLRedirect is used by LearnModelServlet to output XML data
 * <p>
 * @author Sophia Katrenko
 * @version 1.0
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
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
     response.setContentType(CONTENT_TYPE);
     PrintWriter out = response.getWriter();
     String aString = (String)request.getAttribute("output");
     out.println(aString);
     out.close();
}
    
}