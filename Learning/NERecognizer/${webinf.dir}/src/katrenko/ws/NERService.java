/*
 * NERService.java
 *
 * Created on 17 March 2006, 20:43
 *
 * NER web service provides two options - either for annotating biomedical text data
 * or for annotating text data based on the newspaper model.
 * 
 */

package katrenko.ws;
import com.aliasi.coref.EnglishMentionFactory;
import com.aliasi.coref.MentionFactory;
import com.aliasi.coref.WithinDocCorefAnnotateFilter;

import com.aliasi.ne.Decoder;
import com.aliasi.ne.EnglishPronounDictionary;
import com.aliasi.ne.NEAnnotateFilter;
import com.aliasi.ne.NEDictionary;
import com.aliasi.ne.Tags;

import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceAnnotateFilter;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenCategorizer;
import com.aliasi.tokenizer.TokenCategorizer;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import com.aliasi.xml.GroupCharactersFilter;
import com.aliasi.xml.SAXFilterHandler;
import com.aliasi.xml.SimpleElementHandler;
import com.aliasi.xml.SAXFilterHandler;

import java.beans.PropertyVetoException;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URL;

import java.util.Iterator;
import java.util.List;

import java.io.FileReader; 
import java.io.BufferedReader;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.regex.*;
import java.text.*;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

//import javax.servlet.*;
import java.net.URL;
import java.net.URI;

import org.apache.xmlbeans.*;

import org.vle.aid.*;
/**
 * NERService uses <a href="http://www.alias-i.com/lingpipe/">LingPipe</a> models to label the input data provided by
 * a user. The existing models are part of LingPipe and include models trained on the
 * newswire corpora (<code>EN_NEWS.model</code>) and genomics data (<code>EN_GENOMICS.model</code>). The former model outputs generic named entities, such as organizations, 
 * person names and locations, while the latter is used to detect biomedical entities. 
 * <p>
 * @author Sophia Katrenko
 * @version 1.0
 */
public class NERService {
   
    /** Specify the full path to the trained models, EN_NEWS.model and EN_GENOMICS.model */
    //String newx = " ";
   // String newx = "C://Documents and Settings/Sophijka/Mijn documenten/INSTALL/Data/EN_NEWS.model";
   // String genx = "C://Documents and Settings/Sophijka/Mijn documenten/INSTALL/Data/EN_GENOMICS.model";
    private MentionFactory mMentionFactory;
    private Decoder mNewsDecoder;
    private Decoder mGenomicsDecoder;
    private TokenCategorizer mTokenCategorizer;
    private static final double PRUNING_THRESHOLD = 16.0;
    private XmlOptions xmlOpts = new XmlOptions();
    private TokenizerFactory mTokenizerFactory;
    private NEDictionary mPronounDictionary;
    private SentenceModel mSentenceModel;
    private String[] mElementsToAnnotate;
    private static final String PLAIN_TEXT_TOP_LEVEL_ELEMENT = "DOCUMENT";
    private File mModelDirectory;
    public Hashtable hashtableClass = new Hashtable(); 
    
    /** Creates a new instance of NERService */
    public NERService() {}
    
    /** Takes input provided by a user and outputs either the annotated input 
     * (annotated sentences) or all entities found in the input data. To carry out
     * the named entity recognition step, one of the three models is used.
     *
     * @param input_data an absolute path to the text data
     * @param r_type type of NER model (its value is either <code>News</code> or 
     * <code>Medline</code> (Genomics))
     * @param input_type type of the data used (<code>lucene</code> if it's the output from Lucene
     * or <code>text</code> if it is a plain text)
     * @param output_type type of output (<code>annotation</code> if it is annotated input data, <code>NElist</code>
     * for the list of named entities found in the input data or <code>N3</code> for the named entities
     * in N3 format for the repository)
     * @return depending on <code>output_type</code> returns either annotated <code>input_data</code>
     * or the list of named entities found in <code>input_data</code>
     */
    public String NErecognize(String input_data, String r_type, String input_type, String output_type)

