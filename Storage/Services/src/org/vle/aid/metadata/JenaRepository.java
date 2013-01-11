/*
 * JenaRepository.java
 *
 * Created on January 30, 2006, 11:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.metadata;

import com.hp.hpl.jena.rdf.arp.lang.LanguageTag;
import java.io.*;
import java.util.List;
import java.util.Vector;
import java.util.regex.*;
import java.net.URI;

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.util.iterator.NiceIterator;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.query.*;
import org.vle.aid.metadata.exception.AddRdfException;
import org.vle.aid.metadata.exception.RemoveRDFException;

/**
 * A stub class that can be filled-in to allow Jena support in the future.
 * See the documentation of {@link SesameRepository} for details.
 * @author wrvhage
 */
public class JenaRepository implements Repository {

    private static String M_DB = "MySQL";
    private static String M_DBDRIVER_CLASS = "com.mysql.jdbc.Driver";
    
    private String server = "";
    private String repository = "";
    private String username = "";
    private String password = "";

    private String rdf_format = "rdfxml";
    
    private String query_language = "http://jena.hpl.hp.com/2003/07/query/RDQL";
    private String select_output_format = "html_table";
    
    /**
     * Creates a new instance of JenaRepository
     */
    public JenaRepository() { }
    
    public JenaRepository(
            String server, String repository,
            String username, String password) {
        setServer(server);
        setRepository(repository);
        setUsername(username);
        setPassword(password);
    }
    
    public JenaRepository(
            String server, String repository,
            String username, String password,
            String rdf_format) {
        setServer(server);
        setRepository(repository);
        setUsername(username);
        setPassword(password);
        setRdfFormat(rdf_format);
    }
    
