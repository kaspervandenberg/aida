/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient.validation.sparqlConstants;

import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.Concept;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Query;

/**
 *
 * @author kasper
 */
 public enum RdfVariableBindings {
	PATIENT,
	CONCEPT,
	EXPECTATION_ID,
	STATUS;

	public String getQuery() {
		return "?" + getBindingName();
	}

	public String getBindingName() {
		return this.name().toLowerCase();
	}
	
	public void bind(ValueFactory factory, Query query, PatisNumber patient) {
		Value v = factory.createLiteral(patient.getValue());
		bind(query, v);
	}
	
	public void bind(ValueFactory factory, Query query, Concept concept) {
		bind(factory, query, concept.getName());
	}

	public void bind(ValueFactory factory, Query query, ConceptFoundStatus status) {
		Value v = factory.createURI(Config.PropertyKeys.RDF_VALIDATION_URI.getValue(), status.name().toLowerCase());
		bind(query, v);
	}
	
	public void bind(ValueFactory factory, Query query, QName qname) {
		Value v = factory.createURI(qname.getNamespaceURI() + qname.getLocalPart());
		bind(query, v);
	}

	public void bind(Query query, Value value) {
		query.setBinding(name().toLowerCase(), value);
	}

	public PatisNumber getPatient(BindingSet bindings) {
		return PatisNumber.create(bindings.getBinding(getBindingName()).getValue().stringValue());
	}
}
