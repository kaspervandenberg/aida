/*
 * AutoCompletionSVL.java
 *
 * Created on March 22, 2006, 1:23 PM
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
public class AutoCompletionSVL extends HttpServlet {
    
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
        String query_language = "serql";
        String query_type = ((String[])param.get("query_type"))[0];
        
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<ajax-response><response type='object' id='" + id + "_updater'><matches>");
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
            
            String query = null;
            if (query_type.equalsIgnoreCase("concept")) {
                query = "select distinct C, L, namespace(C) from {C} P {L} where L like \"" +
                        matchAnywhere + ((String[])param.get("query"))[0] + "*\"" + ignoreCase +
                        " and ( P = skos:prefLabel or P = rdfs:label ) " + count +
                        " using namespace skos = <http://www.w3.org/2004/02/skos/core#>";
            } else if (query_type.equalsIgnoreCase("property")) {
                query = "select distinct P, localName(P), namespace(P) from {P} rdf:type {rdf:Property} " +
                        "where localName(P) like \"" +
                        matchAnywhere + ((String[])param.get("query"))[0] + "*\"" + ignoreCase + count;
            }  
            String[][] table = null;
            table = (String[][]) call.invoke( new Object[] {
                server_url,repository,username,password,query_language,query
            } );
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
