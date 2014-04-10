// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient;

import checkers.nullness.quals.EnsuresNonNullIf;
import checkers.nullness.quals.NonNull;
import checkers.nullness.quals.Nullable;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
	private final @Nullable URI available;
	private final @Nullable String documentType;
	private final @Nullable Date patientBirthDate;
	private final Gender patientGender;
	private final Map<SemanticModifier, Set<Snippet>> snippets;

	public ResultDocument(DocumentId docId_, @Nullable URI available_, @Nullable String documentType_,
			@Nullable Date patientBirthDate_, Gender patientGender_,
			Map<SemanticModifier, Set<Snippet>> snippets_) {
		docId = docId_;
		available = available_;
		documentType = documentType_;
		patientBirthDate = patientBirthDate_;
		patientGender = patientGender_;
		snippets = snippets_;
	}

	public ResultDocument(ResultDocument other) {
		this.docId = other.getId();
		this.available = other.getUrl();
		this.documentType = other.getType();
		this.patientBirthDate = other.getPatientBirthDate();
		this.patientGender = other.getPatientGender();
		
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
				throw new IllegalArgumentException(String.format(
						"Combined ResultDocuments should all have the same document id, url, and type.\n"
						+ "source id: %s, type: %s, url: %s\n"
						+ "target id: %s, type: %s, url: %s",
						src.getId(), src.getType(), src.getUrl(),
						target.getId(), target.getType(), target.getUrl()));
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

	@SuppressWarnings("nullness")
	@EnsuresNonNullIf(expression={"available", "getUrl()"}, result=true )
	public boolean isAvailable() {
		return available != null;
	}
	
	public @Nullable URI getUrl() {
		return available;
	}

	public @Nullable String getType() {
		return documentType;
	}

	public String getTypeOrDefault(String defaultType) {
		if (documentType != null) {
			if (!documentType.isEmpty()) {
				return documentType;
			}
		}
		return defaultType;
	}

	public @Nullable Date getPatientBirthDate() {
		Date result = this.patientBirthDate;
		if (result != null) {
			return new Date(result.getTime());
		} else {
			return null;
		}
	}

	public Gender getPatientGender() {
		return this.patientGender;
	}

	public Set<SemanticModifier> getModifiers() {
		return Collections.unmodifiableSet(snippets.keySet());
	}
	
	public Set<Snippet> getSnippets(SemanticModifier modifier) {
		Set<Snippet> result = snippets.get(modifier);
		if (result != null) {
			return Collections.unmodifiableSet(result);
		} else {
			return Collections.<Snippet>emptySet();
		}
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
			@SuppressWarnings("nullness")
			@NonNull Set<Snippet> tmp = snippets.remove(oldValue);
			snippets.put(newValue, tmp);
			return tmp;
		} else {
			return Collections.emptySet();
		}
	}
}
