package org.vle.aid.metadata;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.vle.aid.metadata.exception.AddRdfException;
import org.vle.aid.metadata.exception.ClearRepositoryException;
import org.vle.aid.metadata.exception.ExtractRdfException;
import org.vle.aid.metadata.exception.QueryException;
import org.vle.aid.metadata.exception.RemoveRDFException;
import org.vle.aid.metadata.exception.SystemQueryException;
import org.vle.aid.virtuoso.Binding;
import org.vle.aid.virtuoso.Result;
import org.vle.aid.virtuoso.Sparql;
import org.vle.aid.virtuoso.Variable;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;

public class RestRepository implements Repository {
  

  private String server = "";
  private String repository = "";
  private String username = "";
  private String password = "";
  private String rdf_format = "RDF/XML";
  private String query_language = "sparql";
  private String select_output_format = "html_table";
  
  private static final Client client = Client.create(); 
  
  private final static String getReposSparql = "SELECT DISTINCT ?src WHERE {GRAPH ?src {?s ?p ?o}}";
  private final static String getConceptSparql = "SELECT DISTINCT ?Concept WHERE {[] a ?Concept} limit 1";
  private final static String selectSparql = 
    /*
    "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
    "select distinct ?name  ?class " +
    "where {" +
    "    ?class <http://purl.org/dc/terms/isPartOf> ?name " +
    "} " +
    "LIMIT 100";
    */
    "SELECT ?class ?name " +
  		"WHERE {" +
  		"?class <http://www.w3.org/2000/01/rdf-schema#label> ?name" +
  		"}" +
  		"LIMIT 10";
  
  /**
   * Creates a new instance of RestRepository
   */
  public RestRepository() throws QueryException {
    
  }
  
  /**
   * Creates a new instance of RestRepository
   */
  public RestRepository(String url) throws QueryException {
    this.server = url;
  }
  
