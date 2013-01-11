/*
 * BQ_client.java
 *
 * Created on March 6, 2006, 11:38 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.bicad.client;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.regex.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
        
import org.vle.aid.bicad.entities.*;

/**
 *
 * @author Camille
 */
public class BQ_client {
    static Service service = null;
    // TODO make service location configurable
    static String _axisEndpoint = "http://localhost:8080/axis/services/BasicQueriesWS";

    static int locationCount = 0;
    static int annotationCount = 0;
    
    // duplicated from BasicQueries.java
    static String ns_bicad_base      = "http://www.few.vu.nl/~wrvhage/ns/bicad";
    static String ns_bicad           = ns_bicad_base + "#";
    static String bicad_generated_ns = ns_bicad_base + "/generated#";

    static String ns_skos            = "http://www.w3.org/2004/02/skos/core#";
    
    
    /** Creates a new instance of BQ_client */
    public BQ_client() {
        service = new Service();
    }
    
    /*********************************************/
    /*  local utility functions                  */ 
    /*********************************************/
   
    // generate a unique Uri by using context, time and session count
    private String newLocationUri(String context) {
        // strip the namespace from the context to save space 
        String c = context;
        Pattern p = Pattern.compile("^([^\\#]*\\#)?(.*)$");
        Matcher m = p.matcher(context);
        if (m.matches()) {
            c = m.group(2);
        }
        return bicad_generated_ns + "location_"+locationCount++ +"_"+c+"_"+(new java.util.Date().getTime());
    }
    // generate a unique Uri by using context, time and session count
    private String newAnnotationUri(String context) {
        // strip the namespace from the context to save space 
        String c = context;
        Pattern p = Pattern.compile("^([^\\#]*\\#)?(.*)$");
        Matcher m = p.matcher(context);
        if (m.matches()) {
            c = m.group(2);
        }
        return bicad_generated_ns + "annotation_"+annotationCount++ +"_"+c+"_"+(new java.util.Date().getTime());
    }
    
    
    /*********************************************/
    /*  Higher-level functions                   */ 
    /*********************************************/
    public boolean addTag(String repository_server, String repository_name,
                          String repository_user,   String repository_password, 
                          String modelUri, String contextUri, 
                          String doc, String position, String property, String concept) {
        boolean res = true;
        // create doc (tagged document)
        addDocument(repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri, bicad_generated_ns+doc, doc, null, null, doc);
        // create property (tag type)
        addProperty(repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri, property, property);

        // create concept (tag)
        addConcept(repository_server, repository_name,
                   repository_user, repository_password,
                   modelUri, contextUri,
                   concept, null, concept);
        
        addPropertyToConcept(repository_server, repository_name,
                   repository_user, repository_password,
                   modelUri, contextUri,
                   concept, property);
        
        // create location with position if not null
        String locationUri = null;
        // we can't use a blank node for addLocation, since we need an ID with addAnnotation
        locationUri = newLocationUri(contextUri);
//System.out.println("new location = "+locationUri);

        addLocation(repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri, locationUri, bicad_generated_ns+doc, position, null);
        // create annotation
        String annotUri = newAnnotationUri(contextUri);
        addAnnotation(repository_server, repository_name,
                      repository_user, repository_password, 
                      modelUri, contextUri, annotUri, locationUri, property, concept, null, null, null);

        return res;
    }
    
    
    public String[][] getTags(String repository_server, String repository_name,
                              String repository_user,   String repository_password,
                              String model, String context, String property, String concept) {
        return getTags(repository_server,repository_name,repository_user,repository_password,
                       model,context,property,concept,true,false,null);
    }
    public String[][] getTags(String repository_server, String repository_name,
                              String repository_user,   String repository_password,
                              String model, String context, String property, String concept, 
                              boolean ignoreCase, boolean matchAnywhere, Integer limit) {
    
        // tagList = { { "http://purl.org/dc/elements/1.1/creator", "http://www.piet.com#Piet" }, ... };
        String[][] tagList = null;
        // if tagPattern == null or "" get all tags
        // else fire query to find matching tags
        // make 1D array from the 2D result of selectQuery

        String lim = "";
        if (limit != null) {
            lim = " limit " + limit + " ";
        }
        String maw = "";
        if (matchAnywhere) {
            maw = "*";
        }
        String ic = "";
        if (ignoreCase) {
            ic = " ignore case ";
        }

        String query = "";
        if (property != null && concept == null) {
            query = "select distinct P, localName(P), namespace(P) from " +
                    "{ {P} rdf:type {<"+ns_bicad+"Property>} } rdf:li {B} " +
                    "where B = <" + model + "> and " +
                    "localName(P) like \"" + maw + property + "*\" " + ic + lim +
                    "union select distinct P, localName(P), namespace(P) from {P} rdf:type {rdf:Property} " +
                    "where localName(P) like \"" + maw + property + "*\" " + ic + lim;
        } else if (property == null && concept != null) {
            query = "select distinct C, L, namespace(C) from " +
                    "{ {C} rdf:type {<"+ns_bicad+"Concept>}, {C} rdfs:label {L} } rdf:li {B} " +
                    "where B = <" + model + "> and " +
                    "L like \"" + maw + concept + "*\" " + ic + lim +
                    "union select distinct C, L, namespace(C) from {C} P {L} " + 
                    "where ( P = skos:prefLabel or P = skos:altLabel or P = rdfs:label ) and " + 
                    "L like \"" + maw + concept + "*\" " + ic + lim +
                    "using namespace skos = <"+ns_skos+">";
        } else if (property != null && concept != null) {
            query = "select distinct P, C, namespace(P) from " +
                    "{ {C} <"+ns_bicad+"hasProperty> {P} } rdf:li {B} " +
                    "where B = <" + model + "> and " +
                    "P like \"" + maw + property + "*\" " + ic + "and C like \"" + maw + concept + "*\" " + ic + lim;
        }

        tagList = selectQuery(repository_server, repository_name,
                              repository_user, repository_password, 
                              model, context, query, "serql");
        return tagList;
    }
    
