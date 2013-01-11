/*
 * AddTagSVL.java
 *
 * Created on March 27, 2006, 4:32 PM
 */

package org.vle.aid.bicad.client;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.util.Map;

/**
 *
 * @author wrvhage
 * @version
 */
public class AddTagSVL extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map param = request.getParameterMap();
        
        String server_url = ((String[])param.get("server_url"))[0];
        String repository = ((String[])param.get("repository"))[0];
        String username = ((String[])param.get("username"))[0];
        String password = ((String[])param.get("password"))[0];       

        String model = ((String[])param.get("model"))[0];
        String session = ((String[])param.get("session"))[0];
        
        String document = ((String[])param.get("document"))[0];
        String location = ((String[])param.get("location"))[0];
        String property = ((String[])param.get("property"))[0];
        String concept = ((String[])param.get("concept"))[0];                
        
        BQ_client bqc = new BQ_client();
        boolean b = bqc.addTag(server_url,repository,username,password,model,session,document,location,property,concept);
        out.println("<html><body><h2>returned</h2>" + Boolean.toString(b) + "</body></html>");
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
