/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vle.aid.lucene;

import org.junit.Assert;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Fail test when receiving a {@link SAXParseException}
 */
public class SAXExceptionHandler implements ErrorHandler {

	private enum Level {

		warning, error, fatalError
	}
	private static final String MSG_FORMAT = "XML not well formed (%s): %s";
	private static SAXExceptionHandler instance = null;

	public static SAXExceptionHandler getInstance() {
		if (instance == null) {
			instance = new SAXExceptionHandler();
		}
		return instance;
	}

	private SAXExceptionHandler() {
	}

	private void failTest(Level l, SAXParseException ex) {
		final String msg = String.format(MSG_FORMAT, l.name(), ex.toString());
		Assert.fail(msg);
	}

	@Override
	public void warning(SAXParseException exception) {
		failTest(Level.warning, exception);
	}

	@Override
	public void error(SAXParseException exception) {
		failTest(Level.error, exception);
	}

	@Override
	public void fatalError(SAXParseException exception) {
		failTest(Level.fatalError, exception);
	}
	
}
