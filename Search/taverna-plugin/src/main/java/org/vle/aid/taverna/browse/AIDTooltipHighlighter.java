package org.vle.aid.taverna.browse;

import java.awt.Component;

import javax.swing.JComponent;

import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;

public  class AIDTooltipHighlighter extends AbstractHighlighter {

	@Override
	protected Component doHighlight(Component rendererComponent, ComponentAdapter adapter) {
		// TODO Auto-generated method stub
		String toolTip =adapter.getFilteredStringAt(adapter.row, 1);
		((JComponent) rendererComponent).setToolTipText(toolTip); 
		
		return rendererComponent;
	}

}
