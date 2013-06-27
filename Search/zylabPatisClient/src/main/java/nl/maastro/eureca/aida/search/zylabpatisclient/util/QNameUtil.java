// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.util;

import javax.xml.namespace.QName;

/**
 *
 * @author kasper
 */
public class QNameUtil {
	private QNameUtil() { }

	private static QNameUtil singleton = null;
	
	public static QNameUtil instance() {
		if(singleton == null) {
			singleton = new QNameUtil();
		}
		return singleton;
	}

	public QName append(final QName name, String appendix) {
		return new QName(name.getNamespaceURI(),
				name.getLocalPart() + appendix,
				name.getPrefix());
	}
}
