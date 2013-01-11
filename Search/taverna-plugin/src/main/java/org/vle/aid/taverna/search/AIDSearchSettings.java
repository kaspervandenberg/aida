package org.vle.aid.taverna.search;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.vle.aid.taverna.remote.AIDRemoteSearch;
import org.vle.aid.taverna.remote.AIDSearchSettingComboWorker;




/**
 * 
 * Settings, has basically 3 properties, index, field and query. Changes in these properties will fire property change event.
 * 
 * 
 * aida-plugin
 * @author wibisono
 * @date Apr 29, 2009 12:24:51 PM
 */
@SuppressWarnings("serial")
public class AIDSearchSettings extends JPanel  {
		private JComboBox indexCombo, fieldCombo;
		private JTextField queryTextField = new JTextField("Type in query ...");
	
		private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
		private String currentIndex="";
		private String currentField="";
		private String currentQuery="";
		HashMap<String, String []> indexToField = new HashMap<String, String[]>();
		
 		public AIDSearchSettings() {
			JPanel leftPane = new JPanel();
			leftPane.setLayout(new FlowLayout(FlowLayout.LEFT));
			leftPane.add(new JLabel("Search:"));
			indexCombo = new JComboBox();
			fieldCombo = new JComboBox();
			leftPane.add(indexCombo);
			leftPane.add(fieldCombo);
			setLayout(new BorderLayout());
			add(leftPane, BorderLayout.WEST);
			
			add(queryTextField, BorderLayout.CENTER);
			queryTextField.setSize(new Dimension(600,30));
			queryTextField.setBorder(new EmptyBorder(0,0,0,0));
			
			queryTextField.addFocusListener(new QueryFocusListener());
			queryTextField.addKeyListener(new QueryKeyListener());
		
			fieldCombo.addItemListener(new FieldItemListener());
			indexCombo.addItemListener(new IndexItemListener());
			

			AIDSearchSettingComboWorker worker = new AIDSearchSettingComboWorker(indexCombo,fieldCombo,indexToField);
			worker.execute();
			
		}
 		
 		/** 
 		 * Getting fields available for certain index. If once already retrieved from remote, it will be stored in indexToField HashMap.
 		 * @param index
 		 * @return
 		 */
		private String [] getFieldsFromIndex(String index){
			if(indexToField.containsKey(index)) return indexToField.get(index);
			String [] fields = AIDRemoteSearch.listFields(index);
			indexToField.put(index,fields);
			return fields;
		}
		
		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(listener);
		}


		public String getCurrentField() {
			return currentField;
		}

		public void setCurrentField(String currentField) {
			propertyChangeSupport.firePropertyChange("FIELD_CHANGED", this.currentField, currentField);
			this.currentField = currentField;
		}

		public String getCurrentIndex() {
			return currentIndex;
		}

		public void setCurrentIndex(String currentIndex) {
			propertyChangeSupport.firePropertyChange("INDEX_CHANGED", this.currentIndex, currentIndex);
			fieldCombo.setModel(new DefaultComboBoxModel(getFieldsFromIndex(currentIndex)));
			fieldCombo.setSelectedIndex(0);
			setCurrentField(fieldCombo.getItemAt(0).toString());
			this.currentIndex = currentIndex;
		}

		public String getCurrentQuery() {
			return currentQuery;
		}

		public void setCurrentQuery(String currentQuery) {
			propertyChangeSupport.firePropertyChange("QUERY_CHANGED", this.currentQuery, currentQuery);
			this.currentQuery = currentQuery;
		}
		
		class IndexItemListener implements ItemListener {

			public void itemStateChanged(ItemEvent evt) {
				if(evt.getStateChange() == ItemEvent.SELECTED){
					setCurrentIndex(evt.getItem().toString());
				}			
			}
			
		}
		
		class FieldItemListener implements ItemListener {

			public void itemStateChanged(ItemEvent evt) {
				if(evt.getStateChange() == ItemEvent.SELECTED){
						setCurrentField(evt.getItem().toString());
				}
			}
			
		}
		
		public class QueryKeyListener implements KeyListener {

			public void keyPressed(KeyEvent key) {
				// If user press enter 
				if(key.getKeyCode() == 10) {
					setCurrentQuery(queryTextField.getText());
				}
				
			}

			public void keyReleased(KeyEvent key) {
				// TODO Auto-generated method stub

			}

			public void keyTyped(KeyEvent key) {
				// TODO Auto-generated method stub

			}

		}
		public class QueryFocusListener implements FocusListener {

			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub
				if(queryTextField.getText().equals("Type in query ...")) queryTextField.setText("");
				queryTextField.selectAll();
				
			}

			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub

			}

		}
}
