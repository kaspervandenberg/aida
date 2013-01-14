/*
 * RelationExtractionServlet.java
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
import java.util.*;
import java.util.regex.*;

import java.io.BufferedReader;
import java.io.FileReader;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.vle.aid.common.Properties;

/**
 * Client for the RelationExtraction web service. To invoke a service make sure
 * that the port number in <code>String endpoint = "http://localhost:8080/axis/services/RelationExtraction";</code>
 * is set accordingly. Change it if needed.
 *<p>
 * To run this client, use <code>http://localhost:8080/RelationExtraction/extractRelation</code>
 * <p>
 * @author Sophia Katrenko
 * @version 1.0
*/
public class RelationExtractionServlet extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        doGet(request,  response);

        String[] model = new String[1];
        model = request.getParameterValues("modelFile");
        String[] test = new String[1];
        test = request.getParameterValues("inputData");
        String ret = new String();ret = "A. Check if a model and its log file exists \nB. Check if the test data is in a needed format";
        try {
            String endpoint = Properties.Entries.AXIS_ENDPOINT.get() + 
                      "/services/RelationExtractor";
  
             Service  service = new Service();
             Call     call    = (Call) service.createCall();
  
             call.setTargetEndpointAddress(endpoint);
             call.setOperationName("annotateInput");
  
             ret = (String) call.invoke( new Object[] { test[0], model[0] } );
             
         } catch (Exception e) {
             System.err.println(e.toString());
         } 
        
        request.setAttribute("output",ret);
        response.setContentType("text/html;charset=UTF-8");        
        PrintWriter out = response.getWriter();
        //out.println(ret);
        out.println("<br/><br/><TEXTAREA name=\"inputData\" cols=\"60\" rows=\"16\" align=\"center\">");
        out.println("The following protein-protein interactions pairs are found:\n" + ret + "</TEXTAREA><br/><br/>");
        out.close();
        //RequestDispatcher dispatch=request.getRequestDispatcher("/XMLRedirectTest");
        //dispatch.forward(request,response);   
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
            
  
        URL url = RelationLearning.class.getResource("/katrenko/ws/test.txt");
        URI testdataURI = null;
        try{
          testdataURI = new URI(url.toString());
        } catch(Exception ex){System.err.println("Cannot locate test data!");}
      
    File bkFile = new File(testdataURI);
    
    /*try{
        inp = new StringStream(bkFile);
        //bk = sm.stemFile(bkFileInput);
    }catch (FileNotFoundException e) { e.printStackTrace();}  
      */ 
       try{
            BufferedReader inputData = new BufferedReader(new FileReader(bkFile));
            while ( (line = inputData.readLine()) != null)
             {
               outp = new StringBuffer (outp).append(line).toString();
               if (line.equals("")){
                outp = outp + " \n";
               }
               else outp = outp + "\n";
             }
        }catch(Exception ie){System.out.println("Can't read demo file");}
    
   /*     BufferedReader input = null;
        try{
            input = new BufferedReader( new FileReader(inputData) );
        
        }catch(Exception ie){System.out.println("Can't read demo file");}
     */   
        out.println("<html>");
		out.println("<head>");
		out.println("<title>RelationExtraction Web Service</title>");
		out.println("</head>");
        out.println("<body bgcolor=\"#FFFFFF\" text=\"000000\">");
        String model_file = new String();
        model_file = request.getParameter("modelFile"); 
        String test_file = new String();
        test_file = request.getParameter("inputData");
        String top = request.getContextPath();
        
        if ((model_file == null) || (test_file == null)) { 
       //  if (test_file == null) {
           out.println("<table>\n");
           out.println("<td width=\"300\" valign=\"top\"></td>\n");
           out.println("<td align=\"center\" valign=\"top\">\n");
           out.println("<h2 align=\"center\" style = \"font-family:Verdana\"><span style=\"font-weight: bold; color:#C0C0C0\">Test Model Web Service</span></h2><br/><br/>");        
           out.println("<form action=\"" + "\" method=\"POST\">\n");
           out.println("<div align=\"center\">Please enter the following information:</div><br/>");
           out.println("<div align=\"center\">provide a full path to a trained model</div><br/>");
           out.println("<input type=\"text\" name=\"modelFile\" align=\"center\"><br/>");
           out.println("<br/><div align=\"center\">test data</div><br/>");
           //out.println("<input type=\"text\" name=\"inputData\" align=\"center\"><br/>");
           out.println("<TEXTAREA name=\"inputData\" cols=\"60\" rows=\"16\">" + outp + "</TEXTAREA><br/><br/>");
           out.println("<br/><input type=\"submit\" + name=\"button\" align=\"center\">\n");
           out.println("</form>");
           out.println("<//td>\n");
           out.println("<//table>\n");
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
        return "This is a TestModel client which returns an annotated data set used as input data (in XML format)";
    }
    // </editor-fold>
}


    
    
