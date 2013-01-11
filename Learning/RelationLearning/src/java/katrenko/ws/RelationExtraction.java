/*
 * RelationExtraction.java
 *
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package katrenko.ws;

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

/**
 *
 * @author Sophia Katrenko
 */

class ArffHeaderTestR{
    public String[][] data;
    
    public ArffHeaderTestR(){
         data = new String[100000][9];
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

/** WEKA header of the training set */
class ArffHeaderTrainR{
    public FastVector classHeader;
    public FastVector wordHeader;
    public int context_size;
    public int num_classes;
    public int num_instances;
    public int num_att;
    public int uniqueCount;
    public int uniqueCountWord;
    public Hashtable hashtableWord; 
    public Hashtable hashtableClass; 
      
    /** Creates a constructor of ArffHeaderTrain*/
    public ArffHeaderTrainR(){
        classHeader = new FastVector();
        wordHeader = new FastVector();
        hashtableWord = new Hashtable(); 
        hashtableClass = new Hashtable(); 
        uniqueCount = 0; 
        uniqueCountWord = 0; 
    }

    /** Reads a class header from a log file */
    public void addClassHeader(String parsedClassHeader)
    {
        classHeader.addElement(parsedClassHeader);
        hashtableClass.put(new Integer(uniqueCount),parsedClassHeader);
        uniqueCount++;
      }
    
    /** Reads word header from log file */
    public void addWordHeader(String parsedWordHeader)
    {
        wordHeader.addElement(parsedWordHeader);
        hashtableWord.put(new Integer(uniqueCountWord),parsedWordHeader);
        uniqueCountWord++;             
    }
 }

/** Reads from the log file of a given model */
class LogUnMarshallerR extends DefaultHandler{
    
      public ArffHeaderTrainR deArffHeaderTrain;
      private String temp; 
      private String lastElement;
      private String tag;
      public int dummy;
      public int dummy2;
      private int end_counter;
      private BufferedWriter new_writer2;
      private StringBuffer buf = null;
      private StringBuffer bufClass = null;
      
      public LogUnMarshallerR() {}
    
      
      public synchronized void startDocument() 
       {deArffHeaderTrain = new ArffHeaderTrainR();dummy=0;dummy2=0;}
      
      public synchronized void endDocument(){}
      
      public synchronized void startElement(String namespaceURI, String localName, String qName, Attributes atts)
      throws org.xml.sax.SAXException {
    	   tag=qName;end_counter=1;
           if (tag.equals("attribute_value")) {buf = new StringBuffer();}
           if (tag.equals("class_value")) {bufClass = new StringBuffer();}
      }
      
      public synchronized void endElement(String namespaceURI, String localName, String qName)
      throws org.xml.sax.SAXException {
    	 end_counter=0;
         if (tag.equals("attribute_value")) {
           if (buf != null)
           {deArffHeaderTrain.addWordHeader(buf.toString());}
           buf = null;
        }
         else if (tag.equals("class_value")) {
           if (bufClass != null)
           { deArffHeaderTrain.addClassHeader(bufClass.toString());}
           bufClass = null;
        }
      }
      
      public synchronized void characters(char[] chars, int start, int length)
      {
           if (tag.equals("context_size")){
                String dum = new String(chars, start, length);    
                int a;
                if (!dum.trim().equals("")) {
                try {
                    a = (int) (new Integer( dum ).intValue());
                    deArffHeaderTrain.context_size = a;
                } catch (NumberFormatException ignored) {
                          a = 0;
                  }
                }
                
      }
          else 
              if (tag.equals("classes_number")){
                String dum = new String(chars, start, length);    
                if (!dum.trim().equals("")) {
                try {
                    deArffHeaderTrain.num_classes = (int) (new Integer( dum ).intValue());
                } catch (NumberFormatException ignored) {
                  }
                }
                
          }
          else if (tag.equals("instances_number")){
                String dum = new String(chars, start, length);    
                if (!dum.trim().equals("")) {
                 try {
                    deArffHeaderTrain.num_instances = (int) (new Integer( dum ).intValue());
                } catch (NumberFormatException ignored) {
                  }
                }
          }
         else if (tag.equals("attributes_number")){
                String dum = new String(chars, start, length);    
                if (!dum.trim().equals("")) {
                try {
                    deArffHeaderTrain.num_att = (int) (new Integer( dum ).intValue());
                } catch (NumberFormatException ignored) { 
                  }
                }
         }       
         else if (tag.equals("class_value"))
           {
              
                   temp = new String(chars, start, length);
                   if (!temp.trim().equals("")) {
                       if (bufClass != null) {bufClass.append(chars, start, length);} else return;
                }
          }
         else if (tag.equals("attribute_value")){
                temp = new String(chars, start, length);
                if (!temp.trim().equals(" ")){
                  if (buf != null) {buf.append(chars, start, length);} else return;             
                     dummy++;}
    	 }
           
      }
      
    public void ignorableWhitespace(char ch[], int start, int length)
     throws SAXException{ }
      
    public synchronized void parseString(String msgXML) throws Exception 	{
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        ParserAdapter pa = new ParserAdapter(sp.getParser());
        pa.setContentHandler(this);
        byte bytearray[] = msgXML.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytearray);			   
        pa.parse(new InputSource(bais));
        bais.close();
    }
} 

/** RelationExtraction class provides a functionality of extracting relation mentions given a test file and a model.
 *
 */
public class RelationExtraction {
    
