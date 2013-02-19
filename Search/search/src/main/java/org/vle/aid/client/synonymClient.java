/*
 * synonymClient.java
 *
 * TODO: Need to update 'String endpoint'
 * TODO: Remove IndexLocation
 *
 */

package org.vle.aid.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.*;

import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Edgar Meij
 */
   

public final class synonymClient extends HttpServlet {

    Service     service;
    Call        call;
    String          indexLocation = System.getenv("INDEXDIR") + 
            System.getProperty("file.separator");
    String      endpoint = "http://localhost:8080/axis/services/synonym";        
    
    /** logger for Commons logging. */
    private transient Logger log =
	 Logger.getLogger("synonymClient.class.getName()");
         
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
        
        String[] retArray    = {""};
        String   ret         = "";    
        String[] methodArray = {
            "getSynonyms",
            "listSynonyms",
            "listSynonymsXML",
            "updateSynonyms",
            "addSynonym",
            "deleteSynonym"
        };
        
        String root     = request.getContextPath();
        String index    = request.getParameter("index");
        String term     = request.getParameter("term");
        String synonym  = request.getParameter("synonym");
        String button   = request.getParameter("button");
        String method   = request.getParameter("method");
        
        if (term == null)
            term = "";
        if (synonym == null)
            synonym = "";
        
