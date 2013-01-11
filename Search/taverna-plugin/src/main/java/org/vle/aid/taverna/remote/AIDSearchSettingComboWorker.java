package org.vle.aid.taverna.remote;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.jdesktop.swingworker.SwingWorker;
import org.vle.aid.taverna.components.Gui;

public class AIDSearchSettingComboWorker extends SwingWorker<String[], Integer> {
	JComboBox indexComboBox;
	JComboBox fieldsComboBox;
	HashMap<String, String []> indexToField;
	
	public AIDSearchSettingComboWorker(JComboBox indexCombo, JComboBox fieldsCombo, HashMap<String, String[]>indexMap) {
		indexComboBox = indexCombo;
		fieldsComboBox = fieldsCombo;
		indexToField = indexMap;
	}

	@Override
	protected String[] doInBackground() throws Exception {
		AIDRemoteQuery repository = AIDRemoteQuery.getDefaultRepository();
		String[] indexes = new String[0];

		try {
			indexes = AIDRemoteSearch.getIndexes();
			for(String index : indexes){
			    	indexToField.put(index, AIDRemoteSearch.listFields(index));
			}
		} catch (Exception e) {
			Gui.showErrorWarnings("Failed to retrieve repositories from "
					+ repository.getServerUrl(), e);

		}
		return indexes;
	}

	@Override
	protected void done() {
		try {
		    	String [] indexes = (String[]) get();
			indexComboBox.setModel(new DefaultComboBoxModel(indexes));
			//indexComboBox.setSelectedIndex(9); // set to medline by default :p Hard code for demo
			if(indexes.length > 0)
			    fieldsComboBox.setModel(new DefaultComboBoxModel(indexToField.get(indexes[0])));
			//fieldsComboBox.setSelectedIndex(3); // set to medline content :P
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

	}
}