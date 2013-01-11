/*
 * RepositoryFactoryImpl.java
 *
 * Created on January 31, 2006, 2:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.metadata;

/**
 * This class is part of the Factory design pattern to allow the dynamic and configurable 
 * instantiation of the Repository class with various implementations of the Repository, e.g. Sesame, Jena, etc.
 * @author wrvhage
 */
public interface RepositoryFactoryImpl {

    /**
     * This method creates an uninitialized Repository.
     */
    public Repository createRepository();
   
    /**
     * This method produces a Repository with login information.
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param repository the name of the repository or model in the meta-data server (e.g. mem-rdfs-db)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame)
     */
    public Repository createRepository(
            String server, String repository,
            String username, String password);
    
    /**
     * This method produces a Repository with login information and a preferred RDF serialization format.
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param repository the name of the repository or model in the meta-data server (e.g. mem-rdfs-db)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame)
     * @param rdf_format the encoding of the RDF (e.g. rdfxml, turtle, ntriples, etc.)
     */
    public Repository createRepository(
            String server, String repository,
            String username, String password,
            String rdf_format);    
    
}
