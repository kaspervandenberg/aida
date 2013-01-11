/*
 * GetTagsSVL.java
 *
 * Created on March 27, 2006, 4:50 PM
 */

package org.vle.aid.bicad.client;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.util.Map;
import java.util.regex.*;

/**
 *
 * @author wrvhage
 * @version
 */
public class GetTagsSVL extends HttpServlet {
    
    private String _lookupns(String ns) {
        if (ns.startsWith("http://www.w3.org/2004/02/skos")) return "skos";
        else if (ns.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns")) return "rdf";
        else if (ns.startsWith("http://www.w3.org/2000/01/rdf-schema")) return "rdfs";
        else if (ns.startsWith("http://www.w3.org/2002/07/owl")) return "owl";
        else if (ns.startsWith("http://www.w3.org/2001/XMLSchema")) return "xs";
        else if (ns.startsWith("http://purl.org/dc/elements")) return "dc";
        else if (ns.startsWith("http://xmlns.com/foaf")) return "foaf";
        else if (ns.startsWith("http://agclass.nal.usda.gov/")) return "nal";
        else if (ns.startsWith("http://www.fao.org/")) return "fao";
        else if (ns.startsWith("http://www.vl-e.nl/aid")) return "aid";
        else if (ns.startsWith("http://www.tno.nl/ns/bicad") ||
                ns.startsWith("http://www.few.vu.nl/~wrvhage/ns/bicad")) return "tno";
        return ns;
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
        
        boolean matchAnywhere = false;
        if (param.get("matchAnywhere") != null && ((String[])param.get("matchAnywhere"))[0].equalsIgnoreCase("true")) matchAnywhere = true;
        boolean ignoreCase = true;
        if (param.get("ignoreCase") != null && ((String[])param.get("ignoreCase"))[0].equalsIgnoreCase("false")) ignoreCase = false;
        int limit = 5;
        if (param.get("count") != null) {
            limit = Integer.parseInt(((String[])param.get("count"))[0]);
        }
        String query_type = ((String[])param.get("query_type"))[0];
        
        String id = ((String[])param.get("id"))[0];
        String server_url = ((String[])param.get("server_url"))[0];
        String repository = ((String[])param.get("repository"))[0];
        String username = ((String[])param.get("username"))[0];
        String password = ((String[])param.get("password"))[0];
        
        String model = ((String[])param.get("model"))[0];
        String session = ((String[])param.get("session"))[0];

        String property = null;
        String concept = null;
        if (query_type.equals("property")) property = ((String[])param.get("query"))[0];
        if (query_type.equals("concept")) concept = ((String[])param.get("query"))[0];
        
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<ajax-response><response type='object' id='" + id + "_updater'><matches>");
        
        BQ_client bqc = new BQ_client();
        String[][] table = bqc.getTags(server_url,repository,username,password,model,session,property,concept,
                                       ignoreCase,matchAnywhere,new Integer(limit));
        for (int i=0;i<table.length&&i<limit;i++) {
            String text = table[i][1];
            String value = table[i][0];
            String comment = _lookupns(table[i][2]);
            out.println("<entry>" +
                    "<text>" + text + "</text>" +
                    "<value>" + value + "</value>" +
                    "<comment>" + comment + "</comment>" +
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