        // Call XML service, special case
        if (button != null && method != null && method.equalsIgnoreCase("listSynonymsXML")) {
            
            response.setContentType("text/xml;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            
            try {
                service = new Service();
                call    = (Call) service.createCall();

                call.setTargetEndpointAddress(endpoint);
                call.setOperationName(method);

                ret = (String) call.invoke( new Object[] { index } );
                writer.println(ret);
                
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.close();
                log.severe(sw.toString());
            }            
            
        } else { // Print entry page
            log.fine("Checking for env. var. INDEXDIR");

            if (indexLocation == null) {
                log.severe("***INDEXDIR not found!!!***");
                indexLocation = "";
            } else {
                log.fine("Found INDEXDIR: " + indexLocation);
            } 
        
            File indexDir = new File(indexLocation);
            File listIndexes[] = indexDir.listFiles();
            
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter writer = response.getWriter();

            writer.println("<html>");
            writer.println("<head>");
            writer.println("<title>Synonym Webservice</title>");
            writer.println("</head>");
            writer.println("<body bgcolor=white>");
            writer.println("<table border=\"0\">");
            writer.println("<tr>");
            writer.println("<td>");
            writer.println("<img src=\"" + root + "/images/vlelogo.png\">");
            writer.println("</td>");
            writer.println("<td>");
            writer.println("<h1>Synonym Webservice</h1>");
            writer.println("</td>");
            writer.println("</tr>");
            writer.println("</table>");
            
            writer.println("<table border=\"0\" width=\"100%\">");
            writer.println("<form name=\"SynonymClientForm\" method=\"get\">");
            writer.println("<tr>");
            writer.println("  <td>Index:<br/>");
            writer.println("    <select name=\"index\">");
            writer.println("      <option value=\"QCK\"" + 
                    "> Choose an index" + "</option>");
            
            for (int k = 0; k < listIndexes.length; ++k) {
				Directory dir = FSDirectory.open(listIndexes[k]);
                if (DirectoryReader.indexExists(dir)) {
                    if (index == null || !index.equals(listIndexes[k].getName())) {
                        writer.println("      <option id=index\""+k+"\"><label for=\"index" + k + "\">" + listIndexes[k].getName() + "</label></option>");
                    } else {
                        writer.println("      <option SELECTED>" + listIndexes[k].getName() + "</option>");
                    }
                }
            }            
            
            writer.println("    </select>");
            writer.println("  </td>");
            writer.println("</tr>");

            writer.println("<tr>");
            writer.println("  <td>");            
            writer.println("action:<br>");
            writer.println("    <select name=\"method\" onchange=\"SynonymClientForm.submit()\">");
            writer.println("<option value=\"listSynonymsXML\">" +
                    "Choose an action" + 
                    "</option>");
            
            for (int k = 0; k < methodArray.length; ++k) {
                
                if (method == null || !method.equals(methodArray[k])) {
                    writer.println("<option>" + methodArray[k] + "</option>");
                } else {
                    writer.println("<option SELECTED>" + methodArray[k] + "</option>");
                }
            }
            
            writer.println("    </select>");
            writer.println("  </td>");
            writer.println("</tr>");
            
            writer.println("<tr>");
            writer.println("  <td>");
            writer.println("Term:");
            writer.println("<br>");
            
            if (method == null) {
                writer.println("<input type=\"text\" name=\"term\" " +
                        "size=\"30\" value=\"\" DISABLED>");
            } else if (method.equalsIgnoreCase("addSynonym")) {
                writer.println("<input type=\"text\" name=\"term\" " +
                        "size=\"30\" value=\"" + term + "\">");                       
            } else if (method.equalsIgnoreCase("deleteSynonym")) {
                writer.println("<input type=\"text\" name=\"term\" " +
                        "size=\"30\" value=\"" + term + "\">");
            } else if (method.equalsIgnoreCase("updateSynonyms")) {
                writer.println("<input type=\"text\" name=\"term\" " +
                        "size=\"30\" value=\"\" DISABLED>");
            } else if (method.equalsIgnoreCase("listSynonyms")) {
                writer.println("<input type=\"text\" name=\"term\" " +
                        "size=\"30\" value=\"\" DISABLED>");
            } else if (method.equalsIgnoreCase("getSynonyms")) {
                writer.println("<input type=\"text\" name=\"term\" " +
                        "size=\"30\" value=\"" + term + "\">");
            } else if (method.equalsIgnoreCase("listSynonymsXML")) {
                writer.println("<input type=\"text\" name=\"term\" " +
                        "size=\"30\" value=\"\" DISABLED>");
            }
                        
            writer.println("  </td>");
            writer.println("</tr>");            
            
            writer.println("<tr>");
            writer.println("  <td>");
            writer.println("Synonym:");
            writer.println("<br>");

            if (method == null) {
                writer.println("<input type=\"text\" name=\"synonym\" " +
                        "size=\"30\" value=\"\" DISABLED>");
            } else if (method.equalsIgnoreCase("addSynonym")) {
                writer.println("<input type=\"text\" name=\"synonym\" " +
                        "size=\"30\" value=\"" + synonym + "\">");                       
            } else if (method.equalsIgnoreCase("deleteSynonym")) {
                writer.println("<input type=\"text\" name=\"synonym\" " +
                        "size=\"30\" value=\"\" DISABLED>");
            } else if (method.equalsIgnoreCase("updateSynonyms")) {
                writer.println("<input type=\"text\" name=\"synonym\" " +
                        "size=\"30\" value=\"\" DISABLED>");
            } else if (method.equalsIgnoreCase("listSynonyms")) {
                writer.println("<input type=\"text\" name=\"synonym\" " +
                        "size=\"30\" value=\"\" DISABLED>");
            } else if (method.equalsIgnoreCase("getSynonyms")) {
                writer.println("<input type=\"text\" name=\"synonym\" " +
                        "size=\"30\" value=\"\" DISABLED>");
            } else if (method.equalsIgnoreCase("listSynonymsXML")) {
                writer.println("<input type=\"text\" name=\"synonym\" " +
                        "size=\"30\" value=\"\" DISABLED>");
            }
            
            writer.println("  </td>");
            writer.println("</tr>"); 
            
            writer.println("<tr>");
            writer.println("  <td>");
            writer.println("<input type=\"submit\" value=\"Submit\" name=\"button\">");
            writer.println("  </td>");
            writer.println("</tr>");            
            
            writer.println("</form>");            
            writer.println("</table>");
            writer.println("<br/> <br/>");
            
            writer.println("<table border=\"0\" width=\"100%\">");
            writer.println("<tr>");
            writer.println("  <td colspan=\"2\">");
            writer.println("    <b>Results</b>");
            writer.println("  </td>");
            writer.println("</tr>");
            
            if (button != null) {
                
                try {
                    service = new Service();
                    call    = (Call) service.createCall();
                    
                    call.setTargetEndpointAddress(endpoint);
                    call.setOperationName(method);

                    if (method.equalsIgnoreCase("addSynonym")) {
                        ret = (String) call.invoke( new Object[] { index, term, synonym } );
                        writer.println("<tr>");
                        writer.println("  <td colspan=\"2\">");
                        writer.println(ret);
                        writer.println("  </td>");
                        writer.println("</tr>");                        
                    } else if (method.equalsIgnoreCase("deleteSynonym")) {
                        ret = (String) call.invoke( new Object[] { index, term } );
                        writer.println("<tr>");
                        writer.println("  <td colspan=\"2\">");
                        writer.println(ret);
                        writer.println("  </td>");
                        writer.println("</tr>");                          
                    } else if (method.equalsIgnoreCase("updateSynonyms")) {
                        ret = (String) call.invoke( new Object[] { index } );
                        writer.println("<tr>");
                        writer.println("  <td colspan=\"2\">");
                        writer.println(ret);
                        writer.println("  </td>");
                        writer.println("</tr>");                          
                    } else if (method.equalsIgnoreCase("listSynonyms")) {
                        retArray = (String[]) call.invoke( new Object[] { index } );
                        
                        for(int j=0 ; j<retArray.length ; j++) {
                            writer.println("<tr>");
                            writer.println("  <td colspan=\"2\">" + retArray[j] + "</td>");
                            writer.println("</tr>");
                        }
                    } else if (method.equalsIgnoreCase("getSynonyms")) {
                        retArray = (String[]) call.invoke( new Object[] { index, term } );
                        
                        for(int j=0 ; j<retArray.length ; j++) {
                            writer.println("<tr>");
                            writer.println("  <td><b>" + term + ": </b></td>");
                            writer.println("  <td>" + retArray[j] + "</td>");
                            writer.println("</tr>");
                        }                            
                    }
                    
                } catch (Exception e) {
				    StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					pw.close();
																				
                    log.severe(sw.toString());
                }
            
            }
            
            writer.println("</table>");
            writer.println("</body>");
            writer.println("</html>");
        }

    }



    
    /** Creates a new instance of client */
    public synonymClient() {
    }
    
}
