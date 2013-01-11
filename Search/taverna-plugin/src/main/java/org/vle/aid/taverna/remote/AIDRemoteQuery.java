package org.vle.aid.taverna.remote;

import java.util.Vector;




/**
 * This class basically is the same with AIDRemote, somehow I should merge them both. 
 * The difference is only on the function call, this one store axis server, server url, and repetitive parameters as member variable
 * Therefore the call is shorter and more readable.
 * 
 * aida-plugin
 * @author wibisono
 * @date Apr 23, 2009 10:31:24 PM
 */
public class AIDRemoteQuery {
	private String axis_service, sesame_server, repository_name, username, password;
	
	/* Result from detect repository will be stored here */
	public boolean is_skosLens, is_virtuoso;
	public String virtuosoNamedGraph, skosVersion;
	

	public String toString(){
		return  "\nAxis server :"+axis_service+"\n" + "Sesame server :"+sesame_server+"\n"+"Repository name :"+repository_name +"\n";
	}
	
	public AIDRemoteQuery(String axis_service, String sesame_server,
			String repository_name, String username, String password) {
		this.axis_service = axis_service;
		this.sesame_server = sesame_server;
		this.repository_name = repository_name;
		this.username = username;
		this.password = password;
	}

	public AIDRemoteQuery(String thesaurusRepositoryName) {
	    		this(AIDRemoteConfig.DEFAULT_THESAURUS_SERVICE,
				AIDRemoteConfig.DEFAULT_SESAME_SERVER, thesaurusRepositoryName, "", "");	    
	}
	
	/**
	 * Get default RepositoryWS services
	 * @return
	 */
	public static AIDRemoteQuery getDefaultRepository(){
		return new AIDRemoteQuery(AIDRemoteConfig.DEFAULT_REPOSITORY_SERVICE,
				AIDRemoteConfig.DEFAULT_SESAME_SERVER, AIDRemoteConfig.DEFAULT_REPOSITORY,
				"", "");
	}
	/**
	 * Get Default Thesaurus RepositoryWS
	 * @return
	 */
	public static AIDRemoteQuery getDefaultThesaurusRepository(){
			return new AIDRemoteQuery(AIDRemoteConfig.DEFAULT_THESAURUS_SERVICE,
					AIDRemoteConfig.DEFAULT_SESAME_SERVER, AIDRemoteConfig.DEFAULT_REPOSITORY,
					"", "");
	}
	/**
	 * Get Default Repository Detect WS
	 * @param repository
	 */
	public static AIDRemoteQuery getDefaultRepositoryDetect(){
		return new AIDRemoteQuery(AIDRemoteConfig.DEFAULT_DETECT_SERVICE,
				AIDRemoteConfig.DEFAULT_SESAME_SERVER, AIDRemoteConfig.DEFAULT_REPOSITORY,
				"", "");
	}

	
	public void setRepository(String repository) {
		this.repository_name = repository;
	}
	
	public String getRepository() {
		return repository_name;
	}

	public String getServerUrl() {
		return sesame_server;
	}
	
	/********************************************************************************************************
	 * Calls provided by RepositoryDetectWS
	 ********************************************************************************************************/
	public String [] detectRepository() throws Exception{    	
	    	String [] result = AIDRemote.detectRepository(axis_service, sesame_server, repository_name, username, password);
	    	virtuosoNamedGraph = result[0];
	    	is_virtuoso = virtuosoNamedGraph.equalsIgnoreCase("Not a Virtuoso") ? false : true;
	    	skosVersion = result[1];
	    	// Tricky part can be confusing, not Skos means Skos Lens
	    	is_skosLens = skosVersion.equals("Not Skos")? true : false;
	    	//System.out.println("Named Graph = " + virtuosoNamedGraph +" \nIs Virtuoso = "+is_virtuoso+"\nSkos Version = "+skosVersion+"\nIs Skoslens = "+is_skosLens);	    	
	    	return result;
	}
	


	/********************************************************************************************************
	 * Calls provided by RepositoryWS
	 ********************************************************************************************************/
	public String selectQuerySerialized(String query_language,
			String select_output_format, String query) throws Exception {
		return AIDRemote.selectQuerySerialized(axis_service, sesame_server,
				repository_name, username, password, query_language,
				select_output_format, query);
	}

	public  boolean addRdfStatement(String subject, String predicate, String object) throws Exception {		
		return AIDRemote.addRdfStatement(axis_service, sesame_server, repository_name, username, password, subject, predicate, object);
	}
	public  boolean removeRdfStatement(String subject, String predicate, String object) throws Exception {		
		return AIDRemote.removeRdfStatement(axis_service, sesame_server, repository_name, username, password, subject, predicate, object);
	}
	public String[][] selectQuery(String query_language, String query)
	throws Exception {
		return AIDRemote.selectQuery(axis_service, sesame_server, repository_name,
				username, password, query_language, query);
	}

	public String[] getRepositories(String read_write) throws Exception {
		return AIDRemote.getRepositories(axis_service, sesame_server, username,
				password, read_write);
	}

       /********************************************************************************************************
        * Calls provided by ThesaurusWS
        ********************************************************************************************************/
	public String[][] getNarrowerTerms(String term) throws Exception {
		return AIDRemote.getNarrowerTerms(axis_service, sesame_server, repository_name,
				username, password, term, skosVersion, virtuosoNamedGraph);
	}