    public String[][] getTagLocations(String repository_server, String repository_name,
                                      String repository_user,   String repository_password, 
                                      String model, String context, String property, String concept) {
        // String[][] locationList = { { "doc0", "1 to 12" }, { "doc1", "12 to 24"} };
        String[][] locationList = null;
        // if tag == null or "" get all locations (doc + position)
        // else fire query to find locations to which an annotation points, 
        //      and that annotation hasProperty the given tag

        String c = "*";
        if (concept != null) c = concept;
        String p = "*";
        if (property != null) p = property;
        
        String query = "select distinct D, X from " +
                "{ {A} bicad:hasProperty {P2},  " +
                "  {A} bicad:hasConcept {C2}, " +
                "  {A} bicad:hasLocation {L}, " +
                "  {L} bicad:hasPosition {X}, " +
                "  {L} bicad:hasDocument {D} } rdf:li {B}, " +
                " {C2} rdfs:subClassOf {C}, {P2} rdfs:subPropertyOf {P} " +
                "where B = <" + model + "> and " +
                "P = <" + p + "> and C = <" + c + "> " +
                "union " +
                "select distinct D, X from " +
                "{ {A} bicad:hasProperty {P2},  " +
                "  {A} bicad:hasConcept {C2}, " +         
                "  {C2} rdfs:subClassOf {C}, {P2} rdfs:subPropertyOf {P}, " +
                "  {A} bicad:hasLocation {L}, " +
                "  {L} bicad:hasPosition {X}, " +
                "  {L} bicad:hasDocument {D} } rdf:li {B} " +
                "where B = <" + model + "> and " +
                "P = <" + p + "> and C = <" + c + "> " +
                "using namespace bicad = <"+ns_bicad+">";
System.out.println("getTagLocations query = "+query);

        locationList = selectQuery(repository_server, repository_name,
                                   repository_user, repository_password, 
                                   model, context, query, "serql");
        return locationList;
    }

