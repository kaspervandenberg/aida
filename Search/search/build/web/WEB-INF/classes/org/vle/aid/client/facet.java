/*
 * jason.java
 *
 * Created on March 22, 2006, 1:23 PM
 */

package org.vle.aid.client;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import java.io.*;

import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author wrvhage
 * @version
 */
public class facet extends HttpServlet {

    /** logger for Commons logging. */
    private transient Logger log = Logger.getLogger("facet.class.getName()");

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @SuppressWarnings("unchecked")
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map         param = request.getParameterMap();
        PrintWriter out   = response.getWriter();

        response.setContentType("text/html;charset=UTF-8");

        String query = null;
        String index = null;
        String field = null;
        String start = null;
        String count = null;

        try {
            query = ((String[]) param.get("query"))[0];
            index = ((String[]) param.get("index"))[0];
            field = ((String[]) param.get("field"))[0];
            start = ((String[]) param.get("start"))[0];
            count = ((String[]) param.get("count"))[0];
        } catch (java.lang.NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            response.setHeader("Location",
                               "http://" + request.getServerName() + ":" + request.getServerPort()
                               + "/search/json_test.html");

            return;
        }

        query.replaceAll("%2B", "+");
        log.info("Received facet_JSON: " + query);

        String   endpoint = "http://localhost/axis/services/getFields";
        String[] res      = new String[0];

        try {
            Service service = new Service();
            Call    call    = (Call) service.createCall();

            call.setTargetEndpointAddress(endpoint);
            call.setOperationName("listFields");
            res = (String[]) call.invoke(new Object[] { index });
        } catch (Exception e) {
            out.println(e.toString());

            for (int i = 0; i < e.getStackTrace().length; i++) {
                out.println(e.getStackTrace()[i].toString());
                log.severe(e.getStackTrace()[i].toString());
            }
        }

        out.println("<html>");
        out.println("<head>");
        out.println(" <title>" + query + " - AIDA Faceted Search Results</title>");
        out.print(" <link href='http://" + request.getServerName() + ":" + request.getServerPort()
                  + "/search/jason?target=search&index=" + index);
        out.print("&field=" + field);
        out.print("&start=" + start);
        out.print("&limit=" + count);
        out.print("&query=" + query);
        out.println("' type='application/json' rel='exhibit/data' />");
        out.println(
            " <script src='http://static.simile.mit.edu/exhibit/api-2.0/exhibit-api.js' type='text/javascript'></script>");
        out.println(
            " <script src='http://static.simile.mit.edu/exhibit/extensions-2.0/time/time-extension.js' type='text/javascript'></script>");
        out.println(" <style>");
        out.println("   body {");
        out.println("       font-family:    Tahoma, Arial, sans serif;");
        out.println("       font-size:      8pt;");
        out.println("       margin:         0;");
        out.println("       padding:        0;");
        out.println("   }");
        out.println("    #logo_rechts {");
        out.println("      position:fixed;");
        out.println("      right:0;");
        out.println("      bottom:0;");
        out.println("      z-index:1;");
        out.println("      background-color:transparent;");
        out.println("      width:162px;");
        out.println("      height:90px;");
        out.println("      filter:alpha(opacity=20);");
        out.println("      -moz-opacity:0.20;");
        out.println("    }");
        out.println("    #logo_links {");
        out.println("      position:fixed;");
        out.println("      left:0;");
        out.println("      bottom:0;");
        out.println("      z-index:1;");
        out.println("      background-color:transparent;");
        out.println("      width:35px;");
        out.println("      height:28px;");
        out.println("      filter:alpha(opacity=50);");
        out.println("      -moz-opacity:0.50;");
        out.println("    }");
        out.println("   table { font-size: 100%; }");
        out.println("   tr { vertical-align: top; }");
        out.println("   #title-area {");
        out.println("       padding:        1em 2em;");
        out.println("       background:     #BCB79E;");
        out.println("   }");
        out.println("   #content-area {");
        out.println("       padding:        2em;");
        out.println("   }");
        out.println("   span.exhibit-collectionView-group-count {");
        out.println("       color:       #ccc;");
        out.println("       font-weight: normal;");
        out.println("   }");
        out.println("   div.exhibit-facet-value-selected {");
        out.println("       background:  none;");
        out.println("       font-weight: bold;");
        out.println("   }");
        out.println(" </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <div id='logo_links'>");
        out.println("      <a href='http://simile.mit.edu/exhibit/'>");
        out.println(
            "        <img src='http://static.simile.mit.edu/graphics/logos/exhibit/exhibit-small-LightGrey.png' border=0/>");
        out.println("      </a>");
        out.println("    </div>");
        out.println("    <div id='logo_rechts'>");
        out.println("      <a href='http://www.adaptivedisclosure.org/'>");
        out.println("        <img src='/search/images/logo.png' border=0/>");
        out.println("      </a>");
        out.println("    </div>");
        out.println(" <h1>AIDA Faceted Search Results</h1>");
        out.println(
            " <div style='position:absolute;right:0;top:0;z-index:9999;background-color:transparent;width:35px;height:28px;' onclick=\"window.open('http://simile.mit.edu/exhibit/')\" ONMOUSEOVER=\"this.style.cursor='pointer';\" >&nbsp;</div>");
        out.println(" <table width='100%'>");
        out.println("   <tr valign='top'>");
        out.println("     <td ex:role='viewPanel'>");
        out.print("       <div ex:role='view' ex:orders='.score' ex:possibleOrders='.score, ");

        String fields = "";
        String facets = "";

        for (int i = 0; i < res.length; i++) {
            if (res[i].equalsIgnoreCase("content")) {
                continue;
            }

            if (res[i].equalsIgnoreCase("id")) {
                continue;
            }

            if (res[i].equalsIgnoreCase("pmid")) {
                continue;
            }

            fields += "." + res[i] + ", ";
            facets += "       <div ex:role='facet' ex:expression='." + res[i] + "' ex:facetLabel='" + res[i]
                      + "' ex:sortMode='count'></div>\n";
        }

        fields = fields.substring(0, fields.length() - 2) + "'";
        out.println(
            fields
            + " ex:directions='descending' ex:possibleDirections='ascending, descending' ex:grouped='false' ex:showAll='false' ex:abbreviatedCount='5'>");
        out.println("       </div>");
        out.println(
            "       <div ex:role='view' ex:viewClass='Timeline' ex:start='.year' ex:colorKey='.mesh' ex:bottomBandUnit='decade' ex:topBandUnit='year'></div>");
        out.println("     </td>");
        out.println("     <td width='25%'>");
        out.println("             <b>Search within results:</b><div ex:role='facet' ex:facetClass='TextSearch'></div>");

        // out.println("       <div ex:role='facet' ex:expression='.PT' ex:facetLabel='Publication type' ex:sortMode='count'></div>");
        // out.println("       <div ex:role='facet' ex:expression='.mesh' ex:facetLabel='MeSH' ex:sortMode='count'></div>");
        // out.println("       <div ex:role='facet' ex:expression='.year' ex:facetLabel='Year of publication' ex:directions='descending' ex:sortMode='count'></div>");
        out.print(facets);
        out.println("     </td>");
        out.println("   </tr>");
        out.println(" </table>");
        out.println(" </body>");
        out.println(" </html>");
        out.close();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}


