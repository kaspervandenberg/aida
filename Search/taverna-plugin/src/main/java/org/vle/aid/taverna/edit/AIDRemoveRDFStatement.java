package org.vle.aid.taverna.edit;

import java.awt.Cursor;

import javax.swing.tree.TreePath;

import org.jdesktop.swingworker.SwingWorker;
import org.vle.aid.taverna.browse.AIDBrowseTreeTable;
import org.vle.aid.taverna.browse.AIDBrowseTreeTableModel;
import org.vle.aid.taverna.browse.AIDBrowseTreeTableNode;
import org.vle.aid.taverna.remote.AIDRemoteQuery;

public class AIDRemoveRDFStatement {

	AIDBrowseTreeTableNode selectedNode, parentNode;
	AIDBrowseTreeTable treeTable;
	TreePath selectedPath;

	public AIDRemoveRDFStatement(AIDBrowseTreeTable treeTable, TreePath selectedPath){
		this.treeTable = treeTable;
		this.selectedPath = selectedPath;
		this.selectedNode = (AIDBrowseTreeTableNode) selectedPath.getLastPathComponent();
		this.parentNode = (AIDBrowseTreeTableNode) selectedPath.getParentPath().getLastPathComponent();
	} 
	public void removeRdf(){
		AIDRemoveRDFStatementWorker worker = new AIDRemoveRDFStatementWorker(treeTable, selectedPath);
		worker.execute();		
	}
	public boolean doIt(){
		AIDRemoteQuery remote = AIDRemoteQuery.getDefaultRepository();

		/** Predicates **/
		String type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		String broader = "http://www.w3.org/2004/02/skos/core#broader";
		String inScheme = "http://www.w3.org/2004/02/skos/core#inScheme";
		String prefLabel = "http://www.w3.org/2004/02/skos/core#prefLabel";

		/** Objects **/
		String concept_type = "http://www.w3.org/2004/02/skos/core#Concept";
		String parent_broader = parentNode.getUrl();
		String scheme = "http://www.afsg.nl/www_foodontology_nl/data/documenten/Ontology/TNO_FI-Ontology_2007_jan.owl#scheme";
		String label = selectedNode.getTerm();

		try {
			remote.removeRdfStatement(selectedNode.getUrl(), type, concept_type);
			remote.removeRdfStatement(selectedNode.getUrl(), broader, parent_broader);
			remote.removeRdfStatement(selectedNode.getUrl(), inScheme, scheme);
			remote.removeRdfStatement(selectedNode.getUrl(), prefLabel, label);	    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	class AIDRemoveRDFStatementWorker extends SwingWorker<Boolean, Integer> {
		AIDBrowseTreeTable treeTable;

		TreePath	   selectedPath;
		AIDBrowseTreeTableNode parentNode;

		AIDRemoveRDFStatementWorker(AIDBrowseTreeTable treeTable, TreePath selectedPath) {
			this.treeTable 		= treeTable;
			this.selectedPath 	= selectedPath;
			this.parentNode 	= (AIDBrowseTreeTableNode) selectedPath.getParentPath().getLastPathComponent();
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			treeTable.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			boolean result = doIt();	   
			parentNode.expandNode();
			return result;
		}

		protected void done() {
			treeTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			AIDBrowseTreeTableModel model = (AIDBrowseTreeTableModel) treeTable.getTreeTableModel();
			
			model.fireStructureChange(treeTable, selectedPath.getParentPath());
			treeTable.expandPath(selectedPath.getParentPath());

		}

	}
}
