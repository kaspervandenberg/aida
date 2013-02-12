/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vle.aid.lucene.tools;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFParseException;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

/**
 *
 * @author emeij
 */
public class Thumbnails {
  
  private static final int TYPE = BufferedImage.TYPE_INT_RGB;//TYPE_USHORT_565_RGB 
  private static final int boxSize = 512;//128;
  
  // The default JPG to use in case the thumbnail creation fails.
  public static final File DEFAULT_THUMBNAIL = new File("." + File.separator + "web" + File.separator + "images" + File.separator + "empty.jpg");
  //new File("." + File.separator + "web" + File.separator + "images" + File.separator + "empty.jpg");
  
  /** logger for Commons logging. */
  private static Logger log = Logger.getLogger("Thumbnails.class.getName()");
  
  /**
   * Creates a jpg thumbnail for a given PDF file and saves it as infile.jpg.
   * 
   * @param infile
   * @throws java.io.IOException
   */
  public static void createthumbnail(File infile) {

    File outfile = new File(infile.getAbsolutePath() + ".jpg");

    if (outfile.exists())
      return;
    
    FileOutputStream fos = null;
    try {
      // load a pdf from a byte buffer
      RandomAccessFile raf = new RandomAccessFile(infile, "r");
      FileChannel channel = raf.getChannel();
      ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
      PDFFile pdffile = new PDFFile(buf);

      // draw the first page to an image
      PDFPage page = pdffile.getPage(0);

      //get the width and height for the doc at the default zoom 
      Rectangle rect = new Rectangle(0, 0,
              (int) page.getBBox().getWidth(),
              (int) page.getBBox().getHeight());

      //generate the image
      Image img = page.getImage(
              rect.width, rect.height, //width & height
              rect, // clip rect
              null, // null for the ImageObserver
              true, // fill background with white
              true // block until drawing is done
              );

      fos = new FileOutputStream(outfile);
      MemoryCacheImageOutputStream os =
              new MemoryCacheImageOutputStream(fos);
      // Constrain the thumbnail to a predefined box size
      int height = boxSize;
      int width = boxSize;

      if (rect.height > rect.width) {
        width = (int) (((float) height / (float) rect.height) * (float) rect.width);
      } else if (rect.width > rect.height) {
        height = (int) (((float) width / (float) rect.width) * (float) rect.height);
      }

      // Create a new thumbnail BufferedImage
      BufferedImage thumb = new BufferedImage(width, height, TYPE);
      Graphics g = thumb.getGraphics();
      g.drawImage(img, 0, 0, width, height, null);

      // Get Writer and set compression
      Iterator iter = ImageIO.getImageWritersByFormatName("JPG");
      if (iter.hasNext()) {
        ImageWriter writer = (ImageWriter) iter.next();
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality(0.75f);
        writer.setOutput(os);
        IIOImage image = new IIOImage(thumb, null, null);
        writer.write(null, image, iwp);
      } else {
        throw new IOException("No suitable encoder found");
      }
    } catch (PDFParseException ppe) {
      if (log.isLoggable(Level.SEVERE)) {
        log.severe(infile + " produced an error: " + ppe.toString());
      }
      try {
        createDefaultThumbnail(infile);
      } catch (IOException ioe) {
        if (log.isLoggable(Level.SEVERE)) {
          log.severe("createDefaultThumbnail() produced an error: " + ioe.toString());
        }
      }
    } catch (Exception e) {
      if (log.isLoggable(Level.SEVERE)) {
        log.severe(infile + " produced an error: " + e.toString());
      }
      try {
        createDefaultThumbnail(infile);
      } catch (IOException ioe) {
        if (log.isLoggable(Level.SEVERE)) {
          log.severe("createDefaultThumbnail() produced an error: " + ioe.toString());
        }
      }
    } finally {
      try {
        if (fos != null) {
          fos.close();
        }
      } catch (IOException ioe) {
        if (log.isLoggable(Level.SEVERE)) {
          log.severe("Could not close FileOutputStream: " + ioe.toString());
        }
      }
      
      log.info("Created thumbnail for " + infile.getName());
    }

  }

  private static void createDefaultThumbnail(File infile) throws IOException {


    //log.info("Children of " + "./web/images");
    //File[] ls = new File("./web/images").listFiles();
    //for (File f : ls)  {
    //log.info(" " + f.getName());
    //}

    //log.info("1st child of " + DEFAULT_THUMBNAIL.getName() + ": "+ DEFAULT_THUMBNAIL.listFiles()[0].getName());

    log.info("generating default thumbnail: " + new File(infile.getAbsolutePath() + ".jpg"));
    InputStream in = new FileInputStream(DEFAULT_THUMBNAIL);
    OutputStream out = new FileOutputStream(new File(infile.getAbsolutePath() + ".jpg"));

    // Transfer bytes from in to out
    byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
    in.close();
    out.close();
  }
}
