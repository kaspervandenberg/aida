package org.vle.aid.metadata.ws;

import org.vle.aid.metadata.Repository;
import org.vle.aid.metadata.RepositoryFactory;

public class Repository2WS {
	
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Repository2WS.class);
    
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
