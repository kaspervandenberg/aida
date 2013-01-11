/*
 * applyCRFServlet.java
 *
 * Created on понеділок, 4, лютого 2008, 18:44
 */

package katrenko.ws;

import java.io.*;
import java.net.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import java.lang.Object.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.IOException;
import java.rmi.RemoteException;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.ServletException;
import javax.xml.rpc.ServiceException;
import java.util.logging.Logger;

/*import java.io.File;
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
*/

/**
 *
 * @author Admin
 * @version
 */
public class applyCRFServlet extends HttpServlet {
    
    Service service;
    Call        call;
    String      endpoint = "http://localhost:8080/axis/services/";       
    private transient Logger log =
 	            Logger.getLogger(applyCRFServlet.class.getName());
     
    
    
        
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    public void init(ServletConfig config) throws ServletException {
  
        
//        super.init(config);
    
    //String initial = config.getInitParameter("initial");
    
   /*  model[0] = config.getInitParameter("model");
     test[0] = config.getInitParameter("test");
     outputMode[0] = config.getInitParameter("outputMode");
    */
/*    try {
        Service  service = new Service();
        service.setMaintainSession(false);
        Call call    = (Call) service.createCall();   
 	           
        call.setTargetEndpointAddress(endpoint + "getIndexes");
        call.setOperationName("listIndexes");
        indexes = (String[]) call.invoke( new Object[] { null } );
    }
    catch (Exception e) {
      System.out.println("wi-wi-wi");
    }
 */
  }
    
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        //
          
        /*model = request.getParameter("model");
        test = request.getParameter("test");
        outputMode = request.getParameter("outputMode");
        */
           
        doGet(request,  response);
      //  HttpSession session = request.getSession(true);
      //  session.setMaxInactiveInterval(600);
   
         /*String model;
         String test;
         String outputMode;
   */
        
        /*     
        if (model == null)
            model = (String) session.getAttribute("model");

        if (test == null)
            test = (String) session.getAttribute("test");

        if (outputMode == null)
            outputMode = (String) session.getAttribute("outputMode");
     */   
      //  response.setContentType("text/html;charset=UTF-8");
      //  PrintWriter out = response.getWriter();
        String ret = new String();
        String[] test = new String[1];
        test = request.getParameterValues("test");
        
        String[] model = new String[1];
        model = request.getParameterValues("model");
        
        String[] outputMode = new String[1];
        outputMode = request.getParameterValues("outputMode");
        
        /*
        session.setAttribute("model", model);
        session.setAttribute("test", test);
        session.setAttribute("outputMode", outputMode);
        */
        
        service = new Service();
        try{
            call = (Call) service.createCall();            
        }catch (ServiceException e){log.severe(e.toString());}
        
       
   /*     response.setContentType("text/html;charset=UTF-8");    
        out.println("<html>");
	out.println("<head>");
	out.println("<title>CRF Web Service</title>");
	out.println("</head>");
        out.println("<body bgcolor=\"#808080\" text=\"000000\">");
        out.println("<h2 align=\"center\" style = \"font-family:Verdana\"><span style=\"font-weight: bold; color:#C0C0C0\">Test Model Web Service</span></h2><br/><br/>");
        String outp = new String();outp = "We have identified a transcriptional repressor, Nrg1, in a genetic screen designed to reveal negative factors involved in the expression of STA1, which encodes a glucoamylase. The NRG1 gene encodes a 25-kDa C2H2 zinc finger protein which specifically binds to two regions in the upstream activation sequence of the STA1 gene, as judged by gel retardation and DNase I footprinting analyses. Disruption of the NRG1 gene causes a fivefold increase in the level of the STA1 transcript in the presence of glucose.";
        
        if ((model == null) || (test == null) || (outputMode == null)) { 
       
           out.println("<form action=\"" + "\" method=\"GET\">\n");
           out.println("<h4>Please enter the following information:</h4>");
           out.println("<h4>choose the trained model</h4>");
           out.println("<select id=\"model\" name=\"model\">");
           out.println("<option value=\"1\">BioCreative I</option>");
           out.println("<option value=\"0\">NLPBA model</option>");
           out.println("</select>");
           out.println("<h4>enter your test data below</h4>");
           out.println("<TEXTAREA name=\"test\" cols=\"60\" rows=\"16\">" + outp + "</TEXTAREA><br/><br/>");
           out.println("<h4>choose an output type</h4>");
           out.println("<select id=\"ouputMode\" name=\"outputMode\">");
           out.println("<option value=\"3\">a list of entities</option>");
           out.println("<option value=\"2\">SGML</option>");
           out.println("<option value=\"1\">IOB format</option>");
           out.println("<option value=\"4\">ABNER format</option>");
           out.println("</select>");
           out.println("<input type=\"submit\" + name=\"button\">\n");
           out.println("</form>");
        }
        
        if ((model != null) || (test != null) || (outputMode != null)) { 
            
 	            response.setContentType("text/html;charset=UTF-8");
 	*/           
 	            try {             
 	                call.setTargetEndpointAddress(endpoint + "CRFapply");
 	                call.setOperationName("apply");
 	                ret = (String) call.invoke( new Object[] { test[0], model[0], outputMode[0], "1"} );
 	                System.out.println(ret);
 	               
 	            } catch (RemoteException e) {
 	                log.severe("RemoteException: " + e.toString());
 	               
 	                StringWriter sw = new StringWriter();
 	                PrintWriter pw = new PrintWriter(sw);
 	                e.printStackTrace(pw); 	   
 	                log.severe(sw.toString());
 	            }
 	           
 	         
                