    // returns the full set of entities (Annotation, Location, Document, Position, Property and Concept)
    //      added by each addTag.
    public String[][] getModel(String repository_server, String repository_name,
                               String repository_user,   String repository_password, 
                               String model, String context) {
        String[][] pathList = null;

        String select = "select distinct A,L,D,X,P,C from "; 
        String conditions = "{A} bicad:hasLocation {L}, " +
                       "{L} bicad:hasDocument {D}, " +
                       "{L} bicad:hasPosition {X}, " +
                       "{A} bicad:hasConcept {C}, " +
                       "{A} bicad:hasProperty {P} ";
        String query /*= select +"{ "+conditions+"} "*/;
        if (model != null && context != null)
            query = select +"{ "+conditions+"} rdf:li {Model}, { "+conditions+"} rdf:li {Context} " +
                         "where Model = <" + model + "> and Context = <"+context+"> ";
        else if (model != null)
            query = select +"{ "+conditions+"} rdf:li {Model} " +
                         "where Model = <" + model + "> ";
        else if (context != null)
            query = select +"{ "+conditions+"} rdf:li {Context} " +
                         "where Context = <" + context + "> ";
        else // both are null
            query = select +"{ "+conditions+"} rdf:li {AnyBag} ";
        query = query.concat("using namespace bicad = <"+ns_bicad+">");
System.out.println("getModel query = "+query);
        pathList = selectQuery(repository_server, repository_name,
                              repository_user, repository_password, 
                               model, null, query, "serql");
//    addSelectionRule(repository_server, repository_name,
//                     repository_user, repository_password, 
//                     model, context, 
//                     bicad_generated_ns+"getModel_SR", query, "serql", "getModel_SR");
//    pathList = executeSelectionRule(repository_server, repository_name,
//                     repository_user, repository_password, 
//                     model, context, 
//                     bicad_generated_ns+"getModel_SR");
//    removeSelectionRule(repository_server, repository_name,
//                     repository_user, repository_password, 
//                     model, context, 
//                     bicad_generated_ns+"getModel_SR");

        return pathList;
    }
    
