/*
 * LingPipe v. 2.0
 * Copyright (C) 2003-5 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 * 
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://www.alias-i.com/lingpipe/licenseV1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.test.unit.xml;

import com.aliasi.test.unit.MockObjectHelper;

import com.aliasi.xml.SimpleElementHandler;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * The mock handler logs all method calls to a single array of tuples,
 * which is accessible through {@link getCalls()}.  Each call is
 * represented as the tuple consisting of the method name followed by
 * all of the arguments to it.  Basic types must be wrapped as objects
 * before being entered into the handler.
 */
public class MockDefaultHandler extends DefaultHandler {

    /**
     * The list of calls to methods of this object.
     */
    MockObjectHelper mHelper = new MockObjectHelper();

    /**
     * Construct a new mock handler.
     */
    public MockDefaultHandler() {
        super();
    }

    /**
     * Return the array of calls to this object, as described in the
     * class documentation.
     */
    public ArrayList getCalls() {
        return mHelper.getCalls();
    }

    public void startDocument() {
        mHelper.add("startDocument");
    }

    public void endDocument() {
        mHelper.add("endDocument");
    }

    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts) {
        mHelper.add("startElement",namespaceURI,localName,qName,
                    toString(atts));
    }

    public void endElement(String namespaceURI, String localName,
                           String qName) {
        mHelper.add("endElement",namespaceURI,localName,qName);
    }

    public void characters(char[] ch, int start, int length) {
        mHelper.add("characters",new String(ch,start,length));
    }

    public void ignorableWhitespace(char[] ch, int start, int length) {
        mHelper.add("ignorableWhitespace",ch,
                    new Integer(start),new Integer(length));
    }

    public void processingInstruction(String target, String data) {
        mHelper.add("processingInstruction",target,data);
    }

    public void startPrefixMapping(String prefix, String uri) {
        mHelper.add("startPrefixMapping",prefix,uri);
    }

    public void endPrefixMapping(String prefix) {
        mHelper.add("endPrefixMapping",prefix);
    }

    public void skippedEntity(String name) {
        mHelper.add("skippedEntity",name);
    }

    public void setDocumentLocator(Locator locator) {
        mHelper.add("setDocumentLocator",locator);
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        mHelper.add("resolveEntity",publicId,systemId);
        return null;
    }


    public void error(SAXParseException exception) {
        mHelper.add("error",exception);
    }

    public void fatalError(SAXParseException exception) {
        mHelper.add("fatalError",exception);
    }

    public void warning(SAXParseException exception) {
        mHelper.add("warning",exception);
    }

    public void notationDecl(String name, String publicId,
                             String systemId) {
        mHelper.add("notationDecl",name,publicId,systemId);
    }

    public void unparsedEntityDecl(String name, String publicId,
                                   String systemId, String notationName) {
        mHelper.add("unparsedEntityDecl",name,publicId,systemId,notationName);
    }

    public static String toString(Attributes atts) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < atts.getLength(); ++i) {
            if (i > 0) sb.append(';');
            sb.append(atts.getQName(i) + '='
                      + atts.getValue(i));
        }
        return sb.toString();
    }

    public static final String EMPTY_ATTS_STRING
        = toString(SimpleElementHandler.EMPTY_ATTS);

}


