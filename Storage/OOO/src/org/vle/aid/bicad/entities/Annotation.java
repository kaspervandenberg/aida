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
public class Annotation {
    private String annotationUri;
    private String locationUri;
    private String propertyUri;
    private String conceptUri;
    private String valueLit;
    private String valueProducerLit;    // will be a URI, but not handled as such in BasicQueries
    private String displayNameLit;
    
    /** Creates an empty new instance of Annotation */
    public Annotation() {
//        annotationUri   = "";
//        locationUri     = "";
//        propertyUri     = "";
//        conceptUri      = "";
//        valueLit       = "";
//        valueProducerLit = "";
//        displayNameLit = "";
    }
    public Annotation(String annUri, String locUri, String propUri, String conceptUri,
                      String val, String valueProd, String dispName) {
        annotationUri   = annUri;
        locationUri     = locUri;
        propertyUri     = propUri;
        conceptUri      = conceptUri;
        valueLit       = val;
        valueProducerLit = valueProd;
        displayNameLit = dispName;
    }
    
    public String getAnnotationUri() { return annotationUri; }
    public void setAnnotationUri(String annUri) { annotationUri = annUri; }
    
    public String getLocationUri() { return locationUri; }
    public void setLocationUri(String locUri) { locationUri = locUri; }
    
    public String getPropertyUri() { return propertyUri; }
    public void setPropertyUri(String propUri) { propertyUri = propUri; }
    
    public String getConceptUri() { return conceptUri; }
    public void setConceptUri(String conUri) { conceptUri = conUri; }

    public String getValueLit() { return valueLit; }
    public void setValueLit(String val) { valueLit = val; }

    public String getValueProducerLit() { return valueProducerLit; }
    public void setValueProducerLit(String valueProd) { this.valueProducerLit = valueProd; }


    public String getDisplayNameLit() { return displayNameLit; }
    public void setDisplayNameLit(String dispName) { displayNameLit = dispName; }
}