	public String[][] getBroaderTerms(String term) throws Exception {
		return AIDRemote.getBroaderTerms(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[][] getRelatedTerms(String term) throws Exception {
		return AIDRemote.getRelatedTerms(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[] getRDFSLabels(String term) throws Exception {
		return AIDRemote.getRDFSLabels(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[] getPreferedTerms(String term) throws Exception {
		return AIDRemote.getPreferedTerms(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[] getAlternativeTerms(String term) throws Exception {
		return AIDRemote.getAlternativeTerms(axis_service, sesame_server,
				repository_name, username, password, term);
	}

	public String[] getDefinitions(String term) throws Exception {
		return AIDRemote.getDefinitions(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[] getNotes(String term) throws Exception {
		return AIDRemote.getNotes(axis_service, sesame_server, repository_name, username,
				password, term);
	}

	public String[] getScopeNotes(String term) throws Exception {
		return AIDRemote.getScopeNotes(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[] getChangeNotes(String term) throws Exception {
		return AIDRemote.getChangeNotes(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[] getHistoryNotes(String term) throws Exception {
		return AIDRemote.getHistoryNotes(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[] getEditorialNotes(String term) throws Exception {
		return AIDRemote.getEditorialNotes(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[] getPublicNotes(String term) throws Exception {
		return AIDRemote.getPublicNotes(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[] getPrivateNotes(String term) throws Exception {
		return AIDRemote.getPrivateNotes(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[] getSubjectsOf(String url) throws Exception {
		return AIDRemote.getSubjectsOf(axis_service, sesame_server, repository_name,
				username, password, url);
	}

	public String[] getSubjects(String term) throws Exception {
		return AIDRemote.getSubjects(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[] getPrimarySubjects(String term) throws Exception {
		return AIDRemote.getPrimarySubjects(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[] getPrimarySubjectsOf(String url) throws Exception {
		return AIDRemote.getPrimarySubjectsOf(axis_service, sesame_server,
				repository_name, username, password, url);
	}

	public String[][] getTopConcepts(String scheme_label) throws Exception {
		return AIDRemote.getTopConcepts(axis_service, sesame_server, repository_name,
				username, password, scheme_label, skosVersion, virtuosoNamedGraph );
	}

	public String[][] getConceptSchemesWithNamespace(String namespace)
			throws Exception {
		return AIDRemote.getConceptSchemesWithNamespace(axis_service, sesame_server,
				repository_name, username, password, namespace);
	}

	public String[][] getConceptSchemes() throws Exception {
		//System.out.println("Calling concept schemes with "+skosVersion+" "+virtuosoNamedGraph+" "+repository_name);
		String result[][] = AIDRemote.getConceptSchemes(axis_service, sesame_server, repository_name,
				username, password, skosVersion, virtuosoNamedGraph);
		//System.out.println("Getting something "+result);
		return result;
	}

	public String[][] getInSchemes(String term) throws Exception {
		return AIDRemote.getInSchemes(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[][] getInCollections(String term) throws Exception {
		return AIDRemote.getInCollections(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[][] getCollectionMembers(String term) throws Exception {
		return AIDRemote.getCollectionMembers(axis_service, sesame_server,
				repository_name, username, password, term);
	}

	public String[][] getMatches(String term) throws Exception {
		return AIDRemote.getMatches(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[][] getExactMatches(String term) throws Exception {
		return AIDRemote.getExactMatches(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public boolean removeExactMatch(String subject, String object)
			throws Exception {
		return AIDRemote.removeExactMatch(axis_service, sesame_server, repository_name,
				username, password, subject, object);
	}

	public String[][] getDisjointMatches(String term) throws Exception {
		return AIDRemote.getDisjointMatches(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public boolean removeDisjointMatch(String subject, String object)
			throws Exception {
		return AIDRemote.removeDisjointMatch(axis_service, sesame_server,
				repository_name, username, password, subject, object);

	}

	public String[][] getRelatedMatches(String term) throws Exception {
		return AIDRemote.getRelatedMatches(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public boolean removeRelatedMatch(String subject, String object)
			throws Exception {
		return AIDRemote.removeRelatedMatch(axis_service, sesame_server, repository_name,
				username, password, subject, object);
	}

	public String[][] getNarrowMatches(String term) throws Exception {
		return AIDRemote.getNarrowMatches(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public boolean removeNarrowMatch(String subject, String object)
			throws Exception {
		return AIDRemote.removeNarrowMatch(axis_service, sesame_server, repository_name,
				username, password, subject, object);
	}

	public String[][] getBroadMatches(String term) throws Exception {
		return AIDRemote.getBroadMatches(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public boolean removeBroadMatch(String subject, String object)
			throws Exception {
		return AIDRemote.removeBroadMatch(axis_service, sesame_server, repository_name,
				username, password, subject, object);
	}

	public String[][] getPartMatches(String term) throws Exception {
		return AIDRemote.getPartMatches(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[][] getTermCompletion(String term) throws Exception {
		return AIDRemote.getTermCompletion(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public boolean removePartMatch(String subject, String object)
			throws Exception {
		return AIDRemote.removePartMatch(axis_service, sesame_server, repository_name,
				username, password, subject, object);
	}

	public String[][] getWholeMatches(String term) throws Exception {
		return AIDRemote.getWholeMatches(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public boolean removeWholeMatch(String subject, String object)
			throws Exception {
		return AIDRemote.removeWholeMatch(axis_service, sesame_server, repository_name,
				username, password, subject, object);
	}

	public String[][] getTermUri(String term) throws Exception {
		return AIDRemote.getTermUri(axis_service, sesame_server, repository_name,
				username, password, term);
	}

	public String[][] getNumberOfNarrowerTerms(String term) throws Exception {
		return AIDRemote.getNumberOfNarrowerTerms(axis_service, sesame_server,
				repository_name, username, password, term);
	}

	public String[][] getNumberOfNarrowerTermsMulti(String terms[])
			throws Exception {
		return AIDRemote.getNumberOfNarrowerTermsMulti(axis_service, sesame_server,
				repository_name, username, password, terms);
	}

}
