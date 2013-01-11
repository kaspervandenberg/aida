package org.vle.aid.metadata;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.net.HttpURLConnection;

import org.vle.aid.metadata.exception.AddRdfException;
import org.vle.aid.metadata.exception.ClearRepositoryException;
import org.vle.aid.metadata.exception.ExtractRdfException;
import org.vle.aid.metadata.exception.RemoveRDFException;
import org.vle.aid.metadata.exception.SystemQueryException;
import org.vle.aid.metadata.exception.QueryException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.HttpMethod;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLParser;

import org.openrdf.model.impl.LiteralImpl;

public class AIDRepository implements org.vle.aid.metadata.Repository {

	private String server = "";
	private String repository = "";
	private String username = "";
	private String password = "";
	private String rdf_format = "RDF/XML";
	private String query_language = "sparql";
	private String select_output_format = "html_table";

	private HttpClient http_client = new HttpClient();
	GetMethod method=new GetMethod();

	public boolean Virtuoso = false;
	public String named_graph = "";

	final String Q_NAMEGRAPHS 	    = "Select ?X where {GRAPH ?X {?S ?P ?O}}";
	final String Q_NAMEGRAPHS_LABEL = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
									  "Select ?X ?L where { GRAPH ?X {?S ?P ?O} . OPTIONAL {?X rdfs:label ?L} }";
	final String Q_PROBING	  = "Select ?S ?O where {?S a ?O} limit 1"; // sometimes variable predicates are not allowed
	
	NameValuePair nvp_format = new NameValuePair("format", "srj");
	NameValuePair nvp_output = new NameValuePair("output", "json");

	static HashMap<String, String> serverTypesMap = new HashMap<String, String>();

	public AIDRepository() {

	}

	public AIDRepository(String sparql_endpoint) {

		method = new GetMethod(sparql_endpoint);
		this.server = sparql_endpoint;
	}

	public AIDRepository(String sparql_endpoint, String repository,
			String username, String password) {

		method = new GetMethod(sparql_endpoint);
		this.server = sparql_endpoint;
		this.repository = repository;
		this.username = username;
		this.password = password;
	}

