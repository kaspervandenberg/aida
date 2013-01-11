/*
 * SesameRepository.java
 *
 * Created on January 30, 2006, 11:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.metadata;

import java.io.*;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.net.URI;
import java.util.regex.*;

import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.repository.*;
import org.openrdf.sesame.repository.local.*;
import org.openrdf.sesame.query.*;
import org.openrdf.model.*;
import org.openrdf.model.impl.*;
import org.openrdf.sesame.admin.AdminMsgCollector;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.config.DefaultRepositoryInfo;


/**
 * This class implements all {@link Repository} functionality on top of the Sesame API.
 * @author wrvhage
 */
public class SesameRepository implements Repository {

    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SesameRepository.class);
    
    private String server = "";
    private String repository = "";
    private String username = "";
    private String password = "";

    private String rdf_format = "rdfxml";
    
    private String query_language = "serql";
    private String select_output_format = "html_table";
    
    /**
     * Creates a new instance of SesameRepository
     */
    public SesameRepository() { }
    
    /**
     * Creates a new instance of SesameRepository and sets default values
     * for the server URL, repository name, username, and password.
     * i.e. the URL where Sesame runs (e.g. http://localhost:8080/sesame),
     * the Sesame repository to use (e.g. mem-rdfs-db), and the username
     * and password that allow access to this repository.
     */
    public SesameRepository(
            String server, String repository,
            String username, String password) {
        setServer(server);
        setRepository(repository);
        setUsername(username);
        setPassword(password);
    }
    
    /**
     * See above, but also sets a default value for the RDF serialization format that is used to output RDF of, for example, CONSTRUCT queries and exports.
     * @param rdf_format rdfxml, ntriples, turtle, n3, etc.
     */
    public SesameRepository(
            String server, String repository,
            String username, String password,
            String rdf_format) {
        setServer(server);
        setRepository(repository);
        setUsername(username);
        setPassword(password);
        setRdfFormat(rdf_format);
    }
    
    /**
     * These methods allow you to get or set the current value of the Sesame login information variables.
     * e.g. setServer("http://localhost:8080/sesame") sets the Repository to use the Sesame server at http://localhost:8080/sesame for the next request.
     */
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
    /**
     * @param query_language Sets the query language to use for the next SELECT or CONSTRUCT query. Possible values are: "serql", "rdql", "rql", etc.
     */
    public void setQueryLanguage(String query_language) { this.query_language = query_language; }

    public String getSelectOutputFormat() { return this.select_output_format; }
    /**
     * @param select_output_format Sets the format to use for the output of SELECT queries. Currently, the only supported value is "html_table".
     */
    public void setSelectOutputFormat(String select_output_format) { this.select_output_format = select_output_format; }

    /**
     * Logs in on a Sesame server. (No repository specified yet.)
     */
    private SesameService _login(String server_url, String username, String password) {
        SesameService service = null;
        try {    
            String s = server_url;
            if (s.equals("")) {
                s = "http://localhost:8080/sesame/";
            }
            service = Sesame.getService(new java.net.URL(s));
            
            // login is facultative
            if (!username.equals("")) {
                service.login(username,password);
            }            
        } catch (Exception e) { e.printStackTrace(); }
        return service;
    }

    /**
     * Attempts to detect whether the parameter "value" is a URI or a literal.
     */
    // forced type
    private Object _parseValue(String value,ValueFactory vf,String type) {
        if (type == null) return null;
        Value rv = null;
        if (type.equals("LITERAL")) {
            Pattern pdt = Pattern.compile("^\"(.*)\"\\^\\^<?(.*)>?$");
            Pattern plang = Pattern.compile("^\"(.*)\"@(\\w+)$");
            Matcher mdt = pdt.matcher(value);
            Matcher mlang = plang.matcher(value);
            if (mdt.matches()) {
                String lit = mdt.group(1);
                String dt = mdt.group(2);
                rv = vf.createLiteral(lit,vf.createURI(dt));
            } else if (mlang.matches()) {
                String lit = mlang.group(1);
                String lang = mlang.group(2);
                rv = vf.createLiteral(lit,lang);
            } else {
                rv = vf.createLiteral(value);
            }                    
        } else if (type.equals("URI")) {
            Pattern bracesp = Pattern.compile("^<(.*)>$");
            Matcher bracesm = bracesp.matcher(value);
            if (bracesm.matches()) {
                value = bracesm.group(1);
            }
            Pattern p = Pattern.compile("^(\\w+):\\/\\/([^\\/:\\?\\#]+)?(:(\\d+))?(\\/+[^\\?\\#]*)?(\\?([^\\#]*))?(\\#(.*))?\\s*$");
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
                        if (scheme.equals("file")) {
                            uri = new java.net.URI(scheme, "//" + path, fragment);
                        } else {
                            uri = new java.net.URI(scheme,host,path,query,fragment);
                        }
                    } else {
                        uri = new java.net.URI(scheme,userinfo,host,Integer.parseInt(port),path,query,fragment);
                    }
                    rv = vf.createURI(uri.toASCIIString());
                } catch (Exception e) { e.printStackTrace(); }            
            }
        }
        return rv;
    }

    // detect type
    private Object _parseValue(String value,ValueFactory vf) {
        Value rv = null;
        logger.debug("parsing value " + value);
        if (value != null) {
            Pattern p = Pattern.compile("^<?(\\w+):\\/\\/([^\\/:\\?\\#]+)?(:(\\d+))?(\\/+[^\\?\\#]*)?(\\?([^\\#]*))?(\\#(.*))?\\s*>?$");
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
                        if (scheme.equals("file")) {
                            uri = new java.net.URI(scheme, "//" + path, fragment);
                        } else {
                            uri = new java.net.URI(scheme,host,path,query,fragment);
                        }
                    } else {
                        uri = new java.net.URI(scheme,userinfo,host,Integer.parseInt(port),path,query,fragment);
                    }
                    rv = vf.createURI(uri.toASCIIString());
                } catch (Exception e) { e.printStackTrace(); }
            } else {
                Pattern pdt = Pattern.compile("^\"(.*)\"\\^\\^<?(.*)>?$");
                Pattern plang = Pattern.compile("^\"(.*)\"@(\\w+)$");
                Matcher mdt = pdt.matcher(value);
                Matcher mlang = plang.matcher(value);
                if (mdt.matches()) {
                    String lit = mdt.group(1);
                    String dt = mdt.group(2);
                    rv = vf.createLiteral(lit,vf.createURI(dt));
                } else if (mlang.matches()) {
                    String lit = mlang.group(1);
                    String lang = mlang.group(2);
                    rv = vf.createLiteral(lit,lang);
                } else {
                    rv = vf.createLiteral(value);
                }
            }
        } else {
            rv = vf.createBNode();
        }
        return rv;
    }
    
    /**
     * Verifies if the parameter "format" contains a supported value.
     */
    private RDFFormat _verifyRdfFormat(String format) {        
            String f = format;
            if (f.equals("")) {
                f = "rdfxml";
            }
            return RDFFormat.forValue(f.toLowerCase());
    }
    
    /**
     * Verifies if the parameter "query_language" contains a supported value.
     */
    private QueryLanguage _verifyQueryLanguage(String query_language) {
            String ql = query_language;
            if (ql.equals("")) {
                ql = "serql";
            }
            
            // QueryLanguage.forValue() does not work in Sesame 1.2.3
            QueryLanguage qlang = null;
            if (ql.equals("serql")) {
                qlang = QueryLanguage.SERQL;
            } else if (ql.equals("rql")) {
                qlang = QueryLanguage.RQL;
            } else if (ql.equals("rdql")) {
                qlang = QueryLanguage.RDQL;
            }
            return qlang;
    }
    
    /**
     * List all repositories of the current default server.
     * @return Array of repository names.
     */
    public String[] getRepositories() {
        return getRepositories(getServer(),getUsername(),getPassword(),"r");
    }
    
    public String[] getRepositories(String server_url, String username, String password, String read_write) {
         Vector rv = new Vector();
         try {    
            SesameService service = _login(server_url,username,password);
            RepositoryList rl = service.getRepositoryList();
            List l = null;
            String rw = read_write.toLowerCase();
            if (rw.equals("rw") || rw.equals("w")) {
                l = rl.getReadWriteRepositories();
            } else {
                l = rl.getReadableRepositories();
            }
            Iterator i = l.iterator();
            while (i.hasNext()) {
                String id = ((DefaultRepositoryInfo)i.next()).getRepositoryId();
                rv.add(id);
            }
            
        } catch (Exception e) { e.printStackTrace(); }
        String r[] = new String[rv.size()];
        rv.toArray(r);
        return r;
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
            SesameService service = _login(server_url,username,password);
            RDFFormat frm = _verifyRdfFormat(rdf_format);
            
            java.net.URL du = new java.net.URL(data_uri);
            String base_uri = data_uri + "#";
            boolean verify_data = true;
            
            StringBufferInputStream str = new StringBufferInputStream(data);
            org.openrdf.sesame.repository.SesameRepository rep = service.getRepository(repository);
            AdminMsgCollector a = new AdminMsgCollector();
            rep.addData(str, base_uri, frm, verify_data, a);

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
            SesameService service = _login(server_url,username,password);
            org.openrdf.sesame.repository.SesameRepository rep = service.getRepository(repository);

            Graph g = new GraphImpl();
            ValueFactory vf = g.getValueFactory();            
            Resource subj = (Resource)_parseValue(subject,vf);
            org.openrdf.model.URI pred = (org.openrdf.model.URI)_parseValue(predicate,vf,"URI");
            Value obj = (Value)_parseValue(object,vf);
            g.add(subj,pred,obj);

            rep.addGraph(g);
            
        } catch (Exception e) { e.printStackTrace(); return false; }
        
        return true;

    }
    
    public boolean addRdfStatement(
            String server_url,String repository,
            String username,String password,
            String subject, String predicate, String object, String context) {
        try {
            SesameService service = _login(server_url,username,password);
            org.openrdf.sesame.repository.SesameRepository rep = service.getRepository(repository);

            Graph g = new GraphImpl();
            ValueFactory vf = g.getValueFactory();            
            Resource subj = (Resource)_parseValue(subject,vf);
            org.openrdf.model.URI pred = (org.openrdf.model.URI)_parseValue(predicate,vf,"URI");
            Value obj = (Value)_parseValue(object,vf);

            /*
             * context implemented as rdf:Bag, statements added with reification
             * when stated in a context, a triple is not universally stated.
             * e.g. when I say snow is black it does not mean that snow is black
             * and it also does not mean that you say show is black.
             */
            // state that the context is a rdf:Bag
            g.add(vf.createURI(context),
                  vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                  vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag"));
            // create a blank node to symbolize the triple, define it to be a member of the rdf:Bag
            BNode triple = vf.createBNode();
            g.add(triple,
                  vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#li"),
                  vf.createURI(context));
            // state that the triple is a rdf:Statement and connect it to the subject, predicate, and object.
            g.add(triple,
                  vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                  vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement"));
            g.add(triple,
                  vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject"),
                  subj);
            g.add(triple,
                  vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate"),
                  pred);
            g.add(triple,
                  vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#object"),
                  obj);

            rep.addGraph(g);
            
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
            SesameService service = _login(server_url,username,password);
            org.openrdf.sesame.repository.SesameRepository rep = service.getRepository(repository);

            RDFFormat frm = _verifyRdfFormat(rdf_format);
            java.net.URL du = new java.net.URL(data_uri);
            String base_uri = data_uri + "#";
            boolean verify_data = true;
            AdminMsgCollector a = new AdminMsgCollector();

            rep.addData(du, base_uri, frm, verify_data, a);
            
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
            SesameService service = _login(server_url,username,password);
            org.openrdf.sesame.repository.SesameRepository rep = service.getRepository(repository);

            RDFFormat frm = _verifyRdfFormat(rdf_format);
            java.net.URL du = new java.net.URL(data_uri);
            String base_uri = data_uri + "#";
            boolean verify_data = true;
                  
            LocalService lservice = Sesame.getService();
            boolean inferencing = false;
            LocalRepository lrep = lservice.createRepository("tmp", inferencing);
            AdminMsgCollector a = new AdminMsgCollector();
            StringBufferInputStream str = new StringBufferInputStream(data);
            lrep.addData(str, base_uri, frm, verify_data, a);
            String q = "construct * from {s} p {o}";
            Graph g = lrep.performGraphQuery(QueryLanguage.SERQL, q);
      
            rep.removeGraph(g);
            
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
            SesameService service = _login(server_url,username,password);
            org.openrdf.sesame.repository.SesameRepository rep = service.getRepository(repository);
            
            Graph g = new GraphImpl();
            ValueFactory vf = g.getValueFactory();
            Resource subj = (Resource)_parseValue(subject,vf);
            org.openrdf.model.URI pred = (org.openrdf.model.URI)_parseValue(predicate,vf,"URI");
            Value obj = (Value)_parseValue(object,vf);
//            g.add(subj,pred,obj);
//            rep.removeGraph(g);
            AdminMsgCollector a = new AdminMsgCollector();
            rep.removeStatements(subj,pred,obj,a);
            if (a.hasErrors()) return false;

            /*
            String w = "where P = <" + predicate + ">";
            if (subject != null) {
                w += " and S = <" + subject + ">";
            }
            if (object != null) {
                try {
                    // if either of these two statements fail, it's probably a literal and not a URI
                    // in which case we should go to the catch part of this try.                                     
                    java.net.URI obj_uri = new java.net.URI(object);
                    new GraphImpl().getValueFactory().createURI(object);  
                    w += " and O = <" + object + ">";
                } catch (Exception e) {
                    w += " and O = " + object;
                }
            }
            rep.removeGraph(QueryLanguage.SERQL,
                "construct * from {S} P {O} " + w);
  */          
        } catch (Exception e) { e.printStackTrace(); return false; }
        
        return true;

    }

    public boolean removeRdfStatement(
            String server_url, String repository,
            String username, String password,
            String subject, String predicate, String object, String context) {
        try {
            SesameService service = _login(server_url,username,password);
            org.openrdf.sesame.repository.SesameRepository rep = service.getRepository(repository);

            String w = "where P = <" + predicate + ">";
            if (subject != null) {
                w += " and S = <" + subject + ">";
            }
            if (object != null) {
                try {
                    /*
                     * if either of these two statements fail, it's probably a literal and not a URI
                     * in which case we should go to the catch part of this try.
                     */                
                    java.net.URI obj_uri = new java.net.URI(object);
                    new GraphImpl().getValueFactory().createURI(object);  
                    w += " and O = <" + object + ">";
                } catch (Exception e) {
                    String obj = object;
                    if (!obj.startsWith("\"") && !obj.startsWith("\'")) {
                        w += " and O = \"" + object + "\"";
                    } else {
                        w += " and O = " + object;
                    }
                }
            }
            rep.removeGraph(QueryLanguage.SERQL,
                "construct * from " +
                "{T} rdf:subject  {S}, " +
                "{T} rdf:predicate  {P}, " +
                "{T} rdf:object  {O}, " +
                "{T} rdf:type  {<http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement>}, " +
                "{T} rdf:li {<" + context + ">} " + w);
            
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
            SesameService service = _login(server_url,username,password);
            org.openrdf.sesame.repository.SesameRepository rep = service.getRepository(repository);

            RDFFormat frm = _verifyRdfFormat(rdf_format);
            java.net.URL du = new java.net.URL(data_uri);
            String base_uri = data_uri + "#";
            boolean verify_data = true;
            
            LocalService lservice = Sesame.getService();
            boolean inferencing = false;
            LocalRepository lrep = lservice.createRepository("tmp", inferencing);            
            String q = "construct * from {s} p {o}";
            Graph g = lrep.performGraphQuery(QueryLanguage.SERQL, q);

            rep.removeGraph(g);
            
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
            SesameService service = _login(server_url,username,password);
            org.openrdf.sesame.repository.SesameRepository rep = service.getRepository(repository);
            AdminMsgCollector a = new AdminMsgCollector();
            rep.clear(a);

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
            SesameService service = _login(server_url,username,password);
            org.openrdf.sesame.repository.SesameRepository rep = service.getRepository(repository);

            RDFFormat frm = _verifyRdfFormat(rdf_format);

            boolean ontology = true;                        
            boolean instances = true;
            boolean explicitOnly = true;
            boolean niceOutput = true;
            is = rep.extractRDF(frm,ontology,instances,explicitOnly,niceOutput);

            int size = 1123123;
            byte buff[] = new byte[size];
            OutputStream xos = new ByteArrayOutputStream(size);

            int k;
            while ((k=is.read(buff)) != -1) {
                xos.write(buff,0,k);
                rv += xos.toString();
            }
            
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
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            constructQuery(server,repository,username,password,query_language,rdf_format,query,baos);
            rv = baos.toString();
            baos.close();
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
            SesameService service = _login(server_url,username,password);
            org.openrdf.sesame.repository.SesameRepository rep = service.getRepository(repository);
            
            RDFFormat frm = _verifyRdfFormat(rdf_format);
            QueryLanguage qlang = _verifyQueryLanguage(query_language);
            
            GraphQueryResultListener graphListener = new RdfGraphWriter(frm, os);
            rep.performGraphQuery(QueryLanguage.SERQL, query, graphListener);
            
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
            SesameService service = _login(server_url,username,password);
            org.openrdf.sesame.repository.SesameRepository rep = service.getRepository(repository);

            QueryLanguage qlang = _verifyQueryLanguage(query_language);
            
            String of = select_output_format;
            if (of.equals("")) {
                of = "html_table";
            }
            
            QueryResultsTable rt = rep.performTableQuery(qlang, query);

            int rowCount = rt.getRowCount();
            int columnCount = rt.getColumnCount();

            if (of.equals("html_table")) {
                rv += "<table>";
                rv += "<tr>";
                String[] names = rt.getColumnNames();
                for (int column = 0; column < columnCount; column++) {
                    rv += "<th>" + names[column] + "</th>";
                }
                rv += "</tr>";
                for (int row = 0; row < rowCount; row++) {
                    rv += "<tr>";
                    for (int column = 0; column < columnCount; column++) {
                        rv += "<td>";
                        Value value = rt.getValue(row, column);
                        
                        if (value != null) {
                            rv += value.toString();
                        } else {
                            rv += "null";
                        }
                        rv += "</td>";
                    }
                    rv += "</tr>";
                }
                rv += "</table>";
            }
            
        } catch (Exception e) { e.printStackTrace(); }
        
        return rv;
    }
    
    
    public String[][] selectQuery(
            String server_url, String repository,
            String username, String password,
            String query_language,
            String query) {
        
        try {           
            SesameService service = _login(server_url,username,password);
            org.openrdf.sesame.repository.SesameRepository rep = service.getRepository(repository);    
            
            QueryLanguage qlang = _verifyQueryLanguage(query_language);
            
            QueryResultsTable rt = rep.performTableQuery(qlang, query);

            int rowCount = rt.getRowCount();
            int columnCount = rt.getColumnCount();
            String[][] resultTable = new String[rowCount][columnCount];
            
            for (int row = 0; row < rowCount; row++) {
                for (int column = 0; column < columnCount; column++) {
                    Value value = rt.getValue(row, column);
                    resultTable[row][column] = value.toString();
                }
            }
            return resultTable;
        } catch (org.openrdf.sesame.query.MalformedQueryException e1) {
						System.err.println("Error with query: " + query);
              
        } catch (Exception e) { e.printStackTrace(); }
        
        return null;
    }

}
