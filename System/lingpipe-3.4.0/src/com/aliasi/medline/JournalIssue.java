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
 * A <code>JournalIssue</code> contains information about a particular
 * issue of a journal, including publication date and optionally
 * volume and issue number.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class JournalIssue {

    private final String mVolume;
    private final String mIssue;
    private final PubDate mPubDate;

    JournalIssue(String volume,
                 String issue,
                 PubDate pubDate) {
        mVolume = volume;
        mIssue = issue;
        mPubDate = pubDate;
    }

    /**
     * Returns a string representing the volume number of
     * this journal issue, or the empty (zero length) string
     * if none was provided.
     *
     * @return The volume number of this journal issue.
     */
    public String volume() {
        return mVolume;
    }

    /**
     * Returns a string representing the issue number of
     * this journal issue, or the empty (zero length) string if
     * none was provided.
     *
     * @return The issue number for this journal.
     */
    public String issue() {
        return mIssue;
    }

    /**
     * Returns the publication date for this journal issue.
     *
     * @return The publication date for this journal issue.
     */
    public PubDate pubDate() {
        return mPubDate;
    }

    /**
     * Returns a string-based representation of this journal issue.
     *
     * @return A string-based representation of this journal issue.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        sb.append(" Volume=" + volume());
        sb.append(" Issue=" + issue());
        sb.append(" Pub Date=" + pubDate());
        sb.append('}');
        return sb.toString();
    }

    // <!ELEMENT JournalIssue (Volume?, Issue?, %PubDate.Ref;)>
    // <!ELEMENT Volume (#PCDATA)>
    // <!ELEMENT Issue (#PCDATA)>
    // <!ENTITY % PubDate.Ref "PubDate">
    static class Handler extends DelegateHandler {
        private final TextAccumulatorHandler mVolumeHandler 
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mIssueHandler 
            = new TextAccumulatorHandler();
        private final PubDate.Handler mPubDateHandler;
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            mPubDateHandler = new PubDate.Handler(delegator);
            setDelegate(MedlineCitationSet.VOLUME_ELT,mVolumeHandler);
            setDelegate(MedlineCitationSet.ISSUE_ELT,mIssueHandler);
            setDelegate(MedlineCitationSet.PUB_DATE_ELT,mPubDateHandler);
        }
        public void startDocument() throws SAXException {
            mVolumeHandler.reset();
            mIssueHandler.reset();
            super.startDocument();
        }
        public JournalIssue getJournalIssue() {
            return new JournalIssue(mVolumeHandler.getText(),
                                    mIssueHandler.getText(),
                                    mPubDateHandler.getPubDate());

        }

    }


}
