/*
 * BasicQueriesWS.java
 *
 * Created on February 21, 2006, 4:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.bicad.ws;

import java.rmi.RemoteException;

import org.vle.aid.bicad.BasicQueries;
import org.vle.aid.bicad.RepositoryAccessException;
import org.vle.aid.bicad.entities.*;
import org.vle.aid.metadata.exception.AddRdfException;
import org.vle.aid.metadata.exception.QueryException;
import org.vle.aid.metadata.exception.RemoveRDFException;


/**
 *
 * @author Camille
 */
public class BasicQueriesWS {
    
    /** Creates a new instance of BasicQueriesWS */
    public BasicQueriesWS() {
    }
    
    public String[][] selectQuery(String repository_server, String repository_name,
                                  String repository_user,   String repository_password, 
                                  String modelUri, String contextlUri,
                                  String query, String queryLanguage) throws QueryException {
//System.out.println("BQ_WS parameters: model="+modelUri+";context="+contextlUri+";query="+query+"; qLang="+queryLanguage);
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        return bq.selectQuery(query, queryLanguage);
        
    }
    
    public Boolean deleteModel(String repository_server, String repository_name,
                               String repository_user,   String repository_password, 
                               String modelUri, String contextlUri) throws QueryException, RemoveRDFException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        // deleteModel will always use modelUri, since that has just been set by the constructor
        boolean res = bq.deleteModel();
        return new Boolean(res);
   }

    /*********************************************/
    /*  functions about Annotation               */ 
    /**
     * @throws AddRdfException *******************************************/
    public void addAnnotation(String repository_server, String repository_name,
                              String repository_user,   String repository_password, 
                              String modelUri, String contextlUri,
                              String annotationUri, String locUri, String propUri, String conceptUri,
                              String valueLit, String valueProdLit, String dispNameLit)
                throws RepositoryAccessException, AddRdfException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        bq.addAnnotation(annotationUri, locUri, propUri, conceptUri, valueLit, valueProdLit, dispNameLit);
    }
    
    public Annotation getAnnotation(String repository_server, String repository_name,
                                    String repository_user,   String repository_password, 
                                    String modelUri, String contextlUri,
                                    String annotationUri) throws QueryException {
//        System.out.println("getting annotation");
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        Annotation anno = bq.getAnnotation(annotationUri);
//System.out.println("annotation ID ="+anno.getAnnotationUri());        
        return anno;
    }
    public String[] getAnnotations(String repository_server, String repository_name,
                                   String repository_user,   String repository_password, 
                                   String modelUri, String contextlUri) throws QueryException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        String[] annos = bq.getAnnotations();
        return annos;
    }
    
    public Boolean removeAnnotation(String repository_server, String repository_name,
                                    String repository_user,   String repository_password, 
                                    String modelUri, String contextlUri,
                                    String annotationUri, String locUri, String propUri, String conceptUri,
                                    String valueLit, String valueProdLit, String dispNameLit) throws RemoveRDFException {
        boolean res = false;
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        res = bq.removeAnnotation(annotationUri, locUri, propUri, conceptUri, valueLit, valueProdLit, dispNameLit);
        return new Boolean(res);
    }
    public Boolean removeAnnotation(String repository_server, String repository_name,
                                    String repository_user,   String repository_password, 
                                    String modelUri, String contextlUri,
                                    String annotationUri ) throws QueryException, RemoveRDFException {
        boolean res = false;
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        res = bq.removeAnnotation(annotationUri);
        return new Boolean(res);
    }

    /*********************************************/
    /*  functions about BiCadObject              */ 
    /**
     * @throws AddRdfException *******************************************/
    public void addBiCadObject(String repository_server, String repository_name,
                           String repository_user,   String repository_password, 
                           String modelUri, String contextlUri,
                           String objectUri, String[] annoUris, String dispNameLit)
                throws RepositoryAccessException, AddRdfException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        bq.addBiCadObject(objectUri, annoUris, dispNameLit);
    }
    
    public BiCadObject getBiCadObject(String repository_server, String repository_name,
                              String repository_user,   String repository_password, 
                              String modelUri, String contextlUri,
                              String objectUri) throws QueryException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        BiCadObject object = bq.getBiCadObject(objectUri);
