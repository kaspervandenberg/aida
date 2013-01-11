/*
 * RepositoryAddRdfSVL.java
 *
 * Created on January 30, 2006, 1:07 PM
 */

package org.vle.aid.metadata.client;

import java.io.*;
import java.net.*;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 *
 * @author wrvhage
 * @version
 */
public class RepositoryClearSVL extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map param = request.getParameterMap();
        out.println("<html><body>");
        try {
            String endpoint = null;
            if (param.get("axis_url") != null) {
                endpoint = ((String[])param.get("axis_url"))[0] + "/services/RepositoryWS";
            } else {
                endpoint = "http://localhost:8080/axis/services/RepositoryWS";
            }
  
            Service service = new Service();
            Call call = (Call) service.createCall();
  
            call.setTargetEndpointAddress(endpoint);
            call.setOperationName("clear");
              
            Boolean b = (Boolean) call.invoke( new Object[] { 
                    ((String[])param.get("server_url"))[0], ((String[])param.get("repository"))[0],
                    ((String[])param.get("username"))[0], ((String[])param.get("password"))[0]
                } );
            out.println("<h2>returned</h2>" + b.toString());
             
         } catch (Exception e) {
             out.println("<b>" + e.toString() + "</b><br/><br/>");
             for (int i=0;i<e.getStackTrace().length;i++) {
                 out.println(e.getStackTrace()[i].toString() + "<br/>");
             }
         }
        out.println("</body></html>");
        out.close();
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
