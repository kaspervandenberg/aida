/*
 * Concept.java
 *
 * Created on February 7, 2006, 9:35 AM
 */

package org.vle.aid.bicad.entities;

/**
 *
 * @author  Camille
 */
public class Concept {
    private String   _conceptUri;
    private String[] _propertyUris;
    private String   _displayNameLit;
    
    /** Creates an empty new instance of Concept */
    public Concept() {
//        _conceptUri      = "";
//        _propertyUris[0] = "";
//        _displayNameLit  = "";
    }
    public Concept(String conceptUri, String[] propUris, String dispNameLit) {
	_conceptUri      = conceptUri;
        _propertyUris    = propUris;
        _displayNameLit  = dispNameLit;
    }
    
    public String getConceptUri() { return _conceptUri; }
    public void setConceptUri(String conceptUri) { _conceptUri = conceptUri; }
    
    public String[] getPropertyUris() { return _propertyUris; }
    public void setPropertyUris(String[] propUris) { _propertyUris = propUris; }
    
    public String getDisplayNameLit() { return _displayNameLit; }
    public void setDisplayNameLit(String dispNameLit) { _displayNameLit = dispNameLit; }
}
