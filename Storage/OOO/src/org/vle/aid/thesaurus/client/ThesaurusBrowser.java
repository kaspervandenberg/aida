/*
 * ThesaurusBrowser.java
 *
 * Created on March 7, 2006, 2:39 PM
 */
package org.vle.aid.thesaurus.client;

import java.io.*;
import java.util.Map;
import java.net.URI;
import java.net.URLEncoder;

import java.util.Iterator;
import java.util.regex.Pattern;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 *
 * @author wrvhage
 * @version
 */
public class ThesaurusBrowser extends HttpServlet {

  static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ThesaurusBrowser.class);
  
    private transient java.util.logging.Logger log =
      java.util.logging.Logger.getLogger("SearcherWS.class.getName()");
    
  static String server_url = null;
  static String repository = null;
  static String username = null;
  static String password = null;
  static String login_info = null;
  static String ns = "null";
  static String focus_term = null;
  static String base_url = null;
  static String ws_server_url = null;
  
  static boolean outputjason = false;
  
  private static Pattern NEWLINE = Pattern.compile("\\n");

  /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    Map param = request.getParameterMap();
    String uri = HttpUtils.getRequestURL(request).toString();

    if (param.containsKey("test"))
      return;

    try {
      URI uriURI = new URI(HttpUtils.getRequestURL(request).toString());
      String scheme = uriURI.getScheme();
      String host = uriURI.getHost();
      int port = uriURI.getPort();
      String path = uriURI.getPath();

      if (param.containsKey("json"))
        outputjason = true;
      else 
        outputjason = false;
      
      if (!param.containsKey("server_url")) {
        throw new ServletException("server_url not defined");
      } else {
        server_url = ((String[]) param.get("server_url"))[0];
      }

      if (!param.containsKey("ws_server_url")) {
        ws_server_url = server_url;
        ws_server_url = ws_server_url.replaceAll("/sesame.*", "");
      } else {
        ws_server_url = ((String[]) param.get("ws_server_url"))[0];
      }

      if (!param.containsKey("repository")) {
        throw new ServletException("repository not defined");
      } else {
        repository = ((String[]) param.get("repository"))[0];
      }
      if (!param.containsKey("username")) {
        throw new ServletException("username not defined");
      } else {
        username = ((String[]) param.get("username"))[0];
      }
      if (!param.containsKey("password")) {
        throw new ServletException("password not defined");
      } else {
        password = ((String[]) param.get("password"))[0];
      }
      if (!param.containsKey("ns")) {        //throw new ServletException("ns (namespace) not defined"); 
      } else {
        ns = ((String[]) param.get("ns"))[0];
      }

      if (!param.containsKey("term")) {
        focus_term = null;
      } else {
        focus_term = ((String[]) param.get("term"))[0];
        ns = focus_term.split("#")[0];
      }

      // extjs specific
      if (param.containsKey("node"))
        focus_term = ((String[]) param.get("node"))[0];
      
      base_url = scheme + "://" + host + ":" + port + path + "?";
      login_info = "&amp;server_url=" + URLEncoder.encode(server_url, "UTF-8") +
              "&amp;repository=" + URLEncoder.encode(repository, "UTF-8") +
              "&amp;username=" + URLEncoder.encode(username, "UTF-8") +
              "&amp;password=" + URLEncoder.encode(password, "UTF-8");

      URI u2 = new URI(scheme, null, host, port, "/OOO/ThesaurusSearch", null, null);

      /*
       * Entire Thesaurus, Concept Scheme, Collection, or Concept
       */
      if (param.get("mapping") == null && !outputjason) {

        response.setContentType("text/xml;charset=UTF-8");

        out.print("<viewer>");
        out.print("<ns>" + ns + "</ns>");
        out.print("<search_action>" + u2.toString() + "</search_action>" +
                "<server_url>" + server_url + "</server_url>" +
                "<repository>" + repository + "</repository>" +
                "<username>" + username + "</username>" +
                "<password>" + password + "</password>");

        if (focus_term == null &&
                ((param.get("scheme") == null &&
                param.get("collection") == null) ||
                (param.get("type") != null &&
                ((String[]) param.get("type"))[0].equals("http://www.w3.org/2004/02/skos/core#Concept")))) {

          // Thesaurus
          out.print(_lookupNarrowerWithCounts("getConceptSchemesWithNamespace", new Object[]{ns},
                  "<concept_schemes>", "</concept_schemes>",
                  "<concept_scheme>", "</concept_scheme>",
                  new String[]{"scheme", "scheme_name"}, "browser.showTopConcepts",
                  new String[]{"ns"}, new String[]{ns}));

        } else if ((focus_term == null &&
                param.get("scheme") == null &&
                param.get("collection") != null) || // FIXME: obsolete
                (param.get("type") != null &&
                ((String[]) param.get("type"))[0].equals("http://www.w3.org/2004/02/skos/core#Collection"))) {

          // Collection
          out.print("<uri>" + ((String[]) param.get("term"))[0] + "</uri>");


          out.print(_callComplexWS("getCollectionMembers", new Object[]{((String[]) param.get("term"))[0]},
                  "<collection_members name=\"" + ((String[]) param.get("name"))[0] + "\">", "</collection_members>",
                  "<member>", "</member>",
                  new String[]{"term", "name", "type"}, "browser.loadConcept",
                  new String[]{"ns"}, new String[]{ns}));

          out.print(_callComplexWS("getInCollections", new Object[]{((String[]) param.get("term"))[0]},
                  "<in_collection name=\"" + ((String[]) param.get("name"))[0] + "\">", "</in_collection>",
                  "<collection>", "</collection>",
                  new String[]{"term", "name", "type"}, "browser.loadConcept",
                  new String[]{"ns"}, new String[]{ns}));

        } else if ((focus_term == null &&
                param.get("scheme") != null) ||
                (param.get("type") != null &&
                ((String[]) param.get("type"))[0].equals("http://www.w3.org/2004/02/skos/core#ConceptScheme"))) {

          // Concept Scheme
          out.print("<uri>" + ((String[]) param.get("scheme"))[0] + "</uri>");

          //System.err.println(((String[]) param.get("scheme"))[0]);

          out.print(_lookupNarrowerWithCounts("getTopConcepts", new Object[]{((String[]) param.get("scheme"))[0]},
                  "<top_concepts concept_scheme_name=\"" + ((String[]) param.get("scheme_name"))[0] + "\">", "</top_concepts>",
                  "<top_concept>", "</top_concept>",
                  new String[]{"term"}, "browser.loadConcept",
                  new String[]{"ns"}, new String[]{ns}));

        } else if (focus_term != null) { // focus_term != null

          out.print("<uri>" + focus_term + "</uri>");

          if (param.get("label_lookup") != null) {

            /*
             * Just output the label and number of narrower terms of a class.
             */

            out.print("<lookup_results>");
            out.print(_callSimpleWS("getPreferedTerms", focus_term, "<prefLabel>", "</prefLabel>"));
            out.print(_callSimpleWS("getRDFSLabels", focus_term, "<label>", "</label>"));
            out.print(_lookupNrNT((Object) focus_term));
            out.print("</lookup_results>");

          } else {

            /*
             * Standard Concept view.
             */

            out.print("<concept_view>");

            out.print(_callSimpleWS("getPreferedTerms", focus_term, "<prefLabel>", "</prefLabel>"));
            out.print(_callSimpleWS("getAlternativeTerms", focus_term, "<altLabel>", "</altLabel>"));

            out.print(_callSimpleWS("getNotes", focus_term, "<note>", "</note>"));
            out.print(_callSimpleWS("getDefinitions", focus_term, "<definition>", "</definition>"));

            out.print(_lookupNarrowerWithCounts("getInSchemes", new Object[]{focus_term}, "", "", "<scheme>", "</scheme>",
                    new String[]{"scheme", "scheme_name"}, "browser.showTopConcepts",
                    new String[]{"ns"}, new String[]{ns}));
            out.print(_callComplexWS("getInCollections", new Object[]{focus_term}, "", "", "<collection>", "</collection>",
                    new String[]{"term", "name", "type"}, "browser.loadConcept",
                    new String[]{"ns"}, new String[]{ns}));

            out.print(_lookupNarrowerWithCounts("getBroaderTerms", focus_term, "<broader>", "</broader>"));
            out.print(_lookupNarrowerWithCounts("getNarrowerTerms", focus_term, "<narrower>", "</narrower>"));
            out.print(_lookupNarrowerWithCounts("getRelatedTerms", focus_term, "<related>", "</related>"));

            out.print("</concept_view>");
          }
        }

        out.print("</viewer>");
      } else if (outputjason) { // JSON output

        response.setContentType("text/html;charset=UTF-8");

        if (param.containsKey("rootnodes")) { // get only the rootnodes
          out.println("{topterms:[" + jsonGetTopConcepts() + "]}");
          return;
        }

        String[][] rv = null;
        String broader = "";
        String narrower = "";

        // extjs
        if (focus_term.startsWith("extjs_s_"))
          focus_term = focus_term.replaceFirst("extjs_s_", "");        
        
        // Check if we're dealing with a term or a uri
        if (focus_term.startsWith("http://") || param.containsKey("suggest")) {
          rv = new String[1][1];
          rv[0][0] = focus_term;
        } else {
            rv = _searchTermUri(focus_term);
        }

        // if we have something left...
        if (rv != null) {
          for (int i = 0; i < rv.length; i++) { // foreach term; usually just one
            
            final String current_term = rv[i][0];
            
            
            
            // search
            if (param.containsKey("broader") || param.containsKey("sibling")) {
              broader = jsonGetBroaderConcepts(current_term);
            } else if (param.containsKey("recurseChildren")){
              narrower += jsonGetNarrowerConceptsRecursively(current_term, 1);
            } else if (param.containsKey("suggest")) {
              narrower += jsonGetSuggestions(current_term);
            } else {
              narrower += jsonGetAltConcepts(current_term);
              //narrower +=  jsonGetPrefConcepts(current_term);
              //narrower += jsonGetNarrowerConceptsRecursively(current_term, 1);
              narrower += jsonGetNarrowerConcepts(current_term);
            }
          }
        }
        
        out.println("[");
        if (broader.length() > 0)
          out.println(broader);
        
        if (narrower.length() > 0)
          out.println(narrower);
        
        out.println("]");

      /*
       * only outputting mappings
       */
      } else if (focus_term != null) {

        response.setContentType("text/xml;charset=UTF-8");

        out.print("<mappings>");

        out.print(_callWS("getExactMatches", focus_term, "<exactMatch>", "</exactMatch>"));
        out.print(_callWS("getBroadMatches", focus_term, "<broadMatch>", "</broadMatch>"));
        out.print(_callWS("getNarrowMatches", focus_term, "<narrowMatch>", "</narrowMatch>"));
        out.print(_callWS("getPartMatches", focus_term, "<partMatch>", "</partMatch>"));
        out.print(_callWS("getWholeMatches", focus_term, "<wholeMatch>", "</wholeMatch>"));
        out.print(_callWS("getDisjointMatches", focus_term, "<disjointMatch>", "</disjointMatch>"));
        out.print(_callWS("getRelatedMatches", focus_term, "<relatedMatch>", "</relatedMatch>"));

        out.print("</mappings>");

      }

    } catch (java.net.URISyntaxException e) {
      logger.error(e.getMessage(), e);
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      String reason = sw.toString();
      pw.close();
      logger.error(e.getMessage(), e);
      if (outputjason) {
        out.print("{'success':false, 'errors':{'reason':'" + e.toString() + "'}}");
      } else {
        out.print(reason);
      }
    } finally {
      out.close();
    }
  }

  private String chop(String input) {
    if (input.length() > 0)
      input = input.substring(0, input.length() - 1);
    return input;
  }
  
  private String indentCharacter(String character, int level) {
    StringBuffer out = new StringBuffer();
    for (int i = 0; i < level; i++) {
      out.append(character);
    }
    return out.toString();
  }
  
  private String chomp(String input) {
    if (input.length() > 0 && input.charAt(input.length()-1) == '\n')
      return chop(input);
    else 
      return input;
  }
  
  private String removeNewlines(String input) {
    return NEWLINE.matcher(input).replaceAll("");
  }

  private String jsonGetTopConcepts() {
    java.util.Vector done = new java.util.Vector();
    String output = "";
    
    String[][] cschemes =
      _lookupNarrowerWithCounts("getConceptSchemesWithNamespace",
      new Object[]{ns});

    if (cschemes == null)
      return "[]";
      
    //System.err.println("Found "+ cschemes.length +" schemes: ");
    // sort here
    java.util.TreeMap out = new java.util.TreeMap();

    for (int i = 0; i < cschemes.length; i++) {
      for (int j = 0; j < cschemes[i].length; j++) {

        String _scheme = cschemes[i][j];
        
        if (!_scheme.startsWith("http://")) 
          continue;
        
        if (done.contains(_scheme))
          continue;
          
        done.add(_scheme);
        
        //System.err.println("scheme: " + _scheme);
        String[][] top =
          _lookupNarrowerWithCounts("getTopConcepts",
          new Object[]{_scheme});

        if (top == null)
          continue;
          
        for (int k = 0; k < top.length; k++) {
          out.put(top[k][1], top[k][0]);
        }
      }
    }
    
    java.util.Iterator it = out.keySet().iterator();
    while (it.hasNext()) {
      String id = (String)it.next();
      output += "{id:'" + out.get(id) + "',term:'" + id + "'},";
      //System.err.println(id);
    }

    
    if (output.length() > 0) output = output.substring(0, output.length() - 1);
    
    return output;
  }
  
  private String jsonGetAltConcepts(String current_term) {
    String output = "";
    
    String[] alts = _callSimpleWS("getAlternativeTerms", current_term);
    
    if (alts == null) return output;
    
    for (int j = 0; j < alts.length; j++) {
      
      if (focus_term.equalsIgnoreCase(alts[j])) 
        continue;
      
      output +=
        "\t\t{\n" +
        "\t\t'text':'" + escapeJason(alts[j]) + "',\n" +
        "\t\t'id':'" + alts[j] + "',\n" +
        "\t\t'leaf':'true',\n" +
        "\t\t'qtip':'Alternative Term',\n" +
        "\t\t'iconCls':'alt-icon'\n" +
        "\t\t},\n";
    }
    
    //if (output.length() > 0) output = output.substring(0, output.length() - 1);
    
    return output;
  }
  
  private String jsonGetSuggestions(String current_term) {
    String output = "";
    
    String[][] suggestions = _lookupNarrowerWithCounts("getTermCompletion", new Object[]{current_term});
    
    if (suggestions == null || suggestions.length == 0) return output;
    
    for (int j = 0; j < suggestions.length; j++) {
      output +=
        "\t{\n" +
        "\t'text':'" + escapeJason(suggestions[j][1]) + "',\n" +
        "\t'id':'" + suggestions[j][0] + "'\n" +
        "\t},";
    }
    
    return chop(output) + "\n"; 
  }
  
  private String jsonGetNarrowerConceptsRecursively(String current_term, int indent) {
    String output = "";
    
    String[][] _narrower = _lookupNarrowerWithCounts("getNarrowerTerms", new Object[]{current_term});
    
    if (_narrower == null || _narrower.length == 0) return output;
    
    for (int j = 0; j < _narrower.length; j++) {
      
      //System.err.println(_narrower[j][1] + " is a narrower term of " + current_term);
      output +=
        indentCharacter("\t", indent) + "{\n" +
        indentCharacter("\t", indent+1) + "'text':'" + escapeJason(_narrower[j][1]) + "',\n" +
        indentCharacter("\t", indent+1) + "'id':'" + _narrower[j][0] + "',\n" +
        indentCharacter("\t", indent+1) + "'qtip':'"+_narrower[j][0]+"',\n" +
        indentCharacter("\t", indent+1) + "'iconCls':'narrower-icon',\n";
      
      String children = jsonGetNarrowerConceptsRecursively(_narrower[j][0], indent+2);
      
      if (children.length() > 0) {
        output +=  
          indentCharacter("\t", indent+1) + "'children':[\n" + 
          children +
          indentCharacter("\t", indent+1) + "]\n";
      } else {
        output +=  indentCharacter("\t", indent+1) + "'leaf':'true'\n";
      }
      
      output +=  indentCharacter("\t", indent) + "},";
    }
    
    return chop(output) + "\n"; 
  }
  
  private String jsonGetNarrowerConcepts(String current_term) {
    String output = "";
    
    String[][] _narrower = _lookupNarrowerWithCounts("getNarrowerTerms", new Object[]{current_term});
    
    if (_narrower == null) return output;
    
    for (int j = 0; j < _narrower.length; j++) {
      output +=
        "\t\t\t{\n" +
        "\t\t\t'text':'" + escapeJason(_narrower[j][1]) + "',\n" +
        "\t\t\t'id':'" + _narrower[j][0] + "',\n" +
        "\t\t\t'qtip':'Narrower Term',\n" +
        //"'leaf':'true'," +
        "\t\t\t'iconCls':'narrower-icon'\n" +
        "\t\t\t},";
    }
    
    if (output.length() > 0) output = output.substring(0, output.length() - 1);
    output += "\n";
    return output;
  }
  
  private String jsonGetPrefConcepts(String current_term) {
    String output = "";
    
    String[] prefs = _callSimpleWS("getPreferedTerms", current_term);
    
    if (prefs == null) return output;
    
    for (int j=0; j<prefs.length; j++) {
      
      if (focus_term.equalsIgnoreCase(prefs[j]))
        continue; 
      
      output += 
        "\t\t{\n" +
        "\t\t'text':'" + escapeJason(prefs[j]) + "',\n" +
        "\t\t'id':'" + prefs[j] + "',\n" +
        "\t\t'qtip':'Preferred Term',\n" +
        "\t\t'leaf':'true',\n" +
        "\t\t'iconCls':'pref-icon'" +
        "\t\t},";
    }
    
    if (output.length() > 0) output = output.substring(0, output.length() - 1);
    
    return output;
  }
  
  
  private String jsonGetBroaderConcepts(String current_term) {
    String output = "";

    String[][] _broader = _lookupNarrowerWithCounts("getBroaderTerms", new Object[]{current_term});
    
    for (int j = 0; j < _broader.length; j++) {
      output +=
        //"\t'broader':\n" +
        "\t{\n" +
        "\t'text':'" + escapeJason(_broader[j][1]) + "',\n" +
        "\t'id':'" + removeNewlines(_broader[j][0]) + "',\n" +
        "\t'qtip':'Broader Term',\n" +
        //"'leaf':'true'," +
        "\t'iconCls':'broader-icon',";

      if (true && j == _broader.length - 1) {
        output += "\n\t'children': [\n";
        output += jsonGetNarrowerConcepts(_broader[j][0]);
        output += "\t],";

      }
      
      output += chop(output) + "\n\t},";
    }
  
    return chop(output);
  }
  
  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /** Handles the HTTP <code>GET</code> method.
   * @param request servlet request
   * @param response servlet response
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

  /** Returns a short description of the servlet.
   */
  public String getServletInfo() {
    return "Short description";
  }
  // </editor-fold>

  // copy-paste from ThesaurusSearch.java for convenience
  private String[][] _searchTermUri(String query) {
    String[][] rv = null;
    try {
      String endpoint = ws_server_url + "/axis/services/ThesaurusRepositoryWS";

      Service service = new Service();
      Call call = (Call) service.createCall();

      call.setTargetEndpointAddress(endpoint);
      call.setOperationName("getTermUri");

      rv = (String[][]) call.invoke(new Object[]{server_url, repository, username, password, query});

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return rv;
  }
  // copy-paste from ThesaurusSearch.java for convenience
  private String _searchTermUriJason(String query) {
    String result = "";
    String[][] rv = _searchTermUri(query);

    if (rv != null) {
      for (int i = 0; i < rv.length; i++) {
        result += "\t{'name':'" + rv[i][1] + "'," +
                "'uri':" + rv[i][0] + "'}\n";
      }
    }
    return "[\n" + result + "]";
  }

  private String[] _callSimpleWS(String operation, String param) {
    String[] rv = null;
    try {
      String endpoint = ws_server_url + "/axis/services/ThesaurusRepositoryWS";
      Service service = new Service();
      Call call = (Call) service.createCall();

      call.setTargetEndpointAddress(endpoint);
      call.setOperationName(operation);

      rv = (String[]) call.invoke(new Object[]{server_url, repository, username, password, param});

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return rv;
  }

  private String _callSimpleWS(String operation, String param, String pre_row, String post_row) {
    String rv_str = "";
    String[] rv = _callSimpleWS(operation, param);

    if (rv == null) {
      return rv_str;
    }
    for (int i = 0; i < rv.length; i++) {
      rv_str += pre_row + rv[i] + post_row;
    }
    return rv_str;
  }

  private String _callWS(String operation, String param, String pre_row, String post_row) {
    return _callComplexWS(operation, new Object[]{param}, "", "", pre_row, post_row,
            new String[]{"term"}, "browser.loadConcept", new String[]{"ns"}, new String[]{ns});
  }

  private String _callComplexWS(String operation, Object[] params,
          String pre_entry, String post_entry,
          String pre_row, String post_row,
          String[] col_attributes, String js_function,
          String[] js_params, String[] js_param_values) {
    String rv_str = "";
    try {
      String endpoint = ws_server_url + "/axis/services/ThesaurusRepositoryWS";

      Service service = new Service();
      Call call = (Call) service.createCall();

      call.setTargetEndpointAddress(endpoint);
      call.setOperationName(operation);

      Object[] params_and_server_info = new Object[4 + params.length];
      params_and_server_info[0] = server_url;
      params_and_server_info[1] = repository;
      params_and_server_info[2] = username;
      params_and_server_info[3] = password;
      for (int p = 0; p < params.length; p++) {
        params_and_server_info[4 + p] = params[p];
      }

      //            logger.debug(operation);
      //for (int i=0;i<params_and_server_info.length;i++) {
      //    logger.debug(params_and_server_info[i]);
      //}
      String[][] rv = null;
      rv = (String[][]) call.invoke(params_and_server_info);

      String js_params_str = "";
      for (int n = 0; n < js_params.length; n++) {
        js_params_str += "&amp;" + js_params[n] + "=" + URLEncoder.encode(js_param_values[n], "UTF-8");
      }

      rv_str += pre_entry;
      for (int i = 0; i < rv.length; i++) {
        String js_col_attr_str = "";
        if (col_attributes != null) {
          for (int j = 0; j < col_attributes.length && j < rv[i].length; j++) {
            js_col_attr_str += "&amp;" + col_attributes[j] + "=" + URLEncoder.encode(rv[i][j], "UTF-8");
          }
        }
        js_col_attr_str = js_col_attr_str.substring(5);
        rv_str += pre_row +
                "<link>javascript:" + js_function + "('" + base_url + "','" + js_col_attr_str + js_params_str + login_info + "');</link>" +
                "<name>" + rv[i][1] + "</name>" +
                post_row;
      }
      rv_str += post_entry;

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return rv_str;
  }

  private String _lookupNrNT(Object param) {
    String rv_str = "";
    try {
      String endpoint = ws_server_url + "/axis/services/ThesaurusRepositoryWS";

      Service service = new Service();
      Call call = (Call) service.createCall();

      call.setTargetEndpointAddress(endpoint);
      call.setOperationName("getNumberOfNarrowerTerms");

      Object[] params_and_server_info = new Object[5];
      params_and_server_info[0] = server_url;
      params_and_server_info[1] = repository;
      params_and_server_info[2] = username;
      params_and_server_info[3] = password;
      params_and_server_info[4] = param;

      String[][] rv = null;
      rv = (String[][]) call.invoke(params_and_server_info);

      if (rv != null) {
        rv_str += "<numberOfNarrowerTerms>";
        for (int i = 0; i < rv.length; i++) {
          rv_str += rv[i][1];
        }
        rv_str += "</numberOfNarrowerTerms>";
      } else {
        rv_str = "<numberOfNarrowerTerms/>";
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return rv_str;
  }

  private String _lookupNarrowerWithCounts(String operation, String param, String pre_row, String post_row) {
    return _lookupNarrowerWithCounts(operation, new Object[]{param}, "", "", pre_row, post_row,
            new String[]{"term"}, "browser.loadConcept", new String[]{"ns"}, new String[]{ns});
  }

  private String[][] _lookupNarrowerWithCounts(String operation, Object[] params) {
    String[][] rv = null;
    try {
      String endpoint = ws_server_url + "/axis/services/ThesaurusRepositoryWS";

      Service service = new Service();
      Call call = (Call) service.createCall();

      call.setTargetEndpointAddress(endpoint);
      call.setOperationName(operation);

      Object[] params_and_server_info = new Object[4 + params.length];
      params_and_server_info[0] = server_url;
      params_and_server_info[1] = repository;
      params_and_server_info[2] = username;
      params_and_server_info[3] = password;
      for (int p = 0; p < params.length; p++) {
        params_and_server_info[4 + p] = params[p];
      }

      rv = (String[][]) call.invoke(params_and_server_info);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return rv;
  }

  private String _lookupNarrowerWithCounts(String operation, Object[] params,
          String pre_entry, String post_entry,
          String pre_row, String post_row,
          String[] col_attributes, String js_function,
          String[] js_params, String[] js_param_values) {
    String rv_str = "";
    try {

      String[][] rv = _lookupNarrowerWithCounts(operation, params);

      String js_params_str = "";
      for (int n = 0; n < js_params.length; n++) {
        js_params_str += "&amp;" + js_params[n] + "=" + URLEncoder.encode(js_param_values[n], "UTF-8");
      }

      rv_str += pre_entry;
      if (rv != null) {
        for (int i = 0; i < rv.length; i++) {
          String js_col_attr_str = "";
          if (col_attributes != null) {
            for (int j = 0; j < col_attributes.length && j < rv[i].length; j++) {
              js_col_attr_str += "&amp;" + col_attributes[j] + "=" + URLEncoder.encode(rv[i][j], "UTF-8");
            }
          }
          js_col_attr_str = js_col_attr_str.substring(5);
          String nrnt = _lookupNrNT(rv[i][0]);
          rv_str += pre_row +
                  "<link>javascript:" + js_function + "('" + base_url + "','" + js_col_attr_str + js_params_str + login_info + "');</link>" +
                  "<name>" + rv[i][1] + "</name>" +
                  nrnt +
                  post_row;
        }
      }
      rv_str += post_entry;

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return rv_str;
  }
  
  /**
   * Substitutes some special characters and returns nicer HTML characters
   * Currently:
   * "&"
   * "<"
   * ">"
   * @param text String of text to be escaped
   * @return a String with replaced characters
  */
  public static String escapeJason(String text) {
      text = text.replaceAll("&", "&amp;");
      text = text.replaceAll("\\'", "&quot;");
      text = text.replaceAll("\"", "&quot;");
      text = text.replaceAll("'", "&quot;");
      text = text.replaceAll("`", "&quot;");
      text = text.replaceAll("%", "&quot;");
      text = text.replaceAll("\\f", "");
      text = text.replaceAll("\\r", "");
      text = text.replaceAll("\\n", " ");
      text = text.replaceAll("\\{", " ");
      text = text.replaceAll("\\}", " ");
      text = text.replaceAll("\\]", ")");
      text = text.replaceAll("\\[", "(");
      text = text.replaceAll("\\*", "");
      //text = text.replaceAll("\\<", "&lt;");
      //text = text.replaceAll("\\>", "&gt;");
      return text;
  }
}
    
