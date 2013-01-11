/*
 * applyCRF.java
 *
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package katrenko.ws;
import abner.*;

import java.io.*;
import java.lang.*;
import java.util.*;
/**
 *
 * @author Admin
 */
public class applyCRF{
    
    /** Creates a new instance of applyCRF */
    public applyCRF() {
    }
  	private Tagger initializeTagger(String inp)
		{
		Tagger t;
			if (inp.equals("0"))
 				{
 				t = new Tagger(0); 
 				}
			else if (inp.equals("1"))
 				{
 				t = new Tagger(1); 
 				}
			else { 

			t = new Tagger(new File(inp)); }
		return t;
		};

	public String apply(String testFile, String modelFile, String outputMode, String tokenization){
	// ask user to input the test file name for tagging
//	String TEST_RESULT_FILE = "result_".concat(args[0]).concat(".iob2");  //the output file to store the tagging result
	// prepare the output file
	//	try{
//	PrintWriter result= new PrintWriter(new FileOutputStream(TEST_RESULT_FILE));

	//read the input test file into a big string

//	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));

	String bigStr=new String();
        bigStr = testFile;
/*	System.out.println("Trying to read the test file into a big string");
	String tmp = br.readLine();
	while(tmp!=null){
		bigStr = bigStr + " " + tmp;
		tmp = br.readLine();
	}
	br.close();
 */
	System.out.println("Has finished reading the file into a big string");
	System.out.println("Now begin to tag the test file");

 Tagger t;
 t = initializeTagger(modelFile);

//String outputMode = new String(args[2]);
//String tokenization = new String(args[3]);
 	System.out.println("################################################################");
	System.out.println(bigStr);
	System.out.println("################################################################");
	String ret = bigStr;
	
	if (tokenization.equals("1")) { t.setTokenization(true); //t.tokenize(bigStr);
	String rr = ""; String del = "+-*/(),. ";
	StringTokenizer st = new StringTokenizer(bigStr, del, true);
	while(st.hasMoreTokens()){
		String w = st.nextToken();
		if (w.equals(".")) {rr = rr + " .";}
		else if (w.equals(",")) {rr = rr + " ,";}
		else if (w.equals("?")) {rr = rr + " ?";}
		else if (w.equals(":")) {rr = rr + " :";}
		else {rr = rr + w;}
	}
	ret = t.tokenize(rr);
	}
	else {t.setTokenization(false);}
	t.setDoTaggingForAll(true); 
	System.out.println("################################################################");
	String[][] entitiesTagged = new String[1000][2];
	String tagResult = new String("");

	if (outputMode.equals("1")){
		tagResult=t.tagIOB(ret);}
	else if (outputMode.equals("2")){
		tagResult=t.tagSGML(ret);}
	else if (outputMode.equals("3")){
		
		entitiesTagged = t.getEntities(ret);}
	else {
		tagResult=t.tagABNER(ret);} 

	if (!outputMode.equals("3")) {
	System.out.println(tagResult);}
	else {
                tagResult = "";
		for (int i=0; i<entitiesTagged[0].length; i++) {
			System.out.println(entitiesTagged[1][i]+"\t["+entitiesTagged[0][i]+"]");
                        tagResult += entitiesTagged[1][i]+"\t["+entitiesTagged[0][i]+"]\n";
			}
		}
	// write the result to result.iob2 file
//	result.print(t.tagIOB(bigStr));
//	result.flush();
//	result.close();
	System.out.println("################################################################");
        return tagResult;
}

  
}
