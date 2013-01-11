 package org.vle.aid.taverna;

import javax.swing.ImageIcon;

import org.vle.aid.taverna.components.Gui;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;



public class AIDComponentFactory implements UIComponentFactorySPI {
	AIDComponent aidComponent = null;
	
	public UIComponentSPI getComponent() {
		// Keep the AID component if its already there, instead of creating new one everytime this perspective is opened
		if(aidComponent == null)
			aidComponent = new AIDComponent();

		return aidComponent;
	}

	public ImageIcon getIcon() {
		 return Gui.getIcon("icons/vle.png");
	}

	public String getName() {
		return "AID Plugin";
	}
	public static void main(String args[]) throws InstantiationException, IllegalAccessException {
        UIComponentFactorySPI factory = (UIComponentFactorySPI) AIDComponentFactory.class.newInstance();
        //System.out.println(" See I have no problem here ");
	}
}
