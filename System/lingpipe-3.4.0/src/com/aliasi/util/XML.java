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

package com.aliasi.util;

import com.aliasi.io.FileExtensionFilter;

import java.io.CharArrayReader;
import java.io.FileFilter;
import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Static utility methods and classes for processing XML.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe1.0
 */
public class XML {

    /**
     * Forbid instance construction
     */
    private XML() { 
        /* no instances */
    }

    /**
     * Returns an XML-escaped version of the specified string.
     *
     * @param in String to escape.
     * @return Escaped version of the specified string.
     */
    public static String escape(String in) {
        StringBuffer sb = new StringBuffer();
        escape(in,sb);
        return sb.toString();
    }

    /**
     * Write an XML-escaped version of the specified string
     * to the specified string buffer.
     *
     * @param in String to escape.
     * @param sb String buffer to whcih to write escaped version of
     * string.
     */
    public static void escape(String in, StringBuffer sb) {
        for (int i = 0; i < in.length(); ++i) {
            escape(in.charAt(i),sb);
        }
    }

    /**
     * Write an XML-escaped version of the specified character
     * to the specified string buffer.
     *
     * @param c Character to write.
     * @param sb String buffer to which to write escaped version of
     * character.
     */
    public static void escape(char c, StringBuffer sb) {
        switch (c) {
        case '<':  escapeEntity("lt",sb); return;
        case '>':  escapeEntity("gt",sb); return;
        case '&':  escapeEntity("amp",sb); return;
        case '"':  escapeEntity("quot",sb); return;
        default:   sb.append(c);
        }
    }

    /**
     * Write an XML-escaped version of the entity specified by name to
     * the specified string buffer.
     *
     * @param xmlEntity Name of entity with no markup.
     * @param sb String Buffer to which escaped form of entity is
     * written.
     */
    public static void escapeEntity(String xmlEntity, StringBuffer sb) {
        sb.append('&');
        sb.append(xmlEntity);
        sb.append(';');
    }

    /**
     * Handle the document specified as a string with the specified
     * handler.
     *
     * @param document String representation of XML document.
     * @param handler Handler for SAX events from document.
     * @throws SAXException If there is a SAX exception handling the
     * document.
     * @throws IOException If there is an I/O exception reading from
     * the document.
     */
    public static void handle(String document,
                              DefaultHandler handler)
        throws IOException, SAXException {

        CharArrayReader reader
            = new CharArrayReader(document.toCharArray());
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setContentHandler(handler);
        InputSource in = new InputSource(reader);
        xmlReader.parse(in);
    }

    /**
     * Suffix for XML files.
     */
    public static final String XML_SUFFIX = "xml";

    /**
     * Filter for XML files and directories.
     */
    public static final FileFilter XML_FILE_FILTER
        = new FileExtensionFilter(XML_SUFFIX,true);

    /**
     * Filter for XML files only.
     */
    public static final FileFilter XML_FILE_ONLY_FILTER
        = new FileExtensionFilter(XML_SUFFIX,false);

    /**
     * The feature used to set an <code>XMLReader</code> to be
     * validating or not.  To turn on validation, which is off
     * by default, use:

     * <blockquote><code>
     *   xmlReader.setFeature(XML.VALIDATION_FEATURE,true);
     * </code></blockquote>
     *
     * <P>See <a href="http://www.saxproject.org/?selected=get-set">SAX Project Features and Properties</a>.
     */
    public static final String VALIDATION_FEATURE
        = "http://xml.org/sax/features/validation";


    /**
     * The feature used to set an <code>XMLReader</code> to
     * handle namespaces.  This is an expensive feature and
     * turning it off can double parsing speed.  To turn
     * off namespace parsing, which is on by default, use:
     *
     * <blockquote><code>
     *   xmlReader.setFeature(XML.NAMESPACES_FEATURE,true)
     * </code></blockquote>
     *
     * <P>See <a href="http://www.saxproject.org/?selected=get-set">SAX Project Features and Properties</a>.
     */
    public static final String NAMESPACES_FEATURE
    = "http://xml.org/sax/features/namespaces";

    /**
     * The feature used to set the buffer size for the Xerces
     * parser in bytes.  Use
     *
     * <blockquote><code>
     *   XMLReader reader;
     *   <br>
     *   reader.setProperty(XML.XERCES_BUFFER_SIZE_PROPERTY,2048);
     * </code></blockquote>
     *
     * <P>

     * <P>See <a href="http://www.saxproject.org/?selected=get-set">SAX Project Features and Properties</a>.
     * <P> Also see: <a href="http://xml.apache.org/xerces2-j/properties.html">Xerces 2 Properties</a>.
     */
    public static final String XERCES_BUFFER_SIZE_PROPERTY
    = "http://apache.org/xml/properties/input-buffer-size";
    

}
