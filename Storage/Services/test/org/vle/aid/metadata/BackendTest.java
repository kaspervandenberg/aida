package org.vle.aid.metadata;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.openrdf.http.client.HTTPClient;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.vle.aid.metadata.exception.QueryException;
import org.vle.aid.metadata.exception.SystemQueryException;

public class BackendTest extends TestCase {
			
	static HashMap <String, String[]> serverRepositories  = new HashMap<String, String[]>();
	static HashMap <String, String[]> repositoryTopLevels = new HashMap<String, String[]>();
	static HashMap <String, String[]> topToSecondLevel    = new HashMap<String, String[]>();
	
	// Configuration file, containing values used in this test
	static final String TEST_CONFIG					= "BackendTest.properties";
	// Apache commons configs for loading configuration above
	static PropertiesConfiguration configs ;
	
	
	// Files to store last successful test result on getting repositories list 
	// will be overwritten with what is on configuration file
	static  String repositoryResultFileName 	= "Repositories.test";
	
	// Files to store last successful test result on getting top levels 
	// will be overwritten with what is on configuration file
	static  String topLevelResultFileName 		= "TopLevel.test"; 
	
	// Whether or not to run full test, checking all repositories within a server 
	// will be overwritten with what is on configuration file
	static boolean runShortTest = true;
	
	
	// Default list of SPQRL endpoint server names, 
	// will be overwritten with what is on configuration file
	static String [] serverNames = 	new String[]{
		 "http://dev.adaptivedisclosure.org/openrdf-sesame"
		,"http://amc-app2.amc.sara.nl/openrdf-sesame"
		,"http://tarski.duhs.org:8080/openrdf-sesame"
		,"http://hcls.deri.org/sparql"
	};
	
	// Only one repository per server for a short test.
	// will be overwritten with what is on configuration file
	static String [] testedRepositories = new String [] { "tno", "GO","SNOMED","GO" };
	
	@Override
	protected void setUp() throws Exception {
		configs = new PropertiesConfiguration(new File(TEST_CONFIG));
		runShortTest = new Boolean(configs.getProperty("runShort").toString());
		repositoryResultFileName = (String) configs.getProperty("repositoryResultFileName");
		topLevelResultFileName = (String) configs.getProperty("topLevelResultFileName");
		
		serverNames  = configs.getStringArray("servers");
		testedRepositories = configs.getStringArray("repositories");
		super.setUp();
	}
	
	/**
	 * Main test entry point
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void testEntryPoint() throws IOException, ClassNotFoundException{
		 
		 // Check if previous reference file to check against existed.
		 if(new File(repositoryResultFileName).exists() && new File(topLevelResultFileName).exists()){
			 System.out.println("Using existing previous file test results. Running test");
			 RunTheRealTest();			
		 } else {
			 // We are not running real test since there are no reference file to check against.
			 System.out.println("Creating reference file");
			 createReferenceFile();
		 }
	}
	
	/**
	 * Creating reference file dumps, that will be used and considered as the correct test result.
	 * @throws IOException
	 */
	public void createReferenceFile() throws IOException {
		 for(int i=0;i<serverNames.length;i++){
			 System.out.println("Checking : "+serverNames[i]);
			 String [] repositories = getRepositoriesList(serverNames[i]);
			 serverRepositories.put(serverNames[i], repositories);
			 if(runShortTest){
				 System.out.println("	Getting top concept for : "+testedRepositories[i]);
				 String top[] = getTopLevelConcepts(serverNames[i], testedRepositories[i]);
				 for(String t : top)
					 System.out.println("		"+t);
				 repositoryTopLevels.put(serverNames[i] + "-"+ testedRepositories[i],top);
				 
			 } else {
				for(String rep : repositories){
						 System.out.println("	Getting top concept for : "+rep);
						 String top[] = getTopLevelConcepts(serverNames[i], rep);
						 for(String t : top)
							 System.out.println("		"+t);
						 repositoryTopLevels.put(serverNames[i] + "-"+ rep,top);
				}
			 }
		 }
		 System.out.println("Dumping reference file");
		 dumpToFile(repositoryResultFileName,serverRepositories);
		 dumpToFile(topLevelResultFileName,repositoryTopLevels);
	}
	
	
	private void RunTheRealTest() throws IOException, ClassNotFoundException {
		System.out.println("Running test for repositories on each server");
		runServerRepositoriesCheck();
		System.out.println("Running test for top level on each repositories ");
		runRepositoryTopLevelCheck();
		System.out.println("Congratulations, all test passed");
	}

