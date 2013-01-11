/*
 * RepositoryWS.java
 *
 * Created on January 30, 2006, 11:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.metadata.ws;

import org.vle.aid.metadata.Repository;
import org.vle.aid.metadata.RepositoryFactory;

/**
 *
 * @author wrvhage
 */
public class RepositoryWS {
    
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RepositoryWS.class);
    /** Creates a new instance of RepositoryWS */
    public RepositoryWS() {
        
    }

    /**
     * Return a list of all repositories that match the "read_write" criterion.
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame
     * @param read_write a string containing the optional characters "r" or "w" that indicate whether you want to see read-only repositories ("r") or read-write reposiory ("rw") for the indicated user. An empty string imposes no restriction.
     */
    public String[] getRepositories(
            String server_url,
            String username, String password,
            String read_write) {
        try {
            Repository r = RepositoryFactory.createRepository();
            return r.getRepositories(server_url,username,password,read_write);
        } catch (Exception e) { logger.error(e.getMessage(),e); }
        return null;
    }

    /**
     * Add a string of RDF data (that encodes a set of triples) into a repository.
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param repository the name of the repository or model in the meta-data server (e.g. mem-rdfs-db)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame)
     * @param rdf_format the encoding of the RDF (e.g. rdfxml, turtle, ntriples, etc.)
     * @param data_uri the default URI for the RDF snippet (in the sense of the "base uri" used in Sesame, e.g. to name blank nodes. Use the name of an ontology or file to which the snippet belongs.)
     * @param data the string of RDF data encoded in "rdf_format"
     * @return true or false, indicating success of the operation
     */    
    public boolean addRdf(
            String server_url, String repository,
            String username, String password,
            String rdf_format,
            String data_uri, String data) {
        try {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password,rdf_format);
            return r.addRdf(data_uri,data);
        } catch (Exception e) { logger.error(e.getMessage(),e); }
        return false;
    }
    
    /**
     * Add an RDF triple into a repository.
     * @param subject the URI of the subject of the triple (e.g. http://www.example.com/eg#aResource)
     * @param predicate the URI of the predicate of the triple (e.g. http://www.w3.org/2000/01/rdf-schema#subClassOf)
     * @param object either the URI of the object of the triple, or a literal value that can be typed using XML Schema, or can be assigned a language. (e.g. http://www.example.com/eg#aResource or Reticulation or "Reticulation"^^<http://www.w3.org/2001/XMLSchema#string> or "Reticulation"@en. NB! that typing or assignment of a language requires you to (double)quote the string.)
     * @return true or false, indicating success of the operation
     */ 
    public boolean addRdfStatement(
            String server_url, String repository,
            String username, String password,
            String subject, String predicate, String object) {
        try {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password);
            return r.addRdfStatement(subject,predicate,object);
        } catch (Exception e) { logger.error(e.getMessage(),e); }
        return false;
    }

    /**
     * Add an RDF triple into a repository.
     * @param subject the URI of the subject of the triple (e.g. http://www.example.com/eg#aResource)
     * @param predicate the URI of the predicate of the triple (e.g. http://www.w3.org/2000/01/rdf-schema#subClassOf)
     * @param object either the URI of the object of the triple, or a literal value that can be typed using XML Schema, or can be assigned a language. (e.g. http://www.example.com/eg#aResource or Reticulation or "Reticulation"^^<http://www.w3.org/2001/XMLSchema#string> or "Reticulation"@en. NB! that typing or assignment of a language requires you to (double)quote the string.)
     * @param context the URI of an rdf:Bag that should "contain" a reified version of the triple after addition to the repository, using rdf:member (increases memory usage by a factor 6)
     * @return true or false, indicating success of the operation
     */ 
    public boolean addRdfStatementWithContext(
            String server_url, String repository,
            String username, String password,
            String subject, String predicate, String object, String context) {
        try {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password);
            return r.addRdfStatement(subject,predicate,object,context);
        } catch (Exception e) { logger.error(e.getMessage(),e); }
        return false;
    }

    /**
     * Add an RDF triple into a repository.
     * @param file the URL of the file containing the RDF (e.g. http://... or file://..., check with your browser if the URL is correct.)
     * @return true or false, indicating success of the operation
     */ 
    public boolean addRdfFile(
            String server_url, String repository,
            String username, String password,
            String rdf_format,
            String data_uri) {
        try {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password,rdf_format);
            return r.addRdfFile(data_uri);
        } catch (Exception e) { logger.error(e.getMessage(),e); }
        return false;
    }
    
    public boolean removeRdf(
            String server_url, String repository,
            String username, String password,
            String rdf_format,
            String data_uri, String data) {
        try {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password,rdf_format);
            return r.removeRdf(data_uri,data);
        } catch (Exception e) { logger.error(e.getMessage(),e); }
        return false;
    }
    
    public boolean removeRdfStatement(
            String server_url, String repository,
            String username, String password,
            String subject, String predicate, String object) {
        try {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password);
            return r.removeRdfStatement(subject,predicate,object);
        } catch (Exception e) { logger.error(e.getMessage(),e); }
        return false;
    }
    
    public boolean removeRdfStatementWithContext(
            String server_url, String repository,
            String username, String password,
            String subject, String predicate, String object, String context) {
        try {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password);
            return r.removeRdfStatement(subject,predicate,object,context);
        } catch (Exception e) { logger.error(e.getMessage(),e); }
        return false;
    }
    
    public boolean removeRdfFile(
            String server_url, String repository,
            String username, String password,
            String rdf_format,
            String data_uri) {
        try {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password,rdf_format);
            return r.removeRdfFile(data_uri);
        } catch (Exception e) { logger.error(e.getMessage(),e); }
        return false;
    }
    
    public boolean clear(
            String server_url,String repository,
            String username,String password) {
        try {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password);
            return r.clear();
        } catch (Exception e) { logger.error(e.getMessage(),e); }
        return false;
    }

    public String extractRdf(
            String server_url, String repository,
            String username, String password,
            String rdf_format) {
        try {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password,rdf_format);
            return r.extractRdf();
        } catch (Exception e) { logger.error(e.getMessage(),e); }
        return null;
    }

    /**
     * Pose a "CONSTRUCT" query to the repository.
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param repository the name of the repository or model in the meta-data server (e.g. mem-rdfs-db)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame)
     * @param query_language the language in which the query is formulated (e.g. serql, rql, etc.)
     * @param query the query (e.g. construct {s} p {o} from {o} p {s})
     * @param rdf_format the format of the resulting RDF that is returned
     * @return a string containing an encoding of all constructed triples
     */
    public String constructQuery(
            String server_url, String repository,
            String username, String password,
            String query_language, String rdf_format,
            String query) {
        try {
            Repository r = RepositoryFactory.createRepository();
            return r.constructQuery(server_url,repository,username,password,query_language,rdf_format,query);
        } catch (Exception e) { logger.error(e.getMessage(),e); }
        return null;
    }
    

    /**
     * Pose a "SELECT" query to the repository. (for HTML table output)
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param repository the name of the repository or model in the meta-data server (e.g. mem-rdfs-db)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame)
     * @param query_language the language in which the query is formulated (e.g. serql, rql, etc.)
     * @param query the query (e.g. select distint p from {s} p {o})
     * @param select_output_format the format of the resulting table that contains the results (currently only "html_table" is supported. This operation is only meant for the presentation of the results to human viewers.)
     * @return a string containing an encoding of all selected values
     */
    public String selectQuerySerialized(
            String server_url, String repository,
            String username, String password,
            String query_language, String select_output_format,
            String query) {
        try {
            Repository r = RepositoryFactory.createRepository();
            return r.selectQuery(server_url,repository,username,password,query_language,select_output_format,query);
        } catch (Exception e) { logger.error(e.getMessage(),e); }
        return null;
    }

    /**
     * Pose a "SELECT" query to the repository. (for use in a program)
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param repository the name of the repository or model in the meta-data server (e.g. mem-rdfs-db)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame)
     * @param query_language the language in which the query is formulated (e.g. serql, rql, etc.)
     * @param query the query (e.g. select distint p from {s} p {o})
     * @return an array of arrays containing the result table (e.g. "select s, o from {s} p {o}" could return [["http://www.example.com/eg#Doormat","Doormat"],["http://www.example.com/eg#Doorknob","Doorknob"], ...])
     */    
    public String[][] selectQuery(
            String server_url, String repository,
            String username, String password,
            String query_language, String query) {
        try {
            Repository r = RepositoryFactory.createRepository();
            return r.selectQuery(server_url,repository,username,password,query_language,query);
        } catch (Exception e) { logger.error(e.getMessage(),e); }
        return null;
    }
}
