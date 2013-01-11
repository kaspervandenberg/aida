package org.vle.aid.metadata.ws;

import java.lang.reflect.Array;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.spi.LocaleServiceProvider;

import javax.servlet.ServletException;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import junit.framework.TestCase;
import org.openrdf.OpenRDFException;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.vle.aid.metadata.exception.UnknownException;
import org.vle.aid.metadata.SkosLens;
import org.vle.aid.metadata.SkosLensType;
import org.vle.aid.metadata.ThesaurusRepository;
import org.vle.aid.metadata.ws.ThesaurusRepositoryWS.Topterms;
import org.vle.aid.metadata.ws.ThesaurusRepositoryWS.Topterms.Topterm;
import org.vle.aid.thesaurus.client.ThesaurusConceptNode;

import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.nativerdf.config.NativeStoreConfig;


@Path("/remote")
public class RemoteRepositoryRest {

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	
	
	/**
	 * Load RDF data from URL and store it on temporary repository
	 * 
	 * @param form
	 *            the parameters
	 * @return URL to the loaded repository 
	 * @throws ServletException
	 *             When a param is missing
	 * @throws RemoteException
	 */
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	@Path("/loadrdf")
	public String loadrdf(MultivaluedMap<String, String> param) throws ServletException, RemoteException {

			String rdfURL = null;
			if (!param.containsKey("url")) {
				rdfURL = null;
			} else {
				rdfURL = param.getFirst("url");
			}

			Repository myRepository = new HTTPRepository("http://dev.adaptivedisclosure.org/openrdf-sesame/repositories/remoteLoad");
			
			try {
			   RepositoryConnection con = myRepository.getConnection();
			   try {
			
				  URL url = new URL(rdfURL);
				  con.clear();
				  con.add(url, url.toString(), RDFFormat.RDFXML);
				  
			   }
			   finally {
				  con.close();
			   }
			}
			catch (OpenRDFException e) {
			   // handle exception
				e.printStackTrace();
			}
			catch (java.io.IOException e) {
			   // handle io exception
				e.printStackTrace();
			}

		return rdfURL;
	}

	/**
	 * Load RDF data from URL and store it on a newly created repository
	 * 
	 * @param form
	 *            the parameters
	 * @return URL to the loaded repository 
	 * @throws ServletException
	 *             When a param is missing
	 * @throws RemoteException
	 */
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	@Path("/loadrdfnew")
	public String loadrdfnew(MultivaluedMap<String, String> param) throws ServletException, RemoteException {

			String rdfURL = null;
			String repositoryID;
			if (!param.containsKey("url")) {
				rdfURL = null;
			} else {
				rdfURL = param.getFirst("url");
			}

			if (!param.containsKey("repositoryID")) {
				repositoryID = null;
			} else {
				repositoryID = param.getFirst("repositoryID");
			}

		
			RepositoryConnection con = null;

			try {

				SailImplConfig nativeStoreConfig = new NativeStoreConfig("spoc,posc");
				RepositoryImplConfig sailRepoConfig = new SailRepositoryConfig(nativeStoreConfig);
				RepositoryConfig repoConfig = new RepositoryConfig(repositoryID, sailRepoConfig);
				repoConfig.setTitle(rdfURL);

				RepositoryManager manager = new RemoteRepositoryManager("http://dev.adaptivedisclosure.org/openrdf-sesame");
				manager.initialize();
				manager.addRepositoryConfig(repoConfig);	
						


				Repository myRepository = new HTTPRepository("http://dev.adaptivedisclosure.org/openrdf-sesame",repositoryID);
			    con = myRepository.getConnection();

			
				URL url = new URL(rdfURL);
				con.add(url, url.toString(), RDFFormat.RDFXML);
				con.close();
			}	  
			catch (Exception e) {
				e.printStackTrace();
			}

		return rdfURL;
	}

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	@Path("/alreadyLoaded")
	public String alreadyLoaded(MultivaluedMap<String, String> param) throws ServletException, RemoteException {

			String rdfURL = null;
			String repositoryID = null;

			if (param.containsKey("url")) 
				rdfURL = param.getFirst("url");
			
			if (param.containsKey("repositoryID")) 
				repositoryID = param.getFirst("repositoryID");
			

			try {

				RepositoryManager manager = new RemoteRepositoryManager("http://dev.adaptivedisclosure.org/openrdf-sesame");
				manager.initialize();

				Collection <RepositoryInfo> repositories = manager.getAllRepositoryInfos(true);

				for(RepositoryInfo i : repositories){
					if(i != null && i.getDescription() != null && i.getDescription().contains(rdfURL)) 
						return i.getId();
				}


			}	  
			catch (Exception e) {
				e.printStackTrace();
			}

		return "";
	}

	/**
	 * Looks up concepts
	 * 
	 * @param ui
	 *            the parameters
	 * @return getLensesions
	 * @throws ServletException
	 *             When a param is missing
	 * @throws RemoteException
	 */
	@GET
	@Produces("application/json")
	@Path("/loadrdf")
	public String loadrdf(@Context UriInfo ui) throws ServletException, RemoteException {
		return loadrdf(ui.getQueryParameters());
	}

	@GET
	@Produces("application/json")
	@Path("/loadrdfnew")
	public String loadrdfnew(@Context UriInfo ui) throws ServletException, RemoteException {
		return loadrdfnew(ui.getQueryParameters());
	}


	@GET
	@Produces("application/json")
	@Path("/alreadyLoaded")
	public String alreadyLoaded(@Context UriInfo ui) throws ServletException, RemoteException {
		return alreadyLoaded(ui.getQueryParameters());
	}




}
