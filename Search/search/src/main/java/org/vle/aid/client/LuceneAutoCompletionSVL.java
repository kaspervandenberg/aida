/*
 * LuceneAutoCompletionSVL.java
 *
 * Created on March 22, 2006, 1:23 PM
 */

package org.vle.aid.client;

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
public class LuceneAutoCompletionSVL extends HttpServlet {
    
    /**
     * Substitutes some special characters and returns nicer HTML characters
     * Currently:
     * "&"
     * "<"
     * ">"
     * @param text String of text to be escaped
     * @return a String with replaced characters
    */
    private static String escape(String text) {
        text = text.replaceAll("&", "&amp;");
        text = text.replaceAll("<", "&lt;");
        text = text.replaceAll(">", "&gt;");
        return text;
    }    
    
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
        String count = "50";
        if (param.get("count") != null) {
            count = ((String[])param.get("count"))[0];
        }
        
        String id = ((String[])param.get("id"))[0];
        String index = ((String[])param.get("index"))[0];
        String field = ((String[])param.get("field"))[0];
        String prefix = ((String[])param.get("property"))[0];
        
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<ajax-response><response type='object' id='" + id + "_updater'><matches>");
        try {
            String endpoint = "http://localhost:8080/axis/services/termFinderWS";
            
            Service service = new Service();
            Call call = (Call) service.createCall();
            
            call.setTargetEndpointAddress(endpoint);
            call.setOperationName("getTerms"); 
            
            String[][] table = (String[][]) call.invoke( new Object[] {
                index, field, prefix, count
            } ); 
            
            for (int i=0;i<table.length&&i<Integer.parseInt(count);i++) {
                String text = table[i][0];
                String value = table[i][1];
                String comment = table[i][2];
                out.println("<entry>" +
                        "<text>" + escape(text) + "</text>" +
                        "<value>" + escape(value) + "</value>" +
                        "<comment>" + escape(comment) + " hits</comment>" +
                        "</entry>");
            }
            
        } catch (Exception e) {
            out.println(e.toString());
            for (int i=0;i<e.getStackTrace().length;i++) {
                out.println(e.getStackTrace()[i].toString());
            }
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
