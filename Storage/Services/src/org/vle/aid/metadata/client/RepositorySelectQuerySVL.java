/*
 * RepositoryAddRdfSVL.java
 *
 * Created on January 30, 2006, 1:07 PM
 */
package org.vle.aid.metadata.client;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 *
 * @author wrvhage
 * @version
 */
public class RepositorySelectQuerySVL extends HttpServlet {

  /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    
    Map param = request.getParameterMap();

    boolean scriptTag = false;
    String cb = request.getParameter("callback");

    boolean useJSON =
      (request.getParameter("select_output_format").equalsIgnoreCase("json") 
      ||  request.getParameter("format").equalsIgnoreCase("json"))
      ? true : false;

    if (cb != null) {
      scriptTag = true;
      response.setContentType("text/html;charset=UTF-8");
    } else {
      response.setContentType("application/x-json");
    }

    PrintWriter out = response.getWriter();

    try {

      if (scriptTag) { // do proxy
        String urlname = request.getParameter("remote_url");

        if (urlname == null) {
          throw new ServletException("url not defined");
        }

        String pr = request.getQueryString();
        if (pr != null) {
          urlname += "?" + pr;
        }

        System.err.println("Retrieving " + urlname);

        out.write(cb + "(");

        InputStream in = null;
        
        try {
          URL url = new URL(urlname);   // Create the URL
          URLConnection uc = url.openConnection();
          uc.setDefaultUseCaches(false);
          uc.setUseCaches(false);
          uc.setRequestProperty("Cache-Control", "max-age=0,no-cache");
          uc.setRequestProperty("Pragma", "no-cache");
          in = uc.getInputStream();
          OutputStream xos = new ByteArrayOutputStream();

          // Now copy bytes from the URL to the output stream
          byte[] buffer = new byte[4096];
          int bytes_read;
          while ((bytes_read = in.read(buffer)) != -1) { 
            xos.write(buffer, 0, bytes_read);
          }

          out.write(xos.toString());
        } // On exceptions, print error message and usage message.
        catch (Exception e) {
          // up up and away
          throw new Exception(e);
          
        } finally {
          in.close();
        }

        out.write(");");

      } else {

        String endpoint = request.getParameter("axis_url");
        if (endpoint != null) {
          endpoint += "/services/RepositoryWS";
        } else {
          endpoint = "http://localhost:8080/axis/services/RepositoryWS";
        }

        Service service = new Service();
        Call call = (Call) service.createCall();

        call.setTargetEndpointAddress(endpoint);
        call.setOperationName("selectQuerySerialized");
        String s = (String) call.invoke(new Object[]{
              ((String[]) param.get("server_url"))[0], ((String[]) param.get("repository"))[0],
              ((String[]) param.get("username"))[0], ((String[]) param.get("password"))[0],
              ((String[]) param.get("query_language"))[0], ((String[]) param.get("select_output_format"))[0],
              ((String[]) param.get("query"))[0]
            });
        out.println(s);

      }
    } catch (Exception e) {

      if (useJSON) {

        out.println("{ success: false, errors: { reason: '"+e.toString()
            .replaceAll("\\n","<br/>")
            .replaceAll("'", "\"")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            + "' }}");
      } else {
        out.println("<html><body>");
        out.println("<b>" + e.toString() + "</b><br/><br/>");
        for (int i = 0; i < e.getStackTrace().length; i++) {
          out.println(e.getStackTrace()[i].toString() + "<br/>");
        }
        out.println("</body></html>");
      }
    } finally {
      out.close();
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
