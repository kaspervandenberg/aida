package org.vle.aid.taverna.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.TreeModelListener;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.vle.aid.taverna.browse.AIDBrowseTreeTable;
import org.vle.aid.taverna.browse.AIDBrowseTreeTableModel;
import org.vle.aid.taverna.browse.AIDBrowseTreeTableNode;
import org.vle.aid.taverna.build.AIDBuildTreeTable;
import org.vle.aid.taverna.components.Gui;
import org.vle.aid.taverna.components.JXTabbedPane;
import org.vle.aid.taverna.remote.AIDRemoteConfig;
import org.vle.aid.taverna.remote.AIDRemoteQuery;
import org.vle.aid.taverna.remote.AIDRepositoryComboWorker;
import org.vle.aid.taverna.search.AIDSearchSettings;




/**
 * {@link AIDBrowsePanel} contains both the {@link AIDBuildTreeTable} and {@link AIDBrowseTreeTable}
 * Maintains set of opened repository names to avoid opening the same repository twice
 * Listens to item selection of a repositoriesCombo, and act accordingly when user select new repositories either :
 * 		- open new tab
 * 		- select existing tabs
 * Listen to closeActions of the tabs based on jideTabbedPane, removing the opened repository from the sets.
 * 		- removed because jide components is troublesome in windows.
 * 
 * aida-plugin
 * @author wibisono
 * @date Apr 23, 2009 10:24:42 PM
 */
@SuppressWarnings("serial")
public class AIDBrowsePanel extends JXTaskPane implements ItemListener {

	/* Upper part of the panel to select from existing repository */
	JXTaskPane repositorySelect = null;

	/* Lower part of the panel for browsing repositories */
	JXTaskPane browserTaskPane = null;

	/* Tabbed pane for opened repositories */
	JXTabbedPane browserTab = null;

	/* Set of opened repository names */
	Set<String> openedRepositories = null;

	/* Background tab creator */
	CreateRepositoryTabWorker createTabWorker = null;

	/* Query Builder task pane */
	JXTaskPane builderTaskPane = null;

	/* Combo box for repositories */
	JComboBox repositoriesCombo = null;

	AIDBuildTreeTable buildTreeTable = new AIDBuildTreeTable();

	// Used to fire property change to SKOSLens config panel so that it updates atempted skoslens. 
	private  final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	public AIDBrowsePanel() {
	    		initializeBrowsePanel();
	}
	
	private void initializeBrowsePanel(){
//		buildTreeTable = new AIDBuildTreeTable();
//		builderTaskPane = new JXTaskPane();
//		builderTaskPane.setTitle("AIDA Concept Query Builder");
//
//		// Going to put this tree table into a scroll pane, if this is not set
//		// Default scrollable view port size is too big.
//		buildTreeTable.setPreferredScrollableViewportSize(new Dimension(250,250));
//		builderTaskPane.add(new JScrollPane(buildTreeTable));
//		builderTaskPane.setAnimated(false);
		setTitle("AIDA Thesaurus Browser");
		setAnimated(false);
		
		openedRepositories = new HashSet<String>();
		
		browserTab = new JXTabbedPane();
		browserTab.setClosingAction(new TabCloseAction());
		add(browserTab);
		
		repositoriesCombo = new JComboBox();
		repositoriesCombo.addItemListener(this);
		add(repositoriesCombo);
		
		createTabWorker = new CreateRepositoryTabWorker(AIDRemoteConfig.DEFAULT_REPOSITORY);
		createTabWorker.execute();
		AIDRepositoryComboWorker  createSelectWorker = new AIDRepositoryComboWorker(repositoriesCombo);
		createSelectWorker.execute();		
	}
	
	public void updateRepositoriesCombo(){
		repositoriesCombo.removeAllItems();
		AIDRepositoryComboWorker  createSelectWorker = new AIDRepositoryComboWorker(repositoriesCombo);
		createSelectWorker.execute();	
	}
	/**
	 * What to do when user close repository tab.
	 * Remove the opened repository from the set, and remove the tab.
	 * aida-plugin
	 * @author wibisono
	 * @date Apr 29, 2009 11:42:22 AM
	 */
	class TabCloseAction extends AbstractAction {
		public void actionPerformed(ActionEvent arg0) {
			int selectedIndex = browserTab.getSelectedIndex();
			String removedRepositoryName = browserTab.getTitleAt(selectedIndex);
			openedRepositories.remove(removedRepositoryName);
			browserTab.removeTabAt(selectedIndex);
		}
		
	}

