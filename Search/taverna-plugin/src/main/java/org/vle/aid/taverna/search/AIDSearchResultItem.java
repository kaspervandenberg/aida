package org.vle.aid.taverna.search;

import java.net.MalformedURLException;
import java.net.URL;

import org.jdesktop.swingx.LinkModel;

public class AIDSearchResultItem {
	
	Float score;
	String snippet;
	String description;
	String id;
	String label;
	LinkModel Uri;
	String title;
	
	public AIDSearchResultItem(Float score, String snippet,  String description, String id, String label, String uri,  String title) {
	    	this.score = score;
		this.snippet = snippet;
		this.description = description;
		this.id = id;
		this.label = label;	
		this.Uri = createLink(title, uri.replaceAll(" ","%20"));
		this.title = title;
		
	}

	public String getDescription() {
	    return description;
	}


	public void setDescription(String description) {
	    this.description = description;
	}


	public String getId() {
	    return id;
	}


	public void setId(String id) {
	    this.id = id;
	}


	public String getLabel() {
	    return label;
	}


	public void setLabel(String label) {
	    this.label = label;
	}

	public Float getScore() {
	    return score;
	}


	public void setScore(Float score) {
	    this.score = score;
	}


	public String getSnippet() {
	    return snippet;
	}


	public void setSnippet(String snippet) {
	    this.snippet = snippet;
	}


	public String getTitle() {
	    return title;
	}


	public void setTitle(String title) {
	    this.title = title;
	}


	public LinkModel getUri() {
	    return Uri;
	}


	public void setUri(LinkModel uri) {
	    Uri = uri;
	}


	@Override
	public String toString() {
	    return "\nScore: "+score+" \n   Snippet "+snippet+" \n   Description  "+description+" \n   ID  "+id+" \n   Label "+label+" \n   URI "+Uri.toString()+" \n   Title "+title;
	}

	private LinkModel createLink(String description, String urlString) {
		try {			
			return new LinkModel(description, null, new URL(urlString));
		} catch (MalformedURLException e) {
			// ignore - something went wrong
		}
		return null;
	}

}
