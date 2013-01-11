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

package com.aliasi.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>WellFormedFilter</code> performs basic checks that a
 * document is well-formed XML and throws an exception as soon as an
 * event is detected that would lead to an ill-formed document.
 * Unless exceptions are thrown, events are otherwise passed to the
 * contained handler.  Checks performed include:
 * <ul>
 *   <li>there is a start document event first and end document event last
 *   <li>there is a single top-level element
 *   <li>every element that is begun is closed by a matching tag
 *   <li>text content and attribute values are well-formed unicode 
 *       character sequences
 *   <li>the target and data of a processing instruction are well-formed 
 *       unicode character sequences
 * </ul>
 *
 * <P>The <code>WellFormedFilter</code> is stateful.  To use it
 * again for a new document, call the method {@link #reset()}.
 *
 * <P><b>Warning:</b> This filter only checks well-formedness,
 * not validity with respect to a document type definition (DTD).
 * 
 * <P><b>Warning:</b> Not every way in which a document can be
 * ill-formed is checked by this filter.  Events ignored include:
 * <ul>
 *  <li>Namespaces: prefix mappings are ignored and only qualified
 * names are considered in tests.
 *  <li>Entities: Declarations and resolutions are ignored.
 *  <li>Locator: Document locators are ignored.
 *  <li>Processing Instruction: Ignored.
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 2.4
 * @since   LingPipe1.0
 */
public class WellFormedFilter extends ElementStackFilter {

    private boolean mBegun;
    private boolean mEnded;
    private boolean mFoundTopLevel;

    /**
     * Construct a well-formedness filter and sets the contained
     * handler to the specified value. This handler will be passed all
     * events received by the well-formedness filter that do not throw
     * exceptions due to not being well-formed.
     *
     * @param handler Handler to which filtered events are passed.
     */
    public WellFormedFilter(DefaultHandler handler) {
        super(handler);
        reset();
    }

    /**
     * Construct a well-formedness filter with no specified handler.
     * Until a handler is set with {@link #setHandler(DefaultHandler)},
     * well-formedness checks will be carried out and throw exceptions,
     * but the events will otherwise be ignored.
     */
    public WellFormedFilter() {
        super();
        reset();
    }

    
    /**
     * Resets the state of the filter to accept a new document.  Use
     * this method in between runs to re-use this filter.
     */
    public final void reset() {
        mBegun = false;
        mEnded = false;
        mFoundTopLevel = false;
    }


    /**
     * Tests that the specified target and data are well-formed unicode
     * sequences and delegates to the contained handler.  Throws an
     * exception, if not well formed.
     *
     * @param target Target of the instruction.
     * @param data Value of the instruction, or <code>null</code>.
     * @throws SAXException If this call leads to an ill-formed document.
     */
    public void processingInstruction(String target, String data) throws SAXException {
        testCharacters(target);
        testCharacters(data);
        super.processingInstruction(target,data);
    }

    /**
     * Tests that the document has not already been started, and
     * delegates the call to the contained handler.  If the document
     * was previously started, throws an exception.
     * 
     * @throws SAXException If this call leads to an ill-formed document.
     */
    public void startDocument() throws SAXException {
        if (mBegun) {
            String msg = "Document may only begin once.";
            throw new SAXException(msg);
        }
        mBegun = true;
        super.startDocument();
    }

    /**
     * Tests that the document has been opened and contains a single top-level
     * element that is closed, and delegates the call to the contained handler.
     * Otherwise, throws an exception if the end is premature.
     *
     * @throws SAXException If this call leads to an ill-formed document.
     */
    public void endDocument() throws SAXException {
        testOpen("end document");
        if (!noElement()) {
            String msg = "Attempt to end document without closing element="
                + currentElement();
            throw new SAXException(msg);
        }
        if (!mFoundTopLevel) {
            String msg = "Attempt to end document with no top-level element.";
            throw new SAXException(msg);
        }
        mEnded = true;
        super.endDocument();
    }

    /**
     * Tests that the start element has a well-formed qualified name,
     * and that all attribute qualified names and values are
     * well-formed, then delegates to the contained handler.  Throws
     * an exception if the document has not been started and not ended,
     * or if this element would start a second top-level element.
     *
     * @param namespaceURI The URI of the namespace for this element.
     * @param localName The local name (without prefix) for this
     * element.
     * @param qName The qualified name (with prefix, if any) for this
     * element.
     * @param atts The attributes for this element.
     * @throws SAXException If this call leads to an ill-formed document.
     */
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
        throws SAXException {
     
        testOpen("start element");
        if (noElement()) {
            if (mFoundTopLevel) {
                String msg = "Attempt to start second top-level element="
                    + qName;
                throw new SAXException(msg);
            }
            mFoundTopLevel = true;
        }
        testCharacters(qName);
        for (int i = atts.getLength(); --i >= 0; ) {
            testCharacters(atts.getQName(i));
            testCharacters(atts.getValue(i));
        
        }
        super.startElement(namespaceURI,localName,qName,atts);
    }    

    /**
     * Tests that this end element has a matching start element, and
     * delegates the call to the contained handler.  If there is no
     * matching start element, an exception is raised.
     * 
     * @param namespaceURI The URI of the namespace for this element.
     * @param localName The local name (without prefix) for this
     * element.
     * @param qName The qualified name (with prefix, if any) for this
     * element.
     * @throws SAXException If this call leads to an ill-formed document.
     */
    public void endElement(String namespaceURI,
                           String localName,
                           String qName)
        throws SAXException {
     
        testOpen("end element");
        if (noElement()) {
            String msg = "Attempt to close element with no open; element=" 
                + qName;
            throw new SAXException(msg);
        }
        if (!qName.equals(currentElement())) {
            String msg = " Attempt to close element=" + qName
                + " does not match open element=" + currentElement();
            throw new SAXException(msg);
        }
        super.endElement(namespaceURI,localName,qName);
    }    

    /**
     * Tests that the specified slice of characters is a well-formed
     * sequence of unicode characters, and delegates the call to the
     * contained handler.
     *
     * @param cs Character array from which to draw characters.
     * @param start Index of first character to print.
     * @param length Number of characters to print.
     * @throws SAXException If this call leads to an ill-formed document.
     */
    public void characters(char[] cs, int start, int length) throws SAXException {
        testOpen("characters");
        if (noElement()) {
            String msg = "Characters without containing element.";
            throw new SAXException(msg);
        }
        testCharacters(cs,start,length);
        super.characters(cs,start,length);
    }


    private void testCharacters(String s) throws SAXException {
        testCharacters(s.toCharArray(),0,s.length());
    }

    private void testCharacters(char[] cs, int start, int length)
        throws SAXException {

        for (int i = 0; i < length; ++i) {
            char c = cs[start+i];
            if (c == 0xFFFE) {
                String msg = "Illegal byte-order character=U+FFFE";
                throw new SAXException(msg);
            }
            if (c == 0xFFFF) {
                String msg = "Illegal sentinel character=U+FFFF";
                throw new SAXException(msg);
            }
            if (isLowSurrogate(c)) {
                ++i;
                if (i == length) {
                    String msg = "Characters ended on low surrogate=U+"
                        + Integer.toHexString(c);
                    throw new SAXException(msg);
                } 
                char c2 = cs[start+i];
                if (!isHighSurrogate(c2)) {
                    String msg = "Low surrogate character=U+"
                        + Integer.toHexString(c)
                        + " not matched by high surrogate; following character=U+"
                        + Integer.toHexString(c2);
                    throw new SAXException(msg);
                }
            }
            if (isHighSurrogate(c)) {
                String msg;
                if (i == 0) 
                    msg = "Illegal high surrogate at start of string, found character=U+"
                        + Integer.toHexString(c);
                else
                    msg = "Illegal high surrogate character=U+"
                        + Integer.toHexString(c)
                        + " following non low surrogate character=U+"
                        + Integer.toHexString(cs[start+i-1]);
                throw new SAXException(msg);
            }
        }
    }

    private boolean isLowSurrogate(char c) {
        return c >= 0xD800 && c <= 0xDBFF;
    }

    private boolean isHighSurrogate(char c) {
        return c >= 0xDC00 && c <= 0xDFF;
    }

    private void testOpen(String contextMsg) throws SAXException {
        if (!mBegun) {
            String msg = "Attempt to " + contextMsg + " before starting document.";
            throw new SAXException(msg);
        }
        if (mEnded) {
            String msg = "Attempt to " + contextMsg + " after document ended.";
            throw new SAXException(msg);
        }

    }
     
}