if (object == null) System.out.println("No object found for "+objectUri); 
else                System.out.println("Object found for "+objectUri);
        return object;
    }
    
    public Boolean removeBiCadObject(String repository_server, String repository_name,
                                 String repository_user,   String repository_password, 
                                 String modelUri, String contextlUri,
                                 String objectUri, String[] annoUris, String dispNameLit) throws RemoveRDFException {
        boolean res = false;
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        res = bq.removeBiCadObject(objectUri, annoUris, dispNameLit);
        return new Boolean(res);
    }
    public Boolean removeBiCadObject(String repository_server, String repository_name,
                                 String repository_user,   String repository_password, 
                                 String modelUri, String contextlUri,
                                 String objectUri ) throws QueryException, RemoveRDFException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        boolean res = bq.removeBiCadObject(objectUri);
        return new Boolean(res);
    }
    
    public void addAnnotationToBiCadObject(String repository_server, String repository_name,
                                     String repository_user,   String repository_password, 
                                     String modelUri, String contextlUri,
                                     String objectUri, String annoUri)
                throws RepositoryAccessException, AddRdfException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        bq.addAnnotationToBiCadObject(objectUri, annoUri);
    }
    public Boolean removeAnnotationFromBiCadObject(String repository_server, String repository_name,
                                             String repository_user,   String repository_password, 
                                             String modelUri, String contextlUri,
                                             String objectUri, String annoUri) throws RemoveRDFException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        boolean res = bq.removeAnnotationFromBiCadObject(objectUri, annoUri);
        return new Boolean(res);
    }

    
    /*********************************************/
    /*  functions about Concept                  */ 
    /**
     * @throws AddRdfException *******************************************/
    public void addConcept(String repository_server, String repository_name,
                           String repository_user,   String repository_password, 
                           String modelUri, String contextlUri,
                           String conceptUri, String[] propUris, String dispNameLit) 
                throws RepositoryAccessException, AddRdfException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        bq.addConcept(conceptUri, propUris, dispNameLit);
    }
    
    public Concept getConcept(String repository_server, String repository_name,
                              String repository_user,   String repository_password, 
                              String modelUri, String contextlUri,
                              String conceptUri) throws QueryException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        Concept concept = bq.getConcept(conceptUri);
        return concept;
    }
    
    public Boolean removeConcept(String repository_server, String repository_name,
                                 String repository_user,   String repository_password, 
                                 String modelUri, String contextlUri,
                                 String conceptUri, String[] propUris, String dispNameLit) throws RemoveRDFException {
        boolean res = false;
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        res = bq.removeConcept(conceptUri, propUris, dispNameLit);
        return new Boolean(res);
    }
    public Boolean removeConcept(String repository_server, String repository_name,
                                 String repository_user,   String repository_password, 
                                 String modelUri, String contextlUri,
                                 String conceptUri ) throws QueryException, RemoveRDFException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        boolean res = bq.removeConcept(conceptUri);
        return new Boolean(res);
    }
    
    public void addPropertyToConcept(String repository_server, String repository_name,
                                     String repository_user,   String repository_password, 
                                     String modelUri, String contextlUri,
                                     String conceptUri, String propUri) 
                throws RepositoryAccessException, AddRdfException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        bq.addPropertyToConcept(conceptUri, propUri);
    }
    public Boolean removePropertyFromConcept(String repository_server, String repository_name,
                                             String repository_user,   String repository_password, 
                                             String modelUri, String contextlUri,
                                             String conceptUri, String propUri) throws RemoveRDFException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        boolean res = bq.removePropertyFromConcept(conceptUri, propUri);
        return new Boolean(res);
    }

    
    /*********************************************/
    /*  functions about ConceptRelation          */ 
    /**
     * @throws AddRdfException *******************************************/
    public void addConceptRelation(String repository_server, String repository_name,
                           String repository_user,   String repository_password, 
                           String modelUri, String contextUri,
                           String conRelUri, String subjUri, String relTypeUri, String dispNameLit, String[] objUris) 
                throws RepositoryAccessException, AddRdfException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextUri);
        bq.addConceptRelation(conRelUri, subjUri, relTypeUri, dispNameLit, objUris);
    }
    
    public ConceptRelation getConceptRelation(String repository_server, String repository_name,
                              String repository_user,   String repository_password, 
                              String modelUri, String contextUri,
                              String conRelUri) throws QueryException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextUri);
        ConceptRelation conrel = bq.getConceptRelation(conRelUri);
        return conrel;
    }
    
    public Boolean removeConceptRelation(String repository_server, String repository_name,
                                 String repository_user,   String repository_password, 
                                 String modelUri, String contextUri,
                                 String conRelUri, String subjUri, String relTypeUri, String dispNameLit, String[] objUris) throws RemoveRDFException {
        boolean res = false;
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextUri);
        res = bq.removeConceptRelation(conRelUri, subjUri, relTypeUri, dispNameLit, objUris);
        return new Boolean(res);
    }
    public Boolean removeConceptRelation(String repository_server, String repository_name,
                                 String repository_user,   String repository_password, 
                                 String modelUri, String contextUri,
                                 String conRelUri ) throws QueryException, RemoveRDFException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextUri);
        boolean res = bq.removeConceptRelation(conRelUri);
        return new Boolean(res);
    }
    
    public void addObjectToConceptRelation(String repository_server, String repository_name,
                                     String repository_user,   String repository_password, 
                                     String modelUri, String contextlUri,
                                     String conRelUri, String objectUri) 
                throws RepositoryAccessException, AddRdfException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        bq.addObjectToConceptRelation(conRelUri, objectUri);
    }
    public Boolean removeObjectFromConceptRelation(String repository_server, String repository_name,
                                             String repository_user,   String repository_password, 
                                             String modelUri, String contextlUri,
                                             String conRelUri, String objectUri) throws RemoveRDFException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        boolean res = bq.removeObjectFromConceptRelation(conRelUri, objectUri);
        return new Boolean(res);
    }
    
    
    /*********************************************/
    /*  functions about Document                 */ 
    /**
     * @throws AddRdfException *******************************************/
    public void addDocument(String repository_server, String repository_name,
                            String repository_user,   String repository_password, 
                            String modelUri, String contextlUri,
                            String documentUri, String origDocUri, String dispDocUri, 
                            String textDocUri, String dispNameLit) 
                throws RepositoryAccessException, AddRdfException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        bq.addDocument(documentUri, origDocUri, dispDocUri, textDocUri, dispNameLit);
    }
    
    public Document getDocument(String repository_server, String repository_name,
                                String repository_user,   String repository_password, 
                                String modelUri, String contextlUri,
                                String documentUri) throws QueryException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        Document doc = bq.getDocument(documentUri);
        return doc;
    }
    
    public Boolean removeDocument(String repository_server, String repository_name,
                                  String repository_user,   String repository_password, 
                                  String modelUri, String contextlUri,
                                  String documentUri, String origDocUri, String dispDocUri, 
                                  String textDocUri, String dispNameLit) throws RemoveRDFException {
        boolean res = false;
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        res = bq.removeDocument(documentUri, origDocUri, dispDocUri, textDocUri, dispNameLit);
        return new Boolean(res);
    }
    public Boolean removeDocument(String repository_server, String repository_name,
                        String repository_user,   String repository_password, 
                        String modelUri, String contextlUri,
                                  String documentUri ) throws QueryException, RemoveRDFException {
        boolean res = false;
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        res = bq.removeDocument(documentUri);
        return new Boolean(res);
    }


    /*********************************************/
    /*  functions about Location                 */ 
    /**
     * @throws AddRdfException *******************************************/
    public void addLocation(String repository_server, String repository_name,
                            String repository_user,   String repository_password, 
                            String modelUri, String contextlUri,
                            String locationUri, String documentUri, 
                            String posLit, String dispNameLit) 
            throws RepositoryAccessException, AddRdfException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        bq.addLocation(locationUri, documentUri, posLit, dispNameLit);
    }
    
    public Location getLocation(String repository_server, String repository_name,
                                String repository_user,   String repository_password, 
                                String modelUri, String contextlUri,
                                String locationUri) throws QueryException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        Location loc = bq.getLocation(locationUri);
        return loc;
    }
    
    public Boolean removeLocation(String repository_server, String repository_name,
                                  String repository_user,   String repository_password, 
                                  String modelUri, String contextlUri,
                                  String locationUri, String documentUri, 
                                  String posLit, String dispNameLit) throws RemoveRDFException {
        boolean res = false;
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        res = bq.removeLocation(locationUri, documentUri, posLit, dispNameLit);
        return new Boolean(res);
    }
    public Boolean removeLocation(String repository_server, String repository_name,
                                  String repository_user,   String repository_password, 
                                  String modelUri, String contextlUri,
                                  String locationUri ) throws QueryException, RemoveRDFException {
        boolean res = false;
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        res = bq.removeLocation(locationUri);
        return new Boolean(res);
    }

    /*********************************************/
    /*  functions about ObjectRelation           */ 
    /**
     * @throws AddRdfException *******************************************/
    public void addObjectRelation(String repository_server, String repository_name,
                           String repository_user,   String repository_password, 
                           String modelUri, String contextlUri,
                           String objRelUri, String subjUri, String[] objUris, String relTypeUri, String dispNameLit)
                throws RepositoryAccessException, AddRdfException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        bq.addObjectRelation(objRelUri, subjUri, objUris, relTypeUri, dispNameLit);
    }
    
    public ObjectRelation getObjectRelation(String repository_server, String repository_name,
                              String repository_user,   String repository_password, 
                              String modelUri, String contextlUri,
                              String objRelUri) throws QueryException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        ObjectRelation objrel = bq.getObjectRelation(objRelUri);
