/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.search.zylabpatisclient.validation.sparqlConstants;

import java.net.URI;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.Concept;
import nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber;
import nl.maastro.eureca.aida.search.zylabpatisclient.classification.ConceptFoundStatus;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Query;

/**
 *
 * @author kasper
 */
 public enum RdfVariableBindings {
	PATIENT {
		@Override
		public <T> T getValue(Class<T> valueType, BindingSet boundVariables) {
			if(valueType.isAssignableFrom(PatisNumber.class)) {
				return valueType.cast(getPatient(boundVariables));
			} else {
				throw new IllegalArgumentException(createIllegalValueTypeMsg(PatisNumber.class, valueType));
			}
		}
	},

	CONCEPT,
	EXPECTATION_ID,

	STATUS {
		@Override
		public <T> T getValue(Class<T> valueType, BindingSet boundVariables) {
			if(valueType.isAssignableFrom(ConceptFoundStatus.class)) {
				return valueType.cast(getFoundStatus(boundVariables));
			} else {
				throw new IllegalArgumentException(createIllegalValueTypeMsg(ConceptFoundStatus.class, valueType));
			}
		}
	},
	
	TITLE {
		@Override
		public <T> T getValue(Class<T> valueType, BindingSet boundVariables) {
			if(valueType.isAssignableFrom(String.class)) {
				return valueType.cast(getStringValue(boundVariables));
			} else {
				throw new IllegalArgumentException(createIllegalValueTypeMsg(String.class, valueType));
			}
		}
	};

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

	public <T> T getValue(Class<T> valueType, BindingSet boundVariables) {
		throw new UnsupportedOperationException("Not yet implemented.");
	} 
	
	public PatisNumber getPatient(BindingSet boundVariables) {
		return PatisNumber.create(getStringValue(boundVariables));
	}

	public ConceptFoundStatus getFoundStatus(BindingSet boundVariables) {
		org.openrdf.model.URI uri_value = getUri(boundVariables);
		QName qname_value = new QName(uri_value.getNamespace(), uri_value.getLocalName());
		return ConceptFoundStatus.valueOf(qname_value);
	}

	protected String getStringValue(BindingSet boundVariables) {
		Value value = getBinding(boundVariables).getValue();
		return value.stringValue();
	}

	private org.openrdf.model.URI getUri(BindingSet boundVariables) {
		Value obj_value = getBinding(boundVariables).getValue();
		if(!(obj_value instanceof org.openrdf.model.URI)) {
			throw new ClassCastException(String.format(
					"Value %s (of binding %s) is not an URI", obj_value.stringValue(), this.name()));
		}
		return (org.openrdf.model.URI)obj_value;
	}

	private Binding getBinding(BindingSet boundVariables) {
		Binding result = boundVariables.getBinding(getBindingName());
		if (result == null) {
			throw new IllegalStateException(String.format("%s is not bound", this.name()));
		}
		return result;
	}

	protected String createIllegalValueTypeMsg(Class<?> acceptedType, Class<?> actualType) {
		String result = String.format(
				"%s.getValue() only supports class %s; received %s.",
				this.name(), acceptedType.getSimpleName(), actualType.getSimpleName());
		return result;
	}
}
