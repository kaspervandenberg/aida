package org.vle.aid.taverna;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

import org.vle.aid.taverna.components.Gui;
import org.vle.aid.taverna.panel.AIDMainPanel;

/**
 * 
 * aida-plugin
 * @author wibisono
 * @date Apr 23, 2009 10:51:05 PM
 */
public class AIDComponent extends JPanel implements UIComponentSPI {

	private static final long serialVersionUID = 1L;

	private AIDMainPanel aidMainPanel = null;
	public AIDComponent() {
		  setLayout(new BorderLayout());
		  aidMainPanel = new AIDMainPanel();
		  add(aidMainPanel, BorderLayout.CENTER);
	}
	
	public void detachFromModel() {

	}

	public ImageIcon getIcon() {
		  return Gui.getIcon("icons/vle.png");
	}

	public void onDisplay() {

	}

	public void onDispose() {
	    	
	}

	


}
