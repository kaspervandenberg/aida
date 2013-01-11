package org.vle.aid.taverna.browse;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.LinkModel;
import org.jdesktop.swingx.treetable.TreeTableModel;
import org.vle.aid.taverna.remote.AIDRemoteQuery;

/**
 * aida-plugin
 * 
 * @author wibisono
 * @date Apr 23, 2009 10:40:50 PM
 */
public class AIDBrowseTreeTableModel implements TreeTableModel {
	protected EventListenerList listenerList;

	private AIDBrowseTreeTableNode root;

	private String conceptColumnName = "<html><center><b>Concept</b></center></html>";
	
	public AIDBrowseTreeTableModel() {
		this(new AIDBrowseTreeTableNode(AIDRemoteQuery.getDefaultThesaurusRepository(),"/","/"));
	}

	public AIDBrowseTreeTableModel(AIDRemoteQuery rep) {
		this(new AIDBrowseTreeTableNode(rep));
		listenerList = new EventListenerList();
	}

	public AIDBrowseTreeTableModel(AIDBrowseTreeTableNode root) {
		this.root = root;
		this.listenerList = new EventListenerList();
		setConceptColumn("<html><center><b>"+root.getSkosVersion()+"</b></center></html>");
	}

	public void setConceptColumn(String conceptColumn){
		   conceptColumnName = conceptColumn;
	}
	/**
	 * {@inheritDoc}
	 */
	public AIDBrowseTreeTableNode getChild(Object parent, int index) {
		if (parent instanceof AIDBrowseTreeTableNode) {
			AIDBrowseTreeTableNode parentAIDThesaurusNode = (AIDBrowseTreeTableNode) parent;
			AIDBrowseTreeTableNode[] children = parentAIDThesaurusNode.getChildren();
			if (children != null) {
				return children[index];
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getChildCount(Object parent) {
		if (parent instanceof AIDBrowseTreeTableNode) {
			String[] childTerms = ((AIDBrowseTreeTableNode) parent).getChildTerms();

			if (childTerms != null) {
				return childTerms.length;
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
			return LinkModel.class;
		case 2:
			return String.class;
			
		default:
			return Object.class;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getColumnCount() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return conceptColumnName;
		case 1:
			return "Url";
		case 2:
			return "Repository";
		
		default:
			return "Column " + column;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getValueAt(Object node, int column) {
		if (node instanceof AIDBrowseTreeTableNode) {
			AIDBrowseTreeTableNode tNode = (AIDBrowseTreeTableNode) node;
			switch (column) {
			case 0:
				return tNode.getTerm();
			case 1:
				return tNode.getUrl();
			case 2:
			    	return tNode.getRepositoryName();
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
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValueAt(Object value, Object node, int column) {
		// does nothing
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
		if (parent instanceof AIDBrowseTreeTableNode && child instanceof AIDBrowseTreeTableNode) {
			AIDBrowseTreeTableNode parentAIDThesaurusNode = (AIDBrowseTreeTableNode) parent;
			AIDBrowseTreeTableNode[] childs = parentAIDThesaurusNode.getChildren();

			for (int i = 0, len = childs.length; i < len; i++) {
				if (childs[i].equals(child)) {
					return i;
				}
			}
		}

		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	public AIDBrowseTreeTableNode getRoot() {
		return root;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isLeaf(Object node) {
		if (node instanceof AIDBrowseTreeTableNode) {
			return ((AIDBrowseTreeTableNode) node).isLeaf();
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
	 * @param expandedPath     path to expanded node
	 * @param source			source of this expansion event.
	 */
	public void fireStructureChange(Object source, TreePath expandedPath) {
		TreeModelListener[] listeners = getTreeModelListeners();
		for (TreeModelListener listener : listeners) {
			listener.treeStructureChanged(new TreeModelEvent(source, expandedPath));
		}
	}
	/**
	 * Notify listeners that the structure of expanded node has changed.
	 * 
	 * @param expandedPath     path to expanded node
	 * @param source			source of this expansion event.
	 */
	public void fireNodeAdded(Object source, TreePath expandedPath) {
		TreeModelListener[] listeners = getTreeModelListeners();
		for (TreeModelListener listener : listeners) {
			listener.treeNodesInserted(new TreeModelEvent(source, expandedPath));
		}
	}

	public void fireNodeRemoved(Object source, TreePath expandedPath) {
		TreeModelListener[] listeners = getTreeModelListeners();
		for (TreeModelListener listener : listeners) {
			listener.treeNodesRemoved(new TreeModelEvent(source, expandedPath));
		}
	}
}
