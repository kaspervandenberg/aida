// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.util;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.UUID;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
import checkers.nullness.quals.MonotonicNonNull;
import checkers.nullness.quals.EnsuresNonNull;

/**
 *
 * @author kasper
 */
public class QNameUtil {
	private static final String QNAME_SEP = ":";
	
	private QNameUtil() { }

	private static @MonotonicNonNull QNameUtil singleton = null;
	
	@EnsuresNonNull("singleton")
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

	public String getPrefixedName(QName name) {
		if (!name.getPrefix().isEmpty()) {
			return name.getPrefix() + QNAME_SEP + name.getLocalPart();
		} else {
			return name.getLocalPart();
		}
	}
	
	public String tinySemiUnique() {
		UUID uuid = UUID.randomUUID();
		ByteBuffer bytes_8 = ByteBuffer.allocate(8);
		ByteBuffer bytes_4 = ByteBuffer.allocate(4);
		bytes_8.putLong(uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits());
		bytes_4.putInt(bytes_8.getInt(0) ^ bytes_8.get(4));
		return DatatypeConverter.printBase64Binary(bytes_4.array()).replace("=", "");
	}
	
	/**
	 * Build an {@link QName} for a preconstructed pattern, modifier, or concept.
	 * The {@code QName} uses
	 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.config.Config.PropertyKeys#PRECONSTRUCTED_PREFIX},
	 * the namespace URI
	 * {@link nl.maastro.eureca.aida.search.zylabpatisclient.config.Config.PropertyKeys#PRECONSTRUCTED_URI}
	 * and this local part.  {@code createQName()}
	 * uses {@link QName#QName(java.lang.String, java.lang.String)}
	 * to build the requested {@code QName}.
	 * 
	 * @param localpart	the local part of the QName to create
	 * 
	 * @return	the constructed {@link QName}
	 * 
	 * @throws URISyntaxException when the constructed URI has an syntax 
	 * 		error. 
	 */
	public QName createQName_inPreconstructedNamespace(String localpart) 
			throws URISyntaxException {
		return new QName(Config.PropertyKeys.PRECONSTRUCTED_URI.getValue(),
				localpart,
				Config.PropertyKeys.PRECONSTRUCTED_PREFIX.getValue());
	}
}
