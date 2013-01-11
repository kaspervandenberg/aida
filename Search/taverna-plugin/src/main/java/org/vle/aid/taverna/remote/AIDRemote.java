package org.vle.aid.taverna.remote;

import java.util.HashSet;
import java.util.Vector;

import org.apache.axis.client.Service;
import org.apache.axis.client.Call;

/**
 * Basically all matching calls from org.vle.aid.metadata.ws.RepositoryWS and
 * org.vle.aid.metadata.ws.ThesaurusRepositoryWS More documentation about what
 * all these query does, refer to documentation on both classes.
 * 
 * aida-plugin
 * 
 * @author wibisono
 * @date Apr 23, 2009 10:29:08 PM
 */
public class AIDRemote {

    static org.apache.log4j.Logger logger			       = org.apache.log4j.Logger.getLogger(AIDRemote.class);

    /********************************************************************************************************
     * Calls provided by RepositoryDetectWS
     ********************************************************************************************************/
    public static String []  detectRepository(String axis_service, String sesame_server_url,  String repository, String username, String password ) throws Exception {
    	Call call = createAxisCall(axis_service, "detectRepository");
    	String[] result = (String []) call.invoke(new Object[] { sesame_server_url, repository, username, password});
	return result;
    }
    
    /********************************************************************************************************
     * Calls provided by RepositoryWS
     ********************************************************************************************************/
    public static String selectQuerySerialized(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String query_language, String select_output_format, String query) throws Exception {

	Call call = createAxisCall(axis_service, "selectQuerySerialized");
	String result = (String) call.invoke(new Object[] { sesame_server_url, repository, username, password, query_language,
		select_output_format, query });

	return result;
    }

    public static boolean addRdfStatement(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String subject, String predicate, String object) throws Exception {

	Call call = createAxisCall(axis_service, "addRdfStatement");
	boolean result = (Boolean) call.invoke(new Object[] { sesame_server_url, repository, username, password, subject, predicate, object});
	return result;
    }

    public static boolean removeRdfStatement(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String subject, String predicate, String object) throws Exception {

	Call call = createAxisCall(axis_service, "removeRdfStatement");
	boolean result = (Boolean) call.invoke(new Object[] { sesame_server_url, repository, username, password, subject, predicate, object});
	return result;
    }
    
