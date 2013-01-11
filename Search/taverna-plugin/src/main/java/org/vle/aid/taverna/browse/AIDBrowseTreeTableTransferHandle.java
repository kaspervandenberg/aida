package org.vle.aid.taverna.browse;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.vle.aid.taverna.build.AIDBuildTreeTableTransferHandle;



@SuppressWarnings("serial")
public class AIDBrowseTreeTableTransferHandle extends TransferHandler {
	/**
	 * Prepare the transferable drag data, which is basically the term, url and 
	 * the repository name. 
	 * 
	 * Those tokenas are packed as a string separated with new lines
	 * {@link AIDBuildTreeTableTransferHandle} will be responsible for unpacking those
	 * when it does the import data.
	 */
	protected Transferable createTransferable(JComponent c) {
		AIDBrowseTreeTable table = (AIDBrowseTreeTable) c;
		int selectedRow = table.getSelectedRow();
		TreePath path = table.getPathForRow(selectedRow);
		AIDBrowseTreeTableNode node = (AIDBrowseTreeTableNode) path
				.getLastPathComponent();

		
		return new StringSelection(node.getTerm() + "\n" + node.getUrl() + "\n"
				+ node.getRepositoryName());

	}

	/**
	 * We support both copy and move actions.
	 */
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY_OR_MOVE;
	}

}
