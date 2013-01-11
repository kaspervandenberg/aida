/*
 * RepositoryAddRdfSVL.java
 *
 * Created on January 30, 2006, 1:07 PM
 */
package org.vle.aid.metadata.client;

import java.io.*;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.axis.client.*;

/**
 *
 * @author wrvhage
 * @version
 */
public class RepositoryGetRepositoriesSVL extends HttpServlet {

  /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {

    Map param = request.getParameterMap();

    boolean outputjason = false;
    if (param.containsKey("json"))
      outputjason = true;

      try {
        String endpoint = null;
        if (param.get("axis_url") != null) {
          endpoint = ((String[]) param.get("axis_url"))[0] + "/services/RepositoryWS";
        } else {
          endpoint = "http://localhost:8080/axis/services/RepositoryWS";
        }

        Service service = new Service();
        Call call = (Call) service.createCall();

        call.setTargetEndpointAddress(endpoint);
        call.setOperationName("getRepositoriesLabel");

        String[][] rs = (String[][]) call.invoke(new Object[]{
                  ((String[]) param.get("server_url"))[0],
                  ((String[]) param.get("username"))[0],
                  ((String[]) param.get("password"))[0],
                  ((String[]) param.get("read_write"))[0]
                });
        
        if (rs != null) {
          if (outputjason) { // { success: false, errors: { reason: 'Login failed. Try again.' }}
            response.setContentType("text/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            
            out.print("{'success': true, ");
            
            out.print("'repositories':[");
            String temprepos = "";
            for (int i = 0; i < rs.length; i++) {
              //temprepos += "{'repository':'" + rs[i] + "'},";
              temprepos += "['"+rs[i][0]+"','"+rs[i][1]+"'],";
            }
            // remove trailing comma
            if (temprepos.length() > 0)
              temprepos = temprepos.substring(0, temprepos.length()-1);
            out.print(temprepos);
            out.print("]}");
            out.close();
          } else {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<html><body>");
            out.println("<h2>returned</h2>");
            for (int i = 0; i < rs.length; i++) {
              out.println(rs[i][0] + " " + rs[i][1] + "<br/>");
            }
            out.println("</body></html>");
            out.close();
          }
        } else {
          if (outputjason) {
            response.setContentType("text/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{'success': false, 'errors': {'reason': 'No repositories found. Try again.'}}");
            out.close();
          } else {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<html><body>");
            out.println("<h2>returned</h2>");
            out.println("</body></html>");
            out.close();
          }
        }
      } catch (Exception e) {
        if (outputjason) {
          response.setIntHeader(e.toString(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } else {
          response.setContentType("text/html;charset=UTF-8");
          PrintWriter out = response.getWriter();
          out.println("<b>" + e.toString() + "</b><br/><br/>");
          for (int i = 0; i < e.getStackTrace().length; i++) {
            out.println(e.getStackTrace()[i].toString() + "<br/>");
          }
          out.close();
        }
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
