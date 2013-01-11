/*
 * Repository.java
 *
 * Created on January 31, 2006, 9:44 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.metadata;

import org.vle.aid.metadata.exception.AddRdfException;
import org.vle.aid.metadata.exception.ClearRepositoryException;
import org.vle.aid.metadata.exception.ExtractRdfException;
import org.vle.aid.metadata.exception.RemoveRDFException;
import org.vle.aid.metadata.exception.SystemQueryException;
import org.vle.aid.metadata.exception.QueryException;

/**
 * This interface defines all operations that a Repository (e.g. {@link SesameRepository}) should implement.
 * @author wrvhage
 */
public interface Repository
{

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

    void setVirtuoso() throws QueryException;
    boolean getVirtuoso();

	// Original get repositories without label/title
    String[] getRepositories() throws SystemQueryException;
    String[] getRepositories(String server_url, String username, String password, String read_write) throws SystemQueryException;


	// Get repositories with label of namedgraph for non sesame (with title for sesame repositories)
    String[][] getRepositoriesLabel() throws SystemQueryException;
    String[][] getRepositoriesLabel(String server_url, String username, String password, String read_write) throws SystemQueryException;

    String extractRdf() throws ExtractRdfException;
    String extractRdf(String rdf_format) throws ExtractRdfException;
    String extractRdf(String server_url, String repository, String username, String password, String rdf_format) throws ExtractRdfException;

    boolean clear() throws ClearRepositoryException;
    boolean clear(String server_url, String repository, String username, String password) throws ClearRepositoryException;

    boolean addRdf(String data_uri, String data) throws AddRdfException;
    boolean addRdf(String server_url, String repository, String username, String password, String rdf_format, String data_uri, String data) throws AddRdfException;
    boolean addRdf(String data_uri, String data, String context) throws AddRdfException;
    boolean addRdf(String server_url, String repository, String username, String password, String rdf_format, String data_uri, String data, String context) throws AddRdfException;

    boolean addRdfFile(String data_uri) throws AddRdfException;
    boolean addRdfFile(String server_url, String repository, String username, String password, String rdf_format, String data_uri) throws AddRdfException;
    boolean addRdfFile(String data_uri, String context) throws AddRdfException;
    boolean addRdfFile(String server_url, String repository, String username, String password, String rdf_format, String data_uri, String context) throws AddRdfException;

    boolean addRdfStatement(String subject, String predicate, String object) throws AddRdfException;
    boolean addRdfStatement(String server_url, String repository, String username, String password, String subject, String predicate, String object) throws AddRdfException;
    boolean addRdfStatement(String subject, String predicate, String object, String context) throws AddRdfException;
    boolean addRdfStatement(String server_url, String repository, String username, String password, String subject, String predicate, String object, String context) throws AddRdfException;

    boolean removeRdf(String data_uri, String data) throws RemoveRDFException;
    boolean removeRdf(String server_url, String repository, String username, String password, String rdf_format, String data_uri, String data) throws RemoveRDFException;
    boolean removeRdf(String data_uri, String data, String context) throws RemoveRDFException;
    boolean removeRdf(String server_url, String repository, String username, String password, String rdf_format, String data_uri, String data, String context) throws RemoveRDFException;

    boolean removeRdfFile(String data_uri) throws RemoveRDFException;
    boolean removeRdfFile(String server_url, String repository, String username, String password, String format, String data_uri) throws RemoveRDFException;
    boolean removeRdfFile(String data_uri, String context) throws RemoveRDFException;
    boolean removeRdfFile(String server_url, String repository, String username, String password, String format, String data_uri, String context) throws RemoveRDFException;

    boolean removeRdfStatement(String subject, String predicate, String object) throws RemoveRDFException;
    boolean removeRdfStatement(String server_url, String repository, String username, String password, String subject, String predicate, String object) throws RemoveRDFException;
    boolean removeRdfStatement(String subject, String predicate, String object, String context) throws RemoveRDFException;
    boolean removeRdfStatement(String server_url, String repository, String username, String password, String subject, String predicate, String object, String context) throws RemoveRDFException;

    String selectQuery(String query) throws QueryException;
    String selectQuery(String server_url, String repository, String username, String password, String query_language, String select_output_format, String query)  throws QueryException;
    String[][] selectQuery(String server_url, String repository, String username, String password, String query_language, String query)  throws QueryException;

    String constructQuery(String query) throws QueryException;
    String constructQuery(String server, String repository, String username, String password, String query_language, String rdf_format, String query) throws QueryException;

}


