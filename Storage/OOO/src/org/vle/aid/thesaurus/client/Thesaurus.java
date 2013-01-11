/*
 * Thesaurus.java
 *
 * Created on March 7, 2006, 2:39 PM
 */

package org.vle.aid.thesaurus.client;

import java.io.*;
import java.net.*;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.lang.exception.NestableException;
import org.apache.commons.configuration.*;
import org.apache.commons.collections.*;

/**
 *
 * @author wrvhage
 * @version
 */
public class Thesaurus extends HttpServlet {
    
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Thesaurus.class);
            
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map param = request.getParameterMap();

        try {

            String prefix = getServletContext().getRealPath("/");
            PropertiesConfiguration config = new PropertiesConfiguration(prefix + "WEB-INF/Thesaurus.properties");
            
            // Server configuration
            String c_server_url = config.getString("server_info.server_url");
            String c_repository = config.getString("server_info.repository");
            String c_username = config.getString("server_info.username");
            String c_password = config.getString("server_info.password");
            String c_mapping_server_url = config.getString("mapping_server_info.server_url");
            String c_mapping_repository = config.getString("mapping_server_info.repository");
            String c_mapping_username = config.getString("mapping_server_info.username");
            String c_mapping_password = config.getString("mapping_server_info.password");
            
            // Data configuration
            String c_default_left_ns = config.getString("default.left_ns");
            String c_default_right_ns = config.getString("default.right_ns");

            // GUI configuration
            boolean c_gui_agricola_enabled = config.getBoolean("gui.agricola_enabled");
            boolean c_gui_agriscaris_enabled = config.getBoolean("gui.agriscaris_enabled");
            boolean c_gui_todo_enabled = config.getBoolean("gui.todo_enabled");
            boolean c_gui_history_enabled = config.getBoolean("gui.history_enabled");
            boolean c_gui_mapping_enabled = config.getBoolean("gui.mapping_enabled");
            boolean c_gui_dc_subject_docs_enabled = config.getBoolean("gui.dc_subject_docs_enabled");

            String c_gui_activity_animation_path = config.getString("gui.activity_animation_path");
            String c_gui_logo_path = config.getString("gui.logo_path");

            InetAddress addr = InetAddress.getLocalHost();
            String hostname = addr.getHostName();
            if (c_server_url == null) {
                c_server_url = hostname + ":8080/sesame";
            }
            if (c_mapping_server_url == null) {
                c_mapping_server_url = hostname = ":8080/sesame";
            }

            out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\">\n");
            out.println("<html>");
            out.println("  <head>");
            out.println("    <title>AIDA Thesaurus Browser</title>");
            out.println("    <script type=\"text/javascript\" src=\"thesaurus/parser.js\"></script>");
            out.println("    <script type=\"text/javascript\" src=\"thesaurus/browser.js\"></script>");
            out.println("    <script type=\"text/javascript\" src=\"thesaurus/agriscaris.js\"></script>");
            out.println("    <script type=\"text/javascript\" src=\"thesaurus/agricola.js\"></script>");
            out.println("    <script type=\"text/javascript\" src=\"thesaurus/todo.js\"></script>");
            out.println("    <link rel=\"stylesheet\" type=\"text/css\" href=\"ooo.css\"/>");
            out.println("    <link rel=\"stylesheet\" type=\"text/css\" href=\"thesaurus/browser.css\"/>");
            out.println("    <link rel=\"stylesheet\" type=\"text/css\" href=\"thesaurus/ns_extensions.css\"/>");
            out.println("    <link rel=\"stylesheet\" type=\"text/css\" href=\"thesaurus/agriscaris.css\"/>");
            out.println("    <link rel=\"stylesheet\" type=\"text/css\" href=\"thesaurus/agricola.css\"/>");
            out.println("    <script type=\"text/javascript\">");
            out.println("      <!--");
            out.println("      function login() {");
            out.println("          var u = document.getElementById('authority_username').value;");
            out.println("          var p = document.getElementById('authority_password').value;");
            out.println("          if(u != '' && p != '') {");
            out.println("              if (browser.authenticate(u,p)) {");
            out.println("                  document.authority_username=u;");
            out.println("                  document.authority_password=p;");
            out.println("                  document.getElementById('right').style.visibility = 'visible';");
            out.println("                  document.getElementById('todo').style.visibility = 'visible';");
            out.println(((param.get("term") != null) ? "browser.refresh_side='left';browser.loadConcept" : "browser.showTopConcepts") + "('ThesaurusBrowser?',");
            out.println("                      'server_url=" + c_server_url + "' +");
            out.println("                      '&repository=" + c_repository + "' +");
            out.println("                      '&username=" + c_username + "' +");
            out.println("                      '&password=" + c_password + "' +");
            out.println("                      '&mapping_server_url=" + c_mapping_server_url + "' +");
            out.println("                      '&mapping_repository=" + c_mapping_repository + "' + ");
            out.println("                      '&mapping_username=" + c_mapping_username + "' +");
            out.println("                      '&mapping_password=" + c_mapping_password + "' + ");
            out.println("                      '&ns=' + browser.default_left_ns" + 
                        ((param.get("term") != null) ? " + '&term=" + ((String[])param.get("term"))[0] + "',true" : "") + ");");
            out.println("              } else {");
            out.println("                  alert('incorrect username or password');");
            out.println("              }");
            out.println("          } else {");
            out.println("              alert('please fill in your username and password');");
            out.println("          }");
            out.println("      }");
            out.println("      browser.default_left_ns = '" + c_default_left_ns + "';");
            out.println("      browser.default_right_ns = '" + c_default_right_ns + "';");
            out.println("      browser.gui_todo_enabled = " + new Boolean(c_gui_todo_enabled).toString() + ";");
            out.println("      browser.gui_history_enabled = " + new Boolean(c_gui_history_enabled).toString() + ";");
            out.println("      browser.gui_mapping_enabled = " + new Boolean(c_gui_mapping_enabled).toString() + ";");
            out.println("      browser.gui_dc_subject_docs_enabled = " + new Boolean(c_gui_dc_subject_docs_enabled).toString() + ";");
            out.println("      browser.gui_agricola_enabled = " + new Boolean(c_gui_agricola_enabled).toString() + ";");
            out.println("      browser.gui_agriscaris_enabled = " + new Boolean(c_gui_agriscaris_enabled).toString() + ";");
            out.println("      browser.gui_activity_animation_path = '" + c_gui_activity_animation_path + "';");
            out.println("      -->");
            out.println("    </script>");
            out.println("  </head>");
            out.println("  <body onload=\"document.getElementById('authority_username').focus();\">");
            out.println("    <div class=\"logo\">");
            out.println("      <img class=\"logo\" src=\"" + c_gui_logo_path + "\" alt=\"AIDA\" />");
            out.println("    </div>");
            out.println("    <div id=\"mapping\">");
            out.println("    </div>");
            out.println("                <div id=\"left\">");
            out.println("      <form action=\"javascript:login();\">");
            out.println("        Username:<br/>");
            out.println("        <input type=\"text\" size=\"20\" id=\"authority_username\"/><br/><br/>");
            out.println("        Password:<br/>");
            out.println("        <input type=\"password\" size=\"20\" id=\"authority_password\"/><br/><br/>");
            out.println("        <input type=\"submit\" value=\"login\"/>");
            out.println("      </form>");
            out.println("    </div>");
            out.println("    <div id=\"right\" style=\"visibility:hidden;\">");
            out.println("      <div class=\"open\">");
            out.println("        <a href=\"javascript:browser.openView();\">open this view</a>");
            out.println("      </div>");
            out.println("    </div>");
            out.println("    <div id=\"todo\" style=\"visibility:hidden;\">");
            out.println("    </div>");
            out.println("  </body>");
            out.println("</html>");
            
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

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
