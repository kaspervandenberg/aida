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

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>DelegatingHandler</code> is a SAX filter that routes events
 * to embedded handlers based on their element embedding.  Although
 * delegating handler can be embedded inside one another for recursive
 * embedding, it is more efficient to use instances of {@link
 * DelegateHandler} below the top level.  This allows what would
 * otherwise be recursion on the execution stack to be represented
 * internally to this class.  Directly embedding delegating handlers
 * may lead to stack overflow.
 * 
 * <P>Together with delegate handlers, delegating handlers implement
 * some of the same functionality as the <a
 * href="jakarta.apache.org/commons/digester/">Jakarta Commons
 * Digester</a>.  The difference is that there are no XPath
 * specifications in LingPipe's delegating handler and all delegation
 * is handled programatically in one step, not by pattern matching as
 * in XPath.  The result is that LingPipe's handlers will be much more
 * efficient than the Digester's in situations where their
 * functionality is adequate.
 *
 * <P>An extensive example of the use of delegating and delegate
 * handlers can be found in the {@link com.aliasi.medline} package.
 * Each object has its own delegate or simple handler and an entire
 * citation is built by simply accumulating lower-level structured
 * results.
 * 
 * @author Bob Carpenter
 * @version 2.2.2
 * @since   LingPipe2.0
 */
public class DelegatingHandler extends SimpleElementHandler {

    final HashMap mDelegateMap = new HashMap();
    final String[] mQNameStack;
    final DefaultHandler[] mDelegateStack;
    int mStackTop = -1;

    /**
     * Construct a delegating handler with up to the specified maximum
     * delegation depth.  This should not exceed the stack size of the
     * JVM.  Attempts to exceed the maximum depth will throw an array
     * out of bounds runtime exception.
     *
     * @param maxDelegationDepth Maximum delegation depth.
     */
    public DelegatingHandler(int maxDelegationDepth) {
        mDelegateStack = new DefaultHandler[maxDelegationDepth];
        mQNameStack = new String[maxDelegationDepth];
    }

    /**
     * Construct a delegating handler with delegation depth 512.
     */
    public DelegatingHandler() {
        this(512);
    }


    /**
     * Set the specified handler to handle events contains in the the
     * specified element.  If the handler is an instance of
     * <code>DelegateHandler</code>, it should be constructed to
     * contain this delegating handler to ensure cooperation on
     * delegation.  Setting the delegate handler to <code>null</code>
     * will remove any existing delegation for the specified element.
     *
     * <P>Note that the element is specified by means of its qualified
     * name, not its base name or URL plus base name.
     *
     * @param qName Qualified name of element.
     * @param handler Handler to accept delegated events.
     * @throws IllegalArgumentException If the handler is a delegate
     * handler that is tied to a different delegating handler.
     */
    public void setDelegate(String qName, DefaultHandler handler) {
        if (handler instanceof DelegateHandler
            && (this != ((DelegateHandler)handler).mDelegatingHandler)) {
            String msg = "Delegate handlers must wrap this delegating handler.";
            throw new IllegalArgumentException(msg);
        }
        mDelegateMap.put(qName,handler);
    }

    /**
     * This method will be called whenever the close tag for the
     * specified element is found.  The handler provided is the
     * handler that was delegated to handle the specified element.
     * This method is useful for incorporating data extracted by a
     * delegate into a larger data structure.
     *
     * <P>This is a default no-operation implementation.  Subclasses
     * may override it to handle the end of delegates.
     *
     * @param qName Qualified name of element that finished.
     * @param handler Handler that was delegated to handle the
     * specified element.
     */
    public void finishDelegate(String qName,
                               DefaultHandler handler) {
        /* do nothing by default */
    }


    /**
     * Returns the delegate for the specified qualified element name.
     * If a delegate has not been declared for the specified element,
     * the default delegate is returned, which may be <code>null</code>.
     *
     * @param qName Qualified name of delegated element.
     * @return The delegate for the specified element.
     */
    public DefaultHandler getDelegate(String qName) {
        return (DefaultHandler) mDelegateMap.get(qName);
    }