    {
            String outr = new String();
        URL url = NERService.class.getResource("/katrenko/ws/EN_NEWS.model");
        URI newx = null;
        try{
          newx = new URI(url.toString());
         } catch(Exception e){System.out.println("Cannot locate EN_NEWS.model!");}
      
       URL urlb = NERService.class.getResource("/katrenko/ws/EN_GENES_BIOCREATIVE03.model");
       URI bio = null;
        try{
            bio = new URI(urlb.toString());
        } catch(Exception e){System.out.println("Cannot locate EN_GENES_BIOCREATIVE03.model!");}
        
       URL urlg = NERService.class.getResource("/katrenko/ws/EN_GENOMICS.model");
       URI genx = null;
        try{
            genx = new URI(urlg.toString());
        } catch(Exception e){System.out.println("Cannot locate EN_GENOMICS.model!");}
       
    try {
            SAXFilterHandler firstFilter = null;
            SAXFilterHandler lastFilter = null;
         
            	MentionFactory mMentionFactory = new EnglishMentionFactory();
            	TokenizerFactory mTokenizerFactory = new IndoEuropeanTokenizerFactory();
            	NEDictionary mPronounDictionary = new EnglishPronounDictionary();
            	SentenceModel mSentenceModel = new IndoEuropeanSentenceModel();
            	String[] mElementsToAnnotate = new String[] {PLAIN_TEXT_TOP_LEVEL_ELEMENT };

                WithinDocCorefAnnotateFilter corefFilter
                    = new WithinDocCorefAnnotateFilter(mMentionFactory);
                
                TokenCategorizer mTokenCategorizer  = new IndoEuropeanTokenCategorizer();
                // if the input data must be annotated by Newspaper model
                if (r_type.equals("News")) {
                
                    File newsModelFile = new File(newx);
                 
                try {
                    mNewsDecoder = new Decoder(newsModelFile,mTokenCategorizer,PRUNING_THRESHOLD);
                } catch (IOException e) {
                    System.out.println("Could not find the news model file=" + newx);
                }
                NEAnnotateFilter neAnnotateFilter
                    = new NEAnnotateFilter(mNewsDecoder,mTokenizerFactory);
                
                neAnnotateFilter.setPronounDictionary(mPronounDictionary);
                neAnnotateFilter.setHandler(corefFilter);

                SentenceAnnotateFilter sentenceFilter
                    = new SentenceAnnotateFilter(mSentenceModel,
                                                 mTokenizerFactory,
                                                 mElementsToAnnotate);
                sentenceFilter.setHandler(new GroupCharactersFilter(
                                                   neAnnotateFilter));
                lastFilter = corefFilter;
                firstFilter = sentenceFilter;
            } else {
                    
                // if the data must be annotated by Genomics model
                File newsModelFile = new File(genx);
                try {
                    mGenomicsDecoder = new Decoder(newsModelFile,mTokenCategorizer,PRUNING_THRESHOLD);
                } catch (IOException e) {
                    System.out.println("Could not find genomics model file=" + genx);
                }
                NEAnnotateFilter neAnnotateFilter
                    = new NEAnnotateFilter(mGenomicsDecoder, mTokenizerFactory);
                
                neAnnotateFilter.setPronounDictionary(mPronounDictionary);
             //   neAnnotateFilter.setHandler(corefFilter);
                
                SentenceAnnotateFilter sentenceFilter
                    = new SentenceAnnotateFilter(mSentenceModel,
                                                 mTokenizerFactory,
                                                 mElementsToAnnotate);
                sentenceFilter.setHandler(new GroupCharactersFilter(
                                                   neAnnotateFilter));
                lastFilter = neAnnotateFilter;
                firstFilter = sentenceFilter;

            }
            //output annotated input data as XML
                
            
            
            //TEST
   /*         Map map1 = new HashMap(); 
            String s1=new String();String s2=new String();
            s1="aaa";s2="bb scs dxcs";
            System.out.println(s1);
            System.out.println(s2);
            System.out.println("NOG");
            map1.put("aab",new Integer(3));
            map1.put(s1,new String(s2));
            System.out.println("MAP1 size" + map1.size());
            System.out.println("AAB " + map1.get("aab"));
            Iterator it1 = map1.keySet().iterator();
            while (it1.hasNext()) {
                // Get key
             System.out.println("MAP1 size insc");
             Object key1 = it1.next();
             System.out.println("MAP1 size insc");
             System.out.println("MAP1 size ins" + map1.size());
            System.out.println("test key "+key1.toString());
            System.out.println("test keyvalue "+map1.get(key1).toString());
            }
     */      
                
   if (input_type.equals("xml")){             
            SaxUnMarshaller handler=new SaxUnMarshaller();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            try{
            SAXParser saxParser = factory.newSAXParser();
            StringBuffer textData = new StringBuffer(input_data);
    ByteArrayInputStream bais = new ByteArrayInputStream(textData.toString().getBytes());
    saxParser.parse(bais, handler); 
    }catch(Exception ex){System.out.println("CAN'T PARSE1");}
            
            String docum=new String();
            String outr_temp=new String();
            outr="<result_final>";
            Iterator it = handler.map.keySet().iterator();
            
            while (it.hasNext()) {
                // Get key
            DocumentToXMLHandler xmlHandler = new DocumentToXMLHandler();
            xmlHandler.ner_type = r_type;
            xmlHandler.outp_type = output_type;
            lastFilter.setHandler(xmlHandler);
             
             Object key = it.next();Object val;
             docum = handler.map.get(key).toString();
             char[] inputChars = docum.toCharArray();
             firstFilter.startDocument();
            
              firstFilter.startSimpleElement(PLAIN_TEXT_TOP_LEVEL_ELEMENT);
              firstFilter.characters(inputChars,0,inputChars.length);
              firstFilter.endSimpleElement(PLAIN_TEXT_TOP_LEVEL_ELEMENT);
              firstFilter.endDocument(); 
            System.out.println("test key "+key.toString());
            System.out.println("test keyvalue "+handler.map.get(key).toString());
              
               outr+="<doc_res id=\""+key.toString()+"\">";
               outr_temp = (xmlHandler.getXML());
               outr+=outr_temp+"</doc_res>";
               outr_temp=""; docum="";
              
            }
            outr+="</result_final>";
   }
   else if (input_type.equals("lucene")){
            
   if (output_type.equals("NElist")) {outr = "<result_final>";}
   else if (output_type.equals("N3")) {
       outr = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .";
       outr += "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .";
       outr += "@prefix xsd: <http://www.w3.org/2001/10/XMLSchema#> .";
       outr += "@prefix : <http://www.vl-e.nl/aid/2007/03/ne#> "; 
   }
            
            
   xmlOpts.setCharacterEncoding("UFT-8");
    String inputXML = null;

// get SearcherWS results here and store them in inputXML
// then:
    try{
ResultDocument resultDoc = ResultDocument.Factory.parse(input_data);
ResultType result = resultDoc.getResult();
Document[] doc = result.getDocArray();
  
     
            DocumentToXMLHandler xmlHandler = new DocumentToXMLHandler();
            xmlHandler.ner_type = r_type;
            xmlHandler.outp_type = output_type;
            lastFilter.setHandler(xmlHandler);
            for(int counter=0;counter<doc.length;counter++)
            {
            xmlHandler.PMID = doc[counter].getFieldArray(0).getValue();
                if (output_type.equals("NElist")) {outr += "<doc id=\"" + doc[counter].getFieldArray(0).getValue()+"\">";}
                //else if (output_type.equals("N3")) {outr += "";}
            
            char[] inputChars = doc[counter].getFieldArray(3).getValue().toCharArray();
             firstFilter.startDocument();
              firstFilter.startSimpleElement(PLAIN_TEXT_TOP_LEVEL_ELEMENT);
              firstFilter.characters(inputChars,0,inputChars.length);
              firstFilter.endSimpleElement(PLAIN_TEXT_TOP_LEVEL_ELEMENT);
              firstFilter.endDocument(); 
               outr += (xmlHandler.getXML());
               outr += "</doc>";
    }
    }catch(Exception e){System.out.println("XMLBeans problem!");}
               outr+="</result_final>";
   }
    else if (input_type.equals("text")){
            outr="<result_final>";
            DocumentToXMLHandler xmlHandler = new DocumentToXMLHandler();
            xmlHandler.ner_type = r_type;
            xmlHandler.outp_type = output_type;
            lastFilter.setHandler(xmlHandler);
            char[] inputChars = input_data.toCharArray();
            firstFilter.startDocument();
            
              firstFilter.startSimpleElement(PLAIN_TEXT_TOP_LEVEL_ELEMENT);
              firstFilter.characters(inputChars,0,inputChars.length);
              firstFilter.endSimpleElement(PLAIN_TEXT_TOP_LEVEL_ELEMENT);
              firstFilter.endDocument(); 
              outr += xmlHandler.getXML();
              outr += "</result_final>";
    }
        } catch (SAXException e) {
            System.out.println("SAX Exception during processing=" + e);
        }
       System.out.println("OUTR " + outr);
        return outr;   
}
}

