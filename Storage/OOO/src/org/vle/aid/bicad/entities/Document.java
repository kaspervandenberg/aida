/*
 * Annotation.java
 *
 * Created on February 7, 2006, 9:35 AM
 */

package org.vle.aid.bicad.entities;

/**
 *
 * @author  Camille
 */
public class Document {
    private String _documentUri;
    private String _origDocLit;     // wil generally be a URI, but not handled as such by BQ
    private String _displayDocLit;  // optional    
    private String _textDocLit;     // optional
    private String _displayNameLit;
    
    /** Creates an empty new instance of Document */
    public Document() {
//        _documentUri     = "";
//        _origDocLit     = "";
//        _displayDocLit  = "";
//        _textDocLit     = "";
//        _displayNameLit = "";
    }
    /** Creates a new instance of Document filled with the parameters */
    public Document(String docUri, String origDocLit, String displayDocLit, String textDocLit, String dispName) {
        _documentUri     = docUri;
        _origDocLit     = origDocLit;
        _displayDocLit  = displayDocLit;
        _textDocLit     = textDocLit;
        _displayNameLit = dispName;
    }
    
    /**
     * gets the Uri (identifier) of the document.
     */    
    public String getDocumentUri() { return _documentUri; }
    /**
     * sets the Uri (identifier) of the document.
     */    
    public void setDocumentUri(String docUri) { _documentUri = docUri; }
    
    /**
     * gets the Location where the original document can be found.
     */    
    public String getOrigDocLit() { return _origDocLit; }
    /**
     * sets the Location where the original document can be found.
     */    
    public void setOrigDocLit(String origDocLit) { _origDocLit = origDocLit; }
    
    /**
     * gets the Location where the version document can be found that can be displayed by the application.
     */    
    public String getDisplayDocLit() { return _displayDocLit; }
    /**
     * sets the Location where the version document can be found that can be displayed by the application.
     */    
    public void setDisplayDocLit(String displayDocLit) { _displayDocLit = displayDocLit; }
    
    /**
     * gets the Location where the version document can be found that is text-only (e.g. for indexing).
     */    
    public String getTextDocLit() { return _textDocLit; }
    /**
     * sets the Location where the version document can be found that is text-only (e.g. for indexing).
     */    
    public void setTextDocLit(String textDocLit) { _textDocLit = textDocLit; }
    
    /**
     * gets the name of the document as it is displayed in the application
     */    
    public String getDisplayNameLit() { return _displayNameLit; }
    /**
     * sets the name of the document as it is displayed in the application
     */    
    public void setDisplayNameLit(String dispName) { _displayNameLit = dispName; }
}
