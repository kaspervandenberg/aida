/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient.validation.sparqlConstants;

import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.Concept;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Query;

/**
 *
 * @author kasper
 */
 public enum RdfVariableBindings {
	PATIENT,
	CONCEPT,
	EXPECTATION_ID;

	public String getQuery() {
		return "?" + name().toLowerCase();
	}
	
	public void bind(ValueFactory factory, Query query, PatisNumber patient) {
		Value v = factory.createLiteral(patient.getValue());
	}
	
	public void bind(ValueFactory factory, Query query, Concept concept) {
		bind(factory, query, concept.getName());
	}

	public void bind(ValueFactory factory, Query query, QName qname) {
		Value v = factory.createURI(qname.getNamespaceURI() + qname.getLocalPart());
		bind(query, v);
	}
	
	public void bind(Query query, Value value) {
		query.setBinding(name().toLowerCase(), value);
	}
}
