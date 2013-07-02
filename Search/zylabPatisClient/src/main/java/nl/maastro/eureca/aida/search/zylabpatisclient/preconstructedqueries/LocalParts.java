/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient.preconstructedqueries;

import java.net.URISyntaxException;
import javax.xml.namespace.QName;

/**
 * QName local parts that prefixed with {@link PreconstructedQueries#getNamespaceUri()}
 * form the {@link QName}s that identify the {@link Query}s in
 * {@link #storedPredicates}.
 */
public enum LocalParts {
	METASTASIS("metastasis", Concepts.METASTASIS, null), HINTS_METASTASIS("hints_metastasis", Concepts.METASTASIS, SemanticModifiers.SUSPICION), NO_METASTASIS("no_metastasis", Concepts.METASTASIS, SemanticModifiers.NEGATED);
	private final String value;
	private final Concepts concept;
	private final SemanticModifiers modifier;
	private transient QName id = null;
	private transient nl.maastro.eureca.aida.search.zylabpatisclient.query.Query query;

	private LocalParts(final String value_, final Concepts concept_, final SemanticModifiers modifier_) {
		value = value_;
		concept = concept_;
		modifier = modifier_;
	}

	/**
	 * Return the {@link QName} composed from this {@code LocalPart} and
	 * {@link #getNamespaceUri()}.
	 *
	 * @see #createQName(java.lang.String)
	 *
	 * @return	the {@link QName} to identify a preconstructed query with
	 */
	public QName getID() {
		if (id == null) {
			try {
				id = PreconstructedQueries.instance().createQName(value);
			} catch (URISyntaxException ex) {
				throw new Error("URISyntaxException in hardcoded URI", ex);
			}
		}
		return id;
	}

	public nl.maastro.eureca.aida.search.zylabpatisclient.query.Query getQuery() {
		if (query == null) {
			if (modifier == null) {
				query = concept;
			} else {
				query = modifier.getAdapter_dynamic().adapt(concept);
			}
		}
		return query;
	}
	
}
