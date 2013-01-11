package org.vle.aid.taverna.components;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.jdesktop.swingx.JXTitledPanel;


public class JXTabbedPane extends JTabbedPane {
   
    private Action closingAction = null;

    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index) {
	super.insertTab(title, icon, component, tip, index);
//	setTabComponentAt(index, new JXTabButton(this));
    }

    public void setClosingAction(Action closingAction) {
	this.closingAction = closingAction;
    }

    public Action getClosingAction() {
	return closingAction;
    }

    public void doCloseAction(ActionEvent e) {
	// TODO Auto-generated method stub
	closingAction.actionPerformed(e);
    }
    public static void main(String[] args) {
	final JFrame frame = new JFrame("Test");
	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	final JXTabbedPane test = new JXTabbedPane();
	test.add(new JXTitledPanel("Bozo"), "Bozo");
	test.add(new JXTitledPanel("Bozi"), "Bozi");
	test.setClosingAction(new AbstractAction(){
	    public void actionPerformed(ActionEvent e) {
			JOptionPane.showConfirmDialog(frame, "You're going to close "+test.getSelectedIndex());
	    
	    }});
	    
	frame.add(test);
	frame.setSize(500, 500);
	frame.setVisible(true);
    }

}