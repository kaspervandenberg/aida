/*
 * TestModel.java
 *
 * Created on 19 March 2006, 22:23
 *
 * TestModel is used to label test set according to the information stored in
 * a model supplied by a user
 * 
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
import weka.classifiers.Evaluation;
import weka.classifiers.Classifier;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToNominal;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.SAXException;

/**
 *
 * @Sophia Katrenko & Pieter Adriaans
 */


class ArffHeaderTrain{
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
      
    public ArffHeaderTrain(){
        classHeader = new FastVector();
        wordHeader = new FastVector();
        hashtableWord = new Hashtable(); 
        hashtableClass = new Hashtable(); 
        uniqueCount = 0; 
        uniqueCountWord = 0; 
    }

    //read class header from log file
    public void addClassHeader(String parsedClassHeader)
    {
        classHeader.addElement(parsedClassHeader);
        hashtableClass.put(new Integer(uniqueCount),parsedClassHeader);
        uniqueCount++;
      }
    
    //read word header from log file
    public void addWordHeader(String parsedWordHeader)
    {
        wordHeader.addElement(parsedWordHeader);
        hashtableWord.put(new Integer(uniqueCountWord),parsedWordHeader);
        uniqueCountWord++;             
    }
 }

// write test data set in array of strings

 class ArffTest{
    public String[][] data;
    public String[][] dataOrig;
    public ArffTest(){
        data=new String[100000][2];dataOrig=new String[100000][2];}
    public void addWord(String token, String tokenClass, int position)
     {
        data[position][0]=token;
        data[position][1]=tokenClass;
      }
    public void addWordOrig(String token, String tokenClass, int position)
     {
        dataOrig[position][0]=token;
        dataOrig[position][1]=tokenClass;
     }
}  

class LogUnMarshaller extends DefaultHandler{
    
      public ArffHeaderTrain deArffHeader;
      private String temp; 
      private String lastElement;
      private String tag;
      public int dummy;
      public int dummy2;
      private int end_counter;
      private BufferedWriter new_writer2;
      private StringBuffer buf = null;
      private StringBuffer bufClass = null;
      
      public LogUnMarshaller() {}
    
      public synchronized void startDocument() 
       {deArffHeader=new ArffHeaderTrain();dummy=0;dummy2=0;}
      
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
           {deArffHeader.addWordHeader(buf.toString());}
           buf = null;
        }
         else if (tag.equals("class_value")) {
           if (bufClass != null)
           { deArffHeader.addClassHeader(bufClass.toString());
           }
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
                    deArffHeader.context_size = a;
                } catch (NumberFormatException ignored) {
                          a = 0;
                  }
                }
                
      }
          else if (tag.equals("classes_number")){
                String dum = new String(chars, start, length);    
                if (!dum.trim().equals("")) {
                try {
                    deArffHeader.num_classes = (int) (new Integer( dum ).intValue());
                } catch (NumberFormatException ignored) {
                  }
                }
                
          }
          else if (tag.equals("instances_number")){
                String dum = new String(chars, start, length);    
                if (!dum.trim().equals("")) {
                 try {
                    deArffHeader.num_instances = (int) (new Integer( dum ).intValue());
                } catch (NumberFormatException ignored) {
                  }
                }
          }
         else if (tag.equals("attributes_number")){
                String dum = new String(chars, start, length);    
                if (!dum.trim().equals("")) {
                try {
                    deArffHeader.num_att = (int) (new Integer( dum ).intValue());
                } catch (NumberFormatException ignored) { // nfe.printStackTrace();
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
    }
}   

      
// read data froma test set
class SaxUnMarshaller extends DefaultHandler{
    
      public ArffTest ArffWords;
      public int instanceCount;
      public LogUnMarshaller handler;
      private String temp; 
      private String lastElement;
      private String classLabel;
      private int root_counter=0;
      private int begin_counter=0, end_counter=0;
      private int uniqueCount=0;
      private int uniqueCountClass=0;
    
      public SaxUnMarshaller() {}
    
      public void readLog(String log_file)
      {
        handler=new LogUnMarshaller();
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
        }catch(Exception ie){System.out.println("Can't read log file");}
  
           handler.parseString(fileData.toString());
        }
        catch(Exception i) {System.out.println("Can't parse a log file!");}
      }
      
      public void startDocument()
       {ArffWords = new ArffTest(); root_counter=0;instanceCount=0;}
      
      public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
      throws org.xml.sax.SAXException {
    	  begin_counter=1;end_counter=0;
    	  if (qName.equals("document") || qName.equals("DOC") || qName.equals("DOC_ID") || qName.equals("set") || qName.equals("article") || qName.equals("articleinfo")) 
           {root_counter=0;} else {root_counter=1;}
    	  classLabel=qName;
      }
      
      public void endElement(String namespaceURI, String localName, String qName)
      throws org.xml.sax.SAXException {
    	  end_counter=1;begin_counter=0;
        } 
      
      public void characters(char[] chars, int start, int length)
      {
    	  if ((root_counter==0) || (end_counter==1))
    	  {	
    	  temp = new String(chars, start, length);
    	  String temp_copy = temp.trim();
    	  if (temp_copy.length()>0) 
    	   {
            StringTokenizer tokenizer = new StringTokenizer(temp_copy);
            while (tokenizer.hasMoreTokens())
    	      {
    	       instanceCount++; 
               String data_token=new String();
               data_token=tokenizer.nextToken();
               //ADDED System.out.println(data_token);
               if (handler.deArffHeader.hashtableWord.containsValue(data_token))
               {ArffWords.addWord(data_token, "no", instanceCount-1);
                ArffWords.addWordOrig(data_token, "no", instanceCount-1);}
               else {
                     ArffWords.addWord("?", "no", instanceCount-1);
                     ArffWords.addWordOrig(data_token, "no", instanceCount-1);
                    }
    	     }
            }
    	  }
          //If token has a label, check whether this label is in the header of training set
    	  else if ((begin_counter==1) && (end_counter==0))
    	  {
    		  instanceCount++; 
    		  temp = new String(chars, start, length);
    		  String temp_copy = temp.trim();
              if (temp_copy.length()>0) 
    	        {                  
                 if ((handler.deArffHeader.hashtableWord.containsValue(temp_copy)) && (handler.deArffHeader.hashtableClass.containsValue(classLabel)))
                  {ArffWords.addWord(temp_copy, classLabel, instanceCount-1);
                   ArffWords.addWordOrig(temp_copy, "no", instanceCount-1);}
                 else if ((handler.deArffHeader.hashtableWord.containsValue(temp_copy)) && (!handler.deArffHeader.hashtableClass.contains(classLabel)))
                  {ArffWords.addWord(temp_copy, "no", instanceCount-1);
                   ArffWords.addWordOrig(temp_copy, "no", instanceCount-1);}
                 else {ArffWords.addWord("?", "no", instanceCount-1);
                       ArffWords.addWordOrig(temp_copy, "no", instanceCount-1);}
              }
    	  }
      }
       
}