      /*  
        out.println("</body>");
        out.println("</html>");
            
 
        System.out.println("TEST " + test);
        System.out.println("MODEL " + model);
        System.out.println("outputMode " + outputMode);
 
        out.close();
       */ 
        request.setAttribute("outp",ret);
        RequestDispatcher dispatch=request.getRequestDispatcher("/output");
        dispatch.forward(request,response);
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        //processRequest(request, response);
        response.setContentType("text/html;charset=UTF-8");        
        PrintWriter out = response.getWriter();
        
        synchronized(this)
{
        String outp = new String(); outp = "We have identified a transcriptional repressor, Nrg1, in a genetic screen designed to reveal negative factors involved in the expression of STA1, which encodes a glucoamylase. The NRG1 gene encodes a 25-kDa C2H2 zinc finger protein which specifically binds to two regions in the upstream activation sequence of the STA1 gene, as judged by gel retardation and DNase I footprinting analyses. Disruption of the NRG1 gene causes a fivefold increase in the level of the STA1 transcript in the presence of glucose.";
        String contents = "";
        /*String line;
            try{
            BufferedReader inputData = new BufferedReader(new FileReader("/home/sophijka/WebServices/TestModelService/Data/totest.xml"));
            while ( (line = inputData.readLine()) != null)
             {
               outp = new StringBuffer (outp).append(line).toString();
             }
        }catch(Exception ie){System.out.println("Can't read demo file");}
  */
        out.println("<html>");
	out.println("<head>");
	out.println("<title>CRF Web Service</title>");
	out.println("</head>");
        out.println("<body bgcolor=\"#FFFFFF\" text=\"000000\">");
        out.println("<h2 align=\"center\" style = \"font-family:Verdana\"><span style=\"font-weight: bold; color:#C0C0C0\">Test Model Web Service</span></h2><br/><br/>");
        String model = new String();
        model = request.getParameter("model"); 
        String test = new String();
        test = request.getParameter("test");
        String outputMode = new String();
        outputMode = request.getParameter("outputMode");
        
        if ((model == null) || (test == null) || (outputMode == null)) { 
       //  if (test_file == null) { 
           out.println("<form action=\"" + "\" method=\"POST\">\n");
           out.println("<h4>Please enter the following information:</h4>");
           out.println("<h4>choose the trained model</h4>");
           out.println("<select id=\"model\" name=\"model\">");
           out.println("<option value=\"1\">BioCreative I</option>");
           //out.println("<option value=\"0\">NLPBA model</option>");
           out.println("</select>");
           out.println("<h4>enter your test data below</h4>");
           out.println("<TEXTAREA name=\"test\" cols=\"60\" rows=\"16\">" + outp + "</TEXTAREA><br/><br/>");
           out.println("<h4>choose an output type</h4>");
           out.println("<select id=\"ouputMode\" name=\"outputMode\">");
           out.println("<option value=\"3\">a list of entities</option>");
           out.println("<option value=\"2\">SGML</option>");
           out.println("<option value=\"1\">IOB format</option>");
           out.println("<option value=\"4\">ABNER format</option>");
           out.println("</select>");
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
      /*  response.setContentType("text/html;charset=UTF-8");    

        PrintWriter out = response.getWriter();
        
        out.println("<html>");
	out.println("<head>");
	out.println("<title>CRF Web Service</title>");
	out.println("</head>");
        out.println("<body>");
        out.println("<TEXTAREA name=\"test_file\" cols=\"60\" rows=\"16\">" + ret + "</TEXTAREA><br/><br/>");           
        out.println("</body>");
        out.println("</html>");*/
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
