package indexer;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 *
 * @author Machiel Jansen, Edgar Meij
 */
public class AnalyzerFactory {
    
  private File stopfileForStandard=null;
  private File stopfileForStop=null;
  private final ConfigurationHandler cfg;
  
  /** logger for Commons logging. */
  private transient Logger log =
    Logger.getLogger("AnalyzerFactory.class.getName()");
  
  /** Creates a new instance of AnalyzerFactory */
  public AnalyzerFactory(ConfigurationHandler cfg) {
    
    stopfileForStandard = new File("cfg/standardstopwords.cfg");
    stopfileForStop = new File("cfg/stopwords.cfg");
    
    if (!stopfileForStandard.exists())
      stopfileForStandard = null;
    if (!stopfileForStop.exists())
      stopfileForStop = null;
    
    this.cfg = cfg;
  }
  
  /** Returns the Analyzer for this doctype
   * @param       DocType         Document type to use
   * @return                      Analyzer
   * @todo  Support custom loading of stopword files
   */
  public Analyzer getAnalyzer(String DocType) {
    String Type = cfg.getDocumentAnalyzer(DocType);
    return getAnalyzerByCode(Type);
  }

  /** Returns the general Analyzer 
   * @return                      Analyzer
   * @todo  Support custom loading of stopword files
   */
  public Analyzer getGlobalAnalyzer() {
    return getAnalyzerByCode(cfg.getGlobalAnalyzer());
  }

  private Analyzer getAnalyzerByCode(String Code) {
    
    Analyzer result;
    
    try {
      if (Code.equalsIgnoreCase("STOP")) {
        if (stopfileForStop == null)
          result = new StopAnalyzer();
        else 
          result = new StopAnalyzer(stopfileForStop);
        return result;
      }
      
      if (Code.equalsIgnoreCase("STANDARD")) {
        // To read from the jar:
        //InputStream in = this.getClass().getResourceAsStream("standardstopwords.cfg");
        
        if (stopfileForStandard == null)
          result = new StandardAnalyzer();
        else 
          result = new StandardAnalyzer(stopfileForStandard);
        
        return result;
      }     
      
      if (Code.equalsIgnoreCase("WHITESPACE")) {
        result = new WhitespaceAnalyzer();
        return result;  
      }
      
      if (Code.equalsIgnoreCase("SIMPLE")) {
        result = new SimpleAnalyzer();
        return result;
      } else {
        String className = new String(Code + "Analyzer");
        Class cls = Class.forName(className);
        Analyzer analyzer = (Analyzer)cls.newInstance();
        return analyzer;  
      }
    } catch(Exception ex) {
      if (log.isLoggable(Level.SEVERE))
        log.severe(ex.toString());
    }
    
    return new StandardAnalyzer();
  }
    
}
