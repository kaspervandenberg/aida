/*
 * BQdemonstrator.java
 *
 * Created on 2 maart 2006, 15:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.bicad.client;

import org.vle.aid.bicad.entities.*;
import org.vle.aid.bicad.client.BQ_client;
/**
 *
 * @author bergcvd2
 */
public class BQdemonstrator {
    private static String _repository_server   = ""; // the default server (localhost:8080/sesame)
    private static String _repository_name     = "mem-rdfs-db"; // the default Main Memory RDFS repository
    private static String _repository_user     = "testuser";   // testuser has write access on the available repositories   
    private static String _repository_password = "opensesame";
    
    private static String _myNamespace  = "http://nowhere.com/~myhome#";

    private static String _myModelUri   = _myNamespace+"Model_1";
    private static String _myContextUri = _myNamespace+"CFB_today";
    

    /** Creates a new instance of BQdemonstrator */
    public BQdemonstrator() {
    }

    public static void test() {
        BQ_client client = new BQ_client();

        boolean res = true;
        /* test Annotation */
//        client.addAnnotation(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, 
//                             _myNamespace+"anno_1", _myNamespace+"loc_1", _myNamespace+"prop_1", 
//                             "Dit is een text string", "calc text", "Annotation #1");
//        Annotation anno = client.getAnnotation(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, _myNamespace+"anno_1");
//        if (anno != null)
//            System.out.println("anno = "+anno.getAnnotationUri()+"; loc = "+anno.getLocationUri()+
//                               "; property = "+anno.getPropertyUri()+"; value = "+anno.getValueLit()+
//                               "; valueProd = "+anno.getValueProducerLit()+"; name = "+anno.getDisplayNameLit() );
//        else
//            System.out.println("Anno is null");
//        res = client.removeAnnotation(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, _myNamespace+"anno_1");
//        System.out.println("remove Anno returned " + res);
//        
        /* test Concept */
//        String propList[] = {_myNamespace+"Property_12", _myNamespace+"Property_13"};
//        client.addConcept(_repository_server, _repository_name,
//                          _repository_user, _repository_password, _myModelUri, _myContextUri, 
//                          _myNamespace+"Concept_1", propList, "Conceptualia");
//        client.addPropertyToConcept(_repository_server, _repository_name,
//                          _repository_user, _repository_password, _myModelUri, _myContextUri,
//                           _myNamespace+"Concept_1", _myNamespace+"Property_1");
//        Concept concept = client.getConcept(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, _myNamespace+"Concept_1");
//        if (concept != null) {
//            System.out.println("concept = "+concept.getConceptUri()+
//                               "; name = "+concept.getDisplayNameLit() );
//            for (int i = 0; i < concept.getPropertyUris().length; ++i)
//                System.out.print("   prop["+i+"] = "+(concept.getPropertyUris()[i])+", ");
//            System.out.println(""); 
//        }
//        else
//            System.out.println("Can't find all statements for Concept_1");
//        res = client.removeConcept(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, _myNamespace+"Concept_1");
//        System.out.println("remove Concept returned " + res);

        /* test Document */
//        client.addDocument(_repository_server, _repository_name,
//                           _repository_user, _repository_password, _myModelUri, _myContextUri, 
//                           _myNamespace+"Doc_532", "a:/orig.doc", null, null, "Document 532");
//        Document doc = client.getDocument(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, _myNamespace+"Doc_532");
//        if (doc != null)
//            System.out.println("doc = "+doc.getDocumentUri()+
//                     "; orig doc = "+doc.getOrigDocLit()+
//                     "; display doc = "+doc.getDisplayDocLit()+
//                     "; text doc = "+doc.getTextDocLit()+
//                     "; display name = "+doc.getDisplayNameLit() );
//        else
//            System.out.println("Can't find all statements for Doc_532");
//        res = client.removeDocument(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, _myNamespace+"Doc_532");
//        System.out.println("remove Doc returned " + res);
       
        /* test Location */
//        client.addLocation(_repository_server, _repository_name,
//                           _repository_user, _repository_password, _myModelUri, _myContextUri, 
//                           _myNamespace+"Location_1", _myNamespace+"Doc_532", "14513, 14519", "location Delft");
//        Location loc = client.getLocation(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, _myNamespace+"Location_1");
//        if (loc != null)
//            System.out.println("loc = "+loc.getLocationUri()+
//                               "; doc = "+loc.getDocumentUri()+
//                               "; pos = "+loc.getPositionLit()+
//                               "; name = "+loc.getDisplayNameLit() );
//        else
//            System.out.println("Can't find all statements for Location_1");
//        res = client.removeLocation(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, _myNamespace+"Location_1");
//        System.out.println("remove Loc returned " + res);
       
        /* test Property */
//        client.addProperty(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, 
//                           _myNamespace+"Property_1", "Property zoveel");
//        Property prop = client.getProperty(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, _myNamespace+"Property_1");
//        if (prop != null)
//            System.out.println("property = "+prop.getPropertyUri()+
//                     "; display name = "+prop.getDisplayNameLit() );
//        else
//            System.out.println("Can't find all statements for Property_1");
//        res = client.removeProperty(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, _myNamespace+"Property_1");
//        System.out.println("remove Prop returned " + res);
//       

        /* test BiCadObject */
        String[] annoUris = {_myNamespace+"Annotation_1", _myNamespace+"Annotation_12"};
        client.addBiCadObject(_repository_server, _repository_name, _repository_user, _repository_password, _myModelUri, _myContextUri, 
                              _myNamespace+"Object_1", annoUris, "anno test object");
        client.addAnnotationToBiCadObject(_repository_server, _repository_name, _repository_user, _repository_password, _myModelUri, _myContextUri, 
                              _myNamespace+"Object_1", _myNamespace+"Annotation_33");
        client.removeAnnotationFromBiCadObject(_repository_server, _repository_name, _repository_user, _repository_password, _myModelUri, _myContextUri, 
                              _myNamespace+"Object_1", _myNamespace+"Annotation_12");
        BiCadObject obj = client.getBiCadObject(_repository_server, _repository_name, _repository_user, _repository_password, _myModelUri, _myContextUri, 
                              _myNamespace+"Object_1");
        if (obj != null) {
            System.out.println("obj = "+obj.getBiCadObjectUri()+
                               "; name = "+obj.getDisplayNameLit() );
            for (int i = 0; i < obj.getAnnotationUris().length; ++i)
                System.out.print("   anno["+i+"] = "+(obj.getAnnotationUris()[i])+", ");
            System.out.println("");
        }
        else
            System.out.println("Can't find all statements for Object_1");
        res = client.removeBiCadObject(_repository_server, _repository_name,
                             _repository_user, _repository_password, _myModelUri, _myContextUri, _myNamespace+"Object_1");
        System.out.println("remove Object returned " + res);
        

        /* test ObjectRelation */
        String[] objUris = {_myNamespace+"Object_2",_myNamespace+"Object_22"};
        client.addObjectRelation(_repository_server, _repository_name, _repository_user, _repository_password, _myModelUri, _myContextUri, 
                              _myNamespace+"ObjectRel_1", _myNamespace+"Object_1", objUris, "partOf", "aggregate");
        client.addObjectToObjectRelation(_repository_server, _repository_name, _repository_user, _repository_password, _myModelUri, _myContextUri, 
                              _myNamespace+"ObjectRel_1", _myNamespace+"Object_3");
        client.removeObjectFromObjectRelation(_repository_server, _repository_name, _repository_user, _repository_password, _myModelUri, _myContextUri, 
                              _myNamespace+"ObjectRel_1", _myNamespace+"Object_2");
        ObjectRelation oRel = client.getObjectRelation(_repository_server, _repository_name, _repository_user, _repository_password, _myModelUri, _myContextUri, 
                              _myNamespace+"ObjectRel_1");
        if (oRel != null) {
            System.out.println("objRel = "+oRel.getObjectRelationUri()+
                               "; subj = "+oRel.getSubjectUri()+
                               "; relType = "+oRel.getRelationTypeUri()+
                               "; name = "+oRel.getDisplayNameLit() );
            for (int i = 0; i < oRel.getObjectUris().length; ++i)
                System.out.print("   obj["+i+"] = "+(oRel.getObjectUris()[i])+", ");
            System.out.println("");
        }
        else
            System.out.println("getObjectRelation returns null");

        res = client.removeObjectRelation(_repository_server, _repository_name,
                             _repository_user, _repository_password, _myModelUri, _myContextUri, _myNamespace+"ObjectRel_1");
        System.out.println("remove ObjectRelation returned " + res);
        
//ORel or = client.test_getORel(_repository_server, _repository_name, _repository_user, _repository_password, _myModelUri, _myContextUri);
//if (or != null) {
//    System.out.println("test_getORel: objRel = "+or.getORelId()+
//                       "; subj = "+or.getSubId()+
//                       "; relType = "+or.getPredicateWithALongName()+
//                       "; name = "+or.getDispName() );
//    for (int i = 0; i < or.getObjIds().length; ++i)
//        System.out.print("   obj["+i+"] = "+(or.getObjIds()[i])+", ");
//    System.out.println("");
//}
//else
//    System.out.println("test_getORel returns null");
//oRel = client.test_getObjectRelation(_repository_server, _repository_name, _repository_user, _repository_password, _myModelUri, _myContextUri);
//if (oRel != null) {
//    System.out.println("test_getObjectRelation: objRel = "+oRel.getObjectRelationUri()+
//                       "; subj = "+oRel.getSubjectUri()+
//                       "; relType = "+oRel.getRelationTypeUri()+
//                       "; name = "+oRel.getDisplayNameLit() );
//    for (int i = 0; i < oRel.getObjectUris().length; ++i)
//        System.out.print("   obj["+i+"] = "+(oRel.getObjectUris()[i])+", ");
//    System.out.println("");
//}
//else
//    System.out.println("test_getObjectRelation returns null");

        /* test ConceptRelation */
        client.addConceptRelation(_repository_server, _repository_name, _repository_user, _repository_password, _myModelUri, _myContextUri, 
                              _myNamespace+"ConceptRel_1", _myNamespace+"Object_1", "partOf", "OvO", objUris);
        client.addObjectToConceptRelation(_repository_server, _repository_name, _repository_user, _repository_password, _myModelUri, _myContextUri, 
                              _myNamespace+"ConceptRel_1", _myNamespace+"Object_3");
        client.removeObjectFromConceptRelation(_repository_server, _repository_name, _repository_user, _repository_password, _myModelUri, _myContextUri, 
                              _myNamespace+"ConceptRel_1", _myNamespace+"Object_2");
        ConceptRelation cRel = client.getConceptRelation(_repository_server, _repository_name, _repository_user, _repository_password, _myModelUri, _myContextUri, 
                              _myNamespace+"ConceptRel_1");
        if (cRel == null)
            System.out.println("getConceptRelation failed");
        else
            System.out.println("getConceptRelation succeeded: "+cRel.getConceptRelationUri());
        res = client.removeConceptRelation(_repository_server, _repository_name,
                             _repository_user, _repository_password, _myModelUri, _myContextUri, _myNamespace+"ConceptRel_1");
        System.out.println("remove ConceptRel_1 returned " + res);

        /* test SelectionRule */
//        client.addSelectionRule(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, 
//                             _myNamespace+"Rule_1", "select distinct O from {S} P {O} where P = <http://www.w3.org/1999/02/22-rdf-syntax-ns#li>", "serql", "Select all");
//        SelectionRule rule = client.getSelectionRule(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, 
//                             _myNamespace+"Rule_1");
//        if (rule != null)
//            System.out.println("rule = "+rule.getSelectionRuleUri()+
//                     "; query = "+rule.getQueryLit()+
//                     "; queryLanguage ="+rule.getQueryLanguageLit()+
//                     "; display name = "+rule.getDisplayNameLit() );
//        else
//            System.out.println("Can't find all statements for Rule_1");
//        String queryResult[][] = client.executeSelectionRule(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, 
//                             _myNamespace+"Rule_1");
//        if (queryResult != null) {
//            for (int i=0;i<queryResult.length;++i)
//            {
//                for (int j=0;j<queryResult[i].length;++j)
//                    System.out.print("  s["+i+"]["+j+"] = "+queryResult[i][j]);
//                System.out.println("");
//            }
//            if (queryResult.length == 0)
//                System.out.println("empty result from executeSelectionRule query");
//        }
//        else
//            System.out.println("no results from executeSelectionRule query");
//        res = client.removeSelectionRule(_repository_server, _repository_name,
//                             _repository_user, _repository_password, _myModelUri, _myContextUri, 
//                             _myNamespace+"Rule_1");
//        System.out.println("remove Rule returned " + res);
       
       
        /* test Higher-level functions */
        client.addTag(_repository_server, _repository_name,
                      _repository_user, _repository_password, _myModelUri, _myContextUri, 
                      "c:/Program Files/test.doc", "11, 22", _myNamespace+"Guten tag", _myNamespace+"April 1st Greetings");
        
        String[][] s = client.getTagLocations(_repository_server, _repository_name,
                             _repository_user, _repository_password, _myModelUri, _myContextUri, _myNamespace+"Guten tag", _myNamespace+"April 1st Greetings");
        if (s != null) {
            for (int i=0;i<s.length;++i)
            {
                for (int j=0;j<s[i].length;++j)
                    System.out.print("  s["+i+"]["+j+"] = "+s[i][j]);
                System.out.println("");
            }
            if (s.length == 0)
                System.out.println("empty result from getTagLocations query");
        }
        else
            System.out.println("no results from getTagLocations query");
        
        s = client.getModel(_repository_server, _repository_name,
                            _repository_user, _repository_password, _myModelUri, _myContextUri );
        if (s != null) {
            for (int i=0;i<s.length;++i)
            {
                for (int j=0;j<s[i].length;++j)
                    System.out.print("  s["+i+"]["+j+"] = "+s[i][j]);
                System.out.println("");
            }
            if (s.length == 0)
                System.out.println("empty result from getModel query");
        }
        else
            System.out.println("no results from getModel query");
        
        String[] annos = client.getAnnotations(_repository_server, _repository_name,
                            _repository_user, _repository_password, _myModelUri, _myContextUri );
        if (annos != null)
            for (int i=0;i<annos.length;++i)
                System.out.print("  anno["+i+"] = "+annos[i]);
        
//        client.deleteModel(_repository_server, _repository_name,
//                           _repository_user, _repository_password, _myModelUri, _myContextUri);
    }
    
    public static void main(String[] args) {
   
        test();
    }
}
