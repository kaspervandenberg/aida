/*
 * GetTagLocationsSVL.java
 *
 * Created on March 27, 2006, 4:54 PM
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
public class GetTagLocationsSVL extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map param = request.getParameterMap();
        
        String matchAnywhere = "";
        if (param.get("matchAnywhere") != null && ((String[])param.get("matchAnywhere"))[0].equalsIgnoreCase("true")) matchAnywhere = "*";
        String ignoreCase = " ignore case ";
        if (param.get("ignoreCase") != null && ((String[])param.get("ignoreCase"))[0].equalsIgnoreCase("true")) ignoreCase = "";
        int limit = 5;
        String count = " limit 5";
        if (param.get("count") != null) {
            limit = Integer.parseInt(((String[])param.get("count"))[0]);
            count = " limit " + ((String[])param.get("count"))[0];
        }
        
        String id = ((String[])param.get("id"))[0];
        String server_url = ((String[])param.get("server_url"))[0];
        String repository = ((String[])param.get("repository"))[0];
        String username = ((String[])param.get("username"))[0];
        String password = ((String[])param.get("password"))[0];       

        String model = ((String[])param.get("model"))[0];
        String session = ((String[])param.get("session"))[0];
        
        String property = null;
        if (param.get("property") != null) property = ((String[])param.get("property"))[0];
        String concept = null; 
        if (param.get("concept") != null) concept = ((String[])param.get("concept"))[0];
        
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<ajax-response><response type='object' id='" + id + "_updater'><matches>");

        BQ_client bqc = new BQ_client();
        String[][] table = bqc.getTagLocations(server_url,repository,username,password,model,session,property,concept);
        System.out.println("locs: " + table.length);
        for (int i=0;i<table.length;i++) {            
                String doc = table[i][0];
                String loc = table[i][1];
                out.println("<entry>" +
                        "<text>" + doc + "</text>" +
                        "<value>" + doc + "#" + loc + "</value>" +
                        "<comment>" + loc + "</comment>" +
                        "</entry>");
        }
        out.println("</matches></response></ajax-response>");
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
