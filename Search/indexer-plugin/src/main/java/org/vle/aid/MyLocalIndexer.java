package org.vle.aid; 

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import org.apache.log4j.Logger;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import indexer.*;


/**
 * TODO: document AID Indexer helper
 * 
 * @author 
 *
 */
public class MyLocalIndexer implements LocalWorker 
{
	private static Logger logger = Logger.getLogger(MyLocalIndexer.class);

	//TODO: define your input names
	public String [] inputNames() {
		return new String [] {"Data file or dir", "configuration file", "Index location"};
	}

	//TODO: define your input mime types
	public String [] inputTypes() {
		return new String[] {"'text/plain'","'text/plain'","'text/plain'"};
	}

	//TODO: define your output names
	public String [] outputNames() {
		return new String[] {"result"};
	}

	//TODO: define your output mime types
	public String [] outputTypes() {
		return new String[] {"'text/plain'"};
	}

	public Map execute(Map inputMap) throws TaskExecutionException {

		Map outputMap = new HashMap();
		DataThingAdapter outputAdapter = new DataThingAdapter(outputMap);
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);

		String inputdir = inAdapter.getString("Data file or dir");
		String config   = inAdapter.getString("configuration file");
		String location = inAdapter.getString("Index location");
		
		if (config == null || config.equals(""))
			throw new TaskExecutionException("The 'configuration file' can not be null.");
		
		try {
	    ConfigurationHandler ch = new ConfigurationHandler(config);
      List<String> docTypes = ch.getDocumentTypes();
	    
	    logger.info("-- Using Global Analyzer: " + ch.getGlobalAnalyzer());
	    logger.info("-- Using Medline Analyzer: " + ch.getDocumentAnalyzer("medline"));
	    logger.info("-- Defined document types in config file: " + docTypes);
	    
	    // Print all *defined* fieldtypes
	    for(Iterator it = docTypes.iterator(); it.hasNext();) {
	    	String docType = (String) it.next();
	    	logger.info("-- Defined fields for " + docType + " in config file: \n--- ");
	    	
	    	String[] fields = ch.getFields(docType);
	    	for (int x=0; x<fields.length; x++) {
	    		logger.info(fields[x] + " ");
	    	}
	    }
	    
	    // Let's go..
	    Indexer i = new Indexer();
			String resultString = i.indexFromCFG(config, location, inputdir);
			//"machiel.jansen", "jacq357", "medline/100.med");
			outputAdapter.putString("result", resultString);
			logger.info(resultString);
			
		} catch(Exception e) {
			logger.error("Error in execute!",e);
			throw new TaskExecutionException(e);
		}
		
		return outputMap;
	}
}
