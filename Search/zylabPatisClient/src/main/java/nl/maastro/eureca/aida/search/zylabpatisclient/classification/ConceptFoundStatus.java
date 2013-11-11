// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.classification;

import java.net.URI;
import java.util.NoSuchElementException;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;

/**
 * Whether the searched {@link nl.maastro.eureca.aida.search.zylabpatisclient.Concept} was found in the
 * searched documents.
 * 
 * @author Kasper van den Berg <kasper.vandenberg@maastron.nl> <kasper@kaspervandenberg.net>
 */
public enum ConceptFoundStatus {
	/**
	 * When the searched criterion is not found in documents about the patient.
	 */
	NOT_FOUND,

	/**
	 * The concept is not found within the patient's documents.
	 */
	FOUND,

	/**
	 * An exclusion criterion is found but the criterion is modified by a
	 * {@link SemanticModifier}.
	 * 
	 * <p><em>NOTE: {@code UNCERTAIN} is reserved for semantically modified
	 * criteria.  If different documents or different matches within one
	 * document result in different {@code Classifications}, use a set of 
	 * {@code Classifications} and do not classify as {@code UNCERTAIN}.</em></p>
	 * 
	 * <p><em>NOTE: compare with {@link #UNKNOWN}</em></p>
	 */
	UNCERTAIN,

	/**
	 * Used for {@link SearchResult} based on queries with unknown criterion/
	 * semantically modified criterion source.
	 */	
	FOUND_CONCEPT_UNKNOWN,
	
	/**
	 * When it's unknown whether the concept (should) be found; for example within 
	 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedResults} for patients/documents 
	 * that are not in 
	 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.validation.ExpectedResults#getDefinedPatients()}.
	 */
	UNKNOWN,

	/**
	 * When one source states that the concept is {@link #NOT_FOUND} and an other states that it is {@link #FOUND}
	 * and no conflict resolution is defined.
	 */
	CONFLICTING
	
	;
	
	private final QName qname;

	private ConceptFoundStatus() {
		this.qname = new QName(Config.PropertyKeys.RDF_VALIDATION_URI.getValue(), name().toLowerCase(), 
				Config.PropertyKeys.RDF_VALIDATION_PREFIX.getValue());
	}

	public static ConceptFoundStatus valueOf(QName id) {
		for (ConceptFoundStatus item : values()) {
			if(item.getQName().equals(id)) {
				return item;
			}
		}
		throw new NoSuchElementException(String.format("No value for %s", id));
	}
	
	public QName getQName() {
		return qname;
	}

}
