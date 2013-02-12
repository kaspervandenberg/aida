/*
 * termFinder.java
 *
 * Created on May 1, 2006, 1:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.client;

import java.io.*;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author edgar
 */
public class termFinder extends HttpServlet {   
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
  @SuppressWarnings("unchecked")
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map param = request.getParameterMap();
        HttpSession session = request.getSession(false);
        
        String root  = request.getContextPath();
        String index = null;
        String field = null;
        
        try {
            // First try session, then request params
            index = (String) session.getAttribute("index");
            String[] selectedFields = (String[]) session.getAttribute("selectedFields");
            field = selectedFields[0];
            
            index = ((String[])param.get("index"))[0];
            field = ((String[])param.get("field"))[0];
            
        } catch (java.lang.NullPointerException e) { }
        
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        out.println("<html>");
        out.println("   <head>");
        out.println("       <title>AID Term Finder</title>");
        out.println("       <link REL=\"SHORTCUT ICON\" HREF=\""+root+"/images/favicon.ico\">");        
        out.println("       <link href='" + root + "/css/aida.css' rel='stylesheet' type='text/css'/>");
        out.println("       <script type='text/javascript' src='" + root + "/javascript/autocompletion/prototype.js'></script>");
        out.println("       <script type='text/javascript' src='" + root + "/javascript/autocompletion/rico.js'></script>");
        out.println("       <script type='text/javascript' src='" + root + "/javascript/autocompletion/suggest_debug.js'></script>");

        out.println("       <script type='text/javascript'>");
        out.println("           var propertyRequestParameters = [];");
        out.println("           propertyRequestParameters.push('query_type=property');");
        out.println("           var propertySuggestOptions = {");
        out.println("               matchAnywhere      : false,");
        out.println("               ignoreCase         : true,");
        out.println("               count              : 50,");
        out.println("               requestParameters  : propertyRequestParameters,");
        out.println("               index              : '" + index + "',");
        out.println("               field              : '" + field + "'");
        out.println("           };");
        out.println("           function injectSuggestBehavior() {");
        out.println("               property_suggest = new TextSuggest(");
        out.println("                               'property',");
        out.println("                               '" + root + "/LuceneAutoCompletionSVL',");
        out.println("                               propertySuggestOptions");
        out.println("               );");
        out.println("           }");
        out.println("       </script>");
        out.println("   </head>");
        out.println("   <body onload='javascript:injectSuggestBehavior();document.annotate.addTerm.focus();'>");
        
        if (index == null || field == null) {
            out.println("<div class='title'>please define an index and a field</div>");
        } else {
            out.println("       <form name='annotate' autocomplete='off' action='" + root + "/AID'>");
            out.println("           <div class='property'>");
            out.println("               <div class='entry'>");
            out.println("                   <div class='label'>Searching index: " + index + " </div>");
            out.println("                   <div class='label'>and field: " + field + "</div>");
            out.println("                   <div class='label'> </div>");
            out.println("                   <div class='label'>Term:</div>");
            out.println("                   <input name='addTerm' id='property' type='text' size='30'/>");
            out.println("                   <input type=submit name=\"addButton\" value=\"Add\">");
            out.println("               </div>");
            out.println("           </div>");
            out.println("       </form>");
        }
        out.println("   </body>");
        out.println("</html>");

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

