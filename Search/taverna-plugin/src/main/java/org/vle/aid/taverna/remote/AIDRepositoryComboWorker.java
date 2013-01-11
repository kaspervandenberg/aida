package org.vle.aid.taverna.remote;

import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.jdesktop.swingworker.SwingWorker;
import org.vle.aid.taverna.components.Gui;

public class AIDRepositoryComboWorker extends SwingWorker<String[], Integer> {
	JComboBox resultComboBox;

	public AIDRepositoryComboWorker(JComboBox repositoriesCombo) {
		this.resultComboBox = repositoriesCombo;
	}

	@Override
	protected String[] doInBackground() throws Exception {
		AIDRemoteQuery repository = AIDRemoteQuery.getDefaultRepository();
		String[] repositories = new String[0];

		try {
			repositories = repository.getRepositories("r");
		} catch (Exception e) {
			Gui.showErrorWarnings("Failed to retrieve repositories from "
					+ repository.getServerUrl(), e);

		}
		return repositories;
	}

	@Override
	protected void done() {
		try {
			resultComboBox.setModel(new DefaultComboBoxModel(get()));

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

	}
}