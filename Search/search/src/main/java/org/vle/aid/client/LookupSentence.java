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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.vle.aid.lucene.tools.Files;
/**
 *
 * @author Edgar Meij
 */
    
public final class LookupSentence extends HttpServlet {  
    
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
        
        String pmid = request.getParameter("pmid");
        String offsetStr = request.getParameter("offset");
        String lengthStr = request.getParameter("length");
        
        int offset;
        int length;        
        boolean error = false;
        
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");        
        
        try { 
            offset = Integer.parseInt(offsetStr); 
            length = Integer.parseInt(lengthStr); 
        } catch (NumberFormatException e) { 
            printError(e.getMessage(), out);
            return;
        }
        
        if (!error) {
            
            try {
                File source = new File("/home/ilps/trecgen2006/source/noJournalDirs.source_files/" + pmid + ".html");
                char[] content = Files.readCharsFromFile(source);
                
                /*
                if (source.canRead()) {
                    
                    log.info("pmid: " + pmid);
                    log.info("Offset: " + offset);
                    log.info("Length: " + length);                    
                    log.info("Opening: " + source.getAbsolutePath());
                    log.info("Length of file: " + content.length);
                    
                }

                out.println("<html>");
                out.println("<head>");
                out.println("<title>Sentence lookup</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("test");
                out.println("</body>");
                out.println("</html>");        
                */
                
                for (int i=0; i<content.length; i++) {
                    if (i == offset) {
                        out.print("<table><tr><td bgcolor=#ffff66><a name='ID'></a>");
                    } else if (i == (offset + length)) {
                        out.print("</td></tr></table>");
                    }
                    
                    out.print(content[i]);
                }
                
            } catch (java.io.FileNotFoundException e) {
                printError(e.getMessage(), out);
                return;                
            } catch (IOException e) {
                printError(e.getMessage(), out);
                return;                
            } finally {
                out.close();
            }
        }
    }
    
    public void printError(String e, PrintWriter out) {
        out.println("<html>");
        out.println("<body>");
        out.println("<H3>Something is wrong with the input: </H3>");
        out.println(e);
        out.println("</body>");
        out.println("</html>");     
    }
}
