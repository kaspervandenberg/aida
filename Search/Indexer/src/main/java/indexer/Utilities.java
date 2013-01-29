package indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.index.IndexReader;

/**
 *
 * @author emeij
 */
public class Utilities {
  
  /** logger for Commons logging. */
  private static transient Logger log =
    Logger.getLogger(Utilities.class.getName());
  
  
  /** Get the extension of a file.
   * 
   * @param f   File to get the extension from
   */  
  public static String getExtension(File f) {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');

    if (i > 0 &&  i < s.length() - 1) {
        ext = s.substring(i+1).toLowerCase();
    }
    return ext;
  }
  
  /** Returns the location of INDEXDIR, or the current dir if that's undefined
   */
  public static String getINDEXDIR() {
    
    String BASE;
    
    if (System.getenv("INDEXDIR") == null 
            || System.getenv("INDEXDIR").length() == 0
            ) {
      if (log.isLoggable(Level.INFO))
        log.info("Env. variable INDEXDIR not found, using current dir as root.");
      BASE = new File(new File("t.tmp").getAbsolutePath()).getParentFile().getAbsolutePath();
    } else {
      BASE = System.getenv("INDEXDIR");
    }
    
    if (!BASE.endsWith(File.separator))
      BASE += File.separator;
    
    return BASE;
  }
  
