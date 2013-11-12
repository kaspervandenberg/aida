// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.validation.sparqlConstants;

import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
 public enum RdfPredicates {
	EXPECTS_RESULTS("expectsResults"),
	HAS_PATIENT("patient"),
	PATISNUMBER("patis"),
	PART_OF("partOf"),
	HAS_STATUS("status"),
	RESULT("result"),
	LABEL("http://www.w3.org/2000/01/rdf-schema#", "label"),
	
	;


	private final QName qname;

	private RdfPredicates(String localName) {
		this.qname = new QName(Config.PropertyKeys.RDF_VALIDATION_URI.getValue(), localName, Config.PropertyKeys.RDF_VALIDATION_PREFIX.getValue());
	}
	
	private RdfPredicates(String namespace, String localName) {
		this.qname = new QName(namespace, localName);
	}

	public String getFullName() {
		return qname.getNamespaceURI() + qname.getLocalPart();
	}
	
}
