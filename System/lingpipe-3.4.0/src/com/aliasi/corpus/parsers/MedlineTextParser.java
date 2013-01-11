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

package com.aliasi.corpus.parsers;

import com.aliasi.corpus.Handler;
import com.aliasi.corpus.TextHandler;
import com.aliasi.corpus.XMLParser;

import com.aliasi.medline.Abstract;
import com.aliasi.medline.MedlineCitationSet;

import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>MedlineTextParser</code> extracts all text from the
 * abstracts of MEDLINE citations, passing them to the contained text
 * handler.
 *
 * <p>See {@link MedlineCitationSet} for more information on
 * the MEDLINE corpus format.
 *
 * @author  Bob Carpenter
 * @version 2.1
 * @since   LingPipe2.1
 */
public class MedlineTextParser extends XMLParser {

    /**
     * Construct a text parser that extracts abstract text from
     * MEDLINE citations with a <code>null</code> handler.
     * The text handler may be set later using {@link
     * #setHandler(Handler)}.
     */
    public MedlineTextParser() {
        super(); 
    }

    /**
     * Construct a MEDLINE text parser with the specified handler.
     * The handler may be reset later using {@link
     * #setHandler(Handler)}.
     *
     * @param handler Text handler for this parser.
     */
    public MedlineTextParser(TextHandler handler) {
    super(handler);
    }

    /**
     * Returns the text handler for this parser.
     *
     * @return The text handler for this parser.
     * @throws ClassCastException If the handler      * class.
     */
    public TextHandler getTextHandler() {
    return (TextHandler) getHandler();
    }

    /**
     * Sets the text handler to the specified value.
     * 
     * @param handler New text handler for this parser.
     * @throws IllegalArgumentException If the handler does
     * not implement the interface <code>TextHandler</code>.
     */
    public void setHandler(Handler handler) {
    if (!(handler instanceof TextHandler)) {
        String msg = "Handler must implement the TextHandler interface."
        + " Found class=" + handler.getClass();
        throw new IllegalArgumentException(msg);
    }
    super.setHandler(handler);
    }

    /**
     * Returns the XML handler for this text parser.  
     */
    public DefaultHandler getXMLHandler() {
        return new XMLHandler((TextHandler) getHandler());
    }

    private static class XMLHandler extends DelegatingHandler {
        TextHandler mTextHandler;
        AbstractHandler mAbstractHandler = new AbstractHandler();
        XMLHandler(TextHandler textHandler) {
            mTextHandler = textHandler;
            setDelegate(MedlineCitationSet.ABSTRACT_ELT,
                        mAbstractHandler);
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(MedlineCitationSet.ABSTRACT_ELT)) {
                String text = mAbstractHandler.getText();
                String strippedText
                    = Abstract.textWithoutTruncationMarker(text);
                char[] cs = strippedText.toCharArray();
                mTextHandler.handle(cs,0,cs.length);
            }
        }
    }

    private static class AbstractHandler extends DelegatingHandler {
        TextAccumulatorHandler mTextAccumulator 
        = new TextAccumulatorHandler();
        public AbstractHandler() {
            setDelegate(MedlineCitationSet.ABSTRACT_TEXT_ELT,
                        mTextAccumulator);
        }
        public void startDocument() {
            mTextAccumulator.reset();
        }
        String getText() {
            return mTextAccumulator.getText();
        }
    }

}