    /***************************************************************/
    /*  Below here only functions that have a 1-on-1 BQ equivalent */ 
    /***************************************************************/
    public boolean deleteModel(String repository_server, String repository_name,
                        String repository_user,   String repository_password, 
                        String modelUri, String contextUri) {
        Boolean res = new Boolean(false);
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("deleteModel");

// System.out.println("BQ_client parameters: model="+modelUri+";context="+contextUri+";query="+query+"; qLang="+queryLanguage);            
            res = (Boolean) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri } );
         } catch (Exception e) { e.printStackTrace(); }
         return res.booleanValue();
    }
    
    
    public String[][] selectQuery(String repository_server, String repository_name,
                                  String repository_user,   String repository_password, 
                                  String modelUri, String contextUri, String query, String queryLanguage) {
        String[][] list = null;
        
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("selectQuery");

// System.out.println("BQ_client parameters: model="+modelUri+";context="+contextUri+";query="+query+"; qLang="+queryLanguage);            
            list = (String[][]) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, query, queryLanguage } );
         } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    
    /*********************************************/
    /*  functions about Annotation               */ 
    /*********************************************/
    public void addAnnotation(String repository_server, String repository_name,
                       String repository_user,   String repository_password, 
                       String modelUri, String contextUri, 
                       String annoUri, String locUri, String propUri, String conceptUri,
                       String valueLit, String valueProdLit, String dispNameLit)
    {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("addAnnotation");

            // addXXX doesn't return anything
            call.invoke( new Object[] { 
                    repository_server, repository_name, repository_user, repository_password, 
                    modelUri, contextUri, annoUri, locUri, propUri, conceptUri, valueLit, valueProdLit, dispNameLit } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    
    public Annotation getAnnotation(String repository_server, String repository_name,
                             String repository_user,   String repository_password, 
                             String modelUri, String contextUri, String annoUri)
    {
        Annotation anno = null;
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("getAnnotation");

            // register serializer and deserializer for Annotation
            QName qm = new QName( "urn:BasicQueriesWS", "Annotation" ); 
            call.registerTypeMapping(Annotation.class, qm,
                      new org.apache.axis.encoding.ser.BeanSerializerFactory(Annotation.class, qm),        
                      new org.apache.axis.encoding.ser.BeanDeserializerFactory(Annotation.class, qm));
    
            anno = (Annotation) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, annoUri } );
        } catch (Exception e) { e.printStackTrace(); }
        return anno;
    }
    public String[] getAnnotations(String repository_server, String repository_name,
                             String repository_user,   String repository_password, 
                             String modelUri, String contextUri)
    {
        String[] annos = null;
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("getAnnotations");

            annos = (String[]) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri } );
        } catch (Exception e) { e.printStackTrace(); }
        return annos;
    }
 
    public boolean removeAnnotation(String repository_server, String repository_name,
                            String repository_user,   String repository_password, 
                            String modelUri, String contextUri, String annoUri) 
    {
        Boolean res = new Boolean(false);
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("removeAnnotation");

            res = (Boolean) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, annoUri } );
         } catch (Exception e) { e.printStackTrace(); }
        return res.booleanValue();
    }

    /*********************************************/
    /*  functions about BiCadObject              */ 
    /*********************************************/
    public void addBiCadObject(String repository_server, String repository_name,
                    String repository_user,   String repository_password, 
                    String modelUri, String contextUri,
                    String objectUri, String[] annoUris, String dispNameLit)
    {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("addBiCadObject");

            // addXxx doesn't return anything
            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri,
                    objectUri, annoUris, dispNameLit } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    
    public BiCadObject getBiCadObject(String repository_server, String repository_name,
                       String repository_user,   String repository_password, 
                       String modelUri, String contextUri, 
                       String objectUri)
    {
        BiCadObject obj = null;
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("getBiCadObject");

            // register serializer and deserializer for XXX
            QName qm = new QName( "urn:BasicQueriesWS", "BiCadObject" ); 
            call.registerTypeMapping(BiCadObject.class, qm,
                      new org.apache.axis.encoding.ser.BeanSerializerFactory(BiCadObject.class, qm),        
                      new org.apache.axis.encoding.ser.BeanDeserializerFactory(BiCadObject.class, qm));
    
            obj = (BiCadObject) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, 
                        objectUri } );
        } catch (Exception e) { e.printStackTrace(); }
        return obj;
    }
 
    public boolean removeBiCadObject(String repository_server, String repository_name,
                          String repository_user,   String repository_password, 
                          String modelUri, String contextUri, String objectUri) 
    {
        Boolean res = new Boolean(false);
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("removeBiCadObject");

            res = (Boolean) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, 
                        objectUri } );
         } catch (Exception e) { e.printStackTrace(); }
        return res.booleanValue();
    }
    
    public void addAnnotationToBiCadObject(String repository_server, String repository_name,
                                     String repository_user,   String repository_password, 
                                     String modelUri, String contextUri, 
                                     String objectUri, String annoUri) {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("addAnnotationToBiCadObject");

            // addXXX doesn't return anything
            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri,
                    objectUri, annoUri } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    public void removeAnnotationFromBiCadObject(String repository_server, String repository_name,
                                     String repository_user,   String repository_password, 
                                     String modelUri, String contextUri, 
                                     String objectUri, String annoUri) {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("removeAnnotationFromBiCadObject");

            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri,
                    objectUri, annoUri } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    
    /*********************************************/
    /*  functions about Concept                  */ 
    /*********************************************/
    public void addConcept(String repository_server, String repository_name,
                    String repository_user,   String repository_password, 
                    String modelUri, String contextUri,
                    String conceptUri, String[] propUris, String dispNameLit)
    {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("addConcept");

            // addXxx doesn't return anything
            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri,
                    conceptUri, propUris, dispNameLit } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    
    public Concept getConcept(String repository_server, String repository_name,
                       String repository_user,   String repository_password, 
                       String modelUri, String contextUri, 
                       String conceptUri)
    {
        Concept concept = null;
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("getConcept");

            // register serializer and deserializer for XXX
            QName qm = new QName( "urn:BasicQueriesWS", "Concept" ); 
            call.registerTypeMapping(Concept.class, qm,
                      new org.apache.axis.encoding.ser.BeanSerializerFactory(Concept.class, qm),        
                      new org.apache.axis.encoding.ser.BeanDeserializerFactory(Concept.class, qm));
    
            concept = (Concept) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, 
                        conceptUri } );
        } catch (Exception e) { e.printStackTrace(); }
        return concept;
    }
 
    public boolean removeConcept(String repository_server, String repository_name,
                          String repository_user,   String repository_password, 
                          String modelUri, String contextUri, String conceptUri) 
    {
        Boolean res = new Boolean(false);
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("removeConcept");

            res = (Boolean) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, 
                        conceptUri } );
         } catch (Exception e) { e.printStackTrace(); }
        return res.booleanValue();
    }
    
    public void addPropertyToConcept(String repository_server, String repository_name,
                                     String repository_user,   String repository_password, 
                                     String modelUri, String contextUri, 
                                     String conceptUri, String propUri) {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("addPropertyToConcept");

            // addXXX doesn't return anything
            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri,
                    conceptUri, propUri } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    public void removePropertyFromConcept(String repository_server, String repository_name,
                                     String repository_user,   String repository_password, 
                                     String modelUri, String contextUri, 
                                     String conceptUri, String propUri) {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("removePropertyFromConcept");

            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri,
                    conceptUri, propUri } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }


    /*********************************************/
    /*  functions about ConceptRelation          */ 
    /*********************************************/
    public void addConceptRelation(String repository_server, String repository_name,
                    String repository_user,   String repository_password, 
                    String modelUri, String contextUri,
                    String conRelUri, String subjUri, String relTypeUri, String dispNameLit, String[] objUris)
    {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("addConceptRelation");

            // addXxx doesn't return anything
            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri,
                    conRelUri, subjUri, relTypeUri, dispNameLit, objUris } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    
    public ConceptRelation getConceptRelation(String repository_server, String repository_name,
                       String repository_user,   String repository_password, 
                       String modelUri, String contextUri, 
                       String conRelUri)
    {
        ConceptRelation conrel = null;
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("getConceptRelation");

            // register serializer and deserializer for XXX
            QName qm = new QName( "urn:BasicQueriesWS", "ConceptRelation" ); 
            call.registerTypeMapping(ConceptRelation.class, qm,
                      new org.apache.axis.encoding.ser.BeanSerializerFactory(ConceptRelation.class, qm),        
                      new org.apache.axis.encoding.ser.BeanDeserializerFactory(ConceptRelation.class, qm));
    
            conrel = (ConceptRelation) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, 
                        conRelUri } );
        } catch (Exception e) { e.printStackTrace(); }
        return conrel;
    }
 
    public boolean removeConceptRelation(String repository_server, String repository_name,
                          String repository_user,   String repository_password, 
                          String modelUri, String contextUri, 
                          String conRelUri) 
    {
        Boolean res = new Boolean(false);
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("removeConceptRelation");

            res = (Boolean) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, 
                        conRelUri } );
         } catch (Exception e) { e.printStackTrace(); }
        return res.booleanValue();
    }
    
    public void addObjectToConceptRelation(String repository_server, String repository_name,
                                     String repository_user,   String repository_password, 
                                     String modelUri, String contextUri, 
                                     String conRelUri, String objUri) {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("addObjectToConceptRelation");

            // addXXX doesn't return anything
            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri,
                    conRelUri, objUri } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    public void removeObjectFromConceptRelation(String repository_server, String repository_name,
                                     String repository_user,   String repository_password, 
                                     String modelUri, String contextUri, 
                                     String conRelUri, String objUri) {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("removeObjectFromConceptRelation");

            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri,
                    conRelUri, objUri } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    
    
    /*********************************************/
    /*  functions about Document                 */ 
    /*********************************************/
    public void addDocument(String repository_server, String repository_name,
                     String repository_user,   String repository_password, 
                     String modelUri, String contextUri,
                     String documentUri, String origDocUri, String dispDocUri, 
                     String textDocUri, String dispNameLit) {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("addDocument");

            // addXXX doesn't return anything
            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri, documentUri, origDocUri, dispDocUri, textDocUri, dispNameLit
                    } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    
    public Document getDocument(String repository_server, String repository_name,
                         String repository_user,   String repository_password, 
                         String modelUri, String contextUri, String docUri)
    {
        Document doc = null;
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("getDocument");

            // register serializer and deserializer for XXX
            QName qm = new QName( "urn:BasicQueriesWS", "Document" ); 
            call.registerTypeMapping(Document.class, qm,
                      new org.apache.axis.encoding.ser.BeanSerializerFactory(Document.class, qm),        
                      new org.apache.axis.encoding.ser.BeanDeserializerFactory(Document.class, qm));
    
            doc = (Document) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, docUri } );
        } catch (Exception e) { e.printStackTrace(); }
        return doc;
    }
 
    public boolean removeDocument(String repository_server, String repository_name,
                           String repository_user,   String repository_password, 
                           String modelUri, String contextUri, String docUri) 
    {
        Boolean res = new Boolean(false);
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("removeDocument");

            res = (Boolean) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, docUri } );
         } catch (Exception e) { e.printStackTrace(); }
        return res.booleanValue();
    }
    
    
    /*********************************************/
    /*  functions about Location                 */ 
    /*********************************************/
    public void addLocation(String repository_server, String repository_name,
                     String repository_user,   String repository_password, 
                     String modelUri, String contextUri,
                     String locationUri, String documentUri, 
                     String posLit, String dispNameLit)
    {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("addLocation");

            // addXXX doesn't return anything
            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri, locationUri, documentUri, posLit, dispNameLit } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    
    public Location getLocation(String repository_server, String repository_name,
                         String repository_user,   String repository_password, 
                         String modelUri, String contextUri, String locationUri)
    {
        Location loc = null;
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("getLocation");

            // register serializer and deserializer for XXX
            QName qm = new QName( "urn:BasicQueriesWS", "Location" ); 
            call.registerTypeMapping(Location.class, qm,
                      new org.apache.axis.encoding.ser.BeanSerializerFactory(Location.class, qm),        
                      new org.apache.axis.encoding.ser.BeanDeserializerFactory(Location.class, qm));
    
            loc = (Location) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, locationUri } );
        } catch (Exception e) { e.printStackTrace(); }
        return loc;
    }
 
    public boolean removeLocation(String repository_server, String repository_name,
                            String repository_user,   String repository_password, 
                            String modelUri, String contextUri, String locationUri) 
    {
        Boolean res = new Boolean(false);
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("removeLocation");

            res = (Boolean) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, locationUri } );
         } catch (Exception e) { e.printStackTrace(); }
        return res.booleanValue();
    }
    
    
    /*********************************************/
    /*  functions about ObjectRelation           */ 
    /*********************************************/
    public void addObjectRelation(String repository_server, String repository_name,
                    String repository_user,   String repository_password, 
                    String modelUri, String contextUri,
                    String objRelUri, String subjUri, String[] objUris, String relTypeUri, String dispNameLit)
    {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("addObjectRelation");

            // addXxx doesn't return anything
            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri,
                    objRelUri, subjUri, objUris, relTypeUri, dispNameLit } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    
    public ObjectRelation getObjectRelation(String repository_server, String repository_name,
                       String repository_user,   String repository_password, 
                       String modelUri, String contextUri, 
                       String objRelUri)
    {
        ObjectRelation objrel = null;
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("getObjectRelation");

            // register serializer and deserializer for XXX
            QName qm = new QName( "urn:BasicQueriesWS", "ObjectRelation" ); 
            call.registerTypeMapping(ObjectRelation.class, qm,
                      new org.apache.axis.encoding.ser.BeanSerializerFactory(ObjectRelation.class, qm),        
                      new org.apache.axis.encoding.ser.BeanDeserializerFactory(ObjectRelation.class, qm));
    
            objrel = (ObjectRelation) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, 
                        objRelUri } );