  /**
   * Creates a new instance of RestRepository and sets default values
   * for the server URL, repository name, username, and password.
   * i.e. the URL where Sesame2 runs (e.g. http://localhost:8080/sesame),
   * the Sesame repository to use (e.g. mem-rdfs-db), and the username
   * and password that allow access to this repository.
   */
  public RestRepository(
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
  public RestRepository(
      String server, String repository,
      String username, String password,
      String rdf_format) {
    setServer(server);
    setRepository(repository);
    setUsername(username);
    setPassword(password);
    setRdfFormat(rdf_format);
  }

  
  @Override
  public boolean addRdf(String dataUri, String data) throws AddRdfException {
    throw new AddRdfException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean addRdf(String serverUrl, String repository, String username,
      String password, String rdfFormat, String dataUri, String data)
      throws AddRdfException {
    throw new AddRdfException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean addRdf(String dataUri, String data, String context)
      throws AddRdfException {
    throw new AddRdfException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean addRdf(String serverUrl, String repository, String username,
      String password, String rdfFormat, String dataUri, String data,
      String context) throws AddRdfException {
    throw new AddRdfException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean addRdfFile(String dataUri) throws AddRdfException {
    throw new AddRdfException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean addRdfFile(String serverUrl, String repository,
      String username, String password, String rdfFormat, String dataUri)
      throws AddRdfException {
    throw new AddRdfException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean addRdfFile(String dataUri, String context)
      throws AddRdfException {
    throw new AddRdfException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean addRdfFile(String serverUrl, String repository,
      String username, String password, String rdfFormat, String dataUri,
      String context) throws AddRdfException {
    throw new AddRdfException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean addRdfStatement(String subject, String predicate, String object)
      throws AddRdfException {
    throw new AddRdfException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean addRdfStatement(String serverUrl, String repository,
      String username, String password, String subject, String predicate,
      String object) throws AddRdfException {
    throw new AddRdfException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean addRdfStatement(String subject, String predicate,
      String object, String context) throws AddRdfException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean addRdfStatement(String serverUrl, String repository,
      String username, String password, String subject, String predicate,
      String object, String context) throws AddRdfException {
    throw new AddRdfException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean clear() throws ClearRepositoryException {
    throw new ClearRepositoryException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean clear(String serverUrl, String repository, String username,
      String password) throws ClearRepositoryException {
    throw new ClearRepositoryException("Not implemented yet.", new IOException());
  }

  @Override
  public String constructQuery(String query) throws QueryException {
    throw new QueryException("Not implemented yet.", new IOException());
  }

  @Override
  public String constructQuery(String server, String repository,
      String username, String password, String queryLanguage, String rdfFormat,
      String query) throws QueryException {
    throw new QueryException("Not implemented yet.", new IOException());
  }

  @Override
  public String extractRdf() throws ExtractRdfException {
    throw new ExtractRdfException("Not implemented yet.", new IOException());
  }

  @Override
  public String extractRdf(String rdfFormat) throws ExtractRdfException {
    throw new ExtractRdfException("Not implemented yet.", new IOException());
  }

  @Override
  public String extractRdf(String serverUrl, String repository,
      String username, String password, String rdfFormat)
      throws ExtractRdfException {
    throw new ExtractRdfException("Not implemented yet.", new IOException());
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public String getQueryLanguage() {
    return this.query_language;
  }

  @Override
  public String getRdfFormat() {
    return this.rdf_format;
  }

  @Override
  public String getRepository() {
    return this.repository;
  }

  @Override
  public String getSelectOutputFormat() {
    return this.select_output_format;
  }

  @Override
  public String getServer() {
    return this.server;
  }

  @Override
  public String getUsername() {
    return this.username;
  }

  @Override
  public boolean getVirtuoso() {
    return false;
  }

  @Override
  public boolean removeRdf(String dataUri, String data)
      throws RemoveRDFException {
    throw new RemoveRDFException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean removeRdf(String serverUrl, String repository,
      String username, String password, String rdfFormat, String dataUri,
      String data) throws RemoveRDFException {
    throw new RemoveRDFException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean removeRdf(String dataUri, String data, String context)
      throws RemoveRDFException {
    throw new RemoveRDFException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean removeRdf(String serverUrl, String repository,
      String username, String password, String rdfFormat, String dataUri,
      String data, String context) throws RemoveRDFException {
    throw new RemoveRDFException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean removeRdfFile(String dataUri) throws RemoveRDFException {
    throw new RemoveRDFException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean removeRdfFile(String serverUrl, String repository,
      String username, String password, String format, String dataUri)
      throws RemoveRDFException {
    throw new RemoveRDFException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean removeRdfFile(String dataUri, String context)
      throws RemoveRDFException {
    throw new RemoveRDFException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean removeRdfFile(String serverUrl, String repository,
      String username, String password, String format, String dataUri,
      String context) throws RemoveRDFException {
    throw new RemoveRDFException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean removeRdfStatement(String subject, String predicate,
      String object) throws RemoveRDFException {
    throw new RemoveRDFException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean removeRdfStatement(String serverUrl, String repository,
      String username, String password, String subject, String predicate,
      String object) throws RemoveRDFException {
    throw new RemoveRDFException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean removeRdfStatement(String subject, String predicate,
      String object, String context) throws RemoveRDFException {
    throw new RemoveRDFException("Not implemented yet.", new IOException());
  }

  @Override
  public boolean removeRdfStatement(String serverUrl, String repository,
      String username, String password, String subject, String predicate,
      String object, String context) throws RemoveRDFException {
    throw new RemoveRDFException("Not implemented yet.", new IOException());
  }

  @Override
  public String selectQuery(String query) throws QueryException {
    

    
    
    
    
    throw new QueryException("Not implemented yet.", new IOException());
  }

  @Override
  public String selectQuery(String serverUrl, String repository,
      String username, String password, String queryLanguage,
      String selectOutputFormat, String query) throws QueryException {
    
    
    
    throw new QueryException("Not implemented yet.", new IOException());
    
  }

  @Override
  public String[][] selectQuery(String serverUrl, String repository,
      String username, String password, String queryLanguage, String query)
      throws QueryException {

    URI uri;
    try {
      uri = UriBuilder
          .fromPath(serverUrl)
          //.queryParam("debug", "on")
          .queryParam("format", "XML")
          //.queryParam("format", "application/rdf+xml")
          //.queryParam("named-graph-uri", "")
          .queryParam("formatting", "Raw")
          .queryParam("default-graph-uri", "")
          .queryParam("query", URLEncoder.encode(query, "UTF-8"))
        .build();
    } catch (Exception e) {
      throw new QueryException("Error with select query", e);
    } 
    
    System.err.println(uri.toASCIIString());
    //WebResource wr = client.resource(uri);
    Sparql rs;
    String[][] resultTable = new String[0][0];
    
    try {
      rs = client.resource(uri).accept(MediaType.APPLICATION_XML).post(Sparql.class);
      
      List<String> bindingNames = new ArrayList<String>();
      
      for (Variable v : rs.getHead().getVariable()) {
        bindingNames.add(v.getName());
      }
      int columnCount = bindingNames.size();
      Vector<String[]> result_vectors = new Vector<String[]>();
      
      for (Result r : rs.getResults().getResult()) {
        
        String[] values = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
          Binding b = r.getBinding().get(1);
          values[i] = b.getUri();
        }
        result_vectors.add(values);
      }
      
      int rowCount = result_vectors.size();
      resultTable = new String[rowCount][columnCount];
  
      for (int row = 0; row < rowCount; row++) {
        String[] rowValues = result_vectors.get(row);
        for (int column = 0; column < columnCount; column++) {
          resultTable[row][column] = rowValues[column];
        }
      }
    } catch (UniformInterfaceException e) {
      System.err.println("Error connecting to " + serverUrl + ", " + e.getMessage());
    }

    return resultTable;
  }

  @Override
  public void setPassword(String password) {
    this.password = password; 
  }

  @Override
  public void setQueryLanguage(String queryLanguage) {
    this.query_language = queryLanguage;
  }

  @Override
  public void setRdfFormat(String rdfFormat) {
    this.rdf_format = rdfFormat;
  }

  @Override
  public void setRepository(String repository) {
    this.repository = repository;
  }

  @Override
  public void setSelectOutputFormat(String selectOutputFormat) {
    this.select_output_format = selectOutputFormat;
  }

  @Override
  public void setServer(String server) {
    this.server = server;
  }

  @Override
  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public String[] getRepositories() throws SystemQueryException {
    return getRepositories(getServer(), getUsername(), getPassword(), "r");
  }

  @Override
  public String[] getRepositories(String serverUrl, String username,
      String password, String readWrite) throws SystemQueryException {
    try {
      return executeQuery(RestRepository.getReposSparql);
    } catch (Exception e) {
      throw new SystemQueryException("Error getting repositories", e);
    }
  }
 
  public String[][] getRepositoriesLabel() throws SystemQueryException{
        String res[] = getRepositories(getServer(),getUsername(),getPassword(),"r");
		// Just a mockup for the time being
		String ret[][] = new String[res.length][2];
		for(int i=0;i<res.length;i++) ret[i] = new String[]{res[i],res[i]};

		return ret;

    }

  public String[][] getRepositoriesLabel(String server_url, String username, String password, String read_write) throws SystemQueryException {
  	    String res[] = getRepositories(server_url, username, password, read_write);
		// Just a mockup for the time being
		String ret[][] = new String[res.length][2];
		for(int i=0;i<res.length;i++) ret[i] = new String[]{res[i],res[i]};

		return ret;
	}
  @Override
  public void setVirtuoso() throws QueryException {
    // TODO Auto-generated method stub
    
  }
  
  private String[] executeQuery(String q) throws IllegalArgumentException, UriBuilderException, UnsupportedEncodingException {
    
    TreeSet<String> out = new TreeSet<String>();
    URI uri = UriBuilder
        .fromPath(this.server)
        //.queryParam("debug", "on")
        .queryParam("format", "XML")
        .queryParam("formatting", "Raw") // myExperiment
        //.queryParam("format", "application/rdf+xml")
        //.queryParam("named-graph-uri", "")
        .queryParam("default-graph-uri", "")
        .queryParam("query", URLEncoder.encode(q, "UTF-8"))
      .build();
    
System.err.println(uri.toASCIIString());
    
    //WebResource wr = client.resource(uri);
    Sparql rs = client.resource(uri).accept(MediaType.APPLICATION_XML).post(Sparql.class);
    
    for (Result r : rs.getResults().getResult()) {
      for (Binding b : r.getBinding()) {
        out.add(b.getUri());
      }
    }
    
    return out.toArray(new String[0]);
  }

  // http://aruld.info/yahoo-search-restful-client-using-jersey/
  public static void main(String... args) {
    
    // try out a bunch of URLs
    for (String server : new String[] {
        "http://rodos.zoo.ox.ac.uk/sparql/"
        ,"http://rdf.myexperiment.org/sparql"
      }) 
    {
      try {
        
        System.out.println("  [Opening server]");
        RestRepository rr = new RestRepository(server);
        
        System.out.println("  [Listing reps]");
        try {
          for (String r : rr.getRepositories()) {
            System.out.println("  repository: " + r);
          }
        } catch (SystemQueryException e) {
          System.err.println("error listing repositories, " + e.getMessage());
        }
        
        // do a simple select query
        System.out.println("  [select query]");
        String[][] rv = rr.selectQuery(rr.getServer(), "", "", "", "", RestRepository.selectSparql);
        String[][] rv2 = new String[rv.length][2];
        System.out.println(rv2.length + " hits");
        
        for (int k=0; k<rv.length; k++) {
          for (int m=0; m < rv[k].length; m++) {
            System.out.println("    " + rv[k][m]);
          }
        }
  
        for(int i=0;i<rv2.length;i++) // without an rdfs:label retrieved, the second element substitutes the label
        {
            rv2[i][0] = rv[i][0];
  
            if(rv[i][1]==null)
            {
              
                if(rv[i][0] != null && rv[i][0].contains("://"))
                {
                    if(rv[i][0].contains("#"))
                        rv2[i][1] = rv[i][0].substring(rv[i][0].lastIndexOf("#")+1);
                    else
                    {
                        String sub = rv[i][0].substring(rv[i][0].lastIndexOf("/"));
                        rv2[i][1] = sub.substring(sub.lastIndexOf("/")+1);
                    }
                }
                else
                    rv2[i][1] = rv[i][0];
            }
            else
                rv2[i][1] = rv[i][1];
            
            System.out.println("    " + rv2[i][0] + " - " + rv2[i][1]);
        }
        
        
      } catch (Exception e) {
       
        e.printStackTrace();
      }
    }
  }  
}
