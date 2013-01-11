/*
 * LearnModel.java
 *
 * Created on March 20, 2006, 5:22 PM
 *
 * 
 * Input data: annotated data in XML format as, e.g.:
 *
 * <doc>In contrast to the genes of the <protein>MAGE-A</protein>, 
 * <protein>MAGE- B</protein> and <protein>MAGE-C</protein>
 * clusters, <protein>MAGED2</protein> is expressed ubiquitously. </doc>
 * 
 * Please, do not forget to include a root tag <doc>. All other tags are
 * considered to be class labels (such as protein in the example above)
 *
 * Parameters to be specified:
 *   train_set (full path to the training set)
 *   context_window (size of the context window, e.g. 3 which means that 
 *              3 elements to the left and to the right of a given element are
 *              considered)
 *   mode (automatic or interactive - present version has automatic only)
 *   model_file (path and filename where the model will be stored. Please note 
 *               that model will be stored as WEKA model plus a log file containing
 *               information necessary for the aplying this model in future, by using
 *               TestModel web service)
 */

package katrenko.ws;

/**
 *
 * @author Sophia Katrenko
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


/** class containing header information for Weka*/
class ArffHeader{
    public String[][] data;
    public FastVector classHeader;
    public FastVector wordHeader;
    
    public ArffHeader(){
        classHeader=new FastVector();
        wordHeader=new FastVector();
        data=new String[100000][2];}
    
    public void addClassHeader(String parsedClassHeader)
    {
        classHeader.addElement(parsedClassHeader);
        //System.out.println("class "+parsedClassHeader);
    }
    public void addWordHeader(String parsedWordHeader)
    {
        wordHeader.addElement(parsedWordHeader);
    }
    public void addWord(String token, String tokenClass, int position)
    {
        data[position][0]=token;
        data[position][1]=tokenClass;
    }
}

/** unmarshalling (reading) the training set (from XML) */
class SaxUnMarshaller extends DefaultHandler{
    
      public ArffHeader deArffHeader;
      public int instanceCount;
      private String temp; 
      private String lastElement;
      private String classLabel;
      private int root_counter=0;
      private int begin_counter=0, end_counter=0;
      private int uniqueCount=0;
      private int uniqueCountClass=0;
      private Hashtable hashtableAtt = new Hashtable(); 
      private Hashtable hashtableClass = new Hashtable(); 
   
      public SaxUnMarshaller() {}
    
      public void startDocument()
       {root_counter=0;deArffHeader=new ArffHeader();instanceCount=0;}
      
      public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
      throws org.xml.sax.SAXException {
    	  begin_counter=1;end_counter=0;
    	  if (qName.equals("document") || qName.equals("doc")) 
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
 
               if (hashtableAtt.containsValue(data_token))
               {}
               // store information in a WordHeader (attribute)
               else {uniqueCount++; 
                     hashtableAtt.put(new Integer(uniqueCount),data_token);
                     deArffHeader.addWordHeader(data_token);
                    }
               if (hashtableClass.containsValue("no"))
               {}
               // store class value information in a ClassHeader (class attribute)
               else {
                     uniqueCountClass++;
                     hashtableClass.put(new Integer(uniqueCountClass),"no");
                     deArffHeader.addClassHeader("no");
                     }
               deArffHeader.addWord(data_token, "no", instanceCount-1);
    	     }
    		  }
    	  }
    	  else if ((begin_counter==1) && (end_counter==0))
    	  {
    		  instanceCount++; 
    		  temp = new String(chars, start, length);
    		  String temp_copy = temp.trim();
                  if (hashtableAtt.containsValue(temp_copy))
               {}
               else {uniqueCount++; hashtableAtt.put(new Integer(uniqueCount),temp_copy);
                     deArffHeader.addWordHeader(temp_copy);}
               if (hashtableClass.containsValue(classLabel))
               {}
               else {
                     uniqueCountClass++;
                     hashtableClass.put( new Integer(uniqueCountClass),classLabel);
                     deArffHeader.addClassHeader(classLabel);}
               deArffHeader.addWord(temp_copy, classLabel, instanceCount-1);   
    	  }
      }
   //   public void endDocument()
   //    {deArffHeader.addWordHeader("?");uniqueCount++;}
      public void endPrefixMapping(String prefix){}
      public void ignorableWhitespace(char[] ch, int start, int length){}
      public void processingInstruction(String target, String data){}
      public void setDocumentLocator(Locator locator){}
      public void skippedEntity(String name){}    
      public void startPrefixMapping(String prefix, String uri){}
    
       
}


