package org.vle.aid.taverna.remote;

import java.beans.PropertyChangeSupport;

public class AIDRemoteConfig {


	
    public static String DEFAULT_SERVICE_HOST			 	= "http://aida.homelinux.org";
//  public static String DEFAULT_SERVICE_HOST	 			    = "http://dev.adaptivedisclosure.org";

    public static String DEFAULT_SESAME_SERVER	 			= "http://aida.homelinux.org/openrdf-sesame"; 
 // public static String DEFAULT_SESAME_SERVER 				= "http://tarski.duhs.org:8080";
    
    public static String DEFAULT_USERNAME		     		= "";
    public static String DEFAULT_PASSWORD		     		= "";

    public static String DEFAULT_REPOSITORY_SERVICE			= DEFAULT_SERVICE_HOST + "/axis/services/RepositoryWS";
    public static String DEFAULT_THESAURUS_SERVICE 			= DEFAULT_SERVICE_HOST + "/axis/services/SkosLensWS";

    public static String DEFAULT_QUERY_LANGUAGE	       		= "sparql";
    public static String DEFAULT_REPOSITORY		   			= "tno";
    public static String DEFAULT_OUTPUT_FORMAT				= "html_table";
    
    public static String DEFAULT_SEARCH_SERVICE  			= DEFAULT_SERVICE_HOST + "/axis/services/SearcherWS";
    public static String DEFAULT_SEARCH_FIELDS_SERVICE  	= DEFAULT_SERVICE_HOST + "/axis/services/getFields";
    public static String DEFAULT_SEARCH_INDEXES_SERVICE  	= DEFAULT_SERVICE_HOST + "/axis/services/getIndexes";
        
    public static String DEFAULT_DETECT_SERVICE  			= DEFAULT_SERVICE_HOST + "/axis/services/RepositoryDetectWS";
    public static String DEFAULT_SKOSLENS_SERVICE  			= DEFAULT_SERVICE_HOST + "/axis/services/SkosLensWS";
        
    public static String TOP_CONCEPT						= "rdfs:Class";
    public static String NARROWER_PREDICATE					= "rdfs:SubClassOf";
    
    public static void setSesameServer(String newSesameServer){
    	DEFAULT_SESAME_SERVER = newSesameServer;    	
    }
    

    public static void setUsername(String username){
    	DEFAULT_USERNAME = username;
    }
    
    public static void setPassword(String password){
    	DEFAULT_PASSWORD = password;
    }

    public static void setTopConcept(String newTopConcept) {
    	TOP_CONCEPT = newTopConcept;
    }
    
    public static void setNarrowerPredicate(String newNarrowerPredicate) {
    	NARROWER_PREDICATE = newNarrowerPredicate;
    }
}
