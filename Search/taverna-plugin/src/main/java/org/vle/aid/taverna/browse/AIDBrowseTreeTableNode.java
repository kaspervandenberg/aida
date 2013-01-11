package org.vle.aid.taverna.browse;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.vle.aid.taverna.components.Gui;
import org.vle.aid.taverna.remote.AIDRemoteQuery;

/**
 * aida-plugin
 * 
 * @author wibisono
 * @date Apr 23, 2009 10:50:12 PM
 */
public class AIDBrowseTreeTableNode {
	private String term, url;

	private AIDRemoteQuery repository = null;

	private AIDBrowseTreeTableNode[] children = null;

	private String[] childTerms = null;

	private boolean isLeaf = false;

	private boolean expanded = false;

	private static final Logger logger = Logger
			.getLogger(AIDBrowseTreeTableNode.class);

	public AIDBrowseTreeTableNode(AIDRemoteQuery repository) {
		this.url = repository.getServerUrl();
		this.term = repository.getRepository();
		this.repository = repository;
		initializeRootRepository();
	}

	/**
	 * @param repository
	 * @param url
	 * @param term
	 */
	public AIDBrowseTreeTableNode(AIDRemoteQuery repository, String url,
			String term) {
		this.repository = repository;
		this.term = term;
		this.url = url;
	}

	public boolean isLeaf() {
		return isLeaf;
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

	public String getRepositoryName() {
		return repository.getRepository();
	}

	public AIDBrowseTreeTableNode[] getChildren() {

		return children;
	}

	public String[] getChildTerms() {

		return childTerms;
	}

	public boolean expanded() {
		return expanded;
	}

	public String getSkosVersion(){
		if(repository.skosVersion.contains("2004")) return "SKOS 2004";
		if(repository.skosVersion.contains("2008")) return "SKOS 2008";
		return repository.skosVersion;
	}
	
	private void initializeRootRepository() {
		try {
			AIDRemoteQuery repository1 = AIDRemoteQuery.getDefaultRepositoryDetect();
			repository1.setRepository(repository.getRepository());
			String [] results = repository1.detectRepository();
			
			repository.skosVersion = repository1.skosVersion;
			repository.virtuosoNamedGraph = repository1.virtuosoNamedGraph;
			
			String[][] conceptSchemes = repository.getConceptSchemes();
			
			//System.out.println("Test "+conceptSchemes.length);
			String conceptScheme ="";

			if(conceptSchemes.length > 0)
				conceptScheme = conceptSchemes[0][0];
			
			
			String topConcepts [][] = repository.getTopConcepts(conceptScheme);
			//if (topConcepts == null)
			//	throw new Exception("Failed to get top concept scheme");

			children = new AIDBrowseTreeTableNode[topConcepts.length];
			childTerms = new String[topConcepts.length];
			for (int i = 0; i < topConcepts.length; i++) {
				children[i] = new AIDBrowseTreeTableNode(repository,
						topConcepts[i][0], topConcepts[i][1]);
				childTerms[i] = topConcepts[i][1];
			}

			isLeaf = (children.length == 0);
		} catch (Exception e) {
			logger.error("Failed to initialize Root Repository : " + e.getMessage() );
			
		}
	}

	public void expandNode() {
		try {
			String[][] narrowerTerms = repository.getNarrowerTerms(url);
			if (narrowerTerms == null)
				throw new Exception("Failed to  get narrower terms");

			children = new AIDBrowseTreeTableNode[narrowerTerms.length];
			childTerms = new String[narrowerTerms.length];
			for (int i = 0; i < narrowerTerms.length; i++) {
				children[i] = new AIDBrowseTreeTableNode(repository,
						narrowerTerms[i][0], narrowerTerms[i][1]);
				childTerms[i] = narrowerTerms[i][1];
			}

			isLeaf = (children.length == 0);
			expanded = true;
		} catch (Exception e) {
			logger.error("Failed to initialize children : " + e.getMessage());
			Gui.showErrorWarnings("Failed to expand node : " + repository, e);
		}
	}

	public static void main(String[] args) {
		AIDBrowseTreeTableNode node = new AIDBrowseTreeTableNode(AIDRemoteQuery
				.getDefaultThesaurusRepository());
		//System.out.println(Arrays.toString(node.getChildTerms()));
	}

	public int getChildCount() {
		// TODO Auto-generated method stub
		return childTerms.length;
	}

	public AIDBrowseTreeTableNode getChild(int index) {
		// TODO Auto-generated method stub
		return children[index];
	}

}
