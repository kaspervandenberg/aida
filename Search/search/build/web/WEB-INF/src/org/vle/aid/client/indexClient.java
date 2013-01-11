/*
 * synonymClient.java
 *
 *
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

/**
 *
 * @author Edgar Meij
 */
    
public final class indexClient extends HttpServlet {
    
    Service     service;
    Call        call;
    String      endpoint = "http://localhost/axis/services/";        
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
        String config = request.getParameter("config").replaceAll("%2F","/");
        String src = request.getParameter("src").replaceAll("%2F","/");
        log.info("SRC: " + src);
        String button = request.getParameter("button");
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
                call.setTargetEndpointAddress(endpoint + "IndexWS");
                call.setOperationName("indexFromCFG");
                retXML = (String) call.invoke( new Object[] { config, index, src } );
                out.println(retXML);
                
            } catch (RemoteException e) {
                log.severe("RemoteException: " + e.toString());
                
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
   
                log.severe(sw.toString());
            }
            
        } else {
            
//            File indexDir = new File("/scratch/emeij/indexes/");
//            File listIndexes[] = indexDir.listFiles();
//            reader = IndexReader.open("/scratch/emeij/indexes/" + "baseline");
            
            response.setContentType("text/html;charset=UTF-8");

            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet IndexServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>LuceneIndexer</h1>");
            out.println("<form name=\"QCK\" method=\"get\">");
            out.println("index:");            

            out.println("<br><br>");
                
            
            out.println("index:");
            out.println("<br>");            
            out.println("<input type=\"text\" name=\"index\" size=\"25\" value=\"\">");
            out.println("<br><br>");
            
            out.println("config:");
            out.println("<br>");            
            out.println("<input type=\"text\" name=\"config\" size=\"25\" value=\"\">");
            out.println("<br><br>");
            
            out.println("src:");
            out.println("<br>");            
            out.println("<input type=\"text\" name=\"src\" size=\"25\" value=\"10\">");
            out.println("<br><br>");            

            out.println("<input type=\"submit\" value=\"Submit\" name=\"button\">");
            out.println("</form>");
            out.println("</body>");
            out.println("</html>");        
        
        
        }
    }    
}
