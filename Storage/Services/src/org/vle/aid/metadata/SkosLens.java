/*
 * SkosLens.java
 *
 * Created on June 26 2009
 * by: wongiseng
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.metadata;

import java.util.Arrays;
import java.util.Comparator;

import java.util.Vector;
import java.util.TreeMap;

import javax.ws.rs.core.MultivaluedMap;

import org.vle.aid.metadata.exception.QueryException;
import org.vle.aid.metadata.ws.SkosLensWS;


/**
 * This class works on top of a regular {@link Repository}.
 * It adds high level thesaurus operations for manipulating repositories that contain SKOS and SKOS Mapping.
 * For documentation of the methods see {@link SkosLensWS}.
 * @author wrvhage
 */
public class SkosLens {

    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SkosLens.class);

    private Repository theRepository = null;
    private boolean is_virtuoso = false;
    private String named_graph = "";
    private boolean is_skosLens = true;
    private String skos_version = "";

    String PREFIX_XSD       = "prefix xsd:<http://www.w3.org/2001/XMLSchema#>";
    String PREFIX_DC        = "prefix dc:<http://purl.org/dc/elements/1.1/>";

    String PREFIX_RDF       = "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
    String PREFIX_RDFS      = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>";

    String PREFIX_SKOS_08   = "prefix skos:<http://www.w3.org/2008/05/skos#>";
    String PREFIX_SKOS_04   = "prefix skos:<http://www.w3.org/2004/02/skos/core#>";

    String PREFIX_OWL       = "prefix owl:<http://www.w3.org/2002/07/owl#>";
    String PREFIX_OWL2      = "prefix owl2:<http://www.w3.org/2006/12/owl2#>";
    String PREFIX_OWL2XML   = "prefix owl2xml:<http://www.w3.org/2006/12/owl2-xml#>";

    String top_concept         	= "rdfs:Class";
    String narrower_predicate   = "rdfs:SubClassOf";

    public SkosLens() {}

    /**
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param repository the name of the repository or model in the meta-data server (e.g. mem-rdfs-db)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame)
     */
    public SkosLens(String server_url,String repository,String username,String password)
    {
        try
        {
            theRepository = RepositoryFactory.createRepository(server_url,repository,username,password);
            setRepository(theRepository);
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
    }

    
    public SkosLens(MultivaluedMap<String, String> param) {
    	try {
    	    String server_url = getParameter("server_url", param);
            String repository = getParameter("repository", param);
            String username   = getParameter("username", param);
            String password   = getParameter("password", param);
            
            theRepository = RepositoryFactory.createRepository(server_url,repository,username,password);
            setRepository(theRepository);
            
    		top_concept 		= getParameter("top_concept", param);
    		narrower_predicate 	= getParameter("narrower_predicate", param);
    		skos_version		= getParameter("skos_version", param);
    		is_skosLens			= skos_version.equals("Not Skos") ? true : false;
    		is_virtuoso 		= getParameter("virtuoso_namedgraph",param).startsWith("Not a Virtuoso") ? false : true;
            named_graph 		= is_virtuoso ? getParameter("virtuoso_namedgraph",param): "";
            
    	} catch(Exception e){
    		e.printStackTrace();
    	}
	}
    
    // Constructor helper
    public void setLensType(String topConcepts, String narrowerPredicate, String skosVersion, String virtuosoNamedGraph){
        top_concept = topConcepts;
        narrower_predicate = narrowerPredicate;
        skos_version = skosVersion;
        is_skosLens = skosVersion.equals("Not Skos") ? true : false;
        is_virtuoso = virtuosoNamedGraph.startsWith("Not a Virtuoso") ? false : true;
        named_graph = is_virtuoso ? virtuosoNamedGraph : "";
    }
    
	/**
     * Array of string version
     */
    public static Vector <String [] > getAvailableLensesAsString(){
           Vector <String []> lenses = new Vector <String []>();
           lenses.add(new String []{"OWL Classes",   "owl:Class",      "rdfs:subClassOf"});
           lenses.add(new String []{"OWL ObjectProperty",   "owl:ObjectProperty",      "owl:subPropertyOf"});
           lenses.add(new String []{"OWL AnnotationProperty",   "owl:AnnotationProperty",      "owl:subPropertyOf"});
           lenses.add(new String []{"OWL DataProperty",   "owl:DataProperty",      "owl:subPropertyOf"});

           lenses.add(new String []{"RDFS Classes",  "rdfs:Class",     "rdfs:subClassOf"});
           lenses.add(new String []{"RDF Properties","rdf:Property",   "rdfs:subPropertyOf"});
           lenses.add(new String []{"JADE Classes",  "JADE:Class",     "JADE:SubClassOf"});
           lenses.add(new String []{"SKOS 2004", "skos:topConceptOf", "skos:narrower"});
           lenses.add(new String []{"SKOS 2008", "skos:topConceptOf", "skos:narrower"});
           return lenses;
    }
    /*
     * Available skos lenses, just return all the possible ones.
     */
    public static Vector <SkosLensType> getAvailableLenses(){
           Vector <SkosLensType> lenses = new Vector <SkosLensType>();
           lenses.add(new SkosLensType("OWL Classes",   "owl:Class",      "rdfs:subClassOf"));
           lenses.add(new SkosLensType("OWL ObjectProperty",   "owl:ObjectProperty",      "owl:subPropertyOf"));
           lenses.add(new SkosLensType("OWL AnnotationProperty",   "owl:AnnotationProperty",      "owl:subPropertyOf"));
           lenses.add(new SkosLensType("OWL DataProperty",   "owl:DataProperty",      "owl:subPropertyOf"));

           lenses.add(new SkosLensType("RDFS Classes",  "rdfs:Class",     "rdfs:subClassOf"));
           lenses.add(new SkosLensType("RDF Properties","rdf:Property",   "rdfs:subPropertyOf"));
           lenses.add(new SkosLensType("JADE Classes",  "JADE:Class",     "JADE:SubClassOf"));
           lenses.add(new SkosLensType("SKOS 2004", "skos:topConceptOf", "skos:narrower"));
           lenses.add(new SkosLensType("SKOS 2008", "skos:topConceptOf", "skos:narrower"));
           return lenses;
    }

    
    public String getLensConcept(){
        return top_concept;
    }
    public String getLensNarrowerPredicate(){
        return narrower_predicate;
    }
    public void setRepository(Repository r) { this.theRepository = r; }
    public Repository getRepository() { return theRepository; }

    private String[][] makeURIUnique(String [][] dup){
			TreeMap <String, String []> uniqMap = new TreeMap<String, String[]>();
			for(String[] row : dup)
				uniqMap.put(row[0], row);
			
			if(dup == null || dup.length <1 ) return new String[0][0];
			String [][] res = new String[uniqMap.size()][dup[0].length];
		
			int i=0;
			for(String [] row : uniqMap.values())
				res[i++] = row;

			return res;

	}
    /**
     * Sorts an array of pairs [[String URI, String label],...] in alphabetical order of the labels.
     */
    private String[][] _sortByLabel(String[][] dup_uri_label)
    {
		if(dup_uri_label == null) return new String[0][0];	
		// need this to avoid duplicate uri with different labels

		String [][] uri_label = makeURIUnique(dup_uri_label);
        Arrays.sort(uri_label,new Comparator()
        {
            public int compare(Object obj1, Object obj2)
            {
                int result = 0;
                String[] str1 = (String[]) obj1;
                String[] str2 = (String[]) obj2;
                /* Sort on second element of each array (label) */
                if ((result = str1[1].compareToIgnoreCase(str2[1])) == 0)
                {
                    /* If same label, sort on second element (uri) */
                    result = str1[0].compareToIgnoreCase(str2[0]);
                }
                return result;
            }
        });
        return uri_label;
    }

    /**
     * Turns an array of arrays into an array of singletons.
     * @param mat a matrix as an array of arrays
     * @param col the column to single out
     */
    private String[] _getColumn(String[][] mat,int col)
    {
        String[] rv = new String[mat.length];
        for (int i=0;i<mat.length;i++)
        {
            rv[i] = mat[i][col];
        }
        Arrays.sort(rv,String.CASE_INSENSITIVE_ORDER);
        return rv;
    }

    /**
     * Takes an array of singleton arrays and appends an empty string to each singleton,
     * turning it into an array of pairs.
     */
    private String[][] _addEmptyColumn(String[][] vec)
    {
        if (vec == null) return null;
        String[][] rv = new String[vec.length][2];
        for (int i=0;i<vec.length;i++)
        {
            rv[i][0] = vec[i][0];
            rv[i][1] = "";
        }
        return rv;
    }

    /**
     * Takes queryResult array (?Term ?Label), if the results contains no label, then this function will try to assign label
     * Either by taking something after # or the last string after / within the URL
     * Return value is then sorted. 
     * @param queryResult
     * @return
     */
    private String [][] _appendLabelAndSort(String [][] queryResult){
    	
		if(queryResult == null || queryResult.length ==0) return new String[0][0];

		String[][] result = new String[queryResult.length][2];

        for(int i=0;i<queryResult.length;i++) // without an rdfs:label retrieved, the second element substitutes the label
        {
        	// The URI
            result[i][0] = queryResult[i][0];

            // If this result does not have label
            if(queryResult[i][1]==null)
            {
                if(queryResult[i][0].contains("://"))
                {
                    if(queryResult[i][0].contains("#"))
                        result[i][1] = queryResult[i][0].substring(queryResult[i][0].lastIndexOf("#")+1);
                    else
                    {
                        String sub = queryResult[i][0].substring(queryResult[i][0].lastIndexOf("/"));
                        result[i][1] = sub.substring(sub.lastIndexOf("/")+1);
                    }
                }
                else
                    result[i][1] = queryResult[i][0];
            }
            else
                result[i][1] = queryResult[i][1];
        }
        return _sortByLabel(result);    
    }
    
    /**
     * Append label for concept schemes retrieved
     * @param queryResult
     * @return
     */
    private String [][] _appendCSLabelAndSort(String [][] queryResult){
        String[][] result = new String[queryResult.length][2];

        for(int i=0;i<queryResult.length;i++) // the second element substitutes the label
        {
            result[i][0] = queryResult[i][0];
            if(queryResult[i][0].contains(".owl"))
                result[i][1] = queryResult[i][0].substring(queryResult[i][0].lastIndexOf("/"), queryResult[i][0].lastIndexOf(".owl"));
            else if(queryResult[i][0].contains("#"))
            {
                result[i][1] = queryResult[i][0].substring(queryResult[i][0].lastIndexOf("/"), queryResult[i][0].lastIndexOf("#"));
            }
            else
            {
                String sub = queryResult[i][0].substring(queryResult[i][0].lastIndexOf("/"));
                result[i][1] = sub.substring(sub.lastIndexOf("/")+1);
            }
        }
        return _sortByLabel(result);
    }
    
    private String _getSkosPrefix(){
    	String result="";
        if(skos_version.equalsIgnoreCase("08"))
        	result = "prefix skos:<http://www.w3.org/2008/05/skos#> ";
        else
        	result= "prefix skos:<http://www.w3.org/2004/02/skos/core#> ";
        return result;
    }

    /*
     * Semantic Relations
     */

    public String[][] getNarrowerTerms(String term) throws QueryException
    {
    	try
    	{
            if(!is_skosLens)
            {
                String query = _getSkosPrefix();

                query +=    "select DISTINCT ?N ?NL ";

                // VIRTUOSO HANDLING
                if(!named_graph.equalsIgnoreCase("") && named_graph.indexOf("Not Virtuoso")<0 )
                    query += "from <" + named_graph + "> ";
				else
				if(theRepository.getRepository().toLowerCase().startsWith("http://") || theRepository.getRepository().toLowerCase().startsWith("file")){
                    query += "from <" + theRepository.getRepository() + "> ";
				}


                query +=
                    "where	{ " +
                    "{ <" + term + "> skos:narrower ?N } " +
                    "union " +
                    "{ ?N skos:broader <" + term + "> } . " +
                    "OPTIONAL { ?N skos:prefLabel ?NL } . " +
                    "FILTER(?N!=<" + term + ">) }"; 

                String[][] result = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);
                
				if(result == null) {
					result = new AIDRepository().selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);
				} 

                return _appendLabelAndSort(result);
                
            }
            else //NO SKOS NARROWER TERMS: PROCEED WITH SKOS:LENS
            {
                String query =
                        "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                        "select distinct ?x ?l ";

                // VIRTUOSO HANDLING this need to be changed
                if(!named_graph.equalsIgnoreCase("") && named_graph.indexOf("Not Virtuoso")<0 )
                    query += "from <" + named_graph + "> ";
				else
				if(theRepository.getRepository().toLowerCase().startsWith("http://") || theRepository.getRepository().toLowerCase().startsWith("file")){
                    query += "from <" + theRepository.getRepository() + "> ";
				}

                query += "where {?x ?p <" + term + "> . " +
                        "OPTIONAL {  ?x rdfs:label ?l } . " +
						"OPTIONAL {  ?mid ?p <"+term+"> . ?x ?p ?mid . filter(?x != ?mid) } . " + 
						// We don't want anything in between ?x and term, so ?mid should not be bound/existed
                        "FILTER(?x!=<" + term + "> &&  !(BOUND(?mid)) &&  " +
                        "(?p=" + narrower_predicate + "  || ?p=rdf:type))} "; 



               String[][] result = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);

				if(result == null) {
					result = new AIDRepository().selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);
				} 

                return _appendLabelAndSort(result);
            }
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("getNarrowerTerms Error:" + org.getMessage(), org);
	    }
    }

    public String[][] getBroaderTerms(String term) throws QueryException
    {
    	try
    	{
    		String[][] rv = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select DISTINCT ?B ?BL " +
                "where	{ " +
                "{ <" + term + "> skos:broader ?B } " +
                "union " +
                "{ ?B skos:narrower <" + term + "> } . " +
                "?B skos:prefLabel ?BL}");
    		return _sortByLabel(rv);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("getBroaderTerms Error:" + org.getMessage(), org);
	    }
    }

    public String[][] getRelatedTerms(String term) throws QueryException
    {
    	try
    	{
    		String[][] rv = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select ?R ?RL " +
                "where " +
                "{ <" + term + "> skos:related ?R . " +
                "?R skos:prefLabel ?RL}");
    		return _sortByLabel(rv);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("getRelatedTerms Error:" + org.getMessage(), org);
	    }
    }


    /*
     * Labelling
     */

    public String[] getRDFSLabels(String term) throws QueryException
    {
    	try
    	{
    		String[][] rv = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",
                 "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                 "select ?PL " +
                 "where " +
                 "{ <" + term + "> rdfs:label ?PL}");
    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("getRDFSLabels Error:" + org.getMessage(), org);
	    }
    }

    public String[] getPreferedTerms(String term) throws QueryException
    {
    	try
    	{
    		String[][] rv = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select ?PL " +
                "where " +
                "{ <" + term + "> skos:prefLabel ?PL}");
    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("getPreferedTerms Error:" + org.getMessage(), org);
	    }
    }

    public String[][] getTermCompletion(String term) throws QueryException
    {
    	try
    	{
            if(!is_skosLens)
            {
                String query = _getSkosPrefix();
                    /*"select N, NL " +
                    "from {N} skos:prefLabel {NL} " +
                    "where NL like \""+term+"*\" IGNORE CASE " +
                    "union " +
                    "select N, NL " +
                    "from {N} skos:altLabel {NL} " +
                    "where NL like \""+term+"*\" IGNORE CASE " +
                    "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/

                query +=    "select DISTINCT ?N ?NL ";

                // VIRTUOSO HANDLING
                if(!named_graph.equalsIgnoreCase("") && named_graph.indexOf("Not Virtuoso")<0 )
                    query += "from <" + named_graph + "> ";
				else
				if(theRepository.getRepository().toLowerCase().startsWith("http://") || theRepository.getRepository().toLowerCase().startsWith("file")){
                    query += "from <" + theRepository.getRepository() + "> ";
				}

                query +=
                    "where	{ " +
                    "{ ?N skos:prefLabel ?NL } " +
                    "union " +
                    "{ ?N skos:altLabel ?NL } . " +
                    "FILTER regex(str(?NL), \"^"+term+".*$\", \"i\")}";

                String[][] rv = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);
				if(rv == null) {
					rv = new AIDRepository().selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);
				} 

                return rv;
            }
            else
            {
                String query =
                        "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                        "select ?x ?l ";

                // VIRTUOSO HANDLING
                if(!named_graph.equalsIgnoreCase(""))
                    query += "from <" + named_graph + "> ";

                query += "where { " +
                        "OPTIONAL {  ?x rdfs:label ?l } . " +
                        "FILTER regex(str(?l), \"^"+term+".*$\", \"i\")}";

                String[][] result = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);
                return _appendLabelAndSort(result);
            }
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("getTermCompletion Error:" + org.getMessage(), org);
	    }
    }

    public String[] getAlternativeTerms(String term) throws QueryException
    {
    	try
    	{
    		String query = _getSkosPrefix();

            query +=    "select DISTINCT ?PL " +
                        "where " +
                        "{ <" + term + "> skos:altLabel ?PL}";

            String[][] rv = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);
			if(rv == null) {
					rv = new AIDRepository().selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);
			} 


    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("getAlternativeTerms Error:" + org.getMessage(), org);
	    }
    }


    /*
     * Getting top concept based on current concept scheme. There are two parts, one for dealing with SKOS repository, and the other one for dealing with non skos.
     * The one dealing with non skos repository does not care about concept schemes. 
     */

    public String[][] getTopConcepts(String scheme) throws QueryException
    {
    	try
    	{
            if(!is_skosLens)
            {
                String query = "";
                String query_pref = _getSkosPrefix();
                query = query_pref + "select ?T ?L ";

                // VIRTUOSO HANDLING
                if(!named_graph.equalsIgnoreCase("") && named_graph.indexOf("Not Virtuoso")<0 )
                    query += "from <" + named_graph + "> ";
				else
				if(theRepository.getRepository().toLowerCase().startsWith("http://") || theRepository.getRepository().toLowerCase().startsWith("file")){
                    query += "from <" + theRepository.getRepository() + "> ";
				}

                query +=
                    "where { " +
                    "{ <" + scheme + "> skos:hasTopConcept ?T } " +
                    "union " +
                    "{ ?T skos:topConceptOf <" + scheme + "> } ." +
                    "?T skos:prefLabel ?L} ";

                
				logger.info("Check q : "+query);
                String[][] rv = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);

				if(rv == null) {
					rv = new AIDRepository().selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);
				} 


                if(rv != null && rv.length == 0) //A more standard but less formal approach
                {
                    String query1 = query_pref + "select ?T ";

                    // VIRTUOSO HANDLING
                	if(!named_graph.equalsIgnoreCase("") && named_graph.indexOf("Not Virtuoso")<0 )
                        query1 += "from <" + named_graph + "> ";
					else
					if(theRepository.getRepository().toLowerCase().startsWith("http://") || theRepository.getRepository().toLowerCase().startsWith("file")){
						query1 += "from <" + theRepository.getRepository() + "> ";
					}

                    query1 += "where { " +
                            "{ <" + scheme + "> skos:hasTopConcept ?T } " +
                            "union " +
                            "{ ?T skos:topConceptOf <" + scheme + "> } }";
                    

                    String[][] result1 = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query1);

					if(result1 == null) {
						result1 = new AIDRepository().selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query1);
					} 


                    if(result1 == null || result1.length == 0) //A definite but unformal approach notice scheme is ignored here
                    {
                        String query2 = query_pref +
                                "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "prefix owl:<http://www.w3.org/2002/07/owl#> " +
                                "select distinct ?x ?l ";

                        // VIRTUOSO HANDLING
                		if(!named_graph.equalsIgnoreCase("") && named_graph.indexOf("Not Virtuoso")<0 )
                            query2 += "from <" + named_graph + "> ";
						else
						if(theRepository.getRepository().toLowerCase().startsWith("http://") || theRepository.getRepository().toLowerCase().startsWith("file")){
							query2 += "from <" + theRepository.getRepository() + "> ";
						}


                        query2 +=   "   WHERE { "+
                                    "         ?x rdf:type skos:Concept . "+
                                    "         ?x skos:prefLabel ?l ."+
                                    "         OPTIONAL { "+
                                    "           {?x skos:broader ?y}"+
                                    "               UNION "+
                                    "           {?y skos:narrower ?x}"+
                                    "         } ."+
                                    "         FILTER (!bound(?y)) "+
                                    "   }";
                        
                        String[][] result2 = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query2);

                     	if(result2 == null) {
							result2 = new AIDRepository().selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query2);

						} 

   
                        return _appendLabelAndSort(result2);
                    } // end of attempt

                    _appendLabelAndSort(result1);
                }

                return _sortByLabel(rv);
            }
            else //NO SKOS TOP CONCEPTS: PROCEED WITH SKOS:LENS
            {
                String query =
                       "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                       "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                       "prefix owl:<http://www.w3.org/2002/07/owl#> " +
                       "select ?x ?l ";

                // VIRTUOSO HANDLING
                if(!named_graph.equalsIgnoreCase("") && named_graph.indexOf("Not Virtuoso")<0)
                    query += "from <" + named_graph + "> ";
				else
				// Handling neither virtuoso nor sesame
				if(theRepository.getRepository().toLowerCase().startsWith("http://") || theRepository.getRepository().toLowerCase().startsWith("file")){
                    query += "from <" + theRepository.getRepository() + "> ";
				}
					

                // If LENSE_CONCEPT is defined
                if(!top_concept.equals("")){
                    query += "where  {  ?x rdf:type "+ top_concept + " . " +
                           "		    OPTIONAL {  ?x "+ narrower_predicate +" ?parent } . " +
                           "		    OPTIONAL {  ?x rdfs:label ?l } . " +
						   			    // top level has no parent
                           "		    FILTER(!BOUND(?parent))}";
                }else {
                    query += "where  {  ?x "+ narrower_predicate +" ?parent . "+
                           "		    OPTIONAL {  ?x rdfs:label ?l } . " +
						   			    // top level has no parent
                           "			FILTER(!BOUND(?parent))}";
                }

				logger.info("Check ns : "+query);
                String[][] result = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);

               	if(result == null || result.length == 0){
					result = new AIDRepository().selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);
				}

				System.out.println(result);
                return _appendLabelAndSort(result);
            }
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("getTopConcepts Error:" + org.getMessage(), org);
	    }
    }

    public String[][] getConceptSchemes() throws QueryException
    {
    	try
    	{
            return getConceptSchemes("");
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("getConceptSchemes Error:" + org.getMessage(), org);
	    }
    }

    public String[][] getConceptSchemes(String ns) throws QueryException
    {
    	try
    	{
            if(!is_skosLens)
            {
                String query = "";
                String query_pref = _getSkosPrefix();
                
                query_pref += "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                              "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> ";
                
                query = query_pref + "select ?S ?L ";

                // VIRTUOSO HANDLING
                if(!named_graph.equalsIgnoreCase("") && named_graph.indexOf("Not Virtuoso")<0)
                    query += "from <" + named_graph + "> ";

                query += "where " +
                    "{ ?S rdf:type skos:ConceptScheme . " +
                    " ?S ?P ?L . " +
                    " FILTER (?P = rdfs:label || ?P = skos:prefLabel) " +
                    "} ";

                String[][] rv = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);

                if(rv == null || rv.length == 0) //A more standard but less formal approach
                {
                    String query1 = query_pref + "select ?S ";

                    // VIRTUOSO HANDLING
                	if(!named_graph.equalsIgnoreCase("") && named_graph.indexOf("Not Virtuoso")<0)
                        query1 += "from <" + named_graph + "> ";

                    query1 += "where " +
                        "{ ?S rdf:type skos:ConceptScheme } ";

                    String[][] rv1 = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query1);

                    if(rv1 == null || rv1.length == 0) //A definite but unformal approach
                    {
                        String[][] rv2 = new String[1][2];
                        String url = theRepository.getServer();
                        if(url.endsWith("/"))
                            rv2[0][0] = url + theRepository.getRepository();
                        else
                            rv2[0][0] = url + "/" + theRepository.getRepository();
                        rv2[0][1] = theRepository.getRepository();

                        return rv2;
                    }

                    return _appendCSLabelAndSort(rv1);
                }

                return _sortByLabel(rv);
            }
            else //NO SKOS CONCEPT SCHEME: PROCEED WITH SKOS:LENS
            {
                String query =
                        "prefix dc:<http://purl.org/dc/elements/1.1/> " +
                        "prefix owl:<http://www.w3.org/2002/07/owl#> " +
                        "select ?O " +
                        "where " +
                        "{ owl:Ontology dc:title ?O }";

                String[][] rv0 = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);
				
				if(rv0 == null) {
					rv0 = new AIDRepository().selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);

				} 

                if(rv0.length == 0) //A more standard but less formal approach
                {
                    query = "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "prefix owl:<http://www.w3.org/2002/07/owl#> " +
                            "select ?S ";

                    // VIRTUOSO HANDLING
                	if(!named_graph.equalsIgnoreCase("") && named_graph.indexOf("Not Virtuoso")<0 )
                        query += "from <" + named_graph + "> ";

                    query += "where { ?S rdf:type owl:Ontology } limit 1";  // we dont need more than 1, because anyway the TopConcepts
                                                                            // SKOS_lens Query that follows and depends on this one
                                                                            // does not consider seperate concept chemes
                                                                            // as those do not exist in reality in a simple RDF/OWL repository

                    String[][] result = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);
					if(result == null) return null;
                    return _appendCSLabelAndSort(result);
                }

                String[][] rv2 = new String[rv0.length][2];

                for(int i=0;i<rv0.length;i++) // the second element substitutes the label - here the same
                {
                    rv2[i][0] = rv0[i][0];
                    rv2[i][1] = rv0[i][0];
                }

                return _sortByLabel(rv2);
            }
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("getConceptSchemes(string) Error:" + org.getMessage(), org);
	    }
    }


    public String[][] getNumberOfNarrowerTerms(String term) throws QueryException
    {
    	try
    	{
    		logger.debug("getting nrnt for <" + term + ">");
    		String[][] rv = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",
                "prefix stat:<http://www.few.vu.nl/~wrvhage/2007/03/statistics#> " +
                "select ?C ?N " +
                "where " +
                "{ ?C stat:numberOfNarrowerTerms ?N . " +
                "FILTER (?C = <" + term + ">) }");
    		return rv;
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("getNumberOfNarrowerTerms Error:" + org.getMessage(), org);
	    }
    }

    public String[][] getNumberOfNarrowerTerms(String[] terms) throws QueryException
    {
    	try
    	{
    		String [][] rv = new String[terms.length][2];
    		for (int i=0;i<terms.length;i++)
    		{
    			logger.debug("loop getting nrnt for <" + terms[i] + ">");
    			rv[i] = getNumberOfNarrowerTerms(terms[i])[0];
    		}
    		return rv;
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("getNumberOfNarrowerTerms(Array) Error:" + org.getMessage(), org);
	    }
    }

    public String[][] getTermUri(String label) throws QueryException
    {
    	try
    	{
            String query = "";
            if(skos_version.equalsIgnoreCase("08"))
                query += "prefix skos:<http://www.w3.org/2008/05/skos#> ";
            else
                query += "prefix skos:<http://www.w3.org/2004/02/skos/core#> ";

            query +=
                "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                "select ?C ?L where	{ " +
                "?C ?P ?L . " +
                "FILTER ((?P = rdfs:altlabel || ?P = skos:prefLabel) && " +
                "regex(str(?L), \"^"+label+"$\", \"i\"))}";

            String[][] rv = theRepository.selectQuery(theRepository.getServer(),theRepository.getRepository(),theRepository.getUsername(),theRepository.getPassword(),"sparql",query);

    		return _sortByLabel(rv);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("getTermUri Error:" + org.getMessage(), org);
	    }
    }

    public String getParameter(String paramString, MultivaluedMap<String, String> param) throws Exception {
      	  if (!param.containsKey(paramString)) 
              throw new Exception(paramString + " not defined");
            return param.getFirst(paramString);          
      }


	/**
	 * Get only query string that we are sending */

    public String getTopConceptsQS(String scheme) throws QueryException
    {
            if(!is_skosLens)
            {
                String query = "";
                String query_pref = _getSkosPrefix();
                query = query_pref + "select ?T ?L ";

                // VIRTUOSO HANDLING
                if(!named_graph.equalsIgnoreCase("") && named_graph.indexOf("Not Virtuoso")<0 )
                    query += "from <" + named_graph + "> ";
				else
				if(theRepository.getRepository().toLowerCase().startsWith("http://") || theRepository.getRepository().toLowerCase().startsWith("file")){
                    query += "from <" + theRepository.getRepository() + "> ";
				}

                query +=
                    "where { " +
                    "{ <" + scheme + "> skos:hasTopConcept ?T } " +
                    "union " +
                    "{ ?T skos:topConceptOf <" + scheme + "> } ." +
                    "?T skos:prefLabel ?L} ";

                
				return query;

            }
            else //NO SKOS TOP CONCEPTS: PROCEED WITH SKOS:LENS
            {
                String query =
                       "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                       "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                       "prefix owl:<http://www.w3.org/2002/07/owl#> " +
                       "select ?x ?l ";

                // VIRTUOSO HANDLING
                if(!named_graph.equalsIgnoreCase("") && named_graph.indexOf("Not Virtuoso")<0)
                    query += "from <" + named_graph + "> ";
				else
				// Handling neither virtuoso nor sesame
				if(theRepository.getRepository().toLowerCase().startsWith("http://") || theRepository.getRepository().toLowerCase().startsWith("file")){
                    query += "from <" + theRepository.getRepository() + "> ";
				}
					

                // If LENSE_CONCEPT is defined
                if(!top_concept.equals("")){
                    query += "where  {  ?x rdf:type "+ top_concept + " . " +
                           "		    OPTIONAL {  ?x "+ narrower_predicate +" ?parent } . " +
                           "		    OPTIONAL {  ?x rdfs:label ?l } . " +
						   			    // top level has no parent
                           "		    FILTER(!BOUND(?parent))}";
                }else {
                    query += "where  {  ?x "+ narrower_predicate +" ?parent . "+
                           "		    OPTIONAL {  ?x rdfs:label ?l } . " +
						   			    // top level has no parent
                           "			FILTER(!BOUND(?parent))}";
                }
				return query;
            }
    	
    }
    
    public String getNarrowerTermsQS(String term) throws QueryException
    {
    	try
    	{
            if(!is_skosLens)
            {
                String query = _getSkosPrefix();

                query +=    "select DISTINCT ?N ?NL ";

                // VIRTUOSO HANDLING
                if(!named_graph.equalsIgnoreCase("") && named_graph.indexOf("Not Virtuoso")<0 )
                    query += "from <" + named_graph + "> ";
				else
				if(theRepository.getRepository().toLowerCase().startsWith("http://") || theRepository.getRepository().toLowerCase().startsWith("file")){
                    query += "from <" + theRepository.getRepository() + "> ";
				}


                query +=
                    "where	{ " +
                    "{ <" + term + "> skos:narrower ?N } " +
                    "union " +
                    "{ ?N skos:broader <" + term + "> } . " +
                    "OPTIONAL { ?N skos:prefLabel ?NL } . " +
                    "FILTER(?N!=<" + term + ">) }"; 

                return query;                
            }
            else //NO SKOS NARROWER TERMS: PROCEED WITH SKOS:LENS
            {
                String query =
                        "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                        "select distinct ?x ?l ";

                // VIRTUOSO HANDLING this need to be changed
                if(!named_graph.equalsIgnoreCase("") && named_graph.indexOf("Not Virtuoso")<0 )
                    query += "from <" + named_graph + "> ";
				else
				if(theRepository.getRepository().toLowerCase().startsWith("http://") || theRepository.getRepository().toLowerCase().startsWith("file")){
                    query += "from <" + theRepository.getRepository() + "> ";
				}

                query += "where {?x ?p <" + term + "> . " +
                        "OPTIONAL {  ?x rdfs:label ?l } . " +
						"OPTIONAL {  ?mid ?p <"+term+"> . ?x ?p ?mid . filter(?x != ?mid) } . " + 
						// We don't want anything in between ?x and term, so ?mid should not be bound/existed
                        "FILTER(?x!=<" + term + "> &&  !(BOUND(?mid)) &&  " +
                        "(?p=" + narrower_predicate + "  || ?p=rdf:type))} "; 



                return query;
            }
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("getNarrowerTerms Error:" + org.getMessage(), org);
	    }
    }


}
