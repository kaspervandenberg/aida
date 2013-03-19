package indexer;
/*
 * DocConverter.java
 *
 * Created on March 24, 2008, 1:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.io.RandomAccessFile;
import java.io.PipedReader;
import java.io.PipedWriter;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.OverlappingFileLockException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import java.util.List;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

//import org.textmining.text.extraction.WordExtractor;

import indexer.Utilities;

//import nl.uva.science.wsdtf.BackEnd;
//import nl.uva.science.wsdtf.BackEndImpl;

/**
 *
 * @author alogo
 */
public class DocConverter implements Runnable{
    private File file;
    private int currentOpp;
    private String destDir;
    private int theadCount;
    private PipedReader pipeReader;
    private PipedWriter pipeWriter;
    
    public static final int extractTextFromPdf = 1;
//    private Writer writer;
//    private  Thread thread;
    
    /** Creates a new instance of DocConverter */
    public DocConverter(){
       
        try {
            pipeWriter = new PipedWriter();
            pipeReader = new PipedReader(getPipeWriter()); 
        } catch (IOException ex) {
            ex.printStackTrace();
        } 
    }
    
    
    public String extractContent(String ext){
        return PDFToString();    
    }    
    
    
    public String[] extractDocFileds(){
        
        return null;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    
    public void setFile(String path) {
        file = new File(path);
    }
    
    
    public void extractTextFromPdf(Writer writer){
        long start = System.currentTimeMillis();
        try{
            PDDocument pdfDocument = PDDocument.load( file );
            if ( pdfDocument.isEncrypted() ) {
            //Just try using the default password and move on
                pdfDocument.decrypt( "" );  
            }
            
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.writeText( pdfDocument, writer );
                      

            pdfDocument.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }catch (CryptographyException ex) {
            ex.printStackTrace();
        }catch (InvalidPasswordException ex) {
            ex.printStackTrace();
        }
    }
    
    
    private void extractTextFromPdf(){
        long start = System.currentTimeMillis();
        try{
            PDDocument pdfDocument = PDDocument.load( file );
            if ( pdfDocument.isEncrypted() ) {
            //Just try using the default password and move on
                pdfDocument.decrypt( "" );  
            }
            
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.writeText( pdfDocument, getPipeWriter() );
                      

            pdfDocument.close();
            getPipeWriter().flush();
            getPipeWriter().close();
        }catch(IOException ex){
            System.out.println(this.file.getName());
            ex.printStackTrace();
        }catch (CryptographyException ex) {
            ex.printStackTrace();
        }catch (InvalidPasswordException ex) {
            ex.printStackTrace();
        }
    }
       
    public String PDFToString(){
        long start = System.currentTimeMillis();
        try{
            PDDocument pdfDocument = PDDocument.load( file );
            if ( pdfDocument.isEncrypted() ) {
            //Just try using the default password and move on
                pdfDocument.decrypt( "" );  
            }

            //create a writer where to append the text content.
            StringWriter writer = new StringWriter();
            
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.writeText( pdfDocument, writer );

            String contents = writer.getBuffer().toString();
                       
//            long end = System.currentTimeMillis();
            pdfDocument.close();
//            System.out.println("Elapsed time:"+ (end - start));
            return Utilities.cleanString(new String(contents.getBytes("ISO-8859-1")));
        }catch(IOException ex){
            System.out.println(this.file.getName());
            ex.printStackTrace();
        }catch (CryptographyException ex) {
            ex.printStackTrace();
        }catch (InvalidPasswordException ex) {
            ex.printStackTrace();
        }
    return null;
    }
    
   
    
    public String savePDFAsTxt(File pdfFile){
        file =pdfFile;
        return pdf2Txt();
//        currentOpp = PDFTOTEXT;
//       
//        theadCount++;
//        if(theadCount<=10){
//            System.out.println(theadCount);
//            thread = new Thread(this);
//            thread.run();   
//        }else{
//            try {
//                thread.join();
//                theadCount--;
////                       System.out.println("Total Memory"+Runtime.getRuntime().totalMemory());    
//                System.out.println("Before Free Memory"+Runtime.getRuntime().freeMemory());
//                System.runFinalization();
//                System.gc();
//                System.out.println("After Free Memory"+Runtime.getRuntime().freeMemory());
// 
//            } catch (InterruptedException ex) {
//                ex.printStackTrace();
//            }catch (Throwable ex) {
//                ex.printStackTrace();
//            }  
//        }
     
    }
    
      
       
    public String pdf2Txt(){

        long start = System.currentTimeMillis();
        File txtFile=null;
        try{
            PDDocument pdfDocument = PDDocument.load( file );
            if ( pdfDocument.isEncrypted() ) {
            //Just try using the default password and move on
                pdfDocument.decrypt( "" );  
            }

            txtFile = new File(getDestDir()+"/"+file.getName()+".txt");
            
            FileWriter fileWriter = new FileWriter(txtFile);
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.writeText( pdfDocument, fileWriter );
            pdfDocument.close();
            fileWriter.flush();
            fileWriter.close();
        }catch(IOException ex){
            System.out.println(this.file.getName());
            ex.printStackTrace();
        }catch (CryptographyException ex) {
            ex.printStackTrace();
        }catch (InvalidPasswordException ex) {
            ex.printStackTrace();
        }
        return txtFile.getAbsolutePath();
    }
    
    public void run(){
        switch(getCurrentOpp()){
            case extractTextFromPdf:
                extractTextFromPdf(getPipeWriter());
                try {
                    getPipeWriter().flush();
                    getPipeWriter().close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                
                break;
        }
    }

    public String getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

//    public Writer getWriter() {
//        return writer;
//    }
//
//    public void setWriter(Writer writer) {
//        this.writer = writer;
//    }

    public PipedReader getPipeReader() {
        return pipeReader;
    }

    public PipedWriter getPipeWriter() {
        return pipeWriter;
    }

    public int getCurrentOpp() {
        return currentOpp;
    }

    public void setCurrentOpp(int currentOpp) {
        this.currentOpp = currentOpp;
    }
       
}
