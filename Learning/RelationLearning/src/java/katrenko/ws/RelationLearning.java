/*
 * RelationLearning.java
 *
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package katrenko.ws;

/**
 *
 * Sophia Katrenko
 */
import weka.core.*;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Utils;
import weka.classifiers.trees.J48;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.*;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.*;
import weka.classifiers.functions.SMO;
import weka.classifiers.Evaluation;
import weka.classifiers.Classifier;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToNominal;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.regex.*;
import java.text.*;
import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.net.URL;
import java.net.URI;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.*;

import java.util.Iterator;
import java.util.List;

import java.io.FileReader; 
import java.io.BufferedReader;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.regex.*;
import java.text.*;

/**
 *
 * @author Sophia Katrenko
 */
class ArffHeaderR{
    public String[][] data;
    public FastVector classHeader;
    public FastVector wordHeader;
    
    public ArffHeaderR(){
        classHeader = new FastVector();
        wordHeader = new FastVector();
        data = new String[100000][9];}
    
    public void addClassHeader(String parsedClassHeader)
    {
        classHeader.addElement(parsedClassHeader);
    }
    public void addWordHeader(String parsedWordHeader)
    {
        wordHeader.addElement(parsedWordHeader);
    }
    public void addWord(String leftArg1, String Arg1, String rightArg1, String leftArg2, String Arg2, String rightArg2,  String linkToken,  String otherProtein, String tokenClass, int position)
    {
        data[position][0] = leftArg1;
        data[position][1] = Arg1;
        data[position][2] = rightArg1;
        data[position][3] = leftArg2;
        data[position][4] = Arg2;
        data[position][5] = rightArg2;
        data[position][6] = linkToken;
        data[position][7] = otherProtein;
        data[position][8] = tokenClass;
    }
}

// writing log file as XML
class WriterToXMLR {

  public  static String rootElementName 
   = "training_info";
  public  static String methodElementName = "method";
  public  static String filenameElementName;
  public  static String dateElementName="date";
  public  static String contextSizeElementName="context_size";
  public  static String classElementName;
  public  static String accuracyElementName="accuracy";
  public  static String arffClassValuesElementName="class_values";
  public  static String arffAttributeElementName="attribute";
  public  static String xmlDeclaration 
   = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
  public  static int accuracy;
  public  static String arffClassValues;
  public  static String[] arffClass  = new String[100];
  public  static String[] arffAtt  = new String[1000000];
  public  static double[] prec = new double[100];
  public  static double[] recall = new double[100];
  public  static double[] fScore = new double[100];
  public  int instancesNumber;
  public FastVector classv = new FastVector();
  public  int attributeValuesNumber;
  public  int classesNumber;
  public  static String arffAttribute;
  public  static int contextSize;
  public  static String date;
  public  static String method;
  
  public WriterToXMLR(){}
  // The following info is written into XML file:
  // name of the file used for training
  // date
  // size of the context window
  // method
  // accuracy for each class
  // ARFF headers: for attributes & class values
  void writeToXML(String location) {
      try{
      BufferedWriter new_writer = new BufferedWriter( new FileWriter(location));
      new_writer.write(xmlDeclaration);new_writer.newLine();
      new_writer.write("<" + rootElementName + ">");  new_writer.newLine();
      new_writer.write("  <" + methodElementName +">");
      new_writer.write(method);
      new_writer.write("</" + methodElementName +">");new_writer.newLine();
      new_writer.write("  <" + dateElementName +">");
      new_writer.write(date);
      new_writer.write("</" + dateElementName +">");new_writer.newLine();
      new_writer.write(" <instances_number>");
      new_writer.write(String.valueOf(instancesNumber));
      new_writer.write("</instances_number>");new_writer.newLine();
      new_writer.write(" <attributes_number>");
      new_writer.write(String.valueOf(attributeValuesNumber));
      new_writer.write("</attributes_number>");new_writer.newLine();
      new_writer.write(" <classes_number>");
      new_writer.write(String.valueOf(classesNumber));
      new_writer.write("</classes_number>");new_writer.newLine();
      new_writer.write(" <classes>");new_writer.newLine();
      for (int k=0; k<classesNumber; k++)
      {
       new_writer.write("  <"+classv.elementAt(k)+">");new_writer.newLine();
       new_writer.write("   <precision>");
       new_writer.write(String.valueOf(prec[k]));
       new_writer.write("</precision>");new_writer.newLine();
       new_writer.write("   <recall>");
       new_writer.write(String.valueOf(recall[k]));
       new_writer.write("</recall>");new_writer.newLine();
       new_writer.write("   <F-score>");
       new_writer.write(String.valueOf(fScore[k]));
       new_writer.write("</F-score>");new_writer.newLine();
       new_writer.write("  </"+classv.elementAt(k)+">");new_writer.newLine();
      }
      new_writer.write(" </classes>");new_writer.newLine();
      for (int k=0; k<classesNumber; k++)
      {
       new_writer.write("<class_value>");
       new_writer.write("<![CDATA[");
       new_writer.write(arffClass[k]);
       new_writer.write("]]></class_value>");new_writer.newLine();
         
      }
      for (int k=0; k<attributeValuesNumber; k++)
      {
       new_writer.write("<attribute_value>");
       new_writer.write("<![CDATA[");
       new_writer.write(arffAtt[k]);
       new_writer.write("]]></attribute_value>");new_writer.newLine();
         
      } 
      new_writer.write("</" + rootElementName + ">");  new_writer.newLine();
      new_writer.flush();new_writer.close();
      }catch (IOException ioe) {System.err.println("Can't be printed"+ioe);}
  }

}