	@SuppressWarnings("unchecked")
	private void runServerRepositoriesCheck() throws IOException, ClassNotFoundException {
		serverRepositories = (HashMap <String, String[]> )readFromFile(repositoryResultFileName);
		repositoryTopLevels = (HashMap <String, String[]> )readFromFile(topLevelResultFileName);
		for(String server : serverRepositories.keySet()){
				String [] repositories =  getRepositoriesList(server);
				String [] storedRep = serverRepositories.get(server);
				
				assertEquals(repositories.length, storedRep.length);
				for(int i=0;i<repositories.length;i++)
					assertEquals(repositories[i], storedRep[i]);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void runRepositoryTopLevelCheck() throws IOException, ClassNotFoundException {
		serverRepositories = (HashMap <String, String[]> )readFromFile(repositoryResultFileName);
		repositoryTopLevels = (HashMap <String, String[]> )readFromFile(topLevelResultFileName);
		if(runShortTest){
				for(int i=0;i<serverNames.length;i++){
					String [] topLevels =  getTopLevelConcepts(serverNames[i], testedRepositories[i]);
					String [] storedTopLevels = repositoryTopLevels.get(serverNames[i] + "-"+ testedRepositories[i]);
					System.out.println("Checking equalities of "+testedRepositories[i]+" repository");
					assertEquals(topLevels.length, storedTopLevels.length);
					for(int j=0;j<topLevels.length;j++)
						assertEquals(topLevels[j], storedTopLevels[j]);
				}
		} else {
				for(String serverName : serverRepositories.keySet()){
				 System.out.println("Checking SPARQL End point "+serverName);
				 for(String repository : serverRepositories.get(serverName)){
						String [] topLevels =  getTopLevelConcepts(serverName, repository);
						String [] storedTopLevels = repositoryTopLevels.get(serverName + "-"+ repository);
						System.out.println("Checking equalities of "+repository+" repository");
						assertEquals(topLevels.length, storedTopLevels.length);
						for(int i=0;i<topLevels.length;i++)
							assertEquals(topLevels[i], storedTopLevels[i]);
				 }		
				 System.out.println("Checking SPARQL End point "+serverName+ " finished ");
				}
		}
	}

	
	String [] getRepositoriesList(String serverX) throws SystemQueryException{
		Sesame2Repository repository= new Sesame2Repository(serverX,"","","","");
		return getColumn(repository.getRepositoriesLabel(),1);
	}

	String [] getTopLevelConcepts(String serverX, String repositoryY) throws QueryException{
		ThesaurusRepository tr = new ThesaurusRepository(serverX,repositoryY,"","");
		return getColumn(tr.getTopConcepts(""),0);
	}
	
	String [] getNarrower(String serverX, String repositoryY, String nodeX) throws QueryException{
		ThesaurusRepository tr = new ThesaurusRepository(serverX,repositoryY,"","");
		return getColumn(tr.getNarrowerTerms(nodeX),0);
	}
	
	String [][] getNarrowerLabel(String serverX, String repositoryY, String nodeX) throws QueryException{
		ThesaurusRepository tr = new ThesaurusRepository(serverX,repositoryY,"","");
		return tr.getNarrowerTerms(nodeX);
	}

	private String[] getColumn(String[][] labelURLArray, int idx) {
		if(labelURLArray == null) return new String[0];
		String [] result = new String[labelURLArray.length];
		for(int i=0;i<result.length;i++) result[i] = labelURLArray[i][idx];
		return result;
	}

	
	private void dumpToFile(String fileName, Object obj) throws IOException{
		FileOutputStream dump = new FileOutputStream(new File(fileName));
		ObjectOutputStream out = new ObjectOutputStream(dump);
		out.writeObject(obj);
		out.close();
	}
	
	private Object readFromFile(String fileName) throws IOException, ClassNotFoundException{
		FileInputStream dumpR = new FileInputStream(new File(fileName));
		ObjectInputStream inp = new ObjectInputStream(dumpR);
		return inp.readObject();
	}
	
	public void DeriRepo() throws Exception{
		String [] result = getRepositoriesList("http://hcls.deri.org/sparql");
		assert(result != null);
		for(String x : result) System.out.println(x);
	}
	
	public void DeriGo() throws Exception{
		String server = "http://hcls.deri.org/sparql";
		String [] repos = getRepositoriesList(server);
		for(String repository : repos){
				System.out.println("Checking "+repository);
				String [] result = getTopLevelConcepts(server, repository);
				assert(result != null);
				System.out.println("     "+result.length);
				for(String x : result) System.out.println("		"+x);
		}
	}
	
	public void TNO() throws Exception{
		String [][] result = getNarrowerLabel("http://dev.adaptivedisclosure.org/openrdf-sesame", "tno", "http://www.afsg.nl/www_foodontology_nl/data/documenten/Ontology/TNO_FI-Ontology_2007_jan.owl#food");
		assert(result != null);
		System.out.println(result.length);
		for(String[] x : result) System.out.println(Arrays.deepToString(x));
	}
	
	public void GetRepositorySesameAPI() throws Exception{
		HTTPClient http_cl = new HTTPClient();
		http_cl.setServerURL("http://hcls.deri.org/sparql");
		
		TupleQueryResult res0 = http_cl.getRepositoryList();
		List<String> bindingNames = res0.getBindingNames();

		while (res0.hasNext()) {
		  BindingSet bindingSet = res0.next();
		  for(String bindName : bindingNames)
			  System.out.println(bindName +" " + bindingSet.getBinding(bindName));

		}
	}
}
