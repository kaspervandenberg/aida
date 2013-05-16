// © Maastro Clinics, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class SimpleNamespaceContext implements NamespaceContext {
	private final Map<String, String> namespaces;

	public SimpleNamespaceContext(String defaultNamespace, 
			Map<String, String> mappings) {
		namespaces = new HashMap<>();
		namespaces.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
		namespaces.put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
		namespaces.put(XMLConstants.DEFAULT_NS_PREFIX, defaultNamespace);
		namespaces.putAll(mappings);
	}

	@Override
	public String getNamespaceURI(String prefix) {
		if (prefix == null) {
			throw new IllegalArgumentException("Resolving null namespace prefix");
		}
		String result = namespaces.get(prefix);
		if (result == null) {
			return XMLConstants.NULL_NS_URI;
		}
		return result;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		if (namespaceURI == null) {
			throw new IllegalArgumentException("Resolving null namespaceURI");
		}
		Iterator<String> i = getPrefixes(namespaceURI);
		if(!i.hasNext()) {
			return null;
		}
		return i.next();
	}

	@Override
	public Iterator<String> getPrefixes(final String namespaceURI) {
		if (namespaceURI == null) {
			throw new IllegalArgumentException("Resolving null namespaceURI");
		}
		
		return new Iterator<String>() {
			Iterator<Map.Entry<String, String>> delegate = 
					namespaces.entrySet().iterator();
			String nextPrefix = null;

			@Override
			public boolean hasNext() {
				while (nextPrefix == null && delegate.hasNext()) {
					Map.Entry<String, String> next = delegate.next();
					if(namespaceURI.equals(next.getValue())) {
						nextPrefix = next.getKey();
					}
				}
				return nextPrefix != null;
			}

			@Override
			public String next() {
				if(!hasNext()) {
					throw new NoSuchElementException("Iteration at end");
				}
				String result = nextPrefix;
				nextPrefix = null;
				return result;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported.");
			}
		};
	}
}
