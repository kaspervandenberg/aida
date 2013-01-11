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

import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.DelegateHandler;

import com.aliasi.util.Iterators;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>KeywordList</code> consists of a set of topics with a
 * specified owner.  
 * 
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public class KeywordList implements Iterable<Topic> {

    private final String mOwner;
    private final Topic[] mKeywords;
    
    KeywordList(String owner, Topic[] keywords) {
        mOwner = owner;
        mKeywords = keywords;
    }


    /**
     * Returns the iterator over the topics in this keyword list.  This
     * implements <code>Iterable&lt;Topic&gt;</code>, allowing general
     * for-loops over topics to be used.
     *
     * @return The iterator over topics.
     */
    public Iterator<Topic> iterator() {
        return new Iterators.Array<Topic>(mKeywords);
    }

    /**
     * Returns the owner of this keyword list.  
     *
     * <P>The set of possible owner values is:
     *
     * <blockquote>
     * <table border='1' cellpadding='5'>
     * <tr><td><i>Owner</i></td><td><i>Description</i></td></tr>
     * <tr><td><code>NASA</code></td>
     *     <td>National Aeronautics and Space Administration</td></tr>
     * <tr><td><code>PIP</code></td>
     *     <td>Population Information Program, Johns Hopkins School of 
     *         Public Health</td></tr>
     * <tr><td><code>KIE</code></td>
     *     <td>Kennedy Institute of Ethics, Georgetown University</td></tr>
     * </table>
     * </blockquote>
     *
     * @return Owner of this keyword list.
     */
    public String owner() {
        return mOwner;
    }

    /**
     * Returns the array of keywords represented as topics.
     * As topics, each keyword has a text content and an indication
     * of whether it is a major or minor topic for the article.
     *
     * @return The keywords for this keyword list.
     */
    public Topic[] keywords() {
        return mKeywords;
    }

    /**
     * Returns a string-based representation of this keyword list.
     *
     * @return A string-based representation of this keyword list.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        sb.append("Owner=");
        sb.append(owner());
        sb.append(" Keywords=");
        sb.append(Arrays.asList(keywords()));
        sb.append('}');
        return sb.toString();
    }


    // <!ELEMENT KeywordList (Keyword+)>
    // <!ATTLIST KeywordList
    //           Owner %Owner; "NLM">
    // <!ELEMENT Keyword (#PCDATA)>
    // <!ATTLIST Keyword
    //           MajorTopicYN (Y | N) "N" >
    static class Handler extends DelegateHandler {
        private boolean mVisited = false;
        private String mOwner;
        private final ArrayList mKeywordList = new ArrayList();
        private final Topic.Handler mKeywordHandler;
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            mKeywordHandler = new Topic.Handler();
            setDelegate(MedlineCitationSet.KEYWORD_ELT,
                        mKeywordHandler);
        }
        public void reset() {
            mKeywordList.clear();
            mKeywordHandler.reset();
            mVisited = false;
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
            mVisited = true;
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(MedlineCitationSet.KEYWORD_ELT)) {
                mKeywordList.add(mKeywordHandler.getTopic());
            }
        }
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts)
            throws SAXException {

            super.startElement(namespaceURI,localName,qName,atts);
            if (qName.equals(MedlineCitationSet.KEYWORD_LIST_ELT)) 
                mOwner = atts.getValue(MedlineCitationSet.OWNER_ATT);

        }
        public KeywordList getKeywordList() {
            if (!mVisited) return null;
            Topic[] keywords = new Topic[mKeywordList.size()];
            mKeywordList.toArray(keywords);
            return new KeywordList(mOwner,keywords);
        }
    }
}
