/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vle.aid.lucene;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.vle.aid.ResultType;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Utility to convert SearcherWS results to XML
 * 
 * @author Kasper van den Berg <kasper@kaspervandenberg.net>
 */
public class XMLUtil {
	private static XMLUtil instance = null;
	private DocumentBuilder docBuilder = createXmlDocBuilder(false);

	private XMLUtil() {
	}

	public static XMLUtil getInstance() {
		if (instance == null) {
			instance = new XMLUtil();
		}
		return instance;
	}

	public Node toXmlNode(final String xmlContents) {
		InputStream resultStream = new ByteArrayInputStream(xmlContents.getBytes(Charset.defaultCharset()));
		Node result;
		try {
			result = docBuilder.parse(resultStream);
			return result;
		} catch (SAXException | IOException ex) {
			throw new Error("Error parsing XML file", ex);
		}
	}

	public Node toXmlNode(final ResultType xmlContents) {
		return xmlContents.getDomNode();
	}

	public static DocumentBuilder createXmlDocBuilder(final boolean validating) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(validating);
		factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(SAXExceptionHandler.getInstance());
			return builder;
		} catch (ParserConfigurationException ex) {
			throw new Error("error creating XML parser", ex);
		}
	}
	
}
