/*
 * result.java
 * 
 * TODO: Single thread model?
 * TODO: Make it smarter, so it recognizes \", \', ...
 * 
 */
 
package org.vle.aid.client;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import java.util.logging.Logger;
import java.util.*;
import java.io.*;

import org.vle.aid.*;
import org.vle.aid.lucene.tools.Tokenizer;

import org.apache.xmlbeans.*;

import org.apache.lucene.search.Query;

import org.vle.aid.common.Properties;

/**
 *
 * @author Edgar Meij
 */
public final class result extends HttpServlet {
    
    private         Service     service;
    private         Call        call;
    private final   String      endpoint =
				    Properties.Entries.AXIS_ENDPOINT.get() +
				    "/services/";
    private final   String      root = "/search";
    private         String      queryExpansionURL;
    private         String      spellSuggestURL;
    private         String      msg;
    private         XmlOptions  xmlOpts = new XmlOptions();
    private         ResultDocument resultDoc;
    
    /** logger for Commons logging. */
    private transient Logger log =
   Logger.getLogger("ProcessServlet.class.getName()");    
    
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

    public class MyResult implements Runnable {

        /** the percentage that is finished */
        int n = 0;
        Date start = new Date();
        Date end;
        
        boolean retrievedExpansions = false;
        boolean retrievedSpellSuggestions = false;
        
        // Stuff from search interface
        Query LuceneQuery = null;
        String query = "";
        String MaxHitsString = "";
        String index = "";
        String[] selectedFields = null;
        String operator = "or";        
        String errorMsg = "";
        
        String retXML = null;
        
        String[] retSyns = null;
        String retSpellSuggestion = null;
        String[] retWordnetSyns = null;       
        String[] retOnlineSyns = null;
        
        String[] termsInQuery = null;
        String[][] synonymMatrix = null;
        String[] spellMatrix = null;
        String[][] wordnetMatrix = null;         
        String[][] onlineSynMatrix = null;
        
        public void run() {  
            
            termsInQuery       = Tokenizer.wordsToArray(query.replaceAll("\\?",""));
            synonymMatrix      = new String[termsInQuery.length][];
            spellMatrix        = new String[termsInQuery.length];
            wordnetMatrix      = new String[termsInQuery.length][]; 
            onlineSynMatrix    = new String[termsInQuery.length][]; 
        
            try {
                service = new Service();
                call    = (Call) service.createCall();    
                
                for (int i=0 ; i<termsInQuery.length ; ++i) {
                    // Step 1
                    // Get synonyms
                    call.setTargetEndpointAddress(endpoint + "synonym");
                    call.setOperationName("getSynonyms");

                    retSyns = 
                            (String[]) call.invoke( new Object[] { index, termsInQuery[i] } );
                            
                    if(retSyns != null) {
                        retrievedExpansions = true;
                        log.fine("retrievedExpansions found");
                        synonymMatrix[i] = new String[retSyns.length];
                        for(int j = 0; j < synonymMatrix[i].length; j++)
                            synonymMatrix[i][j] = retSyns[j];
                    } else {
                        log.fine("retrievedExpansions NOT found");
                        synonymMatrix[i] = new String[1];
                        synonymMatrix[i][0] = null;
                    }     
                }
                
                n = 25;
                
                for (int i=0 ; i<termsInQuery.length ; ++i) {
                    // Step 2
                    // Get spellcheck
                    call.setTargetEndpointAddress(endpoint + "spellCheck");
                    call.setOperationName("didyoumean");

                    spellMatrix[i] = 
                        (String) call.invoke( new Object[] { index, termsInQuery[i] } );
                            
                    if (spellMatrix[i] != null)
                        retrievedSpellSuggestions = true;
                        
                    // TODO: Use positions
                        
                }
                
                n = 50;

                // Step 3
                // Get results XML
                call.setTargetEndpointAddress(endpoint + "SearcherWS");
                call.setOperationName("searchMFquery");
                
                retXML = 
                        (String) call.invoke( new Object[] { index, query, MaxHitsString, selectedFields, operator } );

                n = 75;
                
                for (int i=0 ; i<termsInQuery.length ; ++i) { 
                    // Step 4
                    // Get WordnetSyns
                    call.setTargetEndpointAddress(endpoint + "getWordnetSynWS");
                    call.setOperationName("getWordnetSynonyms");

                    retWordnetSyns = 
                        (String[]) call.invoke( new Object[] { termsInQuery[i]} );
                        
                    if(retWordnetSyns != null) {
                        retrievedExpansions = true;
                        
                        wordnetMatrix[i] = new String[retWordnetSyns.length];
                        
                        for(int j = 0; j < wordnetMatrix[i].length; j++)
                            wordnetMatrix[i][j] = retWordnetSyns[j];

                    } else {
                        wordnetMatrix[i] = new String[1];
                        wordnetMatrix[i][0] = null;
                    }

                    // Step 5
                    // Get online Acronyms
                    call.setTargetEndpointAddress(endpoint + "getOnlineAcronymsWS");
                    call.setOperationName("getOnlineAcronyms");

                    retOnlineSyns = 
                            null;
                        //(String[]) call.invoke( new Object[] { termsInQuery[i]} );
                        
                    if(retOnlineSyns != null) {
                        retrievedExpansions = true;
                        
                        onlineSynMatrix[i] = new String[retOnlineSyns.length];
                        
                        for(int j = 0; j < onlineSynMatrix[i].length; j++)
                            onlineSynMatrix[i][j] = retOnlineSyns[j];

                    } else {
                        onlineSynMatrix[i] = new String[1];
                        onlineSynMatrix[i][0] = null;
                    }
                }                
                
                n = 100;

        if (retXML == null) {
          errorMsg = "Search error. Is the query well formulated?";
          n = -1;
                }
        end = new Date();

            } catch (ServiceException e) {
                n = -1;
                log.severe("ServiceException: " + e.toString());
                
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
   
                errorMsg = sw.toString();
                pw.close();
            } catch (RemoteException e) {
                n = -1;
                log.severe("RemoteException: " + e.toString());
                
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
   
                errorMsg = sw.toString();
                pw.close();
            }
 
        }
        
