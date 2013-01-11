/*
 * SelectionRule.java
 *
 * Created on February 7, 2006, 9:35 AM
 */

package org.vle.aid.bicad.entities;

/**
 *
 * @author  Camille
 */
public class SelectionRule {
    private String _selectionRuleUri;
    private String _queryLit;
    private String _queryLang;
    private String _displayNameLit;
    
    /** Creates an empty new instance of SelectionRule */
    public SelectionRule() {
//        _selectionRuleUri = "";
//        _queryLang       = "";
//        _queryLit        = "";
//        _displayNameLit  = "";
    }
    public SelectionRule(String selRuleUri, String queryLit, String queryLang, String dispName) {
        _selectionRuleUri = selRuleUri;
        _queryLit         = queryLit;
        _queryLang        = queryLang;
        _displayNameLit   = dispName;
    }
    
    public String getSelectionRuleUri() { return _selectionRuleUri; }
    public void setSelectionRuleUri(String selRuleUri) { _selectionRuleUri = selRuleUri; }
    
    public String getQueryLit() { return _queryLit; }
    public void setQueryLit(String queryLit) { _queryLit = queryLit; }
    
    public String getDisplayNameLit() { return _displayNameLit; }
    public void setDisplayNameLit(String dispName) { _displayNameLit = dispName; }
    
    public String getQueryLanguageLit() { return _queryLang; }
    public void setQueryLanguageLit(String queryLang) { _queryLang = queryLang; }
}
