/*
 * ChangeLogger.java
 *
 * Created on January 5, 2007, 11:39 PM
 */

package org.vle.aid.metadata.client;

import java.io.*;
import java.net.*;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author wrvhage
 * @version
 */
public class ChangeLogger extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map param = request.getParameterMap();

        Logger logger = Logger.getLogger(ChangeLogger.class);

        String prefix = getServletContext().getRealPath("/");
        PropertyConfigurator.configureAndWatch(prefix + "WEB-INF/ChangeLogger.properties");

        String what = ((String[])param.get("what"))[0];
        logger.info(what);
        out.print("<reply>true</reply>");
    }
    
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
}