        /**
         * Gets the current percentage of the file that is done.
         * @return a percentage or -1 if something went wrong.
         */
        public int getPercentage() {
            return n;
        }
        
        public void setQuery(Query inputQuery) {
            LuceneQuery = inputQuery;
        }
        
        public void setQuery(String inputQuery) {
            query = inputQuery;
        }
        
        public void setOperator(String inputOperator) {
            operator = inputOperator;
        }
                
        public void setIndex(String inputIndex) {
            index = inputIndex;
        }
        
        public void setMaxHits(String inputMaxHitsString) {
            MaxHitsString = inputMaxHitsString;
        }
        
        public void setFields(String[] inputSelectedFields) {
            selectedFields = inputSelectedFields;
        }
        
        public String getRetXML() {
            return retXML;
        }
        
        public String[] getTermsInQuery() {
            return termsInQuery;
        }
        
        public String[] getRetSyns() {
            return retSyns;
        }
        
        public String getRetSpellSuggestion() {
            return retSpellSuggestion;
        }
                
    }
    
    public void processRequest(HttpServletRequest request,
        HttpServletResponse response)
            throws IOException, ServletException {
      
        HttpSession session = null;
        Object resultSesionObject = null;
        
        try {
            session = request.getSession(true);
            
            // session.get's and check            
            if (session.isNew()) {
                // Redirect
                session.invalidate();
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                //PrintWriter out = response.getWriter(); 
                //out.println("<html><HEAD><META HTTP-EQUIV='Refresh' CONTENT='0; URL=/search/AID'/></HEAD></html>");
                response.sendRedirect(root + "/AID");
                return;
            }
            
            resultSesionObject = session.getAttribute("myResult");            
            
        } catch (IllegalStateException e) {
            response.sendRedirect(root + "/AID");
        }
        
        MyResult result;
        
        if (resultSesionObject == null) {
          result = new MyResult();
          session.setAttribute("myResult", result);
                
                // User clicked on spelling suggestion:
                if (request.getParameter("spell") == null) {
                    result.setQuery((String) session.getAttribute("query"));
                } else {
                    result.setQuery((String) request.getParameter("query"));
                }
                
                result.setQuery((Query) session.getAttribute("luceneQuery"));
                result.setIndex((String) session.getAttribute("index"));
                result.setMaxHits((String) session.getAttribute("maxHitsString"));
                result.setFields((String[]) session.getAttribute("selectedFields"));
                result.setOperator((String) session.getAttribute("operator"));
                
          Thread t = new Thread(result);
          t.start();
        } else {
          result = (MyResult) resultSesionObject;
        }        
        
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        
        switch (result.getPercentage()) {
          case -1:
            isError(result, response.getOutputStream());
            return;
          case 100:
                        queryExpansionURL = response.encodeURL(root.concat("/queryExpansion"));
                        spellSuggestURL = response.encodeURL(root.concat("/result?spell=true&query="));
            isFinished(result, response.getWriter(), session);
            return;
          default:
            isBusy(result, response.getOutputStream(), msg);
            return;
        }
        
    }
    /**
     * Sends an HTML page to the browser saying how many percent of the document is finished.
     * @param result  the class that holds the result
     * @param stream  the outputstream of the servlet
     * @param msg the status message
     * @throws IOException
     */
    private void isBusy(MyResult result, ServletOutputStream stream, String msg) throws IOException {
        stream.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        stream.print("<html>\n\t<head>\n\t\t<title>Please wait...</title>\n\t\t<meta http-equiv=\"Refresh\" content=\"2\">\n\t");
        stream.println("<link rel=\"stylesheet\" type=\"text/css\" href=\""+root+"/css/qckcss.css\"/>");
        stream.println("</head>");
        stream.println("<body bgcolor=white background=\""+root+"/images/background.gif\">");
        stream.println("<div id=\"AIDfp\">");
        
        // Header
        stream.println("  <table border=\"0\" width=700>");
        stream.println("  <tr valign=\"top\" bgcolor=white><td class=\"navtext\">");
        stream.println("    <img src=\""+root+"/images/top.png\" width=\"709\" height=\"200\" border=\"0\"><!-- bla --> </img>");
        stream.println("</td></tr></table>");
        stream.println("</td></tr>");

        //bgcolor="#324E81"
        // End header        

        stream.println("</div></td></tr></table>");
        
        //bgcolor="#324E81"
        // End header        
        stream.println("<table cellpadding=\"1\" cellspacing=\"1\" bgcolor=\"grey\" color=\"#324E81\" align=\"center\">");
        stream.println("<center>");
        stream.println("<tr bgcolor=\"white\"><td bgcolor=\"white\">");        
        stream.println("<span class=\"complete\">");
        //stream.println("<tr bgcolor=\"white\"><td bgcolor=\"white\">");
        //stream.println(String.valueOf(result.getPercentage()));
        //stream.println(msg);
        //stream.println("</td></tr>");
        
        int perc = result.getPercentage();
        
        perc /= 25;

        for (int i = 0; i < perc; ++i) {
            stream.println("&#9608;&#9608;&#9608;&#9608;&#9608");
        }
        
        stream.println("</span><span class=\"incomplete\">");

        for (int i = 0; i < 4-perc; ++i) {
            stream.println("&#9608;&#9608;&#9608;&#9608;&#9608");
        }
        
        stream.println("<font color=grey>Please wait...</font></span></td></tr></center>");
        stream.print("</table>\n</div>\n</body>\n</html>");
        
    }
    
