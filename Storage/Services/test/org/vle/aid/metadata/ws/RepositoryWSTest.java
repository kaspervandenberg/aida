package org.vle.aid.metadata.ws;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import java.util.Arrays;

public class RepositoryWSTest extends TestCase
{
    public Service     service  = new Service();
    
    // Correct Default Parameter Values:
    private static String       endpoint            = "http://localhost:8080/axis/services/";
    private static String       web_service_name    = "RepositoryWS";
    private static String       query_function_name = "selectQuery";
    private static String[]     function_names      = {"getRepositories",
                                            "addRdf", "addRdfStatement", "addRdfStatementWithContext", "addRdfFile",
                                            "removeRdf", "removeRdfStatement", "removeRdfStatementWithContext","removeRdfFile",
                                            "clear", "extractRdf", "constructQuery",
                                            "selectQuerySerialized", "selectQuery"};

    private static String repository_server    = "http://aida.science.uva.nl:9999/openrdf-sesame";
    private static String repository           = "BioAID_test";
    private static String rdf_format           = "RDF/XML";
    private static String query_lang           = "serql";
                                            //"sparql";
    private static String select_query         = "SELECT * FROM {x} p {y} LIMIT 20";
                                            //"select * where {?s ?p ?o} LIMIT 20";
    private static String construct_query      = "CONSTRUCT * FROM {_:node13qtd80mvx38} rdfs:subClassOf {rdfs:Resource}";
    private static String namespaces = "USING NAMESPACE " +
            "dc = <http://purl.org/dc/elements/1.1/>," + 
            "rdfs = <http://www.w3.org/2000/01/rdf-schema#>," + 
            "owl2xml = <http://www.w3.org/2006/12/owl2-xml#>," + 
            "Workflow = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Workflow.owl#>," + 
            "xsd = <http://www.w3.org/2001/XMLSchema#>," + 
            "owl = <http://www.w3.org/2002/07/owl#>," + 
            "rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>," + 
            "owl2 = <http://www.w3.org/2006/12/owl2#>," + 
            "Text = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#>," + 
            "TextMining = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#>," + 
            "BioModel = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioModel.owl#>," + 
            "BioAnnotations = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioAnnotations.owl#>," + 
            "Proto-ontology = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/>," + 
            "MappingBioTextMining = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/MappingBioTextMining.owl#>," + 
            "AIDA_Instances = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/AIDA_Instances.owl#>," + 
            "txt = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#>," + 
            "meth = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#>," + 
            "bio = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioModel.owl#>," + 
            "map = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/MappingBioTextMining.owl#>," + 
            "aida = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/AIDA_Instances.owl#>," + 
            "eg = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Enriched-ontology/ExampleInstances.owl#>," + 
            "proto = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/>," + 
            "inst = <http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Enriched-ontology/BioAID_Instances.owl#> ";
     private static String data_uri    = "http://rdf.adaptivedisclosure.org/tmp/ExampleRDF/ExampleInstanceEnzymeTermEZH2_090127.rdf.xml";
     private static String data    =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rdf:RDF xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"+
                        "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\nxmlns:owl2xml=\"http://www.w3.org/2006/12/owl2-xml#\"\n"+
                        "xmlns:Workflow=\"http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Workflow.owl#\"\n"+
                        "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\nxmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"+
                        "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\nxmlns:owl2=\"http://www.w3.org/2006/12/owl2#\"\n"+
                        "xmlns:Text=\"http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#\"\n"+
                        "xmlns:TextMining=\"http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#\"\n"+
                        "xmlns:BioModel=\"http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioModel.owl#\"\n"+
                        "xmlns:MappingBioTextMining=\"http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/MappingBioTextMining.owl#\"\n"+
                        "xmlns:BioAnnotations=\"http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioAnnotations.owl#\"\n"+
                        "xmlns:Proto-ontology=\"http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/\"\n"+
                        "xmlns:AIDA_Instances=\"http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/AIDA_Instances.owl#\"\n"+
                        "xmlns:ExampleInstances=\"http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Enriched-ontology/ExampleInstances.owl#\">\n"+
                    "<rdf:Description rdf:about=\"http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Enriched-ontology/ExampleInstances.owl#ExampleInstance_QueryProteinTerm\">\n"+
                        "<rdf:type rdf:resource=\"http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#EnzymeTerm\"/>\n"+
                        "<rdfs:label rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">an enzyme term</rdfs:label>\n"+
                        "<rdfs:comment rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">E.g. 'EZH2'</rdfs:comment>\n"+
                        "<MappingBioTextMining:references rdf:resource=\"http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Enriched-ontology/ExampleInstances.owl#ExampleInstance_Enzyme\"/>\n"+
                        "<Text:is_Content_Component_Of rdf:resource=\"http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Enriched-ontology/ExampleInstances.owl#ExampleInstance_OriginalDocumentSearchQuery\"/>\n"+
                    "</rdf:Description>\n"+
                    "</rdf:RDF>";
    private static String subject   = "http://www.afsg.nl/www_foodontology_nl/data/documenten/Ontology/TNO_FI-Ontology_2007_jan.owl#scheme";
    private static String predicate = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    private static String object    = "http://www.w3.org/2004/02/skos/core#ConceptScheme";
    private static String context   = "proto:Text.owl";

            
    public RepositoryWSTest(String testName)
    {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    
    public void testRepositoryWSExceptions() throws Exception
    {
        System.err.println("repository webservice");
        
        System.err.println();System.err.println("- I. test exceptions");
        
        for(int i=0; i<function_names.length; i++)
        {
            System.err.println();System.err.println("-- I."+i+".1 test wrong web services endpoint exception on: " + function_names[i]);
            try
            {
                String endpoint_wrong           = "http://localhost:8080/axis/service/";
                doNoReturnCallPerFunction(endpoint_wrong, web_service_name, repository_server, repository, i);
                fail("Should raise an Exception");
            }
            catch (Exception e)
            {
                System.err.println("Caught expected Exception:"+e);
                e.printStackTrace();
            }

            System.err.println();System.err.println("-- I."+i+".2 test wrong web_service_name name exception on: " + function_names[i]);
            try
            {
                String web_service_name_wrong   = "RepositorWS";
                doNoReturnCallPerFunction(endpoint, web_service_name_wrong, repository_server, repository, i);
                fail("Should raise an Exception");
            }
            catch (Exception e)
            {
                System.err.println("Caught expected Exception:"+e);
                e.printStackTrace();
            }
        
            System.err.println();System.err.println("-- I."+i+".3 test wrong repository_server directory name exception on: " + function_names[i]);
            try
            {
                String repository_server_wrong  = "http://aida.science.uva.nl:9999/openrdf-sesame2";
                doNoReturnCallPerFunction(endpoint, web_service_name, repository_server_wrong, repository, i);
                fail("Should raise an Exception");
            }
            catch (Exception e)
            {
                System.err.println("Caught expected Exception:"+e);
                e.printStackTrace();
            }
        
            System.err.println();System.err.println("-- I."+i+".4 test wrong repository_server port exception on: " + function_names[i]);
            try
            {
                String repository_server_wrong  = "http://aida.science.uva.nl:8899/openrdf-sesame";
                doNoReturnCallPerFunction(endpoint, web_service_name, repository_server_wrong, repository, i);
                fail("Should raise an Exception");
            }
            catch (Exception e)
            {
                System.err.println("Caught expected Exception:"+e);
                e.printStackTrace();
            }
        
            System.err.println();System.err.println("-- I."+i+".5 test wrong repository_server adress exception on: " + function_names[i]);
            try
            {
                String repository_server_wrong  = "http://aida.science.vu.nl:8899/openrdf-sesame";
                doNoReturnCallPerFunction(endpoint, web_service_name, repository_server_wrong, repository, i);
                fail("Should raise an Exception");
            }
            catch (Exception e)
            {
                System.err.println("Caught expected Exception:"+e);
                e.printStackTrace();
            }

            if(i!=0) // (Cant't get this exception for GetRepositories - repository is not an argument)
            {
                System.err.println();System.err.println("-- I."+i+".6 test wrong repository name exception on: " + function_names[i]);
                try
                {
                    String repository_wrong        = "tno2";
                    doNoReturnCallPerFunction(endpoint, web_service_name, repository_server, repository_wrong, i);
                    fail("Should raise an Exception");
                }
                catch (Exception e)
                {
                    System.err.println("Caught expected Exception:"+e);
                    e.printStackTrace();
                }
            }
            System.gc();
        }        
    }

    private void doNoReturnCallPerFunction(String endpoint, String web_service_name,
                        String repository_server, String repository, int i) throws Exception
    {
        Call        call     = (Call) service.createCall();
        call.setTargetEndpointAddress(endpoint + web_service_name);
        call.setOperationName(function_names[i]);
        
        switch(i)
        {
            //getRepositories
            case 0:   call.invoke( new Object[]{repository_server, "", "", "r"});
            //addRdf
            case 1:   call.invoke( new Object[]{repository_server, repository, "", "", rdf_format, data_uri, data});
            //addRdfStatement
            case 2:   call.invoke( new Object[]{repository_server, repository, "", "", subject, predicate, object});
            //addRdfStatementWithContext
            case 3:   call.invoke( new Object[]{repository_server, repository, "", "", subject, predicate, object, context});
            //addRdfFile
            case 4:   call.invoke( new Object[]{repository_server, repository, "", "", rdf_format, data_uri});
            //removeRdf
            case 5:   call.invoke( new Object[]{repository_server, repository, "", "", rdf_format, data_uri, data});
            //removeRdfStatement
            case 6:   call.invoke( new Object[]{repository_server, repository, "", "", subject, predicate, object});
            //removeRdfStatementWithContext
            case 7:   call.invoke( new Object[]{repository_server, repository, "", "", subject, predicate, object, context});
            //removeRdfFile
            case 8:   call.invoke( new Object[]{repository_server, repository, "", "", rdf_format, data_uri});
            //clear
            case 9:   call.invoke( new Object[]{repository_server, repository, "", ""});
            //extractRdf
            case 10:  call.invoke( new Object[]{repository_server, repository, "", "", rdf_format});
            //constructQuery
            case 11:  call.invoke( new Object[]{repository_server, repository, "", "", query_lang, rdf_format, construct_query});
            //selectQuerySerialized
            case 12:  call.invoke( new Object[]{repository_server, repository, "", "", query_lang, "html_table", select_query});
            //selectQuery
            case 13:  call.invoke( new Object[]{repository_server, repository, "", "", query_lang, select_query});
            // "useful for debugging in case of modifications"
            default:  call.invoke( new Object[]{repository_server, "", "", "r"});
        }
        System.gc();
    }

    public void testRepositoryWSGetRepositories() throws Exception
    {
        System.err.println();System.err.println("- II. test fuctions");

        System.err.println();System.err.println("-- II.1 test GetRepositories");

        Call        call     = (Call) service.createCall();
        call.setTargetEndpointAddress(endpoint + web_service_name);
        call.setOperationName("getRepositories");

        String[] res = (String[]) call.invoke( new Object[]{repository_server, "", "", "r"});
        Arrays.sort(res);

        boolean matched = false;
        for(int i=0;i<res.length;i++)
        {
            if(res[i].compareTo(repository)==0)
            {
                matched = true;
                break;
            }
        }
        Assert.assertTrue(matched);
        System.gc();
    }

    public void testRepositoryWSAddRdf() throws Exception
    {
        System.err.println();System.err.println("-- II.2 test AddRdf");

        Call        call     = (Call) service.createCall();
        call.setTargetEndpointAddress(endpoint + web_service_name);
        call.setOperationName("addRdf");

        boolean matched = false;
        if(call.invoke( new Object[]{repository_server, repository, "", "", rdf_format, data_uri, data}).equals(true))
            matched = true;

        Assert.assertTrue(matched);
        System.gc();
    }

    public void testRepositoryWSRemoveRdf() throws Exception
    {
        System.err.println();System.err.println("-- II.3 test RemoveRdf");

        Call        call     = (Call) service.createCall();
        call.setTargetEndpointAddress(endpoint + web_service_name);
        call.setOperationName("removeRdf");

        boolean matched = false;
        if(call.invoke( new Object[]{repository_server, repository, "", "", rdf_format, data_uri, data}).equals(true))
            matched = true;

        Assert.assertTrue(matched);
        System.gc();
    }

    public void testRepositoryWSAddRdfStatement() throws Exception
    {
        System.err.println();System.err.println("-- II.4 test AddRdfStatement");

        Call        call     = (Call) service.createCall();
        call.setTargetEndpointAddress(endpoint + web_service_name);
        call.setOperationName("addRdfStatement");

        boolean matched = false;
        if(call.invoke( new Object[]{repository_server, repository, "", "", subject, predicate, object}).equals(true))
            matched = true;

        Assert.assertTrue(matched);
        System.gc();
    }

    public void testRepositoryWSRemoveRdfStatement() throws Exception
    {
        System.err.println();System.err.println("-- II.5 test RemoveRdfStatement");

        Call        call     = (Call) service.createCall();
        call.setTargetEndpointAddress(endpoint + web_service_name);
        call.setOperationName("removeRdfStatement");

        boolean matched = false;
        if(call.invoke( new Object[]{repository_server, repository, "", "", subject, predicate, object}).equals(true))
            matched = true;

        Assert.assertTrue(matched);
        System.gc();
    }

    public void testRepositoryWSAddRdfStatementWithContext() throws Exception
    {
        System.err.println();System.err.println("-- II.6 test AddRdfStatementWithContext");

        Call        call     = (Call) service.createCall();
        call.setTargetEndpointAddress(endpoint + web_service_name);
        call.setOperationName("addRdfStatementWithContext");

        boolean matched = false;
        if(call.invoke( new Object[]{repository_server, repository, "", "", subject, predicate, object, context}).equals(true))
            matched = true;

        Assert.assertTrue(matched);
        System.gc();
    }

    public void testRepositoryWSRemoveRdfStatementWithContext() throws Exception
    {
        System.err.println();System.err.println("-- II.7 test RemoveRdfStatementWithContext");

        Call        call     = (Call) service.createCall();
        call.setTargetEndpointAddress(endpoint + web_service_name);
        call.setOperationName("removeRdfStatementWithContext");

        boolean matched = false;
        if(call.invoke( new Object[]{repository_server, repository, "", "", subject, predicate, object, context}).equals(true))
            matched = true;

        Assert.assertTrue(matched);
        System.gc();
    }

    public void testRepositoryWSAddRdfWithContext() throws Exception
    {
        System.err.println();System.err.println("-- II.8 test AddRdfWithContext");

        Call        call     = (Call) service.createCall();
        call.setTargetEndpointAddress(endpoint + web_service_name);
        call.setOperationName("addRdfWithContext");

        boolean matched = false;
        if(call.invoke( new Object[]{repository_server, repository, "", "", rdf_format, data_uri, data, context}).equals(true))
            matched = true;

        Assert.assertTrue(matched);
        System.gc();
    }

    public void testRepositoryWSRemoveRdfWithContext() throws Exception
    {
        System.err.println();System.err.println("-- II.9 test RemoveRdfWithContext");

        Call        call     = (Call) service.createCall();
        call.setTargetEndpointAddress(endpoint + web_service_name);
        call.setOperationName("removeRdfWithContext");

        boolean matched = false;
        if(call.invoke( new Object[]{repository_server, repository, "", "", rdf_format, data_uri, data, context}).equals(true))
            matched = true;

        Assert.assertTrue(matched);
        System.gc();
    }

    
    private String[][] doQuery(String query1) throws Exception
    {
        Call        call     = (Call) service.createCall();
        call.setTargetEndpointAddress(endpoint + web_service_name);
        call.setOperationName(query_function_name);

        String[][] result =
            (String[][]) call.invoke( new Object[]
            {
                repository_server,
                repository,
                "","",
                query_lang,
                query1 + namespaces,
            } );

        System.gc();
        return result;
    }

    private boolean formatAndConfirmQueryResult(String[][] result, String[] result_expected) throws Exception
    {
        if((result.length==0)&&(result.length==result_expected.length))
        {
            System.err.println("result matches: true");
            return true;
        }
        else
        {
            String[] result_formatted = new String[result.length];

            for(int i=0;i<result.length;i++)
            {
                for(int j=0;j<result[i].length;j++)
                {
                    if(j==0)
                        result_formatted[i] = result[i][j];
                    else
                        result_formatted[i] = result_formatted[i] + " " + result[i][j];
                }
            }
            Arrays.sort(result_formatted);

            boolean blnResult = Arrays.equals(result_expected,result_formatted);

            System.err.println("result matches: " + blnResult);

            System.gc();
            return blnResult;
        }
    }

    public void testRepositoryWSQueryAllClasses() throws Exception
    {
        System.err.println();System.err.println("- III. test more complex queries");

        System.err.println();System.err.println("-- III.A. Class Queries");
        
        System.err.println();System.err.println("--- III.A.1. test All classes");
       
        String query1 = "SELECT DISTINCT c "+
                        "FROM "+
                        "{c} rdf:type {owl:Class} ";
        
        String[] result_expected =
        {
            "_:node13qtd80mvx1",
            "_:node13qtd80mvx10",
            "_:node13qtd80mvx13",
            "_:node13qtd80mvx18",
            "_:node13qtd80mvx30",
            "_:node13qtd80mvx34",
            "_:node13qtd80mvx38",
            "_:node13qtd80mvx4",
            "_:node13qtd80mvx42",
            "_:node13qtd80mvx45",
            "_:node13qtd80mvx48",
            "_:node13qtd80mvx52",
            "_:node13qtd80mvx55",
            "_:node13qtd80mvx7",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioModel.owl#BiologicalModel",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioModel.owl#Enzyme",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioModel.owl#Interaction",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioModel.owl#Protein",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioModel.owl#ProteinProteinInteraction",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#Assertion",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#Document",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#DocumentContentComponent",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#EnzymeTerm",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#InteractionAssertion",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#InteractionTerm",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#Protein-ProteinInteractionAssertion",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#ProteinTerm",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#Term",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#DiscoveryScore",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#DocumentRetrievalBySearchProcess",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#DocumentRetrievalProcess",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#DocumentSearchQuery",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TermColocationProcess",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TermRecognitionProcess",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TermRelationExtractionProcess",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TextMiningDiscovery",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TextMiningProcess",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TextMiningProcessRun",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Workflow.owl#ComputationElement",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Workflow.owl#ComputationRun",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Workflow.owl#Workflow",
            "http://www.w3.org/2002/07/owl#Thing"
        };

        String[][] result = doQuery(query1);

        boolean blnResult = formatAndConfirmQueryResult(result, result_expected);

        Assert.assertTrue(blnResult);
        System.gc();
    }    
    
    public void testRepositoryWSQueryDirectSubClasses() throws Exception
    {
        System.err.println();System.err.println("--- III.A.2 test direct subClasses");

        String query1 = "SELECT DISTINCT c " +
                        "FROM " +
                        "{c} serql:directSubClassOf {pc} rdf:type {owl:Class} " +
                        "WHERE " +
                        "pc = owl:Thing ";

        String[] result_expected =
        {
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioModel.owl#BiologicalModel",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioModel.owl#Interaction",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioModel.owl#Protein",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#Document",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#DocumentContentComponent",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#DiscoveryScore",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#DocumentSearchQuery",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TextMiningDiscovery",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TextMiningProcess",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TextMiningProcessRun",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Workflow.owl#ComputationElement",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Workflow.owl#ComputationRun",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Workflow.owl#Workflow"
        };

        String[][] result = doQuery(query1);

        boolean blnResult = formatAndConfirmQueryResult(result, result_expected);
        
        Assert.assertTrue(blnResult);
        System.gc();
    }

    public void testRepositoryWSQuerySubclassesOfSubclasses() throws Exception
    {
        System.err.println();System.err.println("--- III.A.3. test Subclasses of subclasses");

        String query1 = "SELECT DISTINCT pc, c " +
                        "FROM " +
                        "{pc} serql:directSubClassOf {ppc} rdf:type {owl:Class}, " +
                        "{c} serql:directSubClassOf {pc} rdf:type {owl:Class} " +
                        "WHERE " +
                        "ppc = owl:Thing ";

        String[] result_expected =
        {
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioModel.owl#Interaction http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioModel.owl#ProteinProteinInteraction",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioModel.owl#Protein http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/BioModel.owl#Enzyme",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#DocumentContentComponent http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#Assertion",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#DocumentContentComponent http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/Text.owl#Term",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TextMiningProcess http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#DocumentRetrievalProcess",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TextMiningProcess http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TermColocationProcess",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TextMiningProcess http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TermRecognitionProcess",
            "http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TextMiningProcess http://rdf.adaptivedisclosure.org/owl/BioAID/myModel/Proto-ontology/TextMining.owl#TermRelationExtractionProcess"
        };

        String[][] result = doQuery(query1);
        
        boolean blnResult = formatAndConfirmQueryResult(result, result_expected);

        Assert.assertTrue(blnResult);
        System.gc();
    }

    public void testRepositoryWSQueryPropertiesWithInstancesAndDomain() throws Exception
    {
        System.err.println();System.err.println("-- III.B. Property queries");

        System.err.println();System.err.println("--- III.B.1. test properties with instances and a domain");

        String query1 = "SELECT DISTINCT domain, property " +
                        "FROM " +
                        "{domain_instance} property {range}, " +
                        "{property} rdfs:domain {domain} serql:directType {owl:Class} " +
                        "WHERE " +
                        "domain = meth:DiscoveryScore ";

        String[] result_expected = {};

        String[][] result = doQuery(query1);
        
        boolean blnResult = formatAndConfirmQueryResult(result, result_expected);
        
        Assert.assertTrue(blnResult);
        System.gc();
    }

   
}