/** writes log file as XML (is needed when a model is applied to a new data set) */
class WriterToXML {

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
  
  /**creates a constructor of WritertoXML*/
  public WriterToXML(){}
  
  /** The following info is written into XML file:
  * name of the file used for training
  * date
  * size of the context window
  * method
  * accuracy for each class
  * ARFF headers: for attributes & class values
   */
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
      new_writer.write("  <" + contextSizeElementName +">");
      new_writer.write(String.valueOf(contextSize));
      new_writer.write("</" + contextSizeElementName +">");new_writer.newLine();
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
       new_writer.write(String.valueOf(prec[k]*100));
       new_writer.write("</precision>");new_writer.newLine();
       new_writer.write("   <recall>");
       new_writer.write(String.valueOf(recall[k]*100));
       new_writer.write("</recall>");new_writer.newLine();
       new_writer.write("   <F-score>");
       new_writer.write(String.valueOf(fScore[k]*100));
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
      }catch (IOException ioe) {System.out.println("Can't be printed"+ioe);}
  }

}

/** LearnModel web service provides the following functionality:
 *   learns a model given the annotated corpus and stores it
 *   together with its log file. Data is represented using a sliding window approach.*/ 
public class LearnModel {
    
    /** Creates a new instance of LearnModel */
    public LearnModel() {
    }
    private Instances train_data=null;
    private Instances new_train_data=null;

    /**Uses a sliding window approach to create data representation used for learning a model
     *<p>
     *@param doc annotated corpus
     *@param wsize context size (for a sliding window)
     *@param inp input data (obtained from {@link train_model} method)
     */
    private void makeSlidingWindow1(String doc, int wsize, String inp)
    {
       
   try{
    SaxUnMarshaller handler=new SaxUnMarshaller();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    if (inp.equals("text")) {
    StringBuffer textData = new StringBuffer(doc);
    ByteArrayInputStream bais = new ByteArrayInputStream(textData.toString().getBytes());
    saxParser.parse(bais, handler); 
    }
    else 
    saxParser.parse(doc, handler);
    // initialization: window size, final size, position of focus 
     int k=0;

     //=================================TrainingSet=============        
     int finsize=wsize*2+1; int focus=wsize+1;
     FastVector new_attributes=new FastVector(finsize+1);
     FastVector new_classValues=new FastVector();
     for (int count=0;count<finsize;count++)
     {
         new_attributes.addElement(new Attribute("att"+count,  handler.deArffHeader.wordHeader));
     }
     new_attributes.addElement(new Attribute("Class", handler.deArffHeader.classHeader));
     String new_nameOfDataset="SlidingWindow";
     new_train_data=new Instances(new_nameOfDataset,new_attributes,handler.instanceCount-finsize);
     new_train_data.setClassIndex(new_train_data.numAttributes()-1);
     int dk;
     // create sliding window instances
     for (int j=0;j<handler.instanceCount-finsize;j++){
         dk=j;Instance new_instance = new Instance(new_train_data.numAttributes());
        for (int ct=0; ct<finsize; ct++)
        {
            new_instance.setDataset(new_train_data);            
            new_instance.setValue(new_train_data.attribute("att"+ct),handler.deArffHeader.wordHeader.indexOf(handler.deArffHeader.data[dk][0]));
            if (ct==wsize) {new_instance.setClassValue(handler.deArffHeader.classHeader.indexOf(handler.deArffHeader.data[dk][1]));}
            dk++;
        }
         new_train_data.add(new_instance);
     }
      //save input data in ARFF format - for testing purposes only
     /*  BufferedWriter new_writer = new BufferedWriter(
           new FileWriter("C://Documents and Settings/Sophijka/Data/train/corpus2NEW_again.arff"));
           new_writer.write(new_train_data.toString());new_writer.newLine();
           new_writer.flush();new_writer.close(); */
  
   //=================================END---TrainingSet=============        
   
   
   } catch(Exception ex) {ex.printStackTrace();}
     }

