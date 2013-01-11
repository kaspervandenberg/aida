package org.vle.aid.taverna.search;

import java.util.List;
import java.util.Vector;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;



import org.jdesktop.swingx.LinkModel;
import org.json.JSONArray;
import org.json.JSONObject;

public class AIDSearchResultModel implements TableModel {
	List<AIDSearchResultItem> rows = new Vector<AIDSearchResultItem>();
	String query;
	// Number of hits found in this query.
	int hits;
	
	public AIDSearchResultModel() {}
	
	public AIDSearchResultModel(JSONObject searchResult) {
	    try {
	    		hits   = searchResult.getInt("hits");
	    		query = searchResult.getString("query");    		
	    		JSONArray items =  searchResult.getJSONArray("items");
	    		for(int i=0;i<items.length();i++){
	    		    	JSONObject item = items.getJSONObject(i);
	    		    	AIDSearchResultItem newItem = new AIDSearchResultItem(
	    		    		new Float(item.getString("score")),
	    		    		item.getString("snippet"),
	    		    		item.getString("description"),
	    		    		item.getString("id"),
	    		    		item.getString("label"),
	    		    		item.getString("uri"),
	    		    		item.getString("title")
	    		    	); 
	    		    	rows.add(newItem);
	    		}
	    } catch(Exception e){
			
	    }
	}

	public AIDSearchResultModel(List<AIDSearchResultItem> rows) {
		super();
		this.rows = rows;
	}

	public void addTableModelListener(TableModelListener arg0) {
		// TODO Auto-generated method stub

	}

	public Class<?> getColumnClass(int col) {
		// TODO Auto-generated method stub
		switch(col){
			case 0: return LinkModel.class;
			case 1: return String.class;
			case 2: return Float.class;			
		}
		return null;
	}

	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	public String getColumnName(int col) {
		// TODO Auto-generated method stub
		switch(col){
			case 0: return "<html><b>Title</b></html>";
			case 1: return "<html><b>Excerpt</b></html>";
			case 2: return "Score";
		}
		return null;
	}

	public int getRowCount() {
		// TODO Auto-generated method stub
		return rows.size();
	}

	public Object getValueAt(int row, int col) {
		// TODO Auto-generated method stub
		AIDSearchResultItem item = rows.get(row);
		switch(col){			
			case 0: return item.getUri();
			case 1: return item.getSnippet();
			case 2: return item.getScore();
		}
		
		return null;
	}

	public boolean isCellEditable(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeTableModelListener(TableModelListener arg0) {
		// TODO Auto-generated method stub

	}

	public void setValueAt(Object arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}
	public void addRow(AIDSearchResultItem item){
		rows.add(item);
	}

	public int getHits() {
		// TODO Auto-generated method stub
		return hits;
	}
}
