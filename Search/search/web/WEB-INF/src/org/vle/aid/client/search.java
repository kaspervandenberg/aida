/*
 * search.java
 *
 * TODO: Read Index configfile for comments and fields and Analyzers
 * TODO: Make submit and clear images
 * TODO: Properly handle Exceptions
 * TODO: get analzer from index-configfile
 *
 */

package org.vle.aid.client;


import java.io.*;
import java.util.HashSet;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryParser.ParseException;

//import java.util.HashMap;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 *
 * @author Edgar Meij
 */
public class search extends HttpServlet {
    
    int                 count;
    String[]            indexes;
    String[]            fields;
    int[]               maxHitsOptions = {10,20,50,100};
    private Analyzer    analyzer;
    private String      indexLocation;
    private Query       luceneQuery;
    
    private static final String endpoint =
        "http://localhost/axis/services/";
    
        /** logger for Commons logging. */
    private transient Logger log =
	 Logger.getLogger("search.class.getName()");
    
    public void init(ServletConfig config) throws ServletException {
        
        super.init(config);
        
        log.fine("Checking for env. var. INDEXDIR");
        indexLocation = System.getenv("INDEXDIR");
        
        if (indexLocation == null)
            log.severe("***INDEXDIR not found!!!***");
        
        try {
            Service  service = new Service();
            service.setMaintainSession(false);
            Call call    = (Call) service.createCall();    
            
            
            call.setTargetEndpointAddress(endpoint + "getIndexes");
            call.setOperationName("listIndexes");
            indexes = (String[]) call.invoke( new Object[] { null } );
         } catch (Exception e) {
            log.info(e.toString());
         }
    }
    
    public void destroy() {
        // History?
        //saveState();
    }
    
