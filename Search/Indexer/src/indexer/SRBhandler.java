package indexer;

import edu.sdsc.grid.io.local.*;
import edu.sdsc.grid.io.srb.*;
import edu.sdsc.grid.io.*;

import java.net.URI;
import java.io.*;

/**
 *
 * @author Machiel Jansen, Edgar Meij
 */
public class SRBhandler {
  private final String HOST = "srb.grid.sara.nl";
  private final int PORT = 50000;
  private final String DOMAIN = "vlenl";
  private final String STORAGE = "vleGridStore";

  private SRBAccount account = null;
  private String homeDirectory = "";
  //private String LOCALPATH;

  /** Creates a new SRBhandler */
  public SRBhandler() {

  }

  /** Set login credentials
   * @param       userName        username
   * @param       password        password
  */
  public void setAccount(String userName, String password) {
    try {
      //System.err.println("PREV VERSION: " + SRBAccount.getVersion());
      //SRBAccount.setVersion(SRBAccount.SRB_VERSION_3_0_2);
      homeDirectory= "/VLENL/home/" + userName + "." + DOMAIN + "/";
      account = new SRBAccount(HOST, PORT, userName, password, homeDirectory, DOMAIN, STORAGE);
      account.setOptions(SRBAccount.ENCRYPT1);
     } catch(Exception ex) {
       System.err.println(ex.toString());
     }
  }
  
  private void copySRBdataDir(String fromPath, String destinationDir) {
    try {
      SRBFileSystem srbFileSystem = new SRBFileSystem( account );
      SRBFile fromFile = new SRBFile(srbFileSystem, fromPath);
      if (fromFile.isFile()) {
        LocalFile f = new LocalFile(fromFile.getName());
        fromFile.copyTo(f);
      }
      srbFileSystem.close();
    } catch(Exception ex) {
        System.err.println(ex.toString());
    }  
  }

  /** Retrieves the SRB files
   * @param       fileName           file to get
   * @param       destinationDir    
   * @return      errorcode?
  */
  public String getSRBFile(String fileName, String destinationDir) {
    try {
      String destPath = checkPath(destinationDir);   
      String fromPath = "./" + fileName;     
      SRBFileSystem srbFileSystem = new SRBFileSystem( account );
      SRBFile fromFile = new SRBFile(srbFileSystem, fromPath);
      getSRBdata(fromFile, destinationDir);
      return fromFile.getPath();
    } catch(Exception ex) {
      System.err.println(ex.toString());
    }
      return "";
  }

  private void getSRBdata(SRBFile file, String destinationDir) {
    if (file.canRead()) {
      if (file.isDirectory()) {
        String[] files = file.list();
        // an IO error could occur
        if (files != null) {
          for (int i = 0; i < files.length; i++) {
            getSRBdata(new SRBFile(file, files[i]), destinationDir);
          }
        }
      } else {   
        LocalFile f = new LocalFile(destinationDir + file.getPath().substring(1));
        try{
          file.copyTo(f);
        } catch(Exception ex){ }  
      }
    }
  }

  private String checkPath(String path) {
    if (path.startsWith("../"))
      checkPath(path.substring(3, path.length()));
    else
      return path;
 
    return "";
  }
  
  /** Does the actual indexing???
   * @param       Path          File to index
  */
  public void indexSRBFile(String Path) {
    String dest = account.getUserName();
    getSRBFile(Path, dest);
  }
}