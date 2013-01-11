/*
 * ThesaurusRepositoryWS.java
 *
 * Created on March 7, 2006, 10:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.metadata.ws;

import org.vle.aid.metadata.ThesaurusRepository;

/**
 *
 * @author wrvhage
 */
public class ThesaurusRepositoryWS {
            
    /*
     * Semantic Relations
     */

    /**
     * Find narrower Concepts and their prefLabel.
     * @param server_url the URL of the meta-data server that will be queried (e.g. http://www.host.org:8080/sesame)
     * @param repository the name of the repository or model in the meta-data server (e.g. mem-rdfs-db)
     * @param username the username of the user to access the repository as used by the meta-data server (e.g. testuser)
     * @param password the password of the user to access the repository as used by the meta-data server (e.g. opensesame)
     * @param term  the URI of the "broader" skos:Concept, the "focus" concept.
     * @return an array of pairs of skos:Concepts and their skos:prefLabel
     */
    public String[][] getNarrowerTerms(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getNarrowerTerms(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    public String[][] getBroaderTerms(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getBroaderTerms(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String[][] getRelatedTerms(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getRelatedTerms(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /*
     * Labelling
     */

    /**
     * Find rdfs:label values for a Class, e.g. a skos:Collection or skos:ConceptScheme.
     * @return an array of rdfs:labels, literal values
     */
    public String[] getRDFSLabels(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getRDFSLabels(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    public String[] getPreferedTerms(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getPreferedTerms(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    public String[] getAlternativeTerms(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getAlternativeTerms(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    
    /*
     * Documentation
     */

    public String[] getDefinitions(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getDefinitions(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String[] getNotes(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getNotes(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String[] getScopeNotes(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getScopeNotes(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String[] getChangeNotes(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getChangeNotes(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String[] getHistoryNotes(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getHistoryNotes(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String[] getEditorialNotes(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getEditorialNotes(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String[] getPublicNotes(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getPublicNotes(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;

    }

    public String[] getPrivateNotes(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getPrivateNotes(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    
    /*
     * Subject Indexing 
     */

    /**
     * Get skos:Concepts that are used to annotate the resource identified by "url".
     * @param url the URI of a resource that is annotated with skos:Concepts using skos:isSubjectOf
     * @return an array of URI's of skos:Concepts
     */
    public String[] getSubjectsOf(String server_url,String repository,String username,String password,String url) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getSubjectsOf(url);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Get URI's of resources that have been annotated with skos:Concepts "term".
     * @param term the URI of a skos:Concept that is used to annotate resources using skos:subject
     * @return an array of URI's of resources that have been annotated with skos:Concept "term"
     */
    public String[] getSubjects(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getSubjects(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String[] getPrimarySubjects(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getPrimarySubjects(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    public String[] getPrimarySubjectsOf(String server_url,String repository,String username,String password,String url) {
         try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getPrimarySubjectsOf(url);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    
    /*
     * Concept Schemes
     */
    
    public String[][] getTopConcepts(String server_url,String repository,String username,String password,String scheme_label) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getTopConcepts(scheme_label);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    /**
     * Return all URI's of skos:ConceptSchemes in namespace "namespace".
     * @param namespace the URI of the namespace
     * @return array of pairs of URI's of skos:ConceptSchemes and their rdfs:label
     */
    public String[][] getConceptSchemesWithNamespace(String server_url,String repository,String username,String password,String namespace) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getConceptSchemes(namespace);
        } catch (Exception e) { e.printStackTrace(); }
        return null;        
    }

    public String[][] getConceptSchemes(String server_url,String repository,String username,String password) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getConceptSchemes();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    public String[][] getInSchemes(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getInSchemes(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String[][] getInCollections(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getInCollections(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String[][] getCollectionMembers(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getCollectionMembers(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    /*
     * SKOS Mapping
     */
    public String[][] getMatches(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getMatches(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String[][] getExactMatches(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getExactMatches(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    public boolean removeExactMatch(String server_url,String repository,String username,String password,String subject,String object) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.removeExactMatch(subject,object);
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public String[][] getDisjointMatches(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getDisjointMatches(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean removeDisjointMatch(String server_url,String repository,String username,String password,String subject,String object) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.removeDisjointMatch(subject,object);
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public String[][] getRelatedMatches(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getRelatedMatches(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean removeRelatedMatch(String server_url,String repository,String username,String password,String subject,String object) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.removeRelatedMatch(subject,object);
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
    
    public String[][] getNarrowMatches(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getNarrowMatches(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean removeNarrowMatch(String server_url,String repository,String username,String password,String subject,String object) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.removeNarrowMatch(subject,object);
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
    
    public String[][] getBroadMatches(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getBroadMatches(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean removeBroadMatch(String server_url,String repository,String username,String password,String subject,String object) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.removeBroadMatch(subject,object);
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public String[][] getPartMatches(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getPartMatches(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    public String[][] getTermCompletion(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getTermCompletion(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean removePartMatch(String server_url,String repository,String username,String password,String subject,String object) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.removePartMatch(subject,object);
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public String[][] getWholeMatches(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getWholeMatches(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    public boolean removeWholeMatch(String server_url,String repository,String username,String password,String subject,String object) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.removeWholeMatch(subject,object);
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Gets the URI of all skos:Concepts with a skos:prefLabel or skos:altLabel that matches "term".
     * @param term a literal string that describes a concept. (e.g. "Spline Reticulation")
     * @return an array of pairs of skos:Concepts and their skos:prefLabel or skos:altLabel (that matched "term")
     */
    public String[][] getTermUri(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getTermUri(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String[][] getNumberOfNarrowerTerms(String server_url,String repository,String username,String password,String term) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getNumberOfNarrowerTerms(term);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String[][] getNumberOfNarrowerTermsMulti(String server_url,String repository,String username,String password,String terms[]) {
        try {
            ThesaurusRepository tr = new ThesaurusRepository(server_url,repository,username,password);
            return tr.getNumberOfNarrowerTerms(terms);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

}
