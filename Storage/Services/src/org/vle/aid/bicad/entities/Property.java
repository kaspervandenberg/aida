/*
 * Property.java
 *
 * Created on February 7, 2006, 9:35 AM
 */

package org.vle.aid.bicad.entities;

/**
 *
 * @author  Camille
 */
public class Property {
    private String _propertyUri;
    private String _displayNameLit;
    
    /** Creates an empty new instance of Property */
    public Property() {
//        _propertyUri     = "";
//        _displayNameLit = "";
    }
    public Property(String propUri, String dispName) {
        _propertyUri     = propUri;
        _displayNameLit = dispName;
    }
    
    public String getPropertyUri() { return _propertyUri; }
    public void setPropertyUri(String propUri) { _propertyUri = propUri; }
    
    public String getDisplayNameLit() { return _displayNameLit; }
    public void setDisplayNameLit(String dispName) { _displayNameLit = dispName; }
}
