package org.vle.aid.taverna.search;

import java.awt.Cursor;
import java.util.concurrent.ExecutionException;

import org.jdesktop.swingworker.SwingWorker;
import org.json.JSONObject;
import org.vle.aid.taverna.components.Gui;
import org.vle.aid.taverna.panel.AIDSearchPanel;
import org.vle.aid.taverna.remote.AIDRemoteSearch;

/**
 * Worker responsible to perform search, mostly instantiated by {@link AIDSearchPanel} 
 * 
 * aida-plugin
 * @author wibisono
 * @date Apr 29, 2009 11:02:56 AM
 */
public class AIDSearchQueryWorker extends SwingWorker<JSONObject, Integer> {
	
	/** Temporarily before I implement the rest options for index, start, count, field, i'll use these defaults */
	String queryString="", field ="content", index = "MedLine";
	Integer start = 1, count = 10;
	AIDSearchPanel panel;
	
	public AIDSearchQueryWorker(String queryString, AIDSearchPanel panel) {
		this.queryString = queryString;
		this.panel = panel;
	}
	public AIDSearchQueryWorker(String index, String queryString, Integer start, String field, Integer count, AIDSearchPanel panel) {
		this.index = index;   
		this.queryString = queryString;
		this.start = start;
		this.field = field;
		this.count = count;
		this.panel = panel;
		
		
	}
	
	@Override
	protected JSONObject doInBackground() throws Exception {
		//System.out.println("Executing "+index +" |"+queryString + "| "+start+" "+field+" "+count);
		panel.setBusy(true);
		if(queryString == null || queryString.trim().length()==0) return null;
		return AIDRemoteSearch.search(index, queryString, start,field, count);
	}
	@Override
	protected void done() {
		try {
			panel.update(get());			
			panel.setBusy(false);
		} catch (InterruptedException e) {
			Gui.showErrorWarnings("Query failed",e);
		} catch (ExecutionException e) {
			Gui.showErrorWarnings("Query failed",e);
		}
	}
}