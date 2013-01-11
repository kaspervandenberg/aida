
package katrenko.ws;

/* Collocation.java
 *
 * Created on 18 March 2006, 00:36
 *
 * This is CollocationService which provides the following functionality:
 *  - find N collocations (parameter max) 
 *        of a certain length (parameter ngram),
 *        with a minimum count (parameter min)
 *        in a background corpus (parameter data_train standing for the folder
 *                                where all training files are located)
 * - find collocations in a second corpus (data_test) w.r.t. background corpus
 *
 * If user's parameters are not correct (e.g., as a minimum count are used
 * symbols instead of numbers), a parameter is set to the default value.
 *
 * Default values for the parameters are the following:
 * collocation length (ngram)                          = 2
 * minimum count (min)                                 = 2
 * maximum number of collocations to be returned (max) = 1000
 *
 * Foreground corpus (data_test) is optional and if it is not provided, search
 * for collocations is performed in a background corpus (data_train) only.
 */


import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.lm.TokenizedLM;
import com.aliasi.util.Files;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.IOException;
import java.lang.Double;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import java.io.BufferedReader;
import java.io.FileReader;
/**
 *
 * @author Sophijka
 */
public class CollocationService {
    
    /** Creates a new instance of Collocation */
    public CollocationService() {
    }

   
    private static int MAX_NGRAM_REPORTING_LENGTH = 5;
   
    public String find_phrases(String data_train, String data_test, String ngram, String min, String max) {
         
        String str="<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        str=str+"<result>";
        
        int NGRAM_REPORTING_LENGTH; 
        int NGRAM;
        int MIN_COUNT;
        int MAX_COUNT;
        // read ngram length
        try {
          NGRAM = (int) (new Integer( ngram ).intValue());
          NGRAM_REPORTING_LENGTH = (int) (new Integer( ngram ).intValue());
        } catch(NumberFormatException ignored) {
          // if not, set ngram length to 2
          NGRAM = 2;NGRAM_REPORTING_LENGTH = 2;
          }
        str=str+"<ngram_length>" + NGRAM + "</ngram_length>";
        str=str+"<ngram_rep>" + NGRAM_REPORTING_LENGTH + "</ngram_rep>";
        // read minimum count of ngrams
         try {
           MIN_COUNT = (int) (new Integer( min ).intValue());
           } catch(NumberFormatException ignored) {
           // if not, set minimum count to 2
           MIN_COUNT = 2;
           }
        str=str+"<min_occurrence>"+MIN_COUNT+"</min_occurrence>";
        // read maximum number of collocations to be returned 
        try {
          MAX_COUNT = (int) (new Integer( max ).intValue());
          } catch(NumberFormatException ignored) {
        // if not, set maximum number of collocations to 1000
           MAX_COUNT = 1000;
           }
        str=str+"<max_return>"+MAX_COUNT+"</max_return>";
        File BACKGROUND_DIR = new File(data_train);
        File FOREGROUND_DIR = new File(data_test);
        String[] res_col;String res_string=new String();;
	IndoEuropeanTokenizerFactory tokenizerFactory 
	    = new IndoEuropeanTokenizerFactory();
        str = str + "<training>";
        str=str+"<dataset>" + "\"" + BACKGROUND_DIR + "\"" +"</dataset>";
	// Training background model
        try{
	TokenizedLM backgroundModel = buildModel(tokenizerFactory,
						 NGRAM,
						 BACKGROUND_DIR);
        
	backgroundModel.sequenceCounter().prune(3);

        // Assembling collocations in Training
	ScoredObject[] coll 
	    = backgroundModel.collocations(NGRAM_REPORTING_LENGTH,
					   MIN_COUNT,MAX_COUNT);

        res_col=new String[coll.length];
	res_col=report(coll);
        
        if (res_col.length>0)
        {
            str=str+"<collocations_number>"+res_col.length+"</collocations_number>";
        }
        else {str=str+"<collocations_number>0</collocations_number>";}
	// Output collocations in order of significance
        for (int i=0;i<res_col.length;i++)
        {
         str = str + "<collocation>";       
         int corr;corr=res_col[i].indexOf("Collocation: ");
         String cr;cr=res_col[i].substring(0, corr);        
         int incorr;incorr=res_col[i].indexOf("Score: ");
         str=str+"<phrase>"+res_col[i].substring(13, incorr-1)+"</phrase>";
         str=str+"<score>"+res_col[i].substring(incorr+7, res_col[i].length())+"</score>";
         str = str + "</collocation>";
        
         res_string=res_string+"\n"+res_col[i];}
         str = str + "</training>";
        // if foreground corpus is provided, look for collocations w.r.t. the background corpus
        if (data_test.length()!=0)
         try{
         str = str + "<testing>";
         str =str + "<dataset>" + "\"" + FOREGROUND_DIR + "\"" + "</dataset>";
	// Training foreground model
	TokenizedLM foregroundModel = buildModel(tokenizerFactory,
						 NGRAM,
						 FOREGROUND_DIR);
	foregroundModel.sequenceCounter().prune(3);

	// Assembling new terms in test vs. training
	ScoredObject[] newTerms 
	    = foregroundModel.newTerms(NGRAM_REPORTING_LENGTH,
				       MIN_COUNT,
				       MAX_COUNT,
				       backgroundModel);

	// Output new terms in order of signficance
	
        res_col=new String[newTerms.length];
	res_col=report(newTerms);
        
        if (res_col.length>0)
        {
            str=str+"<collocations_number>"+res_col.length+"</collocations_number>";
        }
        else {str=str+"<collocations_number>0</collocations_number>";}
        for (int i=0;i<res_col.length;i++)
        {
         str = str + "<collocation>";       
         int corr;corr=res_col[i].indexOf("Collocation: ");
         String cr;cr=res_col[i].substring(0, corr);        
         int incorr;incorr=res_col[i].indexOf("Score: ");
         str=str+"<phrase>"+res_col[i].substring(13, incorr-1)+"</phrase>";
         str=str+"<score>"+res_col[i].substring(incorr+7, res_col[i].length())+"</score>";
         str = str + "</collocation>";
         
         res_string=res_string+"\n"+res_col[i];}
	 str = str + "</testing>";	
         } catch (IOException ioe) {System.out.println("Can't find FRONT");
         }
        } catch (IOException ioe) {System.out.println("Can't find BACK");}
	// Search for colocations is finished
        str=str+"</result>";
        // you may uncomment the following piece of code if you wish to store
        // the output on a server
        /*try{
           BufferedWriter new_writer = new BufferedWriter(
           new FileWriter("C://Documents and Settings/Sophijka/Data/traincoll.xml"));
           new_writer.write(str);new_writer.newLine();
           new_writer.flush();new_writer.close();}
        catch(Exception ie) {System.out.println("Cannot create output file");}*/
        
        // return found collocations as XML
        return str;
    } 

