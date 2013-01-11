/*
 * BasicQueries.java
 *
 * Created on 30 januari 2006, 14:44
 */

/* TODO:
 *  - ? Annotation: execute ValueProducer (Discussion with Hap)
 *  - ? addStatement: remove statement if there already is a statement with that subject and predicate
 s*/

package org.vle.aid.bicad;

import org.vle.aid.bicad.entities.*;
import org.vle.aid.metadata.*;

/**
 *
 * @author  CF vd Berg
 */
public class BasicQueries {
    // namespaces
    static String ns_bicad_base        = "http://www.few.vu.nl/~wrvhage/ns/bicad";
    static String ns_bicad             = ns_bicad_base + "#";
        // other's namespaces
    static String ns_dc     = "http://purl.org/dc/elements/1.1/";
    static String ns_rdf    = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    static String ns_rdfs   = "http://www.w3.org/2000/01/rdf-schema#";
    
    private Repository _rep = null;
    private static String _repository_server   = ""; // the default server (localhost:8080/sesame)
    private static String _repository_name     = "mem-rdfs-db"; // the default Main Memory RDFS repository
    private static String _repository_user     = "testuser";   // testuser has write access on the available repositories   
    private static String _repository_password = "opensesame";
    public  static String queryLanguageSERQL   = "serql";
    public  static String queryLanguageSPARQL  = "sparql";
    private static String _queryLanguage       = queryLanguageSERQL;    // default language
    private String _modelUri   = ns_bicad+"defaultModel";
    private String _contextUri = ns_bicad+"defaultContext";

    // TODO: think if these should be stored per entity in stead of here all together
    // predicates
    static String pred_hasAnnotation    = "hasAnnotation";
    static String pred_hasDisplayDoc    = "hasDisplayDoc";
    static String pred_hasDocument      = "hasDocument";
    static String pred_hasLocation      = "hasLocation";
    static String pred_hasConcept       = "hasConcept";
    static String pred_hasObject        = "hasObject";
    static String pred_hasOriginalDoc   = "hasOriginalDoc";
    static String pred_hasPosition      = "hasPosition";
    static String pred_hasPredicate     = "hasPredicate";
    static String pred_hasProperty      = "hasProperty";
    static String pred_hasQuery         = "hasQuery";
    static String pred_hasQueryLanguage = "hasQueryLanguage";
    static String pred_hasSubject       = "hasSubject";
    static String pred_hasTextDoc       = "hasTextDoc";
    static String pred_hasValue         = "hasValue";
    static String pred_hasValueProducer = "hasValueProducer";
        // rdf predicates
    static String pred_label            = "label";
    static String pred_type             = "type";
     
    // data types
    static String type_annotation       = "Annotation";
    static String type_concept          = "Concept";
    static String type_conceptRelation  = "ConceptRelation";
    static String type_document         = "Document";
    static String type_location         = "Location";
    static String type_object           = "Object";
    static String type_objectRelation   = "ObjectRelation";
    static String type_property         = "Property";
    static String type_selectionRule    = "SelectionRule";
    static String type_statement        = "Statement";
    
