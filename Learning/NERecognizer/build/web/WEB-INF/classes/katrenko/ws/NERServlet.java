/*
 * NERServlet.java
 *
 * Created on 21 March 2006, 17:12
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
import katrenko.ws.NERServlet;

import org.vle.aid.common.Properties;

/**
 * NERServlet is a client for NERService. To invoke the web service, make sure that
 * the port number in <code> String endpoint = "http://localhost/axis/services/NERecognizerService";</code>
 * is set accordingly. Change it if needed.
 * <p>
 * To run this client, use <code>http://localhost/NERecognizerService/NERServlet</code>
 * <p>
 * @author Sophia Katrenko
 * @version 1.0
 */
public class NERServlet extends HttpServlet {
    
   
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");        
        PrintWriter out = response.getWriter();
        synchronized(this)
{
        out.println("<html>");
	out.println("<head>");
	out.println("<title>Named Entity Recognition</title>");
	out.println("</head>");
	out.println("<body bgcolor=\"#8080\" text=\"000000\">");
        // out.println("<p><img src=\"http://pc-swi18.science.uva.nl:8084/axis/temp/vle1.jpg\"/></p>");
        out.println("<h2 align=\"center\" style = \"font-family:Verdana\"><span style=\"font-weight: bold; color:#C0C0C0\">Named Entity Recognition</span></h2><br/><br/>");
        String data=new String();
        data = request.getParameter("data");  
        String model=new String();
        model = request.getParameter("model");
        String inp_type=new String();
        inp_type = request.getParameter("inp_type");  
        String out_type=new String();
        out_type = request.getParameter("out_type");
        if (data == null) { 
            out.println("<form action=\"" + "\" method=\"POST\">\n");
           out.println("<h4>Please enter your text below</h4>");
           out.println("<TEXTAREA name=\"data\" cols=\"60\" rows=\"16\">LVIV [Lviv] , Rus. Lvov, Pol. Lwów, Ger. Lemberg, city (1989 pop. 791,000), capital of Lviv region, W Ukraine, at the watershed of the Western Bug and Dniester rivers and in the northern foothills of the Carpathian Mts. The chief city of W Ukraine, Lviv is a major rail and highway junction and an industrial and commercial center. Machine building, food processing, and the manufacture of chemicals and pharmaceuticals, motor vehicles, and textiles are the leading industries. Lviv is also an educational and cultural center, with a famous university (est. 1661) and several institutes of the Ukrainian Academy of Sciences. Landmarks include a 16th-century palace and two 14th-century cathedrals. Founded c.1256 by Prince Daniel of Halych, the city was named for his son Lev and developed as a great commercial center on the trade route from Vienna to Kiev.</TEXTAREA><br/>");
           out.println("<h4>Choose the NER model, please</h4>");
           out.println("<select id=\"model\" name=\"model\">");
           out.println("<option value=\"News\">News</option>");
           out.println("<option value=\"Medline\">Medline</option>");
	   //out.println("<option value=\"Genomics\">Genomics</option>");
	   //out.println("<option value=\"BioCreative\">BioCreative</option>");
           out.println("</select>");
           out.println("<h4>Type of input</h4>");
           out.println("<select id=\"inp_type\" name=\"inp_type\">");
           out.println("<option value=\"text\">Text</option>");
           out.println("<option value=\"lucene\">Lucene</option>");
           out.println("</select>");
           out.println("<h4>Output type</h4>");
           out.println("<select id=\"out_type\" name=\"out_type\">");
           out.println("<option value=\"annotation\">Annotation</option>");
           out.println("<option value=\"NElist\">NE list</option>");
           //out.println("<option value=\"N3\">Repository (N3)</option>");
           out.println("</select>");
           
           out.println("<br/><br/><input type=\"submit\" + name=\"button\">\n");
           out.println("</form>");
        }
        out.println("</body>");
        out.println("</html>");
 
    } 
    }
    
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        doGet(request,  response);

        String[] dat = new String[1];
        dat = request.getParameterValues("data");
      //  System.out.println(dat[0]);     
        String[] mod = new String[1];
        mod = request.getParameterValues("model");
        String[] inp = new String[1];
        inp = request.getParameterValues("inp_type");
        String[] outp = new String[1];
        outp = request.getParameterValues("out_type");
        String ret=new String();ret="<message>Check your input please!</message>";
        
         try {
             String endpoint = Properties.Entries.AXIS_ENDPOINT.get() + 
                      "/services/NERecognizerService";
  
             Service  service = new Service();
             Call     call    = (Call) service.createCall();
  
             call.setTargetEndpointAddress(endpoint);
             call.setOperationName("NErecognize");
	   //  BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("/home/sophijka/Wageningen/2-9.wur")));
	//	String bigStr=new String();
	//	System.out.println("Trying to read the test file into a big string");
	//	String tmp = br.readLine();
	//	while(tmp!=null){
        //		bigStr = bigStr + " " + tmp;
        //		tmp = br.readLine();
	//	}
	//	br.close();

	//	dat[0] = bigStr;
 		ret = (String) call.invoke( new Object[] { dat[0], mod[0], inp[0], outp[0] } );
             //ret = (String) call.invoke( new Object[] { dat[0], mod[0], inp[0], outp[0] } );
             System.out.println(ret);     
         } catch (Exception e) {
             System.err.println(e.toString());
         } 
       
request.setAttribute("output",ret);
RequestDispatcher dispatch=request.getRequestDispatcher("/DataToXML");
dispatch.forward(request,response);       
    }
    
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "This is a client for the NERService which outputs either the list of named entities found in the" +
                "input data or annotated input";
    }
    // </editor-fold>
}