    private static TokenizedLM buildModel(TokenizerFactory tokenizerFactory,
					  int ngram,
					  File directory) 
	throws IOException {

	String[] trainingFiles = directory.list();
	TokenizedLM model = 
	    new TokenizedLM(tokenizerFactory,
			    ngram);
	// Training on directory
		    
	for (int j = 0; j < trainingFiles.length; ++j) {
	    String text = Files.readFromFile(new File(directory,
						      trainingFiles[j]));
	    model.train(text);
	}
	return model;
    }

    private static String[] report (ScoredObject[] nGrams) {
        String[] result=new String[nGrams.length];
        int j=0;
        String dummy = new String();
	for (int i=0; i<nGrams.length; ++i){
	    double score = nGrams[i].score();
	    String[] toks = (String[]) nGrams[i].getObject();
            dummy = report_filter(score,toks);
            if (dummy.length()!=0) { result[j]=dummy; j++;}
            
	}
        String[] final_res=new String[j];
        for (int k=0;k<j;k++) {final_res[k]=result[k];}
        return final_res;
    }
    
    private static String report_filter(double score, String[] toks) {
	String accum = "";
        String res=new String();int flag=0;
	for (int j=0; j<toks.length; ++j) {
	    // if you look for capitalized words only, uncomment the following line
            //if (nonCapWord(toks[j])) return "";
            if (toks[j].length()<3) {flag = 1;}
            accum += " "+toks[j];
	}
        if (flag == 0)
        {String s = String.valueOf(score);
        res="Collocation:"+ accum + " Score: "+s;}
        else if (flag == 1) {res="";}
        return res;
    }

    private static boolean nonCapWord(String tok) {
	if (!Character.isUpperCase(tok.charAt(0)))
	    return true;
	for (int i = 1; i < tok.length(); ++i) 
	    if (!Character.isLowerCase(tok.charAt(i))) 
		return true;
	return false;
    }

}

