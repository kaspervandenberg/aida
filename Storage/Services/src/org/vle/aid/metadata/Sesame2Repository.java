package org.vle.aid.metadata;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.sail.memory.MemoryStore;
import org.vle.aid.metadata.exception.AddRdfException;
import org.vle.aid.metadata.exception.ClearRepositoryException;
import org.vle.aid.metadata.exception.ExtractRdfException;
import org.vle.aid.metadata.exception.RemoveRDFException;
import org.vle.aid.metadata.exception.SystemQueryException;
import org.vle.aid.metadata.exception.QueryException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import org.apache.log4j.Logger;

public class Sesame2Repository implements org.vle.aid.metadata.Repository {

  private String server = "";
  private String repository = "";
  private String username = "";
  private String password = "";
  private String rdf_format = "RDF/XML";
  private String query_language = "sparql";
  private String select_output_format = "html_table";
  public boolean Virtuoso = false;
  public String named_graph = "";

  private static Pattern pdt = Pattern.compile("^\"(.*)\"\\^\\^<?(.*)>?$");
  private static Pattern plang = Pattern.compile("^\"(.*)\"@(\\w+)$");
  private static Pattern p = Pattern.compile("^(\\w+):\\/\\/([^\\/:\\?\\#]+)?(:(\\d+))?(\\/+[^\\?\\#]*)?(\\?([^\\#]*))?(\\#(.*))?\\s*$");
  private static Pattern bracesp = Pattern.compile("^<(.*)>$");
  private static Pattern p2 = Pattern.compile("^<?(\\w+):\\/\\/([^\\/:\\?\\#]+)?(:(\\d+))?(\\/+[^\\?\\#]*)?(\\?([^\\#]*))?(\\#(.*))?\\s*>?$");
 
  private static HashMap<String, Boolean> isVirtuosoMap = new HashMap<String, Boolean>();

  private static Logger logger = Logger.getLogger("Sesame2Repository");

  /**
   * Creates a new instance of Sesame2Repository
   */
  public Sesame2Repository() throws QueryException {
    setVirtuoso();
  }

  /**
   * Creates a new instance of Sesame2Repository and sets default values
   * for the server URL, repository name, username, and password.
   * i.e. the URL where Sesame2 runs (e.g. http://localhost:8080/sesame),
   * the Sesame repository to use (e.g. mem-rdfs-db), and the username
   * and password that allow access to this repository.
   */
  public Sesame2Repository(
      String server, String repository,
      String username, String password) {
    setServer(server);
    setRepository(repository);
    setUsername(username);
    setPassword(password);
  }

  /**
   * See above, but also sets a default value for the RDF serialization format
   * that is used to output RDF of, for example, CONSTRUCT queries and exports.
   * @param rdf_format rdfxml, ntriples, turtle, n3, etc.
   */
  public Sesame2Repository(
      String server, String repository,
      String username, String password,
      String rdf_format) {
    setServer(server);
    setRepository(repository);
    setUsername(username);
    setPassword(password);
    setRdfFormat(rdf_format);
  }