  /** Return the second part of a line after the split "- " 
   * @param src   sourcefile
   * @param dest  target file
   */
  public static synchronized void copy(File src, File dest) throws IOException {
    InputStream in = new FileInputStream(src);
    OutputStream out = new FileOutputStream(dest);

    // Transfer bytes from in to out
    byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0) {
    out.write(buf, 0, len);
    }
    in.close();
    out.close();
  }
  
  /** Return the second part of a line after the split "- " 
   * @param nonsplitted string to split
   */
  public static String splitLine(String nonsplitted) {
    String[] s;
    String splitted;
    s = nonsplitted.split("- ");
    splitted = s[s.length-1];
    return splitted;  
  }
  
  
  public static int getDocsInCache(String index) {
    index = getINDEXDIR() + index;
    int cnt = 0;
    File c = new File(index, "cache");
    if (c.exists())
      cnt = c.list().length;
    return cnt;
  }

  public static int getDocsInIndex(String index) {
    index = getINDEXDIR() + index;
    int cnt = 0;
    
    try {
      IndexReader r = IndexReader.open(index);
      cnt = r.numDocs();
      r.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return cnt;
  }
  
  /** Deletes all files and subdirectories under dir.
   * Returns true if all deletions were successful.
   * If a deletion fails, the method stops attempting to delete and returns false.
   *
   * @param dir dir to delete
   * @return    true iff it succeeded
   */
  public static boolean deleteDir(File dir) {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (int i=0; i<children.length; i++) {
        boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
    }

    // The directory is now empty so delete it
    return dir.delete();
  }
  
/** Read a InputStreamReader, UTF-8 safe
   *
   * @param isr InputStreamReader to read
   * @return contents
   */
  public static String loadTextStream(InputStreamReader isr) 
          throws DocumentHandlerException {
    
    BufferedReader bs = null;
    StringBuffer sb = new StringBuffer(1024);
    
    try {
       
      bs = new BufferedReader(isr);
      //char[] chars = new char[1024];
      String s = null;

      while( (s = bs.readLine()) != null) {	
	sb.append(new String(s.getBytes(),"UTF-8"));
      }

      //while( (numRead = bs.read(chars)) > -1){
        //sb.append(String.valueOf(chars));  
      //}
    } catch (IOException e) {
      throw new DocumentHandlerException("Cannot extract text ", e);
    } finally {
      try {
        if (bs != null)
          bs.close();
      } catch (IOException e) {
        throw new DocumentHandlerException("Cannot extract text ", e);
      }
    }
    
    return sb.toString(); 
  } 
  
  
  /** Read a text file, UTF-8 safe
   *
   * @param file file to read
   * @return contents
   */
  public static String loadTextFile(File file) 
          throws DocumentHandlerException {
    
    InputStreamReader is = null;
    
    try {
      is = new InputStreamReader(new FileInputStream(file), "UTF-8");
    } catch (IOException e) {
      throw new DocumentHandlerException("Cannot extract text from a Text document", e);
    }
    
    return loadTextStream(is);
    
  }
  
  /** Removes control chars from a string 
   *
   * @param in String  to clean
   * @return cleaned string
   */
  public static String cleanString(String in) {
    // The pattern matches control characters
    Pattern p = Pattern.compile("\\p{Cntrl}");
    Matcher m = p.matcher("");
    m.reset(in);
    return m.replaceAll(" ");
  }
  
  /**
   * Naive way of generating a config :)
   * 
   * @return
   */
  public static String createDefaultConfigFile() {
    String config = "<?xml version='1.0' encoding='UTF-8'?>"+
      
      "<!--"+
      "    Document   : indexconfig.xml"+
      "    Description: Example config file"+
      "    -->"+
      
      "<config xmlns='http://aid.org/'"+
      "        xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"+
      "        xsi:schemaLocation='http://aid.org/ file:config.xsd'"+
      "        >"+
      
      "<!-- You usually only have to change the following three items-->"+
      
      
      "  <Name>My_index</Name>"+
      "  <DataPath>datadir</DataPath>"+
      "  <IndexOverwrite>true</IndexOverwrite>"+
      
      "  <IndexAnalyzer>STANDARD</IndexAnalyzer>"+
      
      "  <SRBused>false</SRBused>"+
      "  <MergeFactor>300</MergeFactor>"+
      "  <MaxBufferedDocs>30</MaxBufferedDocs>"+
      
      "  <DocType FileType='medline'>"+
      "    <FileExtension>med</FileExtension>"+
      "    <DocTypeAnalyzer>STANDARD</DocTypeAnalyzer>"+
      "    <Field Name='AU'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>true</Store>"+
      "      <Termvector>NO</Termvector>"+
      "      <Description>'Author field of Medline'</Description>"+
      "    </Field>"+
      "    <Field Name='TI'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>true</Store>"+
      "      <Termvector>YES</Termvector>"+
      "      <Description>'Title field of Medline'</Description>"+
      "    </Field>"+
      "    <Field Name='AB'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>true</Store>"+
      "      <Termvector>YES</Termvector>"+
      "      <Description>'Abstract field of Medline'</Description>"+
      "    </Field>"+
      "    <Field Name='FAU'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>true</Store>"+
      "      <Termvector>NO</Termvector>"+
      "      <Description>'First Author field of Medline'</Description>"+
      "    </Field>"+
      "    <Field Name='MH'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>true</Store>"+
      "      <Termvector>NO</Termvector>"+
      "      <Description>'MESH Heading field of Medline'</Description>"+
      "    </Field>"+
      "    <Field Name='PMID'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>true</Store>"+
      "      <Termvector>NO</Termvector>"+
      "      <Description>'PMID field of Medline'</Description>"+
      "    </Field>"+
      "    <Field Name='SO'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>true</Store>"+
      "      <Termvector>NO</Termvector>"+
      "      <Description>'SO field of Medline'</Description>"+
      "    </Field>"+
      "  </DocType>"+

      "  <DocType FileType='txt'>"+
      "    <FileExtension>txt</FileExtension>"+
      "    <FileExtension>xml</FileExtension>"+
      "    <DocTypeAnalyzer>STANDARD</DocTypeAnalyzer>"+
      "    <Field Name='path'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>true</Store>"+
      "      <Termvector>NO</Termvector>"+
      "      <Description>'path'</Description>"+
      "    </Field>"+
      "    <Field Name='content'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>true</Store>"+
      "      <Termvector>YES</Termvector>"+
      "      <Description>'content'</Description>"+
      "    </Field>"+
      "    <Field Name='title'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>true</Store>"+
      "      <Termvector>YES</Termvector>"+
      "      <Description>'title'</Description>"+
      "    </Field>"+
      "    <Field Name='summary'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>false</Store>"+
      "      <Termvector>NO</Termvector>"+
      "      <Description>'summary'</Description>"+
      "    </Field>"+
      "  </DocType>"+

      "  <DocType FileType='pdf'>"+
      "    <FileExtension>pdf</FileExtension>"+
      "    <DocTypeAnalyzer>STANDARD</DocTypeAnalyzer>"+
      "    <Field Name='path'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>true</Store>"+
      "      <Termvector>NO</Termvector>"+
      "      <Description>'path'</Description>"+
      "    </Field>"+
      "    <Field Name='content'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>true</Store>"+
      "      <Termvector>YES</Termvector>"+
      "      <Description>'content'</Description>"+
      "    </Field>"+
      "    <Field Name='title'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>true</Store>"+
      "      <Termvector>YES</Termvector>"+
      "      <Description>'title'</Description>"+
      "    </Field>"+
      "    <Field Name='summary'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>false</Store>"+
      "      <Termvector>NO</Termvector>"+
      "      <Description>'summary'</Description>"+
      "    </Field>"+
      "  </DocType>"+
      
      "  <DocType FileType='msword'>"+
      "    <DocTypeAnalyzer>STANDARD</DocTypeAnalyzer>"+
      "    <FileExtension>doc</FileExtension>"+
      "    <Field Name='path'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>true</Store>"+
      "      <Termvector>NO</Termvector>"+
      "      <Description>'path'</Description>"+
      "    </Field>"+
      "    <Field Name='content'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>true</Store>"+
      "      <Termvector>YES</Termvector>"+
      "      <Description>'content'</Description>"+
      "    </Field>"+
      "    <Field Name='summary'>"+
      "      <Index>TOKENIZED</Index>"+
      "      <Store>false</Store>"+
      "      <Termvector>NO</Termvector>"+
      "      <Description>'summary'</Description>"+
      "    </Field>"+
      "  </DocType>"+
      "</config>";
    
    return Utilities.createTemporaryFile(config, "xml");
  }
  
  /**
  * Creates a temporary file containing some input string
  * 
  * @param text        String containing the file contents
  * @param filename    String containing the file name
  * @param overwrite        to overwrite any existing file 
  * @todo              Check compatibility with mac/unix/windows etc.
  * @return            location of the file
  */
  public static String createTemporaryFile (String text, String filename, boolean overwrite) {
    
    int cnt = 0;
    
    String tmp = System.getenv("TMP");
    if (tmp == null || tmp.equalsIgnoreCase(""))
      tmp = ".";
    
    String file = tmp + File.separator + filename;
    
    if (new File(file).exists()) {
      if (!overwrite) {
        while (new File(file).exists()) {
          cnt++;
          file = tmp + File.separator + cnt + "." + filename;
        }
      }
    }     

    BufferedWriter bw = null;
    try {
       bw = new BufferedWriter(new FileWriter(file, true));
       bw.write(text);
       bw.newLine();
       bw.flush();
    } catch (IOException ioe) {
       ioe.printStackTrace();
    } finally {                       // always close the file
       if (bw != null) try {
          bw.close();
       } catch (IOException ioe2) {
          ioe2.printStackTrace();
       }
    }
    
    return file;
    
  }
  
  /**
  * Creates a temporary file containing some input string
  * 
  * @param config      String containing the file contents
  * @todo              Check compatibility with mac/unix/windows etc.
  * @return            location of the file
  */
  public static String createTemporaryFile (String text, String extension) {
    return createTemporaryFile(text, "indexer.tmp." + "." + extension, false);
  }
}
