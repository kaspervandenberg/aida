package org.vle.aid.metadata;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.vle.aid.metadata.exception.QueryException;

public class Sesame2RepositoryFactoryImpl implements RepositoryFactoryImpl {

	public Repository createRepository() 
	{
        try {
            return new Sesame2Repository();
        } catch (QueryException ex) {
            Logger.getLogger(Sesame2RepositoryFactoryImpl.class.getName()).log(Level.SEVERE, null, ex);
        } 
		return null; 
        //return new AIDRepository();
	}

	public Repository createRepository(String server, String repository,
			String username, String password) 
	{
		return new Sesame2Repository(server,repository,username,password); 
		//return new AIDRepository(server,repository,username,password); 
	}

	public Repository createRepository(String server, String repository,
			String username, String password, String rdf_format) 
	{
		return new Sesame2Repository(server,repository,username,password,rdf_format);
		//return new AIDRepository(server,repository,username,password,rdf_format);
	}

}
