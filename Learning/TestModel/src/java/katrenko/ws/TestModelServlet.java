/*
 * TestModelServlet.java
 *
 * Created on 31 March 2006, 12:06
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

public class TestModelServlet extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
   

 
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        doGet(request,  response);

        String[] model = new String[1];
        model = request.getParameterValues("model_file");
        String[] test = new String[1];
        test = request.getParameterValues("test_file");
    //    System.out.println("TEST " + test_file[0]);
        String ret = new String();ret = "<doc>Please check your test data once again!</doc>";
        try {
            String endpoint = Properties.Entries.AXIS_ENDPOINT.get() + 
                      "/services/TestModel";
  
             Service  service = new Service();
             Call     call    = (Call) service.createCall();
  
             call.setTargetEndpointAddress(endpoint);
             call.setOperationName("test_model");
  
             ret = (String) call.invoke( new Object[] { model[0], test[0], "text"} );
          
         } catch (Exception e) {
             System.err.println(e.toString());
         } 
    //    out.close();
        request.setAttribute("output",ret);
        RequestDispatcher dispatch=request.getRequestDispatcher("/XMLRedirectTest");
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
        String outp = new String();outp = "";
        String contents = "";
        String line;
            try{
            BufferedReader inputData = new BufferedReader(new FileReader("/home/sophijka/WebServices/TestModelService/Data/totest.xml"));
            while ( (line = inputData.readLine()) != null)
             {
               outp = new StringBuffer (outp).append(line).toString();
             }
        }catch(Exception ie){System.out.println("Can't read demo file");}
  
        out.println("<html>");
	out.println("<head>");
	out.println("<title>Test Model Web Service</title>");
	out.println("</head>");
        out.println("<body bgcolor=\"#808080\" text=\"000000\">");
        // out.println("<p><img src=\"http://pc-swi18.science.uva.nl:8084/axis/temp/vle1.jpg\"/></p>");
        out.println("<h2 align=\"center\" style = \"font-family:Verdana\"><span style=\"font-weight: bold; color:#C0C0C0\">Test Model Web Service</span></h2><br/><br/>");
        String model_file = new String();
        model_file = request.getParameter("model_file"); 
        String test_file = new String();
        test_file = request.getParameter("test_file");
        
       // if ((model_file == null) || (test_file == null)) { 
         if (test_file == null) { 
           out.println("<form action=\"" + "\" method=\"POST\">\n");
           out.println("<h4>Please enter the following information:</h4>");
           out.println("<h4>choose the trained model</h4>");
           out.println("<select id=\"model_file\" name=\"model_file\">");
           //out.println("<option value=\"C:/Documents and Settings/Sophijka/Mijn documenten/INSTALL/Data/TNO_ALL_1_Nolead.mod\">Nolead.mod</option>");
           // out.println("<option value=\"C:/Documents and Settings/Sophijka/Mijn documenten/INSTALL/Data/TNO_ALL_1_CARCbalanced.mod\">corpus2BAL.mod</option>");
           out.println("<option value=\"/home/sophijka/WebServices/TestModelService/Data/corpus2.mod\">corpus2.mod</option>");
         //  out.println("<option value=\"3\">3</option>");
           out.println("</select>");
           out.println("<h4>Please enter your test data below (already inserted text can be used with corpus2.mod model)</h4>");
           out.println("<h5>(Do not forget to add \"document\" tag!)</h5>");
           out.println("<TEXTAREA name=\"test_file\" cols=\"60\" rows=\"16\">" + outp + "</TEXTAREA><br/><br/>");
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


    
    
