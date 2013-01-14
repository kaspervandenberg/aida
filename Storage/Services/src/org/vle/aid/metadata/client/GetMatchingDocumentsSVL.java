/*
 * GetMatchingDocumentsSVL.java
 *
 * Created on March 22, 2006, 1:23 PM
 */
package org.vle.aid.metadata.client;

import java.io.*;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import org.vle.aid.common.Properties;

/**
 *
 * @author wrvhage
 * @version
 */
public class GetMatchingDocumentsSVL extends HttpServlet
{

    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GetMatchingDocumentsSVL.class);

    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map param = request.getParameterMap();


        int limit = 5;
        String count = " limit 5";
        if (param.get("count") != null)
        {
            limit = Integer.parseInt(((String[]) param.get("count"))[0]);
            count = " limit " + ((String[]) param.get("count"))[0];
        }
        String id = ((String[]) param.get("id"))[0];
        String server_url = ((String[]) param.get("server_url"))[0];
        String repository = ((String[]) param.get("repository"))[0];
        String username = ((String[]) param.get("username"))[0];
        String password = ((String[]) param.get("password"))[0];
        String query_language = "sparql";

        String property = ((String[]) param.get("property"))[0];
        String concept = ((String[]) param.get("concept"))[0];

        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ajax-response><response type='object' id='" + id + "_updater'><matches>");
        try {
            String endpoint = null;
            if (param.get("axis_url") != null)
            {
                endpoint = ((String[]) param.get("axis_url"))[0] + "/services/RepositoryWS";
            } 
            else
            {
                endpoint =  Properties.Entries.AXIS_ENDPOINT.get() + "/services/RepositoryWS";
            }

            Service service = new Service();
            Call call = (Call) service.createCall();

            int hits = 0;
            call.setTargetEndpointAddress(endpoint);
            call.setOperationName("selectQuery");

            String query = /*"select distinct D from {D} P {C} " +
                    "where P = <" + property + "> and C = <" + concept + "> and " +
                    "(D like \"http:*\" or D like \"https:*\" or D like \"file:*\" or D like \"srb:*\")"*/
                    "select DISTINCT ?D " +
                    "where { ?D ?P ?C . " +
                    " FILTER (?P = <" + property + "> && ?C = <" + concept + "> && " +
                    " (regex(str(?D), \"^http:.*$\", \"i\") || " +
                    "  regex(str(?D), \"^https:.*$\", \"i\") || " +
                    "  regex(str(?D), \"^file:.*$\", \"i\") || " +
                    "  regex(str(?D), \"^srb:.*$\", \"i\"))) }"+ count;

            String[][] table = null;

            logger.debug(property + "|" + concept + "|" + count + "|" + id + "|" + server_url + "|" + repository + "|" + username + "|" + password + "|" + query + "|" + query_language);

            table = (String[][]) call.invoke(new Object[]{
                        server_url, repository, username, password, query_language, query
                    });

            for (int i = 0; i < table.length; i++)
            {
                String doc = table[i][0];
                out.println("<entry>" +
                        "<text>" + doc + "</text>" +
                        "<value>" + doc + "</value>" +
                        "<comment></comment>" +
                        "</entry>");
            }

        } 
        catch (Exception e)
        {
            out.println(e.toString());
            for (int i = 0; i < e.getStackTrace().length; i++)
            {
                out.println(e.getStackTrace()[i].toString());
            }
        }
        out.println("</matches></response></ajax-response>");
        out.close();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /** Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }
    // </editor-fold>
}
