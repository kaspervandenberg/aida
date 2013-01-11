package org.vle.aid.metadata.ws;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.repository.manager.RemoteRepositoryManager;

public class AIDRepositoryManager extends RemoteRepositoryManager {

	public AIDRepositoryManager(String serverURL, String repositoryID) {
		super(serverURL);
		try {
			
			RepositoryConfig config = new RepositoryConfig(repositoryID, "Remote Loaded", new SailRepositoryConfig());

			addRepositoryConfig(config);

			createRepository(repositoryID);			
		} catch (RepositoryConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated constructor stub
	}

}
