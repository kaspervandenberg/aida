/*
 * queryExpansion.java
 *
 */

package org.vle.aid.client;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

public class queryExpansion extends HttpServlet {
    private String root;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        HttpSession session = req.getSession(true);

        root = req.getContextPath();

        String addButton = req.getParameter("addButton");

        try {
            session = req.getSession(true);

            if (session.isNew()) {
                session.invalidate();
                res.setContentType("text/html;charset=UTF-8");

                PrintWriter out = res.getWriter();

                out.println(
                    (new StringBuilder()).append("<html><HEAD><META HTTP-EQUIV='Refresh' CONTENT='0; URL=").append(
                        root).append("/AID'/></HEAD></html>").toString());
                out.close();
            }
        } catch (IllegalStateException e) {
            res.sendRedirect((new StringBuilder()).append(root).append("/AID").toString());
        }

        String originalQuery = (String) session.getAttribute("query");

        if (addButton != null) {
            String newQueryTerms[] = (String[]) req.getParameterValues("newQueryTerms");

            if ((newQueryTerms == null) || (newQueryTerms.length == 0)) {
                res.sendRedirect(
                    res.encodeURL(
                        root.concat(
                            (new StringBuilder()).append("/AID?query=").append(
                                originalQuery.replaceAll("\\s+", "+")).toString())));
            } else {
                String newQuery = originalQuery.replaceAll("\\s+", "+");

                for (int i = 0; i < newQueryTerms.length; i++) {
                    newQuery = (new StringBuilder()).append(newQuery).append("+").append(newQueryTerms[i]).toString();
                }

                res.sendRedirect(
                    res.encodeURL(
                        root.concat((new StringBuilder()).append("/AID?query=").append(newQuery).toString())));
            }
        } else {
            String spellMatrix[]       = (String[]) session.getAttribute("retSpellSuggestions");
            String termsInQuery[]      = (String[]) session.getAttribute("termsInQuery");
            String wordnetMatrix[][]   = (String[][]) session.getAttribute("retWordnetSynsMatrix");
            String synonymMatrix[][]   = (String[][]) session.getAttribute("retSynsMatrix");
            String onlineSynMatrix[][] = (String[][]) session.getAttribute("onlineSynMatrix");

            res.setContentType("text/html;charset=UTF-8");

            PrintWriter out = res.getWriter();

            out.println(
                "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>AID Search interface</title>");
            out.println(
                (new StringBuilder()).append("<style type='text/css'>@import url(").append(root).append(
                    "/css/qckcss.css);").toString());
            out.println("</style>");
            out.println(
                (new StringBuilder()).append("<link REL=\"SHORTCUT ICON\" HREF=\"").append(root).append(
                    "/images/favicon.ico\">").toString());
            out.println(
                (new StringBuilder()).append("<script type=\"text/javascript\" src=\"").append(root).append(
                    "/javascript/overlib.js\">").toString());
            out.println("<!-- bla -->");
            out.println("</script>");
            out.println("</head>");
            out.println(
                (new StringBuilder()).append("<body bgcolor=white background=\"").append(root).append(
                    "/images/background.gif\">").toString());
            out.println("<div id=\"AIDfp\">");
            out.println("<a name=\"top\"/>");
            out.println("  <table border=\"0\" width=600>");
            out.println("<tr><td colspan=3>");
            out.println("<table>");
            out.println("\t<tr valign=\"top\" bgcolor=white><td class=\"navtext\">");
            out.println("\t<div id=\"navlinks\">");
            out.println(
                (new StringBuilder()).append("\t\t<img src=\"").append(root).append(
                    "/images/top.png\" width=\"709\" height=\"200\" border=\"0\"><!-- bla --> </img>").toString());
            out.println(
                (new StringBuilder()).append(
                    "\t\t<div style=\"position: relative;top: -55px;margin-left: 15px;\"><a href=\"").append(
                    root).append("/\">Home</a></div>").toString());
            out.println("\t\t<div style=\"position: relative;top: -71px;margin-left: 180px;\">Concept Finder</div>");
            out.println("\t\t<div style=\"position: relative;top: -87px;margin-left: 410px;\">Search Details</div>");
            out.println("\t\t<div style=\"position: relative;top: -103px;margin-left: 620px;\">History</div>");
            out.println("</div></td></tr></table>");
            out.println("</td></tr>");
            out.println(
                (new StringBuilder()).append("    <form name=\"selectionForm\" method=\"get\" action=\"").append(
                    res.encodeURL(req.getRequestURI())).append("\">").toString());
            out.println("      <tr>");
            out.println("        <td width=33% class='resultItemCenter'>");

            if (synonymMatrix != null) {
                out.println("          Found index-specific syonyms:<br>");
                out.println("          <select name='newQueryTerms' multiple>");

                for (int i = 0; i < synonymMatrix.length; i++) {
                    for (int j = 0; j < synonymMatrix[i].length; j++) {
                        if (synonymMatrix[i][j] != null) {
                            out.println(
                                (new StringBuilder()).append("            <option value='").append(
                                    synonymMatrix[i][j]).append("'>").append(synonymMatrix[i][j]).append(
                                    "</option>").toString());
                        }
                    }
                }

                out.println("          </select>");
            }

            out.println("        </td>");
            out.println("        <td width=33% class='resultItemCenter'>");

            if (wordnetMatrix != null) {
                out.println("          Found Wordnet syonyms:<br>");
                out.println("          <select name='newQueryTerms' multiple>");

                for (int i = 0; i < wordnetMatrix.length; i++) {
                    for (int j = 0; j < wordnetMatrix[i].length; j++) {
                        if (wordnetMatrix[i][j] != null) {
                            out.println(
                                (new StringBuilder()).append("            <option value='").append(
                                    wordnetMatrix[i][j]).append("'>").append(wordnetMatrix[i][j]).append(
                                    "</option>").toString());
                        }
                    }
                }

                out.println("          </select>");
            }

            out.println("        </td>");
            out.println("        <td width=33% class='resultItemCenter'>");

            if (onlineSynMatrix != null) {
                out.println("          Found online acronyms:<br>");
                out.println("          <select name='newQueryTerms' multiple>");

                for (int i = 0; i < onlineSynMatrix.length; i++) {
                    for (int j = 0; j < onlineSynMatrix[i].length; j++) {
                        if (onlineSynMatrix[i][j] != null) {
                            out.println(
                                (new StringBuilder()).append("            <option value='").append(
                                    onlineSynMatrix[i][j]).append("'>").append(onlineSynMatrix[i][j]).append(
                                    "</option>").toString());
                        }
                    }
                }

                out.println("          </select>");
            }

            out.println("        </td>          </tr>");
            out.println("      <tr>");
            out.println("        <td class='resultItemCenter' width=100% colspan=3>");
            out.println("              <hr/>");
            out.println("        </td>");
            out.println("      </tr>");
            out.println("      <tr>");
            out.println("        <td class='resultItemCenter' width=100% colspan=3>");
            out.println("          <input type=submit name=\"addButton\" value=\"Add\">");
            out.println("        </td>");
            out.println("      </tr>");
            out.println("    </form>");
            out.println("  </table>");
            out.println("<div id=\"footer\">");
            out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
            out.println("<tr>");
            out.println(
                (new StringBuilder()).append("<td width=\"1%\"><img src=\"").append(root).append(
                    "/images/footer-leftcurve.gif\" width=\"10\" height=\"31\" border=\"0\"/></td>").toString());
            out.println("<td width=\"98%\" bgcolor=\"#\" class=\"footertext\">");
            out.println("<a href=\"#top\">Top</a>");
            out.println("        |\t\t");
            out.println(
                (new StringBuilder()).append("<a href=\"").append(root).append(
                    "/synonym\">Synonym client</a>").toString());
            out.println("        |\t\t");
            out.println("<a href=\"http://www.vl-e.nl\">Vl-e</a>");
            out.println("</td>");
            out.println(
                (new StringBuilder()).append("<td width=\"1%\"><img src=\"").append(root).append(
                    "/images/footer-rightcurve.gif\" width=\"10\" height=\"31\" border=\"0\"/></td>").toString());
            out.println("</tr>");
            out.println("</table>");
            out.println("</div>");
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
            out.close();
        }
    }
}