/** Output of annotation/named entities in XML */
class DocumentToXMLHandler extends SimpleElementHandler {
    private StringBuffer mOutDocument = new StringBuffer();
    private int mLastSentenceNumber = 0;
    private String mTypeID;
    private String mTypeEnd;
    public String PMID;
    private int flag = 0;
    public String ner_type;
    public String outp_type;
    public String sentence;
    public Map mapEntityMetaInfo;
    public Map mapEntityInfo;
    public Map mapEntityFrequency;
    public Map mapEntityOffset;
    public Map mapEntitySentence;
    private int entityFrequency = 0;
    private int offset = 0;
    private String entityID;
    public DocumentToXMLHandler() { }
    public void startDocument() {
        mOutDocument = new StringBuffer();
        mapEntityMetaInfo = new HashMap();
        mapEntityInfo = new HashMap();
        mapEntityFrequency = new HashMap();
        mapEntityOffset = new HashMap();
        mapEntitySentence = new HashMap();
        offset = 0;
        entityID = new String();
        entityFrequency = 0;
        flag = 0;
        //mOutDocument.append("<?xml version=\"1.0\"  encoding=\"utf-8\"?><results>");
    }
    public void endDocument() {
        //mOutDocument.append("</results>");
    }
    public void startElement(String domain, String localName,
                             String qName, Attributes atts) {
     if (outp_type.equals("annotation")){   
        if (qName.equals(SentenceAnnotateFilter.SENTENCE_ELEMENT)) {
            mOutDocument.append("<sentence ");
            mOutDocument.append("id=" + "\"" + Integer.toString(++mLastSentenceNumber) + "\">");
        } else if 
         (qName.equals(NEAnnotateFilter.DEFAULT_ENTITY_ELEMENT)) {
            String entityType = atts.getValue(NEAnnotateFilter.DEFAULT_ENTITY_TYPE_ATTRIBUTE);
            entityID = new String();
            entityID = atts.getValue(WithinDocCorefAnnotateFilter.ID_ATTRIBUTE);
            if (ner_type.equals("News"))
            {mTypeID = entityType + " id=" + "\"" + entityID + "\"";flag = 1;}
            //if Genomics model is chosen, omit Entity's ID
            else {mTypeID = entityType; flag = 1;}
            mTypeEnd = entityType;
        }
    }
     
     else if(outp_type.equals("NElist") || outp_type.equals("N3")){
         System.out.println("ENteredARG! ");
         if (qName.equals(SentenceAnnotateFilter.SENTENCE_ELEMENT)) 
          { sentence = new String(); }
         else if 
         (qName.equals(NEAnnotateFilter.DEFAULT_ENTITY_ELEMENT)) {
            String entityType = atts.getValue(NEAnnotateFilter.DEFAULT_ENTITY_TYPE_ATTRIBUTE);
            entityID = new String();
            entityID = atts.getValue(WithinDocCorefAnnotateFilter.ID_ATTRIBUTE);
            if (ner_type.equals("News"))
            {mTypeID = entityType + " id=" + "\"" + entityID + "\"";flag = 1;}
            //if Genomics model is chosen, omit Entity's ID
            else {mTypeID = entityType; flag = 1;}
            //String temp_copy = temp.trim();
            // System.out.println("TEMP is"+temp_copy);
            //String concat=new String(); concat=classLabel+"_"+att;
            //hashtableClass.put(concat, temp_copy);
            if (!mapEntityMetaInfo.containsKey(entityID)){
            System.out.println("ENtered! ");
            mapEntityMetaInfo.put(entityID, new String(entityType));
            //mapEntityFrequency.put(entityID, new String(Integer.toString(entityFrequency)));
            mapEntityFrequency.put(entityID, new String(Integer.toString(1)));
            System.out.println("Initiated " + mapEntityFrequency.get(entityID).toString());
            System.out.println("ID " + mapEntityMetaInfo.get(entityID).toString());
            }
            else {String dum = mapEntityFrequency.get(entityID).toString();
                  entityFrequency = Integer.parseInt(dum);  
                  entityFrequency++;
                  System.out.println("exists " + entityFrequency);
                  System.out.println("IDagain " + mapEntityMetaInfo.get(entityID).toString());
                  mapEntityFrequency.put(entityID, new String(Integer.toString(entityFrequency)));
                }
            mTypeEnd = entityType;
        }
     }
    }
    public void endElement(String domain, String localName,
                           String qName) {
        if (outp_type.equals("annotation")){   
        if (qName.equals("sent")) {
            mOutDocument.append("</sentence>");
        } else if (qName.equals("ENAMEX")) {
            mOutDocument.append("</" + mTypeEnd + ">");
        }
        }
        else if(outp_type.equals("NElist")  || outp_type.equals("N3")){
         if (qName.equals("sent")) {}
         if (qName.equals("ENAMEX")) {
         mOutDocument.append("</" + mTypeEnd + ">");
        }   
        }
    }
    public void characters(char[] cs, int start, int length) {
        offset += length;
        sentence += new String(cs, start, length);
    if (outp_type.equals("annotation")){   
        if (flag==1) {mOutDocument.append("<"+ mTypeID + ">");
                      mOutDocument.append(cs,start,length); flag=0;}
        else {mOutDocument.append(cs,start,length);}
      }
    else if(outp_type.equals("NElist")  || outp_type.equals("N3")){if (flag==1) {//ADD mOutDocument.append("<"+ mTypeID + ">");mOutDocument.append(cs,start,length); 
                    mOutDocument.append("<"+ mTypeID + ">");
                    mOutDocument.append(cs,start,length);
         //           mOutDocument.append("</"+ entityType + ">");
                    if (!mapEntityInfo.containsKey(entityID)){ 
                    String ne = new String(cs,start,length);
                    mapEntityInfo.put(entityID, ne);
                    mapEntityOffset.put(entityID, new String(Integer.toString(offset-length)));
                    System.out.println("Added " + ne);
                    }flag=0;}}
    }
    