	/* Let the panel/listener here listen to changes in build tree table model */
	public void addBuildTreeTableModelListener(TreeModelListener panel) {
		buildTreeTable.getTreeTableModel().addTreeModelListener(panel);
	}

	/**
	 * When user change selected repository :
	 * 		- if its already opened, select the existing tab
	 * 		- if its not already opened, spawn worker
	 */
	public void itemStateChanged(ItemEvent e) {
	
		if(e.getStateChange() == ItemEvent.SELECTED) {
			
			String repositoryName = e.getItem().toString();
	
			if (openedRepositories.contains(repositoryName)) {
				for (int i = 0; i < browserTab.getComponentCount(); i++)
					if (browserTab.getTitleAt(i).equals(repositoryName)) {
						browserTab.setSelectedIndex(i);
						break;
					}
			} else {
				createTabWorker = new CreateRepositoryTabWorker(repositoryName);
				createTabWorker.execute();
			}
			propertyChangeSupport.firePropertyChange("REFRESH_ATTEMPTEDLENS", "", repositoryName);
		}
	}
	
	/**
	 * Updating the browse tables that had already been opened with new Skos lenses.
	 * Triggered by REFRESH_TOP_CONCEPTS or with REFRESH_NARROWER_CONCEPTS
	 */
	public void updateBrowseTables () {
		
		int updatedIdx = browserTab.getSelectedIndex();
		if(updatedIdx < 0 ) return;
		
		String updatedRepository = browserTab.getTitleAt(updatedIdx);
		openedRepositories.remove(updatedRepository);
		browserTab.remove(updatedIdx);
		createTabWorker = new CreateRepositoryTabWorker(updatedRepository);
		createTabWorker.execute();
		
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
	       if(propertyChangeSupport != null) propertyChangeSupport.addPropertyChangeListener(listener);
	       super.addPropertyChangeListener(listener);
	}

	class CreateRepositoryTabWorker extends
			SwingWorker<AIDBrowseTreeTableNode, Integer> {
		String repositoryName;

		public CreateRepositoryTabWorker(String repositoryName) {
			this.repositoryName = repositoryName;
		}

		@Override
		protected AIDBrowseTreeTableNode doInBackground() throws Exception {


			
			/* Create root node */
			AIDRemoteQuery repository = AIDRemoteQuery.getDefaultThesaurusRepository();
			repository.setRepository(repositoryName);
			AIDBrowseTreeTableNode result = new AIDBrowseTreeTableNode(repository);
			
			
			return result;
		}

		protected void done() {
			try {
				AIDBrowseTreeTableNode newRepositoryRoot = get();

//				/* remove the busy tab we just added before */
//				browserTab.remove(browserTab.getTabCount() - 1);

				if (newRepositoryRoot != null) {
					AIDBrowseTreeTableModel treeModel = new AIDBrowseTreeTableModel(newRepositoryRoot);
					AIDBrowseTreeTable newBrowseTree  = new AIDBrowseTreeTable(treeModel);
					
					
					
//					newBrowseTree.setPreferredScrollableViewportSize(new Dimension(150,700));
					browserTab.add(new JScrollPane(newBrowseTree), repositoryName);

					/* Remember in hash that we have this new repository */
					openedRepositories.add(repositoryName);

//					/* Again activate the newest tab because we just removed the busy wheel ? */
					browserTab.setSelectedIndex(browserTab.getTabCount() - 1);

					
				}

			} catch (Exception ex) {
				Gui.showErrorWarnings("Failed to create tab for repository :" + repositoryName, ex);
			}
		}
	}
	
	
	
	public static void main(String[] args) {
		Gui.showInFrame(new JScrollPane(new AIDBrowsePanel()));
	}


	
	
}
