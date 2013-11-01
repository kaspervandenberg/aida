// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class ResultDocument {
	private final DocumentId docId;
	private final /*@Nullable*/URI available;
	private final /*@Nullable*/String documentType;
	private final Map<SemanticModifier, Set<Snippet>> snippets;

	public ResultDocument(DocumentId docId_, URI available_, String documentType_,
			Map<SemanticModifier, Set<Snippet>> snippets_) {
		docId = docId_;
		available = available_;
		documentType = documentType_;
		snippets = snippets_;
	}

	public ResultDocument(DocumentId docId_, URI available_, String documentType_) {
		this(docId_, available_, documentType_, new HashMap<SemanticModifier, Set<Snippet>>());
	}
	
	public ResultDocument(DocumentId docId_) {
		this(docId_, null, null, new HashMap<SemanticModifier, Set<Snippet>>());
	}

	public ResultDocument(ResultDocument other) {
		this.docId = other.getId();
		this.available = other.getUrl();
		this.documentType = other.getType();
		
		Set<SemanticModifier> othersModifers = other.getModifiers(); 
		this.snippets = new HashMap<>(othersModifers.size());
		for (SemanticModifier semMod : othersModifers) {
			this.snippets.put(semMod, new HashSet<>(other.getSnippets(semMod)));
		}
	}

	public static ResultDocument combine(List<ResultDocument> sources) 
			throws IllegalArgumentException {
		if(sources.isEmpty()) {
			throw new IllegalArgumentException(
					"provide at least 1 source ResultDocument.");
		}
		ResultDocument target = new ResultDocument(sources.get(0));

		for (ResultDocument src : sources.subList(1, sources.size())) {
			if(!target.getId().equals(src.getId()) ||
					!Objects.equals(target.getUrl(), src.getUrl()) ||
					!Objects.equals(target.getType(), src.getType())) {
				throw new IllegalArgumentException(
						"Combined ResultDocuments should all have the same document id, url, and type");
			}
			for (SemanticModifier semMod : src.getModifiers()) {
				target.addAllSnippets(semMod, src.getSnippets(semMod));
			}
		}
		return target;
	}

	public DocumentId getId() {
		return docId;
	}

	public boolean isAvailable() {
		return available != null;
	}
	
	public URI getUrl() {
		return available;
	}

	public String getType() {
		return documentType;
	}

	public Set<SemanticModifier> getModifiers() {
		return Collections.unmodifiableSet(snippets.keySet());
	}
	
	public Set<Snippet> getSnippets(SemanticModifier modifier) {
		return Collections.unmodifiableSet(snippets.get(modifier));
	}

	public Set<Snippet> getSnippets() {
		Set<Snippet> result = new HashSet<>();
		for (Set<Snippet> snippetSet : snippets.values()) {
			result.addAll(snippetSet);
		}
		return result;
	}

	public Set<ConceptFoundStatus> getClassifiers() {
		Set<ConceptFoundStatus> result = new HashSet<>();
		for (SemanticModifier semMod : getModifiers()) {
			result.add(semMod.getClassification());
		}
		return result;
	}

	public void addSnippet(SemanticModifier modifier, Snippet snippet) {
		if(!snippets.containsKey(modifier)) {
			snippets.put(modifier, new HashSet<Snippet>());
		}
		snippets.get(modifier).add(snippet);
	}

	public void addAllSnippets(SemanticModifier modifier, Collection<Snippet> snippetCol) {
		if(!snippets.containsKey(modifier)) {
			snippets.put(modifier, new HashSet<Snippet>());
		}
		snippets.get(modifier).addAll(snippetCol);
	}

	public void removeModifier(SemanticModifier modifier) {
		snippets.remove(modifier);
	}

	public Set<Snippet> reclassify(SemanticModifier oldValue, SemanticModifier newValue) {
		if(snippets.containsKey(oldValue) && !oldValue.equals(newValue)) {
			Set<Snippet> tmp = snippets.remove(oldValue);
			snippets.put(newValue, tmp);
			return tmp;
		} else {
			return Collections.emptySet();
		}
	}
}
