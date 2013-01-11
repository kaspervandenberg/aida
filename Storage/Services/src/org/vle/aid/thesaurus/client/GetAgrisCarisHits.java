/*
 * GetAgrisCarisHits.java
 *
 * Created on January 11, 2006, 10:31 AM
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
public class GetAgrisCarisHits extends HttpServlet {

    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GetAgrisCarisHits.class);

    private String agris_caris_search_url = "http://www.fao.org/agris/search/search.do";
    private String agris_caris_export_url = "http://www.fao.org/agris/search/export.do";
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
        
        String page = _getAgrisCarisSearchResults(((String[])param.get("query"))[0]);

        String ids = _getDocumentIDs(page);

        String export = _getAgrisCarisExportResults(ids);

        out.println(export);

        out.close();
    }

    private String _getAgrisCarisSearchResults(String query) {
        String q = "";
        try { q = URLEncoder.encode(query,"UTF-8"); } 
        catch (Exception e) { 
            logger.error("URL encoding error for address: " + query,e);
        }
        return _fetchURL(agris_caris_search_url + "?hitcount=" + hitcount + "&method=Search&query=" + q);
    }

    private String _getAgrisCarisExportResults(String query) {
        String q = "";
        try { q = URLEncoder.encode(query,"UTF-8"); } 
        catch (Exception e) { 
            logger.error("URL encoding error for address: " + query,e);
        }
        return _fetchURL(agris_caris_export_url + "?export=" + q);
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

    private String _getDocumentIDs(String doc) {
        Vector names = new Vector();
        String rv = "";
        Source source=new Source(doc);
        StringWriter sw = new StringWriter();
        source.setLogWriter(sw); // send log messages to string
        source.fullSequentialParse();
        List elementList=source.findAllElements();
        for (Iterator i=elementList.iterator(); i.hasNext();) {
            Element element=(Element)i.next();
            if (element.getName().equals("input")) {
                String type;
                String name;
                if ((type = element.getAttributeValue("type")) != null) {
                    if (type.equals("checkbox") && (name = element.getAttributeValue("name")) != null) {
                        names.add(name);
                    }
                }
            }
        }
        if (names.size() > 0) {
            rv = (String)names.get(0);
            for (int i=1;i<names.size();i++) {
                rv = rv.concat(",");
                rv = rv.concat((String)names.get(i));
            }
        }
        String sws = sw.toString();
        if (sws.length() > 0){
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
