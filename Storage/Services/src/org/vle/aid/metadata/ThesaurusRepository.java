/*
 * ThesaurusRepository.java
 *
 * Created on March 7, 2006, 10:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.metadata;

import java.util.Arrays;
import java.util.Comparator;

import javax.servlet.ServletException;
import javax.ws.rs.core.MultivaluedMap;

import org.vle.aid.metadata.exception.QueryException;
import org.vle.aid.metadata.exception.RemoveRDFException;

import org.apache.log4j.Logger;

/**
 * This class works on top of a regular {@link Repository}.
 * It adds high level thesaurus operations for manipulating repositories that contain SKOS and SKOS Mapping.
 * For documentation of the methods see {@link ThesaurusRepositoryWS}.
 * @author wrvhage
 */
public class ThesaurusRepository {

    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ThesaurusRepository.class);

    private Repository r = null;
    private boolean virtuoso = false;
    private String named_graph = "";
    private boolean skos_lens = false;
    private String skos_version = "";


    public ThesaurusRepository() {}

    /**
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param repository the name of the repository or model in the meta-data server (e.g. mem-rdfs-db)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame)
     */
    public ThesaurusRepository(String server_url,String repository,String username,String password) 
    {
        try 
        {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password);
            setRepository(r);
            setVirtuoso();
            if(getVirtuoso())
                setNamedGraph();
            setSkosVersion();
            setSkosLens();      
        } 
        catch (Exception e) 
        { 
        	e.printStackTrace(); 
        }
    }
    
    public ThesaurusRepository(MultivaluedMap<String, String> param) 
    {
        try 
        {
          String server_url = null;
          String repository = null;
          String username = null;
          String password = null;

          if (!param.containsKey("server_url")) 
          {
            throw new Exception("server_url not defined");
          } 
          else 
          {
            server_url = param.getFirst("server_url");
          }

          if (!param.containsKey("repository")) 
          {
            throw new Exception("repository not defined");
          } 
          else 
          {
            repository = param.getFirst("repository");
          }
          if (!param.containsKey("username")) 
          {
            throw new Exception("username not defined");
          } 
          else 
          {
            username = param.getFirst("username");
          }
          if (!param.containsKey("password")) 
          {
            throw new Exception("password not defined");
          } 
          else 
          {
            password = param.getFirst("password");
          }
          
          Repository r = RepositoryFactory.createRepository(server_url,repository,username,password);
          setRepository(r);
          setVirtuoso();
          if(getVirtuoso())
              setNamedGraph();
          setSkosVersion();
          setSkosLens(); 
          
        } 
        catch (Exception e) 
        { 
          e.printStackTrace(); 
        }
    }
    
    

    public void setRepository(Repository r) { this.r = r; }
    public Repository getRepository() { return r; }

    public void setSkosVersion() throws QueryException
    {
        this.skos_version = _detectSkosVersion();
    }
    public String getSkosVersion() { return this.skos_version; }

    public String _detectSkosVersion() throws QueryException
    {
        try
        {
            String query =
                "select ?S ?O ";

            // VIRTUOSO HANDLING
            if(!getNamedGraph().equalsIgnoreCase(""))
                query += "from <" + getNamedGraph() + "> ";

            query +=
                "where " +
                "{ ?S ?P ?O . " +
                " FILTER (regex(str(?O) , \"http://www.w3.org/2008/05/skos#Concept\", \"i\") || " +
                "         regex(str(?O) , \"http://www.w3.org/2004/02/skos/core#Concept\", \"i\"))" +
                "} limit 1";

            String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query);

            if(rv== null || rv.length==0)
                return "0";
            else if(rv[0][1].contains("2008"))
                return "08";
            else if(rv[0][1].contains("2004"))
                return "04";
            else return null;
        }
        catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("_detectSkosVersion Query Error:" + org.getMessage(), org);
	    }
    }


    public void setSkosLens() throws QueryException
    {
        if(getSkosVersion().equalsIgnoreCase("0"))
            this.skos_lens =  true;
        else
            this.skos_lens =  false;        
    }
    public boolean getSkosLens() { return this.skos_lens; }

    public void setVirtuoso() throws QueryException
    {
        r.setVirtuoso();
        this.virtuoso = r.getVirtuoso();
    }
    public boolean getVirtuoso() { return this.virtuoso; }

    public void setNamedGraph() throws QueryException
    {
    	
//        this.named_graph = _getVirtuosoNamedGraph(r.getRepository());
    	this.named_graph = r.getRepository();
    }
    public String getNamedGraph() { return this.named_graph; }

    public String _getVirtuosoNamedGraph(String repository) throws QueryException
    {
        try
        {
                String query1 = "SELECT ?src WHERE " +
                                "{GRAPH ?src {" +
                                "               ?s ?p ?o " +
                                                ". FILTER(regex(str(?src), \"/"+repository+"$\", \"i\")) " +
                                "}} limit 1 ";
                String[][] named_graph_res = r.selectQuery(r.getServer(),"",r.getUsername(),r.getPassword(),"sparql",query1);

                return named_graph_res[1][0];
        }
    	catch (Throwable org)
		{
			org.printStackTrace();

			throw new QueryException("_getVirtuosoNamedGraph Query Error:" + org.getMessage(), org);
	    }
    }

    /**
     * Sorts an array of pairs [[String URI, String label],...] in alphabetical order of the labels.
     */
    private String[][] _sortByLabel(String[][] uri_label) 
    {
        Arrays.sort(uri_label,new Comparator() 
        {
            public int compare(Object obj1, Object obj2) 
            {
                int result = 0;
                String[] str1 = (String[]) obj1;
                String[] str2 = (String[]) obj2;

                if(str1[1] == null ) return -1;                
                /* Sort on second element of each array (label) */
                if ((result = str1[1].compareToIgnoreCase(str2[1])) == 0) 
                {
                	if(str1[0] == null) return 1;
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

    /*
     * Semantic Relations
     */

    public String[][] getNarrowerTerms(String term) throws QueryException 
    {
    	try
    	{
            if(!getSkosLens())
            {
                String query = "";
                    /*"select N, NL " +
                    "from {B} skos:narrower {N}, {N} skos:prefLabel {NL} " +
                    "where B = <" + term + "> " +
                    "union select N, NL " +
                    "from {N} skos:broader {B}, {N} skos:prefLabel {NL} " +
                    "where B = <" + term + "> " +
                    "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                if(getSkosVersion().equalsIgnoreCase("08"))
                    query += "prefix skos:<http://www.w3.org/2008/05/skos#> ";
                else
                    query += "prefix skos:<http://www.w3.org/2004/02/skos/core#> ";

                query +=    "select DISTINCT ?N ?NL ";

                // VIRTUOSO HANDLING
                if(!getNamedGraph().equalsIgnoreCase(""))
                    query += "from <" + getNamedGraph() + "> ";

                query +=
                    "where	{ " +
                    "{ <" + term + "> skos:narrower ?N } " +
                    "union " +
                    "{ ?N skos:broader <" + term + "> } . " +
                    "OPTIONAL { ?N skos:prefLabel ?NL } }";

                String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query);

                String[][] rv2 = new String[rv.length][2];

                for(int i=0;i<rv2.length;i++) // without an rdfs:label retrieved, the second element substitutes the label
                {
                    rv2[i][0] = rv[i][0];

                    if(rv[i][1]==null)
                    {
                        if(rv[i][0].contains("://"))
                        {
                            if(rv[i][0].contains("#"))
                                rv2[i][1] = rv[i][0].substring(rv[i][0].lastIndexOf("#")+1);
                            else
                            {
                                String sub = rv[i][0].substring(rv[i][0].lastIndexOf("/"));
                                rv2[i][1] = sub.substring(sub.lastIndexOf("/")+1);
                            }
                        }
                        else
                            rv2[i][1] = rv[i][0];
                    }
                    else
                        rv2[i][1] = rv[i][1];
                }
                
                return _sortByLabel(rv2);
            }
            else //NO SKOS NARROWER TERMS: PROCEED WITH SKOS:LENS
            {
                String query =
                        "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                        "select distinct ?x ?l ";

                // VIRTUOSO HANDLING
                if(!getNamedGraph().equalsIgnoreCase(""))
                    query += "from <" + getNamedGraph() + "> ";

                query += "where {?x ?p <" + term + "> . " +
                        "OPTIONAL {  ?x rdfs:label ?l } . " +
						"OPTIONAL {  ?y ?p <"+term+"> . ?x ?p ?y . filter(?x != ?y) } . " + 
                        "FILTER(?x!=<" + term + "> &&  !(BOUND(?y)) &&  " +
                        "(?p=rdfs:subClassOf || ?p=rdf:type))} ";

                String[][] rv1 = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query);
                String[][] rv2 = new String[rv1.length][2];

                for(int i=0;i<rv1.length;i++) // without an rdfs:label retrieved, the second element substitutes the label
                {
                    rv2[i][0] = rv1[i][0];
                    if(rv1[i][1]==null)
                    {
                        if(rv1[i][0].contains("://"))
                        {
                            if(rv1[i][0].contains("#"))
                                rv2[i][1] = rv1[i][0].substring(rv1[i][0].lastIndexOf("#")+1);
                            else
                            {
                                String sub = rv1[i][0].substring(rv1[i][0].lastIndexOf("/"));
                                rv2[i][1] = sub.substring(sub.lastIndexOf("/")+1);
                            }
                        }
                        else
                            rv2[i][1] = rv1[i][0];
                    }
                    else
                        rv2[i][1] = rv1[i][1];
                }
                
                return _sortByLabel(rv2);
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
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select B, BL " +
                "from {N} skos:broader {B}, {B} skos:prefLabel {BL} " +
                "where N = <" + term + "> " +
                "union select B, BL " +
                "from {B} skos:narrower {N}, {B} skos:prefLabel {BL} " +
                "where N = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
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
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select R, RL " +
                "from {T} skos:related {R}, {R} skos:prefLabel {RL} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
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
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                 /*"select PL " +
                 "from {T} rdfs:label {PL} " +
                 "where T = <" + term + "> ");*/
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
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select PL " +
                "from {T} skos:prefLabel {PL} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
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
            if(!getSkosLens())
            {
                String query = "";
                    /*"select N, NL " +
                    "from {N} skos:prefLabel {NL} " +
                    "where NL like \""+term+"*\" IGNORE CASE " +
                    "union " +
                    "select N, NL " +
                    "from {N} skos:altLabel {NL} " +
                    "where NL like \""+term+"*\" IGNORE CASE " +
                    "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                if(getSkosVersion().equalsIgnoreCase("08"))
                    query += "prefix skos:<http://www.w3.org/2008/05/skos#> ";
                else
                    query += "prefix skos:<http://www.w3.org/2004/02/skos/core#> ";

                query +=    "select DISTINCT ?N ?NL ";

                // VIRTUOSO HANDLING
                if(!getNamedGraph().equalsIgnoreCase(""))
                    query += "from <" + getNamedGraph() + "> ";

                query +=
                    "where	{ " +
                    "{ ?N skos:prefLabel ?NL } " +
                    "union " +
                    "{ ?N skos:altLabel ?NL } . " +
                    "FILTER regex(str(?NL), \"^"+term+".*$\", \"i\")}";

                String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query);
                        
                return rv;
            }
            else
            {
                String query =
                        "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                        "select ?x ?l ";

                // VIRTUOSO HANDLING
                if(!getNamedGraph().equalsIgnoreCase(""))
                    query += "from <" + getNamedGraph() + "> ";

                query += "where { " +
                        "OPTIONAL {  ?x rdfs:label ?l } . " +
                        "FILTER regex(str(?l), \"^"+term+".*$\", \"i\")}";

                String[][] rv1 = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query);
				if(rv1 == null) return null;

                String[][] rv2 = new String[rv1.length][2];

                for(int i=0;i<rv1.length;i++) // without an rdfs:label retrieved, the second element substitutes the label
                {
                    rv2[i][0] = rv1[i][0];
                    if(rv1[i][1]==null)
                    {
                        if(rv1[i][0].contains("://"))
                        {
                            if(rv1[i][0].contains("#"))
                                rv2[i][1] = rv1[i][0].substring(rv1[i][0].lastIndexOf("#")+1);
                            else
                            {
                                String sub = rv1[i][0].substring(rv1[i][0].lastIndexOf("/"));
                                rv2[i][1] = sub.substring(sub.lastIndexOf("/")+1);
                            }
                        }
                        else
                            rv2[i][1] = rv1[i][0];
                    }
                    else
                        rv2[i][1] = rv1[i][1];
                }

                return rv2;
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
    		String query = "";
                /*"select PL " +
                "from {T} skos:altLabel {PL} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
            if(getSkosVersion().equalsIgnoreCase("08"))
                query += "prefix skos:<http://www.w3.org/2008/05/skos#> ";
            else
                query += "prefix skos:<http://www.w3.org/2004/02/skos/core#> ";

            query +=    "select DISTINCT ?PL " +
                        "where " +
                        "{ <" + term + "> skos:altLabel ?PL}";

            String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query);
            
    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getAlternativeTerms Error:" + org.getMessage(), org);
	    }
    }


    /*
     * Collections
     */

    public String[][] getInCollections(String term) throws QueryException 
    {
    	try
    	{
    		String query = /*"select C, CL, CT " +
	            "from {C} rdf:type {CT}, {C} skos:member {T}, {T} rdf:type {TT}, {C} rdfs:label {CL} " +
	            "where T = <" + term + "> and " +
	            "( TT = skos:Collection or TT = skos:Concept ) and " +
	            "CT = <http://www.w3.org/2004/02/skos/core#Collection> " +
	            "using namespace skos = <http://www.w3.org/2004/02/skos/core#>";*/
                "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select DISTINCT ?C ?CL ?CT " +
                "where {" +
                "?C rdf:type ?CT . " +
                "?C skos:member <" + term + "> . " +
                "{<" + term + "> rdf:type skos:Collection} union {<" + term + "> rdf:type skos:Concept} . " +
                "?C rdfs:label ?CL . " +
                "FILTER (?CT = skos:Collection) " +
                "}";
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query);
    		return _sortByLabel(rv);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getInCollections Error:" + org.getMessage(), org);
	    }
    }

    public String[][] getCollectionMembers(String collection) throws QueryException 
    {
    	try
    	{
	        String query = /*"select T, TL, TT " +
	            "from {C} rdf:type {CT}, {C} skos:member {T}, {T} rdf:type {TT}, {T} P {TL} " +
	            "where C = <" + collection + "> and " +
	            "( TT = skos:Collection or TT = skos:Concept ) and " +
	            "( P = rdfs:label or P = skos:prefLabel ) and " +
	            "CT = <http://www.w3.org/2004/02/skos/core#Collection> " +
	            "using namespace skos = <http://www.w3.org/2004/02/skos/core#>";*/
                "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select DISTINCT ?T ?TL ?TT " +
                "where {" +
                "<" + collection + "> rdf:type skos:Collection . " +
                "<" + collection + "> skos:member ?T . " +
                "?T rdf:type ?TT . " +
                "?T ?P ?TL . " +
                "FILTER ((?TT = skos:Collection || ?TT = skos:Concept) " +
                "&& (?P = rdfs:label || ?P = skos:prefLabel)) " +
                "}";
	        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query);
	        return _sortByLabel(rv);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getCollectionMembers Error:" + org.getMessage(), org);
	    }  
    }

    /*
     * Documentation
     */

    public String[] getDefinitions(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select PL " +
                "from {T} skos:definition {PL} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select ?PL " +
                "where " +
                "{ <" + term + "> skos:definition ?PL}");
    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getDefinitions Error:" + org.getMessage(), org);
	    }  
    }

    public String[] getNotes(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select S " +
                "from {T} Q {S} " +
                "where T = <" + term + "> and " +
                "isLiteral(S) and " +
                "( Q = <http://www.w3.org/2004/02/skos/core#scopeNote> or " +
                "Q = <http://www.w3.org/2004/02/skos/core#changeNote> or " +
                "Q = <http://www.w3.org/2004/02/skos/core#historyNote> or " +
                "Q = <http://www.w3.org/2004/02/skos/core#editorialNote> or " +
                "Q = <http://www.w3.org/2004/02/skos/core#privateNote> or " +
                "Q = <http://www.w3.org/2004/02/skos/core#publicNote> ) " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select DISTINCT ?S " +
                "where {" +
                "{ <" + term + "> skos:scopeNote ?S} union " +
                "{ <" + term + "> skos:changeNote ?S} union " +
                "{ <" + term + "> skos:historyNote ?S} union " +
                "{ <" + term + "> skos:editorialNote ?S} union " +
                "{ <" + term + "> skos:privateNote ?S} union " +
                "{ <" + term + "> skos:publicNote ?S} . " +
                "FILTER isLiteral(?S)}");
    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getNotes Error:" + org.getMessage(), org);
	    } 
    }

    public String[] getScopeNotes(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select S " +
                "from {T} skos:scopeNote {S} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select ?S " +
                "where " +
                "{ <" + term + "> skos:scopeNote ?S} ");
    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getScopeNotes Error:" + org.getMessage(), org);
	    } 
    }

    public String[] getChangeNotes(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select S " +
                "from {T} skos:changeNote {S} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select ?S " +
                "where " +
                "{ <" + term + "> skos:changeNote ?S} ");
    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getChangeNotes Error:" + org.getMessage(), org);
	    } 
    }

    public String[] getHistoryNotes(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select S " +
                "from {T} skos:historyNote {S} " +
                "where T = >" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select ?S " +
                "where " +
                "{ <" + term + "> skos:historyNote ?S} ");
    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getHistoryNotes Error:" + org.getMessage(), org);
	    } 
    }

    public String[] getEditorialNotes(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select S " +
                "from {T} skos:editorialNote {S} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select ?S " +
                "where " +
                "{ <" + term + "> skos:editorialNote ?S} ");
    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getEditorialNotes Error:" + org.getMessage(), org);
	    }
    }

    public String[] getPublicNotes(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select S " +
                "from {T} skos:publicNote {S} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select ?S " +
                "where " +
                "{ <" + term + "> skos:publicNote ?S} ");
    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getPublicNotes Error:" + org.getMessage(), org);
	    }
    }

    public String[] getPrivateNotes(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select S " +
                "from {T} skos:privateNote {S} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select ?S " +
                "where " +
                "{ <" + term + "> skos:privateNote ?S} ");
    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getPrivateNotes Error:" + org.getMessage(), org);
	    }
    }


    /*
     * Subject Indexing
     */

    public String[] getSubjectsOf(String url) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select T " +
                "from {T} skos:isSubjectOf {U} " +
                "where U = <" + url + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select ?T " +
                "where " +
                "{ ?T skos:isSubjectOf <" + url + ">} ");
    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getSubjectsOf Error:" + org.getMessage(), org);
	    }
    }

    public String[] getSubjects(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select U " +
                "from {T} skos:subject {U} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select ?U " +
                "where " +
                "{ <" + term + "> skos:subject ?U} ");
    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getSubjects Error:" + org.getMessage(), org);
	    }
    }

    public String[] getPrimarySubjects(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select U " +
                "from {T} skos:primarySubject {U} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select ?U " +
                "where " +
                "{ <" + term + "> skos:primarySubject ?U} ");
    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getPrimarySubjects Error:" + org.getMessage(), org);
	    }
    }

    public String[] getPrimarySubjectsOf(String url) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select T " +
                "from {T} skos:isPrimarySubjectOf {U} " +
                "where T = <" + url + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select ?T " +
                "where " +
                "{ ?T skos:isPrimarySubjectOf ?U . " +
                "FILTER (?T = <" + url + ">) } ");
    		return _getColumn(rv,0);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getPrimarySubjectsOf Error:" + org.getMessage(), org);
	    }
    }


    /*
     * Concept Schemes
     */
    
    public String[][] getTopConcepts(String scheme) throws QueryException 
    {
    	try
    	{
            if(!getSkosLens())
            {
                String query = "";
                String query_pref = "";
                    /*"select T, L " +
                    "from {S} skos:hasTopConcept {T}, {T} skos:prefLabel {L} " +
                    "where S = <" + scheme + "> " +
                    "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                if(getSkosVersion().equalsIgnoreCase("08"))
                    query_pref += "prefix skos:<http://www.w3.org/2008/05/skos#> ";
                else
                    query_pref += "prefix skos:<http://www.w3.org/2004/02/skos/core#> ";

                query = query_pref + "select ?T ?L ";

                // VIRTUOSO HANDLING
                if(!getNamedGraph().equalsIgnoreCase(""))
                    query += "from <" + getNamedGraph() + "> ";

                query +=
                    "where { " +
                    "{ <" + scheme + "> skos:hasTopConcept ?T } " +
                    "union " +
                    "{ ?T skos:topConceptOf <" + scheme + "> } ." +
                    "?T skos:prefLabel ?L} ";

                String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query);
                if(rv == null ||rv.length == 0) //A more standard but less formal approach
                {
                    String query1 = query_pref + "select ?T ";

                    // VIRTUOSO HANDLING
                    if(!getNamedGraph().equalsIgnoreCase(""))
                        query1 += "from <" + getNamedGraph() + "> ";

                    query1 += "where { " +
                            "{ <" + scheme + "> skos:hasTopConcept ?T } " +
                            "union " +
                            "{ ?T skos:topConceptOf <" + scheme + "> } }";

                    String[][] rv1 = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query1);

                    if(rv1==null || rv1.length == 0) //A definite but unformal approach
                    {
                        String query2 = query_pref + 
                                "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "prefix owl:<http://www.w3.org/2002/07/owl#> " +
                                "select distinct ?x ?l ";

                        // VIRTUOSO HANDLING
                        String namedGraph = getNamedGraph();
                        if(namedGraph != null && !namedGraph.equalsIgnoreCase(""))
                            query2 += "from <" + namedGraph + "> ";

			/* Change into simpler query based on Andrew's code */
			/*
                        query2 += "where {?x rdf:type skos:Concept . " +
                                   "OPTIONAL { {?z skos:narrower ?x} " +
                                   "            union " +
                                   "           {?x skos:broader ?z} . " +
                                   "            ?z rdf:type ?k . " +
                                   "            FILTER ( ?k=skos:Concept && ?z!=owl:Thing && ?x!=?z)} . " +
                                   "OPTIONAL { ?x skos:prefLabel ?l } ." +
                                   "FILTER(?x!=owl:Thing && !(isBlank(?x)) && !BOUND(?z) )}";

			*/
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

                        String[][] rv2 = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query2);
                        String[][] rv3 = new String[rv2.length][2];

                        for(int i=0;i<rv2.length;i++) // without an rdfs:label retrieved, the second element substitutes the label
                        {
                            rv3[i][0] = rv2[i][0];

                            if(rv2[i][1]==null)
                            {
                                if(rv2[i][0].contains("://"))
                                {
                                    if(rv2[i][0].contains("#"))
                                        rv3[i][1] = rv2[i][0].substring(rv2[i][0].lastIndexOf("#")+1);
                                    else
                                    {
                                        String sub = rv2[i][0].substring(rv2[i][0].lastIndexOf("/"));
                                        rv3[i][1] = sub.substring(sub.lastIndexOf("/")+1);
                                    }
                                }
                                else
                                    rv3[i][1] = rv2[i][0];
                            }
                            else
                                rv3[i][1] = rv2[i][1];
                        }
                        return _sortByLabel(rv3);
                    }

                    String[][] rv2 = new String[rv1.length][2];

                    for(int i=0;i<rv1.length;i++) // the second element substitutes the label
                    {
                        rv2[i][0] = rv1[i][0];
                        if(rv1[i][0].contains("#"))
                            rv2[i][1] = rv1[i][0].substring(rv1[i][0].lastIndexOf("#")+1);
                        else
                        {
                            String sub = rv1[i][0].substring(rv1[i][0].lastIndexOf("/"));
                            rv2[i][1] = sub.substring(sub.lastIndexOf("/")+1);
                        }
                    }
                    return _sortByLabel(rv2);
                }
                
                return _sortByLabel(rv);
            }
            else //NO SKOS TOP CONCEPTS: PROCEED WITH SKOS:LENS
            {
                String query =
                       /*"select x " +
                       "from {x} rdf:type {y} " +
                       "where y=owl:Class " +
                       "and x!=owl:Thing " +
                       "and not isBNode(x) " +
                       "and not exists (select x " +
                       "                from {x} rdfs:subClassOf {z}, " +
                       "                     {z} rdf:type {k} " +
                       "                where k=owl:Class and z!=owl:Thing and x!=z) " +
                       "using namespace owl=<http://www.w3.org/2002/07/owl#>, " +
                       "rdf=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>, " +
                       "rdfs=<http://www.w3.org/2000/01/rdf-schema#> ";*/
                       "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                       "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                       "prefix owl:<http://www.w3.org/2002/07/owl#> " +
                       "prefix jade:<http://jade.cselt.it/beangenerator#>" +
                       "select ?X ?L ";

                // VIRTUOSO HANDLING
                if(!getNamedGraph().equalsIgnoreCase(""))
                    query += "from <" + getNamedGraph() + "> ";

                /*
                 * OK This three attempts of query is a hack that should be fixed
                 * as soon as I have a cleaner idea on how to implement rules for SKOS Lens
                 * Which based on template and can be adjusted for these different views
                 */
                String queryOWL =  query + " WHERE { ?X rdf:type owl:Class . " +
                                   		   " 		 OPTIONAL   { ?X rdfs:label ?L } . " +
										   " 		 OPTIONAL   { ?X rdfs:subClassOf ?Y } . " +
										   " 	 	 FILTER (!BOUND(?Y)) } ";

				/*
                                   " OPTIONAL   { ?X rdfs:subClassOf ?Z . " +
                                   "              ?Z rdf:type ?K . " +
                                   "               FILTER ( ?K=owl:Class && ?Z!=owl:Thing && ?X!=?Z)} . " +
                                   " FILTER(?X!=owl:Thing && !(isBlank(?X)) && !BOUND(?Z) )}"; */

                // Try rdfs:Class instead of owl:Class
                String queryRDFS = query + " WHERE { ?X rdf:type rdfs:Class ." +
                         " OPTIONAL { ?X rdfs:subClassOf ?Y } . " +
                         " OPTIONAL { ?X rdfs:label ?L } . " + 
						 // we don't want ?X to have any parent ?Y
                         " FILTER(!BOUND(?Y))  }";

                // For zhiming's ontology with jade, if we give filter bound on Z it does not work
                String queryJade = query + " WHERE { ?X rdf:type jade:JADE-CLASS ." +
                         " OPTIONAL { ?X rdfs:subClassOf ?Z } . " +
                         " OPTIONAL { ?X rdfs:label ?L } } ";

                // For uniprot keywords
                String queryUniprotKW = query + " WHERE { ?X rdf:type <http://purl.uniprot.org/core/Concept>." +
                         " OPTIONAL { ?X rdfs:label ?L } } ";



                // Try the original RDFS/OWL query first, from kostas
                String[][] rv1 = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",queryRDFS);

                if(rv1 == null || rv1.length == 0){
                    rv1 = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",queryOWL);
                }
			
				// Other possible queries
                if(rv1 == null || rv1.length == 0){
                    rv1 = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",queryJade);
                }
                
                if(rv1 == null || rv1.length == 0){
                    rv1 = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",queryUniprotKW);
                }

                if(rv1 == null ) return null;
                String[][] rv2 = new String[rv1.length][2];

                for(int i=0;i<rv1.length;i++) // without an rdfs:label retrieved, the second element substitutes the label
                {
                    rv2[i][0] = rv1[i][0];

                    if(rv1[i][1]==null)
                    {
                        if(rv1[i][0].contains("://"))
                        {
                            if(rv1[i][0].contains("#"))
                                rv2[i][1] = rv1[i][0].substring(rv1[i][0].lastIndexOf("#")+1);
                            else
                            {
                                String sub = rv1[i][0].substring(rv1[i][0].lastIndexOf("/"));
                                rv2[i][1] = sub.substring(sub.lastIndexOf("/")+1);
                            }
                        }
                        else
                            rv2[i][1] = rv1[i][0];
                    }
                    else
                        rv2[i][1] = rv1[i][1];
                }
                return _sortByLabel(rv2);
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
            if(!getSkosLens())
            {
                String query = "";
                String query_pref = "";
                /*"select S, L " +
                    "from {S} rdf:type {C}, " +
                    "{S} P {L} " +
                    "where C = <http://www.w3.org/2004/02/skos/core#ConceptScheme> and " +
                    "( P = <http://www.w3.org/2000/01/rdf-schema#label> or " +
                    " P = <http://www.w3.org/2004/02/skos/core#prefLabel> )";*/
                if(getSkosVersion().equalsIgnoreCase("08"))
                    query_pref += "prefix skos:<http://www.w3.org/2008/05/skos#> ";
                else
                    query_pref += "prefix skos:<http://www.w3.org/2004/02/skos/core#> ";

                query_pref += "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                              "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> ";
                query = query_pref + "select ?S ?L ";

                // VIRTUOSO HANDLING
                if(!getNamedGraph().equalsIgnoreCase(""))
                    query += "from <" + getNamedGraph() + "> ";

                query += "where " +
                    "{ ?S rdf:type skos:ConceptScheme . " +
                    "  OPTIONAL { ?S ?P ?L . " +
                    " 		  FILTER (?P = rdfs:label || ?P = skos:prefLabel) } " +
                    "} ";
                String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query);

                if(rv.length == 0) //A more standard but less formal approach
                {
                    String query1 = query_pref + "select ?S ";

                    // VIRTUOSO HANDLING
                    if(!getNamedGraph().equalsIgnoreCase(""))
                        query1 += "from <" + getNamedGraph() + "> ";

                    query1 += "where " +
                        "{ ?S rdf:type skos:ConceptScheme } ";

                    String[][] rv1 = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query1);

                    if(rv1.length == 0) //A definite but unformal approach
                    {
                        String[][] rv2 = new String[1][2];
                        String url = r.getServer();
                        if(url.endsWith("/"))
                            rv2[0][0] = url + r.getRepository();
                        else
                            rv2[0][0] = url + "/" + r.getRepository();
                        rv2[0][1] = r.getRepository();
                        
                        return rv2;
                    }

                    String[][] rv2 = new String[rv1.length][2];

                    for(int i=0;i<rv1.length;i++) // the second element substitutes the label
                    {
                        rv2[i][0] = rv1[i][0];
                        if(rv1[i][0].contains(".owl"))
                            rv2[i][1] = rv1[i][0].substring(rv1[i][0].lastIndexOf("/"), rv1[i][0].lastIndexOf(".owl"));
                        else if(rv1[i][0].contains("#"))
                        {
                            rv2[i][1] = rv1[i][0].substring(rv1[i][0].lastIndexOf("/"), rv1[i][0].lastIndexOf("#"));
                        }
                        else
                        {
                            String sub = rv1[i][0].substring(rv1[i][0].lastIndexOf("/"));
                            rv2[i][1] = sub.substring(sub.lastIndexOf("/")+1);
                        }
                    }
                    return _sortByLabel(rv2);
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

                String[][] rv0 = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query);

                if(rv0==null || rv0.length == 0) //A more standard but less formal approach
                {
                    query = "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "prefix owl:<http://www.w3.org/2002/07/owl#> " +
                            "select ?S ";                           

                    // VIRTUOSO HANDLING                    
                    if(!getNamedGraph().equalsIgnoreCase(""))
                        query += "from <" + getNamedGraph() + "> ";

                    query += "where { ?S rdf:type owl:Ontology } limit 1";  // we dont need more than 1, because anyway the TopConcepts
                                                                            // SKOS_lens Query that follows and depends on this one
                                                                            // does not consider seperate concept chemes
                                                                            // as those do not exist in reality in a simple RDF/OWL repository
                    
                    String[][] rv1 = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query);
                    String[][] rv2 = new String[rv1.length][2];

                    for(int i=0;i<rv1.length;i++) // the second element substitutes the label
                    {
                        rv2[i][0] = rv1[i][0];
                        if(rv1[i][0].contains(".owl"))
                            rv2[i][1] = rv1[i][0].substring(rv1[i][0].lastIndexOf("/"), rv1[i][0].lastIndexOf(".owl"));
                        else
                        {
                            String sub = rv1[i][0].substring(rv1[i][0].lastIndexOf("/"));
                            rv2[i][1] = sub.substring(sub.lastIndexOf("/")+1);
                        }                            
                    }
                    return _sortByLabel(rv2);
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

    public String[][] getInSchemes(String term) throws QueryException 
    {
    	try
    	{
	        String query = /*"( select C, L " +
	            "from {T} skos:inScheme {C}, {C} P {L} " +
	            "where T = <" + term + "> and " +
	            "( P = <http://www.w3.org/2000/01/rdf-schema#label> or " +
	            " P = <http://www.w3.org/2004/02/skos/core#prefLabel> ) " +
	            ") union ( select C, L " +
	            "from {C} skos:hasTopConcept {T}, {C} P {L} " +
	            "where T = <" + term + "> and " +
	            "( P = <http://www.w3.org/2000/01/rdf-schema#label> or " +
	            " P = <http://www.w3.org/2004/02/skos/core#prefLabel> ) ) " +
	            "using namespace skos = <http://www.w3.org/2004/02/skos/core#>";*/
                "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select DISTINCT ?C ?L " +
                "where	{ " +
                "{ <" + term + "> skos:inScheme ?C } " +
                "union " +
                "{ ?C skos:hasTopConcept <" + term + "> } . " +
                "?C ?P ?L . " +
                "FILTER (?P = rdfs:label || ?P = skos:prefLabel) " +
                "} ";
	        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query);
	        return _sortByLabel(rv);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getInSchemes Error:" + org.getMessage(), org);
	    }
    }


    public String[][] getBroadMatches(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"( select B, BL " +
                "from {N} skosmap:broadMatch {B}, {B} skos:prefLabel {BL} " +
                "where N = <" + term + "> " +
                " ) union " +
                "( select B, BL " +
                "from {B} skosmap:narrowMatch {N}, {B} skos:prefLabel {BL} " +
                "where N = <" + term + "> ) " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                "skosmap = <http://www.w3.org/2004/02/skos/mapping#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "prefix skosmap:<http://www.w3.org/2004/02/skos/mapping#> " +
                "select DISTINCT ?B ?BL " +
                "where	{ " +
                "{ <" + term + "> skosmap:broadMatch ?B } " +
                "union " +
                "{ ?B skosmap:narrowMatch <" + term + "> } . " +
                "?B skos:prefLabel ?BL " +
                "} ");
    		if (rv == null || rv.length == 0) 
    		{
    			rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                                /*"( select B " +
                                "from {A} skosmap:broadMatch {B} " +
                                "where A = <" + term + "> " +
                                " ) union " +
                                "( select B " +
                                "from {B} skosmap:narrowMatch {A} " +
                                "where A = <" + term + "> ) " +
                                "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                                "skosmap = <http://www.w3.org/2004/02/skos/mapping#>");*/
                                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                                "prefix skosmap:<http://www.w3.org/2004/02/skos/mapping#> " +
                                "select DISTINCT ?B " +
                                "where	{ " +
                                "{ <" + term + "> skosmap:broadMatch ?B } " +
                                "union " +
                                "{ ?B skosmap:narrowMatch <" + term + "> } " +
                                "} ");
    			return _addEmptyColumn(rv);
    		} 
    		else 
    		{
    			return _sortByLabel(rv);
    		}
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getBroadMatches Error:" + org.getMessage(), org);
	    }
    }

    public boolean removeBroadMatch(String s,String o) throws RemoveRDFException 
    {
    	try
    	{
    		boolean b = true;
    		b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      s,"<http://www.w3.org/2004/02/skos/mapping#broadMatch>",o);
    		b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      o,"<http://www.w3.org/2004/02/skos/mapping#narrowMatch>",s);
    		return b;
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new RemoveRDFException("removeBroadMatch Error:" + org.getMessage(), org);
	    }
    }

    public String[][] getNarrowMatches(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"( select N, L " +
                "from {B} skosmap:narrowMatch {N}, {N} skos:prefLabel {L} " +
                "where B = <" + term + "> " +
                " ) union " +
                "( select N, L " +
                "from {N} skosmap:broadMatch {B}, {N} skos:prefLabel {L} " +
                "where B = <" + term + "> ) " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                "skosmap = <http://www.w3.org/2004/02/skos/mapping#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "prefix skosmap:<http://www.w3.org/2004/02/skos/mapping#> " +
                "select DISTINCT ?N ?L " +
                "where	{ " +
                "{ <" + term + "> skosmap:narrowMatch ?N } " +
                "union " +
                "{ ?N skosmap:broadMatch <" + term + "> } . " +
                "?N skos:prefLabel ?L " +
                "} ");
    		if (rv == null || rv.length == 0) 
    		{
    			rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                            /*"( select B " +
                            "from {A} skosmap:narrowMatch {B} " +
                            "where A = <" + term + "> " +
                            " ) union " +
                            "( select B " +
                            "from {B} skosmap:broadMatch {A} " +
                            "where A = <" + term + "> ) " +
                            "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                            "skosmap = <http://www.w3.org/2004/02/skos/mapping#>");*/
                            "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                            "prefix skosmap:<http://www.w3.org/2004/02/skos/mapping#> " +
                            "select DISTINCT ?B " +
                            "where	{ " +
                            "{ <" + term + "> skosmap:narrowMatch ?B } " +
                            "union " +
                            "{ ?B skosmap:broadMatch <" + term + "> } " +
                            "} ");
    			return _addEmptyColumn(rv);
    		} 
    		else 
    		{
    			return _sortByLabel(rv);
    		}
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getNarrowMatches Error:" + org.getMessage(), org);
	    }
    }

    public boolean removeNarrowMatch(String s,String o) throws RemoveRDFException 
    {
    	try
    	{
    		boolean b = true;
    		b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      s,"<http://www.w3.org/2004/02/skos/mapping#narrowMatch>",o);
    		b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      o,"<http://www.w3.org/2004/02/skos/mapping#broadMatch>",s);
    		return b;
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new RemoveRDFException("removeNarrowMatch Error:" + org.getMessage(), org);
	    }
    }


    public String[][] getExactMatches(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                                    /*"( select B, BL " +
                                    "from {A} skosmap:exactMatch {B}, {B} skos:prefLabel {BL} " +
                                    "where A = <" + term + "> " +
                                    " ) union " +
                                    "( select B, BL " +
                                    "from {B} skosmap:exactMatch {A}, {B} skos:prefLabel {BL} " +
                                    "where A = <" + term + "> ) " +
                                    "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                                    "skosmap = <http://www.w3.org/2004/02/skos/mapping#>");*/
                                    "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                                    "prefix skosmap:<http://www.w3.org/2004/02/skos/mapping#> " +
                                    "select DISTINCT ?B ?BL " +
                                    "where	{ " +
                                    "{ <" + term + "> skosmap:exactMatch ?B } " +
                                    "union " +
                                    "{ ?B skosmap:exactMatch <" + term + "> } . " +
                                    "?B skos:prefLabel ?BL " +
                                    "} ");
            if (rv == null || rv.length == 0) 
            {
            	rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                                /*"( select A " +
                                "from {B} skosmap:exactMatch {A} " +
                                "where B = <" + term + "> " +
                                " ) union " +
                                "( select A " +
                                "from {A} skosmap:exactMatch {B} " +
                                "where B = <" + term + "> ) " +
                                "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                                "skosmap = <http://www.w3.org/2004/02/skos/mapping#>");*/
                                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                                "prefix skosmap:<http://www.w3.org/2004/02/skos/mapping#> " +
                                "select DISTINCT ?A " +
                                "where	{ " +
                                "{ <" + term + "> skosmap:exactMatch ?A } " +
                                "union " +
                                "{ ?A skosmap:exactMatch <" + term + "> } " +
                                "} ");
            	return _addEmptyColumn(rv);
            } 
            else 
            {
            return _sortByLabel(rv);
            }
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getExactMatches Error:" + org.getMessage(), org);
	    }
    }

    public boolean removeExactMatch(String s,String o) throws RemoveRDFException 
    {
    	try
    	{
    		boolean b = true;
    		b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      s,"<http://www.w3.org/2004/02/skos/mapping#exactMatch>",o);
    		b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      o,"<http://www.w3.org/2004/02/skos/mapping#exactMatch>",s);
    		return b;
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new RemoveRDFException("removeExactMatch Error:" + org.getMessage(), org);
	    }
    }

    public String[][] getMatches(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"( select M, ML " +
                "from {N} P {M}, {M} skos:prefLabel {ML} " +
                "where N = <" + term + "> and " +
                "( P = <http://www.w3.org/2004/02/skos/mapping#exactMatch> or " +
                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#disjointMatch> or " +
                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#relatedMatch> or " +
                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#partMatch> or " +
                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#wholeMatch> or " +
                " P = <http://www.w3.org/2004/02/skos/mapping#narrowMatch> or " +
                " P = <http://www.w3.org/2004/02/skos/mapping#broadMatch> ) " +
                " ) union " +
                "( select M, ML " +
                "from {M} P {N}, {M} skos:prefLabel {BL} " +
                "where N = <" + term + "> and " +
                "( P = <http://www.w3.org/2004/02/skos/mapping#exactMatch> or " +
                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#disjointMatch> or " +
                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#relatedMatch> or " +
                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#partMatch> or " +
                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#wholeMatch> or " +
                " P = <http://www.w3.org/2004/02/skos/mapping#narrowMatch> or " +
                " P = <http://www.w3.org/2004/02/skos/mapping#broadMatch> ) ) " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "select DISTINCT ?M ?ML " +
                "where	{ " +
                "{ <" + term + "> ?P ?M } " +
                "union " +
                "{ ?M ?P <" + term + "> } . " +
                "?M skos:prefLabel ?ML . " +
                "FILTER (?P = <http://www.w3.org/2004/02/skos/mapping#exactMatch> || " +
                "?P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#disjointMatch> || " +
                "?P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#relatedMatch> || " +
                "?P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#partMatch> || " +
                "?P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#wholeMatch> || " +
                "?P = <http://www.w3.org/2004/02/skos/mapping#narrowMatch> || " +
                "?P = <http://www.w3.org/2004/02/skos/mapping#broadMatch>) " +
                "} ");
    		if (rv == null || rv.length == 0) 
    		{
    			rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
	                /*"( select M " +
	                "from {N} P {M} " +
	                "where N = <" + term + "> and " +
	                "( P = <http://www.w3.org/2004/02/skos/mapping#exactMatch> or " +
	                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#disjointMatch> or " +
	                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#relatedMatch> or " +
	                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#partMatch> or " +
	                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#wholeMatch> or " +
	                " P = <http://www.w3.org/2004/02/skos/mapping#narrowMatch> or " +
	                " P = <http://www.w3.org/2004/02/skos/mapping#broadMatch> ) " +
	                " ) union " +
	                "( select M " +
	                "from {M} P {N} " +
	                "where N = <" + term + "> and " +
	                "( P = <http://www.w3.org/2004/02/skos/mapping#exactMatch> or " +
	                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#disjointMatch> or " +
	                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#relatedMatch> or " +
	                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#partMatch> or " +
	                " P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#wholeMatch> or " +
	                " P = <http://www.w3.org/2004/02/skos/mapping#narrowMatch> or " +
	                " P = <http://www.w3.org/2004/02/skos/mapping#broadMatch> ) ) " +
	                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");*/
                    "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                    "select DISTINCT ?M " +
                    "where	{ " +
                    "{ <" + term + "> ?P ?M } " +
                    "union " +
                    "{ ?M ?P <" + term + "> } . " +
                    "FILTER (?P = <http://www.w3.org/2004/02/skos/mapping#exactMatch> || " +
                    "?P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#disjointMatch> || " +
                    "?P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#relatedMatch> || " +
                    "?P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#partMatch> || " +
                    "?P = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#wholeMatch> || " +
                    "?P = <http://www.w3.org/2004/02/skos/mapping#narrowMatch> || " +
                    "?P = <http://www.w3.org/2004/02/skos/mapping#broadMatch>) " +
                    "} ");
    			return _addEmptyColumn(rv);
    		} 
    		else 
    		{
    			return _sortByLabel(rv);
    		}
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getMatches Error:" + org.getMessage(), org);
	    }
    }

    public String[][] getDisjointMatches(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"( select B, BL " +
                "from {A} skosmapext:disjointMatch {B}, {B} skos:prefLabel {BL} " +
                "where A = <" + term + "> " +
                " ) union " +
                "( select B, BL " +
                "from {B} skosmapext:disjointMatch {A}, {B} skos:prefLabel {BL} " +
                "where A = <" + term + "> ) " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "prefix skosmapext:<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#> " +
                "select DISTINCT ?B ?BL " +
                "where	{ " +
                "{ <" + term + "> skosmapext:disjointMatch ?B } " +
                "union " +
                "{ ?B skosmapext:disjointMatch <" + term + "> } . " +
                "?B skos:prefLabel ?BL " +
                "} ");
    		if (rv == null || rv.length == 0) 
    		{
    			rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                            /*"( select B " +
                            "from {A} skosmapext:disjointMatch {B} " +
                            "where A = <" + term + "> " +
                            " ) union " +
                            "( select B " +
                            "from {B} skosmapext:disjointMatch {A} " +
                            "where A = <" + term + "> ) " +
                            "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                            "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");*/
                            "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                            "prefix skosmapext:<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#> " +
                            "select DISTINCT ?B " +
                            "where	{ " +
                            "{ <" + term + "> skosmapext:disjointMatch ?B } " +
                            "union " +
                            "{ ?B skosmapext:disjointMatch <" + term + "> } " +
                            "} ");
    			return _addEmptyColumn(rv);
    		} 
    		else 
    		{
    			return _sortByLabel(rv);
    		}
        }
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getDisjointMatches Error:" + org.getMessage(), org);
	    }
    }

    public boolean removeDisjointMatch(String s,String o) throws RemoveRDFException 
    {
    	try
    	{
    		boolean b = true;
    		b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      s,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#disjointMatch>",o);
    		b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      o,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#disjointMatch>",s);
    		return b;
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new RemoveRDFException("removeDisjointMatch Error:" + org.getMessage(), org);
	    }
    }

    public String[][] getRelatedMatches(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"(select A, L " +
                "from {B} skosmapext:relatedMatch {A}, {A} skos:prefLabel {L} " +
                "where B = <" + term + "> " +
                " ) union " +
                "( select A, L " +
                "from {A} skosmapext:relatedMatch {B}, {A} skos:prefLabel {L} " +
                "where B = <" + term + "> ) " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "prefix skosmapext:<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#> " +
                "select DISTINCT ?A ?L " +
                "where	{ " +
                "{ <" + term + "> skosmapext:relatedMatch ?A } " +
                "union " +
                "{ ?A skosmapext:relatedMatch <" + term + "> } . " +
                "?A skos:prefLabel ?L " +
                "} ");
    		if (rv == null || rv.length == 0) 
    		{
    			rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                            /*"( select A " +
                            "from {B} skosmapext:relatedMatch {A} " +
                            "where B = <" + term + "> " +
                            " ) union " +
                            "( select A " +
                            "from {A} skosmapext:relatedMatch {B} " +
                            "where B = <" + term + "> ) " +
                            "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                            "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");*/
                            "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                            "prefix skosmapext:<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#> " +
                            "select DISTINCT ?A " +
                            "where	{ " +
                            "{ <" + term + "> skosmapext:relatedMatch ?A } " +
                            "union " +
                            "{ ?A skosmapext:relatedMatch <" + term + "> } " +
                            "} ");
    			return _addEmptyColumn(rv);
    		} 
    		else 
    		{
    			return _sortByLabel(rv);
    		}
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getRelatedMatches Error:" + org.getMessage(), org);
	    }	
    }

    public boolean removeRelatedMatch(String s,String o) throws RemoveRDFException 
    {
    	try
    	{
    		boolean b = true;
    		b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      s,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#relatedMatch>",o);
    		b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      o,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#relatedMatch>",s);
    		return b;
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new RemoveRDFException("removeRelatedMatch Error:" + org.getMessage(), org);
	    }	
    }

    public String[][] getPartMatches(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"( select N, L " +
                "from {B} skosmapext:partMatch {N}, {N} skos:prefLabel {L} " +
                "where B = <" + term + "> " +
                " ) union " +
                "( select N, L " +
                "from {N} skosmapext:wholeMatch {B}, {N} skos:prefLabel {L} " +
                "where B = <" + term + "> ) " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "prefix skosmapext:<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#> " +
                "select DISTINCT ?N ?L " +
                "where	{ " +
                "{ <" + term + "> skosmapext:partMatch ?N } " +
                "union " +
                "{ ?N skosmapext:wholeMatch <" + term + "> } . " +
                "?N skos:prefLabel ?L " +
                "} ");
    		if (rv == null || rv.length == 0) 
    		{
    			rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                            /*"( select A " +
                            "from {B} skosmapext:partMatch {A} " +
                            "where B = <" + term + "> " +
                            " ) union " +
                            "( select A " +
                            "from {A} skosmapext:wholeMatch {B} " +
                            "where B = <" + term + "> ) " +
                            "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                            "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");*/
                            "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                            "prefix skosmapext:<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#> " +
                            "select DISTINCT ?A " +
                            "where	{ " +
                            "{ <" + term + "> skosmapext:partMatch ?A } " +
                            "union " +
                            "{ ?A skosmapext:wholeMatch <" + term + "> } " +
                            "} ");
    			return _addEmptyColumn(rv);
    		} 
    		else 
    		{
    			return _sortByLabel(rv);
    		}
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getPartMatches Error:" + org.getMessage(), org);
	    }
    }

    public boolean removePartMatch(String s,String o) throws RemoveRDFException 
    {
    	try
    	{
    		boolean b = true;
    		b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      s,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#partMatch>",o);
    		b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      o,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#wholeMatch>",s);
    		return b;
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new RemoveRDFException("removePartMatch Error:" + org.getMessage(), org);
	    }
    }

    public String[][] getWholeMatches(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"( select N, L " +
                "from {B} skosmapext:wholeMatch {N}, {N} skos:prefLabel {L} " +
                "where B = <" + term + "> " +
                " ) union " +
                "( select N, L " +
                "from {N} skosmapext:partMatch {B}, {N} skos:prefLabel {L} " +
                "where B = <" + term + "> ) " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");*/
                "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                "prefix skosmapext:<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#> " +
                "select DISTINCT ?N ?L " +
                "where	{ " +
                "{ <" + term + "> skosmapext:wholeMatch ?N } " +
                "union " +
                "{ ?N skosmapext:partMatch <" + term + "> } . " +
                "?N skos:prefLabel ?L " +
                "} ");
    		if (rv == null || rv.length == 0) 
    		{
    			rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                            /*"( select A " +
                            "from {B} skosmapext:wholeMatch {A} " +
                            "where B = <" + term + "> " +
                            " ) union " +
                            "( select A " +
                            "from {A} skosmapext:partMatch {B} " +
                            "where B = <" + term + "> ) " +
                            "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                            "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");*/
                            "prefix skos:<http://www.w3.org/2004/02/skos/core#> " +
                            "prefix skosmapext:<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#> " +
                            "select DISTINCT ?A " +
                            "where	{ " +
                            "{ <" + term + "> skosmapext:wholeMatch ?A } " +
                            "union " +
                            "{ ?A skosmapext:partMatch <" + term + "> } " +
                            "} ");
    			return _addEmptyColumn(rv);
    		} 
    		else 
    		{
    			return _sortByLabel(rv);
    		}
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getWholeMatches Error:" + org.getMessage(), org);
	    }
    }

    public boolean removeWholeMatch(String s,String o) throws RemoveRDFException 
    {
    	try
    	{
    		boolean b = true;
    		b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      s,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#wholeMatch>",o);
    		b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      o,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#partMatch>",s);
    		return b;
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new RemoveRDFException("removeWholeMatch Error:" + org.getMessage(), org);
	    }
    }

    public String[][] getTermUri(String label) throws QueryException 
    {
    	try
    	{
            String query = "";
    		    /*"select C, L from {C} P {L} " +
                "where L like \"" + label + "\" ignore case and " +
                "( P = <http://www.w3.org/2004/02/skos/core#prefLabel> or " +
                " P = <http://www.w3.org/2004/02/skos/core#altLabel> )");*/
            if(getSkosVersion().equalsIgnoreCase("08"))
                query += "prefix skos:<http://www.w3.org/2008/05/skos#> ";
            else
                query += "prefix skos:<http://www.w3.org/2004/02/skos/core#> ";

            query +=
                "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                "select ?C ?L " +
                "where	{ " +
                "?C ?P ?L . " +
                "FILTER ((?P = rdfs:altlabel || ?P = skos:prefLabel) && " +
                "regex(str(?L), \"^"+label+"$\", \"i\"))}";

            String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",query);
            
    		return _sortByLabel(rv);
    	}
    	catch (Throwable org)
		{
			org.printStackTrace();
				
			throw new QueryException("getTermUri Error:" + org.getMessage(), org);
	    }
    }

    public String[][] getNumberOfNarrowerTerms(String term) throws QueryException 
    {
    	try
    	{
    		String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"sparql",
                /*"select C, N from {C} stat:numberOfNarrowerTerms {N} " +
                "where C = <" + term + "> " +
                "using namespace stat = <http://www.few.vu.nl/~wrvhage/2007/03/statistics#>");*/
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
}
