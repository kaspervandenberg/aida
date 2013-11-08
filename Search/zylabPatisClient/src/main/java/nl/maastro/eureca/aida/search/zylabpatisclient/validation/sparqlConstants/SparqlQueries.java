/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient.validation.sparqlConstants;

import static nl.maastro.eureca.aida.search.zylabpatisclient.validation.sparqlConstants.RdfVariableBindings.*;
import static nl.maastro.eureca.aida.search.zylabpatisclient.validation.sparqlConstants.RdfPredicates.*;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author kasper
 */
 public enum SparqlQueries {
	PATIENT_QUERY(
				CONCEPT, EXPECTS_RESULTS, EXPECTATION_ID, ".",
				EXPECTATION_ID, HAS_PATIENT, "_:patient", ".", 
				"_:patient", PATISNUMBER, PATIENT) {

		@Override
		public Query prepareQuery(RepositoryConnection connection) throws RepositoryException {
			throw new UnsupportedOperationException("Not supported.");
		}
	},
	
	DEFINED_PATIENTS("SELECT", PATIENT, "WHERE", "{", PATIENT_QUERY, "}"),

	IS_PATIENT_DEFINED("ASK", "{", PATIENT_QUERY, "}"),
	
	IS_AS_EXPECTED("ASK", "{", PATIENT_QUERY, ".",
			"_:patient", RESULT, "_:result", ".",
			"_:result", PART_OF, EXPECTATION_ID, ";",
				HAS_STATUS, STATUS, "}");

	private final StringBuilder contents = new StringBuilder();

	private SparqlQueries(Object... query) {
		addObjects(query);
	}

	public Query prepareQuery(RepositoryConnection connection) throws RepositoryException {
		try {
			return connection.prepareQuery(QueryLanguage.SPARQL, contents.toString());
		} catch (MalformedQueryException ex) {
			throw new Error(ex);
		}
	} 

	String getContents() {
		return contents.toString();
	}

	private SparqlQueries add(String str) {
		addSep();
		contents.append(str);
		return this;
	}

	private SparqlQueries add(RdfPredicates pred) {
		addSep();
		contents.append("<");
		contents.append(pred.getFullName());
		contents.append(">");
		return this;
	}

	private SparqlQueries add(RdfVariableBindings binding) {
		addSep();
		contents.append(binding.getQuery());
		return this;
	}

	private String getValue() {
		return contents.toString();
	}

	private void addObjects(Object[] objects) {
		for (Object obj : objects) {
			addObject(obj);
		}
	}

	private void addObject(Object obj) {
		if (obj instanceof String) {
			this.add((String) obj);
		} else if (obj instanceof RdfPredicates) {
			this.add((RdfPredicates) obj);
		} else if (obj instanceof RdfVariableBindings) {
			this.add((RdfVariableBindings) obj);
		} else if (obj.getClass().isArray()) {
			this.addObjects((Object[]) obj);
		} else if (obj instanceof SparqlQueries) {
			this.add(((SparqlQueries)obj).getValue());
		} else {
			throw new IllegalArgumentException(new ClassCastException(String.format("Expecting String, Predicates, or Bindings received %s", obj.getClass())));
		}
	}

	private void addSep() {
		if (contents.length() > 0) {
			contents.append(" ");
		}
	}

	protected BooleanQuery prepareBooleanQueryImpl(RepositoryConnection connection) throws RepositoryException {
		try {
			return connection.prepareBooleanQuery(QueryLanguage.SPARQL, contents.toString());
		} catch (MalformedQueryException ex) {
			throw new Error(ex);
		}
	}
	
}
