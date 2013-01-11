package org.vle.aid.taverna.build;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.TableHeaderUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.vle.aid.taverna.AIDEditDataflow;
import org.vle.aid.taverna.panel.AIDSearchPanel;


@SuppressWarnings("serial")
public class AIDBuildTreeTable extends JXTreeTable implements ActionListener{
	
	public class MultilineHeaderRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			
			
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		}

	}



	/* Selected path updated by mouse listener which triggers popup (AIDBuildTreePopupAdapter) */
    TreePath selectedPath ; 
	JPopupMenu buildTreePopupMenu = new JPopupMenu(); 
    
    public AIDBuildTreeTable() {
		super(new AIDBuildTreeTableModel());

		setEditable(true);
		setEnabled(true);

		setTransferHandler(new AIDBuildTreeTableTransferHandle());

		initializePopupMenu();
		addMouseListener(new AIDBuildTreePopupAdapter(this));

		setHighlighters(new CompoundHighlighter());
		setHighlighters(HighlighterFactory.createSimpleStriping(HighlighterFactory.GENERIC_GRAY));
		setColumnControlVisible(true);
		
	}
    
    void initializePopupMenu(){
    	buildTreePopupMenu = new JPopupMenu("Modify RDF");
		JMenuItem mustOccur = new JMenuItem("Must Occur");
		mustOccur.setActionCommand("mustOccur");
		mustOccur.addActionListener(this);

		JMenuItem mustNotOccur = new JMenuItem("Must Not Occur");
		mustNotOccur.setActionCommand("mustNotOccur");
		mustNotOccur.addActionListener(this);
		
		JMenuItem mayOccur = new JMenuItem("May Occur");
		mayOccur.setActionCommand("mayOccur");
		mayOccur.addActionListener(this);
		
		JMenuItem removeItem = new JMenuItem("Remove Node");
		removeItem.setActionCommand("removeItem");
		removeItem.addActionListener(this);

		JMenuItem addItemToWorkflow= new JMenuItem("Add Class/Concept to Workflow");
		addItemToWorkflow.setActionCommand("addToWorkflow");
		addItemToWorkflow.addActionListener(this);

		buildTreePopupMenu.add(addItemToWorkflow);
		buildTreePopupMenu.add(removeItem);
		buildTreePopupMenu.add(mustOccur);
		buildTreePopupMenu.add(mustNotOccur);
		buildTreePopupMenu.add(mayOccur);
		
		buildTreePopupMenu.setLightWeightPopupEnabled(true);
		buildTreePopupMenu.setEnabled(true);
		add(buildTreePopupMenu);
		
		JTableHeader headers = getTableHeader();
		//headers.setDefaultRenderer(new MultilineHeaderRenderer());
		
    }
    
    /**
     * Whenever these actions happened, treeModel listener of this query builder must be notified
     * In current case it will be {@link AIDSearchPanel} which implements {@link TreeModelListener}
     * Notifying is done by {@link AIDBuildTreeTableModel}.fireStructureChange
     * 
     * In {@link AIDSearchPanel} now the part responsible for this expect a treeExpansionEvent from an {@link AIDBuildTreeTable}
     * 
     */
    
	public void actionPerformed(ActionEvent ae) {
		
		TreePath parentPath = selectedPath.getParentPath();

		AIDBuildTreeTableModel model 		= (AIDBuildTreeTableModel) getTreeTableModel();
		AIDBuildTreeTableNode selectedNode 	= (AIDBuildTreeTableNode) selectedPath.getLastPathComponent();
		AIDBuildTreeTableNode parentNode   	= (AIDBuildTreeTableNode) parentPath.getLastPathComponent();

		String command = ae.getActionCommand();
		if (command.equals("removeItem")) {
			parentNode.removeChild(selectedNode);
			model.fireStructureChange(new TreeExpansionEvent(this, parentPath));
		}
		if (command.equals("mustOccur")) {
			selectedNode.setMustOccur(true);
			model.fireStructureChange(new TreeExpansionEvent(this, selectedPath));
		}
		if (command.equals("mustNotOccur")) {
			selectedNode.setMustNotOccur(true);
			model.fireStructureChange(new TreeExpansionEvent(this, selectedPath));
		}
		if (command.equals("mayOccur")) {
			selectedNode.setMustNotOccur(false);
			selectedNode.setMustOccur(false);
			model.fireStructureChange(new TreeExpansionEvent(this, selectedPath));
		}
		if (command.equals("addToWorkflow")) {
			String newStringConstantName  = selectedNode.getTerm().replaceAll(" ","_");
			String newStringConstantValue = selectedNode.getUrl();
		    AIDEditDataflow.editDataFlowAddStringConstantProcessor(newStringConstantName, newStringConstantValue);
		}
	}
	


	class AIDBuildTreePopupAdapter extends MouseAdapter {
		AIDBuildTreeTable table;
		public AIDBuildTreePopupAdapter(AIDBuildTreeTable table) {
			// TODO Auto-generated constructor stub
			this.table = table;
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				Point location = new Point(e.getX(), e.getY());
				selectedPath = getPathForLocation(location.x, location.y);
				if (selectedPath != null) {
					buildTreePopupMenu.show(e.getComponent(), location.x, location.y);
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
					buildTreePopupMenu.show(e.getComponent(), location.x, location.y);
				}
			} else{
				/** 
				 * Maybe this is not the way to do it but for some reason, in taverna the normal click in renderer of check box does not work 
				 * So i detected the click with mouse pressed, determine the location and update/toggle the checkbox component
				 * */
			    int col = getSelectedColumn();
				if (col >= 1) {
					selectedPath = getPathForLocation(location.x, location.y);
					AIDBuildTreeTableNode node = (AIDBuildTreeTableNode) selectedPath
							.getLastPathComponent();
					if (col == 1)
						node.setMustOccur(!node.getMustOccur());
					else if (col == 2)
						node.setMustNotOccur(!node.getMustNotOccur());
					AIDBuildTreeTableModel model = (AIDBuildTreeTableModel) getTreeTableModel();
					model.fireStructureChange(new TreeExpansionEvent(table, selectedPath));
				}
			}
			
		}
	}

}


