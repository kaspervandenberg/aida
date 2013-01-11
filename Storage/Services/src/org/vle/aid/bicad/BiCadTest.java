/*
 * BiCadTest.java
 *
 * Created on January 31, 2006, 4:15 PM
 */

package org.vle.aid.bicad;

import java.rmi.RemoteException;

import org.vle.aid.bicad.entities.*;
import org.vle.aid.metadata.exception.QueryException;

/**
 *
 * @author  Camille
 */
public class BiCadTest {
    
    /** Creates a new instance of BiCadTest */
    public BiCadTest() {
    }
    
    /***********************************************/
    /*  Test function to try out new functionality */ 
    /**
     * @throws RemoteException *********************************************/
    static void test() throws RemoteException {
        java.util.Date d = new java.util.Date();
        BasicQueries bq = new BasicQueries(null, null, null, null, 
                        BasicQueries.ns_bicad+"model_1", BasicQueries.ns_bicad+"CFB, "+d.toString()); 
        String ns_bicad = bq.ns_bicad;

        Location loc = null, loc2 = null;
        try {
            bq.addLocation(ns_bicad+"Location_1", ns_bicad+"Doc_532", "14513, 14519", "location Delft");
            loc2 = new Location(ns_bicad+"Location_2", ns_bicad+"Doc_532", "14523, 14529", "location TNO-TPD");
            bq.addLocation(loc2);
            bq.addAnnotation(ns_bicad+"Annotation_1", ns_bicad+"Location_2", ns_bicad+"Property_site", ns_bicad+"Concept_afsd",
                    "Delft", "external calculator", "Annotate mens");
//            addDocument(ns_bicad+"Doc_532", "a:/orig.doc", "a:/display.pdf", "a:/orig.txt", "Document 532");
            bq.addDocument(ns_bicad+"Doc_532", "a:/orig I nal.doc", null, null, "Document 532");

            String[] annoUris = {ns_bicad+"Annotation_1", ns_bicad+"Annotation_12"};
            bq.addBiCadObject(ns_bicad+"Object_1", annoUris, "anno test object");
            bq.addAnnotationToBiCadObject(ns_bicad+"Object_1", ns_bicad+"Annotation_33");
            bq.removeAnnotationFromBiCadObject(ns_bicad+"Object_1", ns_bicad+"Annotation_12");

            String[] objUris = {ns_bicad+"Object_2"};
            bq.addObjectRelation(ns_bicad+"ObjectRel_1", ns_bicad+"Object_1", objUris, "partOf", "aggregate");
            bq.addObjectToObjectRelation(ns_bicad+"ObjectRel_1", ns_bicad+"Object_3");
            bq.removeObjectFromObjectRelation(ns_bicad+"ObjectRel_1", ns_bicad+"Object_2");
        
            String[] propUris = {ns_bicad+"Property_1", ns_bicad+"Property_12"};
            bq.addConcept(ns_bicad+"Concept_1", propUris, "concept with 2 properties");
            bq.addPropertyToConcept(ns_bicad+"Concept_1", ns_bicad+"Property_3");
            bq.removePropertyFromConcept(ns_bicad+"Concept_1", ns_bicad+"Property_1");

            bq.addConceptRelation(ns_bicad+"ConceptRel_1", ns_bicad+"Object_1", "partOf", "OvO", objUris);
            bq.addObjectToConceptRelation(ns_bicad+"ConceptRel_1", ns_bicad+"Object_3");
            bq.removeObjectFromConceptRelation(ns_bicad+"ConceptRel_1", ns_bicad+"Object_2");
        
            bq.addSelectionRule(ns_bicad+"Rule_1",//"select distinct * from {S} P {O} where P = rdf:li"
                                                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                                "select distinct ?S ?P ?O " +
                                                "where { ?S ?P ?O . filter (?P = rdf:li)}",
                                                bq.queryLanguageSPARQL, "Select all");
            bq.addProperty(ns_bicad+"Property_1", "Property zoveel");
        } catch (Exception e) {e.printStackTrace(); }
        
        loc = bq.getLocation(ns_bicad+"Location_1");
        if (loc != null)
            System.out.println("loc = "+loc.getLocationUri()+
                               "; doc = "+loc.getDocumentUri()+
                               "; pos = "+loc.getPositionLit()+
                               "; name = "+loc.getDisplayNameLit() );
        else
            System.out.println("Can't find all statements for Location_1");

        Annotation anno = bq.getAnnotation(ns_bicad+"Annotation_1");
        if (anno != null)
            System.out.println("anno = "+anno.getAnnotationUri()+"; loc = "+anno.getLocationUri()+
                               "; property = "+anno.getPropertyUri()+"; value = "+anno.getValueLit()+
                               "; valueProd = "+anno.getValueProducerLit()+"; name = "+anno.getDisplayNameLit() );
        else
            System.out.println("Can't find all statements for Annotation_1");

        Document doc = bq.getDocument(ns_bicad+"Doc_532");
        if (doc != null)
            System.out.println("doc = "+doc.getDocumentUri()+
                     "; orig doc = "+doc.getOrigDocLit()+
                     "; display doc = "+doc.getDisplayDocLit()+
                     "; text doc = "+doc.getTextDocLit()+
                     "; display name = "+doc.getDisplayNameLit() );
        else
            System.out.println("Can't find all statements for Doc_532");

        BiCadObject obj = bq.getBiCadObject(ns_bicad+"Object_1");
        if (obj != null) {
            System.out.println("obj = "+obj.getBiCadObjectUri()+
                               "; name = "+obj.getDisplayNameLit() );
            for (int i = 0; i < obj.getAnnotationUris().length; ++i)
                System.out.print("   anno["+i+"] = "+(obj.getAnnotationUris()[i])+", ");
            System.out.println("");
        }
        else
            System.out.println("Can't find all statements for Object_1");
        
        ObjectRelation rel = bq.getObjectRelation(ns_bicad+"ObjectRel_1");
        if (rel != null) {
            System.out.println("objRel = "+rel.getObjectRelationUri()+
                               "; subj = "+rel.getSubjectUri()+
                               "; relType = "+rel.getRelationTypeUri()+
                               "; name = "+rel.getDisplayNameLit() );
            for (int i = 0; i < rel.getObjectUris().length; ++i)
                System.out.print("   obj["+i+"] = "+(rel.getObjectUris()[i])+", ");
            System.out.println("");
        }
        else
            System.out.println("Can't find all statements for ObjectRel_1");

        Concept concept = bq.getConcept(ns_bicad+"Concept_1");
        if (concept != null) {
            System.out.println("concept = "+concept.getConceptUri()+
                               "; name = "+concept.getDisplayNameLit() );
            for (int i = 0; i < concept.getPropertyUris().length; ++i)
                System.out.print("   prop["+i+"] = "+(concept.getPropertyUris()[i])+", ");
            System.out.println(""); 
        }
        else
            System.out.println("Can't find all statements for Concept_1");

        ConceptRelation concRel = bq.getConceptRelation(ns_bicad+"ConceptRel_1");
        if (concRel != null) {
            System.out.println("ConceptRel = "+concRel.getConceptRelationUri()+
                               "; subj = "+concRel.getSubjectUri()+
                               "; relType = "+concRel.getRelationTypeUri()+
                               "; name = "+concRel.getDisplayNameLit() );
            for (int i = 0; i < concRel.getObjectUris().length; ++i)
                System.out.print("   obj["+i+"] = "+(concRel.getObjectUris()[i])+", ");
            System.out.println("");
        }
        else
            System.out.println("Can't find all statements for ConceptRel_1");
        
        SelectionRule rule = bq.getSelectionRule(ns_bicad+"Rule_1");
        if (rule != null)
            System.out.println("rule = "+rule.getSelectionRuleUri()+
                     "; query = "+rule.getQueryLit()+
                     "; queryLanguage ="+rule.getQueryLanguageLit()+
                     "; display name = "+rule.getDisplayNameLit() );
        else
            System.out.println("Can't find all statements for Rule_1");
        String queryResult[][] = bq.executeSelectionRule(ns_bicad+"Rule_1");
        if (queryResult != null) {
            for (int i=0;i<queryResult.length;++i)
            {
                for (int j=0;j<queryResult[i].length;++j)
                    System.out.print("  s["+i+"]["+j+"] = "+queryResult[i][j]);
                System.out.println("");
            }
            if (queryResult.length == 0)
                System.out.println("empty result from executeSelectionRule query");
        }
        else
            System.out.println("no results from executeSelectionRule query");
        

        Property prop = bq.getProperty(ns_bicad+"Property_1");
        if (prop != null)
            System.out.println("property = "+prop.getPropertyUri()+
                     "; display name = "+prop.getDisplayNameLit() );
        else
            System.out.println("Can't find all statements for Property_1");
        
       String[] annos = bq.getAnnotations();
       if (annos != null)
            for (int i=0;i<annos.length;++i)
                System.out.print("["+i+"] = "+annos[i]+"; ");
       else
           System.out.println("No annotations found");
        
        boolean removeRes = true, testRes = true;
        removeRes &= bq.removeProperty(ns_bicad+"Property_1");
        removeRes &= bq.removeSelectionRule(ns_bicad+"Rule_1");
        removeRes &= bq.removeConceptRelation(ns_bicad+"ConceptRel_1");
        removeRes &= bq.removeConcept(ns_bicad+"Concept_1");
        removeRes &= bq.removeObjectRelation(ns_bicad+"ObjectRel_1"); 
        /*removeRes &=*/ bq.removeDocument(ns_bicad+"Doc_532");    // remove fails partially since 2 parameters were not filled
        removeRes &= bq.removeBiCadObject(ns_bicad+"Object_1");
        testRes   &= bq.removeBiCadObject(ns_bicad+"Object_2");      // test if remove also works for non-existing object
        removeRes &= bq.removeAnnotation(ns_bicad+"Annotation_1");
        removeRes &= bq.removeLocation(loc2);
        removeRes &= bq.removeLocation(ns_bicad+"Location_1");
        System.out.println("Result of removing valid entities = "+removeRes+"; of removing non-existing object = "+testRes);
        
        // model should be empty if it started empty
        bq.deleteModel();
        
        // Alternative test functionality
        // String out = rep.extractRdf();
        // System.out.println(out);
    }
    
    /**
     * @param args the command line arguments
     * @throws RemoteException 
     */
    public static void main(String[] args) throws RemoteException {
        test(); 
    }
}