if (objrel != null)            
  System.out.println("BQ_client: getObjectRelation("+objRelUri+").subject = "+objrel.getSubjectUri());        
        } catch (Exception e) { System.out.println("BQ_client error calling getObjectRelation"); e.printStackTrace(); }
        return objrel;
    }
 
    public boolean removeObjectRelation(String repository_server, String repository_name,
                          String repository_user,   String repository_password, 
                          String modelUri, String contextUri, String objRelUri) 
    {
        Boolean res = new Boolean(false);
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("removeObjectRelation");

            res = (Boolean) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, 
                        objRelUri } );
         } catch (Exception e) { e.printStackTrace(); }
        return res.booleanValue();
    }
    
    public void addObjectToObjectRelation(String repository_server, String repository_name,
                                     String repository_user,   String repository_password, 
                                     String modelUri, String contextUri, 
                                     String objRelUri, String objUri) {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("addObjectToObjectRelation");

            // addXXX doesn't return anything
            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri,
                    objRelUri, objUri } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    public void removeObjectFromObjectRelation(String repository_server, String repository_name,
                                     String repository_user,   String repository_password, 
                                     String modelUri, String contextUri, 
                                     String objRelUri, String objUri) {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("removeObjectFromObjectRelation");

            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri,
                    objRelUri, objUri } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    
    
    /*********************************************/
    /*  functions about Property                 */ 
    /*********************************************/
    public void addProperty(String repository_server, String repository_name,
                     String repository_user,   String repository_password, 
                     String modelUri, String contextUri,
                     String propUri, String dispNameLit)
    {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("addProperty");

            // addXXX doesn't return anything
            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri, propUri, dispNameLit } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    
    public Property getProperty(String repository_server, String repository_name,
                         String repository_user,   String repository_password, 
                         String modelUri, String contextUri, String propUri)
    {
        Property prop = null;
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("getProperty");

            // register serializer and deserializer for XXX
            QName qm = new QName( "urn:BasicQueriesWS", "Property" ); 
            call.registerTypeMapping(Property.class, qm,
                      new org.apache.axis.encoding.ser.BeanSerializerFactory(Property.class, qm),        
                      new org.apache.axis.encoding.ser.BeanDeserializerFactory(Property.class, qm));
    
            prop = (Property) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, propUri } );
        } catch (Exception e) { e.printStackTrace(); }
        return prop;
    }
 
    public boolean removeProperty(String repository_server, String repository_name,
                           String repository_user,   String repository_password, 
                           String modelUri, String contextUri, String propUri) 
    {
        Boolean res = new Boolean(false);
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("removeProperty");

            res = (Boolean) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, propUri } );
         } catch (Exception e) { e.printStackTrace(); }
        return res.booleanValue();
    }
    
    
    /*********************************************/
    /*  functions about SelectionRule            */ 
    /*********************************************/
    public void addSelectionRule(String repository_server, String repository_name,
                     String repository_user,   String repository_password, 
                     String modelUri, String contextUri,
                     String ruleUri, String queryLit, String queryLangLit, String dispNameLit) 
    {
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("addSelectionRule");

            // addXXX doesn't return anything
            call.invoke( new Object[] { 
                    repository_server, repository_name,
                    repository_user, repository_password, 
                    modelUri, contextUri, 
                    ruleUri, queryLit, queryLangLit, dispNameLit } );
             
         } catch (Exception e) { e.printStackTrace(); }
    }
    
    public SelectionRule getSelectionRule(String repository_server, String repository_name,
                         String repository_user, String repository_password, 
                         String modelUri, String contextUri, 
                         String ruleUri)
    {
        SelectionRule rule = null;
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("getSelectionRule");

            // register serializer and deserializer for XXX
            QName qm = new QName( "urn:BasicQueriesWS", "SelectionRule" ); 
            call.registerTypeMapping(SelectionRule.class, qm,
                      new org.apache.axis.encoding.ser.BeanSerializerFactory(SelectionRule.class, qm),        
                      new org.apache.axis.encoding.ser.BeanDeserializerFactory(SelectionRule.class, qm));
    
            rule = (SelectionRule) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, ruleUri } );
        } catch (Exception e) { e.printStackTrace(); }
        return rule;
    }
 
    public boolean removeSelectionRule(String repository_server, String repository_name,
                           String repository_user, String repository_password, 
                           String modelUri, String contextUri, 
                           String ruleUri) 
    {
        Boolean res = new Boolean(false);
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("removeSelectionRule");

            res = (Boolean) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, ruleUri } );
        } catch (Exception e) { e.printStackTrace(); }
        return res.booleanValue();
    }
     
    public String[][] executeSelectionRule(String repository_server, String repository_name,
                           String repository_user, String repository_password, 
                           String modelUri, String contextUri, 
                           String ruleUri) 
    {
        String[][] res = null;
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(_axisEndpoint);
            call.setOperationName("executeSelectionRule");

            res = (String[][]) call.invoke( new Object[] { 
                        repository_server, repository_name,
                        repository_user, repository_password, 
                        modelUri, contextUri, ruleUri } );
        } catch (Exception e) { e.printStackTrace(); }
        return res;
    }
    
