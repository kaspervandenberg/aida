/*
 * synonymClient.java
 *
 *
 * Need to update 'String endpoint'
 *
 */

package org.vle.aid.client;

import java.io.IOException;
import java.rmi.RemoteException;
import javax.servlet.ServletException;
import javax.xml.rpc.ServiceException;

import java.io.PrintWriter;
import java.io.*;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import org.apache.lucene.index.IndexReader;

import org.vle.aid.common.Properties;

/**
 *
 * @author Edgar Meij
 */
    
public final class searchClient extends HttpServlet {
    
    Service     service;
    Call        call;
    String      endpoint =  Properties.Entries.AXIS_ENDPOINT.get() + "/services/";        
    IndexReader reader = null;
    
    /** logger for Commons logging. */
    private transient Logger log =
            Logger.getLogger(searchClient.class.getName());    
    
    /**
     * Respond to a GET request for the content produced by
     * this servlet.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are producing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
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
    
    public void processRequest(HttpServletRequest request,
        HttpServletResponse response)
            throws IOException, ServletException {
        
        String index = request.getParameter("index");
        String query = request.getParameter("query");
        String hits = request.getParameter("maxHits");
        String field = request.getParameter("field");
        String button = request.getParameter("button");
        String[] fieldList = {"Error"};
        String retXML;
        
        PrintWriter out = response.getWriter();
        service = new Service();
        try {
            call    = (Call) service.createCall();     
        } catch (ServiceException e) {
            log.severe(e.toString());
        }
                
        if ( button != null ) {
            response.setContentType("text/xml;charset=UTF-8");
            
            try {              
                call.setTargetEndpointAddress(endpoint + "SearcherWS");
                call.setOperationName("search");
                retXML = (String) call.invoke( new Object[] { index, query, hits, field } );
                out.println(retXML);
                
            } catch (RemoteException e) {
                log.severe("RemoteException: " + e.toString());
                
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
   
                log.severe(sw.toString());
            }
            
        } else {
            
            File indexDir = new File("/scratch/emeij/indexes/");
            File listIndexes[] = indexDir.listFiles();
            reader = IndexReader.open("/scratch/emeij/indexes/" + "baseline");
            
            response.setContentType("text/html;charset=UTF-8");

            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet LuceneServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>LuceneServletAdvanced</h1>");
            out.println("<form name=\"QCK\" method=\"get\">");
            out.println("index:");            
            out.println("<br>");
            out.println("<select name=\"index\" onchange=\"QCK.submit()\">");
            out.println("<option value=\"baseline\">" +
                    "Choose an index" + 
                    "</option>");
            for (int k = 0; k < listIndexes.length; ++k) {

                // TODO: static boolean indexExists(Directory directory)
                // Returns true if an index exists at the specified directory.
                if (listIndexes[k].isDirectory()) {
                
                    if (index == null || !index.equals(listIndexes[k].getName())) {
                        out.println("<option>" + listIndexes[k].getName() + "</option>");
                    } else {
                        out.println("<option SELECTED>" + listIndexes[k].getName() + "</option>");
                    }
                }
            }

            out.println("</select>");
            out.println("<br><br>");
            
            try {
            
                out.println("Search field:<br>");
                out.println("<select name=\"field\">");
                
                if (index != null) {
                    
                try {
                call.setTargetEndpointAddress(endpoint + "getFields");
                call.setOperationName("listFields");     
                fieldList = (String[]) call.invoke( new Object[] { index } );

            } catch (Exception e) {
                log.info(e.toString());
            }

                    
            for (int k = 1; k < fieldList.length; ++k) {
                out.println("<option>" + fieldList[k] + "</option>");
                // out.println("<b>" + fieldList[k] + "</b>, ");
                 }
            }
                
                out.println("</select>");
                out.println("<br><br>");
                
            } catch(Exception e) {
                log.severe("LuceneClientServletAdvancedError: " + e.getMessage());
                
            }
            
            out.println("query:");
            out.println("<br>");            
            out.println("<input type=\"text\" name=\"query\" size=\"25\" value=\"\">");
            out.println("<br><br>");
            
            out.println("maxHits:");
            out.println("<br>");            
            out.println("<input type=\"text\" name=\"maxHits\" size=\"5\" value=\"10\">");
            out.println("<br><br>");            

            out.println("<input type=\"submit\" value=\"Submit\" name=\"button\">");
            out.println("</form>");
            out.println("</body>");
            out.println("</html>");        
        
        
        }
    }    
}