    /** Creates a new instance of RelationExtraction */
    public RelationExtraction() {
    }
    public static LogUnMarshallerR handler;
   
    /** Annotates the <code>inputData</code> based on the <code>modelFile</code> and returns 
     * the the pairs of arguments where a given relation holds. 
     * <p>
     * @param inputData a test set
     * @param modelFile an absolute path to a model
     * @return pairs of arguments 
     * <p>
     * Note that the <code>inputData</code> has to be prsented as follows:
     * <p>
     * <ul>
     * <li> each word takes one line
     * <li> sentences are separated by newlines
     * <li> annotations of the named entities are done in the IOB format (a word and
     * its tag are separated by a space)
     * </ul>
     * <p>
     * Example: <p>
     * PROTEIN1 B-protein  <p>
     * regulates O  <p>
     * PROTEIN2 B-protein  <p>
     * . O  <p>
     * <p><p>
     * PROTEIN1a B-protein <p>
     * PROTEIN1b I-protein <p>
     * binds O  <p>
     * to O  <p>
     * PROTEIN2 B-protein  <p>
     * . O  <p>
     * <p>
     */
    public String annotateInput(String inputData, String modelFile){
      ArffHeaderTestR deArffHeader;
      int instanceCount = 0; 
      String lastElement;
      String classLabel;
      int root_counter = 0;
      int begin_counter = 0, end_counter = 0;
      int uniqueCount = 0;
      deArffHeader = new ArffHeaderTestR();
      Hashtable hashtableAtt = new Hashtable(); 
      String[][] pair2Predict = new String[100000][3];
      String[] sentenceNotStemmed = new String[100000];
      String prediction = new String("");
   
      handler = new LogUnMarshallerR(); String log_file = new String();
      log_file = modelFile + ".log";
      
      StringBuffer fileData = new StringBuffer(1000000);
        try{
           try{
               BufferedReader reader = new BufferedReader(
                new FileReader(log_file));
                char[] buf = new char[1024];
                int numRead=0;
                while((numRead=reader.read(buf)) != -1){
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
        }
        reader.close();
        }catch(Exception ex){ex.printStackTrace();}
  
           handler.parseString(fileData.toString());
        }
        catch(Exception ex) {ex.printStackTrace();}  
        
     StringBuffer contents = new StringBuffer();
     BufferedReader input = null;
     //stem BK
     Stemmer sm = new Stemmer(); String bk = new String();
        URL url = RelationLearning.class.getResource("/katrenko/ws/BK.txt");
        URI bkuri = null;
        try{
          bkuri = new URI(url.toString());
        } catch(Exception ex){System.err.println("Cannot locate background info!");}
      
    File bkFile = new File(bkuri);
    try{
        FileInputStream bkFileInput = new FileInputStream(bkFile);
        bk = sm.stemFile(bkFileInput);
        try{
        bkFileInput.close();
        }catch (Exception e) { e.printStackTrace();}  
    }catch (FileNotFoundException e) { e.printStackTrace();}  
    
    //tokenize stemmed background-knowledge list and add it to the ARFF header
    StringTokenizer st = new StringTokenizer(bk," ");
    String[] bkList = new String[100];
    int bkCount = 0;
    while (st.hasMoreTokens()) {
        String dum = st.nextToken();
        bkList[bkCount] = dum.replaceAll(" ",""); 
        bkCount++;        
    }
//    try {
      //use buffering, reading one line at a time
      //FileReader always assumes default encoding is OK!
//OLD      input = new BufferedReader( new FileReader(inputData) );
      //            input = new BufferedReader(new StringReader(inputData));
      String line = null; //not declared within while loop
    //  System.out.println("all data " + inputData);
      StringTokenizer stInput = new StringTokenizer(inputData,"\n");
    //String[] iList = new String[900000];
    //int bkCount = 0;
    
      String prote = new String();
      String memory = new String();
      String itag = new String();
      String btag = new String();
      btag = "no"; itag = "no";
      /*
      * readLine is a bit quirky :
      * it returns the content of a line MINUS the newline.
      * it returns null only for the END of the stream.
      * it returns an empty String if two newlines appear in a row.
      */
      //arrays for each sentence and for relation mentions
      String[] sentence = new String[1000000];
      int[] protein = new int[100000];
      String[] proteinWord = new String[100000];
      int i = 0; int j=0, k=0, p2p=0, sentenceCount=0, flag=0;
      while (stInput.hasMoreTokens()) {
        line = stInput.nextToken();
        //System.out.println("line is " + line);
        //bkList[bkCount] = dum.replaceAll(" ",""); 
        //bkCount++;        
   // }
   //   while (( line = input.readLine()) != null){
        if (line.equals(" ")) { sentenceCount++;
            for (int l=0;l<k-1;l++) {
                for (int m=l+1;m<k;m++) {
                    instanceCount++;
                    int countProtein1 = 0; String linkWord = new String("no"); 
                    String countProtein = new String(); countProtein = "no";
                    if (protein[l] < protein[m]) 
                     {
                        for (int r=(protein[l]+1);r<(protein[m]-1);r++) 
                            {for (int protpos=0; protpos<k-1; protpos++) 
                              {if (protein[protpos]==r) {countProtein = "yes";}}}
                        }
                    if (protein[l] < protein[m]) 
                     {for (int r=protein[l];r<(protein[m]-1);r++) {
                        for (int bkWord = 0; bkWord < bkCount; bkWord++)
                        {                            
                            if (sentence[r].equals(bkList[bkWord])) {linkWord = bkList[bkWord];}} 
                      }
                    }
                          pair2Predict[p2p][0] = sentenceNotStemmed[protein[l]-1];
                          pair2Predict[p2p][1] = sentenceNotStemmed[protein[m]-1];
                          pair2Predict[p2p][2] = linkWord;
                          String arg1 = new String(); String arg2 = new String();
                          arg1 = sentence[protein[l]-1]; arg2 = sentence[protein[m]-1];
                          if (!handler.deArffHeaderTrain.hashtableWord.containsValue(sentence[protein[l]-1])) { arg1 = "?";}
                          if (!handler.deArffHeaderTrain.hashtableWord.containsValue(sentence[protein[m]-1])) { arg2 = "?";}
                          p2p++;
                          if ((protein[l]-1)==0 && (protein[m]-1)==(i-1)) { 
                                    String s1 = new String(); String s2 = new String(); 
                                    s1 = sentence[protein[l]]; s2 = sentence[protein[m]-2]; 
                                    if (!handler.deArffHeaderTrain.hashtableWord.containsValue(sentence[protein[l]])) { s1 = "?";}
                                    if (!handler.deArffHeaderTrain.hashtableWord.containsValue(sentence[protein[m]-2])) { s2 = "?";}
                                    deArffHeader.addWord("-", arg1, s1, s2, arg2, "-", linkWord, countProtein, "no", instanceCount-1);
                                    }
                          else if ((protein[l]-1)==0) { 
                                String s1 = new String(); String s2 = new String(); String s3 = new String();
                                s1 = sentence[protein[l]]; s2 = sentence[protein[m]-2]; s3 = sentence[protein[m]];
                                if (!handler.deArffHeaderTrain.hashtableWord.containsValue(sentence[protein[l]])) { s1 = "?";}
                                if (!handler.deArffHeaderTrain.hashtableWord.containsValue(sentence[protein[m]-2])) { s2 = "?";}
                                if (!handler.deArffHeaderTrain.hashtableWord.containsValue(sentence[protein[m]])) { s3 = "?";}
                                deArffHeader.addWord("-", arg1, s1, s2, arg2, s3, linkWord, countProtein, "no", instanceCount-1);   
                               }
                          else if ((protein[m]-1)==(i-1)) {                                 
                                String s1 = new String(); String s2 = new String(); String s3 = new String();
                                s1 = sentence[protein[l]-2]; s2 = sentence[protein[l]]; s3 = sentence[protein[m]-2];
                                if (!handler.deArffHeaderTrain.hashtableWord.containsValue(sentence[protein[l]-2])) { s1 = "?";}
                                if (!handler.deArffHeaderTrain.hashtableWord.containsValue(sentence[protein[l]])) { s2 = "?";}
                                if (!handler.deArffHeaderTrain.hashtableWord.containsValue(sentence[protein[m]-2])) { s3 = "?";}
                                deArffHeader.addWord(s1, arg1, s2, arg2, s3, "-", linkWord, countProtein, "no", instanceCount-1);   
                                }
                          else {                                 
                                String s1 = new String(); String s2 = new String(); String s3 = new String(); String s4 = new String();
                                s1 = sentence[protein[l]-2]; s2 = sentence[protein[l]]; s3 = sentence[protein[m]-2]; s4 = sentence[protein[m]];
                                if (!handler.deArffHeaderTrain.hashtableWord.containsValue(sentence[protein[l]-2])) { s1 = "?";}
                                if (!handler.deArffHeaderTrain.hashtableWord.containsValue(sentence[protein[l]])) { s2 = "?";}
                                if (!handler.deArffHeaderTrain.hashtableWord.containsValue(sentence[protein[m]-2])) { s3 = "?";}
                                if (!handler.deArffHeaderTrain.hashtableWord.containsValue(sentence[protein[m]])) { s4 = "?";}
                                deArffHeader.addWord(s1, arg1, s2, s3, arg2, s4, linkWord, countProtein, "yes", instanceCount-1);                                   
                          }
                    //System.out.println("any proteins inbetween? " + countProtein);
                    //System.out.println("any words inbetween? " + linkWord);
                flag=0;
                }}
             k=0; i=0; 
    }
        else {
                st = new StringTokenizer(line," ");
                while (st.hasMoreTokens()) {
                  String dum = st.nextToken();
                  if ((dum.length()==1) && (!dum.equals("O"))) { 
                    if (!sm.stemToken(dum).equals("")) {sentence[i] = sm.stemToken(dum).replaceAll("\\n",""); sentence[i] = sentence[i].replaceAll(" ",""); 
                                                        sentenceNotStemmed[i] = dum;
                                                        memory = dum; i++;}
                    else {sentence[i] = dum; memory = dum; i++;}
                    }
                 if (dum.equals("O")) {                     
                                        if (itag.equals("yes")) { i--; i--; protein[k] = i+1; 
                                                                  proteinWord[k] = prote;                                                                  
                                                                  k++; btag = "no"; itag = "no"; 
                                                                  sentence[i] = prote; sentenceNotStemmed[i] = prote; i++;
                                                                  sentence[i] = memory; sentenceNotStemmed[i] = memory; i++; }
                                        if ((btag.equals("yes")) && (!itag.equals("yes"))) {protein[k] = i-1; 
                                                                                            proteinWord[k] = prote;
                                                                                            k++; btag = "no"; itag = "no"; }
                                        memory = "";
                                        }
                else if (dum.length()>1)
                {    
                if (dum.substring(0,2).equals("B-")) { prote = memory; btag = "yes"; itag = "no"; }
                else if (dum.substring(0,2).equals("I-")) { prote = prote + "_" + memory; i--; itag = "yes"; }
                else {
                      if (!sm.stemToken(dum).equals("")) { memory = dum; sentenceNotStemmed[i] = dum; sentence[i] = sm.stemToken(dum).replaceAll("\\n",""); 
                                                           sentence[i] = sentence[i].replaceAll(" ",""); i++;}
                      else {sentence[i] = dum; sentenceNotStemmed[i] = dum; memory = dum; i++;}
                     } 
                }
              }
            }
      }
    //added   
//OLD             input.close();
    //end_added  
/*    }
    catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
 
    catch (IOException ex){
      ex.printStackTrace();
    }
 **/
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
    Instances test_data = null;
    FastVector attributes = new FastVector(7);
    FastVector classValues = new FastVector();
    
    for (int count=1;count<9;count++)
     {
         attributes.addElement(new Attribute("att"+count,  handler.deArffHeaderTrain.wordHeader));         
     }
      attributes.addElement(new Attribute("Class", handler.deArffHeaderTrain.classHeader));
      String nameOfDataset="relationLearner";
      //System.out.println("INSTANCES " + instanceCount);
      test_data=new Instances(nameOfDataset,attributes,instanceCount);
      test_data.setClassIndex(test_data.numAttributes()-1);
      //System.out.println("#attributes " + (test_data.numAttributes()-1));
      for (int ct = 0; ct < instanceCount; ct++){
           Instance instance = new Instance(test_data.numAttributes());
            instance.setDataset(test_data);            
            instance.setValue(test_data.attribute("att1"), handler.deArffHeaderTrain.wordHeader.indexOf(deArffHeader.data[ct][0]));            
            instance.setValue(test_data.attribute("att2"), handler.deArffHeaderTrain.wordHeader.indexOf(deArffHeader.data[ct][1]));
            instance.setValue(test_data.attribute("att3"), handler.deArffHeaderTrain.wordHeader.indexOf(deArffHeader.data[ct][2]));
            instance.setValue(test_data.attribute("att4"), handler.deArffHeaderTrain.wordHeader.indexOf(deArffHeader.data[ct][3]));
            instance.setValue(test_data.attribute("att5"), handler.deArffHeaderTrain.wordHeader.indexOf(deArffHeader.data[ct][4]));
            instance.setValue(test_data.attribute("att6"), handler.deArffHeaderTrain.wordHeader.indexOf(deArffHeader.data[ct][5]));
            instance.setValue(test_data.attribute("att7"), handler.deArffHeaderTrain.wordHeader.indexOf(deArffHeader.data[ct][6]));
            instance.setValue(test_data.attribute("att8"), handler.deArffHeaderTrain.wordHeader.indexOf(deArffHeader.data[ct][7]));
            instance.setClassValue(handler.deArffHeaderTrain.classHeader.indexOf(deArffHeader.data[ct][8]));
            test_data.add(instance);
     }
      // write test data representation in ARFF format (for testing puroposes only!)
      /*
      try{ 
           BufferedWriter new_writer = new BufferedWriter(
           new FileWriter("C://Documents and Settings/Sophijka/Mijn documenten/RL4WS/testTNEW.arff"));
           new_writer.write(test_data.toString());new_writer.newLine();
           new_writer.flush();new_writer.close();
           }catch(Exception ex) {System.out.println("cant open"); ex.printStackTrace();}     
       */
      try{
      // deserialize model
        ObjectInputStream ois = new ObjectInputStream(
                          new FileInputStream(modelFile));
        Classifier cls = (Classifier) ois.readObject();
        
        Instances labeled = new Instances(test_data);
        //class label variable
        double clsLabel;       
        // label test set based on the deserialized model
        for (int ik = 0; ik < test_data.numInstances(); ik++) {
           clsLabel = cls.classifyInstance(test_data.instance(ik));
           labeled.instance(ik).setClassValue(clsLabel);} 
        Integer classV;
       
           for (int ins = 0; ins < labeled.numInstances(); ins++) {   
            int classV_copy = (int) (labeled.instance(ins).classValue()); 
            classV = new Integer(classV_copy); 
             if (handler.deArffHeaderTrain.hashtableClass.containsKey(classV)){
               //omit label if it equals "no"  and print it, otherwise
              if (!handler.deArffHeaderTrain.hashtableClass.get(classV).equals("no")) 
               {
                  prediction = prediction + pair2Predict[ins][0] + " " + pair2Predict[ins][1] + " " + pair2Predict[ins][2] + "\n";
                  //System.out.println("YES " + test_data.instance(ins).stringValue(6) + " " +pair2Predict[ins][0] + " " + pair2Predict[ins][1]);
                }
             }
            }
        ois.close();        
          }catch(Exception ex) {System.out.println("TEST5"); ex.printStackTrace();}     
    return prediction;    
   }
    
  /*   public static void main(String []args) {
         System.out.println("bla-bla-bla");
        File testFile = new File("C:\\Documents and Settings\\Sophijka\\Mijn documenten\\RL4WS\\test4relTEST.txt");       
        File modelFile = new File("C:\\Documents and Settings\\Sophijka\\Mijn documenten\\RL4WS\\modelREL1.mod");
        String ttest = "C:\\Documents and Settings\\Sophijka\\Mijn documenten\\RL4WS\\test.data";
        String mod = "C:\\Documents and Settings\\Sophijka\\Mijn documenten\\RL4WS\\modelREL1.mod";
        String test = new String(""); String model; String line;
        try{
            BufferedReader inputData = new BufferedReader(new FileReader(testFile));
            while ( (line = inputData.readLine()) != null)
             {
               test = new StringBuffer (test).append(line).toString();
             }
        }catch(Exception ie){System.out.println("Can't read demo file");}
        System.out.println("test is" + test);
        
        RelationExtraction relex = new RelationExtraction();
        String tt = relex.annotateInput(ttest, mod);
        System.out.println("returned:\n" + tt);
  }
     */
}
