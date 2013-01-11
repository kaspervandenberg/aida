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
public class RepositorySelectQueryTableSVL extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        Map param = request.getParameterMap();
        boolean html = (param.get("html") != null);
        boolean xml = (param.get("xml") != null);
     
        if (html) response.setContentType("text/html;charset=UTF-8");
        else if (xml) response.setContentType("text/xml;charset=UTF-8");
        else response.setContentType("text/plain;charset=UTF-8");
        
        if (html) out.println("<html><body>");
        if (xml) out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<result>");
        
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
            call.setOperationName("selectQuery");
            String[][] table = (String[][]) call.invoke( new Object[] { 
                    ((String[])param.get("server_url"))[0], ((String[])param.get("repository"))[0],
                    ((String[])param.get("username"))[0], ((String[])param.get("password"))[0],
                    ((String[])param.get("query_language"))[0],((String[])param.get("query"))[0]
                } );

            if (table != null) {
                if (html) {
                    out.println("<h2>returned</h2>");
                    out.println("<table borders=1>");
                }
                
                for (int i=0;i<table.length;i++) {
                    if (html) out.println("<tr>");
                    else if (xml) out.println("<row>");
                    for (int j=0;j<table[i].length;j++) {
                        if (xml) out.println("<col>" + table[i][j] + "</col>");
                        else if (html) {
                            if (table[i][j] == null) out.println("<td>null</td>");
                            else out.println("<td>" + table[i][j] + "</td>");
                        } else {
                            if (table[i][j] != null) out.println(table[i][j]);
                            if (j < table[i].length-1) out.println(",");
                        }
                    }
                    if (html) out.println("</tr>");
                    else if (xml) out.println("</row>");
                    else out.println(";");
                }
                if (html) out.println("</table>");             
            }
         } catch (Exception e) {
             out.println("<b>" + e.toString() + "</b><br/><br/>");
             for (int i=0;i<e.getStackTrace().length;i++) {
                 out.println(e.getStackTrace()[i].toString() + "<br/>");
             }
         }
        if (html) out.println("</body></html>");
        else if (xml) out.println("</result>");
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