  public String getServer() {
    return this.server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  public String getRepository() {
    return this.repository;
  }

  public void setRepository(String repository) {
    this.repository = repository;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRdfFormat() {
    return this.rdf_format;
  }

  public void setRdfFormat(String rdf_format) {
    this.rdf_format = rdf_format;
  }

  public String getQueryLanguage() {
    return this.query_language;
  }

  /**
   * @param query_language Sets the query language to use for the next SELECT or CONSTRUCT query.
   */
  public void setQueryLanguage(String query_language) {
    this.query_language = query_language;
  }

  public String getSelectOutputFormat() {
    return this.select_output_format;
  }

  /**
   * @param select_output_format Sets the format to use for the output of SELECT queries.
   * Currently, the only supported value is "html_table".
   */
  public void setSelectOutputFormat(String select_output_format) {
    this.select_output_format = select_output_format;
  }

  public void setVirtuoso() throws QueryException {
    this.Virtuoso = _detectVirtuoso(getServer());
  }

  public boolean getVirtuoso() {
    return this.Virtuoso;
  }

  public boolean _detectVirtuoso(String server) throws QueryException {
    try {

	  if(isVirtuosoMap.containsKey(server))
		  	return isVirtuosoMap.get(server);

      String[][] res_virt = selectQuery(server, "", "", "", "SPARQL",
            "select ?s ?o where {?s a ?o} LIMIT 1");

      if ((res_virt.length != 0)||(res_virt != null))
      // IT MOST DEFINETELY IS A (e.g. VIRTUOSO) SPARQL ENDPOINT
      // FULLY ACCESIBLE THROUGH THE SESAME2 API
      {
		isVirtuosoMap.put(server, true);
        return true;
      }
	  isVirtuosoMap.put(server, false);
      return false;
    } catch (Throwable org) {
	  isVirtuosoMap.put(server, false);
      return false;
    }    
  }

  /**
   * Attempts to detect whether the parameter "value" is a URI or a literal.
   */
  // forced type
  private Object _parseValue(String value, ValueFactory vf, String type) {
    if (type == null) {
      return null;
    }
    Value rv = null;
    if (type.equalsIgnoreCase("LITERAL")) {
      Matcher mdt = pdt.matcher(value);
      Matcher mlang = plang.matcher(value);
      if (mdt.matches()) {
        String lit = mdt.group(1);
        String dt = mdt.group(2);
        rv = vf.createLiteral(lit, vf.createURI(dt));
      } else if (mlang.matches()) {
        String lit = mlang.group(1);
        String lang = mlang.group(2);
        rv = vf.createLiteral(lit, lang);
      } else {
        rv = vf.createLiteral(value);
      }
    } else if (type.equalsIgnoreCase("URI")) {
      
      Matcher bracesm = bracesp.matcher(value);
      if (bracesm.matches()) {
        value = bracesm.group(1);
      }
      
      Matcher m = p.matcher(value);
      if (m.matches()) {
        String scheme = m.group(1);
        String userinfo = null;
        String host = m.group(2);
        String port = m.group(4);
        String path = m.group(5);
        String query = m.group(7);
        String fragment = m.group(9);
        try {
          java.net.URI uri = null;
          if (port == null) {
            if (scheme.equalsIgnoreCase("file")) {
              uri = new java.net.URI(scheme, "//" + path, fragment);
            } else {
              uri = new java.net.URI(scheme, host, path, query, fragment);
            }
          } else {
            uri = new java.net.URI(scheme, userinfo, host, Integer.parseInt(port), path, query, fragment);
          }
          rv = vf.createURI(uri.toASCIIString());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return rv;
  }

  // detect type
  private Object _parseValue(String value, ValueFactory vf) {
    Value rv = null;
    if (value != null) {
      
      Matcher m = p2.matcher(value);
      if (m.matches()) {
        String scheme = m.group(1);
        String userinfo = null;
        String host = m.group(2);
        String port = m.group(4);
        String path = m.group(5);
        String query = m.group(7);
        String fragment = m.group(9);
        try {
          java.net.URI uri = null;
          if (port == null) {
            if (scheme.equalsIgnoreCase("file")) {
              uri = new java.net.URI(scheme, "//" + path, fragment);
            } else {
              uri = new java.net.URI(scheme, host, path, query, fragment);
            }
          } else {
            uri = new java.net.URI(scheme, userinfo, host, Integer.parseInt(port), path, query, fragment);
          }
          rv = vf.createURI(uri.toASCIIString());
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {

        Matcher mdt = pdt.matcher(value);
        Matcher mlang = plang.matcher(value);
        if (mdt.matches()) {
          String lit = mdt.group(1);
          String dt = mdt.group(2);
          rv = vf.createLiteral(lit, vf.createURI(dt));
        } else if (mlang.matches()) {
          String lit = mlang.group(1);
          String lang = mlang.group(2);
          rv = vf.createLiteral(lit, lang);
        } else {
          rv = vf.createLiteral(value);
        }
      }
    } else {
      rv = vf.createBNode();
    }
    return rv;
  }

  private RDFFormat _verifyRdfFormat(String format) {
    //for clients that used to work with Sesame 1 WS
    if (format.equalsIgnoreCase("rdfxml")) {
      format = "RDF/XML";
    }
    if (format.equalsIgnoreCase("ntriples")) {
      format = "N-triples";
    }

    return RDFFormat.valueOf(format);
  }

  private QueryLanguage _verifyQueryLanguage(String query_language) {
    String ql = query_language;
    if (ql.length() == 0) {
      ql = "sparql";
    }

    QueryLanguage qlang = null;
    if (ql.equalsIgnoreCase("sparql")) {
      qlang = QueryLanguage.SPARQL;
    } else {
      if (ql.equalsIgnoreCase("serql")) {
        qlang = QueryLanguage.SERQL;
      } else {
        if (ql.equalsIgnoreCase("serqo")) {
          qlang = QueryLanguage.SERQO;
        }
      }
    }

    return qlang;
  }

  private org.openrdf.repository.Repository _initRepository(String server_url, String repository) throws RepositoryException {
    try {
      org.openrdf.repository.Repository myRepository =
          new HTTPRepository(server_url, repository);

      myRepository.initialize();

      return myRepository;
    } catch (Throwable org) {
      org.printStackTrace();

      throw new RepositoryException("_initRepository Exception Error:" + org.getMessage(), org);
    }
  }

  public boolean addRdf(String data_uri, String data) throws AddRdfException {
    return addRdf(getServer(), getRepository(), getUsername(), getPassword(), getRdfFormat(), data_uri, data);
  }

  public boolean addRdf(String server_url, String repository,
      String username, String password, String rdf_format,
      String data_uri, String data) throws AddRdfException {
    try {
      RDFFormat frm = _verifyRdfFormat(rdf_format);
      String baseURI = data_uri + "#";

      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);
      RepositoryConnection con = myRepository.getConnection();

      StringReader str = new StringReader(data);
      try {
        con.add(str, baseURI, frm);
      } finally {
        con.close();
      }
    } catch (Throwable org) {
      org.printStackTrace();

      throw new AddRdfException("AddRdf Error:" + org.getMessage(), org);
    }
    return true;
  }

  public boolean addRdf(String data_uri, String data, String context) throws AddRdfException {
    return addRdf(getServer(), getRepository(), getUsername(), getPassword(), getRdfFormat(), data_uri, data, context);
  }

  public boolean addRdf(String server_url, String repository,
      String username, String password, String rdf_format,
      String data_uri, String data, String context) throws AddRdfException {
    try {
      RDFFormat frm = _verifyRdfFormat(rdf_format);
      String baseURI = data_uri + "#";

      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);
      ValueFactory vf = myRepository.getValueFactory();
      RepositoryConnection con = myRepository.getConnection();

      StringReader str = new StringReader(data);
      try {
        con.add(str, baseURI, frm, vf.createURI(context));
      } finally {
        con.close();
      }
    } catch (Throwable org) {
      org.printStackTrace();

      throw new AddRdfException("AddRdfWithContext Error:" + org.getMessage(), org);
    }
    return true;
  }

  public boolean addRdfFile(String data_uri) throws AddRdfException {
    return addRdfFile(getServer(), getRepository(), getUsername(), getPassword(), getRdfFormat(), data_uri);
  }

  public boolean addRdfFile(String server_url, String repository,
      String username, String password,
      String rdf_format, String data_uri) throws AddRdfException {
    try {
      RDFFormat frm = _verifyRdfFormat(rdf_format);
      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);
      RepositoryConnection con = myRepository.getConnection();
      String baseURI = data_uri + "#";

      if(data_uri.contains("://"))
      {
        URL url = new URL(data_uri);
        try {
          con.add(url, baseURI, frm);
        } finally {
          con.close();
        }
      }
      else
      {
        File file = new File(data_uri);
        try {
          con.add(file, baseURI, frm);
        } finally {
          con.close();
        }
      }      
    } catch (Throwable org) {
      org.printStackTrace();

      throw new AddRdfException("AddRdfFile Error:" + org.getMessage(), org);
    }
    return true;
  }

  public boolean addRdfFile(String data_uri, String context) throws AddRdfException {
    return addRdfFile(getServer(), getRepository(), getUsername(), getPassword(), getRdfFormat(), data_uri, context);
  }

  public boolean addRdfFile(String server_url, String repository,
      String username, String password, String rdf_format,
      String data_uri, String context) throws AddRdfException {
    try {
      RDFFormat frm = _verifyRdfFormat(rdf_format);
      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);
      ValueFactory vf = myRepository.getValueFactory();
      RepositoryConnection con = myRepository.getConnection();
      String baseURI = data_uri + "#";

      if(data_uri.contains("://"))
      {
        URL url = new URL(data_uri);
        try {
          con.add(url, baseURI, frm, vf.createURI(context));
        } finally {
          con.close();
        }
      }
      else
      {
        File file = new File(data_uri);
        try {
          con.add(file, baseURI, frm, vf.createURI(context));
        } finally {
          con.close();
        }
      }
    } catch (Throwable org) {
      org.printStackTrace();

      throw new AddRdfException("AddRdfFileWithContext Error:" + org.getMessage(), org);
    }
    return true;
  }

  public boolean addRdfStatement(String subject, String predicate,
      String object) throws AddRdfException {
    return addRdfStatement(getServer(), getRepository(), getUsername(), getPassword(), subject, predicate, object);
  }

  public boolean addRdfStatement(String server_url, String repository,
      String username, String password, String subject, String predicate,
      String object) throws AddRdfException {
    try {
      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);

      ValueFactory vf = myRepository.getValueFactory();
      Resource subj = (Resource) _parseValue(subject, vf);
      org.openrdf.model.URI pred = (org.openrdf.model.URI) _parseValue(predicate, vf, "URI");
      Value obj = (Value) _parseValue(object, vf);

      RepositoryConnection con = myRepository.getConnection();
      try {
        con.add(subj, pred, obj);
      } finally {
        con.close();
      }
    } catch (Throwable org) {
      org.printStackTrace();

      throw new AddRdfException("addRdfStatement Error:" + org.getMessage(), org);
    }
    return true;
  }

  public boolean addRdfStatement(String subject, String predicate,
      String object, String context) throws AddRdfException {
    return addRdfStatement(getServer(), getRepository(), getUsername(), getPassword(), subject, predicate, object, context);
  }

  public boolean addRdfStatement(String server_url, String repository,
      String username, String password, String subject, String predicate,
      String object, String context) throws AddRdfException {
    try {
      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);

      ValueFactory vf = myRepository.getValueFactory();
      Resource subj = (Resource) _parseValue(subject, vf);
      org.openrdf.model.URI pred = (org.openrdf.model.URI) _parseValue(predicate, vf, "URI");
      Value obj = (Value) _parseValue(object, vf);

      RepositoryConnection con = myRepository.getConnection();
      try {
        con.add(subj, pred, obj, vf.createURI(context));
      } finally {
        con.close();
      }
    } catch (Throwable org) {
      org.printStackTrace();

      throw new AddRdfException("addRdfStatementWithContext Error:" + org.getMessage(), org);
    }
    return true;
  }

  public boolean clear() throws ClearRepositoryException {
    return clear(getServer(), getRepository(), getUsername(), getPassword());
  }

  public boolean clear(String server_url, String repository,
      String username, String password) throws ClearRepositoryException {
    try {
      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);
      RepositoryConnection con = myRepository.getConnection();

      try {
        con.clear();
      } finally {
        con.close();
      }
    } catch (Throwable org) {
      org.printStackTrace();

      throw new ClearRepositoryException("Clear Error:" + org.getMessage(), org);
    }
    return true;
  }