/**
 * RelationLearning uses a local context of the named entities (including the named
 * entities themselves) and the background knowledge to learn a model for
 * a binary relation detection. 
 * <p>
 * @author Sophia Katrenko
 * @version 1.0
 */
public class RelationLearning {
    private static Instances train_data = null;
    private static FastVector attributes=new FastVector(7);
    private static FastVector classValues=new FastVector();
      
    /** Creates a new instance of RelationLearning */
    public RelationLearning() {
    }
    
    protected void readInput(String input_data){
      ArffHeaderR deArffHeader;
      int instanceCount = 0; 
      String lastElement;
      String classLabel;
      int root_counter = 0;
      int begin_counter = 0, end_counter = 0;
      int uniqueCount = 0;
      deArffHeader = new ArffHeaderR();
      Hashtable hashtableAtt = new Hashtable(); 
      Hashtable hashtableClass = new Hashtable(); 
      hashtableClass.put(new Integer(1),"no");
      deArffHeader.addClassHeader("no");
      hashtableClass.put(new Integer(2),"yes");
      deArffHeader.addClassHeader("yes"); 
      hashtableAtt.put(new Integer(1),"no");
      hashtableAtt.put(new Integer(2),"yes");
      deArffHeader.addWordHeader("no"); 
      deArffHeader.addWordHeader("yes"); 
      String[] sentenceNotStemmed = new String[100000];
      StringBuffer contents = new StringBuffer();
      BufferedReader input = null;
     //stemming background knowledge
        Stemmer sm = new Stemmer(); String bk = new String();
        URL url = RelationLearning.class.getResource("/katrenko/ws/BK.txt");
        URI bkuri = null;
        try{
          bkuri = new URI(url.toString());
        } catch(Exception e){System.err.println("Cannot locate background info!" + e);}
      
    File bkFile = new File(bkuri);
    try{
        FileInputStream bkFileInput = new FileInputStream(bkFile);
        bk = sm.stemFile(bkFileInput);
        try{
            bkFileInput.close();
        }catch (Exception e) {e.printStackTrace(); }  
    }catch (FileNotFoundException e) {e.printStackTrace(); }  
    
    //tokenize stemmed background-knowledge list and add it to the ARFF header
    StringTokenizer st = new StringTokenizer(bk," ");
    String[] bkList = new String[100];
    int bkCount = 0;
    while (st.hasMoreTokens()) {
        String dum = st.nextToken();
        bkList[bkCount] = dum.replaceAll(" ",""); 
        //System.out.print("*" + bkList[bkCount] + "*");
        if (!hashtableAtt.containsValue(dum)){
            uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),dum);
            deArffHeader.addWordHeader(dum);}
        bkCount++;        
        
    }
    uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),"?"); deArffHeader.addWordHeader("?");
    
    StringTokenizer stInput = new StringTokenizer(input_data,"\n");
    //try {
      //input = new BufferedReader( new FileReader(input_data) );
      String line = null; //not declared within while loop
      
      //arrays for each sentence and for relation mentions
      String[] sentence = new String[10000];
      String[][] relation = new String[10000][2];
      int[] protein = new int[10000];
      int i = 0; int j=0, k=0,sentenceCount=0, flag=0;
      while (stInput.hasMoreTokens()) {
        line = stInput.nextToken();
        //System.out.println("line is " + line);
        //while (( line = input.readLine()) != null){
        if (line.equals(" ")) { sentenceCount++;
            //System.out.println("sentence #" + sentenceCount);
            for (int l=0;l<k;l++) {
                sentence[protein[l]-1] = sentenceNotStemmed[protein[l]-1];
            }
            // bubble sort of positions of named entities
            int temp1, temp2, temp;
            for (temp1 = (k - 1); temp1 >= 0; temp1--)
            {
                for (temp2 = 1; temp2 <= temp1; temp2++)
                {
                    if (protein[temp2-1] > protein[temp2])
                    {
                    temp = protein[temp2-1];
                    protein[temp2-1] = protein[temp2];
                    protein[temp2] = temp;
                    }
                }
            }
            //for (int l=0; l<k; l++) {System.out.println("sorted prot " + protein[l]);}
            for (int l=0; l<k-1; l++) {
                for (int m=l+1;m<k;m++) {
                    instanceCount++;
                    for (int r=0;r<j;r++){
                      
                      if ((relation[r][0].equals(String.valueOf(protein[l])) && relation[r][1].equals(String.valueOf(protein[m]))) || (relation[r][0].equals(String.valueOf(protein[m])) && relation[r][1].equals(String.valueOf(protein[l]))))
                      {
                          flag=1;}
                          
                    }
                    int countProtein1 = 0; String linkWord = new String("no"); 
                    String countProtein = new String(); countProtein = "no";
                    if (protein[l] < protein[m]) 
                     {
                        for (int r= (protein[l]+1); r<(protein[m]-1); r++){
                          for (int pr=0; pr < k; pr++){
                              if (protein[pr] == r) {countProtein = "yes";}
                            }   
                          }                            
                      }
                    if (protein[l] < protein[m]) 
                     {for (int r=protein[l];r<(protein[m]-1);r++) {
                        for (int bkWord = 0; bkWord < bkCount; bkWord++)
                        {
                            if (sentence[r].equals(bkList[bkWord])) {linkWord = bkList[bkWord];}} }
                    }
                    if (flag!=1){                          
                          if ((protein[l]-1)==0 && (protein[m]-1)==(i-1)) { 
                                if (!hashtableAtt.containsValue("-")){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),"-");deArffHeader.addWordHeader("-");}
                                if (!hashtableAtt.containsValue(sentence[protein[l]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]-1]); deArffHeader.addWordHeader(sentence[protein[l]-1]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-1]); deArffHeader.addWordHeader(sentence[protein[m]-1]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[l]])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]]); deArffHeader.addWordHeader(sentence[protein[l]]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]-2])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-2]);deArffHeader.addWordHeader(sentence[protein[m]-2]);} 
                                deArffHeader.addWord("-", sentence[protein[l]-1], sentence[protein[l]], sentence[protein[m]-2], sentence[protein[m]-1], "-", linkWord, countProtein, "no", instanceCount-1);   
                                }
                          else if ((protein[l]-1)==0) { 
                                if (!hashtableAtt.containsValue("-")){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),"-");deArffHeader.addWordHeader("-");}
                                if (!hashtableAtt.containsValue(sentence[protein[l]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]-1]); deArffHeader.addWordHeader(sentence[protein[l]-1]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-1]); deArffHeader.addWordHeader(sentence[protein[m]-1]);}                                 
                                if (!hashtableAtt.containsValue(sentence[protein[l]])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]]); deArffHeader.addWordHeader(sentence[protein[l]]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]]); deArffHeader.addWordHeader(sentence[protein[m]]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]-2])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-2]);deArffHeader.addWordHeader(sentence[protein[m]-2]);} 
                                deArffHeader.addWord("-", sentence[protein[l]-1], sentence[protein[l]], sentence[protein[m]-2], sentence[protein[m]-1], sentence[protein[m]], linkWord, countProtein, "no", instanceCount-1);   
                                }
                          else if ((protein[m]-1)==(i-1)) { 
                                if (!hashtableAtt.containsValue("-")){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),"-");deArffHeader.addWordHeader("-");}
                                if (!hashtableAtt.containsValue(sentence[protein[l]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]-1]); deArffHeader.addWordHeader(sentence[protein[l]-1]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-1]); deArffHeader.addWordHeader(sentence[protein[m]-1]);}                                 
                                if (!hashtableAtt.containsValue(sentence[protein[l]-2])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]-2]); deArffHeader.addWordHeader(sentence[protein[l]-2]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[l]])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]]); deArffHeader.addWordHeader(sentence[protein[l]]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]-2])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-2]); deArffHeader.addWordHeader(sentence[protein[m]-2]);}                                 
                                deArffHeader.addWord(sentence[protein[l]-2], sentence[protein[l]-1], sentence[protein[l]], sentence[protein[m]-2], sentence[protein[m]-1], "-", linkWord, countProtein, "no", instanceCount-1);   
                          }
                          else {
                                if (!hashtableAtt.containsValue(sentence[protein[l]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]-1]); deArffHeader.addWordHeader(sentence[protein[l]-1]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-1]); deArffHeader.addWordHeader(sentence[protein[m]-1]);}                                 
                                if (!hashtableAtt.containsValue(sentence[protein[l]])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]]); deArffHeader.addWordHeader(sentence[protein[l]]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]]); deArffHeader.addWordHeader(sentence[protein[m]]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]-2])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-2]); deArffHeader.addWordHeader(sentence[protein[m]-2]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[l]-2])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]-2]); deArffHeader.addWordHeader(sentence[protein[l]-2]);} 
                                deArffHeader.addWord(sentence[protein[l]-2], sentence[protein[l]-1], sentence[protein[l]], sentence[protein[m]-2], sentence[protein[m]-1], sentence[protein[m]], linkWord, countProtein, "no", instanceCount-1);   
                                }}
                    if (flag==1){                  
                              if ((protein[l]-1)==0 && (protein[m]-1)==(i-1)) { 
                                    if (!hashtableAtt.containsValue(sentence[protein[l]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]-1]); deArffHeader.addWordHeader(sentence[protein[l]-1]);} 
                                    if (!hashtableAtt.containsValue(sentence[protein[m]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-1]); deArffHeader.addWordHeader(sentence[protein[m]-1]);}                                 
                                    if (!hashtableAtt.containsValue("-")){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),"-"); deArffHeader.addWordHeader("-");} 
                                    if (!hashtableAtt.containsValue(sentence[protein[l]])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]]); deArffHeader.addWordHeader(sentence[protein[l]]);} 
                                    if (!hashtableAtt.containsValue(sentence[protein[m]-2])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-2]);deArffHeader.addWordHeader(sentence[protein[m]-2]);} 
                                    deArffHeader.addWord("-", sentence[protein[l]-1], sentence[protein[l]], sentence[protein[m]-2], sentence[protein[m]-1], "-", linkWord, countProtein, "yes", instanceCount-1);   
                                    }
                          else if ((protein[l]-1)==0) { 
                                if (!hashtableAtt.containsValue(sentence[protein[l]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]-1]); deArffHeader.addWordHeader(sentence[protein[l]-1]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-1]); deArffHeader.addWordHeader(sentence[protein[m]-1]);}                                 
                                if (!hashtableAtt.containsValue("-")){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),"-"); deArffHeader.addWordHeader("-");}   
                                if (!hashtableAtt.containsValue(sentence[protein[l]])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]]); deArffHeader.addWordHeader(sentence[protein[l]]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]]); deArffHeader.addWordHeader(sentence[protein[m]]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]-2])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-2]); deArffHeader.addWordHeader(sentence[protein[m]-2]);} 
                                deArffHeader.addWord("-", sentence[protein[l]-1], sentence[protein[l]], sentence[protein[m]-2], sentence[protein[m]-1], sentence[protein[m]], linkWord, countProtein, "yes", instanceCount-1);   
                                }
                          else if ((protein[m]-1)==(i-1)) { 
                                if (!hashtableAtt.containsValue(sentence[protein[l]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]-1]); deArffHeader.addWordHeader(sentence[protein[l]-1]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-1]); deArffHeader.addWordHeader(sentence[protein[m]-1]);} 
                                if (!hashtableAtt.containsValue("-")){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),"-"); deArffHeader.addWordHeader("-");} 
                                if (!hashtableAtt.containsValue(sentence[protein[l]-2])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]-2]); deArffHeader.addWordHeader(sentence[protein[l]-2]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[l]])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]]); deArffHeader.addWordHeader(sentence[protein[l]]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]-2])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-2]); deArffHeader.addWordHeader(sentence[protein[m]-2]);}                                                                 
                                deArffHeader.addWord(sentence[protein[l]-2], sentence[protein[l]-1], sentence[protein[l]], sentence[protein[m]-2],  sentence[protein[m]-1], "-", linkWord, countProtein, "yes", instanceCount-1);   
                                }
                          else {
                                if (!hashtableAtt.containsValue(sentence[protein[l]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]-1]); deArffHeader.addWordHeader(sentence[protein[l]-1]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]-1])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-1]); deArffHeader.addWordHeader(sentence[protein[m]-1]);}                                 
                                if (!hashtableAtt.containsValue(sentence[protein[l]])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]]); deArffHeader.addWordHeader(sentence[protein[l]]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]]); deArffHeader.addWordHeader(sentence[protein[m]]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[m]-2])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[m]-2]); deArffHeader.addWordHeader(sentence[protein[m]-2]);} 
                                if (!hashtableAtt.containsValue(sentence[protein[l]-2])){uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),sentence[protein[l]-2]); deArffHeader.addWordHeader(sentence[protein[l]-2]);}                                
                                deArffHeader.addWord(sentence[protein[l]-2], sentence[protein[l]-1], sentence[protein[l]], sentence[protein[m]-2], sentence[protein[m]-1], sentence[protein[m]], linkWord, countProtein, "yes", instanceCount-1);   
                                }}
                        //System.out.println("any proteins inbetween? " + countProtein);
                        //System.out.println("any words inbetween? " + linkWord);
                    flag=0;
                }}
             k=0; i=0; j=0;
    }   
        else if ((line.length()>12) && (line.substring(0, 8).equals("protein("))){
                
                st = new StringTokenizer(line," ");
                while (st.hasMoreTokens()) {
                String dum = st.nextToken();
                int arg = 0;
                arg = Integer.parseInt(dum.substring(8, dum.indexOf(")")));
                int flag1 = 0; int max = k;
                for(int position = 0; position < max; position++){
                    if (protein[position] == (arg-1))   { flag1 = 1;}
                }
                if (flag1 == 0)   { protein[k] = arg; k++; }
                flag1 = 0;
                }
        }
        else if ((line.length()>19) && (line.substring(0, 16).equals("relation_mention"))){              
                st = new StringTokenizer(line," ");
                while (st.hasMoreTokens()) {
                String dum = new String();
                dum = st.nextToken();
                relation[j][0] = dum.substring(dum.indexOf("(")+1, dum.indexOf(","));
                relation[j][1] = dum.substring(dum.indexOf(",")+1, dum.indexOf(")"));
                int arg1 = Integer.parseInt(relation[j][0])-1; int arg2 = Integer.parseInt(relation[j][1])-1;             
                j++;    
             }
            }
            else  { 
                    if (!sm.stemToken(line).equals("")) {
                        sentenceNotStemmed[i] = line;
                        sentence[i] = sm.stemToken(line).replaceAll("\\n",""); 
                        sentence[i] = sentence[i].replaceAll(" ",""); 
                        } 
                    else {sentence[i] = line;}
                    i++;}
      }
 /*   }
    catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
    catch (IOException ex){
      ex.printStackTrace();
    }*/
    /*finally {
      try {
        if (input!= null) {
          //flush and close both "input" and its underlying FileReader
          input.close();
        }
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
    }*/
     for (int count=1;count<9;count++)
     {
         attributes.addElement(new Attribute("att"+count,  deArffHeader.wordHeader));
     }
      attributes.addElement(new Attribute("Class", deArffHeader.classHeader));
      String nameOfDataset="relationLearner";
      train_data=new Instances(nameOfDataset,attributes,instanceCount);
      train_data.setClassIndex(train_data.numAttributes()-1);
      for (int ct = 0; ct < instanceCount; ct++){
           Instance instance = new Instance(train_data.numAttributes());
            instance.setDataset(train_data);            
            instance.setValue(train_data.attribute("att1"), deArffHeader.wordHeader.indexOf(deArffHeader.data[ct][0]));
            instance.setValue(train_data.attribute("att2"), deArffHeader.wordHeader.indexOf(deArffHeader.data[ct][1]));
            instance.setValue(train_data.attribute("att3"), deArffHeader.wordHeader.indexOf(deArffHeader.data[ct][2]));
            instance.setValue(train_data.attribute("att4"), deArffHeader.wordHeader.indexOf(deArffHeader.data[ct][3]));
            instance.setValue(train_data.attribute("att5"), deArffHeader.wordHeader.indexOf(deArffHeader.data[ct][4]));
            instance.setValue(train_data.attribute("att6"), deArffHeader.wordHeader.indexOf(deArffHeader.data[ct][5]));
            instance.setValue(train_data.attribute("att7"), deArffHeader.wordHeader.indexOf(deArffHeader.data[ct][6]));
            instance.setValue(train_data.attribute("att8"), deArffHeader.wordHeader.indexOf(deArffHeader.data[ct][7]));
            instance.setClassValue(deArffHeader.classHeader.indexOf(deArffHeader.data[ct][8]));
            train_data.add(instance);
     }
    
    }
    
    /** Creates a model <code>modelFile</code> based on the <code>inputData</code> and returns 
     * the results of the 10-fold cross-validation. 
     * <p>
     * @param inputData annotated training set
     * @return <code>modelFile</code> (log file <code>modelFile.log</code> is stored in the same directory as <code>modelFile</code>)     
     * <p>
     * Note that this is a supervised learner which means that
     * the <code>inputData</code> has to be annotated as follows:
     * <p>
     * all tokens/words are separated by newline
     * <p>
     * sentences are separated by the newline
     * <p>
     * The first line right after the last word in a sentence contains *all* available/detected named entities, the second
     * line lists all relation mentions. Annotations are done assuming the position of the first word in a sentence to be 1.
     * Examples: <p>
     * This<p>
     * is <p>
     * PROTEIN1 <p>
     * interacting <p>
     * with <p>
     * PROTEIN2 <p>
     * but <p>
     * not <p>
     * interacting <p>
     * with 
     * PROTEIN3 <p>
     * . <p>
     * protein(3) protein(6) protein(11)<p>
     * relation_mention(3,6)<p>
     *<p><p>
     * This <p>
     * is <p>
     * a <p>
     * test <p>
     * . <p>
     * <p>
     */
    public String learnRelation(String inputData, String modelFile){
        //result string
        String resultCrossVal = new String();
        resultCrossVal = "";
        //reading the annotations from the input file
        //File trainFile = new File(inputData);
        //RelationLearning rel = new RelationLearning();
        readInput(inputData);        
        String outp=new String();
        outp = "";
        double clsLabel;
        String perf = new String(); perf = "";
        double[] prec1 = new double[100];
        double[] recall1 = new double[100];
        double[] fScore1 = new double[100];
        String[] infoClass =new String[100]; 
        String outp_smo = new String(); outp_smo = "";
        SMO smo = new SMO();
        Evaluation eval_smo = null;
        try{
         eval_smo = new Evaluation(train_data);
         smo.buildClassifier(train_data);
         eval_smo.crossValidateModel(smo, train_data, 10, train_data.getRandomNumberGenerator(1));
         outp_smo = eval_smo.toSummaryString();
        } catch (Exception ie) {perf = perf + "<attention>Please, check your input data</attention>";}
        if (!outp_smo.equals("")) {outp = outp + outp_smo;}
        try{
            ObjectOutputStream oos = new ObjectOutputStream(
                           new FileOutputStream(modelFile));
            oos.writeObject(smo);
            oos.flush();
            oos.close();
        }catch (IOException io){System.out.println("Can't write into file" + modelFile);}        
        Date now = new Date();
        long nowLong = now.getTime();
        DateFormat df4 = DateFormat.getDateInstance(DateFormat.SHORT); 
        String s4 = df4.format(now);
        // write into XML
        WriterToXMLR writer = new WriterToXMLR();
        writer.method = "Support Vector Machines";
        writer.date = s4;
        writer.instancesNumber = train_data.numInstances();
        writer.classesNumber = train_data.numClasses();
        writer.attributeValuesNumber = train_data.attribute(1).numValues();
        for (int m=0; m < train_data.classAttribute().numValues(); m++)
         {
          String ind_value = new String();
          ind_value = train_data.classAttribute().value(m);
          writer.arffClass[m] = ind_value;
          writer.prec[m] = (Math.round((eval_smo.precision(m))*10000))/100;
          writer.recall[m] = (Math.round((eval_smo.recall(m))*10000))/100;
          writer.fScore[m] = (Math.round((eval_smo.fMeasure(m))*10000))/100;
         }
        for (int m=0;m< train_data.classAttribute().numValues();m++)
        {
            writer.classv.addElement(train_data.classAttribute().value(m));
     
        }
        for (int m=0;m < train_data.attribute(1).numValues();m++)
        {
        writer.arffAtt[m] = train_data.attribute(1).value(m);
        }
        writer.arffAttribute = train_data.attribute(1).toString();
        writer.arffClassValues = train_data.classAttribute().toString();
        writer.writeToXML(modelFile+".log");    
        perf = perf + "<performance_info>";
        perf = perf + "<date>" + s4 + "</date>";
        perf = perf + "<method>Support Vector Machines</method>";
        int corr; corr = outp_smo.indexOf("Incorrectly");
        String cr; cr = outp_smo.substring(0, corr).substring(31);
        int incorr; incorr = outp_smo.indexOf("Kappa");
        String inc; inc = outp_smo.substring(corr, incorr).substring(33);
        int kappa; kappa = outp_smo.indexOf("Mean");
        String kap; kap = outp_smo.substring(incorr,kappa).substring(17);
        int mean; mean = outp_smo.indexOf("Root mean");
        String mae; mae = outp_smo.substring(kappa,mean).substring(21);
        int root; root = outp_smo.indexOf("Relative");
        String rt; rt = outp_smo.substring(mean,root).substring(25);
        int rel; rel = outp_smo.indexOf("Root relative");
        String rl; rl = outp_smo.substring(root,rel).substring(25);
        int rootrel; rootrel = outp_smo.indexOf("Total Number");
        String rlrt; rlrt = outp_smo.substring(rel,rootrel).substring(30);
        perf = perf + "<correctly_classified>" + cr + "</correctly_classified>";
        perf = perf + "<incorrectly_classified>" + inc + "</incorrectly_classified>";
        perf = perf + "<kappa_statistics>" + kap + "</kappa_statistics>";
        perf = perf + "<mean_absolute_error>" + mae + "</mean_absolute_error>";
        perf = perf + "<root_mean_squared_error>" + rt + "</root_mean_squared_error>";
        perf = perf + "<relative_absolute_error>" + rl + "</relative_absolute_error>";
        perf = perf + "<root_relative_squared_error>" + rlrt + "</root_relative_squared_error>";        
        for (int m=0;m<train_data.classAttribute().numValues();m++)
            {
            String ind_value = new String();
            ind_value = train_data.classAttribute().value(m);
            perf = perf + "<class>";
            perf = perf + "<class_value>" + ind_value + "</class_value>";
            perf = perf + "<precision>" + (Math.round((eval_smo.precision(m))*10000))/100.00 + "%</precision>";
            perf = perf + "<recall>" + (Math.round((eval_smo.recall(m))*10000))/100.00  + "%</recall>";
            perf = perf + "<F-measure>" + (Math.round((eval_smo.fMeasure(m))*10000))/100.00  + "%</F-measure>";
            perf = perf + "</class>";
            }
        perf = perf + "</performance_info>";
        return perf;

//        return resultCrossVal;
    }
    
 //   public static void main(String []args) {
 //        String testFile = new String("C:\\Documents and Settings\\Sophijka\\Mijn documenten\\RL4WS\\TEST.txt");
 //        RelationLearning rel = new RelationLearning();
 //        System.out.println("returned " + rel.learnRelation(testFile, "C:\\Documents and Settings\\Sophijka\\Mijn documenten\\RL4WS\\modelREL1.mod"));
 // }
    
}