// applying model to the test data set
public class TestModel {
    
    private Instances test_data=null;
    private int focus;
    private int context;
    private Hashtable hashtableClass = new Hashtable(); 
    private SaxUnMarshaller handlerTest;
    /** Creates a new instance of TestModel */
    public TestModel() {}
    
    private void makeSlidingWindow(String doc, String log, String inp)
    {
    int countClass;
    try{
    // read information about the model from a log file    
    handlerTest=new SaxUnMarshaller();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    handlerTest.readLog(log);
    // parse test set
    if (inp.equals("text")) {
    StringBuffer textData = new StringBuffer(doc);
    
    ByteArrayInputStream bais = new ByteArrayInputStream(textData.toString().getBytes());
     //InputSource bais = new InputSource(inp);
    //ADDED System.out.println("BAIS" + bais.toString());
    saxParser.parse(bais, handlerTest); 
    }
    else 
    saxParser.parse(doc, handlerTest); 
    int k=0;

     //=================================create test set=============        
     // size of the context window
     int finsize = handlerTest.handler.deArffHeader.context_size*2+1;
     // size of the context supplied by a user
     context = handlerTest.handler.deArffHeader.context_size;
     // position of word in a focus
     focus = handlerTest.handler.deArffHeader.context_size+1;
     countClass = 0;
     FastVector new_attributes=new FastVector(finsize+1);
     FastVector new_classValues=new FastVector();
     //write class information into a hashtable
     for (int dd=0;dd<handlerTest.handler.deArffHeader.classHeader.size();dd++)
     {
      hashtableClass.put(new Integer(countClass),handlerTest.handler.deArffHeader.classHeader.elementAt(dd).toString());
      countClass++;   
     }
     
     // check whether there are any duplicates ina word header - for testing pusposes only
     /*
     String tt = new String();
     for (int dd=0;dd<handlerTest.handler.deArffHeader.wordHeader.size();dd++)
     {
         tt = handlerTest.handler.deArffHeader.wordHeader.elementAt(dd).toString();
         for (int dj=dd+1;dj<handlerTest.handler.deArffHeader.wordHeader.size();dj++)
         {
             if (tt.equals(handlerTest.handler.deArffHeader.wordHeader.elementAt(dj).toString()))
             {System.out.println("DUPLICATE" + tt);}
         }
     }
     */

     //set ARFF headers from the training set as headers as headers for the test set
     for (int count=0;count<finsize;count++)
     {
         new_attributes.addElement(new Attribute("att"+count,  handlerTest.handler.deArffHeader.wordHeader));
     } 
     new_attributes.addElement(new Attribute("Class", handlerTest.handler.deArffHeader.classHeader));     
     // set a nema of a data set
     String new_nameOfDataset="SlidingWindow";
     test_data=new Instances(new_nameOfDataset,new_attributes,handlerTest.instanceCount-finsize);
     test_data.setClassIndex(test_data.numAttributes()-1);
     // fill in a weka test data set
     int dk;int wsize=focus-2;int ind, indClass;
     for (int j=0;j<=handlerTest.instanceCount-finsize;j++){
         dk=j;Instance new_instance = new Instance(test_data.numAttributes());
        for (int ct=0; ct<finsize; ct++)
        {
            new_instance.setDataset(test_data);     
            ind = handlerTest.handler.deArffHeader.wordHeader.indexOf(handlerTest.ArffWords.data[dk][0]);
            indClass = handlerTest.handler.deArffHeader.classHeader.indexOf(handlerTest.ArffWords.data[dk][1]);
            new_instance.setValue(test_data.attribute("att"+ct), ind);
            // set class value for a word in focus
            if (ct==wsize) {new_instance.setClassValue(indClass);}
            dk++;
        }
         test_data.add(new_instance);
     }
      //save input data in ARFF format - for testing purposes only
      /*BufferedWriter new_writer = new BufferedWriter(
           new FileWriter("C://Documents and Settings/Sophijka/Data/train/testNEW.arff"));
           new_writer.write(test_data.toString());new_writer.newLine();
           new_writer.flush();new_writer.close();
      */
   //=================================END of a test set in ARFF format=============        
  
   } catch(Exception ex) {ex.printStackTrace();}
}