    public String getXML() {
        Iterator it = mapEntityMetaInfo.keySet().iterator();
            while (it.hasNext()) {
              Object key = it.next();
              if(outp_type.equals("NElist")){
                 
              //COM mOutDocument.append("<" + mapEntityMetaInfo.get(key).toString() + " id=\"" + key.toString() + "\" freq=\"" + mapEntityFrequency.get(key).toString());
              //COM mOutDocument.append("\" offset=\"" + mapEntityOffset.get(key) + "\">" + mapEntityInfo.get(key).toString() + "</" + mapEntityMetaInfo.get(key).toString() + ">");
              System.out.println("CHECK: " + mapEntityInfo.get(key).toString());
              }
              else if (outp_type.equals("N3")){
                  mOutDocument.append(":ne" + key.toString() + " rdf:type :" + mapEntityMetaInfo.get(key).toString() + " . ");                  
                  mOutDocument.append(":ne" + key.toString() + " rdfs:label " + mapEntityInfo.get(key).toString() + " .");
                  mOutDocument.append(":ne" + key.toString() + " :inDocument " + PMID + " .");
                  mOutDocument.append(":ne" + key.toString() + " :frequency  \"" + mapEntityFrequency.get(key).toString() + "\"^^xsd:integer ."); 
              }
            }
        return mOutDocument.toString();
    }
}


