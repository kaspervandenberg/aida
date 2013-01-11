package org.vle.aid.taverna.build;

import java.util.List;
import java.util.Vector;

import org.vle.aid.taverna.remote.AIDRemoteQuery;

/**
 * aida-plugin
 * Tree node for query building.
 * @author wibisono
 * @date Apr 23, 2009 10:50:12 PM
 */
public class AIDBuildTreeTableNode {

	private String term, url;

	/* Whether or not the query must have this term or must not have */
	private boolean mustOccur, mustNotOccur;

	private List<AIDBuildTreeTableNode> children = null;

	public AIDBuildTreeTableNode() {
		term = "Root Query";
		url = "";
		children = new Vector<AIDBuildTreeTableNode>();
	}

	public AIDBuildTreeTableNode(String term, String url) {
		this.term = term;
		this.url = url;
		mustOccur = false;
		mustNotOccur = false;
		children = new Vector<AIDBuildTreeTableNode>();
	}

	/**
	 * Build query for this node and the rest of the children 
	 * Made based on the behaviour of aida web version except currently AltLabel is not yet shown/used
	/* @param parentMarked means parent already marked must occur or must not occur, 
	 * 					   therefore the children does not need to be prepended with - or +
	 * 
	 */
	public String getQueryString(boolean parentMarked){
			StringBuffer result = new StringBuffer("(\""+term+"\")");
			if(mustOccur){
				if(!parentMarked) {
					result.insert(0, "+");
					if(children.size() > 0)	result.append("+(");
				}
				for(AIDBuildTreeTableNode child: children)
					result.append(child.getQueryString(true));
				if(!parentMarked){
					if(children.size() > 0) result.append(")");
				}
			}
			else
			if(mustNotOccur){
				if(!parentMarked) {
					result.insert(0, "-");
					if(children.size() > 0) result.append("-(");
				}
				for(AIDBuildTreeTableNode child: children)
					result.append(child.getQueryString(true));
				if(!parentMarked){
					if(children.size() > 0) result.append(")");
				}
			}
			else{
				for(AIDBuildTreeTableNode child: children)
					result.append(child.getQueryString(false));
			}
			return result.toString();
	}
	public boolean isLeaf() {
		return children.size() == 0;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setMustOccur(boolean mustHave) {

		this.mustOccur = mustHave;
		if (mustHave)
			mustNotOccur = false;

	}

	public void setMustNotOccur(boolean mustNotHave) {
		this.mustNotOccur = mustNotHave;
		if (mustNotHave)
			mustOccur = false;
	}

	public boolean getMustOccur() {
		return mustOccur;
	}

	public boolean getMustNotOccur() {
		return mustNotOccur;
	}

	public List<AIDBuildTreeTableNode> getChildren() {
		return children;
	}

	public void addChild(AIDBuildTreeTableNode node) {
		children.add(node);
	}

	public void addChild(String term, String url) {
		AIDBuildTreeTableNode newChild = new AIDBuildTreeTableNode(term, url);
		children.add(newChild);
	}

	public void removeChild(String term, String url) {
		removeChild(new AIDBuildTreeTableNode(term, url));
	}

	public void removeChild(AIDBuildTreeTableNode node) {
		children.remove(node);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AIDBuildTreeTableNode) {
			AIDBuildTreeTableNode other = (AIDBuildTreeTableNode) obj;
			return term.equals(other.term) && url.equals(other.url);
		}
		return false;
	}


	public void expandTree(String repositoryName) {

		AIDRemoteQuery repository = new AIDRemoteQuery(repositoryName);
		try {
			String[][] narrowerTerms = repository.getNarrowerTerms(url);
			for (int i = 0; i < narrowerTerms.length; i++) {
				addChild(narrowerTerms[i][1], narrowerTerms[i][0]);
			}
			for (AIDBuildTreeTableNode child : children)
				child.expandTree(repositoryName);
		} catch (Exception e) {
		}

	}

}
