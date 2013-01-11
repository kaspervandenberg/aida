package org.vle.aid.taverna.browse;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXFindBar;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.vle.aid.taverna.AIDEditDataflow;
import org.vle.aid.taverna.edit.AIDAddRDFStatement;
import org.vle.aid.taverna.edit.AIDRemoveRDFStatement;

/**
 * 
 * Thesaurus Tree Table that should update lazily, triggered by expansion of the
 * tree.
 * 
 * Listens to tree expansion and spawn worker to expand node if needed.
 * 
 * aida-plugin
 * 
 * @author wibisono
 * @date Apr 23, 2009 10:39:10 PM
 */

@SuppressWarnings("serial")
public class AIDBrowseTreeTable extends JXTreeTable implements
		TreeExpansionListener, ActionListener {
	JXFindBar findbar;

	public AIDBrowseTreeTable(AIDBrowseTreeTableModel model) {
		super(model); // Wohooo !
		decorate();

		// Enable and setup drag and drop transfer handlers
		setDragEnabled(true);
		setTransferHandler(new AIDBrowseTreeTableTransferHandle());

		// Tree expansion which will spawn browser
		addTreeExpansionListener(this);

		// Mouse event Listener for popups
		addMouseListener(new AIDBrowserPopupAdapter());
		initializePopupMenu();
		setDefaultRenderer(String.class, new AIDBrowseTreeTableCellRenderer());
	}

	public AIDBrowseTreeTable() {
		// TODO Auto-generated constructor stub
		super(new AIDBrowseTreeTableModel());
	}

	private void decorate() {
		// Add a findbar to allow user search the tree
		findbar = new JXFindBar(getSearchable());
		setColumnControlVisible(true);
		setHighlighters(new CompoundHighlighter());
		setHighlighters(HighlighterFactory
				.createSimpleStriping(HighlighterFactory.GENERIC_GRAY));
		
		
	}

	public void treeExpanded(TreeExpansionEvent evt) {
		AIDBrowseTreeTableNode node = (AIDBrowseTreeTableNode) evt.getPath()
				.getLastPathComponent();
		if (!node.expanded()) {
			AIDTreeNodeExpandWorker worker = new AIDTreeNodeExpandWorker(evt
					.getSource(), evt.getPath());
			worker.execute();
		}
	}

	public void treeCollapsed(TreeExpansionEvent evt) {

	}

	/**
	 * Worker class to expand tree node. aida-plugin
	 * 
	 * @author wibisono
	 * @date Apr 23, 2009 10:48:27 PM
	 */
	class AIDTreeNodeExpandWorker extends
			SwingWorker<AIDBrowseTreeTableNode[], Integer> {
		AIDBrowseTreeTableNode node;

		Object source;

		TreePath selectedPath;

		AIDTreeNodeExpandWorker(Object source, TreePath selectedPath) {
			this.node = (AIDBrowseTreeTableNode) selectedPath
					.getLastPathComponent();
			this.selectedPath = selectedPath;
			this.source = source;
		}

		@Override
		protected AIDBrowseTreeTableNode[] doInBackground() throws Exception {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			node.expandNode();
			return node.getChildren();
		}

		protected void done() {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			AIDBrowseTreeTableModel model = (AIDBrowseTreeTableModel) getTreeTableModel();
			model.fireStructureChange(source, selectedPath);
			expandPath(selectedPath);
		}

	}

	void initializePopupMenu() {
		// TODO: Think about adapting this popup depending on the state, type of
		// the node.
		// Somehow the current PopupAdapter need to be adjusted later, making
		// use of information about the node.

		addRDFPopupMenu = new JPopupMenu("Modify RDF");
		JMenuItem refreshNode = new JMenuItem("Refresh");
		refreshNode.setActionCommand("Refresh");
		refreshNode.addActionListener(this);

		JMenuItem expandNode = new JMenuItem("Expand");
		expandNode.setActionCommand("Expand");
		expandNode.addActionListener(this);

		JMenuItem addRdfItem = new JMenuItem("Add RDF");
		addRdfItem.setActionCommand("AddRDF");
		addRdfItem.addActionListener(this);

		JMenuItem removeRdfItem = new JMenuItem("Remove RDF");
		removeRdfItem.setActionCommand("RemoveRDF");
		removeRdfItem.addActionListener(this);

		JMenuItem addItemToWorkflow= new JMenuItem("Add Class/Concept to Workflow");
		addItemToWorkflow.setActionCommand("addToWorkflow");
		addItemToWorkflow.addActionListener(this);

		addRDFPopupMenu.add(addItemToWorkflow);
		addRDFPopupMenu.add(refreshNode);
		addRDFPopupMenu.add(expandNode);
		addRDFPopupMenu.add(addRdfItem);
		addRDFPopupMenu.add(removeRdfItem);

		addRDFPopupMenu.setLightWeightPopupEnabled(true);
		addRDFPopupMenu.setEnabled(true);
		add(addRDFPopupMenu);
	}

	JPopupMenu addRDFPopupMenu = null;

	TreePath selectedPath = null;

	/**
	 * Both mouse release and pressed should be overwritten because of platform
	 * specific handling of popup triggers
	 */
	class AIDBrowserPopupAdapter extends MouseAdapter {
		@Override
		public void mouseReleased(MouseEvent e) {
			Point location = new Point(e.getX(), e.getY());
			if (e.isPopupTrigger()) {
				selectedPath = getPathForLocation(location.x, location.y);
				if (selectedPath != null) {
					addRDFPopupMenu.show(e.getComponent(), location.x,
							location.y);
				}
			} else
				super.mouseReleased(e);
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			Point location = new Point(e.getX(), e.getY());
			if (e.isPopupTrigger()) {
				selectedPath = getPathForLocation(location.x, location.y);
				if (selectedPath != null) {
					addRDFPopupMenu.show(e.getComponent(), location.x,
							location.y);
				}
			} else
				super.mousePressed(e);
			
		}
	}

	public void actionPerformed(ActionEvent e) {

		// Selected node guaranteed not null because this action is triggered
		// from popup which only shows if there is a path to selectedNode.
		AIDBrowseTreeTableNode selectedNode = (AIDBrowseTreeTableNode) selectedPath.getLastPathComponent();
		
		String command = e.getActionCommand();

		if (command.equals("Expand") || command.equals("Refresh")) {
			if (command.equals("Refresh") || !selectedNode.expanded()) {
				AIDTreeNodeExpandWorker worker = new AIDTreeNodeExpandWorker(this, selectedPath);
				worker.execute();
			}
		}
		if (command.equals("AddRDF")) {
			AIDAddRDFStatement addRdfDialog = new AIDAddRDFStatement(this, selectedPath);
			addRdfDialog.setVisible(true);
		}
		if (command.equals("RemoveRDF")) {
			AIDRemoveRDFStatement remove = new AIDRemoveRDFStatement(this, selectedPath);
			remove.removeRdf();
		}
		if (command.equals("addToWorkflow")) {
			String newStringConstantName  = selectedNode.getTerm().replaceAll(" ","_");
			String newStringConstantValue = selectedNode.getUrl();
		    AIDEditDataflow.editDataFlowAddStringConstantProcessor(newStringConstantName, newStringConstantValue);
		}
	}

}