    public String test_model(java.lang.String model_file, java.lang.String test_file, java.lang.String input_type) {
       //performance 
       String  prediction = new String();
       prediction = "";
       //class label variable
       double clsLabel;
       try{
        // locate log file
        String log_file = model_file + ".log";
        // create sliding window over test set
        makeSlidingWindow(test_file, log_file, input_type);
        test_data.setClassIndex(test_data.numAttributes()-1);
        Instances labeled = new Instances(test_data);
        
        // deserialize model
        ObjectInputStream ois = new ObjectInputStream(
                          new FileInputStream(model_file));
        Classifier cls = (Classifier) ois.readObject();
        ois.close();
        int counter=context;
        // label test set based on the deserialized model
        for (int i = 0; i < test_data.numInstances(); i++) {
           clsLabel = cls.classifyInstance(test_data.instance(i));
           labeled.instance(i).setClassValue(clsLabel);} 

        // write classified instances into XML
           
           Integer classV;
           prediction = prediction + "<?xml version=\"1.0\" encoding=\"utf-8\"?><document>";
           for (int i = 0; i < labeled.numInstances(); i++) {   
            if (i==0) {
                for (int m=0;m<context;m++){
                  prediction = prediction + handlerTest.ArffWords.dataOrig[m][0] + " ";              
                }
            }
            int classV_copy = (int) (labeled.instance(i).classValue()); 
            classV = new Integer(classV_copy); 
             if (hashtableClass.containsKey(classV)){
               //omit label if it equals "no"  and print it, otherwise
              if (!hashtableClass.get(classV).equals("no")) 
               {
                  prediction = prediction + "<" + hashtableClass.get(classV) + ">";
                  prediction = prediction + handlerTest.ArffWords.dataOrig[counter][0] + "</";
                  prediction = prediction + hashtableClass.get(classV) + "> ";
                  }
             
             else {
                  prediction = prediction + handlerTest.ArffWords.dataOrig[counter][0] + " ";                  
                  //prediction = prediction + labeled.instance(i).stringValue(context) + " ";
                 }
             }
            if (i==labeled.numInstances()-1) {
                for (int m=context+1;m<context*2;m++){
                   prediction = prediction + handlerTest.ArffWords.dataOrig[labeled.numInstances()-1+m][0] + " "; 
           
                }
            }
            counter++;
            }
           prediction = prediction + "</document>";
           
      //save input data in ARFF format - for testing purposes only
      /*BufferedWriter new_writer = new BufferedWriter(
           new FileWriter("C://Documents and Settings/Sophijka/Data/train/testNEWlabel.arff"));
           new_writer.write(labeled.toString());new_writer.newLine();
           new_writer.flush();new_writer.close();
        */   
      }catch(Exception ex) {ex.printStackTrace();}
     
      return prediction;
    }
   
}