    public static String[][] selectQuery(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String query_language, String query) throws Exception {

	Call call = createAxisCall(axis_service, "selectQuery");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, query_language,
		query });

	return result;
    }

    public static String[] getRepositories(String axis_service, String sesame_server_url, String username, String password,
	    String read_write) throws Exception {

	Call call = createAxisCall(axis_service, "getRepositories");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, username, password, read_write });

	return result;
    }

    /********************************************************************************************************
     * Calls provided by ThesaurusRepositoryWS
     ********************************************************************************************************/
    public static String[][] getNarrowerTerms(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term, String skosVersion, String virtuosoNamedGraph) throws Exception {
	Call call = createAxisCall(axis_service, "getNarrowerTerms");
	//String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term, AIDRemoteConfig.TOP_CONCEPT, AIDRemoteConfig.NARROWER_PREDICATE, skosVersion, virtuosoNamedGraph });
	return makeUnique(result);
    }

    public static String[][] getBroaderTerms(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getBroaderTerms");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[][] getRelatedTerms(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getRelatedTerms");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[] getRDFSLabels(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getRDFSLabels");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[] getPreferedTerms(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getPreferedTerms");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[] getAlternativeTerms(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getAlternativeTerms");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[] getDefinitions(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getDefinitions");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[] getNotes(String axis_service, String sesame_server_url, String repository, String username, String password,
	    String term) throws Exception {
	Call call = createAxisCall(axis_service, "getNotes");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[] getScopeNotes(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getScopeNotes");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[] getChangeNotes(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getChangeNotes");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[] getHistoryNotes(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getHistoryNotes");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[] getEditorialNotes(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getEditorialNotes");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[] getPublicNotes(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getPublicNotes");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[] getPrivateNotes(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getPrivateNotes");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[] getSubjectsOf(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String url) throws Exception {
	Call call = createAxisCall(axis_service, "getSubjectsOf");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, repository, username, password, url });
	return result;
    }

    public static String[] getSubjects(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getSubjects");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[] getPrimarySubjects(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getPrimarySubjects");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[] getPrimarySubjectsOf(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String url) throws Exception {
	Call call = createAxisCall(axis_service, "getPrimarySubjectsOf");
	String[] result = (String[]) call.invoke(new Object[] { sesame_server_url, repository, username, password, url });
	return result;
    }

    public static String[][] getTopConcepts(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String scheme_label, String skosVersion, String virtuosoNamedGraph) throws Exception {
		Call call = createAxisCall(axis_service, "getTopConcepts");
		//System.out.println("Calling top concept with "+axis_service+" " + sesame_server_url+" "+repository+" "+AIDRemoteConfig.CURRENT_HOST+" "+AIDRemoteConfig.TOP_CONCEPT+" "+AIDRemoteConfig.NARROWER_PREDICATE);
		String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, scheme_label, AIDRemoteConfig.TOP_CONCEPT, AIDRemoteConfig.NARROWER_PREDICATE, skosVersion, virtuosoNamedGraph });
		
		//System.out.println("CHECK " +result.length);
		//return new String[0][0];
		return makeUnique(result);
    }

    public static String[][] getConceptSchemesWithNamespace(String axis_service, String sesame_server_url, String repository,
	    String username, String password, String namespace) throws Exception {
		Call call = createAxisCall(axis_service, "getConceptSchemesWithNamespace");
		String[][] result = (String[][]) call.invoke(new Object[] { axis_service, sesame_server_url, repository, username, password, namespace });
		return result;
    }

    public static String[][] getConceptSchemes(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String skosVersion, String virtuosoNamedGraph) throws Exception {
		Call call = createAxisCall(axis_service, "getConceptSchemes");
		String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username,  password, AIDRemoteConfig.TOP_CONCEPT, AIDRemoteConfig.NARROWER_PREDICATE, skosVersion, virtuosoNamedGraph });
	
		return makeUnique(result);
    }

    public static String[][] getInSchemes(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getInSchemes");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[][] getInCollections(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getInCollections");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[][] getCollectionMembers(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getCollectionMembers");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[][] getMatches(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getMatches");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[][] getExactMatches(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getExactMatches");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static boolean removeExactMatch(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String subject, String object) throws Exception {
	Call call = createAxisCall(axis_service, "removeExactMatch");
	boolean result = (Boolean) call.invoke(new Object[] { sesame_server_url, repository, username, password, subject, object });
	return result;
    }

    public static String[][] getDisjointMatches(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getDisjointMatches");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static boolean removeDisjointMatch(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String subject, String object) throws Exception {
	Call call = createAxisCall(axis_service, "removeDisjointMatch");
	boolean result = (Boolean) call.invoke(new Object[] { sesame_server_url, repository, username, password, subject, object });
	return result;
    }

    public static String[][] getRelatedMatches(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getRelatedMatches");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static boolean removeRelatedMatch(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String subject, String object) throws Exception {
	Call call = createAxisCall(axis_service, "removeRelatedMatch");
	boolean result = (Boolean) call.invoke(new Object[] { sesame_server_url, repository, username, password, subject, object });
	return result;
    }

    public static String[][] getNarrowMatches(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getNarrowMatches");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static boolean removeNarrowMatch(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String subject, String object) throws Exception {
	Call call = createAxisCall(axis_service, "removeNarrowMatch");
	boolean result = (Boolean) call.invoke(new Object[] { sesame_server_url, repository, username, password, subject, object });
	return result;
    }

    public static String[][] getBroadMatches(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getBroadMatches");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static boolean removeBroadMatch(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String subject, String object) throws Exception {
	Call call = createAxisCall(axis_service, "removeBroadMatch");
	boolean result = (Boolean) call.invoke(new Object[] { sesame_server_url, repository, username, password, subject, object });
	return result;
    }

    public static String[][] getPartMatches(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getPartMatches");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[][] getTermCompletion(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getTermCompletion");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static boolean removePartMatch(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String subject, String object) throws Exception {
	Call call = createAxisCall(axis_service, "removePartMatch");
	boolean result = (Boolean) call.invoke(new Object[] { sesame_server_url, repository, username, password, subject, object });
	return result;
    }

    public static String[][] getWholeMatches(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getWholeMatches");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static boolean removeWholeMatch(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String subject, String object) throws Exception {
	Call call = createAxisCall(axis_service, "removeWholeMatch");
	boolean result = (Boolean) call.invoke(new Object[] { sesame_server_url, repository, username, password, subject, object });
	return result;
    }

    public static String[][] getTermUri(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getTermUri");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[][] getNumberOfNarrowerTerms(String axis_service, String sesame_server_url, String repository, String username,
	    String password, String term) throws Exception {
	Call call = createAxisCall(axis_service, "getNumberOfNarrowerTerms");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, term });
	return result;
    }

    public static String[][] getNumberOfNarrowerTermsMulti(String axis_service, String sesame_server_url, String repository,
	    String username, String password, String terms[]) throws Exception {
	Call call = createAxisCall(axis_service, "getNumberOfNarrowerTermsMulti");
	String[][] result = (String[][]) call.invoke(new Object[] { sesame_server_url, repository, username, password, terms });
	return result;
    }

    
    
    /** Common routine to setup service call to axis */
    static Call createAxisCall(String axis_service, String operationName) throws Exception {
	Service service = new Service();
	Call call = (org.apache.axis.client.Call) service.createCall();
	call.setTargetEndpointAddress(axis_service);
	call.setOperationName(operationName);
	return call;
    }

    /**
         * Some of those non skos returned duplicate children this makes it
         * unique
         */
    static String[][] makeUnique(String[][] dups) {
	HashSet<String> already = new HashSet<String>();
	Vector<String[]> result = new Vector<String[]>();

	for (String[] row : dups) {
	    // Comparing terms only not url, assuming its unique per node
	    if (already.contains(row[1]))
		continue;
	    already.add(row[1]);
	    result.add(row);
	}
	return result.toArray(new String[0][]);

    }

    
    
}
