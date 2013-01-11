/*
 * ConceptRelation.java
 *
 * Created on April 25, 2006, 11:46 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.bicad.entities;

/**
 *
 * @author Camille
 */
public class ConceptRelation {
    private String conceptRelationUri;
    private String subjectUri;
    private String[] objectUris;
    private String relationTypeUri;
    private String displayNameLit;
    
    
    /** Creates a new instance of ConceptRelation */
    public ConceptRelation() {
//        conceptRelationUri = "";
//        subjectUri         = "";
//        objectUris[0]      = "";
//        relationTypeUri    = "";
//        displayNameLit     = "";
    }
    public ConceptRelation(String conRelUri, String subjUri, String[] ObjUris, String relTypeUri, String dispNameLit) {
        conceptRelationUri = conRelUri;
        subjectUri         = subjUri;
        objectUris         = ObjUris;
        relationTypeUri    = relTypeUri;
        displayNameLit     = dispNameLit;
    }

    public String getConceptRelationUri() {
        return conceptRelationUri;
    }
    public void setConceptRelationUri(String conceptRelationUri) {
        this.conceptRelationUri = conceptRelationUri;
    }

    public String getSubjectUri() {
        return subjectUri;
    }
    public void setSubjectUri(String subjectUri) {
        this.subjectUri = subjectUri;
    }

    public String[] getObjectUris() {
        return objectUris;
    }
    public void setObjectUris(String[] objectUris) {
        this.objectUris = objectUris;
    }

    public String getRelationTypeUri() {
        return relationTypeUri;
    }
    public void setRelationTypeUri(String relationTypeUri) {
        this.relationTypeUri = relationTypeUri;
    }

    public String getDisplayNameLit() {
        return displayNameLit;
    }
    public void setDisplayNameLit(String displayNameLit) {
        this.displayNameLit = displayNameLit;
    }

}
