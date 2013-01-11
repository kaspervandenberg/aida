package org.vle.aid.taverna.build;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.TreePath;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXTreeTable;
import org.vle.aid.taverna.browse.AIDBrowseTreeTableTransferHandle;
import org.vle.aid.taverna.panel.AIDSearchPanel;

/**
 * Transfer handler for handling drop action from browse tree, importing data into this current tree
 * Nodes will be expanded once they are dropped here. 
 * Nodes contains data packed with new lines from {@link AIDBrowseTreeTableTransferHandle}
 * 
 * Search panel is needed to be notified/updated when things are dropped in the build tree table.
 * aida-plugin
 * @author wibisono
 * @date Apr 24, 2009 5:17:22 PM
 */

@SuppressWarnings("serial")
public class AIDBuildTreeTableTransferHandle extends TransferHandler {
    @Override
    public boolean importData(JComponent comp, Transferable t) {
    	// TODO Auto-generated method stub
    
		JXTreeTable treeTable = (JXTreeTable) comp;

		AIDBuildTreeTableModel model = (AIDBuildTreeTableModel) treeTable.getTreeTableModel();
		AIDBuildTreeTableNode root = model.getRoot();

		String data = "";
		try {
			data = (String) t.getTransferData(DataFlavor.stringFlavor);
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] dataSplit = data.split("\n");
		String term = dataSplit[0], url = dataSplit[1], repository = dataSplit[2];
		root.addChild(term, url);

		AIDBuildTreeTableNode newNode = model.getChild(root, model.getChildCount(root) - 1);
		ExpandBuildTreeNodeWorker worker = new ExpandBuildTreeNodeWorker(repository, newNode, model, treeTable);
		worker.execute();

		return true;
	}
	
	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		return true;
	}
	/**
	 * Internal worker to expand node once node are dropped in {@link AIDBuildTreeTable}
	 * Once the work of expanding is done, underlaying table model will fire structure change and notify listeners
	 * 
	 * Currently {@link AIDSearchPanel} is also listening for changes and updating new query when this happens.
	 *  
	 * @author wibisono
	 *
	 */
	class ExpandBuildTreeNodeWorker extends
			SwingWorker<AIDBuildTreeTableNode, Integer> {
		String repositoryName;

		AIDBuildTreeTableNode root;

		AIDBuildTreeTableModel model;

		JXTreeTable tree;

		
		public ExpandBuildTreeNodeWorker(String repositoryName,
				AIDBuildTreeTableNode root, AIDBuildTreeTableModel model,
				JXTreeTable tree) {
			this.root = root;
			this.model = model;
			this.repositoryName = repositoryName;
			this.tree = tree;
		}

		@Override
		protected AIDBuildTreeTableNode doInBackground() throws Exception {
			tree.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			root.expandTree(repositoryName);
			return root;
		}

		protected void done() {
			try {
				AIDBuildTreeTableNode newRepositoryRoot = get();
				tree.setCursor(Cursor.getDefaultCursor());
				/* For builtin swinglabs treeModelChange event listener */	
				model.fireStructureChange(new TreeExpansionEvent(newRepositoryRoot, new TreePath(root)));
				/* Basically this is the one responsible notifying searchPanel */
				model.fireStructureChange(new TreeExpansionEvent(tree, tree.getPathForRow(0)));
				
			} catch (Exception e) {

			}
		}
	}
}
