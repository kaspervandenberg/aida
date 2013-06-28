// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.util;

import java.nio.ByteBuffer;
import java.util.UUID;
import javax.xml.bind.DatatypeConverter;
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
	
	public String tinySemiUnique() {
		UUID uuid = UUID.randomUUID();
		ByteBuffer bytes_8 = ByteBuffer.allocate(8);
		ByteBuffer bytes_4 = ByteBuffer.allocate(4);
		bytes_8.putLong(uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits());
		bytes_4.putInt(bytes_8.getInt(0) ^ bytes_8.get(4));
		return DatatypeConverter.printBase64Binary(bytes_4.array()).replace("=", "");
	}
	
}
