/*
 * ThesaurusRepositorySVL.java
 *
 * Created on March 7, 2006, 2:39 PM
 */

package org.vle.aid.metadata.client;

import java.io.*;
import java.net.*;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import org.vle.aid.common.Properties;

/**
 *
 * @author wrvhage
 * @version
 */
public class ThesaurusRepositorySVL extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map param = request.getParameterMap();
        boolean html = (param.get("html") != null);
        if (html) out.println("<html><body>");
        try {
            String endpoint = null;
            if (param.get("axis_url") != null) {
                endpoint = ((String[])param.get("axis_url"))[0] + "/services/ThesaurusRepositoryWS";
            } else {
                endpoint =  Properties.Entries.AXIS_ENDPOINT.get() + "/services/ThesaurusRepositoryWS";
            }
  
            Service service = new Service();
            Call call = (Call) service.createCall();
  
            call.setTargetEndpointAddress(endpoint);
            call.setOperationName(((String[])param.get("operation"))[0]);
              
            String[] rv = null;
            boolean rvb = false;
            if (param.get("term") != null && ((String[])param.get("term")) != null) {
                rv = (String[]) call.invoke( new Object[] {
                    ((String[])param.get("server_url"))[0], ((String[])param.get("repository"))[0],
                    ((String[])param.get("username"))[0], ((String[])param.get("password"))[0],
                    ((String[])param.get("term"))[0]
                } );
            } else if (param.get("url") != null && ((String[])param.get("url")) != null) {
                rv = (String[]) call.invoke( new Object[] {
                    ((String[])param.get("server_url"))[0], ((String[])param.get("repository"))[0],
                    ((String[])param.get("username"))[0], ((String[])param.get("password"))[0],
                    ((String[])param.get("url"))[0]
                } );
            } else if (param.get("subject") != null && ((String[])param.get("subject")) != null &&
                       param.get("object") != null && ((String[])param.get("object")) != null) {                
                rvb = ((Boolean) call.invoke( new Object[] {
                    ((String[])param.get("server_url"))[0], ((String[])param.get("repository"))[0],
                    ((String[])param.get("username"))[0], ((String[])param.get("password"))[0],
                    ((String[])param.get("subject"))[0], ((String[])param.get("object"))[0]
                } )).booleanValue();
            } else {
                 rv = (String[]) call.invoke( new Object[] {
                    ((String[])param.get("server_url"))[0], ((String[])param.get("repository"))[0],
                    ((String[])param.get("username"))[0], ((String[])param.get("password"))[0]
                } );
            }
            if (html) out.println("<h2>returned</h2>");
            if (rv != null) {
                for (int i=0;i<rv.length;i++) {
                    out.println(rv[i]);
                    if (html) out.println("<br/>");
                }
            } else {
                out.println(rvb);
                if (html) out.println("<br/>");
            }
             
         } catch (Exception e) {
             if (html) out.println("<b>");
             out.println(e.toString());
             if (html) out.println("</b><br/><br/>");
             for (int i=0;i<e.getStackTrace().length;i++) {
                 out.println(e.getStackTrace()[i].toString());
                 if (html) out.println("<br/>");
             }
         }
        if (html) out.println("</body></html>");
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
