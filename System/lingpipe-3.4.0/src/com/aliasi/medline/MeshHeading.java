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

import java.util.ArrayList;
import java.util.Arrays;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>MeshHeading</code> represents a particular heading in NLM's
 * controlled vocabulary of Medical Subject Headings (MeSH).  Each
 * heading is composed of a descriptor and zero or more qualifiers.
 * Each descriptor and qualifier is marked as to whether it is major
 * or minor for the article.
 *
 * <P>For more information about the MeSH vocabulary, see:
 *
 * <blockquote>
 * <a href=" http://www.nlm.nih.gov/mesh/meshhome.html">MeSH Home Page</a>
 * </blockquote>
 * 
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public  class MeshHeading {

    private final Topic[] mTopics;

    MeshHeading(Topic[] topics) {
        mTopics = topics;
    }

    /**
     * Return an array containing the descriptor followed by
     * all qualifiers for this heading.
     *
     * @return All topics for this heading.
     */
    public Topic[] topics() {
        return mTopics;
    }

    /**
     * Return the descriptor for this heading.
     *
     * @return The descriptor for this heading.
     */
    public Topic descriptor() {
        return mTopics[0];
    }

    /**
     * Returns an array of zero or more qualifiers for this
     * heading.
     *
     * @return Zero or more qualifiers for this heading.
     */
    public Topic[] qualifiers() {
        Topic[] result = new Topic[mTopics.length-1];
        System.arraycopy(mTopics,1,result,0,mTopics.length-1);
        return result;
    }

    /**
     * Returns a string-based representation of this heading.
     *
     * @return A string-based representation of this heading.
     */
    public String toString() {
        return Arrays.asList(mTopics).toString();
    }

    // anonymously collects the list elements; should use
    // this pattern elsewhere to pull all finishDelegate out
    // of MedlineCitation for lists
    static class ListHandler extends DelegateHandler {
        private final Handler mMeshHeadingHandler;
        private final ArrayList mMeshHeadingList = new ArrayList();
        public ListHandler(DelegatingHandler delegator) {
            super(delegator);
            mMeshHeadingHandler = new Handler(delegator);
            setDelegate(MedlineCitationSet.MESH_HEADING_ELT,
                        mMeshHeadingHandler);
        }
        public void reset() {
            mMeshHeadingList.clear();
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(MedlineCitationSet.MESH_HEADING_ELT))
                mMeshHeadingList.add(((Handler)handler).getMeshHeading());
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public MeshHeading[] getMeshHeadings() {
            MeshHeading[] meshHeadings 
                = new MeshHeading[mMeshHeadingList.size()];
            mMeshHeadingList.toArray(meshHeadings);
            return meshHeadings;
        }
    }

    // <!ELEMENT MeshHeadingList (MeshHeading+)>
    // <!ELEMENT MeshHeading (DescriptorName, QualifierName*)>
    // <!ELEMENT DescriptorName (#PCDATA)>
    // <!ATTLIST DescriptorName
    //           MajorTopicYN (Y | N) "N" >
    // <!ELEMENT QualifierName (#PCDATA)>
    // <!ATTLIST QualifierName
    //           MajorTopicYN (Y | N) "N" >
    static class Handler extends DelegateHandler {
        private final ArrayList mTopicList = new ArrayList();
        private final Topic.Handler mTopicHandler;
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            mTopicHandler = new Topic.Handler();
            setDelegate(MedlineCitationSet.DESCRIPTOR_NAME_ELT,mTopicHandler);
            setDelegate(MedlineCitationSet.QUALIFIER_NAME_ELT,mTopicHandler);
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            mTopicList.clear();
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(MedlineCitationSet.DESCRIPTOR_NAME_ELT)
                || qName.equals(MedlineCitationSet.QUALIFIER_NAME_ELT)) {
                mTopicList.add(((Topic.Handler)handler).getTopic());
            }
        }
        public MeshHeading getMeshHeading() {
            Topic[] topics = new Topic[mTopicList.size()];
            mTopicList.toArray(topics);
            return new MeshHeading(topics);
        }
    }


}
