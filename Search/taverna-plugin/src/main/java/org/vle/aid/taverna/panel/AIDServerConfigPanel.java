package org.vle.aid.taverna.panel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


import org.jdesktop.swingx.JXTaskPane;
import org.vle.aid.taverna.remote.AIDRemoteConfig;
import org.jdesktop.layout.*;

public class AIDServerConfigPanel extends JXTaskPane implements KeyListener, ItemListener {

	String host = AIDRemoteConfig.DEFAULT_SESAME_SERVER,
			username = AIDRemoteConfig.DEFAULT_USERNAME,
			password = AIDRemoteConfig.DEFAULT_PASSWORD;

	JTextField txtHost, txtUsername;
	JPasswordField txtPassword;

	JComboBox  comboHost = new JComboBox(new String[]{"http://aida.homelinux.org/openrdf-sesame"
													 ,"http://ws.adaptivedisclosure.org/openrdf-sesame"
													 ,"http://amc-app2.amc.sara.nl/openrdf-sesame"
													 ,"http://www.csw.inf.fu-berlin.de:4039/sesame"
													 ,"http://bmir-marshall.stanford.edu/openrdf-sesame"
	});
	
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);

	public AIDServerConfigPanel() {
		setTitle("AIDA Thesaurus Server");
		
		comboHost.setEditable(true);
		//comboHost.setPrototypeDisplayValue("http://ws.adaptivedisclosure.org/");
		txtHost = new JTextField(AIDRemoteConfig.DEFAULT_SESAME_SERVER);
		txtHost.setEditable(true);

		txtUsername = new JTextField(AIDRemoteConfig.DEFAULT_USERNAME,40);
		txtUsername.setEditable(true);

		txtPassword = new JPasswordField(AIDRemoteConfig.DEFAULT_PASSWORD,40);
		txtPassword.setEditable(true);

		JLabel lblServer = new JLabel("Server:");
		JLabel lblUser = new JLabel("Username:");
		JLabel lblPassword = new JLabel("Password:");
		
		
		
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		layout.setAutocreateGaps(true);
		layout.setAutocreateContainerGaps(true);

		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		
	
		hGroup.add(layout.createParallelGroup().add(lblServer).add(lblUser    ).add(lblPassword));
		hGroup.add(layout.createParallelGroup().add(comboHost).add(txtUsername).add(txtPassword));
		
		
		layout.setHorizontalGroup(hGroup);
		
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		
		vGroup.add(layout.createParallelGroup(GroupLayout.BASELINE).add(lblServer).add(comboHost));
		vGroup.add(layout.createParallelGroup(GroupLayout.BASELINE).add(lblUser).add(txtUsername));
		vGroup.add(layout.createParallelGroup(GroupLayout.BASELINE).add(lblPassword).add(txtPassword));
		
		layout.setVerticalGroup(vGroup);
		 
		
		txtHost.addKeyListener(this);
		txtPassword.addKeyListener(this);
		txtUsername.addKeyListener(this);
		comboHost.addItemListener(this);

		setAnimated(false);
		setExpanded(true);
	}

	public void keyPressed(KeyEvent e) {
		// Check if it is enter, validate if server exists, and update the
		// locations If they press enter
		if (e.getKeyChar() == 10) {
			if (validHostUserPassword()) {
				setUsername(txtUsername.getText());
				setPassword(new String(txtPassword.getPassword()));
				setHost(txtHost.getText());
			}
		}
	}

	public String getHost() {
		return host;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (propertyChangeSupport != null)
			propertyChangeSupport.addPropertyChangeListener(listener);
		super.addPropertyChangeListener(listener);
	}

	public void setHost(String newHost) {
		AIDRemoteConfig.setSesameServer(newHost);
		propertyChangeSupport.firePropertyChange("REFRESH_HOST", host, newHost);
		this.host = newHost;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		AIDRemoteConfig.setPassword(password);
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		AIDRemoteConfig.setUsername(username);
		this.username = username;
	}

	private boolean validHostUserPassword() {
		// TODO need to devise a way to validate these

		return true;
	}

	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		if(e.getStateChange() == ItemEvent.SELECTED){
			setUsername(txtUsername.getText());
			setPassword(new String(txtPassword.getPassword()));
			setHost(e.getItem().toString());
		}
	}

}