    /**
     * Sends an error message in HTML to the browser, with printStackTrace included
     * @param result  the class that holds the result
     * @param stream  the outputstream of the servlet
     * @throws IOException
     */
    private void isError(MyResult result, ServletOutputStream stream) throws IOException {
        stream.print("<html>\n\t<head>\n\t\t<title>Error</title>\n\t</head>\n\t<body>");
        stream.print("An error occured, see the server's logfiles:<br> \n");
        stream.print(result.errorMsg);
        stream.println("\t</body>\n</html>");
    }
    
    /**
     * Sends the output to the browser
     * @param result  the class that holds the result
     * @param stream the outputstream of the servlet
     * @param session
     * @throws IOException
     */
    private void isFinished(MyResult result, PrintWriter out, HttpSession session) throws IOException {

        session.removeAttribute("myResult");
        
        session.setAttribute("XMLresults", result.retXML);
        session.setAttribute("termsInQuery", result.termsInQuery);
        session.setAttribute("retSynsMatrix", result.synonymMatrix);
        session.setAttribute("retSpellSuggestions", result.spellMatrix);
        session.setAttribute("retWordnetSynsMatrix", result.wordnetMatrix);        
        session.setAttribute("onlineSynMatrix", result.onlineSynMatrix);        
        session.setAttribute("retrievedExpansions", (new Boolean(result.retrievedExpansions)).toString());       
        
        String[] listFields = (String[]) session.getAttribute("allFields");
        
        boolean sentenceIndex = false;
        if (result.index.startsWith("sentence") || result.index.startsWith("SENTENCE") || result.index.endsWith("SENTENCE") || result.index.endsWith("sentence"))
            sentenceIndex = true;

        xmlOpts.setCharacterEncoding("UTF-8");
        
        double duration = (result.end.getTime() - result.start.getTime())/1000;
        
        try {
            resultDoc = ResultDocument.Factory.parse(result.retXML, xmlOpts);
        } catch (Exception e) {
            log.severe("XML Parse exception: "+e.toString());
            log.severe("XML contains: " + result.retXML);
            resultDoc = null;
            throw new IOException(e.getMessage());
        }
        
        ResultType xmlResult = resultDoc.getResult();
        
        float timeTaken = xmlResult.getTime().floatValue();
        String luceneQuery = xmlResult.getQuery();
        int totalHits = xmlResult.getTotal();
        Document[] doc = xmlResult.getDocArray();
        
        if (resultDoc != null) {
            // Start of the page
            out.println("<!DOCTYPE HTML PUBLIC "+
                    "\"-//W3C//DTD HTML 4.01 Transitional//EN\" "+
                    "\"http://www.w3.org/TR/html4/loose.dtd\">");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>AID Search interface</title>");
            out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\""+root+"/css/qckcss.css\"/>");
            out.println("<link REL=\"SHORTCUT ICON\" HREF=\""+root+"/images/favicon.ico\">");
            out.println("<script type=\"text/javascript\" src=\"" + root + "/javascript/overlib.js\">");
            out.println("<!-- bla -->");
            out.println("</script>");
            out.println("<script type=\"text/javascript\" src=\"" + root + "/javascript/gw.js\"/>");
            out.println("<!-- bla -->");
            out.println("</script>");

            out.println("<script type=\"text/javascript\">");
            out.println("function toggleAllFields (whichField) {");
            out.println(" for (var x = 1; x <= " + doc.length + "; x++){");
            out.println("   toggleLayer(whichField + x);");
            out.println(" }");
            out.println("}      ");      
            out.println("</script>");
            
            out.println("<style type='text/css'>@import url(" + root + "/css/qckcss.css);");
            
            for (int j=0 ; j<listFields.length ; ++j) {
                for (int m=1 ; m<=doc.length ; ++m) {
                    out.println("  div#showField" + listFields[j] + Integer.toString(m) +"{display: none; font-size : 8pt;}");
                }
            }
            
            for(int m = 1; m <= doc.length; m++)
                out.println("  div#showFieldLuceneDocID" + Integer.toString(m) + "{display: none; font-size : 6pt;}");
            
            if (sentenceIndex) 
                out.println("  div#sentenceURL{font-size : 7pt;}");            
            out.println("</style>");
            out.println("</head>");

            // Turn all layers on
            out.print("<body bgcolor=white background=\""+root+"/images/background.gif\" OnLoad=\"javascript:");
            for (int j=0 ; j<listFields.length ; ++j) {
                out.print("toggleAllFields('showField" + listFields[j] + "');");
            }
            out.print("toggleAllFields('showFieldLuceneDocID');");
            out.print("\">");
            out.println("<div id=\"AIDfp\">");
            out.println("<a name=\"top\"/>");
            out.println("  <table border=\"0\" width=700>");

            // Header
            out.println("<tr><td colspan=2>");
            out.println("<table>");
            out.println(" <tr valign=\"top\" bgcolor=white><td class=\"navtext\">");
            out.println(" <div id=\"navlinks\">");
            out.println("   <img src=\""+root+"/images/top.png\" width=\"709\" height=\"200\" border=\"0\"><!-- bla --> </img>");

            out.println("   <div style=\"position: relative;top: -55px;margin-left: 15px;\"><a href=\""+root+"/\">Home</a></div>");
            out.println("   <div style=\"position: relative;top: -71px;margin-left: 180px;\">Concept Finder</div>");
            out.println("   <div style=\"position: relative;top: -87px;margin-left: 410px;\">Search Details</div>");
            out.println("   <div style=\"position: relative;top: -103px;margin-left: 620px;\">History</div>");
            out.println("</div></td></tr></table>");
            out.println("</td></tr>");

            //bgcolor="#324E81"
            // End header

            out.println("    <tr>");        
            out.println("      <td width=20%>");
            out.println("        <b>Index: </b>" + result.index);
            out.println("      </td>");
            out.println("      <td class='resultItemRight'>");
            out.println("        <b onmouseover=\"return overlib('Total time, including webservice calls (Search-only time).',TEXTFONT,'Verdana');\" onmouseout=\"return nd();\">Time taken: </b>" + duration + " (" + timeTaken + ") s");
            out.println("      </td>");            
            out.println("    </tr>"); 

            out.println("    <tr>");        
            out.println("      <td width=20%>");
            out.println("        <a href=\"http://lucene.apache.org/java/docs/queryparsersyntax.html\" target=\"_blank\" onmouseover=\"return overlib('Lucene query syntax help.',TEXTFONT,'Verdana');\" onmouseout=\"return nd();\"><div id=\"popover\"><b>Query:</b></div></a>");
            out.println("      </td>");
            out.println("      <td class='resultItemRight'>");
            out.println("        <b>Hits: </b>" + totalHits);
            out.println("      </td>");            
            out.println("    </tr>");
            
            
            out.println("    <tr>");        
            out.println("      <td colspan=2 align=center class='resultItemCenter'>");
            out.println("        " + luceneQuery);

            out.println("      </td>");
            out.println("    </tr>");   

            out.println("      <tr>");
            out.println("        <td colspan=2>");
            out.println("          <img src='" + root + "/images/pixel.gif' width=700 height=20/>");
            out.println("        </td>");
            out.println("      </tr>");
            
            // If spelling suggestions...
            if (result.retrievedSpellSuggestions) {
                out.println("      <tr>");
                out.print("        <td colspan=2><font color='#FF0000'>Did you mean: </font><i>");
                                
                for (int i=0 ; i < result.spellMatrix.length ; ++i) {
                    if (result.spellMatrix[i] == null) {
                        out.print(result.termsInQuery[i] + " ");
                    } else {
                        out.print("<a href='" + spellSuggestURL + result.query.replaceAll(result.termsInQuery[i], result.spellMatrix[i]).replaceAll(" ", "+") + "'><b>" + result.spellMatrix[i] + "</b></a> ");
                    }
                }

                out.println("        </i></td>");
                out.println("      </tr>");                
                
         
            }
            
            // If query suggestions...
            if (result.retrievedExpansions) {
                out.println("    <tr>");
                out.println("      <td colspan=2>");
                out.println("        <table class='spellWarning' width=60px border='0' cellpadding='0' cellspacing='0'>");
                out.println("          <tr>");
                
                // Other text, looks nicer :-)
                if (result.retrievedSpellSuggestions) {
                    out.println("            <td class='spellWarning'>Note: I may have some more <a class='spellDetails'' href='" + queryExpansionURL + "'>suggestions</a> for your query.&nbsp;&nbsp;</td>");
                } else {
                    out.println("            <td class='spellWarning'>Note: I may have <a class='spellDetails'' href='" + queryExpansionURL + "'>suggestions</a> for your query.&nbsp;&nbsp;</td>");
                }
                out.println("          </tr>");
                out.println("        </table>");
                out.println("      </td>");
                out.println("      <td>");
                out.println("      </td>");
                out.println("    </tr>");
            }
            
            out.println("          <tr>");
            out.println("            <td colspan=3  class='resultItemRight'>");
            out.println("              <img src='" + root + "/images/MS_icons.gif' alt='' name='Save as...'  onmouseover=\"return overlib('Save as...',TEXTFONT,'Verdana');\" onmouseout=\"return nd();\">");
            out.println("            </td>");
            out.println("          </tr>");            
            
            out.println("    <tr>");
            out.println("      <td colspan=2 height=15px>");
            out.println("        <hr/>");
            out.println("      </td>");
            out.println("    </tr>");
            out.println("    <tr>");       
            out.println("      <td width=50px>");

            for (int j=0 ; j<listFields.length ; ++j) {
                out.print("      <input type=checkbox CHECKED onchange=\"javascript:toggleAllFields('showField" + listFields[j] + "');\" id='ID_showfield" + listFields[j] + "'><label class='ShowToggle' for='ID_showfield" + listFields[j] + "'>");
                out.print("Show " + listFields[j] + "</label></input><br>\n");
            }    
            out.print("      <input type=checkbox CHECKED onchange=\"javascript:toggleAllFields('showFieldLuceneDocID');\" id='ID_showfieldLuceneDocID'><label class='ShowToggle' for='ID_showfieldLuceneDocID'>");
            out.print("Show LuceneDocID</label></input><br>\n");            
            out.println("      </td>");

            // Start of the real output: the results
            out.println("      <td width=50px>");
            out.println("        <table>");

            for (int j=0 ; j<doc.length ; ++j) {
                
                // sentenceIndex specific:
                String offset = "0";
                String length = "0";
                String pmid = "0";
                String url = "";
                
                out.println("          <tr>");
                out.println("            <td width=10%>");
                out.println("              <b>(" + doc[j].getRank() + ")</b>");
                out.println("            </td>");
                out.println("            <td>");
                out.println("              <b>" + doc[j].getScore().floatValue() + "</b>");
                out.println("            </td>");            
                out.println("          </tr>");
            
                FieldType[] fields = doc[j].getFieldArray();
                
                for (int i=0;i<fields.length;++i) {    
                    
                    if (sentenceIndex)  {
                        if (fields[i].getName().equalsIgnoreCase("offset")) {
                            offset = fields[i].getValue();
                        } else if (fields[i].getName().equalsIgnoreCase("length")) {
                            length = fields[i].getValue();
                        } else if (fields[i].getName().equalsIgnoreCase("pmid")) {
                            pmid = fields[i].getValue();
                        } 
                    }
                    
                    out.println("          <tr>");
                    out.println("            <td colspan=2 width=200px>");

                    if (fields[i].getName().equalsIgnoreCase("id") || fields[i].getName().equalsIgnoreCase("pmid")) {
                      url = "http://localhost/search/item?index=" +
                        result.index + "&file=" + fields[i].getValue();
                      out.print("              " + "<b><a href='"+url+"'>" + fields[i].getValue() + "</a></b>");// + fields[i].getValue());  
                    } else {
                      out.print("              " + "<div id=\"showField" + fields[i].getName() + Integer.toString(j+1) + "\"><b>" + fields[i].getName() + "</b><br>");  
                      String tmp = fields[i].getValue();
                      byte[] tmp_byte = tmp.getBytes("UTF-8");
                      String tmp2 = new String(tmp_byte);
                      out.println(tmp2 + "</div>");
                    }
                    
                    out.println("            </td>");            
                    out.println("          </tr>");                     
                }
                
                if (sentenceIndex) {
                    out.println("          <tr>");
                    out.println("            <td colspan=2 width=200px>");
                    //http://localhost/search/sentence?pmid=10901326&offset=3449&length=533
                    out.println("              <div id='sentenceURL'>");  
                    out.println("                <a href='" + root + 
                            "/sentence?pmid=" + pmid + 
                            "&offset=" + offset + 
                            "&length=" + length + 
                            "'>lookup " + pmid + "</a>");  
                    out.println("              </div>");
                    out.println("            </td>");            
                    out.println("          </tr>");                                         
                    
                }
                
                out.println("          <tr>");
                out.println("            <td width=100% colspan=2>");
                out.println("              <hr/>");
                out.println("            </td>");
                out.println("          </tr>");                
            }
            // Handle empty fields? Not neccesary
            
            out.println("        </table>");
            out.println("      </td>");
            out.println("    </tr>");
            out.println("  </table>");

            




            //Footer
            out.println("<div id=\"footer\">");
            out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
            out.println("<tr>");
            out.println("<td width=\"1%\"><img src=\""+root+"/images/footer-leftcurve.gif\" width=\"10\" height=\"31\" border=\"0\"/></td>");
            out.println("<td width=\"98%\" bgcolor=\"#\" class=\"footertext\">");
            out.println("<a href=\"#top\">Top</a>");
            out.println("        |    ");
            out.println("<a href=\""+root+"/synonym\">Synonym client</a>");
            out.println("        |    ");
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
        } else {
            // No parsable XML returned by SearcherWS
            //...
        }
    }

    
}
