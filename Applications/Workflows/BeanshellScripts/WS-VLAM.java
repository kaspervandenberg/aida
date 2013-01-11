/*
input
	nColumn (column that contains the values to run SigWin on in the tab delimited input file)
	filename (name of tab delimited data input file)
	base (localGlobusDirectory, directory of installed globus toolkit with the lib directory)
	vlamHome (location on local Grid node where the vlam engine is installed)
	vlamWorkflowName (name of xml file containing the vlam workflow)
output
	SigWinScorePerGeneURL (URL of file containing per gene the 'significant window' score that reflects the representation of the gene in significant windows of all window sizes, weighted per window size (smallest and largest windows count less)
	SigWinsURL (URL of file containing the ranges of significant windows, previously ridges, per window size)
	
	The library wsvlam-gui.jar from the VLAM client installation (e.g. D:\ProgramFiles\VL-e\ws-vlam-client\composer\lib) has to be copied to the Taverna lib directory
*/

import java.util.Vector;
import org.globus.bootstrap.*;

// String base = "D:\ProgramFiles\VL-e\ws-vlam-client\composer\lib\auxlib\gt4.1";// was: System.getenv("GLOBUS_LOCATION"); // possibly 
// String vlamHome = "/home/wibisono/myvlam/composer/"; // the directory where the VLAM workflows are at the local machine(e.g. D:\Marco\vlam\module_descriptions\templates)

System.setProperty("GLOBUS_LOCATION",base);
addClassPath(base+"\lib\bootstrap.jar");

Bootstrap boot = new Bootstrap();
boot.addDirectory(base); // add necessary globus libs, // this can possibly be done the Taverna way (also dirty), i.e. by copying all neccesary libs to the lib directory in the Taverna program directory, e.g. c:\\WINNT\profiles\<your_windows_name>\Application Data\<Taverna program directory>\lib (see Dependencies tab of the beanshell)
boot.addLibDirectory(base); // idem
boot.addLibDirectory(vlamHome); // idem

String [] wsvlamDesc = new String[5];
wsvlamDesc[0] = vlamHome+vlamWorkflowName; // see vlamHome above
wsvlamDesc[1] = "ColumnReader.file_name";
wsvlamDesc[2] = fileName; // data file reference, must be on the machine where the workflow is run
wsvlamDesc[3] = "ColumnReader.column";
wsvlamDesc[4] = nColumn;

/* code to clear the output files, such that they do not exist if the workflow is running or failing? */

boot.launch("nl.wtcw.vle.gui.monitor.SetupAndLaunchTopology", wsvlamDesc); // the client

/* code to poll the existence of the output files before instantiating the outputURLs? */

String SigWinScorePerGeneURL = "/home/inda/data/SigWinScorePerGeneURL.dat"; // ? what could be the filename+path here?
String SigWinsURL = "/home/inda/data/SigWinsURL.dat"; // ? idem