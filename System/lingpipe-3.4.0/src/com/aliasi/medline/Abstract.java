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

package com.aliasi.medline;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import org.xml.sax.SAXException;

/**
 * An <code>Abstract</code> represents the abstract of a MEDLINE
 * document.  Abstracts contain the text of an abstract along with the
 * text of the copyright information.  Not every citation necessarily
 * has an abstract.  The text of the abstract is drawn from the source
 * articles themselves; abstracts are never created by the NLM.  All
 * abstracts are in English, even for articles that are not in
 * English.  
 *
 * <P>Some documents may contain an {@link OtherAbstract} as well as
 * an abstract.
 *
 * <P>The text of an abstract created before 2001  may be truncated.
 * One of the following may appear at the end of the text of a
 * truncated abstract:
 * 
 * <blockquote>
 * <table border='1' cellpadding='5'>
 * <tr><td><i>Truncation Marker</i></td></tr>
 * <tr><td><code>(ABSTRACT TRUNCATED AT 250 WORDS)</code></td></tr>
 * <tr><td><code>(ABSTRACT TRUNCATED AT 400 WORDS)</code></td></tr>
 * <tr><td><code>(ABSTRACT TRUNCATED)</code></td></tr>
 * </table>
 * </blockquote>
 *
 * The message without an explicit word length only shows up on
 * abstracts of more than 4,096 characters from records created
 * between 1996 and 2001.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class Abstract {

    private static final String TRUNCATION_MARKER_250 
        = "(ABSTRACT TRUNCATED AT 250 WORDS)";
    private static final String TRUNCATION_MARKER_400
        = "(ABSTRACT TRUNCATED AT 400 WORDS)";
    private static final String TRUNCATION_MARKER_4096
        = "(ABSTRACT TRUNCATED)";
    private static final String[] TRUNCATION_MARKERS
        = new String[] {
            TRUNCATION_MARKER_250,
            TRUNCATION_MARKER_400,
            TRUNCATION_MARKER_4096 
        };

    private final String mText;
    private final String mCopyrightInformation;

    Abstract(String text, String copyrightInformation) {
        mText = text;
        mCopyrightInformation = copyrightInformation;
    }

    /**
     * Returns the text of this abstract.  See the class documentation
     * for notes on possible trunctation indicators.
     *
     * @return The text of this abstract.
     */
    public String text() {
        return mText;
    }

    /**
     * Returns <code>true</code> if the text of the abstract
     * has been truncated.  This is determined by inspecting
     * the last characters in the abstract as indicated in
     * the class documentation above.
     *
     * @return <code>true</code> if this abstract has been truncated.
     */
    public boolean isTruncated() {
        for (int i = 0; i < TRUNCATION_MARKERS.length; ++i)
            if (text().endsWith(TRUNCATION_MARKERS[i]))
                return true;
        return false;
    }

    /**
     * Returns the text of this abstract with any final truncation
     * markers removed.
     *
     * @return The text of this abstract with any final truncation
     * markers removed.
     */
    public String textWithoutTruncationMarker() {
        return textWithoutTruncationMarker(text());
    }


    /**
     * Returns a trimmed version of the specified text with any
     * final truncation markers removed.
     *
     * @param text Input text.
     * @return Trimmed output with truncation markers stripped.
     */
    public static String textWithoutTruncationMarker(String text) {
        String trimmedText = text.trim();
        for (int i = 0; i < TRUNCATION_MARKERS.length; ++i)
            if (trimmedText.endsWith(TRUNCATION_MARKERS[i]))
                return trimmedText.substring(0,
                                      trimmedText.length()
                                      - TRUNCATION_MARKERS[i].length());
        return trimmedText;
    }

    /**
     * Returns the copyright information for this abstract.  Copyright
     * information is optional; this method returns the empty string
     * if no copyright information was present in the record's
     * abstract.  Copyright information may take any form, but typically
     * includes the publisher name and date (<i>e.g.</i> 
     * <code>Copyright 1999 Academic Press.</code>).
     *
     * @return The copyright information for this abstract.
     */
    public String copyrightInformation() {
        return mCopyrightInformation;
    }

    /**
     * Returns a string-based representation of this abstract.
     *
     * @return A string-based representation of this abstract.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        sb.append("Text=");
        sb.append(text());
        if (copyrightInformation().length() > 0) {
            sb.append(" Copyright Information=");
            sb.append(copyrightInformation());
        }
        sb.append('}');
        return sb.toString();
    }

    // <!ELEMENT Abstract (%Abstract;)>
    // <!ENTITY % Abstract "(AbstractText,CopyrightInformation?)">
    static class Handler extends DelegateHandler {
        private final TextAccumulatorHandler mTextHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mCopyrightInformationHandler
            = new TextAccumulatorHandler();
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            setDelegate(MedlineCitationSet.ABSTRACT_TEXT_ELT,
                        mTextHandler);
            setDelegate(MedlineCitationSet.COPYRIGHT_INFORMATION_ELT,
                        mCopyrightInformationHandler);
        }
        public void reset() {
            mTextHandler.reset();
            mCopyrightInformationHandler.reset();
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public Abstract getAbstract() {
            return new Abstract(mTextHandler.getText(),
                                mCopyrightInformationHandler.getText());
        }
    }

}
