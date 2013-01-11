/*
 * Location.java
 *
 * Created on February 2, 2006, 9:35 AM
 */

package org.vle.aid.bicad.entities;

/**
 *
 * @author  Camille
 */
public class Location {
    private String _locationUri;
    private String _documentUri;
    private String _positionLit;
    private String _displayNameLit;
    
    /** Creates an empty new instance of Location */
    public Location() {
//        _locationUri     = "";
//        _documentUri     = "";
//        _positionLit    = "";
//        _displayNameLit = "";
    }
    public Location(String locUri, String docUri, 
                    String positionLit, String dispName) {
        _locationUri     = locUri;
        _documentUri     = docUri;
        _positionLit    = positionLit;
        _displayNameLit = dispName;
    }
    
    public String getLocationUri() { return _locationUri; }
    public void setLocationUri(String locUri) { _locationUri = locUri; }
    
    public String getDocumentUri() { return _documentUri; }
    public void setDocumentUri(String docUri) { _documentUri = docUri; }
    
    public String getPositionLit() { return _positionLit; }
    public void setPositionLit(String pos) { _positionLit = pos; }
    
    public String getDisplayNameLit() { return _displayNameLit; }
    public void setDisplayNameLit(String dispName) { _displayNameLit = dispName; }
}
