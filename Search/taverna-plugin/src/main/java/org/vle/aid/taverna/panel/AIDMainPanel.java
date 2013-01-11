package org.vle.aid.taverna.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.vle.aid.taverna.components.Gui;



/**
 * aida-plugin
 * @author wibisono
 * @date Apr 23, 2009 10:42:38 PM
 */
@SuppressWarnings("serial")
public class AIDMainPanel extends JXTaskPaneContainer implements PropertyChangeListener  {
    	AIDBrowsePanel browser   	 = null;
    	AIDSearchPanel search   	 = null;
    	AIDQueryPanel query      	  = null;
    	AIDServerConfigPanel sesameServerConfig= null;
    	AIDSkosLensConfigPanel skosLensConfig  = null;
    	JSplitPane browseSearch 	 = null;
    	
	public AIDMainPanel() {

	 
		browser   				= new AIDBrowsePanel();
		sesameServerConfig 		= new AIDServerConfigPanel();
		skosLensConfig			= new AIDSkosLensConfigPanel();
		//search     			= new AIDSearchPanel();
		//query 				= new AIDQueryPanel();
		
		// Let the search panel listens to the changes in AIDBuildTreeTableModel within browser
		// browser.addBuildTreeTableModelListener(search);
		
		// Let this main panel listen to property changes in config panel e.g REFRESH_HOST
		skosLensConfig.addPropertyChangeListener(this);
		sesameServerConfig.addPropertyChangeListener(this);
		
		
		//Skos config listen to browser for updating attempted changes.
		browser.addPropertyChangeListener(skosLensConfig);
		
		add(browser);
		add(skosLensConfig);
		add(sesameServerConfig);		
		
		//add(query, "AIDA Query");
		
	}

	public static void main(String[] args) {
		Gui.showInFrame(new AIDMainPanel());
	}


	/**
	 * If host is changed this main  panel need to be refreshed using the new host.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
	    	if(evt.getPropertyName().equals("REFRESH_HOST")){
	    	    browser.updateRepositoriesCombo();
	    	}
	    	if(evt.getPropertyName().equals("REFRESH_TOPCONCEPT") ||evt.getPropertyName().equals("REFRESH_NARROWER")  ){
	    	    browser.updateBrowseTables();
	    	}
	}

	

}