  public String constructQuery(String server, String repository,
      String username, String password, String query_language,
      String rdf_format, String query) throws QueryException {
    String rv = null;
    try {
      OutputStream xos = new ByteArrayOutputStream();

      constructQuery(server, repository, username, password, query_language, rdf_format, query, xos);
      rv = xos.toString();
      xos.close();

      return rv;
    } catch (Throwable org) {
      org.printStackTrace();

      throw new QueryException("ConstructQuery Error:" + org.getMessage(), org);
    }
  }

  public void constructQuery(String query, OutputStream out) throws QueryException {
    constructQuery(getServer(), getRepository(), getUsername(), getPassword(), getQueryLanguage(), getRdfFormat(), query, out);
  }

  public String constructQuery(String query) throws QueryException {
    return constructQuery(getServer(), getRepository(), getUsername(), getPassword(), getQueryLanguage(), getRdfFormat(), query);
  }

  public String constructQuery(String server_url, String repository,
      String username, String password, String query_language,
      String rdf_format, String query,
      OutputStream os) throws QueryException {
    try {
      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);
      RepositoryConnection con = myRepository.getConnection();

      QueryLanguage query_lang = _verifyQueryLanguage(query_language);

      try {
        RDFXMLWriter writer = new RDFXMLWriter(os);

        con.prepareGraphQuery(query_lang, query).evaluate(writer);

        return os.toString();
      } finally {
        con.close();
      }
    } catch (Throwable org) {
      org.printStackTrace();

      throw new QueryException("ConstructQuery Error:" + org.getMessage(), org);
    }
  }

  public String extractRdf() throws ExtractRdfException {
    return extractRdf(getServer(), getRepository(), getUsername(), getPassword(), getRdfFormat());
  }

  public String extractRdf(String rdf_format) throws ExtractRdfException {
    return extractRdf(getServer(), getRepository(), getUsername(), getPassword(), rdf_format);
  }

  public String extractRdf(String server_url, String repository,
      String username, String password,
      String rdf_format) throws ExtractRdfException {
    try {
      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);
      RepositoryConnection con = myRepository.getConnection();

      try {
        OutputStream xos = new ByteArrayOutputStream();
        RDFXMLWriter writer = new RDFXMLWriter(xos);
        con.export(writer);

        return xos.toString();
      } finally {
        con.close();
      }
    } catch (Throwable org) {
      org.printStackTrace();

      throw new ExtractRdfException("ExtractRdf Error:" + org.getMessage(), org);
    }
  }

  public String[][] getRepositoriesLabel() throws SystemQueryException {
    return getRepositoriesLabel(getServer(), getUsername(), getPassword(), "r");
  }
  public String[] getRepositories() throws SystemQueryException {
    return getRepositories(getServer(), getUsername(), getPassword(), "r");
  }

  /*
   * This version of getRepositories only returns repository names as list of string without URL information
   * Perfectly fine for Sesame repositories, but in case of virtuoso/named graph additional query is needed to get the appropriate URL. 
   * @see org.vle.aid.metadata.Repository#getRepositories(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  // Backward compatibility
  public String[] getRepositories(String server_url, String username, String password, String read_write) throws SystemQueryException {

    	String [][] res2 = getRepositoriesLabel(server_url, username, password, read_write);
		String res[] = new String[res2.length];
		for(int i=0;i<res.length;i++)
			res[i] = res2[i][0];
		return res;
  
  }

  public String[][] getRepositoriesLabel(String server_url, String username,
      String password, String read_write) throws SystemQueryException {
    try {
      if (!_detectVirtuoso(server_url)) {
        Vector<String> result_vectors = new Vector<String>();
        Vector<String> label_vectors = new Vector<String>();

		// First try assuming it is Sesame
		try {
			HTTPClient http_cl = new HTTPClient();
			http_cl.setServerURL(server_url);

			TupleQueryResult res0 = http_cl.getRepositoryList();

			List<String> bindingNames = res0.getBindingNames();

			while (res0.hasNext()) {
			  BindingSet bindingSet = res0.next();
			  Value idValue = bindingSet.getValue("id");
			  Value labelValue = bindingSet.getValue("title");
			  if(idValue != null){
				String idString = getCleanLiterals(idValue);
			  	result_vectors.add(idString);
				if(labelValue != null){
				    String labelString = getCleanLiterals(labelValue);
			  		label_vectors.add(labelString);
				}
				else
			  		label_vectors.add(idString);
			  }

			  /*
			  String value="";
			  Value firstValue = bindingSet.getValue(bindingNames.get(1));

			  if (firstValue != null) {
				value = getCleanLiterals(firstValue);
				if(!value.equalsIgnoreCase("SYSTEM"))
				  result_vectors.add(value);
			  } */

			}
	   		int rowCount = result_vectors.size();
			String[][] res1 = new String[rowCount][2];

			//label and url are the same
			for (int row = 0; row < rowCount; row++) {
			  res1[row] = new String[2];
			  res1[row][0] = label_vectors.get(row);
			  res1[row][1] = result_vectors.get(row);
			}

			res0.close();

			return res1;
		  
		} catch (Exception e){
			e.printStackTrace();
			// This is how I get repositories list from OWLIM.
			AIDRepository rep = new AIDRepository(server_url);
			return rep.getRepositoriesLabel();
		}
       /*String[][] res0 = selectQuery(server_url, "SYSTEM", "", "", "SPARQL",
					"SELECT ?o where {?s <http://www.openrdf.org/config/repository#repositoryID> ?o}");
    	String[] res1 = new String[res0.length - 1];
		int count = 0;
		for(int i=0; i<res0.length; i++)
	    {
          if(!res0[i][0].equalsIgnoreCase("SYSTEM"))
		  {
            res1[count] = res0[i][0].replace("\"", "");
			count++;
		  }
	    }

		return res1;*/
      }
      else {

        boolean isDeri = false;
		
		// Notice the order is label first, then src. Matching the one above on the sesame version
        String query = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
					   "SELECT ?lbl ?src WHERE { "+
					   "GRAPH ?src {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o} ."+
					   "OPTIONAL {?src rdfs:label ?lbl} "+
	 				   "} limit 5000";
        //String query = "SELECT ?src WHERE {GRAPH ?src {?s a ?o}} limit 60000";
        if (server_url.contains("hcls.deri.org")){
        	//query = "SELECT ?src WHERE {GRAPH ?src {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o}} . FILTER(regex(str(?src), \"/obo/\", \"i\")) offset 25 limit 25 ";
        	//query = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?lbl ?src WHERE {GRAPH ?src {?s ?p ?o} .  OPTIONAL {?src rdfs:label ?lbl} } offset 25 limit 25";
        	query = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT  ?lbl ?src WHERE {GRAPH ?src {?s a ?o}  . OPTIONAL {?src rdfs:label ?lbl} } limit 50";
		} 
	
        String[][] result = selectQuery(server_url, "", username, password, "SPARQL", query);

		if(result == null) return new String[0][0];
		
		// Making these results unique
		 
		 HashMap <String,String[]> uniq = new HashMap<String, String[]>();
		 for(int i=0;i<result.length;i++){ 
			 if(result[i][0] == null || result[i][0].equals("")) 
				result[i][0] = result[i][1];
			 uniq.put(result[i][0]+result[i][1], result[i]);
		 }
		 
		 String [][] ret = new String[uniq.size()][];
		 int i=0;
		 for(String [] row : uniq.values())
			 ret[i++]= row;
		
	     return ret;		

      }
    } catch (Throwable org) {
      org.printStackTrace();

      throw new SystemQueryException("GetRepositories Error:" + org.getMessage(), org);
    }
  }

  String [][] jsonToStringArray(String jsonString) throws Exception {
		String [][] result = null;
		
		JSONObject out = (JSONObject) new JSONParser().parse(jsonString);
		JSONArray vars = (JSONArray) ((JSONObject) out.get("head")).get("vars");
		
		JSONObject results = (JSONObject)out.get("results");
		JSONArray  bindings = (JSONArray)results.get("bindings");
		
		int nCol = vars.size();
		int nRow = bindings.size();
		
		result = new String[nRow][nCol];
		
		for(int i=0;i<nRow;i++){
				for(int j=0;j<nCol;j++){
					JSONObject cur = (JSONObject) bindings.get(i);
					result[i][j] = ((JSONObject)cur.get(vars.get(j).toString())).get("value").toString();
				}
		}
		
		return result;
  }
  public boolean removeRdf(String data_uri, String data) throws RemoveRDFException {
    return removeRdf(getServer(), getRepository(), getUsername(), getPassword(), getRdfFormat(), data_uri, data);
  }

  
  public boolean removeRdf(String server_url, String repository,
      String username, String password, String rdf_format,
      String data_uri, String data) throws RemoveRDFException {
    try {
      RDFFormat frm = _verifyRdfFormat(rdf_format);
      String baseURI = data_uri + "#";

      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);
      RepositoryConnection con = myRepository.getConnection();

      StringReader str = new StringReader(data);

      org.openrdf.repository.Repository lrep = new SailRepository(new MemoryStore());
      lrep.initialize();
      RepositoryConnection lcon = lrep.getConnection();

      try {
        lcon.add(str, baseURI, frm);
        con.remove(lcon.getStatements(null, null, null, true));
      } finally {
        lcon.close();
        con.close();
      }
    } catch (Throwable org) {
      org.printStackTrace();

      throw new RemoveRDFException("RemoveRdf Error:" + org.getMessage(), org);
    }
    return true;
  }

  public boolean removeRdf(String data_uri, String data, String context) throws RemoveRDFException {
    return removeRdf(getServer(), getRepository(), getUsername(), getPassword(), getRdfFormat(), data_uri, data, context);
  }

  public boolean removeRdf(String server_url, String repository,
      String username, String password, String rdf_format,
      String data_uri, String data, String context) throws RemoveRDFException {
    try {
      RDFFormat frm = _verifyRdfFormat(rdf_format);
      String baseURI = data_uri + "#";

      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);
      RepositoryConnection con = myRepository.getConnection();

      StringReader str = new StringReader(data);

      org.openrdf.repository.Repository lrep = new SailRepository(new MemoryStore());
      lrep.initialize();
      ValueFactory vf = lrep.getValueFactory();
      RepositoryConnection lcon = lrep.getConnection();

      try {
        URI context_URI = vf.createURI(context);
        lcon.add(str, baseURI, frm, context_URI);
        con.remove(lcon.getStatements(null, null, null, true, context_URI));
      } finally {
        lcon.close();
        con.close();
      }
    } catch (Throwable org) {
      org.printStackTrace();

      throw new RemoveRDFException("RemoveRdfWithContext Error:" + org.getMessage(), org);
    }
    return true;
  }

  public boolean removeRdfFile(String data_uri) throws RemoveRDFException {
    return removeRdfFile(getServer(), getRepository(), getUsername(), getPassword(), getRdfFormat(), data_uri);
  }

  public boolean removeRdfFile(String server_url, String repository,
      String username, String password, String format,
      String data_uri) throws RemoveRDFException {
    try {
      RDFFormat frm = _verifyRdfFormat(rdf_format);
      String baseURI = data_uri + "#";

      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);
      RepositoryConnection con = myRepository.getConnection();

      org.openrdf.repository.Repository lrep = new SailRepository(new MemoryStore());
      lrep.initialize();
      RepositoryConnection lcon = lrep.getConnection();

      if(data_uri.contains("://"))
      {
        URL url = new URL(data_uri);
        try {
          lcon.add(url, baseURI, frm);
          con.remove(lcon.getStatements(null, null, null, true));
        } finally {
          con.close();
        }
      }
      else
      {
        File file = new File(data_uri);
        try {
          lcon.add(file, baseURI, frm);
          con.remove(lcon.getStatements(null, null, null, true));
        } finally {
          con.close();
        }
      }
    } catch (Throwable org) {
      org.printStackTrace();

      throw new RemoveRDFException("RemoveRdfFile Error:" + org.getMessage(), org);
    }
    return true;
  }

  public boolean removeRdfFile(String data_uri, String context) throws RemoveRDFException {
    return removeRdfFile(getServer(), getRepository(), getUsername(), getPassword(), getRdfFormat(), data_uri, context);
  }

  public boolean removeRdfFile(String server_url, String repository, String username, String password, String format, String data_uri, String context) throws RemoveRDFException {
    try {
      RDFFormat frm = _verifyRdfFormat(rdf_format);
      String baseURI = data_uri + "#";

      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);
      RepositoryConnection con = myRepository.getConnection();

      org.openrdf.repository.Repository lrep = new SailRepository(new MemoryStore());
      lrep.initialize();
      ValueFactory vf = lrep.getValueFactory();
      RepositoryConnection lcon = lrep.getConnection();

      if(data_uri.contains("://"))
      {
        URL url = new URL(data_uri);
        try {
          URI context_URI = vf.createURI(context);
          lcon.add(url, baseURI, frm, context_URI);
          con.remove(lcon.getStatements(null, null, null, true, context_URI));
        } finally {
          con.close();
        }
      }
      else
      {
        File file = new File(data_uri);
        try {
          URI context_URI = vf.createURI(context);
          lcon.add(file, baseURI, frm, context_URI);
          con.remove(lcon.getStatements(null, null, null, true, context_URI));
        } finally {
          con.close();
        }
      }
    } catch (Throwable org) {
      org.printStackTrace();

      throw new RemoveRDFException("RemoveRdfFileWithContext Error:" + org.getMessage(), org);
    }
    return true;
  }

  public boolean removeRdfStatement(String subject, String predicate,
      String object) throws RemoveRDFException {
    return removeRdfStatement(getServer(), getRepository(), getUsername(), getPassword(), subject, predicate, object);
  }

  public boolean removeRdfStatement(String server_url, String repository,
      String username, String password, String subject, String predicate,
      String object) throws RemoveRDFException {
    try {
      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);

      ValueFactory vf = myRepository.getValueFactory();
      Resource subj = (Resource) _parseValue(subject, vf);
      org.openrdf.model.URI pred = (org.openrdf.model.URI) _parseValue(predicate, vf, "URI");
      Value obj = (Value) _parseValue(object, vf);

      RepositoryConnection con = myRepository.getConnection();
      try {
        con.remove(subj, pred, obj);
      } finally {
        con.close();
      }
    } catch (Throwable org) {
      org.printStackTrace();

      throw new RemoveRDFException("RemoveRdfStatement Error:" + org.getMessage(), org);
    }
    return true;
  }

  public boolean removeRdfStatement(String subject, String predicate,
      String object, String context) throws RemoveRDFException {
    return removeRdfStatement(getServer(), getRepository(), getUsername(), getPassword(), subject, predicate, object, context);
  }

  public boolean removeRdfStatement(String server_url, String repository,
      String username, String password, String subject, String predicate,
      String object, String context) throws RemoveRDFException {
    try {
      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);

      ValueFactory vf = myRepository.getValueFactory();
      Resource subj = (Resource) _parseValue(subject, vf);
      org.openrdf.model.URI pred = (org.openrdf.model.URI) _parseValue(predicate, vf, "URI");
      Value obj = (Value) _parseValue(object, vf);

      RepositoryConnection con = myRepository.getConnection();
      try {
        con.remove(subj, pred, obj, vf.createURI(context));
      } finally {
        con.close();
      }
    } catch (Throwable org) {
      org.printStackTrace();

      throw new RemoveRDFException("RemoveRdfStatementWithContext Error:" + org.getMessage(), org);
    }
    return true;
  }

  public String selectQuery(String query) throws QueryException {
    return selectQuery(getServer(), getRepository(), getUsername(), getPassword(), getQueryLanguage(), getSelectOutputFormat(), query);
  }

  public String selectQuery(String server_url, String repository,
      String username, String password, String query_language,
      String select_output_format, String query) throws QueryException {
    String rv = "";
	
	logger.info("Check query : " + query);
    try {
      String of = select_output_format;
      if (of.length() == 0) {
        of = "html_table";
      }

      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);
      RepositoryConnection con = myRepository.getConnection();

      QueryLanguage query_lang = _verifyQueryLanguage(query_language);

      TupleQuery tupleQuery = con.prepareTupleQuery(query_lang, query);

      if (of.equalsIgnoreCase("json")) {
        OutputStream xos = new ByteArrayOutputStream();

        tupleQuery.evaluate(new SPARQLResultsJSONWriter(xos));
        rv = xos.toString();
        xos.close();

        return rv;
      } else if (of.equalsIgnoreCase("html_table")) {
        try {
          TupleQueryResult result = tupleQuery.evaluate();

          rv = "<html><body>";
          rv += "<h2>returned</h2>";
          rv += "<table>";

          rv += "<tr>";

          List<String> bindingNames = result.getBindingNames();
          Object[] names = bindingNames.toArray();
          int columnCount = bindingNames.size();

          for (int column = 0; column < columnCount; column++) {
            rv += "<th>" + names[column].toString() + "</th>";
          }
          rv += "</tr>";

          while (result.hasNext()) {
            rv += "<tr>";
            BindingSet bindingSet = result.next();
            String[] values = new String[columnCount];

            Value firstValue = bindingSet.getValue(bindingNames.get(0));
            if (firstValue != null) {
              for (int i = 0; i < columnCount; i++) {
                rv += "<td>";
                values[i] = getCleanLiterals(bindingSet.getValue(bindingNames.get(i)));
                rv += values[i];
                rv += "</td>";
              }
            } else {
              rv += "null";
            }
            rv += "</tr>";
          }
          rv += "</table>";
          result.close();
        } finally {
          con.close();
          rv += "</body></html>";
        }
      }
      return rv;
    } catch (Throwable org) {
      org.printStackTrace();

      throw new QueryException("SelectQuery(Serialized) Error:" + org.getMessage(), org);
    }
  }

  public String[][] selectQuery(String server_url, String repository,
      String username, String password, String query_language,
      String query) throws QueryException {

	logger.info("Check query : " + query);
    try {

      org.openrdf.repository.Repository myRepository = _initRepository(server_url, repository);
      RepositoryConnection con = myRepository.getConnection();

      QueryLanguage query_lang = _verifyQueryLanguage(query_language);

      TupleQuery tupleQuery = con.prepareTupleQuery(query_lang, query);
      TupleQueryResult result = tupleQuery.evaluate();

      try {

        List<String> bindingNames = result.getBindingNames();

        int columnCount = bindingNames.size();

        Vector<String[]> result_vectors = new Vector<String[]>();
        
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();

          String[] values = new String[columnCount];
          for (int i = 0; i < bindingNames.size(); i++) {
        	  Value curValue = bindingSet.getValue(bindingNames.get(i)); 
			  String cleaned = getCleanLiterals(curValue);
			  if(cleaned == null || cleaned.equals("")){
              	values[i] = curValue == null ? null : curValue.toString();
			  } else
				values[i] = cleaned;

            }
            result_vectors.add(values);
        }
        int rowCount = result_vectors.size();
        String[][] resultTable = new String[rowCount][columnCount];

        for (int row = 0; row < rowCount; row++) {
          String[] rowValues = result_vectors.get(row);
          for (int column = 0; column < columnCount; column++) {
            resultTable[row][column] = rowValues[column];
          }
        }
		logger.info("# Results "+ resultTable.length);
        return resultTable;
      } finally {
        //con.close();
        result.close();
      }
    } 
    catch (org.openrdf.repository.http.HTTPQueryEvaluationException e1) {
      return null;
      
    }
    catch (IllegalArgumentException e) {
      return null;
      
    } catch (Throwable org) {
      org.printStackTrace();

      throw new QueryException("SelectQuery Error:" + org.getMessage(), org);
    }
  }

  private String getCleanLiterals(Value full_value) {
    if (full_value != null) {
      if (full_value.getClass().toString().equalsIgnoreCase("class org.openrdf.model.impl.LiteralImpl")) {
        LiteralImpl lit = (LiteralImpl) full_value;
        return lit.getLabel();
      }
    }
    //return full_value.toString();
    return "";
  }
}
