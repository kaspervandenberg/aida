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
/**
 *
 * @author bergcvd2
 */
public class BQdemonstrator
{
    private static String _repository_server   = ""; // the default server (localhost:8080/sesame)
    private static String _repository_name     = "mem-rdfs-db"; // the default Main Memory RDFS repository
    private static String _repository_user     = "testuser";   // testuser has write access on the available repositories   
    private static String _repository_password = "opensesame";
    
    private static String _myNamespace  = "http://nowhere.com/~myhome#";

    private static String _myModelUri   = _myNamespace+"Model_1";
    private static String _myContextUri = _myNamespace+"CFB_today";
    

    /** Creates a new instance of BQdemonstrator */
    public BQdemonstrator()
    {
    }

    public static void test()
    {
        BQ_client client = new BQ_client();

        boolean res = true;

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
        if (obj != null)
        {
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
        if (oRel != null)
        {
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

        /* test Higher-level functions */
        client.addTag(_repository_server, _repository_name,
                      _repository_user, _repository_password, _myModelUri, _myContextUri, 
                      "c:/Program Files/test.doc", "11, 22", _myNamespace+"Guten tag", _myNamespace+"April 1st Greetings");
        
        String[][] s = client.getTagLocations(_repository_server, _repository_name,
                             _repository_user, _repository_password, _myModelUri, _myContextUri, _myNamespace+"Guten tag", _myNamespace+"April 1st Greetings");
        if (s != null)
        {
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
        if (s != null)
        {
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
        
    }
    
    public static void main(String[] args)
    {
       test();
    }
}
