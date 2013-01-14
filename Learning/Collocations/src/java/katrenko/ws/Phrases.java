/*
 * Phrases.java
 *
 * Created on March 20, 2006, 3:13 PM
 */

package katrenko.ws;

import java.io.*;
import java.net.*;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.Object;

import java.io.File;
import java.io.IOException;
import java.lang.Double;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import java.io.BufferedReader;
import java.io.FileReader;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.vle.aid.common.Properties;

/**
 *
 * @author software
 * @version
 */

public class Phrases extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
   

 
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        doGet(request,  response);

	String[] train = new String[1];
        train = request.getParameterValues("train");
        String[] test = new String[1];
        test = request.getParameterValues("test");

        String[] window = new String[1];
        window = request.getParameterValues("window");
        //System.out.println(window[0]);     
        String[] mincount = new String[1];
        mincount = request.getParameterValues("mincount");
        String[] maxcount = new String[1];
        maxcount = request.getParameterValues("maxcount");

//        response.setContentType("text/xml;charset=UTF-8");
//        PrintWriter out = response.getWriter();
        String ret = new String();
        try {
             String endpoint = Properties.Entries.AXIS_ENDPOINT.get() +
		 "/services/CollocationService";
  
             Service  service = new Service();
             Call     call    = (Call) service.createCall();
  
             call.setTargetEndpointAddress(endpoint);
             call.setOperationName("find_phrases");
             //ret = (String) call.invoke( new Object[] { "D://INSTALL/Data/train", "D://INSTALL/Data/test", window[0], mincount[0], maxcount[0] } );       
           ret = (String) call.invoke( new Object[] { train[0], test[0], window[0], mincount[0], maxcount[0] } );       
          /* out.println("<p>Results: " + ret + "</p>");*/
  //         out.println(ret);
    
         } catch (Exception e) {
             System.err.println(e.toString());
         } 
    //    out.close();
        request.setAttribute("output",ret);
        RequestDispatcher dispatch=request.getRequestDispatcher("/XMLRedirect");
        dispatch.forward(request,response);   
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");        
        PrintWriter out = response.getWriter();
        synchronized(this)
{
        out.println("<html>");
	out.println("<head>");
	out.println("<title>Collocations</title>");
	out.println("</head>");
        out.println("<body bgcolor=\"#808080\" text=\"000000\">");
        //out.println("<p><img src=\"http://pc-swi18.science.uva.nl:8084/axis/temp/vle1.jpg\"/></p>");
        out.println("<h2 align=\"center\" style = \"font-family:Verdana\"><span style=\"font-weight: bold; color:#C0C0C0\">Collocations Web Service</span></h2><br/><br/>");
        String ngram=new String();
        ngram = request.getParameter("ngram");  
        String mincount=new String();
        mincount = request.getParameter("mincount");
        String maxcount=new String();
        maxcount = request.getParameter("maxcount");
        
        if ((ngram == null) || (mincount == null) || (maxcount == null)) { 
           out.println("<form action=\"" + "\" method=\"POST\">\n");
           out.println("<h4>Please enter the following information:</h4>");
           out.println("<h4>Context window size</h4>");
           out.println("<select id=\"window\" name=\"window\">");
           out.println("<option value=\"2\">2</option>");
           out.println("<option value=\"3\">3</option>");
           out.println("<option value=\"4\">4</option>");
           out.println("<option value=\"5\">5</option>");
           out.println("</select>");
            out.println("<h4><br/>Training folder (e.g., \"/home/sophijka/WebServices/Data/train/\")</h4>");
           out.println("<input type=\"text\" name=\"train\"><br/>");
           out.println("<h4><br/>Test folder (e.g., \"/home/sophijka/WebServices/Data/test/\")</h4>");
           out.println("<input type=\"text\" name=\"test\"><br/>");
          
	   out.println("<h4><br/>Minimum count for a returned n-gram (e.g., 2)</h4>");
           out.println("<input type=\"text\" name=\"mincount\"><br/>");
           out.println("<h4>Maximum number of collocations to be returned (e.g., 100)</h4>");
           out.println("<input type=\"text\" name=\"maxcount\"><br/><br/>");
           out.println("<input type=\"submit\" + name=\"button\">\n");
           out.println("</form>");
        }
        out.println("</body>");
        out.println("</html>");
        //out.close();

    } 
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