    /** Creates a new instance of BasicQueries. Creates the repository. 
     *
     * @param model     specifies the current model (excl. namespace)
     * @param context   specifies the current session (user + date) (excl. namespace)
     */
    public BasicQueries(String repository_server, String repository_name,
                        String repository_user,   String repository_password, 
                        String modelUri,           String contextUri) {
        if (repository_server   != null) _repository_server   = repository_server;
        if (repository_name     != null) _repository_name     = repository_name;
        if (repository_user     != null) _repository_user     = repository_user;
        if (repository_password != null) _repository_password = repository_password;
        // TODO: modelUri and contextUri should not be null
        if (modelUri  != null) _modelUri   = modelUri;
        if (contextUri!= null) _contextUri = contextUri;
        try {
            _rep = RepositoryFactory.createRepository(_repository_server, _repository_name,
                                                      _repository_user, _repository_password);  
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    
    /***********************************************/
    /*  Functions for access to private parameters */ 
    /***********************************************/
    public void setModel(String modelUri) { _modelUri = modelUri; }
    public String getModel() { return _modelUri; }
    
    public void setContext(String contextUri) { _contextUri = contextUri; }
    public String getContext() { return _contextUri; }

    
    /*********************************************/
    /*  Utility functions (private)              */ 
    /*********************************************/
    /** utility functions to split a BiCad URI into a namespace and an ID */
    private String URI_getNS(String URI) {
        if (!URI.contains(ns_bicad_base)) 
            return URI;
        String[] halves = URI.split("#");
        if (halves.length > 0)
            return halves[0];
        else 
            return null;
    }
    private String URI_getID(String URI) {
        if (!URI.contains(ns_bicad_base)) 
            return URI;
        String[] halves = URI.split("#");
        if (halves.length > 1)
            return halves[1];
        else 
            return null;
    }
    
    private void addStatement(String subject, String predicate, String object)
            throws RepositoryAccessException {
        boolean result = true;
        // don't add empty objects
        // By design this is a valid condition, to prevent checks in all addXxx methods
        if (object == null || object.length()==0) {
System.out.println("addStatement: not adding empty object of "+subject+" "+predicate);
            return;
        }
        result &= _rep.addRdfStatement(subject, predicate, object, _modelUri);
        result &= _rep.addRdfStatement(subject, predicate, object, _contextUri);
        if (!result)
           throw new RepositoryAccessException("Failure to add a statement to the repository");
    }
    
    private boolean removeStatement(String subject, String predicate, String object) {
        boolean result = true;
        if (object == null || object.length()==0) {
System.out.println("removeStatement: not removing empty object of "+subject+" "+predicate);
            return false;   // can't remove empty statements
        }
        result &= _rep.removeRdfStatement(subject, predicate, object, _modelUri);
        result &= _rep.removeRdfStatement(subject, predicate, object, _contextUri);
        return result;
    }
    
    private String queryObject(String subject, String predicate) {
        String query = null;
        if (_queryLanguage == queryLanguageSERQL) {
            query = "select OBJECT from " +
                    "{TRIPLE} rdf:subject  {<" + subject + ">}, " +
                    "{TRIPLE} rdf:predicate  {<" + predicate + ">}, " +
                    "{TRIPLE} rdf:object  {OBJECT}, " +
                    "{TRIPLE} rdf:type  {<" +ns_rdf+type_statement+ ">}, " +
                    "{TRIPLE} rdf:li {<" + _modelUri + ">} ";
        }
        else {
            System.out.println("unknown query language "+_queryLanguage);
            return null;
        }
        String resultTable[][] = _rep.selectQuery(_repository_server, _repository_name,
                                                  _repository_user, _repository_password, 
                                                  _queryLanguage, query);
        if (resultTable == null || resultTable.length < 1) { return null; }
        return resultTable[0][0];
    }
    
    private String[] queryObjectList(String subject, String predicate, boolean stripNamespace) {
        String query = null;
        if (_queryLanguage == queryLanguageSERQL) {
            query = "select OBJECT from " +
                    "{TRIPLE} rdf:subject  {<" + subject + ">}, " +
                    "{TRIPLE} rdf:predicate  {<" + predicate + ">}, " +
                    "{TRIPLE} rdf:object  {OBJECT}, " +
                    "{TRIPLE} rdf:type  {<" +ns_rdf+type_statement+ ">}, " +
                    "{TRIPLE} rdf:li {<" + _modelUri + ">} ";
        }
        else {
            System.out.println("unknown query language "+_queryLanguage);
            return null;
        }
        String resultTable[][] = _rep.selectQuery(_repository_server, _repository_name,
                                                  _repository_user, _repository_password, 
                                                  _queryLanguage, query);
        if (resultTable == null || resultTable.length < 1) { return null; }
        String[] resultList = new String[resultTable.length];
        for (int i = 0; i < resultTable.length; ++i)
            if (stripNamespace)
                resultList[i] = URI_getID(resultTable[i][0]);
            else
                resultList[i] = resultTable[i][0];
        return resultList;
    }
    
    private String[] queryEntityList(String entityType, boolean stripNamespace) {
        String query = null;
        if (_queryLanguage == queryLanguageSERQL) {
            query = "select distinct SUBJECT from " +
                    "{TRIPLE} rdf:subject  {SUBJECT}, " +
//                    "{TRIPLE} rdf:predicate  {<"+ns_rdf+pred_type+">}, " +
                    "{TRIPLE} rdf:predicate {<"+ns_bicad+pred_type+">}, " +
                    "{TRIPLE} rdf:object {<"+ns_bicad+entityType+">}, " +
                    "{TRIPLE} rdf:li {<" + _modelUri + ">} ";
        }
        else {
            System.out.println("unknown query language "+_queryLanguage);
            return null;
        }
//System.out.println("entity query ="+query);        
        String resultTable[][] = _rep.selectQuery(_repository_server, _repository_name,
                                                  _repository_user, _repository_password, 
                                                  _queryLanguage, query);
        if (resultTable == null || resultTable.length < 1) { return null; }
        String[] resultList = new String[resultTable.length];
        for (int i = 0; i < resultTable.length; ++i)
            if (stripNamespace)
                resultList[i] = URI_getID(resultTable[i][0]);
            else
                resultList[i] = resultTable[i][0];
        return resultList;
    }
    
    private String[][] queryStatementsInModel() {
        String query = null;
        if (_queryLanguage == queryLanguageSERQL) {
            query = "select SUBJECT, PRED, OBJECT from " +
                    "{TRIPLE} rdf:subject  {SUBJECT}, " +
                    "{TRIPLE} rdf:predicate  {PRED}, " +
                    "{TRIPLE} rdf:object  {OBJECT}, " +
                    "{TRIPLE} rdf:type  {<" +ns_rdf+type_statement+ ">}, " +
                    "{TRIPLE} rdf:li {<" + _modelUri + ">} ";
        }
        else {
            System.out.println("unknown query language "+_queryLanguage);
            return null;
        }
        return _rep.selectQuery(_repository_server, _repository_name,
                                _repository_user, _repository_password, 
                                _queryLanguage, query);
    }
    
    /*********************************************/
    /*  Utility functions (public)               */ 
    /*********************************************/
    public String exportRepositoryXml() {
        return _rep.extractRdf();
    }
  
    public String getBiCadNamespace() {
        return ns_bicad;
    }
    
    public String[][] selectQuery(String query, String queryLanguage) {
//System.out.println("BQ parameters: query="+query+"; qLang="+queryLanguage);
        return _rep.selectQuery(_repository_server, _repository_name,
                                _repository_user, _repository_password, queryLanguage, query);
// (failed) Test for executeSelectionRule()
//        String queryUri = ns_bicad+"QR_BQ_select";
//        try {
//            addSelectionRule(queryUri, query, queryLanguage, null);
//        } catch (Exception e) {};
//        String result[][] = executeSelectionRule(queryUri);
//if (result == null)        
//    System.out.println("no result from executeSelectionRule");
//else 
//    System.out.println("executeSelectionRule yields length "+result.length);
//        removeSelectionRule(queryUri);
//        return result;
    }
    
    public boolean deleteModel() {
        boolean res = true;
        String[][] statements = queryStatementsInModel();
System.out.println("BQ.deleteModel(): number of statements in model: "+statements.length);        
        for (int i = 0; i < statements.length; ++i) {
System.out.println("    "+i+"\t"+statements[i][0]+", " +statements[i][1]+", "+statements[i][2]);        
            res &= removeStatement(statements[i][0], statements[i][1], statements[i][2]);
        }
        return res;    
    }
    

    /******************************************************/
    /*  Below here are only add, get and remove functions */ 
    /******************************************************/

    /*********************************************/
    /*  functions about Annotation               */ 
    /*********************************************/
    public void addAnnotation(Annotation anno) throws RepositoryAccessException {
        addAnnotation( anno.getAnnotationUri(), anno.getLocationUri(), 
                       anno.getPropertyUri(),   anno.getConceptUri(),   anno.getValueLit(), 
                       anno.getValueProducerLit(), anno.getDisplayNameLit() );
    }
    public void addAnnotation(String annotationUri, String locUri, String propUri, String conceptUri,
                              String valueLit, String valueProdLit, String dispNameLit) 
            throws RepositoryAccessException {
//System.out.println("BQ: addAnnotation(String x 5)");        
        addStatement(annotationUri, ns_bicad+pred_hasLocation, locUri);
        addStatement(annotationUri, ns_bicad+pred_hasConcept, conceptUri);
        addStatement(annotationUri, ns_bicad+pred_hasProperty, propUri);
        addStatement(annotationUri, ns_bicad+pred_hasValue, valueLit);
        addStatement(annotationUri, ns_bicad+pred_hasValueProducer, valueProdLit);
        addStatement(annotationUri, ns_rdf+pred_label, dispNameLit);
        addStatement(annotationUri, ns_rdf+pred_type,  ns_bicad+type_annotation);
    }
    
    public Annotation getAnnotation(String annotationUri) {
        String locUri = null, propUri = null, conceptUri = null, valueLit = null, valueProdLit = null, dispNameLit = null;

//System.out.println("BQ: getAnnotation("+annotationUri+") start");        
        locUri         = queryObject(annotationUri, ns_bicad+pred_hasLocation);
        propUri        = queryObject(annotationUri, ns_bicad+pred_hasProperty);
        conceptUri     = queryObject(annotationUri, ns_bicad+pred_hasConcept);
        valueLit      = queryObject(annotationUri, ns_bicad+pred_hasValue);
        valueProdLit  = queryObject(annotationUri, ns_bicad+pred_hasValueProducer);
        dispNameLit   = queryObject(annotationUri, ns_rdf+pred_label);
//System.out.println("BQ: getAnnotation() stop; locUri = "+locUri );        
        return new Annotation(annotationUri, locUri, propUri, conceptUri, valueLit, valueProdLit, dispNameLit);
    }
    public String[] getAnnotations() {
        return queryEntityList(type_annotation, false);
    }
    
    public boolean removeAnnotation(Annotation anno) {
        return removeAnnotation( anno.getAnnotationUri(), anno.getLocationUri(), 
                                 anno.getPropertyUri(), anno.getConceptUri(),  anno.getValueLit(), 
                                 anno.getValueProducerLit(), anno.getDisplayNameLit() );
    }
    public boolean removeAnnotation(String annotationUri) {
//System.out.println("BQ: remove anno (1xstr)");        
        Annotation anno = getAnnotation(annotationUri);
        return removeAnnotation(anno);
    }
    public boolean removeAnnotation(String annotationUri, String locUri, String propUri, String conceptUri,
                                    String valueLit, String valueProdLit, String dispNameLit) {
        boolean succeed = true;
        
        // try to remove all statements, and just remember if any remove failed
        succeed &= removeStatement(annotationUri, ns_bicad+pred_hasLocation, locUri);
        succeed &= removeStatement(annotationUri, ns_bicad+pred_hasProperty,  propUri);
        succeed &= removeStatement(annotationUri, ns_bicad+pred_hasConcept,  conceptUri);
        succeed &= removeStatement(annotationUri, ns_bicad+pred_hasValue,    valueLit);
        succeed &= removeStatement(annotationUri, ns_bicad+pred_hasValueProducer, valueProdLit);
        succeed &= removeStatement(annotationUri, ns_rdf+pred_label, dispNameLit);
        succeed &= removeStatement(annotationUri, ns_rdf+pred_type,  ns_bicad+type_annotation);
        
        // In contrast to addXXX, does not throw a RepositoryAccessException:
        // it's not a serious error if the given XXX is not in the repository
        return succeed;
    }
    
    
    /*********************************************/
    /*  functions about BiCadObject              */ 
    /*********************************************/
    public void addBiCadObject(BiCadObject obj) throws RepositoryAccessException {
        addBiCadObject( obj.getBiCadObjectUri(), obj.getAnnotationUris(), 
                        obj.getDisplayNameLit() );
    }
    public void addBiCadObject(String objectUri, String[] annoUris, String dispNameLit) 
            throws RepositoryAccessException {
        if (annoUris != null)
            for (int i = 0; i < annoUris.length; ++i)
                addStatement(objectUri, ns_bicad+pred_hasAnnotation, annoUris[i]);
        addStatement(objectUri, ns_rdf+pred_type,  ns_bicad+type_object);
        addStatement(objectUri, ns_rdf+pred_label, dispNameLit);
    }
    
    public BiCadObject getBiCadObject(String objectUri) {
        String[] annoUris = null;
        String dispName = null;

        annoUris  = queryObjectList(objectUri, ns_bicad+pred_hasAnnotation, false);
        dispName = queryObject(objectUri, ns_rdf+pred_label);
        
//        if (annoUris == null || annoUris.length == 0 || dispName == null)
//            return null;
        return new BiCadObject(objectUri, annoUris, dispName);
    }
    public String[] getBiCadObjects() {
        return queryEntityList(type_object, false);
    }
    
    public boolean removeBiCadObject(BiCadObject obj) {
        return removeBiCadObject( obj.getBiCadObjectUri(), obj.getAnnotationUris(), 
                                  obj.getDisplayNameLit() );
    }
    public boolean removeBiCadObject(String objectUri) {
        BiCadObject obj = getBiCadObject(objectUri);
        if (obj == null) return false;
        return removeBiCadObject(obj);
    }
    public boolean removeBiCadObject(String objectUri, String[] annoUris, String dispNameLit) {
        boolean succeed = true;
        
        // try to remove all statements, and just remember if any remove failed
        if (annoUris != null) {
            for (int i = 0; i < annoUris.length; ++i)
                succeed &= removeStatement(objectUri, ns_bicad+pred_hasAnnotation, annoUris[i]);
        }
        
        succeed &= removeStatement(objectUri, ns_rdf+pred_type, ns_bicad+type_object);
        succeed &= removeStatement(objectUri, ns_rdf+pred_label, dispNameLit);
        
        // In contrast to addXXX, does not throw a RepositoryAccessException:
        // it's not a serious error if the given XXX is not in the repository
        return succeed;
    }
    
    public void addAnnotationToBiCadObject(String objectUri, String annoUri) 
            throws RepositoryAccessException {
        addStatement(objectUri, ns_bicad+pred_hasAnnotation, annoUri);
    }
    public boolean removeAnnotationFromBiCadObject(String objectUri, String annoUri) {
        return removeStatement(objectUri, ns_bicad+pred_hasAnnotation, annoUri);
    }
    
    
    /*********************************************/
    /*  functions about Concept                  */ 
    /*********************************************/
    public void addConcept(Concept concept) throws RepositoryAccessException {
        addConcept( concept.getConceptUri(), concept.getPropertyUris(), 
                    concept.getDisplayNameLit() );
    }
    public void addConcept(String conceptUri, String[] propUris, String dispNameLit) 
            throws RepositoryAccessException {
        if (propUris != null)
            for (int i = 0; i < propUris.length; ++i)
                addStatement(conceptUri, ns_bicad+pred_hasProperty, propUris[i]);
        addStatement(conceptUri, ns_rdf+pred_type,  ns_bicad+type_concept);
        addStatement(conceptUri, ns_rdf+pred_label, dispNameLit);
    }
    
    public Concept getConcept(String conceptUri) {
        String[] propUris = null;
        String dispName = null;

        propUris  = queryObjectList(conceptUri, ns_bicad+pred_hasProperty, false);
        dispName = queryObject(conceptUri, ns_rdf+pred_label);
        
        //if (propUris == null || propUris.length == 0 || dispName == null)
        //    return null;
        return new Concept(conceptUri, propUris, dispName);
    }
    public String[] getConcepts() {
        return queryEntityList(type_concept, false);
    }
    
    public boolean removeConcept(Concept concept) {
        return removeConcept( concept.getConceptUri(), concept.getPropertyUris(), 
                              concept.getDisplayNameLit() );
    }
    public boolean removeConcept(String conceptUri) {
        Concept concept = getConcept(conceptUri);
        if (concept == null) return false;
        return removeConcept(concept);
    }
    public boolean removeConcept(String conceptUri, String[] propUris, String dispNameLit) {
        boolean succeed = true;
        
        // try to remove all statements, and just remember if any remove failed
        if (propUris != null) {
            for (int i = 0; i < propUris.length; ++i)
                succeed &= removeStatement(conceptUri, ns_bicad+pred_hasProperty, propUris[i]);
        }
        
        succeed &= removeStatement(conceptUri, ns_rdf+pred_type, ns_bicad+type_concept);
        succeed &= removeStatement(conceptUri, ns_rdf+pred_label, dispNameLit);
        
        // In contrast to addXXX, does not throw a RepositoryAccessException:
        // it's not a serious error if the given XXX is not in the repository
        return succeed;
    }

    public void addPropertyToConcept(String conceptUri, String propUri) 
            throws RepositoryAccessException {
        addStatement(conceptUri, ns_bicad+pred_hasProperty, propUri);
    }
    public boolean removePropertyFromConcept(String conceptUri, String propUri) {
        return removeStatement(conceptUri, ns_bicad+pred_hasProperty, propUri);
    }
    
    /*********************************************/
    /*  functions about ConceptRelation          */ 
    /*********************************************/
    public void addConceptRelation(ConceptRelation rel) throws RepositoryAccessException {
        addConceptRelation( rel.getConceptRelationUri(), rel.getSubjectUri(),
                            rel.getRelationTypeUri(), rel.getDisplayNameLit(), rel.getObjectUris() );
    }
    public void addConceptRelation(String conRelUri, String subjUri, String relTypeUri, String dispNameLit, String[] objUris) 
            throws RepositoryAccessException {
        addStatement(conRelUri, ns_bicad+pred_hasSubject,  subjUri);
        if (objUris != null)
            for (int i = 0; i < objUris.length; ++i)
                addStatement(conRelUri, ns_bicad+pred_hasObject, objUris[i]);
        addStatement(conRelUri, ns_bicad+pred_hasPredicate,  relTypeUri);
        addStatement(conRelUri, ns_rdf+pred_type,  ns_bicad+type_conceptRelation);
        addStatement(conRelUri, ns_rdf+pred_label, dispNameLit);
    }
    
    public ConceptRelation getConceptRelation(String conRelUri) {
        String[] objUris = null;
        String subjUri = null, relTypeUri = null, dispName = null;

        subjUri    = queryObject(conRelUri, ns_bicad+pred_hasSubject);
        objUris    = queryObjectList(conRelUri, ns_bicad+pred_hasObject, false);
        relTypeUri = queryObject(conRelUri, ns_bicad+pred_hasPredicate);
        dispName  = queryObject(conRelUri, ns_rdf+pred_label);
        
//        if (objUris == null || objUris.length == 0 || subjUri == null || relTypeUri == null || dispName == null)
//            return null;
        return new ConceptRelation(conRelUri, subjUri, objUris, relTypeUri, dispName);
    }
    public String[] getConceptRelations() {
        return queryEntityList(type_conceptRelation, false);
    }
    
    public boolean removeConceptRelation(ConceptRelation rel) {
        return removeConceptRelation( rel.getConceptRelationUri(), rel.getSubjectUri(),
                                     rel.getRelationTypeUri(), rel.getDisplayNameLit(), rel.getObjectUris()  );
    }
    public boolean removeConceptRelation(String conRelUri) {
        ConceptRelation rel = getConceptRelation(conRelUri);
        if (rel == null) return false;
        return removeConceptRelation(rel);
    }
    public boolean removeConceptRelation(String conRelUri, String subjUri, String relTypeUri, String dispNameLit, String[] objUris) {
        boolean succeed = true;
        
        succeed &= removeStatement(conRelUri, ns_bicad+pred_hasSubject,  subjUri);
        if (objUris != null)
            for (int i = 0; i < objUris.length; ++i)
                succeed &= removeStatement(conRelUri, ns_bicad+pred_hasObject, objUris[i]);
        succeed &= removeStatement(conRelUri, ns_bicad+pred_hasPredicate,  relTypeUri);
        succeed &= removeStatement(conRelUri, ns_rdf+pred_type,  ns_bicad+type_conceptRelation);
        succeed &= removeStatement(conRelUri, ns_rdf+pred_label, dispNameLit);
        
        // In contrast to addXXX, does not throw a RepositoryAccessException:
        // it's not a serious error if the given XXX is not in the repository
        return succeed;
    }

    public void addObjectToConceptRelation(String conRelUri, String objUri) 
            throws RepositoryAccessException {
        addStatement(conRelUri, ns_bicad+pred_hasObject, objUri);
    }
    public boolean removeObjectFromConceptRelation(String conRelUri, String objUri) {
        return removeStatement(conRelUri, ns_bicad+pred_hasObject, objUri);
    }
    
    
    /*********************************************/
    /*  functions about Document                 */ 
    /*********************************************/
    public void addDocument(Document doc) throws RepositoryAccessException {
        addDocument( doc.getDocumentUri(),    doc.getOrigDocLit(), 
                     doc.getDisplayDocLit(), doc.getTextDocLit(), 
                     doc.getDisplayNameLit() );
    }
    public void addDocument(String documentUri, String origDocUri, String dispDocUri, 
                            String textDocUri, String dispNameLit) 
            throws RepositoryAccessException {
        addStatement(documentUri, ns_bicad+pred_hasOriginalDoc, origDocUri);
        addStatement(documentUri, ns_bicad+pred_hasDisplayDoc,  dispDocUri);
        addStatement(documentUri, ns_bicad+pred_hasTextDoc,     textDocUri);
        addStatement(documentUri, ns_rdf+pred_label, dispNameLit);
        addStatement(documentUri, ns_rdf+pred_type,  ns_bicad+type_document);
    }
    
    public Document getDocument(String documentUri) {
        String origDocUri = null, dispDocUri = null, textDocUri = null, dispNameLit = null;

        origDocUri   = queryObject(documentUri, ns_bicad+pred_hasOriginalDoc);
        dispDocUri   = queryObject(documentUri, ns_bicad+pred_hasDisplayDoc);
        textDocUri   = queryObject(documentUri, ns_bicad+pred_hasTextDoc);
        dispNameLit = queryObject(documentUri, ns_rdf+pred_label);
        
        return new Document(documentUri, origDocUri, dispDocUri, textDocUri, dispNameLit);
    }
    public String[] getDocuments() {
        return queryEntityList(type_document, false);
    }
    
    public boolean removeDocument(Document doc) {
        return removeDocument( doc.getDocumentUri(),    doc.getOrigDocLit(), 
                               doc.getDisplayDocLit(), doc.getTextDocLit(), 
                               doc.getDisplayNameLit() );
    }
    public boolean removeDocument(String documentUri) {
        Document doc = getDocument(documentUri);
        return removeDocument(doc);
    }
    public boolean removeDocument(String documentUri, String origDocUri, String dispDocUri, 
                                  String textDocUri, String dispNameLit) {
        boolean succeed = true;
        
        // try to remove all statements, and just remember if any remove failed
        succeed &= removeStatement(documentUri, ns_bicad+pred_hasOriginalDoc, origDocUri);
        succeed &= removeStatement(documentUri, ns_bicad+pred_hasDisplayDoc,  dispDocUri);
        succeed &= removeStatement(documentUri, ns_bicad+pred_hasTextDoc,     textDocUri);
        succeed &= removeStatement(documentUri, ns_rdf+pred_label, dispNameLit);
        succeed &= removeStatement(documentUri, ns_rdf+pred_type,  ns_bicad+type_document);
        
        // In contrast to addXXX, does not throw a RepositoryAccessException:
        // it's not a serious error if the given XXX is not in the repository
        return succeed;
    }
    
    /*********************************************/
    /*  functions about Location                 */ 
    /*********************************************/
    public void addLocation(Location loc) throws RepositoryAccessException {
        addLocation( loc.getLocationUri(),  loc.getDocumentUri(), 
                     loc.getPositionLit(), loc.getDisplayNameLit() );
    }
    public void addLocation(String locationUri, String documentUri, 
                            String posLit, String dispNameLit) 
            throws RepositoryAccessException {
        addStatement(locationUri, ns_bicad+pred_hasDocument, documentUri);
        addStatement(locationUri, ns_bicad+pred_hasPosition, posLit);
        addStatement(locationUri, ns_rdf+pred_type,  ns_bicad+type_location);
        addStatement(locationUri, ns_rdf+pred_label, dispNameLit);
// for testing:
//throw new RepositoryAccessException("Test error generated in addLocation");
    }
    
    public Location getLocation(String locationUri) {
        String docUri = null, posLit = null, dispName = null;

        docUri    = queryObject(locationUri, ns_bicad+pred_hasDocument);
        posLit   = queryObject(locationUri, ns_bicad+pred_hasPosition);
        dispName = queryObject(locationUri, ns_rdf+pred_label);
        
        return new Location(locationUri, docUri, posLit, dispName);
    }
    public String[] getLocationss() {
        return queryEntityList(type_location, false);
    }
    
    public boolean removeLocation(Location loc) {
        return removeLocation( loc.getLocationUri(),  loc.getDocumentUri(), 
                               loc.getPositionLit(), loc.getDisplayNameLit() );
    }
    public boolean removeLocation(String locationUri) {
        Location loc = getLocation(locationUri);
        return removeLocation(loc);
    }
    public boolean removeLocation(String locationUri, String documentUri, String posLit, String dispNameLit) {
        boolean succeed = true;
        
        // try to remove all statements, and just remember if any remove failed
        succeed &= removeStatement(locationUri, ns_bicad+pred_hasDocument, documentUri);
        succeed &= removeStatement(locationUri, ns_bicad+pred_hasPosition, posLit);
        
        succeed &= removeStatement(locationUri, ns_rdf+pred_type, ns_bicad+type_location);
        succeed &= removeStatement(locationUri, ns_rdf+pred_label, dispNameLit);
        
        // In contrast to addXXX, does not throw a RepositoryAccessException:
        // it's not a serious error if the given XXX is not in the repository
        return succeed;
    }
    
    /*********************************************/
    /*  functions about ObjectRelation           */ 
    /*********************************************/
    public void addObjectRelation(ObjectRelation rel) throws RepositoryAccessException {
        addObjectRelation( rel.getObjectRelationUri(), rel.getSubjectUri(), rel.getObjectUris(),
                           rel.getRelationTypeUri(), rel.getDisplayNameLit() );
    }
    public void addObjectRelation(String objRelUri, String subjUri, String[] objUris, String relTypeUri, String dispNameLit) 
            throws RepositoryAccessException {
        addStatement(objRelUri, ns_bicad+pred_hasSubject,  subjUri);
        if (objUris != null)
            for (int i = 0; i < objUris.length; ++i)
                addStatement(objRelUri, ns_bicad+pred_hasObject, objUris[i]);
        addStatement(objRelUri, ns_bicad+pred_hasPredicate,  relTypeUri);
        addStatement(objRelUri, ns_rdf+pred_type,  ns_bicad+type_objectRelation);
        addStatement(objRelUri, ns_rdf+pred_label, dispNameLit);
    }
    
    public ObjectRelation getObjectRelation(String objRelUri) {
        String[] objUris = null;
        String subjUri = null, relTypeUri = null, dispName = null;

        subjUri    = queryObject(objRelUri, ns_bicad+pred_hasSubject);
        objUris    = queryObjectList(objRelUri, ns_bicad+pred_hasObject, false);
        relTypeUri = queryObject(objRelUri, ns_bicad+pred_hasPredicate);
        dispName  = queryObject(objRelUri, ns_rdf+pred_label);
        
//        if (objUris == null || objUris.length == 0 || subjUri == null || relTypeUri == null || dispName == null)
//            return null;
System.out.println("BQ.getObRel("+objRelUri+") = {"+subjUri+", "+relTypeUri+", "+dispName+"}, #obj = "+objUris.length);        
//        return new ObjectRelation(objRelUri, subjUri, objUris, relTypeUri, dispName);
ObjectRelation or = new ObjectRelation(objRelUri, subjUri, objUris, relTypeUri, dispName);
if (or == null) System.out.println("BQ.getObRel(): new ObjectRelation() returns null");
else System.out.println("BQ.getObRel(): new ObjectRelation() succeeded");
return or;
    }
    public String[] getObjectRelations() {
        return queryEntityList(type_objectRelation, false);
    }
    
    public boolean removeObjectRelation(ObjectRelation rel) {
        return removeObjectRelation( rel.getObjectRelationUri(), rel.getSubjectUri(), rel.getObjectUris(),
                           rel.getRelationTypeUri(), rel.getDisplayNameLit() );
    }
    public boolean removeObjectRelation(String objRelUri) {
        ObjectRelation rel = getObjectRelation(objRelUri);
        if (rel == null) return false;
        return removeObjectRelation(rel);
    }
    public boolean removeObjectRelation(String objRelUri, String subjUri, String[] objUris, String relTypeUri, String dispNameLit) {
        boolean succeed = true;
        
        succeed &= removeStatement(objRelUri, ns_bicad+pred_hasSubject,  subjUri);
        if (objUris != null)
            for (int i = 0; i < objUris.length; ++i)
                succeed &= removeStatement(objRelUri, ns_bicad+pred_hasObject, objUris[i]);
        succeed &= removeStatement(objRelUri, ns_bicad+pred_hasPredicate,  relTypeUri);
        succeed &= removeStatement(objRelUri, ns_rdf+pred_type,  ns_bicad+type_objectRelation);
        succeed &= removeStatement(objRelUri, ns_rdf+pred_label, dispNameLit);
        
        // In contrast to addXXX, does not throw a RepositoryAccessException:
        // it's not a serious error if the given XXX is not in the repository
        return succeed;
    }

    public void addObjectToObjectRelation(String objRelUri, String objUri) 
            throws RepositoryAccessException {
        addStatement(objRelUri, ns_bicad+pred_hasObject, objUri);
    }
    public boolean removeObjectFromObjectRelation(String objRelUri, String objUri) {
        return removeStatement(objRelUri, ns_bicad+pred_hasObject, objUri);
    }
    
    
    /*********************************************/
    /*  functions about Property                 */ 
    /*********************************************/
    public void addProperty(Property prop) throws RepositoryAccessException {
        addProperty( prop.getPropertyUri(), prop.getDisplayNameLit() );
    }
    public void addProperty(String propUri, String dispNameLit) 
            throws RepositoryAccessException {
        addStatement(propUri, ns_rdf+pred_type,  ns_bicad+type_property);
        addStatement(propUri, ns_rdf+pred_label, dispNameLit);
    }
    
    public Property getProperty(String propUri) {
        String dispName = queryObject(propUri, ns_rdf+pred_label);
//        if (dispName == null) return null;
        return new Property(propUri, dispName);
    }
    public String[] getProperties() {
        return queryEntityList(type_property, false);
    }
    
    public boolean removeProperty(Property prop) {
        return removeProperty( prop.getPropertyUri(), prop.getDisplayNameLit() );
    }
    public boolean removeProperty(String propUri) {
        Property prop = getProperty(propUri);
        if (prop == null) return false;
        return removeProperty(prop);
    }
    public boolean removeProperty(String propUri, String dispNameLit) {
        boolean succeed = true;
        
        // try to remove all statements, and just remember if any remove failed
        succeed &= removeStatement(propUri, ns_rdf+pred_type,  ns_bicad+type_property);
        succeed &= removeStatement(propUri, ns_rdf+pred_label, dispNameLit);
        
        // In contrast to addXXX, does not throw a RepositoryAccessException:
        // it's not a serious error if the given XXX is not in the repository
        return succeed;
    }
    
    /*********************************************/
    /*  functions about SelectionRule            */ 
    /*********************************************/
    public void addSelectionRule(SelectionRule rule) throws RepositoryAccessException {
        addSelectionRule( rule.getSelectionRuleUri(),  rule.getQueryLit(), 
                          rule.getQueryLanguageLit(), rule.getDisplayNameLit() );
    }
    public void addSelectionRule(String ruleUri, String queryLit, String queryLangLit, String dispNameLit) 
            throws RepositoryAccessException {
	addStatement(ruleUri, ns_bicad+pred_hasQuery, queryLit);
	addStatement(ruleUri, ns_bicad+pred_hasQueryLanguage, queryLangLit);
        addStatement(ruleUri, ns_rdf+pred_type,  ns_bicad+type_selectionRule);
        addStatement(ruleUri, ns_rdf+pred_label, dispNameLit);
    }
    
    public SelectionRule getSelectionRule(String ruleUri) {
        String query = null, queryLang = null, dispName = null;

        query     = queryObject(ruleUri, ns_bicad+pred_hasQuery);
        queryLang = queryObject(ruleUri, ns_bicad+pred_hasQueryLanguage);
        dispName  = queryObject(ruleUri, ns_rdf+pred_label);
        
//        if (query == null || dispName == null)
//            return null;
        return new SelectionRule(ruleUri, query, queryLang, dispName);
    }
    public String[] getSelectionRules() {
        return queryEntityList(type_selectionRule, false);
    }
    
    public boolean removeSelectionRule(SelectionRule rule) {
        return removeSelectionRule( rule.getSelectionRuleUri(), rule.getQueryLit(), 
                                    rule.getQueryLanguageLit(), rule.getDisplayNameLit() );
    }
    public boolean removeSelectionRule(String ruleUri) {
        SelectionRule rule = getSelectionRule(ruleUri);
        if (rule == null) return false;
        return removeSelectionRule(rule);
    }
    public boolean removeSelectionRule(String ruleUri, String queryLit, String queryLangLit, String dispNameLit) {
        boolean succeed = true;
//System.out.println("removing SelectionRule "+ruleUri+"\t"+queryLangLit+"\t"+dispNameLit+"\t"+queryLit);        
        // try to remove all statements, and just remember if any remove failed
        succeed &= removeStatement(ruleUri, ns_bicad+pred_hasQuery, queryLit);
        succeed &= removeStatement(ruleUri, ns_bicad+pred_hasQueryLanguage, queryLangLit);
        succeed &= removeStatement(ruleUri, ns_rdf+pred_type,  ns_bicad+type_selectionRule);
        succeed &= removeStatement(ruleUri, ns_rdf+pred_label, dispNameLit);
        
        // In contrast to addXXX, does not throw a RepositoryAccessException:
        // it's not a serious error if the given XXX is not in the repository
        return succeed;
    }

    public String[][] executeSelectionRule(String ruleUri) {
        String query, queryLang;

        query     = queryObject(ruleUri, ns_bicad+pred_hasQuery);
        queryLang = queryObject(ruleUri, ns_bicad+pred_hasQueryLanguage);
System.out.println("executing selectionRule "+query);        
        return _rep.selectQuery(_repository_server, _repository_name,
                                _repository_user, _repository_password, queryLang, query);
    }
}
