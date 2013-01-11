/*
 * ThesaurusBrowser.java
 *
 * Created on March 7, 2006, 2:39 PM
 */

package org.vle.aid.thesaurus.client;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 *
 * @author wrvhage
 * @version
 */
public class ThesaurusSearch extends HttpServlet {
    
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ThesaurusBrowser.class);
            
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map param = request.getParameterMap();
        String uri = HttpUtils.getRequestURL(request).toString();
        try {
            URI uriURI = new URI(HttpUtils.getRequestURL(request).toString());
            String scheme = uriURI.getScheme();
            String host = uriURI.getHost();        
            int port = uriURI.getPort();
            String path = uriURI.getPath();        
            
            String server_url = null;
            String repository = null;
            String username = null;
            String password = null;
            String query = null;
            String ns = null;
            
            String ws_server_url = null;
            
            if (!param.containsKey("server_url")) { throw new ServletException("server_url not defined"); } 
            else { server_url = ((String[])param.get("server_url"))[0]; }
            
            if (!param.containsKey("ws_server_url")) { 
                ws_server_url = server_url; 
                ws_server_url = ws_server_url.replaceAll("/sesame.*","");
            } 
            else { ws_server_url = ((String[])param.get("ws_server_url"))[0]; }
            
            if (!param.containsKey("repository")) { throw new ServletException("repository not defined"); } 
            else { repository = ((String[])param.get("repository"))[0]; }
            if (!param.containsKey("username")) { throw new ServletException("username not defined"); } 
            else { username = ((String[])param.get("username"))[0]; }
            if (!param.containsKey("password")) { throw new ServletException("password not defined"); } 
            else { password = ((String[])param.get("password"))[0]; }
            
            if (!param.containsKey("query")) { } 
            else { query = ((String[])param.get("query"))[0]; }
            if (!param.containsKey("ns")) { throw new ServletException("ns (namespace) not defined"); }
            else { ns = ((String[])param.get("ns"))[0]; }
            
            out.print("<viewer>");
            out.print("<ns>" + ns + "</ns>");
            String base_url = scheme + "://" + host + ":" + port + "/OOO/ThesaurusBrowser" + "?";
            String login_info = "&amp;server_url=" + URLEncoder.encode(server_url,"UTF-8") +
                "&amp;repository=" + URLEncoder.encode(repository,"UTF-8") +
                "&amp;username=" + URLEncoder.encode(username,"UTF-8") +
                "&amp;password=" + URLEncoder.encode(password,"UTF-8");
            
            URI u2 = new URI(scheme,null,host,port,"/OOO/ThesaurusSearch",null,null);
            out.print("<search_action>" + u2.toString() + "</search_action>" +
                      "<server_url>" + server_url + "</server_url>" +
                      "<repository>" + repository + "</repository>" +
                      "<username>" + username + "</username>" +
                      "<password>" + password + "</password>");
            
            out.print("<search_results>"); 
            try {
                String endpoint = ws_server_url + "/axis/services/ThesaurusRepositoryWS";
                
                Service service = new Service();
                Call call = (Call) service.createCall();
                
                call.setTargetEndpointAddress(endpoint);
                call.setOperationName("getTermUri");
                
                String[][] rv = null;
                rv = (String[][]) call.invoke( new Object[] { server_url, repository, username, password, query } );
                if (rv != null) {
                    for (int i=0;i<rv.length;i++) {
                        call.setOperationName("getNumberOfNarrowerTerms");
                        String[][] nrnt = (String[][]) call.invoke( new Object[] { server_url, repository, username, password, rv[i][0] } );
                        String nrnt_str = "";
                          if (nrnt != null && nrnt.length > 0) {
                              nrnt_str = "<numberOfNarrowerTerms>" + nrnt[0][1] + "</numberOfNarrowerTerms>";
                          } else {
                              nrnt_str = "<numberOfNarrowerTerms/>";
                          }
                          out.print("<result>" +
                                    "<link>javascript:browser.loadConcept('" + base_url + 
                                    "','term=" + URLEncoder.encode(rv[i][0],"UTF-8") + 
                                    login_info + "&amp;ns=" + ns + "');</link>" +
                                    "<name>" + rv[i][1] + "</name>" +
                                    "<uri>" + rv[i][0] + "</uri>" +
                                    nrnt_str +
                                    "</result>");                    
                    }
                } else {
                    out.print("<result/>");
                }
                
            } catch (Exception e) {
                out.println("<b>" + e.toString() + "</b><br/><br/>");
                for (int i=0;i<e.getStackTrace().length;i++) {
                    out.println(e.getStackTrace()[i].toString() + "<br/>");
                }
            }
            out.print("</search_results>");
            out.print("</viewer>");

        } catch (java.net.URISyntaxException e) { 
            logger.error(e.getMessage(), e);
        }

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
