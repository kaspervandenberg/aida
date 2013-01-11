/*
 * Repository.java
 *
 * Created on January 31, 2006, 9:44 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.metadata;

/**
 * This interface defines all operations that a Repository (e.g. {@link SesameRepository}) should implement.
 * @author wrvhage
 */
public interface Repository {
    
    String getPassword();
    void setPassword(String password);

    String getQueryLanguage();
    void setQueryLanguage(String query_language);

    String getRdfFormat();
    void setRdfFormat(String rdf_format);

    String getRepository();
    void setRepository(String repository);

    String getSelectOutputFormat();
    void setSelectOutputFormat(String select_output_format);

    String getServer();
    void setServer(String server);

    String getUsername();
    void setUsername(String username);

    String[] getRepositories();
    String[] getRepositories(String server_url, String username, String password, String read_write);

    String extractRdf();
    String extractRdf(String rdf_format);
    String extractRdf(String server_url, String repository, String username, String password, String rdf_format);

    boolean clear();
    boolean clear(String server_url, String repository, String username, String password);

    boolean addRdf(String data_uri, String data);
    boolean addRdf(String server_url, String repository, String username, String password, String rdf_format, String data_uri, String data);

    boolean addRdfFile(String data_uri);
    boolean addRdfFile(String server_url, String repository, String username, String password, String rdf_format, String data_uri);

    boolean addRdfStatement(String subject, String predicate, String object);
    boolean addRdfStatement(String server_url, String repository, String username, String password, String subject, String predicate, String object);
    boolean addRdfStatement(String subject, String predicate, String object, String context);
    boolean addRdfStatement(String server_url, String repository, String username, String password, String subject, String predicate, String object, String context);

    boolean removeRdf(String data_uri, String data);
    boolean removeRdf(String server_url, String repository, String username, String password, String rdf_format, String data_uri, String data);

    boolean removeRdfFile(String data_uri);
    boolean removeRdfFile(String server_url, String repository, String username, String password, String format, String data_uri);

    boolean removeRdfStatement(String subject, String predicate, String object);
    boolean removeRdfStatement(String server_url, String repository, String username, String password, String subject, String predicate, String object);
    boolean removeRdfStatement(String subject, String predicate, String object, String context);
    boolean removeRdfStatement(String server_url, String repository, String username, String password, String subject, String predicate, String object, String context);

    String selectQuery(String query);
    String selectQuery(String server_url, String repository, String username, String password, String query_language, String select_output_format, String query);
    String[][] selectQuery(String server_url, String repository, String username, String password, String query_language, String query);

    String constructQuery(String query);
    String constructQuery(String server, String repository, String username, String password, String query_language, String rdf_format, String query);

}


