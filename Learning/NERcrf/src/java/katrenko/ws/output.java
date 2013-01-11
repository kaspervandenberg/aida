/*
 * output.java
 *
 * Created on п'€тниц€, 8, лютого 2008, 13:34
 */

package katrenko.ws;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author Admin
 * @version
 */
public class output extends HttpServlet {
    
    private static final String CONTENT_TYPE = "text/html";

    public void init(ServletConfig config) throws ServletException
    {
    super.init(config);
    }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        doGet(request, response);
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        String aString = (String)request.getAttribute("outp");
        //out.println(aString);
        out.println("<TEXTAREA name=\"test_file\" cols=\"60\" rows=\"16\">" + aString + "</TEXTAREA><br/><br/>");           
        //out.close();
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