System.out.println("BQ_WS: getObjectRelation("+objRelUri+").subject = "+objrel.getSubjectUri());        
        return objrel;
    }
    
    public Boolean removeObjectRelation(String repository_server, String repository_name,
                                 String repository_user,   String repository_password, 
                                 String modelUri, String contextlUri,
                                 String objRelUri, String subjUri, String[] objUris, String relTypeUri, String dispNameLit) throws RemoveRDFException {
        boolean res = false;
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        res = bq.removeObjectRelation(objRelUri, subjUri, objUris, relTypeUri, dispNameLit);
        return new Boolean(res);
    }
    public Boolean removeObjectRelation(String repository_server, String repository_name,
                                 String repository_user,   String repository_password, 
                                 String modelUri, String contextlUri,
                                 String objRelUri ) throws QueryException, RemoveRDFException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        boolean res = bq.removeObjectRelation(objRelUri);
        return new Boolean(res);
    }
    
    public void addObjectToObjectRelation(String repository_server, String repository_name,
                                     String repository_user,   String repository_password, 
                                     String modelUri, String contextlUri,
                                     String objRelUri, String objectUri)
                throws RepositoryAccessException, AddRdfException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        bq.addObjectToObjectRelation(objRelUri, objectUri);
    }
    public Boolean removeObjectFromObjectRelation(String repository_server, String repository_name,
                                             String repository_user,   String repository_password, 
                                             String modelUri, String contextlUri,
                                             String objRelUri, String objectUri) throws RemoveRDFException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        boolean res = bq.removeObjectFromObjectRelation(objRelUri, objectUri);
        return new Boolean(res);
    }
    
    
    /*********************************************/
    /*  functions about Property                 */ 
    /**
     * @throws AddRdfException *******************************************/
    public void addProperty(String repository_server, String repository_name,
                            String repository_user,   String repository_password, 
                            String modelUri, String contextlUri,
                            String propUri, String dispNameLit)
                throws RepositoryAccessException, AddRdfException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        bq.addProperty(propUri, dispNameLit);
    }
    
    public Property getProperty(String repository_server, String repository_name,
                                String repository_user,   String repository_password, 
                                String modelUri, String contextlUri,
                                String propUri) throws QueryException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        Property prop = bq.getProperty(propUri);
        return prop;
    }
    
    public Boolean removeProperty(String repository_server, String repository_name,
                                  String repository_user,   String repository_password, 
                                  String modelUri, String contextlUri,
                                  String propUri, String dispNameLit) throws RemoveRDFException {
        boolean res = false;
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        res = bq.removeProperty(propUri, dispNameLit);
        return new Boolean(res);
    }
    public Boolean removeProperty(String repository_server, String repository_name,
                                  String repository_user,   String repository_password, 
                                  String modelUri, String contextlUri,
                                  String propUri ) throws QueryException, RemoveRDFException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        boolean res = bq.removeProperty(propUri);
        return new Boolean(res);
    }
    

    /*********************************************/
    /*  functions about SelectionRule            */ 
    /**
     * @throws AddRdfException *******************************************/
    public void addSelectionRule(String repository_server, String repository_name,
                            String repository_user,   String repository_password, 
                            String modelUri, String contextlUri,
                            String ruleUri, String queryLit, String queryLangLit, String dispNameLit)
                throws RepositoryAccessException, AddRdfException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        bq.addSelectionRule(ruleUri, queryLit, queryLangLit, dispNameLit);
    }
    
    public SelectionRule getSelectionRule(String repository_server, String repository_name,
                                String repository_user,   String repository_password, 
                                String modelUri, String contextlUri,
                                String ruleUri) throws QueryException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        SelectionRule rule = bq.getSelectionRule(ruleUri);
        return rule;
    }
    
    public Boolean removeSelectionRule(String repository_server, String repository_name,
                                  String repository_user,   String repository_password, 
                                  String modelUri, String contextlUri,
                                  String ruleUri, String queryLit, String queryLangLit, String dispNameLit) throws RemoveRDFException {
        boolean res = false;
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        res = bq.removeSelectionRule(ruleUri, queryLit, queryLangLit, dispNameLit);
        return new Boolean(res);
    }
    public Boolean removeSelectionRule(String repository_server, String repository_name,
                                  String repository_user,   String repository_password, 
                                  String modelUri, String contextlUri,
                                  String ruleUri ) throws QueryException, RemoveRDFException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        boolean res = bq.removeSelectionRule(ruleUri);
        return new Boolean(res);
    }
    
    public String[][] executeSelectionRule(String repository_server, String repository_name,
                                  String repository_user,   String repository_password, 
                                  String modelUri, String contextlUri,
                                  String ruleUri ) throws RemoteException {
        BasicQueries bq = new BasicQueries(repository_server, repository_name,
                                           repository_user, repository_password, modelUri, contextlUri);
        String[][] res = bq.executeSelectionRule(ruleUri);
        return res;
    }

    
    
//    public ORel test_getORel(String repository_server, String repository_name,
//                                  String repository_user,   String repository_password, 
//                                  String modelUri, String contextlUri)
//    {
//        String[] objs = {"o1", "02"};
//        ORel or = new ORel("OREL_01", "sub", objs, "pred", "objRel 01");
//        return or;
//    }
//    public ObjectRelation test_getObjectRelation(String repository_server, String repository_name,
//                                  String repository_user,   String repository_password, 
//                                  String modelUri, String contextlUri)
//    {
//        String[] objs = {"o1", "02"};
//        ObjectRelation or = new ObjectRelation("OREL_01", "sub", objs, "pred", "objRel 01");
//        return or;
//    }
}

