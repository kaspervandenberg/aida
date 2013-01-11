package org.vle.aid.taverna.build;

import java.util.List;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.treetable.TreeTableModel;

/**
 * TreeTableModel for building query aida-plugin
 * 
 * @author wibisono
 * @date Apr 23, 2009 10:40:50 PM
 */
public class AIDBuildTreeTableModel implements TreeTableModel {
	protected EventListenerList listenerList;

	private AIDBuildTreeTableNode root = null;

	public AIDBuildTreeTableModel() {
		root = new AIDBuildTreeTableNode();
		listenerList = new EventListenerList();
	}

	public AIDBuildTreeTableModel(AIDBuildTreeTableNode root) {
		this.root = root;
		this.listenerList = new EventListenerList();
	}

	/**
	 * Helper to retrieve query represented by current tree
	 * @return query based on the current query building tree
	 */
	public String getQueryString(){
		if(root == null) return "";
		
		String result = "";
		
		// We don't want to use the real root of query which only contains "Root Query" string
		// We combine results from the first level children instead.
		for(AIDBuildTreeTableNode child : root.getChildren()){
			result += child.getQueryString(false)+" ";
		}
		
		return result;
	}
	/**
	 * {@inheritDoc}
	 */
	public AIDBuildTreeTableNode getChild(Object parent, int index) {
		if (parent instanceof AIDBuildTreeTableNode) {
			AIDBuildTreeTableNode parentNode = (AIDBuildTreeTableNode) parent;
			List<AIDBuildTreeTableNode> children = parentNode.getChildren();
			if (children != null) {
				return children.get(index);
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getChildCount(Object parent) {
		if (parent instanceof AIDBuildTreeTableNode) {
			AIDBuildTreeTableNode parentNode = (AIDBuildTreeTableNode) parent;
			List<AIDBuildTreeTableNode> children = parentNode.getChildren();
			if (children != null) {
				return children.size();
			}
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<?> getColumnClass(int column) {
		switch (column) {
		case 0:
			return String.class;
		case 1:
			return Boolean.class;
		case 2:
			return Boolean.class;

		default:
			return Object.class;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getColumnCount() {
		return 3;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "<html><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Concept&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</b></html>";
		case 1:
			//return "<html><b>Must<br>Occur</b></html>";
			return "<html><b><font color=Green>√</b></html>";
		case 2:
			return "<html><b><font color=Red>X</b></html>";
			//return "<html><b>Must<br>Not Occur</b></html>";

		default:
			return "Column " + column;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getValueAt(Object node, int column) {
		if (node instanceof AIDBuildTreeTableNode) {
			AIDBuildTreeTableNode treeNode = (AIDBuildTreeTableNode) node;
			switch (column) {
			case 0:
				return treeNode.getTerm();
			case 1:
				return treeNode.getMustOccur();
			case 2:
				return treeNode.getMustNotOccur();
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getHierarchicalColumn() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCellEditable(Object node, int column) {
		return column >= 1;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValueAt(Object value, Object node, int column) {
		if (column >= 1) {
			AIDBuildTreeTableNode tNode = (AIDBuildTreeTableNode) node;
			if (column == 1)
				tNode.setMustOccur((Boolean) value);
			if (column == 2)
				tNode.setMustNotOccur((Boolean) value);
			
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addTreeModelListener(TreeModelListener l) {
		listenerList.add(TreeModelListener.class, l);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getIndexOfChild(Object parent, Object child) {
		if (parent instanceof AIDBuildTreeTableNode
				&& child instanceof AIDBuildTreeTableNode) {
			AIDBuildTreeTableNode parentNode = (AIDBuildTreeTableNode) parent;
			AIDBuildTreeTableNode childNode = (AIDBuildTreeTableNode) child;

			List<AIDBuildTreeTableNode> children = parentNode.getChildren();
			return children.indexOf(childNode);
		}

		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	public AIDBuildTreeTableNode getRoot() {
		return root;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isLeaf(Object node) {
		// Node can be null
		if(node != null){
			if (node instanceof AIDBuildTreeTableNode) {
				return ((AIDBuildTreeTableNode) node).isLeaf();
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeTreeModelListener(TreeModelListener l) {
		listenerList.remove(TreeModelListener.class, l);
	}

	/**
	 * {@inheritDoc}
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		// does nothing
	}

	/**
	 * Gets a an array of all the listeners attached to this model.
	 * 
	 * @return an array of listeners; this array is guaranteed to be non-{@code null}
	 */
	public TreeModelListener[] getTreeModelListeners() {
		return listenerList.getListeners(TreeModelListener.class);
	}

	/**
	 * Notify listeners that the structure of expanded node has changed.
	 * 
	 * @param evt
	 *                from the TreeExpansions which trigger expansionWorker
	 */
	public void fireStructureChange(TreeExpansionEvent evt) {
		TreeModelListener[] listeners = getTreeModelListeners();
		for (TreeModelListener listener : listeners) {
			listener.treeStructureChanged(new TreeModelEvent(evt.getSource(),
					evt.getPath()));
		}
	}
}
