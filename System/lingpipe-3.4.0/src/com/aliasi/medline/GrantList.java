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

import com.aliasi.util.Iterators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>GrantList</code> provides information about the grants that
 * funded the work repored in the article.  The data are recorded as
 * they appear in the article.  The grant list may be incomplete; this
 * may be tested with the method {@link #isComplete()}.  The list of
 * grants returned will be of length zero in the case where no list of
 * grants was provided by a citation.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public class GrantList implements Iterable<Grant> {

    private final boolean mComplete;
    private final Grant[] mGrants;

    GrantList(boolean complete,
              Grant[] grants) {
        mComplete = complete;
        mGrants = grants;
    }

    /**
     * Returns the iterator over the grants in this list.  This
     * implements <code>Iterable&lt;Grant&gt;</code>, allowing general
     * for-loops over grants to be used.
     *
     * @return The iterator over grants.
     */
    public Iterator<Grant> iterator() {
        return new Iterators.Array<Grant>(mGrants);
    }

    /**
     * Returns <code>true</code> if this list of grants is complete.
     *
     * @return <code>true</code> if this list of grants is complete.
     */
    public boolean isComplete() {
        return mComplete;
    }

    /**
     * Returns the list of grants for this grant list.
     *
     * @return The list of grants for this grant list.
     */
    public Grant[] grants() {
        return mGrants;
    }
    
    /**
     * Returns a string-based representation of this
     * grant list.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        sb.append("Complete=");
        sb.append(isComplete());
        sb.append(" Grants=");
        sb.append(Arrays.asList(grants()));
        sb.append('}');
        return sb.toString();
    }

    // <!ELEMENT GrantList (Grant+)>
    // <!ATTLIST GrantList
    //           CompleteYN (Y | N) "Y" >
    static class Handler extends DelegateHandler {
        private boolean mComplete;
        private final ArrayList mGrantList = new ArrayList();
        private final Grant.Handler mGrantHandler;
        private boolean mVisited;
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            mGrantHandler = new Grant.Handler(delegator);
            setDelegate(MedlineCitationSet.GRANT_ELT,
                        mGrantHandler);
        }
        public void reset() {
            mVisited = false;
            mGrantList.clear();
            mGrantHandler.reset();
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
            mVisited = true;
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(MedlineCitationSet.GRANT_ELT)) {
                mGrantList.add(mGrantHandler.getGrant());
            }
        }

        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts)
            throws SAXException {

            super.startElement(namespaceURI,localName,qName,atts);
            if (qName.equals(MedlineCitationSet.GRANT_LIST_ELT)) {
                mComplete
                    = MedlineCitationSet
                    .YES_VALUE
                    .equals(atts.getValue(MedlineCitationSet.COMPLETE_YN_ATT));
            }

        }
        public GrantList getGrantList() {
            if (!mVisited) return null;
            Grant[] grants = new Grant[mGrantList.size()];
            mGrantList.toArray(grants);
            return new GrantList(mComplete,grants);
        }
    }

}
