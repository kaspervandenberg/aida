package org.vle.aid.common;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * A collection of properties properties shared by many AIDA components.
 * 
 * <p>A commonent should access a common property via:
 * <code>Properties.Entries.</code><i>{PROPERTY_NAME}</i><code>.get()</code>
 * </p>
 * 
 * <p>
 * {@code Properties} reads the properties from {@value #propertyFilePath} 
 * which should be accessible in the classpath.<br/>
 * <i>ADVISE:</i>Share the propertyfile between all AIDA Servlets and SOAP 
 * services within a Tomcat instance (or other servlet container).<br/>
 * (Tomcat specific) Specify the directory containing {@value 
 * #propertyFilePath} in "$CATALINA_BASE/conf/catalina.properties"'s 
 * "shared.loader" property.
 * </p>
 */
public class Properties {
	/**
	 * Properties available to all AIDA components.
	 */
	public enum Entries {
		/**
		 * URL where AIDA components can find AXIS.
		 */
		AXIS_ENDPOINT("org.vle.aid.axis_endpoint"),

		/**
		 * Location of the AIDA search-servlet.
		 */
		SEARCH_SERVLET("org.vle.aid.search_servlet");
		
		/**
		 * Retrieve the value of this property.
		 *
		 * @return	the value for this property
		 *
		 * @throws IOException	when {@value propertyFilePath} cannot be read.
		 * @throws NoSuchElementException	when {@value propertyFilePath}
		 *		does not contain a property for the given key.
		 */
		String get() throws IOException,
					NoSuchElementException {
			java.util.Properties map = getPropertyMap();
			if(map == null) {
				throw new Error(new NullPointerException(
					String.format(
					"While looking up property %s; propertyMap is null.", 
					this.name())));
			}
			if(!map.containsKey(key)) {
				throw new NoSuchElementException(
					String.format(
					"While looking up property %s; %s not in propertyMap", 
					this.name(), key));
			}
			return map.getProperty(key);
		}

		/**
		 * key in {@value propertyFilePath} file that corresponds with
		 * this {@code Properties.Entries}.
		 */
		private final String key;
		
		/**
		 * Create a property entry with {@code key}.
		 *
		 * @param key	key in {@value propertyFilePath} that correspons with
		 *		this property.
		 */
		private Entries(String key_) {
			key = key_;
		}
		
	}
	/**
	 * path relative to classpath of the properties file.
	 */
	private static final String propertyFilePath = 
			"/common-aida-runtime.properties";
	
	/**
	 * Properties read into a java.util.Properties-map.
	 *
	 * <p>Garbage collector is free to clean this whenever needed.</p>
	 */
	private static transient java.util.Properties propertyMap = null;

	/**
	 * Retrieve the {@link java.util.Properties} backing this {@code 
	 * org.vle.aid.common.Properties}.
	 *
	 * @return an {@link java.util.Properties}-map with the properties from 
	 * {@value propertyFilePath} loaded
	 *
	 * @throws	IOException when {@code java.util.Properties} does.
	 */
	private static java.util.Properties getPropertyMap() throws IOException {
		if(propertyMap == null) {
			propertyMap = new java.util.Properties();
			propertyMap.load(getClassLoader().getResourceAsStream(propertyFilePath));
		}
		return propertyMap;
	}

	/**
	 * @return the {@link ClassLoader} to use for reading {@value 
	 * propertyFilePath}.
	 */
	private static ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	} 

}
/* vim: set shiftwidth=4 tabstop=4 fo=cqtwan ai : */
