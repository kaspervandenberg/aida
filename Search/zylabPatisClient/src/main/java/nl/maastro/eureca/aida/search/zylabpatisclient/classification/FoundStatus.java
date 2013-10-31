// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.classification;

/**
 * Whether the searched {@link nl.maastro.eureca.aida.search.zylabpatisclient.Concept} was found in the
 * searched documents.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public enum FoundStatus {
	NOT_FOUND,
	FOUND,

	/**
	 * When a concept is modified by a semantic modifier.
	 */
	UNCERTAIN,
	
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
}
