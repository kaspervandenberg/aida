package org.vle.aid.taverna.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXEditorPane;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.vle.aid.taverna.AIDEditDataflow;
import org.vle.aid.taverna.components.Gui;
import org.vle.aid.taverna.remote.AIDDirectQueryWorker;
import org.vle.aid.taverna.remote.AIDRemoteQuery;
import org.vle.aid.taverna.remote.AIDRemoteSearch;
import org.vle.aid.taverna.remote.AIDRepositoryComboWorker;

/**
 * aida-plugin
 * @author wibisono
 * @date Apr 23, 2009 10:25:15 PM
 */
@SuppressWarnings("serial")
public class AIDQueryPanel extends JXTaskPaneContainer implements	ActionListener{

	
	/* Upper part for query and settings */
	private JXTaskPane 	queryTaskPane  = new JXTaskPane();
	/* Lower part for the query result table */
	private JXTaskPane  resultTaskPane = new JXTaskPane();
	
	/* Query editor at the top */
	private JXEditorPane queryEditorPane = new JXEditorPane();

	/* Settings for language */
	private JXPanel controlPanel = new JXPanel();
	
	/* Combobox to select which repository to perform query to */
	private JComboBox repositoryCombo = new JComboBox();
	
	/* Execute query button */
	private JXButton execButton = new JXButton("Go");
	
	private JComboBox languageCombo = new JComboBox(new String[] { "sparql", "serql" });

	
	/* Table for the result of the query */
	private JXTable resultTable;
	
	private JPopupMenu menu = new JPopupMenu();

	public AIDQueryPanel() {
		
	    	initializeQueryPanel();
	}
	
	private void initializeQueryPanel() {
	    
		queryTaskPane.setTitle("AIDA Query");
		queryTaskPane.setLayout(new BorderLayout());

		queryTaskPane.add(controlPanel, BorderLayout.EAST);
		queryTaskPane.add(queryEditorPane, BorderLayout.CENTER);
		
		queryEditorPane.setText("SELECT * WHERE {?x ?p ?y} LIMIT 10");

		resultTaskPane.setTitle("Query Result");
		execButton.addActionListener(this);
		
		controlPanel.add(execButton);
		controlPanel.add(repositoryCombo);
		controlPanel.add(languageCombo);
		
		add(queryTaskPane);
		add(resultTaskPane);
	
		repositoryCombo.setToolTipText("Select which repository to perform this query");
		languageCombo.setToolTipText("Select language to perform query ");
		
		AIDRepositoryComboWorker  repositoryComboWorker = new AIDRepositoryComboWorker(repositoryCombo);
		repositoryComboWorker.execute();
		
		resultTable = createQueryResultTable();
		resultTaskPane.add(new JLabel("Double click on result table cells to add currently selected table cells into workflow"));
		resultTaskPane.add(new JScrollPane(resultTable));
		
		/* Add popup menu */
		JMenuItem addItemToWorkflow= new JMenuItem("Add Class/Concept to Workflow");
		addItemToWorkflow.setActionCommand("addToWorkflow");
		addItemToWorkflow.addActionListener(this);
		menu.add(addItemToWorkflow);
		
		resultTable.add(menu);
	    
	}



	/* Standard table with no fuss, just a bit of decoration */
	JXTable createQueryResultTable(){
			JXTable resultTable = new JXTable();
			resultTable.setColumnControlVisible(true);
			resultTable.setHighlighters(new CompoundHighlighter());
			resultTable.setHighlighters(HighlighterFactory.createSimpleStriping(HighlighterFactory.GENERIC_GRAY));
			resultTable.setEditable(false);
			resultTable.addMouseListener(new AIDQueryMouseAdapter(resultTable));
			resultTable.setToolTipText("Double click to add to workflow");
			
			return resultTable;
	}
	
	
	
	public void actionPerformed(ActionEvent e) {
		 if(e.getActionCommand() == "Go"){
			 AIDDirectQueryWorker worker = new AIDDirectQueryWorker(resultTable, 
				 									queryEditorPane.getText(), 
				 									(languageCombo.getSelectedItem()).toString(), 
				 									repositoryCombo.getSelectedItem().toString());
			 worker.execute();
		 } else 
		 if(e.getActionCommand() == "addToWorkflow"){
				int row = resultTable.getSelectedRow();
				int col = resultTable.getSelectedColumn();
				if(row >= 0 && col >= 0)
					addTableCellAsStringConstant(row,col);
				else
					JOptionPane.showMessageDialog(this, "Please click and select one of the cells first", "Boobies", JOptionPane.WARNING_MESSAGE);
		 }
	}
	
	/**
	 * Double click adapter which will allow user to add new string constant into the workflow/dataflow of taverna.
	 * aida-plugin
	 * @author wibisono
	 * @date May 3, 2009 11:42:59 PM
	 */
	public class AIDQueryMouseAdapter extends MouseAdapter {
		JXTable table;
		public AIDQueryMouseAdapter(JXTable table) {
			this.table = table;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			  if(e.getClickCount() == 2){
				int row = table.getSelectedRow();
				int col = table.getSelectedColumn();
				addTableCellAsStringConstant(row,col);
			  }
			  super.mouseClicked(e);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			Point location = new Point(e.getX(), e.getY());
			if (e.isPopupTrigger()) {
					menu.show(e.getComponent(), location.x,	location.y);
			} else
				super.mouseReleased(e);
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			Point location = new Point(e.getX(), e.getY());
			if (e.isPopupTrigger()) {
					menu.show(e.getComponent(), location.x,	location.y);
			} else
				super.mouseReleased(e);
		}
	}
	
	public static void main(String[] args) {
		AIDQueryPanel queryForm = new AIDQueryPanel();
		Gui.showInFrame(new JScrollPane(queryForm));
	}

	public void addTableCellAsStringConstant(int row, int col) {

		String stringConstantValue = (String) resultTable.getModel().getValueAt(row,col);
		String defaultStringConstantName=stringConstantValue.substring(stringConstantValue.lastIndexOf("/")+1).replaceAll("#", "_").replaceAll("-", "_");
		
		String stringConstantName = JOptionPane.showInputDialog(resultTable, "Please provide processor name : ",defaultStringConstantName);
		if(stringConstantName != null)
			AIDEditDataflow.editDataFlowAddStringConstantProcessor(stringConstantName, stringConstantValue);		
	}


}