    /**
     * Sets this handler to start a new document by resetting
     * any internal state.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void startDocument() throws SAXException {
        mStackTop = -1;
    }

    /**
     * Call delegated.
     *
     * @param namespaceURI The URI identifying the name space, or
     * <code>null</code> if there isn't one.
     * @param localName Local name of element.
     * @param qName Qualified name of element, which is prefixed with
     * the name space URI and a colon if it is non-null, and is equal
     * to local name if there is no name space specified.
     * @param atts Attributes for this element.
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
        throws SAXException {

        // only top-level delegation; if delegate, send it start element
        if (mStackTop >= 0) {
            // System.out.println("    Delegating to " + mDelegateStack[mStackTop]);
            mDelegateStack[mStackTop].startElement(namespaceURI,localName,
                                                   qName,atts);
            return;
        }
        DefaultHandler handler = getDelegate(qName);
        if (handler == null) return;
        handler.startDocument();
        handler.startElement(namespaceURI,localName,qName,atts);
        mStackTop = 0;
        mDelegateStack[0] = handler;
        mQNameStack[0] = qName;
    }

    /**
     * Call is delegated to the delegate and may trigger a finish
     * delegate callback.
     *
     * @param namespaceURI The URI identifying the name space, or
     * <code>null</code> if there isn't one.
     * @param localName Local name of element.
     * @param qName Qualified name of element, which is prefixed with
     * the name space URI and a colon if it is non-null, and is equal
     * to local name if there is no name space specified.
     * @throws SAXException if the contained handler throws a SAX
     * exception.
     */
    public void endElement(String namespaceURI, String localName,
                           String qName)
        throws SAXException {

        if (mStackTop < 0) return;
        DefaultHandler handler = mDelegateStack[mStackTop];
        handler.endElement(namespaceURI,localName,qName);
        if (!qName.equals(mQNameStack[mStackTop])) return;
        --mStackTop;
        if (mStackTop < 0) {
            finishDelegate(qName,handler);
        } else {
            DelegateHandler delHandler
                = (DelegateHandler) mDelegateStack[mStackTop];
            delHandler.finishDelegate(qName,handler);
        }
    }

    /**
     * Call delegated or ignored.
     *
     * @param ch Character array containing characters to handle.
     * @param start Index of first character to handle.
     * @param length Number of characters to handle.
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {

        if (mStackTop >= 0)
            mDelegateStack[mStackTop].characters(ch,start,length);
    }

    /**
     * Call ignored.
     *
     * @param ch Character array containing characters to handle.
     * @param start Index of first character to handle.
     * @param length Number of characters to handle.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException {

        // mHandler.ignorableWhitespace(ch,start,length);
    }

    /**
     * Call delegated.
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or
     * <code>null</code> if none is supplied.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void processingInstruction(String target, String data)
        throws SAXException {

        if (mStackTop >= 0)
            mDelegateStack[mStackTop].processingInstruction(target,data);
    }

    /**
     * Call delegated.
     *
     * @param prefix The namespace prefix being declared.
     * @param uri The namespace URI mapped to the prefix.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {

        if (mStackTop >= 0)
            mDelegateStack[mStackTop].startPrefixMapping(prefix,uri);
    }

    /**
     * Call delegated.
     *
     * @param prefix The namespace prefix being declared.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        if (mStackTop >= 0)
            mDelegateStack[mStackTop].endPrefixMapping(prefix);
    }

    /**
     * Call delegated.
     *
     * @param name The name of the skipped entity.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void skippedEntity(String name) throws SAXException {
        if (mStackTop >= 0)
            mDelegateStack[mStackTop].skippedEntity(name);
    }

    /**
     * Call delegated.
     *
     * @param locator A locator for all SAX document events.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void setDocumentLocator(Locator locator) {
        if (mStackTop >= 0)
            mDelegateStack[mStackTop].setDocumentLocator(locator);
    }

    /**
     * Call delegated.
     *
     * @param publicId The public identifier, or <code>null</code> if
     * none is available.
     * @param systemId The system identifier provided in the XML
     * document.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException {

        try {
            return mStackTop < 0
                ? super.resolveEntity(publicId,systemId)
                : mDelegateStack[mStackTop].resolveEntity(publicId,systemId);
        } catch (Throwable t) {
            SAXFilterHandler.io2SAXException(t);
            return null; // unreachable semantically given io2SAXException
        }
    }


    // ErrorHandler

    /**
     * Call delegated.
     *
     * @param exception The error information, encoded as an exception.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void error(SAXParseException exception) throws SAXException {
        if (mStackTop >= 0)
            mDelegateStack[mStackTop].error(exception);
    }

    /**
     * Call delegated.
     *
     * @param exception The fatal error information, encoded as an
     * exception.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void fatalError(SAXParseException exception)
        throws SAXException {

        if (mStackTop >= 0)
            mDelegateStack[mStackTop].fatalError(exception);
    }

    /**
     * Call delegated.
     *
     * @param exception The warning information, encoded as an exception.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void warning(SAXParseException exception) throws SAXException {
        if (mStackTop >= 0)
            mDelegateStack[mStackTop].warning(exception);
    }


    // DTD Handler

    /**
     * Call delegated.
     *
     * @param name The notation name.
     * @param publicId The notation public identifier, or
     * <code>null</code> if none is available.
     * @param systemId The notation system identifier.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void notationDecl(String name, String publicId,
                             String systemId)
        throws SAXException {

        if (mStackTop >= 0)
            mDelegateStack[mStackTop].notationDecl(name,publicId,systemId);
    }

    /**
     * Call delegated.
     *
     * @param name The entity name.
     * @param publicId The entity public identifier, or
     * <code>null</code> if none is available.
     * @param systemId The entity system identifier.
     * @param notationName The name of the associated notation.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void unparsedEntityDecl(String name, String publicId,
                                   String systemId, String notationName)
        throws SAXException {

        if (mStackTop >= 0)
            mDelegateStack[mStackTop]
                .unparsedEntityDecl(name,publicId,systemId,notationName);
    }

}