    /**
     * Respond to a GET request for the content produced by
     * this servlet.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are producing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
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
    
    @SuppressWarnings("unchecked")
    public void processRequest(HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException {
        
        // First of all, make sure the indexes can be found:
        if (indexLocation == null || indexLocation.equalsIgnoreCase("") || indexes == null || indexes.length == 0) {
            res.sendError(500, "No indexes found in " + indexLocation + ", make sure you have you INDEXDIR variable set to the right location");
            log.severe("No indexes found.");
        } else {
          
        try {
            Service  service = new Service();
            service.setMaintainSession(false);
            Call call    = (Call) service.createCall();    
            
            
            call.setTargetEndpointAddress(endpoint + "getIndexes");
            call.setOperationName("listIndexes");
            indexes = (String[]) call.invoke( new Object[] { null } );
         } catch (Exception e) {
            log.info(e.toString());
         }
        
            // Get project name for relative css, javascript, images, etc.
            String root  = req.getContextPath();

            HttpSession session = req.getSession(true);
            // Time-out after 10 minutes
            session.setMaxInactiveInterval(600);

            String invalidate = req.getParameter("invalidate");

            if (invalidate != null) {

                Enumeration atts = session.getAttributeNames();
                while(atts != null && atts.hasMoreElements()) { 
                    String attName = (String) atts.nextElement(); 
                    session.removeAttribute(attName);
                }
                res.sendRedirect(req.getRequestURI());
            }

            String index, maxHitsString, searchButton;
            String[] selectedFields = null;
            int maxHits;

            searchButton = req.getParameter("searchButton");

            // First get settings from the form
            // If not defined from a stored session
            index = req.getParameter("index");

            // Not submitted
            if (index == null)
                index = (String) session.getAttribute("index");

            // Then store it in the session
            if (index != null) {
                session.setAttribute("index", index);
            } else {
                // Default
                index = indexes[0];
            }

            // Same thing for maxHits
            maxHitsString = req.getParameter("maxHitsString");        

            if (maxHitsString == null)
                maxHitsString = (String) session.getAttribute("maxHitsString");

            if (maxHitsString != null)
                session.setAttribute("maxHitsString", maxHitsString);

            try {
                maxHits = Integer.parseInt(maxHitsString);
            } catch (NumberFormatException e) { 
                // Defaults to 10
                maxHits = 10;
                //log.info("NumberFormatException: " + e);
            }

            // And for field(s)
            selectedFields = req.getParameterValues("selectedFields");      

            if (selectedFields == null) {
                selectedFields = (String[]) session.getAttribute("selectedFields");
            }

            if (selectedFields != null) {
                session.setAttribute("selectedFields", selectedFields);        
            }

            // And query
            String query = req.getParameter("query");
            
            String newTerm = req.getParameter("addTerm");
            
            if (newTerm != null) {
                if (query == null) 
                    query = newTerm;
                else
                    query += " " + newTerm;
            }
            
            if (query != null)
                session.setAttribute("query", query);        
            else 
                query = "";

            String operator = req.getParameter("operator");
            if (operator != null)
                session.setAttribute("operator", operator);
            else 
                operator = "or";

            // Let's begin
            res.setContentType("text/html;charset=UTF-8");
            PrintWriter out = res.getWriter();        
            
            out.println("<!DOCTYPE HTML PUBLIC "+
                    "\"-//W3C//DTD HTML 4.01 Transitional//EN\" "+
                    "\"http://www.w3.org/TR/html4/loose.dtd\">");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>AID Search interface</title>");
            out.println("<style type='text/css'>@import url(" + root + "/css/qckcss.css);");
            out.println("</style>");
            out.println("<link REL=\"SHORTCUT ICON\" HREF=\""+root+"/images/favicon.ico\">");
            out.println("<script type=\"text/javascript\" src=\"" + root + "/javascript/overlib.js\">");
            out.println("<!-- bla -->");
            out.println("</script>");        

			out.println("<script type=\"text/javascript\" src=\"" + root + "/javascript/catchEnter.js\">");
			out.println("<!-- bla -->");
			out.println("</script>");
									
            out.println("</head>");
            
            // User has clicked on the submit button
            if (searchButton != null) {

                // Check for omissions
                if (query == null || query.equalsIgnoreCase("")) {
                    // Pop-up javascript and return to search servlet
                                out.println("<body bgcolor=white background=\""+root+"/images/background.gif\"  OnLoad=\"javascript:alert('Please enter a query.');document.selectionForm.query.focus();\">");
                } else if (selectedFields == null || selectedFields.length == 0) {
                    // Pop-up javascript and return to search servlet
                                out.println("<body bgcolor=white background=\""+root+"/images/background.gif\"  OnLoad=\"javascript:alert('Please select at least one field.');document.selectionForm.query.focus();\">");
                } else {
                                
                    File stopwordsFile = 
                            new File(indexLocation + System.getProperty("file.separator") + 
                            "stopwords.txt");

                    if (stopwordsFile == null || !stopwordsFile.exists() || !stopwordsFile.canRead()) {
                        // throw new IllegalArgumentException("can't read stopword file " + stopwords);
                        log.info("Can't read default stopwords file: " + stopwordsFile.toString());
                        analyzer = new StandardAnalyzer();
                    } else {
                        // No 1.5 on Mac os x...
                        // HashSet <String> stopSet = new HashSet <String> ();
                        HashSet stopSet = new HashSet();
                        stopSet = WordlistLoader.getWordSet(stopwordsFile);

                        String[] stopwords = new String[stopSet.size()];
                        stopSet.toArray(stopwords);

                        analyzer = new StandardAnalyzer(stopwords);                                       
                    }

                    // TODO: get analzer from index-configfile
                    MultiFieldQueryParser mfqParser = 
                            new MultiFieldQueryParser(selectedFields, analyzer);

                    if (operator.equalsIgnoreCase("or"))
                        mfqParser.setDefaultOperator(MultiFieldQueryParser.OR_OPERATOR);
                    else
                        mfqParser.setDefaultOperator(MultiFieldQueryParser.AND_OPERATOR);

                    // Not thread-safe!
                    // So doing it here, instead of result.java
                    try {
                        luceneQuery = mfqParser.parse(MultiFieldQueryParser.escape(query));
                    } catch (ParseException e) {
                        log.severe(e.toString());
                        // Return to user as well
                        // javascript:alert();
                    }

                    // Good to go, so
                    // Store all chosen variables in session

                    session.setAttribute("luceneQuery", luceneQuery);
                    session.setAttribute("allFields", fields);
                    session.setAttribute("query", query);            

                    // Then redirect to processing page
                    res.sendRedirect(res.encodeURL(root.concat("/result?").concat(req.getQueryString())));
                }
            } else { // User did *not* press submit
                out.println("<body bgcolor=white background=\""+root+"/images/background.gif\"  OnLoad=\"javascript:document.selectionForm.query.focus();\">");
            }
                
            if (index != null) {
                try {
                    Service  service = new Service();
                    Call call    = (Call) service.createCall();    

                    call.setTargetEndpointAddress(endpoint + "getFields");
                    call.setOperationName("listFields");     
					fields = (String[]) call.invoke( new Object[] { index } );

                } catch (Exception e) {
                    log.info(e.toString());
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);

                    log.severe(sw.toString());
                    pw.close();                
                }
            }



            // The actual page
            out.println("<div id=\"AIDfp\">");
            out.println("<a name=\"top\"/>");
            out.println("  <table border=\"0\" width=700>");

            // Header
            out.println("<tr><td colspan=2>");
            out.println("<table>");
            out.println("	<tr valign=\"top\" bgcolor=white><td class=\"navtext\">");
            out.println("	<div id=\"navlinks\">");
            out.println("		<img src=\""+root+"/images/top.png\" width=\"709\" height=\"200\" border=\"0\"><!-- bla --> </img>");

            out.println("		<div style=\"position: relative;top: -55px;margin-left: 15px;\"><a href=\""+root+"/\">Home</a></div>");
            out.println("		<div style=\"position: relative;top: -71px;margin-left: 180px;\">Concept Finder</div>");
            out.println("		<div style=\"position: relative;top: -87px;margin-left: 410px;\">Search Details</div>");
            out.println("		<div style=\"position: relative;top: -103px;margin-left: 620px;\">History</div>");
            out.println("</div></td></tr></table>");
            out.println("</td></tr>");

            //bgcolor="#324E81"
            // End header

            out.println("    <form name=\"selectionForm\" method=\"get\" " +
                    "action=\"" + res.encodeURL(req.getRequestURI()) + "\">");

            out.println("      <tr>");
            out.println("        <td colspan=2>");

            out.println("          <table width=700>");
            out.println("            <tr>");
            out.println("              <td width=85%>");
            out.println("          <a href=\"http://lucene.apache.org/java/docs/queryparsersyntax.html\" target=\"_blank\" onmouseover=\"return overlib('Lucene query syntax help.',TEXTFONT,'Verdana');\" onmouseout=\"return nd();\"><div id=\"popover\">Query:</div></a>");

            if (query == null) {
                out.println("          <input type=text name=query size=65/>");
            } else {
                out.println("          <input type=text name=query size=65 value=\"" + query + "\"/>");
            }

            out.println("        </td>");
            out.println("        <td width=15% class='resultItemRight'>");
            out.println("          MaxHits:");
            out.println("          <select name=\"maxHitsString\">");

            for (int k = 0; k < maxHitsOptions.length; ++k) {

                if (maxHitsString == null || maxHits != maxHitsOptions[k]) {
                    out.println("            <option>"+maxHitsOptions[k]+"</option>");                    
                } else {
                    out.println("            <option SELECTED>"+maxHitsOptions[k]+"</option>");
                }
            }

            out.println("          </select>"); 
            out.println("              </td>");
            out.println("            </tr>");
            out.println("          </table>");
            out.println("        </td>");
            out.println("      </tr>");     

            out.println("      <tr>");
            out.println("        <td colspan=2>");
            out.println("          <table width=700>");
            out.println("            <tr>");
            out.println("              <td width=10%>");
            out.println("                Operator:");
            out.println("              </td>");
            out.println("              <td width=70%>");
            out.print("                <input type=radio name=\"operator\" value=\"OR\" id=\"operator_OR\" ");
            out.println(operator.equalsIgnoreCase("or") ? "CHECKED>" : ">");
            out.println("                  <label for=\"operator_OR\">");
            //out.println("                    <div id=\"popover\" onmouseover=\"return overlib('Match at least one of the query terms.',TEXTFONT,'Verdana');\" onmouseout=\"return nd();\">OR</div>");
            out.println("                    OR");
            out.println("                  </label>");            
            out.println("                </input>");             
            out.println("              </td>");
            out.println("              <td width=20% align='right' class='noLink'>");
            out.println("                <a href='" + root + "/testFinder'>Term Finder</a>");
			//out.println("                <a href=\"javascript:window.open('testFinder','Term Finder','toolbar=no,menubar=no,width=320,height=320,resizable=yes');\">Term Finder</a>");
            out.println("              </td>");            
            out.println("            </tr>");
            out.println("            <tr>");
            out.println("              <td width=10%>");
            out.println("              </td>");
            out.println("              <td width=70%>");
            out.print("                <input type=radio name=\"operator\" value=\"AND\" id=\"operator_AND\" ");
            out.println(operator.equalsIgnoreCase("and") ? "CHECKED>" : ">");
            out.println("                  <label for=\"operator_AND\">");
            //out.println("                    <div id=\"popover\" onmouseover=\"return overlib('Match all of the query terms.',TEXTFONT,'Verdana');\" onmouseout=\"return nd();\">AND</div>");
            out.println("                    AND");
            out.println("                  </label>");
            out.println("                </input>");            
            out.println("              </td>");    
            out.println("              <td width=20%>");
            out.println("                ");
            out.println("              </td>");     
            
            out.println("            </tr>");
            out.println("          </table>");
            out.println("        </td>");
            out.println("      </tr>");        

            out.println("      <tr>");
            out.println("        <td colspan=2>");
            out.println("          <img src=\"" + root + "/images/pixel.gif\" width=700 height=40/>");
            out.println("        </td>");
            out.println("      </tr>");  

            out.println("      <tr>");
            out.println("        <td colspan=2>");
            out.println("          <table width=700>");
            out.println("            <tr>");
            out.println("              <td width=350>");
            out.println("                Choose an index:");
            out.println("              </td>");
            out.println("              <td>");
            out.println("                <div id=\"popover\" onmouseover=\"return overlib('Use ctrl, shift or the Apple key to select multiple fields.',TEXTFONT,'Verdana');\" onmouseout=\"return nd();\">Choose fields:</div>  ");
            out.println("              </td>");
            out.println("            </tr>");          
            out.println("          </table>");
            out.println("        </td>");
            out.println("      </tr>");

            out.println("      <tr valign=\"top\">");
            out.println("        <td width=350>");
            out.println("          <table border=\"0\">");

            // Print out radio buttons for all indexes
            java.util.Arrays.sort(indexes);
            for (int k = 0; k < indexes.length; ++k) {
                out.println("        <tr>");
                out.println("          <td>");

                if (index == null || !index.equals(indexes[k])) {

                    // Default start
                    if (k==0 && !index.equals(indexes[k])) {
                        out.println("                <input type=radio "+
                            "onclick=\"document.selectionForm.submit();\" "+
                            "name=\"index\" CHECKED value=\"" + 
                            indexes[k] + "\" id=\"index" + k + 
                            "\"><label for=\"index" + k + "\">" + indexes[k] + 
                            "</label></input>");
                    } else {
                        out.println("                <input type=radio "+
                            "onclick=\"document.selectionForm.submit();\" "+
                            "name=\"index\" value=\"" + 
                            indexes[k] + "\" id=\"index" + k + 
                            "\"><label for=\"index" + k + "\">" + indexes[k] + 
                            "</label></input>");
                    }
                } else {
                    out.println("                <input type=radio "+
                        "onclick=\"document.selectionForm.submit();\" "+
                        "name=\"index\" CHECKED value=\"" + 
                        indexes[k] + "\" id=\"index" + k + 
                        "\"><label for=\"index" + k + "\">" + indexes[k] + 
                        "</label></input>");
                }
                out.println("          </td>");
                out.println("        </tr>");
            }
            out.println("          </table>");
            out.println("        </td>");
            out.println("        <td>");
            out.println("          <select name=\"selectedFields\" multiple onKeyPress=\"checkEnter(event)\" onchange=\"document.selectionForm.submit();\">");

            if (fields != null){
                for (int k = 0; k < fields.length; ++k) {

                    boolean foundIt = false;

                    if (selectedFields != null) {
                        for (int j = 0; j < selectedFields.length; ++j) {
                            if (selectedFields[j].equalsIgnoreCase(fields[k])) {
                                out.println("            <option SELECTED value=\"" + fields[k] + "\">" +
                                            fields[k] +
                                            "        </option>");
                                foundIt = true;
                            }
                        }
                    }

                    if (!foundIt) {
                        out.println("            <option value=\"" + fields[k] + "\">" +
                                    fields[k] +
                                    "        </option>");
                    }
                }
            }

            out.println("          </select>");            

            out.println("        </td>");         
            out.println("      <tr>");
            out.println("        <td width=100% colspan=2 class='resultItemRight'>");
            out.println("          <input type=submit name=\"searchButton\" value=\"Search\">");
            out.println("        </td>");
            out.println("      </tr>");       

            out.println("      <tr>");
            out.println("        <td colspan=2 align=right class='noLink'>");
            out.println("          <a href=\"" + root + "/AID?invalidate=true\"><div id=\"clear\">Clear</div></a>");
            out.println("        </td>");
            out.println("      </tr>");       
            
            out.println("      <tr>");
            out.println("        <td colspan=2>");

            out.println("        </td>");
            out.println("      </tr>");
            out.println("    </form>");
            out.println("  </table>");

            //Footer
            out.println("<div id=\"footer\">");
            out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
            out.println("<tr>");
                out.println("<td width=\"1%\"><img src=\""+root+"/images/footer-leftcurve.gif\" width=\"10\" height=\"31\" border=\"0\"/></td>");
                out.println("<td width=\"98%\" bgcolor=\"#\" class=\"footertext\">");
                    out.println("<a href=\"#top\">Top</a>");
                    out.println("        |		");
                    out.println("<a href=\""+root+"/synonym\">Synonym client</a>");
                    out.println("        |		");
                    out.println("<a href=\"http://www.vl-e.nl\">Vl-e</a>");
                out.println("</td>");
                out.println("<td width=\"1%\"><img src=\""+root+"/images/footer-rightcurve.gif\" width=\"10\" height=\"31\" border=\"0\"/></td>");
            out.println("</tr>");
            out.println("</table>");
            out.println("</div>");
            // End footer

            out.println("</div>");        
            out.println("</body>");
            out.println("</html>");
        }
    }
}
