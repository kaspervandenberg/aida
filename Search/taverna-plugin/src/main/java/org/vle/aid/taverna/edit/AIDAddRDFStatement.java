package org.vle.aid.taverna.edit;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXTitledPanel;
import org.vle.aid.taverna.browse.AIDBrowseTreeTable;
import org.vle.aid.taverna.browse.AIDBrowseTreeTableModel;
import org.vle.aid.taverna.browse.AIDBrowseTreeTableNode;
import org.vle.aid.taverna.remote.AIDRemoteQuery;

/**
 * First shot and guess in the dark attempt on what marco wants, basically reproducing the steps I did manually to add concept pizza on the food branch.
 * Using the interface on : http://wongiseng.homelinux.org:8080/OOO/
 * http://wongiseng.homelinux.org:8080
 *
 * 
 * 
 * Eventually in the end, adding pizza, to bakery, will resulted in the following 
 *       <rdf:Description rdf:about="http://www.afsg.nl/www_foodontology_nl/data/documenten/Ontology/TNO_FI-Ontology_2007_jan.owl#pizza">
 *       	<rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
 *      	<skos:broader rdf:resource="http://www.afsg.nl/www_foodontology_nl/data/documenten/Ontology/TNO_FI-Ontology_2007_jan.owl#bakery"/>
 *       	<skos:inScheme rdf:resource="http://www.afsg.nl/www_foodontology_nl/data/documenten/Ontology/TNO_FI-Ontology_2007_jan.owl#scheme"/>
 *       	<skos:prefLabel xml:lang="en">pizza</skos:prefLabel>
 *       </rdf:Description> 
 * @author wibisono
 *
 */
/**
 * @author wibisono
 * 
 */
@SuppressWarnings("serial")
public class AIDAddRDFStatement extends JDialog implements ActionListener {
	AIDBrowseTreeTableNode node = null;

	AIDBrowseTreeTable treeTable;

	TreePath selectedPath;

	public AIDAddRDFStatement(AIDBrowseTreeTable treeTable, TreePath selectedPath) {
		this.selectedPath = selectedPath;
		this.node = (AIDBrowseTreeTableNode) selectedPath
				.getLastPathComponent();
		this.treeTable = treeTable;
		
		//setIconImage(Gui.getIcon("icons/vle.png").getImage());

		// God forbid maybe this should be grid bag layout also, i wanted to anchored this thing to the left
		// So if its expanded it stays, anchored to the left. Now its centered.
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		
		JXTitledPanel currentPanel = createCurrentPanel(node.getTerm(), node.getUrl(), "Broader Term", false);
		JXTitledPanel childPanel = createChildPanel(node.getTerm(), node.getUrl(), "New Term", true);

		getContentPane().add(currentPanel);
		getContentPane().add(childPanel);
		getContentPane().add(getButtons());

		setSize(node.getUrl().length() * 8, 300);

	}

	JButton addButton = new JButton("Add");

	JButton cancelButton = new JButton("Cancel");

	JTextField childTerm = new JTextField();

	JTextField childUrl = new JTextField();

	JPanel getButtons() {
		JPanel result = new JPanel();
		result.add(cancelButton);
		result.add(addButton);
		cancelButton.addActionListener(this);
		addButton.addActionListener(this);
		return result;
	}

	JXTitledPanel createCurrentPanel(String term, String url, String title,
			boolean editable) {
		JXTitledPanel result = new JXTitledPanel();
		result.setTitle(title);

		JPanel panel = new JPanel();

		GridBagConstraints c = new GridBagConstraints();

		panel.setLayout(new GridBagLayout());

		// Make it a bit bigger
		c.ipady = 15;
		// Add some inset to make it not too crowded
		c.insets = new Insets(5, 10, 5, 10);
		c.anchor = GridBagConstraints.WEST;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		panel.add(new JLabel("Term"), c);

		c.gridx = 1;
		c.gridy = 0;
		childTerm = new JTextField(term);
		childTerm.setEditable(editable);
		panel.add(childTerm, c);

		c.gridx = 0;
		c.gridy = 1;
		panel.add(new JLabel("Url"), c);

		c.gridx = 1;
		c.gridy = 1;
		JTextField txturl = new JTextField(url);
		txturl.setEditable(editable);
		panel.add(txturl, c);

		result.add(panel);
		return result;
	}

	JXTitledPanel createChildPanel(String term, String url, String title,
			boolean editable) {
		JXTitledPanel result = new JXTitledPanel();
		result.setTitle(title);

		JPanel panel = new JPanel();

		GridBagConstraints c = new GridBagConstraints();

		panel.setLayout(new GridBagLayout());

		c.insets = new Insets(5, 10, 5, 10);
		c.ipady = 15;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		panel.add(new JLabel("Term"), c);

		c.gridx = 1;
		c.gridy = 0;
		childTerm = new JTextField(term);
		childTerm.setEditable(editable);
		panel.add(childTerm, c);

		c.gridx = 0;
		c.gridy = 1;
		panel.add(new JLabel("Url"), c);

		c.gridx = 1;
		c.gridy = 1;
		childUrl = new JTextField(url);
		childUrl.setEditable(editable);
		panel.add(childUrl, c);

		result.add(panel);
		return result;
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getActionCommand().equals("Cancel")) {
			dispose();
		} else {
			AIDAddRDFStatementWorker worker = new AIDAddRDFStatementWorker(
					treeTable, selectedPath);
			worker.execute();
			dispose();
		}
	}

	private boolean addNewRDFTerm() {
		// TODO Auto-generated method stub
		// System.out.println("Finally I am going to add " + childTerm.getText());
		AIDRemoteQuery remote = AIDRemoteQuery.getDefaultRepository();

		/** The Subject to be added * */
		String theSubject = childUrl.getText();

		/** Predicates * */
		String type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		String broader = "http://www.w3.org/2004/02/skos/core#broader";
		String inScheme = "http://www.w3.org/2004/02/skos/core#inScheme";
		String prefLabel = "http://www.w3.org/2004/02/skos/core#prefLabel";

		/** Objects * */
		String concept_type = "http://www.w3.org/2004/02/skos/core#Concept";
		String parent_broader = node.getUrl();
		String scheme = "http://www.afsg.nl/www_foodontology_nl/data/documenten/Ontology/TNO_FI-Ontology_2007_jan.owl#scheme";
		String label = childTerm.getText();

		try {
			remote.addRdfStatement(theSubject, type, concept_type);
			remote.addRdfStatement(theSubject, broader, parent_broader);
			remote.addRdfStatement(theSubject, inScheme, scheme);
			remote.addRdfStatement(theSubject, prefLabel, label);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	class AIDAddRDFStatementWorker extends SwingWorker<Boolean, Integer> {
		TreePath selectedPath;
		
		AIDBrowseTreeTable treeTable;

		AIDBrowseTreeTableNode node;
		
		AIDAddRDFStatementWorker(AIDBrowseTreeTable treeTable,
				TreePath selectedPath) {
			this.selectedPath = selectedPath;
			this.treeTable = treeTable;
			this.node = (AIDBrowseTreeTableNode) selectedPath.getLastPathComponent();
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			treeTable.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			boolean result =  addNewRDFTerm();
			node.expandNode();
			return result;
		}

		protected void done() {
			treeTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			AIDBrowseTreeTableModel model = (AIDBrowseTreeTableModel) treeTable
					.getTreeTableModel();
			model.fireStructureChange(this,selectedPath);
			treeTable.expandPath(selectedPath);
		}

	}
}
