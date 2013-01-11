package org.vle.aid.taverna.remote;

import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXTable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.vle.aid.taverna.components.Gui;
/**
 * Background worker for performing query
 * aida-plugin
 * @author wibisono
 * @date Apr 23, 2009 11:37:50 PM
 */
public class AIDDirectQueryWorker extends SwingWorker<String, Integer> {
	String queryString;
	String queryLanguage;
	JXTable resultTable = null;
	AIDRemoteQuery remoteQuery = AIDRemoteQuery.getDefaultRepository();

	public AIDDirectQueryWorker(JXTable resultTable, String queryString, String queryLanguage, String repository) {
		this.resultTable = resultTable;
		this.queryLanguage = queryLanguage;
		this.queryString = queryString;
		remoteQuery.setRepository(repository);
	}

	/**
	 * Real background work
	 */
	@Override
	protected String doInBackground() throws Exception {
		
		String result = null;
		try {
			result= remoteQuery.selectQuerySerialized(queryLanguage, "json", queryString);
		} catch (Exception ex){
			Gui.showErrorWarnings("Failed to perform query on "+remoteQuery, ex);
		}
		return result;
	}
	/**
	 * When its done, create result table 
	 */
	protected void done() {
		try {
			String jsResult = get();
			if (jsResult != null) {
				String [] header=null;
				String [][] result = null;
				
				/** Hackish parsing JSON assuming this structure 
				 * I'll work on this later, the non json version does not have header, so I took this one:
				 * {
						"head": {
							"vars": [ "dum", "dim", "dam" ]
						}, 
						"results": {
							"bindings": [
								{
									"dum": { "type": "uri", "value": "http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#type" }, 
									"dim": { "type": "uri", "value": "http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#type" }, 
									"dam": { "type": "uri", "value": "http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#Property" }
								}, 
								{
									"dum": { "type": "uri", "value": "http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#type" }, 
									"dim": { "type": "uri", "value": "http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#type" }, 
									"dam": { "type": "uri", "value": "http:\/\/www.w3.org\/2000\/01\/rdf-schema#Resource" }
								}
						}
					}
				 */
				JSONObject objResult = new JSONObject(jsResult);
				
				/* Get the headers */
				JSONObject objHeader = objResult.getJSONObject("head");
				JSONArray vars = objHeader.getJSONArray("vars");
				header = new String[vars.length()];
				for(int i=0;i<header.length;i++) header[i] = vars.getString(i);
				
				/* Binding is an array of JSON, each element is a row in query result */
				JSONArray rows = objResult.getJSONObject("results").getJSONArray("bindings");
				/* Allocate result */
				result = new String[rows.length()][header.length];
				/* Fill in result in array of string result */
				for(int i=0;i<result.length;i++)
					for(int j=0;j<header.length;j++)
						result[i][j] = rows.getJSONObject(i).getJSONObject(header[j]).getString("value");
				
				resultTable.setModel(new DefaultTableModel(result, header));

			
			} else {
				resultTable.setModel(new DefaultTableModel(new String [][]{{"No result found"}}, new String[]{"No result found"}));
			}
		} catch (Exception e) {
			resultTable.setModel(new DefaultTableModel(new String [][]{{"No result Found"}}, new String[]{"No result found"}));
		}
	}
}