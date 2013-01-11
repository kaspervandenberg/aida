package org.vle.aid.taverna.browse;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class AIDBrowseTreeTableCellRenderer extends DefaultTableCellRenderer {
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		AIDBrowseTreeTableNode node = (AIDBrowseTreeTableNode) table.getModel().getValueAt(row,col);
		setToolTipText(node.getUrl());
		//System.out.println("Badaboum ?");
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
	}
}
