package indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;

import java.util.List;
import java.util.Vector;
import java.util.Date;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.DateTools;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentInformation;

import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.exceptions.InvalidPasswordException;

import org.pdfbox.util.PDFTextStripper;

/**
 *
 * @author Machiel Jansen, Edgar Meij
 */
public class PdfHandler extends DocHandler {
  private final ConfigurationHandler cfg;
  private final String TYPE= "pdf";
  private final IndexAdder ia;
  private final ConfigurationHandler.FieldTypeCache fields;
  private final char FILE_SEPARATOR = System.getProperty("file.separator").charAt(0);

  /** Creates a new instance of PdfHandler */
  public PdfHandler(ConfigurationHandler cfg) {
    this.cfg = cfg;
    ia = new IndexAdder(cfg);
	fields = this.cfg.getFields(TYPE);
  }

  public String[] getFieldNames(){
    return new String[] { "id", "content", "path", "url", "summary", "modified", "uid", 
            "author", "creationDate", "creator", "keywords", 
            "modificationDate", "producer", "subject", "title", "trapped"
            };
  }
    
  public void addDocumentToIndex(IndexWriter writer, File file)  
          throws DocumentHandlerException {
    
    if (fields.contains("path"))
      ia.addFieldToDocument(TYPE, "path", file.getPath());
    
    // standaard velden
      ia.addFieldToDocument(TYPE, "id", file.getName());
	  // TODO Use URI analogue to MswordHandler
      ia.addFieldToDocument(TYPE, "url", file.getPath().replace(FILE_SEPARATOR, '/'));

    // Add the last modified date of the file a field named "modified".  Use a
    // Keyword field, so that it's searchable, but so that no attempt is made
    // to tokenize the field into words.
    //ia.addFieldToDocument(TYPE, "modified", DateTools.timeToString( file.lastModified(), DateTools.Resolution.MINUTE));

    String uid = file.getPath().replace(FILE_SEPARATOR, '\u0000') + "\u0000" + 
            DateTools.timeToString(file.lastModified(), DateTools.Resolution.MINUTE);

    // Add the uid as a field, so that index can be incrementally maintained.
    // This field is not stored with document, it is indexed, but it is not
    // tokenized prior to indexing.
    //ia.addFieldToDocument(TYPE, "uid", uid);

    FileInputStream input = null;
    try {
      input = new FileInputStream( file );
      addContent(input, file.getPath());
    } catch (Exception ex) {
      throw new DocumentHandlerException(ex.toString());
    } finally {
      if( input != null ) {
        try{ 
          input.close();
        } catch (Exception ex) {
          throw new DocumentHandlerException(ex.toString());
        }
      }
      ia.writeDocument(TYPE, writer);
    }
  }

  private void addContent(InputStream is, String documentLocation ) 
      throws DocumentHandlerException {
    
    try {
      PDDocument pdfDocument = null;
      try {
        pdfDocument = PDDocument.load( is );
        if ( pdfDocument.isEncrypted() ) {
          //Just try using the default password and move on
          pdfDocument.decrypt( "" );  
        }

        //create a writer where to append the text content.
        StringWriter writer = new StringWriter();
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.writeText( pdfDocument, writer );

        // Note: the buffer to string operation is costless;
        // the char array value of the writer buffer and the content string
        // is shared as long as the buffer content is not modified, which will
        // not occur here.
        String contents = writer.getBuffer().toString();
        byte[] tmp_byte = contents.getBytes("ISO-8859-1");
        contents = new String(tmp_byte);


        // Add the tag-stripped contents as a Reader-valued Text field so it will
        // get tokenized and indexed.
        ia.addFieldToDocument(TYPE, "content", Utilities.cleanString(contents));

        PDDocumentInformation info = pdfDocument.getDocumentInformation();
        if( info.getAuthor() != null ) {
          if (fields.contains("author"))
            ia.addFieldToDocument(TYPE, "author", info.getAuthor());
        }

        if( info.getCreationDate() != null ) {
          Date date = info.getCreationDate().getTime();
          //for some reason lucene cannot handle dates before the epoch
          //and throws a nasty RuntimeException, so we will check and
          //verify that this does not happen
          if( date.getTime() >= 0 ) {
            if (fields.contains("creationDate"))
              ia.addFieldToDocument(TYPE, "creationDate", DateTools.dateToString( date, DateTools.Resolution.DAY )) ;
          }

        }

        if( info.getCreator() != null )
          if (fields.contains("creator"))
            ia.addFieldToDocument(TYPE, "creator", info.getCreator());  
        if( info.getKeywords() != null )
          if (fields.contains("keywords"))
            ia.addFieldToDocument(TYPE, "keywords", info.getKeywords() );
        if( info.getModificationDate() != null ) {
          Date date = info.getModificationDate().getTime();
          //for some reason lucene cannot handle dates before the epoch
          //and throws a nasty RuntimeException, so we will check and
          //verify that this does not happen

          if( date.getTime() >= 0 ) {
            if (fields.contains("modificationDate"))
              ia.addFieldToDocument(TYPE, "modificationDate", DateTools.dateToString( date, DateTools.Resolution.MINUTE));
          }
        }

        if ( info.getProducer() != null )                
          if (fields.contains("producer"))
            ia.addFieldToDocument(TYPE, "producer", info.getProducer()  );

        if( info.getSubject() != null )
          if (fields.contains("subject"))
            ia.addFieldToDocument(TYPE, "subject", info.getSubject()  );

        if( info.getTitle() != null )
          if (fields.contains("title"))
            ia.addFieldToDocument(TYPE, "title", info.getTitle()  );

        if( info.getTrapped() != null )
          if (fields.contains("trapped"))
            ia.addFieldToDocument(TYPE, "trapped", info.getTrapped() );

        int summarySize = Math.min( contents.length(), 1000 );
        String summary = contents.substring( 0, summarySize );

        // Add the summary as an UnIndexed field, so that it is stored and returned
        // with hit documents for display.
        if (fields.contains("summary"))
          ia.addFieldToDocument(TYPE, "summary", summary );    
        
      } catch( CryptographyException e ) {
        throw new IOException( "Error decrypting document(" + documentLocation + "): " + e );
      } catch( InvalidPasswordException e ) {
        // they didn't suppply a password and the default of "" was wrong.
        throw new IOException( "Error: The document(" + documentLocation +
                  ") is encrypted and will not be indexed." );
      } finally {
        if ( pdfDocument != null )
          pdfDocument.close();   
      }

    } catch (IOException e) {
      throw new DocumentHandlerException("Cannot extract text from PDF file: " 
              + documentLocation + " ('" + e.getMessage() + "')", e);
    }
  }    
}