//    public ORel test_getORel(String repository_server, String repository_name,
//                         String repository_user, String repository_password, 
//                         String modelUri, String contextUri)
//    {
//        ORel or = null;
//        try {
//            Call call = (Call) service.createCall();
//            call.setTargetEndpointAddress(_axisEndpoint);
//            call.setOperationName("test_getORel");
//
//            // register serializer and deserializer for XXX
//            QName qm = new QName( "urn:BasicQueriesWS", "ORel" ); 
//            call.registerTypeMapping(ORel.class, qm,
//                      new org.apache.axis.encoding.ser.BeanSerializerFactory(ORel.class, qm),        
//                      new org.apache.axis.encoding.ser.BeanDeserializerFactory(ORel.class, qm));
//    
//            or = (ORel) call.invoke( new Object[] { 
//                        repository_server, repository_name,
//                        repository_user, repository_password, 
//                        modelUri, contextUri} );
//        } catch (Exception e) { e.printStackTrace(); }
//        return or;
//    }
//    public ObjectRelation test_getObjectRelation(String repository_server, String repository_name,
//                         String repository_user, String repository_password, 
//                         String modelUri, String contextUri)
//    {
//        ObjectRelation or = null;
//        try {
//            Call call = (Call) service.createCall();
//            call.setTargetEndpointAddress(_axisEndpoint);
//            call.setOperationName("test_getObjectRelation");
//
//            // register serializer and deserializer for XXX
//            QName qm = new QName( "urn:BasicQueriesWS", "ObjectRelation" ); 
//            call.registerTypeMapping(ObjectRelation.class, qm,
//                      new org.apache.axis.encoding.ser.BeanSerializerFactory(ObjectRelation.class, qm),        
//                      new org.apache.axis.encoding.ser.BeanDeserializerFactory(ObjectRelation.class, qm));
//    
//            or = (ObjectRelation) call.invoke( new Object[] { 
//                        repository_server, repository_name,
//                        repository_user, repository_password, 
//                        modelUri, contextUri} );
//        } catch (Exception e) { e.printStackTrace(); }
//        return or;
//    }
 
}