    public String getServer() { return this.server; }
    public void setServer(String server) { this.server = server; }
    public String getRepository() { return this.repository; }
    public void setRepository(String repository) { this.repository = repository; }
    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }

    public String getRdfFormat() { return this.rdf_format; }
    public void setRdfFormat(String rdf_format) { this.rdf_format = rdf_format; }

    public String getQueryLanguage() { return this.query_language; }
    public void setQueryLanguage(String query_language) { this.query_language = query_language; }
    public String getSelectOutputFormat() { return this.select_output_format; }
    public void setSelectOutputFormat(String select_output_format) { this.select_output_format = select_output_format; }

    private DBConnection _login(String server_url, String username, String password) {
        try {
            Class.forName(M_DBDRIVER_CLASS);
            return new DBConnection(server_url,username,password,M_DB);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    private Model _assertRepository(String server_url, String repository, String username, String password) {
        Model rv = null;
        DBConnection conn = _login(getServer(),getUsername(),getPassword());
        ModelMaker maker = ModelFactory.createModelRDBMaker(conn);    
        if(conn.containsModel(repository)) {
            rv = maker.openModel(repository);
        } else {
            rv = maker.createModel(repository);
        }
        return rv;
    }
    
    private Resource _parseValue(String value) {
        Model m = _assertRepository(getServer(),getRepository(),getUsername(),getPassword());
        Resource rv = null;
        if (value != null) {
            try {
                /*
                 * if this statement fails, it's probably a literal and not a URI
                 * in which case we should go to the catch part of this try.
                 */                
                java.net.URI obj_uri = new java.net.URI(value);
                rv = m.createResource(value);
            } catch (Exception e) {
                Pattern pdt = Pattern.compile("^\"(.*)\"\\^\\^<(.*)>$");
                Pattern plang = Pattern.compile("^\"(.*)\"@(\\w+)$");
                Matcher mdt = pdt.matcher(value);
                Matcher mlang = plang.matcher(value);
                if (mdt.matches()) {
                    String lit = mdt.group(1);
                    String dt = mdt.group(2);
             //       rv = m.createTypedLiteral(lit,dt);
                } else if (mlang.matches()) {
                    String lit = mlang.group(1);
                    String lang = mlang.group(2);
                    /*
                     * TODO: implement language for literals
                     */
              //      rv = m.createLiteral(lit);
                } else {
              //      rv = m.createLiteral(value);
                }
            }
        } else {
            rv = m.createResource();
        }
        return rv;
    }
    
    private Object _verifyRdfFormat(String format) {        
        return null;
    }
    
    private Object _verifyQueryLanguage(String query_language) {
        return null;
    }
            
    public String[] getRepositories() {
        return getRepositories(getServer(),getUsername(),getPassword(),"r");
    }
    

    public String[] getRepositories(String server_url, String username, String password, String read_write) {
        String[] rv = null;
        IDBConnection conn = _login(server_url,username,password);
        NiceIterator it = (NiceIterator)conn.getAllModelNames();
        Vector rvv = new Vector();
        while (it.hasNext()) {
            rvv.add((String)it.next());
        }
        rv = (String[])rvv.toArray();
        return rv;        
    }
    
    public String[][] getRepositoriesLabel() {
        String res[] = getRepositories(getServer(),getUsername(),getPassword(),"r");
		// Just a mockup for the time being
		String ret[][] = new String[res.length][2];
		for(int i=0;i<res.length;i++) ret[i] = new String[]{res[i],res[i]};

		return ret;

    }

    public String[][] getRepositoriesLabel(String server_url, String username, String password, String read_write) {
  	    String res[] = getRepositories(server_url, username, password, read_write);
		// Just a mockup for the time being
		String ret[][] = new String[res.length][2];
		for(int i=0;i<res.length;i++) ret[i] = new String[]{res[i],res[i]};

		return ret;
	}
    public boolean addRdf(String data_uri, String data) {
        return addRdf(getServer(),getRepository(),getUsername(),getPassword(),getRdfFormat(),data_uri,data);
    }
    
    public boolean addRdf(
            String server_url,String repository,
            String username,String password,
            String rdf_format,
            String data_uri,String data) {
        try {
        } catch (Exception e) { e.printStackTrace(); return false; }
        
        return true;
    
    }

    public boolean addRdfStatement(
            String subject, String predicate, String object) {
        return addRdfStatement(getServer(),getRepository(),getUsername(),getPassword(),subject,predicate,object);
    }
    
    public boolean addRdfStatement(
            String subject, String predicate, String object, String context) {
        return addRdfStatement(getServer(),getRepository(),getUsername(),getPassword(),subject,predicate,object,context);
    }
    
    public boolean addRdfStatement(
            String server_url,String repository,
            String username,String password,
            String subject, String predicate, String object) {
        try {
            Model m = _assertRepository(server_url,repository,username,password);
            Resource subj = null;
            if (subject == null) {
                subj = ResourceFactory.createResource();
            } else {
                subj = ResourceFactory.createResource(subject);
            }
            Property pred = ResourceFactory.createProperty(predicate);
            Resource obj = _parseValue(object);
            Statement s = m.createStatement(subj,pred,obj);
            m.add(s);
        } catch (Exception e) { e.printStackTrace(); return false; }

        return true;

    }
    
    public boolean addRdfStatement(
            String server_url,String repository,
            String username,String password,
            String subject, String predicate, String object, String context) {
        try {
            Model m = _assertRepository(server_url,repository,username,password);
            Resource subj = null;
            if (subject == null) {
                subj = m.createResource();
            } else {
                subj = m.createResource(subject);
            }
            Property pred = m.createProperty(predicate);
            Resource obj = _parseValue(object);
            Statement s = m.createStatement(subj,pred,obj);
            /*
             * context is implemented as a Bag of reified statements
            ReifiedStatement rs = m.createReifiedStatement(s);
            Resource c = m.createResource(context);
            m.add
            m.add(rs,m.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#li"),c);
              
             */

        } catch (Exception e) { e.printStackTrace(); return false; }

        return true;

    }    

    
    public boolean addRdfFile(String data_uri) {
        return addRdfFile(getServer(),getRepository(),getUsername(),getPassword(),getRdfFormat(),data_uri);
    }

    public boolean addRdfFile(
            String server_url,String repository,
            String username,String password,
            String rdf_format,
            String data_uri) {
        try {
        } catch (Exception e) { e.printStackTrace(); return false; }

        return true;

    }
    
    public boolean removeRdf(String data_uri, String data) {
        return removeRdf(getServer(),getRepository(),getUsername(),getPassword(),getRdfFormat(),data_uri,data);
    }

    public boolean removeRdf(
            String server_url, String repository,
            String username, String password,
            String rdf_format,
            String data_uri, String data) {
        try {
        } catch (Exception e) { e.printStackTrace(); return false; }

        return true;

    }
     
    public boolean removeRdfStatement(
            String subject, String predicate, String object) {
        return removeRdfStatement(getServer(),getRepository(),getUsername(),getPassword(),subject,predicate,object);
    }

    public boolean removeRdfStatement(
            String subject, String predicate, String object, String context) {
        return removeRdfStatement(getServer(),getRepository(),getUsername(),getPassword(),subject,predicate,object,context);
    }

    public boolean removeRdfStatement(
            String server_url,String repository,
            String username,String password,
            String subject, String predicate, String object) {
        try {
        } catch (Exception e) { e.printStackTrace(); return false; }

        return true;

    }

    public boolean removeRdfStatement(
            String server_url, String repository,
            String username, String password,
            String subject, String predicate, String object, String context) {
        try {
        } catch (Exception e) { e.printStackTrace(); return false; }

        return true;

    }

    public boolean removeRdfFile(String data_uri) {
        return removeRdfFile(getServer(),getRepository(),getUsername(),getPassword(),getRdfFormat(),data_uri);
    }

    public boolean removeRdfFile(
            String server_url, String repository,
            String username, String password,
            String format,
            String data_uri) {
        try {
        } catch (Exception e) { e.printStackTrace(); return false; }

        return true;

    }

    public boolean clear() {
        return clear(getServer(),getRepository(),getUsername(),getPassword());
    }

    public boolean clear(
            String server_url,String repository,
            String username,String password) {
        try {
        } catch (Exception e) { e.printStackTrace(); return false; }

        return true;

    }
    
    public String extractRdf() {
        return extractRdf(getServer(),getRepository(),getUsername(),getPassword(),getRdfFormat());
    }

    public String extractRdf(String rdf_format) {
        return extractRdf(getServer(),getRepository(),getUsername(),getPassword(),rdf_format);
    }

    public String extractRdf(
            String server_url, String repository,
            String username, String password,
            String rdf_format) {

        InputStream is = null;
        String rv = "";
        
        try {
        } catch (Exception e) { e.printStackTrace(); return null; }

        return rv;

    }
    
    /*
     * construct query
     */
    
    public String constructQuery(
            String server, String repository,
            String username, String password,
            String query_language, String rdf_format,
            String query) {
        String rv = null;
        try {
            /*
            Query q = QueryFactory.create(query);
            //QueryFactory.create()

            // Execute the query and obtain results
            QueryExecution qe = QueryExecutionFactory.create(q, model);
            ResultSet results = qe.execSelect();

            // Output query results	
            ResultSetFormatter.out(System.out, results, query);

            // Important - free up resources used running the query
            qe.close();
             */
        } catch (Exception e) {}
        return rv;        
    }
    
    public String constructQuery(String query) {
        return constructQuery(getServer(),getRepository(),getUsername(),getPassword(),getQueryLanguage(),getRdfFormat(),query);
    }
    
    public void constructQuery(String query,OutputStream out) {
        constructQuery(getServer(),getRepository(),getUsername(),getPassword(),getQueryLanguage(),getRdfFormat(),query,out);
    }

    public void constructQuery(
            String server_url, String repository,
            String username, String password,
            String query_language, String rdf_format,
            String query, 
            OutputStream os) {
        try {
        } catch (Exception e) { e.printStackTrace(); }
        
    }
    
    /*
     * select query
     */
    
    public String selectQuery(String query) {
        return selectQuery(getServer(),getRepository(),getUsername(),getPassword(),getQueryLanguage(),getSelectOutputFormat(),query);
    }
        
    public String selectQuery(
            String server_url, String repository,
            String username, String password,
            String query_language, String select_output_format,
            String query) {
        
        String rv = "";
        
        try {           
        } catch (Exception e) { e.printStackTrace(); }
        
        return rv;
    }
    
    
    public String[][] selectQuery(
            String server_url, String repository,
            String username, String password,
            String query_language,
            String query) {
        
        try {           
        } catch (Exception e) { e.printStackTrace(); }
        
        return null;
    }

    public boolean addRdf(String server_url, String repository, String username, String password, String rdf_format, String data_uri, String data, String context) throws AddRdfException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addRdfFile(String server_url, String repository, String username, String password, String rdf_format, String data_uri, String context) throws AddRdfException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeRdf(String server_url, String repository, String username, String password, String rdf_format, String data_uri, String data, String context) throws RemoveRDFException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeRdfFile(String server_url, String repository, String username, String password, String format, String data_uri, String context) throws RemoveRDFException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addRdf(String data_uri, String data, String context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addRdfFile(String data_uri, String context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeRdf(String data_uri, String data, String context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeRdfFile(String data_uri, String context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
 
    public boolean getVirtuoso() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setVirtuoso() throws org.vle.aid.metadata.exception.QueryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
