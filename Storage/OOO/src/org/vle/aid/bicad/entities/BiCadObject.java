/*
 * BiCadObject.java
 *
 * Created on February 7, 2006, 9:35 AM
 */

package org.vle.aid.bicad.entities;

/**
 *
 * @author  Camille
 */
public class BiCadObject {
    private String   _biCadObjectUri;
    private String[] _annotationUris;
    private String   _displayNameLit;
    
    /** Creates an empty new instance of BiCadObject */
    public BiCadObject() {
//        _biCadObjectUri    = "";
//        _annotationUris[0] = "";
//        _displayNameLit    = "";
    }
    public BiCadObject(String objectUri, String[] annUris, String dispName) {
        _biCadObjectUri = objectUri;
        _annotationUris = annUris;
        _displayNameLit = dispName;
    }
    
    public String getBiCadObjectUri() { return _biCadObjectUri; }
    public void setBiCadObjectUri(String objectUri) { _biCadObjectUri = objectUri; }

    public String[] getAnnotationUris() { return _annotationUris; }
    public void setAnnotationUris(String[] annUris) { _annotationUris = annUris; }

    public String getDisplayNameLit() { return _displayNameLit; }
    public void setDisplayNameLit(String dispName) { _displayNameLit = dispName; }
}
