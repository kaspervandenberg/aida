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

import com.aliasi.xml.TextAccumulatorHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A <code>Topic</code> consists of a string-based topic and
 * an indication as to whether the topic is a major topic for
 * an article.  
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class Topic {

    private final boolean mMajor;
    private final String mTopic;

    Topic(String topic, boolean major) {
        mTopic = topic;
        mMajor = major;
    }
    
    /**
     * Returns <code>true</code> if this topic is a major
     * topic for the article.
     *
     * @return <code>true</code> if this topic is a major
     * topic.
     */
    public boolean isMajor() {
        return mMajor;
    }

    /**
     * Return the text of the topic.
     *
     * @return The text of the topic.
     */
    public String topic() {
        return mTopic;
    }

    /**
     * Return a string-based representation of this topic.
     *
     * @return A string-based representation of this topic.
     */
    public String toString() {
        return mTopic + (isMajor() ? " [MAJ=Y]" : " [MAJ=N]");
    }

    static class Handler extends TextAccumulatorHandler {
        private boolean mMajor;
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts)
            throws SAXException {

            super.startElement(namespaceURI,localName,qName,atts);
            mMajor = MedlineCitationSet
                .YES_VALUE
                .equals(atts.getValue(MedlineCitationSet.MAJOR_TOPIC_YN_ATT));
        }
        public Topic getTopic() {
            return new Topic(getText(),mMajor);
        }
    }
}