   /**
    * Learns a model given the input data. 
    * Input: annotated text data in XML format as in, e.g.:
    * <p>
    * <code>&lt;doc&gt;In contrast to the genes of the &lt;protein&gt;MAGE-A&lt;/protein&gt;, 
    * &lt;protein&gt;MAGE- B&lt;/protein&gt; and &lt;protein&gt;MAGE-C&lt;/protein&gt;
    * clusters, &lt;protein&gt;MAGED2&lt;/protein&gt; is expressed ubiquitously. &lt;/doc&gt;</code>
    * <p>
    * Do not forget to include a root tag &lt;doc&gt; (&lt;document&gt; is also allowed to be a root tag). All other tags are
    * considered to be class labels (such as <code>protein</code> in the example above).
    * <p>
    * Returns performance given 10-fold cross-validation on the input data. A model
    * is stored in <code>model_file</code> provided by a user. Its log file is created in
    * the same folder using <code>model_file</code> with ".log" appended. Note: a model's log
    * is always stored in the same folder where a model is stored (if any of these are removed
    * by a user eventual applying of a model on the new data set (testing) by using TestModel service will fail).
    *<p>
    *@param train_set annotated training set (in XML format)
    *@param context_window context size (e.g., 2 means context of -2 and +2 given a word in focus)
    *@param mode learning mode (current release supports automatic learning only so <code>mode</code> has to be set to <code>a</code>)
    *@param model_file an absolute path to a folder where a model will be stored (including its name,
    *e.g. "/home/Models/mymodel.mod"
    *@param inp input type (current release supports annotated text only so <code>inp</code> has to be set to <code>text</code>)
    *@return the results of the 10-fold cross-validation
    */
     public String train_model(java.lang.String train_set,java.lang.String context_window,
java.lang.String mode, java.lang.String model_file, java.lang.String inp) {
      
        int window_size;
        String outp=new String();
        outp = "";
        double clsLabel;
        String perf = new String(); perf = "";
        // read context window size
        try {
       window_size = (int) (new Integer( context_window ).intValue());
    } catch(NumberFormatException ignored) {
        // Default value:
        window_size = 3;
    }
      double[] prec1 = new double[100];
      double[] recall1 = new double[100];
      double[] fScore1 = new double[100];
      String[] infoClass =new String[100]; 
       
        // create Weka instances
        makeSlidingWindow1(train_set,window_size,inp);
        // Learning by using Bayesian method
        String outp_nb = new String(); outp_nb = "";
        String outp_bn = new String(); outp_bn = "";
        String outp_j48 = new String(); outp_j48 = "";
        String outp_ibk = new String(); outp_ibk = "";
        NaiveBayes nb = new NaiveBayes();
        NaiveBayes bn1 = new NaiveBayes();
        BayesNet bn = new BayesNet();
        J48 j48 = new J48();
        Evaluation eval_nb = null;
        Evaluation eval_bn = null;
        Evaluation eval_bn1 = null;
        Evaluation eval_j48 = null;
        
        try{
         eval_nb = new Evaluation(new_train_data);
         eval_bn = new Evaluation(new_train_data);
         eval_j48 = new Evaluation(new_train_data);
         eval_bn1 = new Evaluation(new_train_data);
        
        nb.buildClassifier(new_train_data);
        eval_nb.crossValidateModel(nb, new_train_data, 10, new_train_data.getRandomNumberGenerator(1));
        outp_nb = eval_nb.toSummaryString();
               
       
        } catch (Exception ie) {perf = perf + "<attention>Please, check your input data</attention>";}
        
    if (!outp_nb.equals("")) {outp = outp + outp_nb;}
    if (!outp_bn.equals("")) {outp = outp + outp_bn;}
    if (!outp_ibk.equals("")) {outp = outp + outp_ibk;}
    
     // serialize model
  try{
    ObjectOutputStream oos = new ObjectOutputStream(
                           new FileOutputStream(model_file));
    oos.writeObject(nb);
    oos.flush();
    oos.close();
    }catch (IOException io){System.out.println("Can't write into file" + model_file);}
    // write into XML
    WriterToXML writer = new WriterToXML();
    writer.method = "Bayesian";
    writer.contextSize = window_size;
    Date now = new Date();
    long nowLong = now.getTime();
    DateFormat df4 = DateFormat.getDateInstance(DateFormat.FULL); 
    String s4 = df4.format(now);
    writer.date = s4;
    
    writer.instancesNumber = new_train_data.numInstances();
    writer.classesNumber = new_train_data.numClasses();
    writer.attributeValuesNumber = new_train_data.attribute(1).numValues();
  
    perf = perf + "<performance_info>";
    perf = perf + "<date>" + s4 + "</date>";
    perf = perf + "<method>Bayesian</method>";
    
    int corr; corr = outp_nb.indexOf("Incorrectly");
    String cr; cr = outp_nb.substring(0, corr).substring(31);
    int incorr; incorr = outp_nb.indexOf("Kappa");
    String inc; inc = outp_nb.substring(corr, incorr).substring(33);
    int kappa; kappa = outp_nb.indexOf("Mean");
    String kap; kap = outp_nb.substring(incorr,kappa).substring(17);
    int mean; mean = outp_nb.indexOf("Root mean");
    String mae; mae = outp_nb.substring(kappa,mean).substring(21);
    int root; root = outp_nb.indexOf("Relative");
    String rt; rt = outp_nb.substring(mean,root).substring(25);
    int rel; rel = outp_nb.indexOf("Root relative");
    String rl; rl = outp_nb.substring(root,rel).substring(25);
    int rootrel; rootrel = outp_nb.indexOf("Total Number");
    String rlrt; rlrt = outp_nb.substring(rel,rootrel).substring(30);
    perf = perf + "<correctly_classified>" + cr + "</correctly_classified>";
    perf = perf + "<incorrectly_classified>" + inc + "</incorrectly_classified>";
    perf = perf + "<kappa_statistics>" + kap + "</kappa_statistics>";
    perf = perf + "<mean_absolute_error>" + mae + "</mean_absolute_error>";
    perf = perf + "<root_mean_squared_error>" + rt + "</root_mean_squared_error>";
    perf = perf + "<relative_absolute_error>" + rl + "</relative_absolute_error>";
    perf = perf + "<root_relative_squared_error>" + rlrt + "</root_relative_squared_error>";
    perf = perf + "<context_window>" + window_size + "-1-" + window_size + "</context_window>";
    
    for (int m=0;m<new_train_data.classAttribute().numValues();m++)
    {
     String ind_value = new String();
     ind_value = new_train_data.classAttribute().value(m);
    
     writer.arffClass[m] = ind_value;
   
     perf = perf + "<class>";
     perf = perf + "<class_value>" + ind_value + "</class_value>";
     writer.prec[m] = eval_nb.precision(m);
     perf = perf + "<precision>" + eval_nb.precision(m)*100 + "%</precision>";
     writer.recall[m] = eval_nb.recall(m);
     perf = perf + "<recall>" + eval_nb.recall(m)*100 + "%</recall>";
     writer.fScore[m] = eval_nb.fMeasure(m);
     perf = perf + "<F-measure>" + eval_nb.fMeasure(m)*100 + "%</F-measure>";
     perf = perf + "</class>";
    }
    perf = perf + "</performance_info>";
   for (int m=0;m<new_train_data.classAttribute().numValues();m++)
    {
        writer.classv.addElement(new_train_data.classAttribute().value(m));
     
    }
    for (int m=0;m<new_train_data.attribute(1).numValues();m++)
    {
        writer.arffAtt[m] = new_train_data.attribute(1).value(m);
    }
    writer.arffAttribute = new_train_data.attribute(1).toString();
    writer.arffClassValues = new_train_data.classAttribute().toString();
    writer.writeToXML(model_file+".log");
      
         
        return perf;

    }
}