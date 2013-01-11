/*
 * LearnModelServlet.java
 *
 * Created on 20 March 2006, 22:09
 */

package katrenko.ws;

import java.io.*;
import java.net.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Calls LearnModel web service which returns 10-fold cross-validation results on the input data
 * and stores a model and its log file in the folder provided by a user.
 * To run this client, use <code>http://localhost:8080/LearnModel/learn</code>
 * <p>
 * Note: do not forget to check whether Tomcat uses the same port as in 
 * <code>String endpoint = "http://localhost:8080/axis/services/LearnModel";</code>. 
 * Change it if needed.
 * <p>
 * @author Sophia Katrenko
 * @version 1.0
 */
public class LearnModelServlet extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        doGet(request,  response);
        
        String[] con = new String[1];
        con = request.getParameterValues("context");
        String[] train_file = new String[1];
        train_file = request.getParameterValues("train_file");
        String[] model = new String[1];
        model = request.getParameterValues("model");
        
        String ret=new String();ret = "<doc>Please check your training data set once again!</doc>";
         try {
             String endpoint = "http://localhost:8080/axis/services/LearnModel";
  
             Service  service = new Service();
             Call     call    = (Call) service.createCall();
  
             call.setTargetEndpointAddress(endpoint);
             call.setOperationName("train_model");
         //    ret = (String) call.invoke( new Object[] { "C://Documents and Settings/Sophijka/Data/train/corpus2.xml", "3", "a", "C://Documents and Settings/Sophijka/Data/train/corpus2.mod", "ds" } );       
             ret = (String) call.invoke( new Object[] { train_file[0], con[0], "a", model[0], "text" } );       
          // out.println("<p>Results: " + ret + "</p>");
         } catch (Exception e) {
             System.err.println(e.toString()); 
         } 
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
        String outp = new String();outp = "";
        String contents = "";
        String line;

  /*      try{
            BufferedReader inputData = new BufferedReader(new FileReader("../../../../Data/corpus2_partb.xml"));
            while ( (line = inputData.readLine()) != null)
             {
               outp = new StringBuffer (outp).append(line).toString();
             }
        }catch(Exception ie){System.out.println("Can't read demo file");}
 */
        synchronized(this)
{
        out.println("<html>");
	out.println("<head>");
	out.println("<title>Learn Model</title>");
	out.println("</head>");
         out.println("<body bgcolor=\"#808080\" text=\"000000\">");
         //out.println("<p><img src=\"http://pc-swi18.science.uva.nl:8084/axis/temp/vle1.jpg\"/></p>");
        out.println("<h2 align=\"center\" style = \"font-family:Verdana\"><span style=\"font-weight: bold; color:#C0C0C0\">Learn Model Web Service</span></h2><br/><br/>");
        String context = new String();
        context = request.getParameter("context");
        String train_file = new String();
        train_file = request.getParameter("train_file"); 
        String model = new String();
        model = request.getParameter("model"); 
         if ((train_file == null) || (context == null)  || (model == null)){ 
           out.println("<form action=\"" + "\" method=\"POST\">\n");
           out.println("<h4>Please enter the following information:</h4>");
           out.println("<h4><br/>Context - n positions to the left and to the right(e.g., 2)</h4>");
           out.println("<input type=\"text\" name=\"context\"><br/>");
           out.println("<h4><br/>Store a model in (give a full path)</h4>");
           out.println("<input type=\"text\" name=\"model\"><br/>");
	   out.println("<h4>Please enter your annotated training set below (as shown below)</h4>");
           out.println("<h5>(Do not forget to add \"document\" tag!)</h5>");
           out.println("<TEXTAREA name=\"train_file\" cols=\"90\" rows=\"20\">" + "<document>TI - Expression pattern and further characterization of human <protein>MAGED2</protein> and identification of rodent orthologues. AB  - In a search for genes involved in X-linked mental retardation we have analyzed the expression pattern and genomic structure of human <protein>MAGED2</protein>. This gene is a member of a new defined <protein>MAGE-D</protein> cluster in Xp11.2, a hot spot for X-linked mental retardation. Rat and mouse orthologues have been isolated. In contrast to the genes of the <protein>MAGE-A</protein>, <protein>MAGE-B</protein> and <protein>MAGE-C</protein> clusters, <protein>MAGED2</protein> is expressed ubiquitously. High expression was detected in specific brain regions and in the interstitium of testes. Five SNPs in the coding region of human <protein>MAGED2</protein> were characterized and their allele frequencies determined in a German and Turkish population.</document>" + "</TEXTAREA><br/><br/>");
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
