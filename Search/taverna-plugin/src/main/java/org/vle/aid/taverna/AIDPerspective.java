package org.vle.aid.taverna;

import java.io.InputStream;

import javax.swing.ImageIcon;


import org.jdom.Element;
import org.vle.aid.taverna.components.Gui;

import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;


/**
 * aida-plugin-2.0
 * @author wibisono
 * @date May 1, 2009 3:13:36 AM
 */
public class AIDPerspective implements PerspectiveSPI {
	boolean visible = true;
	
	public ImageIcon getButtonIcon() {
		return Gui.getIcon("icons/vle.png");
	}
	
	public String getText() {
		return "AID Plugin";
	}

	public InputStream getLayoutInputStream() {
		// This is a layout file created using taverna perspective design, and adding aida plugin within the layout
		// The version of Workbench that will be using the plugin, must be the same with the version of taverna workbench  used to create this layout
		// Currently this is created by using taverna workbench 2.1.0, updated version.
		return  getClass().getResourceAsStream("aida-perspective.xml");

	}

	public boolean isVisible() {
		return visible;
	}

	public int positionHint() {
		// TODO Auto-generated method stub
		return 10;
	}

	public void setVisible(boolean visible) {
		// TODO Auto-generated method stub
		this.visible = visible;
	}

	public void update(Element arg0) {
		// TODO Auto-generated method stub
		
	}

}