// unmarshalling (reading) the training set (from XML)
class SaxUnMarshaller extends DefaultHandler{
      
      public int instanceCount;
      private String temp; 
      private String lastElement;
      private String classLabel;
      private int root_counter=0;
      private int begin_counter=0, end_counter=0;
      private int uniqueCount=0;
      private int uniqueCountClass=0;
      public Map map = new HashMap();    // hash table   
     
     private String att=new String();
     
      public SaxUnMarshaller() {}
    
      public void startDocument()
       {root_counter=0;instanceCount=0;}
      
      public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
      throws org.xml.sax.SAXException {
    	  
    	  if (qName.equals("aid:result") || qName.equals("DOC") || qName.equals("DOC_ID") || qName.equals("set") || qName.equals("article") || qName.equals("articleinfo")) 
           {root_counter=0;} else {root_counter=1;begin_counter=1;end_counter=0;}
    	  classLabel=qName;att = atts.getValue(0);
           
      }
      public void endElement(String namespaceURI, String localName, String qName)
      throws org.xml.sax.SAXException {
    	  end_counter=1;begin_counter=0;
         // System.out.println("Number of els"+map.size());
        } 
      public void characters(char[] chars, int start, int length)
      {
          
    	  if ((root_counter==0) || (end_counter==1))
    	  {
    	/*
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
    	     }
    		  }*/
    	  }
    	  else if ((begin_counter==1) && (end_counter==0))
    	  {
    		  instanceCount++; 
    		  temp = new String(chars, start, length);
    		  String temp_copy = temp.trim();
                 // System.out.println("TEMP is"+temp_copy);
                  String concat=new String(); concat=classLabel+"_"+att;
                  //hashtableClass.put(concat, temp_copy);
                  map.put(concat, new String(temp_copy));
    	  }
      }      
}