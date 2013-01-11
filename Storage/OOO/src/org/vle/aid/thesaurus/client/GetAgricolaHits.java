/*
 * GetAgricolaHits.java
 *
 * Created on January 16, 2006, 16:53
 */

package org.vle.aid.thesaurus.client;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Vector;
import java.util.List;
import java.util.Iterator;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;

import javax.servlet.*;
import javax.servlet.http.*;

import au.id.jericho.lib.html.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 *
 * @author wrvhage
 * @version
 */
public class GetAgricolaHits extends HttpServlet {

    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GetAgricolaHits.class);

    private String agricola_search_url = "http://agricola.nal.usda.gov/cgi-bin/Pwebrecon.cgi";
    private int hitcount = 5;

    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map param = request.getParameterMap();
        
        if (param.containsKey("hitcount")) {
            hitcount = Integer.valueOf(((String[])param.get("hitcount"))[0]).intValue();
        }
        
        String page = _getAgricolaSearchResults(((String[])param.get("query"))[0]);

        String export = _exportAgricolaHtmlToDublinCore(page);

        out.println(export);

        out.close();
    }

    private String _getAgricolaSearchResults(String query) {
        String q = "";
        try { q = URLEncoder.encode(query,"UTF-8"); } 
        catch (Exception e) { 
            logger.error("URL encoding error for address: " + query,e);
        }
        return _fetchURL(agricola_search_url + "?DB=local&CNT=" + hitcount + "&CMD=" + q + "&STARTDB=AGRIDB");
    }

    private String _fetchURL(String address) {
        String rv = "";
        try {

            logger.debug("fetching: " + address);

            // Create an URL instance
            URL url = new URL(address);

            // Get an input stream for reading
            InputStream in = url.openStream();
            
            // Create a buffered input stream for efficency
            BufferedInputStream bufIn = new BufferedInputStream(in);
            
            // Repeat until end of file
            for (;;) {
                int data = bufIn.read();                
                // Check for EOF
                if (data == -1) break;
                else rv = rv.concat(String.valueOf((char)data));
            }
        } catch (MalformedURLException mue) {
            logger.error("Invalid URL",mue);
        }
        catch (IOException ioe) {
            logger.error("I/O Error - " + ioe,ioe);
        }
        return rv;
    }

    private String _exportAgricolaHtmlToDublinCore(String doc) {
        String rv = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<ags:resources xmlns:ags='http://www.purl.org/agmes/1.1/' " +
            "xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:dcterms='http://purl.org/dc/terms/'>\n";
        Source source=new Source(doc);
        StringWriter sw = new StringWriter();
        source.setLogWriter(sw); // send log messages to the string writer
        source.fullSequentialParse();
        List elementList=source.findAllElements();
        Element[] e = new Element[2];
        Element[] list = (Element[])(elementList.toArray(e));
        for (int i=0;i<list.length-8;i++) {
            if (list[i].getName().equals("tr") &&
                list[i+1].getName().equals("input") &&
                list[i+2].getName().equals("td") &&
                list[i+3].getName().equals("input") &&
                list[i+4].getName().equals("a") &&
                list[i+5].getName().equals("td") &&
                list[i+6].getName().equals("td") &&
                list[i+7].getName().equals("a") &&
                list[i+8].getName().equals("td")) {
                rv += "\t<ags:resource>\n" +
                    "\t\t<dc:creator>\n\t\t\t<ags:creatorPersonal>" + 
                    list[i+8].getContent().toString() + 
                    "\n\t\t\t</ags:creatorPersonal>\n\t\t</dc:creator>\n" +
                    "\t\t<dc:date>\n\t\t\t<dcterms:dateIssued>" + 
                    list[i+5].getContent().toString() + 
                    "\n\t\t\t</dcterms:dateIssued>\n\t\t</dc:date>\n" +
                    "\t\t<dc:title xml:lang=\"en\">" + list[i+7].getContent().toString() + "</dc:title>\n" +
                    "\t</ags:resource>\n";
            }
        }
        rv += "</ags:resources>\n";
        String sws = sw.toString();
        if (sws.length() > 0) {
            String[] sws_lines = sws.split("\\n");
            for (int i=0;i<sws_lines.length;i++) {
                logger.warn(sws_lines[i]);
            }
        }
        return rv;
    }

     
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @Param response servlet response
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
