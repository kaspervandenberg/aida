/*
 * JenaRepositoryFactory.java
 *
 * Created on January 31, 2006, 12:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.metadata;

/**
 * A stub class that can be filled-in to allow Jena support in the future.
 * See the documentation of {@link SesameRepositoryFactoryImpl} for details.
 * @author wrvhage
 */
public class JenaRepositoryFactoryImpl implements RepositoryFactoryImpl {
    
    public Repository createRepository() {
        return new JenaRepository();
    }
    
    public Repository createRepository(
            String server, String repository,
            String username, String password) { 
        return new JenaRepository(server,repository,username,password); 
    }
    
    public Repository createRepository(
            String server, String repository,
            String username, String password,
            String rdf_format) { 
        return new JenaRepository(server,repository,username,password); 
    }
    
}
