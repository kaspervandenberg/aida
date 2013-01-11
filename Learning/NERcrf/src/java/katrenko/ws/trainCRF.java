/*
 * trainCRF.java
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
/* ****************************************************************

Copyright (C) 2004 Burr Settles, University of Wisconsin-Madison,
Dept. of Computer Sciences and Dept. of Biostatistics and Medical
Informatics.

This file is part of the "ABNER (A Biomedical Named Entity
Recognizer)" system. It requires Java 1.4. This software is
provided "as is," and the author makes no representations or
warranties, express or implied. For details, see the "README" file
included in this distribution.

This software is provided under the terms of the Common Public
License, v1.0, as published by http://www.opensource.org. For more
information, see the "LICENSE" file included in this distribution.

**************************************************************** */


package katrenko.ws;
import abner.*;

import java.io.*;
import java.lang.*;
import java.util.*;


/**
 *
 * @author Admin
 */
public class trainCRF {
    String log = new String();
    /** Creates a new instance of trainCRF */
    public trainCRF() {
    }
    
    public String train(String inputData, String modelFile) {
        String log = new String();
	long startTime = System.currentTimeMillis();
	trainCRF te=new trainCRF();
        try{
	String NEW_TRAIN_FILE=te.pre_process(inputData);
        Trainer t = new Trainer();
	//t.train(args[0], args[1], new String[]{"PROTEIN"});
	t.train(NEW_TRAIN_FILE, modelFile);
	long endTime = System.currentTimeMillis();
	log = "The training procedure takes "+1.0*(endTime-startTime)/1000+" seconds.";
        }
        catch(Exception e){};	
        return log;
}

protected String pre_process(String inputfile) throws Exception
{

	String NEW_TRAIN_FILE="train_".concat(inputfile);
	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
	PrintWriter new_train;
	int blank_count=0;
	try{
	new_train= new PrintWriter(new FileOutputStream(NEW_TRAIN_FILE));

	String temp=br.readLine();
	while(temp!=null) {
		if(!temp.equals("")){
			blank_count=0;
			StringTokenizer st = new StringTokenizer(temp, "\t ");
			String word=st.nextToken();
			new_train.print(word);
			new_train.print("|");
			String tag=st.nextToken();
			new_train.print(tag);
			new_train.print(" ");
			temp=br.readLine();
		}
		else{
		temp=br.readLine();
		blank_count++;
		if(blank_count==1) new_train.println();
	}
}
new_train.flush();
new_train.close();
br.close();
}catch(Exception ex){
	System.out.println("Fatal Error: cannot create new train file");
}
return NEW_TRAIN_FILE;
}

}
