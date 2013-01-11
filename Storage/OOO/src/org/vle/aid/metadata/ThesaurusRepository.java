/*
 * ThesaurusRepository.java
 *
 * Created on March 7, 2006, 10:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.metadata;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

/**
 * This class works on top of a regular {@link Repository}.
 * It adds high level thesaurus operations for manipulating repositories that contain SKOS and SKOS Mapping.
 * For documentation of the methods see {@link ThesaurusRepositoryWS}.
 * @author wrvhage
 */
public class ThesaurusRepository {
        
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ThesaurusRepository.class);

    private Repository r = null;

    public ThesaurusRepository() {}

    /**
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param repository the name of the repository or model in the meta-data server (e.g. mem-rdfs-db)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame)
     */
    public ThesaurusRepository(String server_url,String repository,String username,String password) {
        try {
            Repository r = RepositoryFactory.createRepository(server_url,repository,username,password);
            setRepository(r);
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    public void setRepository(Repository r) { this.r = r; }
    public Repository getRepository() { return r; }
    
    /**
     * Sorts an array of pairs [[String URI, String label],...] in alphabetical order of the labels.
     */
    private String[][] _sortByLabel(String[][] uri_label) {
        Arrays.sort(uri_label,new Comparator() {
            public int compare(Object obj1, Object obj2) {
                int result = 0;
                String[] str1 = (String[]) obj1;
                String[] str2 = (String[]) obj2;
                /* Sort on second element of each array (label) */
                if ((result = str1[1].compareToIgnoreCase(str2[1])) == 0) {
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
    private String[] _getColumn(String[][] mat,int col) {
        String[] rv = new String[mat.length];
        for (int i=0;i<mat.length;i++) {
            rv[i] = mat[i][col];
        }
        Arrays.sort(rv,String.CASE_INSENSITIVE_ORDER);
        return rv;
    }

    /**
     * Takes an array of singleton arrays and appends an empty string to each singleton, 
     * turning it into an array of pairs.
     */
    private String[][] _addEmptyColumn(String[][] vec) {
        if (vec == null) return null;
        String[][] rv = new String[vec.length][2];
        for (int i=0;i<vec.length;i++) {
            rv[i][0] = vec[i][0];
            rv[i][1] = "";
        }
        return rv;
    }
    
    /*
     * Semantic Relations
     */
    
    public String[][] getNarrowerTerms(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select N, NL " +
                "from {B} skos:narrower {N}, {N} skos:prefLabel {NL} " +
                "where B = <" + term + "> " + 
                "union select N, NL " +
                "from {N} skos:broader {B}, {N} skos:prefLabel {NL} " +
                "where B = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
        return _sortByLabel(rv);
    }
    
    public String[][] getBroaderTerms(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select B, BL " +
                "from {N} skos:broader {B}, {B} skos:prefLabel {BL} " +
                "where N = <" + term + "> " + 
                "union select B, BL " +
                "from {B} skos:narrower {N}, {B} skos:prefLabel {BL} " +
                "where N = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
        return _sortByLabel(rv);
    }
    
    public String[][] getRelatedTerms(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select R, RL " +
                "from {T} skos:related {R}, {R} skos:prefLabel {RL} " +
                "where T = <" + term + "> " + 
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
        return _sortByLabel(rv);
    }


    /*
     * Labelling
     */

    public String[] getRDFSLabels(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                 "select PL " +
                 "from {T} rdfs:label {PL} " +
                 "where T = <" + term + "> ");
        return _getColumn(rv,0);
    }

    public String[] getPreferedTerms(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select PL " +
                "from {T} skos:prefLabel {PL} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
       return _getColumn(rv,0);
    }
    
    public String[][] getTermCompletion(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select N, NL " +
                "from {N} skos:prefLabel {NL} " +
                "where NL like \""+term+"*\" IGNORE CASE " +
                "union " +
                "select N, NL " +
                "from {N} skos:altLabel {NL} " +
                "where NL like \""+term+"*\" IGNORE CASE " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");      
       return rv;
    }
    
    public String[] getAlternativeTerms(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select PL " +
                "from {T} skos:altLabel {PL} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
       return _getColumn(rv,0);
    }
    

    /*
     * Collections
     */

    public String[][] getInCollections(String term) {
        String query = "select C, CL, CT " + 
            "from {C} rdf:type {CT}, {C} skos:member {T}, {T} rdf:type {TT}, {C} rdfs:label {CL} " + 
            "where T = <" + term + "> and " +
            "( TT = skos:Collection or TT = skos:Concept ) and " + 
            "CT = <http://www.w3.org/2004/02/skos/core#Collection> " +
            "using namespace skos = <http://www.w3.org/2004/02/skos/core#>";
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",query);
        return _sortByLabel(rv);
    }

    public String[][] getCollectionMembers(String collection) {
        String query = "select T, TL, TT " + 
            "from {C} rdf:type {CT}, {C} skos:member {T}, {T} rdf:type {TT}, {T} P {TL} " + 
            "where C = <" + collection + "> and " +
            "( TT = skos:Collection or TT = skos:Concept ) and " + 
            "( P = rdfs:label or P = skos:prefLabel ) and " +
            "CT = <http://www.w3.org/2004/02/skos/core#Collection> " +
            "using namespace skos = <http://www.w3.org/2004/02/skos/core#>";
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",query);
        return _sortByLabel(rv);
    }

    /*
     * Documentation
     */

    public String[] getDefinitions(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select PL " +
                "from {T} skos:definition {PL} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
       return _getColumn(rv,0);
    }

    public String[] getNotes(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select S " +
                "from {T} Q {S} " +
                "where T = <" + term + "> and " +
                "isLiteral(S) and " +
                "( Q = <http://www.w3.org/2004/02/skos/core#scopeNote> or " +
                "Q = <http://www.w3.org/2004/02/skos/core#changeNote> or " +
                "Q = <http://www.w3.org/2004/02/skos/core#historyNote> or " +
                "Q = <http://www.w3.org/2004/02/skos/core#editorialNote> or " +
                "Q = <http://www.w3.org/2004/02/skos/core#privateNote> or " +
                "Q = <http://www.w3.org/2004/02/skos/core#publicNote> ) " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
       return _getColumn(rv,0);
    }

    public String[] getScopeNotes(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select S " +
                "from {T} skos:scopeNote {S} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
       return _getColumn(rv,0);
    }

    public String[] getChangeNotes(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select S " +
                "from {T} skos:changeNote {S} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
       return _getColumn(rv,0);
    }

    public String[] getHistoryNotes(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select S " +
                "from {T} skos:historyNote {S} " +
                "where T = >" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
       return _getColumn(rv,0);
    }

    public String[] getEditorialNotes(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select S " +
                "from {T} skos:editorialNote {S} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
       return _getColumn(rv,0);
    }

    public String[] getPublicNotes(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select S " +
                "from {T} skos:publicNote {S} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
       return _getColumn(rv,0);
    }

    public String[] getPrivateNotes(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select S " +
                "from {T} skos:privateNote {S} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
       return _getColumn(rv,0);
    }

    
    /*
     * Subject Indexing 
     */

    public String[] getSubjectsOf(String url) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select T " +
                "from {T} skos:isSubjectOf {U} " +
                "where U = <" + url + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
       return _getColumn(rv,0);
    }

    public String[] getSubjects(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select U " +
                "from {T} skos:subject {U} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
       return _getColumn(rv,0);
    }

    public String[] getPrimarySubjects(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select U " +
                "from {T} skos:primarySubject {U} " +
                "where T = <" + term + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
       return _getColumn(rv,0);
    }
    
    public String[] getPrimarySubjectsOf(String url) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select T " +
                "from {T} skos:isPrimarySubjectOf {U} " +
                "where T = <" + url + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
       return _getColumn(rv,0);
    }
    
    
    /*
     * Concept Schemes
     */
    
    public String[][] getTopConcepts(String scheme) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select T, L " +
                "from {S} skos:hasTopConcept {T}, {T} skos:prefLabel {L} " +
                "where S = <" + scheme + "> " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
if (rv == null)
  return new String[0][0];
else 
  return _sortByLabel(rv);
    }
    
    public String[][] getConceptSchemes() {
        return getConceptSchemes("");
    }

    public String[][] getConceptSchemes(String ns) {
        String query = "select S, L " +
                "from {S} rdf:type {C}, " +
                "{S} P {L} " +               
                "where C = <http://www.w3.org/2004/02/skos/core#ConceptScheme> and " +
                //"namespace(S) = <" + ns + "#> and " +
                "( P = <http://www.w3.org/2000/01/rdf-schema#label> or " +
                " P = <http://www.w3.org/2004/02/skos/core#prefLabel> )";
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",query);        
        return _sortByLabel(rv);
    }
    
    public String[][] getInSchemes(String term) {
        String query = "( select C, L " +
            "from {T} skos:inScheme {C}, {C} P {L} " +
            "where T = <" + term + "> and " +
            "( P = <http://www.w3.org/2000/01/rdf-schema#label> or " +
            " P = <http://www.w3.org/2004/02/skos/core#prefLabel> ) " + 
            ") union ( select C, L " +
            "from {C} skos:hasTopConcept {T}, {C} P {L} " + 
            "where T = <" + term + "> and " +
            "( P = <http://www.w3.org/2000/01/rdf-schema#label> or " +
            " P = <http://www.w3.org/2004/02/skos/core#prefLabel> ) ) " +                
            "using namespace skos = <http://www.w3.org/2004/02/skos/core#>";
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",query);
        return _sortByLabel(rv);
    }
    
    
    public String[][] getBroadMatches(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "( select B, BL " +
                "from {N} skosmap:broadMatch {B}, {B} skos:prefLabel {BL} " +
                "where N = <" + term + "> " + 
                " ) union " +
                "( select B, BL " +
                "from {B} skosmap:narrowMatch {N}, {B} skos:prefLabel {BL} " +
                "where N = <" + term + "> ) " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                "skosmap = <http://www.w3.org/2004/02/skos/mapping#>");
        if (rv == null || rv.length == 0) {
            rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                               "( select B " +
                               "from {A} skosmap:broadMatch {B} " +
                               "where A = <" + term + "> " +
                               " ) union " +
                               "( select B " +
                               "from {B} skosmap:narrowMatch {A} " +
                               "where A = <" + term + "> ) " +
                               "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                               "skosmap = <http://www.w3.org/2004/02/skos/mapping#>");
            return _addEmptyColumn(rv);
        } else {
            return _sortByLabel(rv);
        }
    }

    public boolean removeBroadMatch(String s,String o) {
        boolean b = true;
        b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      s,"<http://www.w3.org/2004/02/skos/mapping#broadMatch>",o);
        b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      o,"<http://www.w3.org/2004/02/skos/mapping#narrowMatch>",s);
        return b;
    }

    public String[][] getNarrowMatches(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "( select N, L " +
                "from {B} skosmap:narrowMatch {N}, {N} skos:prefLabel {L} " +
                "where B = <" + term + "> " + 
                " ) union " +
                "( select N, L " +
                "from {N} skosmap:broadMatch {B}, {N} skos:prefLabel {L} " +
                "where B = <" + term + "> ) " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                "skosmap = <http://www.w3.org/2004/02/skos/mapping#>");
        if (rv == null || rv.length == 0) {
            rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                               "( select B " +
                               "from {A} skosmap:narrowMatch {B} " +
                               "where A = <" + term + "> " +
                               " ) union " +
                               "( select B " +
                               "from {B} skosmap:broadMatch {A} " +
                               "where A = <" + term + "> ) " +
                               "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                               "skosmap = <http://www.w3.org/2004/02/skos/mapping#>");
            return _addEmptyColumn(rv);
        } else {
            return _sortByLabel(rv);
        }
    }

    public boolean removeNarrowMatch(String s,String o) {
        boolean b = true;
        b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      s,"<http://www.w3.org/2004/02/skos/mapping#narrowMatch>",o);
        b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      o,"<http://www.w3.org/2004/02/skos/mapping#broadMatch>",s);
        return b;
    }

    
    public String[][] getExactMatches(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                                      "( select B, BL " +
                                      "from {A} skosmap:exactMatch {B}, {B} skos:prefLabel {BL} " +
                                      "where A = <" + term + "> " + 
                                      " ) union " +
                                      "( select B, BL " +
                                      "from {B} skosmap:exactMatch {A}, {B} skos:prefLabel {BL} " +
                                      "where A = <" + term + "> ) " +
                                      "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                                      "skosmap = <http://www.w3.org/2004/02/skos/mapping#>");
            logger.debug("found " + rv.length + " exact matches");
        if (rv == null || rv.length == 0) {
            rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                               "( select A " +
                               "from {B} skosmap:exactMatch {A} " +
                               "where B = <" + term + "> " +
                               " ) union " +
                               "( select A " +
                               "from {A} skosmap:exactMatch {B} " +
                               "where B = <" + term + "> ) " +
                               "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                               "skosmap = <http://www.w3.org/2004/02/skos/mapping#>");
            logger.debug("found " + rv.length + " exact matches");
            return _addEmptyColumn(rv);
        } else {
            return _sortByLabel(rv);
        }
    }
    
    public boolean removeExactMatch(String s,String o) {
        boolean b = true;
        b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      s,"<http://www.w3.org/2004/02/skos/mapping#exactMatch>",o);
        b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      o,"<http://www.w3.org/2004/02/skos/mapping#exactMatch>",s);
        return b;
    }

    public String[][] getMatches(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "( select M, ML " +
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
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
        if (rv == null || rv.length == 0) {
            rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "( select M " +
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
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#>");
            return _addEmptyColumn(rv);
        } else {
            return _sortByLabel(rv);
        }
    }
    
    public String[][] getDisjointMatches(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "( select B, BL " +
                "from {A} skosmapext:disjointMatch {B}, {B} skos:prefLabel {BL} " +
                "where A = <" + term + "> " + 
                " ) union " +
                "( select B, BL " +
                "from {B} skosmapext:disjointMatch {A}, {B} skos:prefLabel {BL} " +
                "where A = <" + term + "> ) " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");
        if (rv == null || rv.length == 0) {
            rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                               "( select B " +
                               "from {A} skosmapext:disjointMatch {B} " +
                               "where A = <" + term + "> " +
                               " ) union " +
                               "( select B " +
                               "from {B} skosmapext:disjointMatch {A} " +
                               "where A = <" + term + "> ) " +
                               "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                               "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");
            return _addEmptyColumn(rv);
        } else {
            return _sortByLabel(rv);
        }
    }

    public boolean removeDisjointMatch(String s,String o) {
        boolean b = true;
        b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      s,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#disjointMatch>",o);
        b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      o,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#disjointMatch>",s);
        return b;
    }
    
    public String[][] getRelatedMatches(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "(select A, L " +
                "from {B} skosmapext:relatedMatch {A}, {A} skos:prefLabel {L} " +
                "where B = <" + term + "> " + 
                " ) union " +
                "( select A, L " +
                "from {A} skosmapext:relatedMatch {B}, {A} skos:prefLabel {L} " +
                "where B = <" + term + "> ) " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");
        if (rv == null || rv.length == 0) {
            rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                               "( select A " +
                               "from {B} skosmapext:relatedMatch {A} " +
                               "where B = <" + term + "> " +
                               " ) union " +
                               "( select A " +
                               "from {A} skosmapext:relatedMatch {B} " +
                               "where B = <" + term + "> ) " +
                               "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                               "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");
            return _addEmptyColumn(rv);
        } else {
            return _sortByLabel(rv);
        }
    }

    public boolean removeRelatedMatch(String s,String o) {
        boolean b = true;
        b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      s,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#relatedMatch>",o);
        b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      o,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#relatedMatch>",s);
        return b;
    }
        
    public String[][] getPartMatches(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "( select N, L " +
                "from {B} skosmapext:partMatch {N}, {N} skos:prefLabel {L} " +
                "where B = <" + term + "> " + 
                " ) union " +
                "( select N, L " +
                "from {N} skosmapext:wholeMatch {B}, {N} skos:prefLabel {L} " +
                "where B = <" + term + "> ) " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");
        if (rv == null || rv.length == 0) {
            rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                               "( select A " +
                               "from {B} skosmapext:partMatch {A} " +
                               "where B = <" + term + "> " +
                               " ) union " +
                               "( select A " +
                               "from {A} skosmapext:wholeMatch {B} " +
                               "where B = <" + term + "> ) " +
                               "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                               "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");
            return _addEmptyColumn(rv);
        } else {
            return _sortByLabel(rv);
        }
    }

    public boolean removePartMatch(String s,String o) {
        boolean b = true;
        b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      s,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#partMatch>",o);
        b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      o,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#wholeMatch>",s);
        return b;
    }

    public String[][] getWholeMatches(String term) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "( select N, L " +
                "from {B} skosmapext:wholeMatch {N}, {N} skos:prefLabel {L} " +
                "where B = <" + term + "> " + 
                " ) union " +
                "( select N, L " +
                "from {N} skosmapext:partMatch {B}, {N} skos:prefLabel {L} " +
                "where B = <" + term + "> ) " +
                "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");
        if (rv == null || rv.length == 0) {
            rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                               "( select A " +
                               "from {B} skosmapext:wholeMatch {A} " +
                               "where B = <" + term + "> " +
                               " ) union " +
                               "( select A " +
                               "from {A} skosmapext:partMatch {B} " +
                               "where B = <" + term + "> ) " +
                               "using namespace skos = <http://www.w3.org/2004/02/skos/core#> , " +
                               "skosmapext = <http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#>");
            return _addEmptyColumn(rv);
        } else {
            return _sortByLabel(rv);
        }
    }

    public boolean removeWholeMatch(String s,String o) {
        boolean b = true;
        b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      s,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#wholeMatch>",o);
        b = b && r.removeRdfStatement(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),
                                      o,"<http://www.few.vu.nl/~wrvhage/2006/12/skos/mappingext#partMatch>",s);
        return b;
    }

    public String[][] getTermUri(String label) {
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select C, L from {C} P {L} " +
                "where L like \"" + label + "\" ignore case and " +
                "( P = <http://www.w3.org/2004/02/skos/core#prefLabel> or " +
                " P = <http://www.w3.org/2004/02/skos/core#altLabel> )");
        return _sortByLabel(rv);
    }

    public String[][] getNumberOfNarrowerTerms(String term) {
        logger.debug("getting nrnt for <" + term + ">");
        String[][] rv = r.selectQuery(r.getServer(),r.getRepository(),r.getUsername(),r.getPassword(),"serql",
                "select C, N from {C} stat:numberOfNarrowerTerms {N} " + 
                "where C = <" + term + "> " + 
                "using namespace stat = <http://www.few.vu.nl/~wrvhage/2007/03/statistics#>");
        return rv;
    }
    
    public String[][] getNumberOfNarrowerTerms(String[] terms) {
        String [][] rv = new String[terms.length][2];
        for (int i=0;i<terms.length;i++) {
            logger.debug("loop getting nrnt for <" + terms[i] + ">");
            rv[i] = getNumberOfNarrowerTerms(terms[i])[0];
        }
        return rv;
    }
}
