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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * An <code>OtherAbstract</code> represents an alternative abstract
 * for an article.  Other abstracts are provided by partners of NLM.
 * The existence of an other abstract does not depend on the existence
 * of an abstract.
 *
 * <P>The possible types for an other abstract are as indicated
 * in the following table:
 * 
 * <blockquote>
 * <table border='1' cellpadding='5'>
 * <tr><td><i>Type</i></td><td><i>Description</i></td><td><i>Note</i></td></tr>
 * <tr><td><code>AAMC</code></td>
 *     <td>American Association of Medical Colleges</td>
 *     <td>not currently used</td></tr>
 * <tr><td><code>AIDS</code></td>
 *     <td>Special HIV/AIDS publications with abstracts written by 
 *         someone other than the author</td><td>&nbsp;</td></tr>
 * <tr><td><code>KIE</code></td>
 *     <td>Kennedy Institute of Ethics, Georgetown University</td>
 *     <td>&nbsp;</td></tr>
 * <tr><td><code>PIP</code></td>
 *     <td>Population Information Program, Johns Hopkins School of 
 *         Public Health</td><td>&nbsp;</td></tr>
 * <tr><td><code>NASA</code></td>
 *     <td>National Aeronautics and Space Administration</td>
 *     <td>&nbsp;</td></tr>
 * <tr><td><code>Consumer</code></td>
 *     <td>Abstracts written for consumers</td>
 *     <td>reserved for future use</td></tr>
 * </table>
 * </blockquote>
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class OtherAbstract {

    private final String mType;
    private final String mText;
    private final String mCopyrightInformation;

    OtherAbstract(String type, String text, String copyrightInformation) {
        mType = type;
        mText = text;
        mCopyrightInformation = copyrightInformation;
    }
    
    /**
     * Returns the type of this abstract.  The possible
     * types are enumerated in the class documentation above.
     *
     * @return The type of this abstract.
     */
    public String type() {
        return mType;
    }

    /**
     * Returns the text of this abstract.
     *
     * @return The text of this abstract.
     */
    public String text() {
        return mText;
    }

    /**
     * Returns the copyright information for this abstract,
     * or the empty (zero length) string if none was provided.
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
        sb.append("Type=");
        sb.append(type());
        sb.append(" Text=");
        sb.append(text());
        sb.append('}');
        return sb.toString();
    }

    // <!ELEMENT OtherAbstract (%Abstract;)>
    // <!ATTLIST OtherAbstract
    //           Type %Type;>
    // <!ENTITY % Abstract "(AbstractText,CopyrightInformation?)">
    static class Handler extends DelegateHandler {
        private String mType;
        private final TextAccumulatorHandler mAbstractTextHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mCopyrightInformationHandler
            = new TextAccumulatorHandler();
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            setDelegate(MedlineCitationSet.ABSTRACT_TEXT_ELT,
                        mAbstractTextHandler);
            setDelegate(MedlineCitationSet.COPYRIGHT_INFORMATION_ELT,
                        mCopyrightInformationHandler);
        }
        public void startDocument() throws SAXException {
            mAbstractTextHandler.reset();
            mCopyrightInformationHandler.reset();
            super.startDocument();
        }
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts)
            throws SAXException {

            if (qName.equals(MedlineCitationSet.OTHER_ABSTRACT_ELT))
                mType = atts.getValue(MedlineCitationSet.TYPE_ATT);
            super.startElement(namespaceURI,localName,qName,atts);
        }
        public OtherAbstract getOtherAbstract() {
            return new OtherAbstract(mType,
                                     mAbstractTextHandler.getText(),
                                     mCopyrightInformationHandler.getText());
        }
    }
}
