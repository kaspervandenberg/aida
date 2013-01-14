package org.vle.aid.common;

import java.io.IOException;
import java.util.NoSuchElementException;

public class Properties {
	private static final String propertyFilePath = 
			"/common-aida-runtime.properties";
	
	private static java.util.Properties propertyMap = null;

	private static java.util.Properties getPropertyMap() throws IOException {
		if(propertyMap == null) {
			propertyMap = new java.util.Properties();
			propertyMap.load(getClassLoader().getResourceAsStream(propertyFilePath));
		}
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private static ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	} 

	public enum Entries {
		AXIS_ENDPOINT("org.vle.aid.axis_endpoint");
		
		private final String key;
		
		private Entries(String key_) {
			key = key_;
		}
		
		String get() throws IOException {
			java.util.Properties map = getPropertyMap();
			if(map == null) {
				throw new Error(new NullPointerException(
						String.format("While looking up property %s; propertyMap is null.", this.name())));
			}
			if(!map.containsKey(key)) {
				throw new NoSuchElementException(
						String.format("While looking up property %s; %s not in propertyMap", this.name(), key));
			}
			return map.getProperty(key);
		}
	}
}
/* vim: set shiftwidth=4 tabstop=4 fo=cqtwan : */
