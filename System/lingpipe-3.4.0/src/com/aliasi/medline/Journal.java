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

import java.util.ArrayList;

import org.xml.sax.SAXException;

/**
 * A <code>Journal</code> represents a particular issue of a journal.
 * Information is available such as the title and issue number of the
 * journal, as well as the ISSN for the journal.
 *
 * <P>Information about a particular article in a journal is provided as
 * part of the {@link Article} object.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class Journal {

    private final String mISSN;
    private final JournalIssue mJournalIssue;
    private final String mCoden;
    private final String mTitle;
    private final String mISOAbbreviation;

    Journal(String issn,
            JournalIssue journalIssue,
            String coden,
            String title,
            String isoAbbreviation) {

        mISSN = issn;
        mJournalIssue = journalIssue;
        mCoden = coden;
        mTitle = title;
        mISOAbbreviation = isoAbbreviation;
    }

    /**
     * Returns the International Standard Serial Number (ISSN) for this
     * journal, or <code>null</code> if one was not provided as part
     * of the citation.  ISSNs are eight-digit numbers expressed as
     * hyphenated nine-character strings of the form
     * <code>XXXX-XXXX</code>.  If a journal has both an electronic and
     * print ISSN, only one is chosen and used consistently, though there
     * is no indication of which one.
     *
     * @return The ISSN for this journal.
     */
    public String issn() {
        return mISSN;
    }

    /**
     * Returns a structured representation of the issue of this
     * journal.  The result is always non-<code>null</code>.
     *
     * @return The issue information for this journal.
     */
    public JournalIssue journalIssue() {
        return mJournalIssue;
    }

    /**
     * Returns an abbreviated code name for this journal, or the empty
     * (zero length) string if none was provided.  Note that not every
     * journal has an official abbreviation code.
     *
     * @return The abbreviated name for this journal.
     */
    public String coden() {
        return mCoden;
    }

    /**
     * Returns the title of this journal, or the empty (zero length)
     * string if none was provided.
     *
     * @return The title of the journal.
     */
    public String title() {
        return mTitle;
    }

    /**
     * Returns the ISO standard abbreviation for this journal.
     *
     * @return The ISO abbreviation for this journal.
     */
    public String isoAbbreviation() {
        return mISOAbbreviation;
    }

    /**
     * Returns a string-based representation of this journal.
     *
     * @return A string-based representation of this journal.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("Journal Issue=");
        sb.append(mJournalIssue.toString());
        if (issn() != null && issn().length() > 0) {
            sb.append(" ISSN=");
            sb.append(issn());
        }
        if (coden() != null && coden().length() > 0) {
            sb.append(" Coden=");
            sb.append(coden());
        }
        if (title() != null && title().length() > 0) {
            sb.append(" Title=" + title());
        }
        if (isoAbbreviation() != null && mISOAbbreviation.length() > 0) {
            sb.append(" ISO Abbrev=");
            sb.append(isoAbbreviation());
        }
        sb.append("}");
        return sb.toString();
    }

    // <!ELEMENT Journal (%ISSN.Ref;, JournalIssue, Coden?,
    //                    Title?, ISOAbbreviation?)>
    // <!ENTITY % ISSN.Ref "ISSN?">
    static class Handler extends DelegateHandler {
        private final ArrayList mISSNList = new ArrayList();
        private final TextAccumulatorHandler mISSNHandler
            = new TextAccumulatorHandler();
        private final JournalIssue.Handler mJournalIssueHandler;
        private boolean mFound = false;
        private final TextAccumulatorHandler mCodenHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mJournalTitleHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mISOAbbreviationHandler
            = new TextAccumulatorHandler();
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            mJournalIssueHandler = new JournalIssue.Handler(delegator);
            setDelegate(MedlineCitationSet.ISSN_ELT,mISSNHandler);
            setDelegate(MedlineCitationSet.JOURNAL_ISSUE_ELT,
                        mJournalIssueHandler);
            setDelegate(MedlineCitationSet.CODEN_ELT,mCodenHandler);
            setDelegate(MedlineCitationSet.TITLE_ELT,
                        mJournalTitleHandler);
            setDelegate(MedlineCitationSet.ISO_ABBREVIATION_ELT,
                        mISOAbbreviationHandler);
        }
        public void reset() {
            mFound = false;
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            mISSNList.clear();
            mFound = true;
            mCodenHandler.reset();
            mJournalTitleHandler.reset();
            mISOAbbreviationHandler.reset();
            mISSNHandler.reset();
        }
        public Journal getJournal() {
            if (!mFound) return null;
            return new Journal(mISSNHandler.getText(),
                               mJournalIssueHandler.getJournalIssue(),
                               mCodenHandler.getText(),
                               mJournalTitleHandler.getText(),
                               mISOAbbreviationHandler.getText());
        }
    }

}
