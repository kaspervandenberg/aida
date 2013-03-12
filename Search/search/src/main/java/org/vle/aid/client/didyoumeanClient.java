/*
 * synonymClient.java
 *
 *
 * Need to update 'String endpoint'
 *
 */

package org.vle.aid.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;

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
    
public final class didyoumeanClient extends HttpServlet {

    String          indexLocation = System.getenv("INDEXDIR") + 
            System.getProperty("file.separator");
    
    Service     service;
    Call        call;
    String      endpoint = "http://localhost:8080/axis/services/spellCheck";        
    
    Service     serviceFields;
    Call        callFields;
    String      endpointFields = "http://localhost:8080/axis/services/getFields";
    
    /** logger for Commons logging. */
    private transient Logger log =
	 Logger.getLogger("getWordnetSynWS.class.getName()");     
    
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
        
        String   ret         = "";    
        String[] methodArray = {
            "createModel",
            "didyoumean",
            "didyoumeanDEBUG"
        };
        
        String index    = request.getParameter("index");
        String term     = request.getParameter("term");
        String button   = request.getParameter("button");
        String method   = request.getParameter("method");
        String field    = request.getParameter("field");
        
        if (term == null)
            term = "";
        if (method == null)
            term = "";
        
        // Call XML service, special case
        if (button != null && method != null && method.equalsIgnoreCase("listSynonymsXML")) {
          
            
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
            writer.println("<title>DidYouMean Webservice</title>");
            writer.println("</head>");
            writer.println("<body bgcolor=white>");
            writer.println("<table border=\"0\">");
            writer.println("<tr>");
            writer.println("<td>");
            writer.println("<img src=\"/search/images/vlelogo.png\">");
            writer.println("</td>");
            writer.println("<td>");
            writer.println("<h1>DidYouMean Webservice</h1>");
            writer.println("</td>");
            writer.println("</tr>");
            writer.println("</table>");
            
            writer.println("<table border=\"0\" width=\"700\">");
            writer.println("<form name=\"didyoumeanClientForm\" method=\"get\">");
            writer.println("<tr>");
            writer.println("  <td colspan=3>Index:<br/>");
            writer.println("    <select name=\"index\"  onchange=\"didyoumeanClientForm.submit()\">");
            writer.println("      <option value=\"QCK\"" + 
                    "> Choose an index" + "</option>");
            
            for (int k = 0; k < listIndexes.length; ++k) {
				Directory dir = FSDirectory.open(listIndexes[k]);
                if (DirectoryReader.indexExists(dir)) {
                    if (index == null || !index.equals(listIndexes[k].getName())) {
                        writer.println("      <option>" + listIndexes[k].getName() + "</option>");
                    } else {
                        writer.println("      <option SELECTED>" + listIndexes[k].getName() + "</option>");
                    }
                }
            }            
            
            writer.println("    </select>");
            writer.println("  </td>");
            writer.println("</tr>");

            writer.println("<tr>");
            writer.println("  <td colspan=3>");              
            writer.println("Search field:<br>");
            writer.println("<select name=\"field\">");
                
            if (index != null) {
                
                try {
                    serviceFields = new Service();
                    callFields    = (Call) serviceFields.createCall();
                    
                    callFields.setTargetEndpointAddress(endpointFields);
                    callFields.setOperationName("listFields");
                    
                    String[] fieldList = (String[]) callFields.invoke( new Object[] { index} );
                    
                    for (int k = 1; k < fieldList.length; ++k) {
                        writer.println("<option>" + fieldList[k] + "</option>");
                }
                } catch (Exception e) {
                    writer.println(e.toString());
                }                


            }

            writer.println("</select>");            
            writer.println("  </td>");
            writer.println("</tr>");            
            
            writer.println("<tr>");
            writer.println("  <td colspan=3>");            
            writer.println("action:<br>");
            writer.println("    <select name=\"method\" onchange=\"didyoumeanClientForm.submit()\">");
            writer.println("<option value=\"didyoumean\">" +
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
            
            writer.println("<tr halign=\"left\">");
            writer.println("  <td width=280>");
            writer.println("Term:");
            writer.println("<br>");
            
            writer.println("<input type=\"text\" name=\"term\" " +
                        "size=\"30\" value=\"" + term + "\">");                
            
            writer.println("  </td>");
            
            if (button != null) {
                
                writer.println("  <td width=105>");
                writer.println("    <b>Did you mean: </b>");
                writer.println("  </td>");
                
                
                writer.println("  <td>");
                try {
                    service = new Service();
                    call    = (Call) service.createCall();
                    
                    call.setTargetEndpointAddress(endpoint);
                    call.setOperationName(method);

                    if (method.equalsIgnoreCase("createModel"))
                        ret = (String) call.invoke( new Object[] { index, field } );
                    if (method.equalsIgnoreCase("didyoumean")) 
                        ret = (String) call.invoke( new Object[] { index, term } );
                    if (method.equalsIgnoreCase("didyoumeanDEBUG")) 
                        ret = (String) call.invoke( new Object[] { index, term, field } );
                        
                    writer.println(ret);
                    
                } catch (Exception e) {
                    log.severe(e.toString());
                }
            
            }
            
            writer.println("  </td>");
            writer.println("</tr>");            
            
            writer.println("<tr>");
            writer.println("  <td colspan=3>");
            writer.println("<input type=\"submit\" value=\"Submit\" name=\"button\">");
            writer.println("  </td>");
            writer.println("</tr>");            
            
            writer.println("</form>");            
            writer.println("</table>");
            writer.println("<br/> <br/>");
            
            writer.println("<table border=\"0\" width=\"100%\">");
            writer.println("<tr>");

            writer.println("</tr>");
            
            
            
            writer.println("</table>");
            writer.println("</body>");
            writer.println("</html>");
        }
    }    
}
