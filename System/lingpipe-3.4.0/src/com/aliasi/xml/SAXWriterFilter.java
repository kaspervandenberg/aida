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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>SAXWriterFilter</code> handles SAX events and writes a
 * character-based representation to a specified output stream in the
 * specified character encoding and also passes them to a contained
 * handler.  The methods provided are exactly the same as those for
 * a <code>SAXWriter</code>.
 *
 * <P>All events are passed to the contained handler, and this
 * contained handler may be specified at construction time or through
 * the filter method {@link #setHandler(DefaultHandler)}.
 *
 * <P>Note that a DTD specified by {@link #setDTDString(String)} is
 * not passed to the contained handler, but is only written to the
 * output stream.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class SAXWriterFilter extends SAXFilterHandler {

    /**
     * Encoder for the current charset.  Used to test if
     * a character is encodable or needs to be escaped.
     */
    private CharsetEncoder mCharsetEncoder;

    /**
     * Printer to which characters are written.
     */
    private PrintWriter mPrinter;

    /**
     * Buffered writer to which printer writers characters.
     */
    private BufferedWriter mBufWriter;

    /**
     * Output stream writer to which buffered writer writers
     * characters for conversion to bytes.  Wrapping by buffer as per
     * recommendation in {@link java.io.OutputStreamWriter} class
     * documentation.
     */
    private OutputStreamWriter mWriter;

    /**
     * Character set in which characters are encoded.
     */
    private String mCharsetName;

    /**
     * The string to write for a DTD declaration in the XML file, or
     * <code>null</code> if none.
     */
    private String mDtdString = null;

    /**
     * Set to true if an element has been started, but
     * not yet closed with a final right angle bracket.
     */
    private boolean mStartedElement;

    private final HashMap mPrefixMap = new HashMap();

    /**
     * Construct a SAX writer that writes to the specified output
     * stream using the specified character set.  See {@link
     * #setOutputStream(OutputStream,String)} for details on the
     * management of the output stream and character set.
     *
     * @param out Output stream to which bytes are written.
     * @param charsetName Name of character encoding used to write output.
     * @param handler Contained handler for this filter.
     * @throws UnsupportedEncodingException If the character set is not supported.
     */
    public SAXWriterFilter(OutputStream out, String charsetName, 
                           DefaultHandler handler)
        throws UnsupportedEncodingException {
        super(handler);
        setOutputStream(out,charsetName);
    }

    /**
     * Construct a SAX writer that does not have an output stream,
     * character set, or contained handler specified.  These must be
     * set through {@link #setOutputStream(OutputStream,String)} or an
     * illegal state exception will be thrown.  The contained handler
     * will default to a no-op handler, but may be set by {@link
     * #setHandler(DefaultHandler)}.
     */
    public SAXWriterFilter() { 
        /* do nothing */
    }

    /**
     * Sets the DTD to be written by this writer to the specified
     * value.  There is no error checking on its well-formedness, and
     * it is not wrapped in any way other than being printed on its
     * own line; this allows arbitrary DTDs to be written.
     *
     * @param dtdString String to write after the XML declaration as
     * the DTD declaration.
     */
    public void setDTDString(String dtdString) {
        mDtdString = dtdString;
    }


    /**
     * Sets the output stream to which the XML is written, and the
     * character set which is used to encode characters.  Before
     * writing a document, the output stream and character set must be
     * set by the constructor or by this method.  The output stream is
     * not closed after an XML document is written, but all output to
     * the stream will be produced and does not need to be otherwise
     * flushed.
     *
     * @param out Output stream to which encoded characters are written.
     * @param charsetName Character set to use for encoding characters.
     * @throws UnsupportedEncodingException If the character set is
     * not supported by the Java runtime.
     */
    public final void setOutputStream(OutputStream out, String charsetName)
        throws UnsupportedEncodingException {

        Charset charset = Charset.forName(charsetName);
        mCharsetEncoder = charset.newEncoder();
        mWriter = new OutputStreamWriter(out,mCharsetEncoder);
        mBufWriter = new BufferedWriter(mWriter);
        mPrinter = new PrintWriter(mBufWriter); // no auto-flush
        mCharsetName = charsetName;
    }


    // ContentHandler

    /**
     * Prints the XML declaration, and DTD declaration if any.
     */
    public void startDocument() throws SAXException {
        super.startDocument();
        printXMLDeclaration();
        mStartedElement = false;
    }

    /**
     * Handles the declaration of a namespace mapping from a specified
     * URI to its identifying prefix.  The mapping is buffered and
     * then flushed and printed as an attribute during the next
     * start-element call.
     *
     * @param prefix The namespace prefix being declared..
     * @param uri The namespace URI mapped to prefix.
     * @throws SAXException If the contained handler throws an
     * exception on the specified prefix mapping.
     */
    public void startPrefixMapping(String prefix, String uri) 
        throws SAXException {

        mPrefixMap.put(prefix,uri);
        super.startPrefixMapping(prefix,uri);
    }


    /**
     * Handles the declaration of a namespace mapping from a specified
     * URI to its identifying prefix.  The mapping is buffered and
     * then flushed and printed as an attribute during the next
     * start-element call.
     *
     * @param prefix The namespace prefix being declared..
     * @throws SAXException If the contained handler throws an exception
     * on the specified prefix.
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        super.endPrefixMapping(prefix);
    }


    /**
     * Flushes the underlying character writers output to the
     * output stream, trapping all exceptions.
     */
    public void endDocument() throws SAXException {
        super.endDocument();
        if (mStartedElement) {
            mPrinter.print("/>");
            mStartedElement = false;
        }
        mPrinter.flush();
        try { 
            mBufWriter.flush(); 
        } catch (IOException e) { 
            // do nothing
        }
        try {
            mWriter.flush(); 
        } catch (IOException e) { 
            // do nothing
        }
    }

    /**
     * Prints the start element, using the qualified name, and sorting
     * the attributes using the underlying string ordering.  Namespace
     * URI and local names are ignored, and qualified name must not be
     * <code>null</code>.
     *
     * @param namespaceURI The URI of the namespace for this element.
     * @param localName The local name (without prefix) for this
     * element.
     * @param qName The qualified name (with prefix, if any) for this
     * element.
     * @param atts The attributes for this element.
     */
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
        throws SAXException {

        super.startElement(namespaceURI,localName,qName,atts);
        if (mStartedElement) {
            mPrinter.print(">");
            mStartedElement=false;
        }
        mPrinter.print('<');
        mPrinter.print(qName);
        printAttributes(atts);
        mStartedElement = true;
        // close is picked up later in implicit continuation
        // mPrinter.print('>');
    }

    /**
     * Prints the end element, using the qualified name.  Namespace
     * URI and local name parameters are ignored, and the qualified
     * name must not be <code>null</code>
     *
     * @param namespaceURI The URI of the namespace for this element.
     * @param localName The local name (without prefix) for this
     * element.
     * @param qName The qualified name (with prefix, if any) for this
     * element.
     */
    public void endElement(String namespaceURI, String localName,
                           String qName)
        throws SAXException{

        super.endElement(namespaceURI,localName,qName);
        if (mStartedElement) {
            mPrinter.print("/>");
            mStartedElement = false;
            return;
        }
        mPrinter.print('<');
        mPrinter.print('/');
        mPrinter.print(qName);
        mPrinter.print('>');
    }

    /**
     * Prints the characters in the specified range.
     *
     * @param ch Character array from which to draw characters.
     * @param start Index of first character to print.
     * @param length Number of characters to print.
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {
        super.characters(ch,start,length);
        if (mStartedElement) {
            mPrinter.print('>');
            mStartedElement = false;
        }
        printCharacters(ch,start,length);
    }

    /**
     * Does not print ignorable whitespace, nor does it send the
     * event to the contained handler.
     *
     * @param ch Character array from which to draw characters.
     * @param start Index of first character to print.
     * @param length Number of characters to print.
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException {
        // super.ignorableWhitespace(ch,start,length);
    }

    /**
     * Print a representation of the proecssing instruction.  This
     * will be <code>&langle;?<i>Target</i>&rangle;</code> if there is
     * no data, or * <code>&lt;?<i>Target</i>
     * <i>Data</i>&gt;</code> if there is data.
     *
     * @param target Target of the instruction.
     * @param data Value of the instruction, or <code>null</code>.
     */
    public void processingInstruction(String target, String data)
        throws SAXException {

        super.processingInstruction(target,data);
        if (mStartedElement) {
            mPrinter.print('>');
            mStartedElement = false;
        }
        mPrinter.print("<?");
        mPrinter.print(target);
        if (data != null && data.length() > 0) {
            mPrinter.print(' ');
            mPrinter.print(data);
        }
        mPrinter.print("?>");
    }

    // prints atts and outstanding namespace decls
    private void printAttributes(Attributes atts) {
        if (mPrefixMap.size() > 0) {
            Iterator it = mPrefixMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                printAttribute("xmlns:" + key,value);
            }
            mPrefixMap.clear();
        }
        TreeSet orderedAtts = new TreeSet();
        for (int i = 0; i < atts.getLength(); ++i)
            orderedAtts.add(atts.getQName(i));
        Iterator attsIterator = orderedAtts.iterator();
        while (attsIterator.hasNext()) {
            String attQName = attsIterator.next().toString();
            printAttribute(attQName,atts.getValue(attQName));
        }
    }

    /**
     * Prints an attribute and value, with value properly
     * escaped. Prints leading space before attribute and value pair.
     *
     * @param att Attribute name.
     * @param val Attribute value.
     */
    private void printAttribute(String att, String val) {
        mPrinter.print(' ');
        mPrinter.print(att);
        mPrinter.print('=');
        mPrinter.print('"');
        printCharacters(val);
        mPrinter.print('"');
    }

    /**
     * Print the specified string, with appropriate escapes.
     *
     * @param s Print the characters in the specified string.
     */
    private void printCharacters(String s) {
        printCharacters(s.toCharArray(),0,s.length());
    }

    /**
     * Print the specified range of characters, with escapes.
     *
     * @param ch Array of characters from which to draw.
     * @param start Index of first character to print.
     * @param length Number of characters to print.
     */
    private void printCharacters(char[] ch, int start, int length) {
        for (int i = start; i < start+length; ++i)
            printCharacter(ch[i]);
    }

    /**
     * Print the specified character, rendering it as an entity if
     * necessary (see class doc).
     *
     * @param c Character to print.
     */
    private void printCharacter(char c) {
        // note: does not catch illegal conjugate pairs
        if (!mCharsetEncoder.canEncode(c)) {
            printEntity("#x" + Integer.toHexString((int)c));
            return;
        }
        switch (c) {
        case '<':  { printEntity("lt"); break; }
        case '>':  { printEntity("gt"); break; }
        case '&':  { printEntity("amp"); break; }
        case '"':  { printEntity("quot"); break; }
        default:   { mPrinter.print(c); }
        }
    }

    /**
     * Print the specified entity.
     *
     * @param entity Name of entity to print.
     */
    private void printEntity(String entity) {
        mPrinter.print('&');
        mPrinter.print(entity);
        mPrinter.print(';');
    }

    /**
     * Prints the XML declaration, including the character set
     * declaration and the DTD if any is defined.  The declaration
     * printed is:
     * <blockquote>
     *   <code>&lt;?xml version="1.0" encoding="<i>CharSet</i>"?&gt;</code>.
     * </blockquote>
     * where <code><i>CharSet</i></code> is the string representation
     * of the character set being used.  No spaces are included after
     *  the XML declaration or the DTD declaration, if any.
     */
    private void printXMLDeclaration() {
        mPrinter.print("<?xml");
        printAttribute("version","1.0");
        printAttribute("encoding",mCharsetName);
        mPrinter.print("?>");
        if (mDtdString != null) {
            mPrinter.print(mDtdString);
        }
    }

}
