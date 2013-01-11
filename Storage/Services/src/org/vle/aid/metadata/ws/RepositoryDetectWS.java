/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vle.aid.metadata.ws;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.vle.aid.metadata.exception.QueryException;
import org.vle.aid.metadata.Repository;
import org.vle.aid.metadata.AIDRepository;
import org.vle.aid.metadata.RepositoryFactory;
import org.vle.aid.metadata.SkosLensType;

/**
 *
 * @author wongiseng
 */

@Path("/repository")
public class RepositoryDetectWS {
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RepositoryDetectWS.class);
    String querySkos2004  = "select ?S where {?S a <http://www.w3.org/2004/02/skos/core#Concept>} limit 1";
	String querySkos2008  = "select ?S where {?S a <http://www.w3.org/2008/05/skos#Concept>} limit 1";
	String queryOwlClass  = "select ?S where {?S a <http://www.w3.org/2002/07/owl#Class>} limit 1";
	String queryRdfsClass = "select ?S where {?S a <http://www.w3.org/2000/01/rdf-schema#Class>} limit 1";
  
    private Repository r;
    boolean isVirtuoso = false;
    String skosVersion = "";
    String namedGraph = null;
    
	@Context UriInfo uriInfo;
	@Context Request request;

    class DetectInfo {
    	public String virtuoso;
    	public String skosVersion;
    	public String skosLens;
    }
    
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    @Path("/detect")
    public DetectInfo detect(MultivaluedMap<String, String> param) throws Exception {
    	   DetectInfo result = new DetectInfo() ;
    	
           String server_url = getParameter("server_url", param);
           String repository = getParameter("repository", param);
           String username   = getParameter("username", param);
           String password   = getParameter("password", param);
             
           r = RepositoryFactory.createRepository(server_url,repository,username,password);
           r.setVirtuoso();

		   //Another sad hack, if the repository is starting with http, it is actually a named graph
           isVirtuoso = r.getVirtuoso() || repository.startsWith("http://");
           result.virtuoso =  isVirtuoso ? "Virtuoso" : "Not Virtuoso";
           
           result.skosVersion = detectSkosVersion();
    	   
           if(result.skosVersion.equals(SkosLensTypes.NONSKOS.toString()))
        	   result.skosLens = detectSkosLens();
           else
        	   result.skosLens = null;
           
           return result;
    }

    @GET
    @Produces("application/json")
    @Path("/detect")
    public DetectInfo detect( @Context UriInfo ui) throws Exception {
    	
    	return detect(ui.getQueryParameters());
    }

    private String detectSkosLens() throws QueryException{
        try {
        	if(isVirtuoso){
        		namedGraph = _getVirtuosoNamedGraph(r.getRepository());
        		queryOwlClass = queryOwlClass.replace("where", " from <"+namedGraph+"> where ");
        		queryRdfsClass = queryRdfsClass.replace("where", " from <"+namedGraph+"> where ");
        		
        	}
        	
            String[][] rv = r.selectQuery(r.getServer(), r.getRepository(), r.getUsername(), r.getPassword(), "sparql", queryOwlClass);
			if(rv == null) {
					// Rodo ngawur ini, dianggapnya kalau null terus mesti dihajar dengan AIDRepository
					rv = new AIDRepository().selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",queryOwlClass);
			} 


            if(rv != null && rv.length > 0) return SkosLensTypes.OWLClass.toString();
            
            rv = r.selectQuery(r.getServer(), r.getRepository(), r.getUsername(), r.getPassword(), "sparql", queryRdfsClass);
			if(rv == null) {
					// Rodo ngawur ini, dianggapnya kalau null terus mesti dihajar dengan AIDRepository
					rv = new AIDRepository().selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",queryRdfsClass);
			} 


            if(rv != null && rv.length > 0) return SkosLensTypes.RDFSClass.toString();

            
            return null;            
            
        } catch (Throwable org) {
            org.printStackTrace();

            throw new QueryException("_detectSkosVersion Query Error:" + org.getMessage(), org);
        }
    }

	public String[] detectRepository(String sesame_server_url, String repository_name, String username, String password) throws Exception {
        r = RepositoryFactory.createRepository(sesame_server_url,repository_name,username,password);
        r.setVirtuoso();
        isVirtuoso = r.getVirtuoso();
        
        String skosVersion = detectSkosVersion();
        
        String skosLens = null;
        if(skosVersion.equals(SkosLensTypes.NONSKOS.toString())) 
        	skosLens = detectSkosLens();
        
        String[] result = new String []{isVirtuoso?"Virtuoso":"Not Virtuoso", skosVersion, skosLens};

        return result;
    }

	public String detectSkosVersion() throws QueryException {
        try {
        	if(isVirtuoso){
        		namedGraph = _getVirtuosoNamedGraph(r.getRepository());
        		querySkos2004 = querySkos2004.replace("where", " from <"+namedGraph+"> where ");
        		querySkos2008 = querySkos2008.replace("where", " from <"+namedGraph+"> where ");
        		
        	}
        	
            String[][] rv = r.selectQuery(r.getServer(), r.getRepository(), r.getUsername(), r.getPassword(), "sparql", querySkos2004);

			if(rv == null) {
					// Rodo ngawur ini, dianggapnya kalau null terus mesti dihajar dengan AIDRepository
					rv = new AIDRepository().selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",querySkos2004);
			} 

            if(rv != null && rv.length > 0) {
				return SkosLensTypes.SKOS2004.toString();

			}
            
            rv = r.selectQuery(r.getServer(), r.getRepository(), r.getUsername(), r.getPassword(), "sparql", querySkos2008);
			if(rv == null) {
					// Rodo ngawur ini, dianggapnya kalau null terus mesti dihajar dengan AIDRepository
					rv = new AIDRepository().selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",querySkos2008);
			} 


            if(rv != null && rv.length > 0) return SkosLensTypes.SKOS2008.toString();
            
            return SkosLensTypes.NONSKOS.toString();            
            
        } catch (Throwable org) {
            org.printStackTrace();

            throw new QueryException("_detectSkosVersion Query Error:" + org.getMessage(), org);
        }
    }
  
	

   public String _getVirtuosoNamedGraph(String repository) throws QueryException
    {
		// For the case where the client use named graph as repository already
		if(repository.startsWith("http://")) return repository;
        try
        {
                String query1 = "SELECT ?src WHERE " +
                                "{GRAPH ?src {" +
                                "               ?s ?p ?o " +
                                                ". FILTER(regex(str(?src), \"/"+repository+"$\", \"i\")) " +
                                "}} limit 1 ";
                String[][] named_graph_res = r.selectQuery(r.getServer(),"",r.getUsername(),r.getPassword(),"sparql",query1);
				
                return named_graph_res == null ? "" : named_graph_res[0] == null ? "" : named_graph_res[0][0];
        }
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("_getVirtuosoNamedGraph Query Error:" + org.getMessage(), org);
	    }
    }
    
    enum SkosLensTypes {
         NONSKOS           {public String toString(){return "Not Skos";}}
        ,SKOS2008          {public String toString(){return "http://www.w3.org/2008/05/skos#Concept";}}
        ,SKOS2004          {public String toString(){return "http://www.w3.org/2004/02/skos/core#Concept";}}
        ,OWLClass          {public String toString(){return "http://www.w3.org/2002/07/owl#Class";}}
        ,RDFSClass         {public String toString(){return "http://www.w3.org/2000/01/rdf-schema#Class";}}
        ,RDFSubClassOf     {public String toString(){return "http://www.w3.org/2000/01/rdf-schema#SubClassOf";}}
        ,RDFSProperty      {public String toString(){return "http://www.w3.org/2000/01/rdf-schema#Property";}}
        ,RDFSSubPropertyOf {public String toString(){return "http://www.w3.org/2000/01/rdf-schema#SubPropertyOf";}}
        ,JADEClass         {public String toString(){return "http://jade.cselt.it/beangenerator#JADE-CLASS";}} 
    }
    
    /**
     * Get the parameter or throw an exception
     * @param paramString
     * @param param
     * @return
     * @throws Exception
     */
    public String getParameter(String paramString, MultivaluedMap<String, String> param) throws Exception {
     	  if (!param.containsKey(paramString)) 
            throw new Exception(paramString + " not defined");
          return param.getFirst(paramString);          
    }
}
