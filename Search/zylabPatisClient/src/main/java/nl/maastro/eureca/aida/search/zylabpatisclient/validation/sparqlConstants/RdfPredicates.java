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
	RESULT("result");


	private final QName qname;

	private RdfPredicates(String localName) {
		this.qname = new QName(Config.PropertyKeys.RDF_VALIDATION_URI.getValue(), localName, Config.PropertyKeys.RDF_VALIDATION_PREFIX.getValue());
	}

	public String getFullName() {
		return qname.getNamespaceURI() + qname.getLocalPart();
	}
	
}
