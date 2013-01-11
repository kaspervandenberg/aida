package org.vle.aid.taverna.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.axis.client.Call;
import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXTaskPane;
import org.vle.aid.taverna.browse.AIDBrowseTreeTable;
import org.vle.aid.taverna.browse.AIDBrowseTreeTableModel;
import org.vle.aid.taverna.browse.AIDBrowseTreeTableNode;
import org.vle.aid.taverna.components.Gui;
import org.vle.aid.taverna.remote.AIDRemoteConfig;
import org.vle.aid.taverna.remote.AIDRemoteQuery;
import org.jdesktop.layout.*;

public class AIDSkosLensConfigPanel extends JXTaskPane implements ItemListener, PropertyChangeListener {

    String topConcept="rdfs:Class", 
    	      narrowerPredicate="rdfs:SubClassOf";
    
    JComboBox comboTopConcept, comboNarrower;
    JTextArea attemptedLens = new JTextArea("Attempted Skos Lens:");
    
    private  final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    
    public AIDSkosLensConfigPanel() {
			setTitle("AIDA Skos Lens Configuration");
			comboTopConcept = new JComboBox(new String []	 {"rdfs:Class",  "owl:Class",      "owl:AnnotationProperty",   "owl:ObjectProperty",   "rdf:Property",  "JADE:Class"
					,"<http://purl.org/net/tcm/tcm.lifescience.ntu.edu.tw/Medicine>"
					,"<http://purl.uniprot.org/core/Concept>"
					,"<http://purl.uniprot.org/core/Protein>"
					,"<http://rdf.myexperiment.org/ontologies/base/Announcement>"
			});
			comboNarrower= new JComboBox(new String []{"rdfs:subClassOf",  "skos:Narrower",  "skos:Broader",  "owl:subPropertyOf", "rdfs:subPropertyOf", "JADE:SubClassOf" 
					,"<http://purl.org/net/tcm/tcm.lifescience.ntu.edu.tw/association>"		
			});
			
			JLabel lblTopConcept =  new JLabel("Top Concept: ");
			JLabel lblNarrower   =  new JLabel("Narrower Predicate: ");
			
			JPanel upperPanel = new JPanel();
			
			GroupLayout layout = new GroupLayout(upperPanel);
			upperPanel.setLayout(layout);
			
			layout.setAutocreateGaps(true);			
			layout.setAutocreateContainerGaps(true);

			GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
			
			hGroup.add(layout.createParallelGroup().add(lblTopConcept).add(lblNarrower));
			hGroup.add(layout.createParallelGroup().add(comboTopConcept).add(comboNarrower));
						
						
			layout.setHorizontalGroup(hGroup);
			
			GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
			  
			vGroup.add(layout.createParallelGroup(GroupLayout.BASELINE).add(lblTopConcept).add(comboTopConcept));
			vGroup.add(layout.createParallelGroup(GroupLayout.BASELINE).add(lblNarrower).add(comboNarrower));
			
			layout.setVerticalGroup(vGroup);				
			
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(upperPanel, BorderLayout.NORTH);
			getContentPane().add(new JScrollPane(attemptedLens), BorderLayout.CENTER);
			
			comboTopConcept.addItemListener(this);
			comboNarrower.addItemListener(this);
			
			setAnimated(false);
			setExpanded(true);
			setupAttemptedLens("");
   }
   
   void appendAttempted(String text){
	   attemptedLens.setText(attemptedLens.getText().trim()+"\n"+text+"\n\n\n\n");
   }
   
   private void setupAttemptedLens(String repositoryName) {
	   new CheckAttemptedLensWorker(repositoryName).execute();

	}


@Override
   public void addPropertyChangeListener(PropertyChangeListener listener) {
       if(propertyChangeSupport != null) propertyChangeSupport.addPropertyChangeListener(listener);
       super.addPropertyChangeListener(listener);
   }
   
   public String getTopConcept(){ return topConcept;}
   public String getNarrowerPredicate() { return narrowerPredicate;}
   
   public void setTopConcept(String newTopConcept) {
	   AIDRemoteConfig.setTopConcept(newTopConcept);
	   propertyChangeSupport.firePropertyChange("REFRESH_TOPCONCEPT", topConcept, newTopConcept);
	   this.topConcept = newTopConcept;	   
    }

    public void setNarrowerPredicate(String newNarrowerPredicate) {
		AIDRemoteConfig.setNarrowerPredicate(newNarrowerPredicate);
		propertyChangeSupport.firePropertyChange("REFRESH_NARROWER", narrowerPredicate, newNarrowerPredicate);		
		this.narrowerPredicate = newNarrowerPredicate;		
    }

    public void itemStateChanged(ItemEvent itemEvent) {
    	if(itemEvent.getStateChange() == itemEvent.SELECTED){
    		
    		setNarrowerPredicate(comboNarrower.getSelectedItem().toString());
    		setTopConcept(comboTopConcept.getSelectedItem().toString());    		
    		appendAttempted("Top Concept :"+topConcept+" Narrower: "+narrowerPredicate);
    	}
    }

	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("REFRESH_ATTEMPTEDLENS")){
			attemptedLens.setText("");
			setupAttemptedLens((String)evt.getNewValue());
		}
	}

	
	class CheckAttemptedLensWorker extends SwingWorker<String [], Integer> {
		String repositoryName;

		public CheckAttemptedLensWorker(String repositoryName) {
			this.repositoryName = repositoryName;
		}

		@Override
		protected String [] doInBackground() throws Exception {

			
			 AIDRemoteQuery repository = AIDRemoteQuery.getDefaultRepositoryDetect();
			 if(!repositoryName.equals(""))
				 repository.setRepository(repositoryName);
			 
			 String [] results = repository.detectRepository();
			

			return results;
		}

		protected void done() {
			try {
				String [] detectedRepository = get();

				if (detectedRepository != null) {
					if(detectedRepository[1].contains("2004")) detectedRepository[1] = "Skos 2004";
					if(detectedRepository[1].contains("2008")) detectedRepository[1] = "Skos 2008";
					appendAttempted("Repository Detected as :\n"+detectedRepository[0]+" " + detectedRepository[1]);
				}

			} catch (Exception ex) {
				Gui.showErrorWarnings("Failed to create tab for repository :"
						+ repositoryName, ex);
			}
		}
	}
    	

}