	public AIDRepository(String sparql_endpoint, String repository,
			String username, String password, String rdf_format) {

		method = new GetMethod(sparql_endpoint);
		this.server = sparql_endpoint;
		this.repository = repository;
		this.username = username;
		this.password = password;
		this.rdf_format = rdf_format;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getQueryLanguage() {
		return query_language;
	}

	public void setQueryLanguage(String query_language) {
		this.query_language = query_language;
	}

	public String getRdfFormat() {
		return rdf_format;
	}

	public void setRdfFormat(String rdf_format) {
		this.rdf_format = rdf_format;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getSelectOutputFormat() {
		return select_output_format;
	}

	public void setSelectOutputFormat(String select_output_format) {
		this.select_output_format = select_output_format;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setVirtuoso() throws QueryException {

	}

	public boolean getVirtuoso() {
		return this.Virtuoso;
	}

	/**
	 * Setup query string based on serverType.
	 */
	private void setupMethodAndQueryString(String query, HttpMethod method,
			String serverType) throws QueryException {

		NameValuePair q_pair = new NameValuePair("query", query), f_pair, o_pair;

		if (serverType.equals("json")) {
			f_pair = new NameValuePair("format", "json");
			o_pair = new NameValuePair("output", "json");
		} else if (serverType.equals("srj")) {
			f_pair = new NameValuePair("format", "srj");
			o_pair = new NameValuePair("output", "srj");
		} else if (serverType.equals("xml")) {
			f_pair = new NameValuePair("format", "xml");
			o_pair = new NameValuePair("output", "xml");
		} else
			throw new QueryException(
					"Undetermined sparql endpoint type error ", new Throwable(
							"Query Exception"));

		method.setQueryString(new NameValuePair[] { q_pair, f_pair, o_pair });
	}

	/**
	 * Getting server/sparql end point type. In case it is already stored in
	 * static hashmap we will just retrieve it. Otherwise, Jammer. We will probe
	 * it.
	 */
	private String getServerType(String serverLocation) {
		// If we already know the type just return it.
		if (serverTypesMap.containsKey(serverLocation)){
			////System.out.println("Known server type : " + serverLocation + " " + serverTypesMap.get(serverLocation));
			return serverTypesMap.get(serverLocation);
		}

		return probeServer(serverLocation);
	}

	public  String probeServer(String serverLocation) {
		String format = "json";

		if (probeJSON(serverLocation, format).equals(format)) {
			serverTypesMap.put(serverLocation, format);
			return format;
		}

		format = "srj";
		if (probeJSON(serverLocation, format).equals(format)) {
			serverTypesMap.put(serverLocation, format);
			return format;
		}

		format = "xml";
		if (probeXML(serverLocation).equals("xml")) {
			serverTypesMap.put(serverLocation, "xml");
			return format;
		}

		return "unknown";
	}

	private String probeJSON(String serverLocation, String format) {

		GetMethod probeMethod = new GetMethod(serverLocation);

		try {
			
			setupMethodAndQueryString(Q_PROBING, probeMethod, format);

			int status = http_client.executeMethod(probeMethod);
			if (status == HttpURLConnection.HTTP_OK) {
				String[][] testParse = jsonResultToStringArray(new InputStreamReader(probeMethod.getResponseBodyAsStream()));
				// No exception up to this point, we know this is a properly
				// working JSON server
				return format;
			}
		} catch (Exception e) {
			// do nothing, just do another probe.
		}
		// We actually don't use this return value
		return "not " + format;
	}

	private String probeXML(String serverLocation) {

		String format = "xml";
		GetMethod probeMethod = new GetMethod(serverLocation);

		try {
			setupMethodAndQueryString(Q_PROBING, probeMethod,	format);
			int status = http_client.executeMethod(probeMethod);
			if (status == HttpURLConnection.HTTP_OK) {
				// Try to parse the XML result
				SPARQLResultsXMLParser parser = new SPARQLResultsXMLParser();
				TupleQueryResultBuilder handler = new TupleQueryResultBuilder();

				parser.setTupleQueryResultHandler(handler);
				parser.setValueFactory(new ValueFactoryImpl());

				parser.parse(probeMethod.getResponseBodyAsStream());

				TupleQueryResult result = handler.getQueryResult();

				if (result.getBindingNames().size() == 2)
					return format;
			}
		} catch (Exception e) {
			// do nothing, just do another probe.
		}

		// We actually don't use this return value
		return "not " + format;
	}

	private String [][] processResultToStringArray(HttpMethod method, String format) throws Exception{
				if(format.equals("xml"))
					return xmlResultToStringArray(method);
				
				return jsonResultToStringArray(new InputStreamReader(method.getResponseBodyAsStream()));
	}
	
    private String [][] xmlResultToStringArray(HttpMethod method) throws Exception{
    	String [][] ret = null;
		SPARQLResultsXMLParser parser = new SPARQLResultsXMLParser();
		TupleQueryResultBuilder handler = new TupleQueryResultBuilder();

		parser.setTupleQueryResultHandler(handler);
		parser.setValueFactory(new ValueFactoryImpl());

		parser.parse(method.getResponseBodyAsStream());

		TupleQueryResult result = handler.getQueryResult();
    	
		List <String> binds = result.getBindingNames();
		Vector <String []> rows = new Vector<String[]>();
		while(result.hasNext()){
				BindingSet current = result.next();
				String [] curRow = new String[binds.size()];
				for(int i=0;i<binds.size();i++){
					Value curVal = current.getValue(binds.get(i));
					curRow[i] = getCleanLiterals(curVal);
				}
				rows.add(curRow);
		}
		
		
    	return rows.toArray(new String[0][0]);
    }
    private String getCleanLiterals(Value full_value) {
        if (full_value != null) {
          if (full_value.getClass().toString().equalsIgnoreCase("class org.openrdf.model.impl.LiteralImpl")) {
            LiteralImpl lit = (LiteralImpl) full_value;
            return lit.getLabel();
          }
        }
        return full_value.toString();
      }

	public String[][] getRepositoriesLabel() throws SystemQueryException {
		try {
			String format = getServerType(server);
			////System.out.println("Server type : " + format);
			method = new GetMethod(server);
			setupMethodAndQueryString(Q_NAMEGRAPHS_LABEL, method, format);
			//System.out.println("Sending repo query");
			int status = http_client.executeMethod(method);
			//System.out.println("Status : " + status + " " + HttpURLConnection.HTTP_OK);	
			String res[][] = processResultToStringArray(method, format);
			
			//System.out.println("Before unique : " + res.length);
		    HashMap<String, String[]> unique= new HashMap<String, String[]>();	
			

			for (int i = 0; i < res.length; i++){
				unique.put(res[i][0], res[i]);
			}
			//System.out.println("After unique : " + unique.size());
			// Returning at most 100 unique namedgraphs
			String [][] ret = new String[Math.max(101, unique.size())][];
			int i=0;	
			for(String[] row : unique.values()){
				ret[i++] = row;
				if(i == 100) break;
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return new String[0][0];
		}

	}
	public String[] getRepositories() throws SystemQueryException {

		try {
			String format = getServerType(server);
			////System.out.println("Server type : " + format);
			method = new GetMethod(server);
			setupMethodAndQueryString(Q_NAMEGRAPHS, method, format);
			//System.out.println("Sending repo query");
			int status = http_client.executeMethod(method);
			//System.out.println("Status : " + status + " " + HttpURLConnection.HTTP_OK);	
			String res[][] = processResultToStringArray(method, format);
			
			//System.out.println("Before unique : " + res.length);
		    HashSet<String> unique= new HashSet<String>();	
			

			for (int i = 0; i < res.length; i++){
				unique.add(res[i][0]);
			}
			//System.out.println("After unique : " + unique.size());
			// Returning at most 100 unique namedgraphs
			String [] ret = new String[Math.max(101, unique.size())];

			// Allow user to run query against all repo available
			// This should be without from clause
			ret[0] = "All";
			int i =1;
			for(String s : unique){
				ret[i++] = s;
				if(i == 100) break;
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return new String[0];
		}

	}

	public String[][] getRepositoriesLabel(String sparql_endpoint, String username,
			String password, String read_write) throws SystemQueryException {

		server = sparql_endpoint;
	
		return getRepositoriesLabel();

	}


	public String[] getRepositories(String sparql_endpoint, String username,
			String password, String read_write) throws SystemQueryException {

		server = sparql_endpoint;
	
		return getRepositories();

	}

	public String extractRdf() throws ExtractRdfException {
		return "";
	}

	public String extractRdf(String rdf_format) throws ExtractRdfException {
		return "";
	}

	public String extractRdf(String sparql_endpoint, String repository,
			String username, String password, String rdf_format)
			throws ExtractRdfException {
		return "";
	}

	public boolean clear() throws ClearRepositoryException {
		return true;
	}

	public boolean clear(String sparql_endpoint, String repository,
			String username, String password) throws ClearRepositoryException {
		return true;
	}

	public boolean addRdf(String data_uri, String data) throws AddRdfException {
		return true;
	}

	public boolean addRdf(String sparql_endpoint, String repository,
			String username, String password, String rdf_format,
			String data_uri, String data) throws AddRdfException {
		return true;
	}

	public boolean addRdf(String data_uri, String data, String context)
			throws AddRdfException {
		return true;
	}

	public boolean addRdf(String sparql_endpoint, String repository,
			String username, String password, String rdf_format,
			String data_uri, String data, String context)
			throws AddRdfException {
		return true;
	}

	public boolean addRdfFile(String data_uri) throws AddRdfException {
		return true;
	}

	public boolean addRdfFile(String sparql_endpoint, String repository,
			String username, String password, String rdf_format, String data_uri)
			throws AddRdfException {
		return true;
	}

	public boolean addRdfFile(String data_uri, String context)
			throws AddRdfException {
		return true;
	}

	public boolean addRdfFile(String sparql_endpoint, String repository,
			String username, String password, String rdf_format,
			String data_uri, String context) throws AddRdfException {
		return true;
	}

	public boolean addRdfStatement(String subject, String predicate,
			String object) throws AddRdfException {
		return true;
	}

	public boolean addRdfStatement(String sparql_endpoint, String repository,
			String username, String password, String subject, String predicate,
			String object) throws AddRdfException {
		return true;
	}

	public boolean addRdfStatement(String subject, String predicate,
			String object, String context) throws AddRdfException {
		return true;
	}

	public boolean addRdfStatement(String sparql_endpoint, String repository,
			String username, String password, String subject, String predicate,
			String object, String context) throws AddRdfException {
		return true;
	}

	public boolean removeRdf(String data_uri, String data)
			throws RemoveRDFException {
		return true;
	}

	public boolean removeRdf(String sparql_endpoint, String repository,
			String username, String password, String rdf_format,
			String data_uri, String data) throws RemoveRDFException {
		return true;
	}

	public boolean removeRdf(String data_uri, String data, String context)
			throws RemoveRDFException {
		return true;
	}

	public boolean removeRdf(String sparql_endpoint, String repository,
			String username, String password, String rdf_format,
			String data_uri, String data, String context)
			throws RemoveRDFException {
		return true;
	}

	public boolean removeRdfFile(String data_uri) throws RemoveRDFException {
		return true;
	}

	public boolean removeRdfFile(String sparql_endpoint, String repository,
			String username, String password, String format, String data_uri)
			throws RemoveRDFException {
		return true;
	}

	public boolean removeRdfFile(String data_uri, String context)
			throws RemoveRDFException {
		return true;
	}

	public boolean removeRdfFile(String sparql_endpoint, String repository,
			String username, String password, String format, String data_uri,
			String context) throws RemoveRDFException {
		return true;
	}

	public boolean removeRdfStatement(String subject, String predicate,
			String object) throws RemoveRDFException {
		return true;
	}

	public boolean removeRdfStatement(String sparql_endpoint,
			String repository, String username, String password,
			String subject, String predicate, String object)
			throws RemoveRDFException {
		return true;
	}

	public boolean removeRdfStatement(String subject, String predicate,
			String object, String context) throws RemoveRDFException {
		return true;
	}

	public boolean removeRdfStatement(String sparql_endpoint,
			String repository, String username, String password,
			String subject, String predicate, String object, String context)
			throws RemoveRDFException {
		return true;
	}

	public String selectQuery(String query) throws QueryException {

		try {
			
			method= new GetMethod(server);
			String format = getServerType(server);
			setupMethodAndQueryString(query, method, format);
			
			int status = http_client.executeMethod(method);
			// need to interpret status and throws exception if necessary
			java.io.InputStream resp = method.getResponseBodyAsStream();
			StringBuffer sb = new StringBuffer();

			int c = resp.read();
			while (c > 0) {
				sb.append((char) c);
				c = resp.read();
			}

			return sb.toString();

		} catch (Exception e) {
			return null;
		}
	}

	// Returning raw string ?
	public String selectQuery(String sparql_endpoint, String repository,
			String username, String password, String query_language,
			String select_output_format, String query) throws QueryException {

		try {
			method = new GetMethod(sparql_endpoint);
			String format = getServerType(sparql_endpoint);
			setupMethodAndQueryString(query, method, format);
			int status = http_client.executeMethod(method);
			
			// need to interpret status and throws exception if necessary

			java.io.InputStream resp = method.getResponseBodyAsStream();
			StringBuffer sb = new StringBuffer();

			int c = resp.read();
			while (c > 0) {
				sb.append((char) c);
				c = resp.read();
			}

			return sb.toString();

		} catch (Exception e) {
			return null;
		}
	}

	public String[][] selectQuery(String sparql_endpoint, String repository,
			String username, String password, String query_language,
			String query) throws QueryException {

		//System.out.println("Q: "+query);
		try {
			method = new GetMethod(sparql_endpoint);
			String format = getServerType(sparql_endpoint);
			//System.out.println(sparql_endpoint + " is of type " + format);
			setupMethodAndQueryString(query, method, format);
			int status = http_client.executeMethod(method);
			
			String res[][] = processResultToStringArray(method, format);
			
			return res;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public String constructQuery(String query) throws QueryException {
		return "";
	}

	public String constructQuery(String server, String repository,
			String username, String password, String query_language,
			String rdf_format, String query) throws QueryException {
		return "";
	}

	String[][] jsonResultToStringArray(Reader reader) throws Exception {
		String[][] result = null;
		JSONObject out = (JSONObject) new JSONParser().parse(reader);
		JSONArray vars = (JSONArray) ((JSONObject) out.get("head")).get("vars");

		JSONObject results = (JSONObject) out.get("results");
		JSONArray bindings = (JSONArray) results.get("bindings");

		int nCol = vars.size();
		int nRow = bindings.size();

		result = new String[nRow][nCol];

		for (int i = 0; i < nRow; i++) {
			for (int j = 0; j < nCol; j++) {
				JSONObject cur = (JSONObject) bindings.get(i);

				if (vars.get(j) == null)
					continue;
				String curVar = vars.get(j).toString();

				if (cur.get(curVar) == null)
					continue;

				JSONObject curCol = (JSONObject) cur.get(curVar);

				if (curCol.get("value") == null)
					continue;
				result[i][j] = curCol.get("value").toString();

			}
		}

		return result;
	}
}
