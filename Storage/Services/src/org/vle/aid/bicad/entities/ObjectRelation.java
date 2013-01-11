/*
 * ObjectRelation.java
 *
 * Created on February 7, 2006, 9:35 AM
 */

package org.vle.aid.bicad.entities;

/**
 *
 * @author  Camille
 */
public class ObjectRelation {
    private String   objectRelationUri;
    private String   subjectUri;
    private String[] objectUris;
    private String   relationTypeUri;
    private String   displayNameLit;
    
    /** Creates an empty new instance of ObjectRelation */
    public ObjectRelation() {
//        objectRelationUri = "";
//        subjectUri        = "";
//        objectUris[0]     = "";
//        relationTypeUri   = "";
//        displayNameLit    = "";
    }
    public ObjectRelation(String objRelUri, String subjUri, String[] objUris, String relUri, String dispNameLit) {
        objectRelationUri = objRelUri;
        subjectUri        = subjUri;
        objectUris        = objUris;
        relationTypeUri   = relUri;
        displayNameLit    = dispNameLit;
    }

    public String getDisplayNameLit() {
        return displayNameLit;
    }

    public String[] getObjectUris() {
        return objectUris;
    }

    public String getObjectRelationUri() {
        return objectRelationUri;
    }

    public String getRelationTypeUri() {
        return relationTypeUri;
    }

    public String getSubjectUri() {
        return subjectUri;
    }

    public void setDisplayNameLit(String displayNameLit) {
        this.displayNameLit = displayNameLit;
    }

    public void setObjectUris(String[] objectUris) {
        this.objectUris = objectUris;
    }

    public void setObjectRelationUri(String objectRelationUri) {
        this.objectRelationUri = objectRelationUri;
    }

    public void setRelationTypeUri(String relationTypeUri) {
        this.relationTypeUri = relationTypeUri;
    }

    public void setSubjectUri(String subjectUri) {
        this.subjectUri = subjectUri;
    }
    

}
