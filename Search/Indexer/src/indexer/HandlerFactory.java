package indexer;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Machiel Jansen, Edgar Meij
 */
public class HandlerFactory {
  
  private final ConfigurationHandler cfg;
  private String currentFile;
  
  /** logger for Commons logging. */
    private transient Logger log =
      Logger.getLogger("HandlerFactory.class.getName()");
       
  /** Creates a new instance of HandlerFactory */
  public HandlerFactory(ConfigurationHandler cfg) {
    this.cfg = cfg;
  }

  /**
   * Returns a handler for a given file to be indexed. 
   * The handler is chosen on the basis of the extension of the filename. The 
   * choice is made according to the xml config file
   *
   * @param   filename    File to get the handler for
   * @return              DocHandler to use          
   */
  public DocHandler getHandler(String filename) {
    
    String extension = Utilities.getExtension(new File(filename));
    String name = cfg.getDocType(extension);
    
    //System.err.println("extension of " + filename + " is " + extension + " -----> name: " + name);
    currentFile = filename;
    
    // no handler found
    if (name == null) {
      if (extension == null || extension.length() == 0) {
        return loadHandler("");
      } else {
        name = extension;
      }
    }
    
    String handler = 
              "indexer." 
              + name.substring(0,1).toUpperCase() 
              + name.substring(1) 
              + "Handler";
    


      return loadHandler(handler);

      
    
    /*
     if (filename.equalsIgnoreCase("msword") || extension.equalsIgnoreCase("doc"))
      name = "indexer.MSWordHandler";
    else if (filename.equalsIgnoreCase("medline") || extension.equalsIgnoreCase("med") )
      name = "indexer.MedlineHandler";
    else if (filename.equalsIgnoreCase("text") || extension.equalsIgnoreCase("text") 
            || filename.equalsIgnoreCase("txt") || extension.equalsIgnoreCase("txt"))
      name = "indexer.TxtHandler";
    else 
      name = "indexer." + name.substring(0,1).toUpperCase() + name.substring(1) + "Handler";
     */
    
    
  }

  private DocHandler loadHandler(String name) {
    try {
      Class<?> cls = Class.forName(name);
      Constructor<?> con = cls.getConstructor(new Class[] {ConfigurationHandler.class});
      DocHandler result= (DocHandler)con.newInstance(new Object[] {cfg});

      return result;
      
    } catch(ClassNotFoundException ex) {
      if (log.isLoggable(Level.SEVERE))
        log.severe("Found a file with no handler defined: " + currentFile 
              + " (needs a '" + name + "' class)");
      //log.severe("Could not index '"+currentFile+"': " + ex.toString());
      //ex.printStackTrace();
    } catch(NoSuchMethodException ex) {
      //log.severe("Could not index '"+currentFile+"': " + ex.toString());
      //ex.printStackTrace();
    } catch(InstantiationException ex) {
      //log.severe("Could not index '"+currentFile+"': " + ex.toString());
      //ex.printStackTrace();
    } catch(IllegalAccessException ex) {
      //log.severe("Could not index '"+currentFile+"': " + ex.toString());
      //ex.printStackTrace();
    } catch(InvocationTargetException ex) {
      //log.severe("Could not index '"+currentFile+"': " + ex.toString());
      //ex.printStackTrace();
    }
    
    return null;
  }
    
}
