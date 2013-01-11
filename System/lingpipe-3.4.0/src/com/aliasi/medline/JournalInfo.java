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
 * A <code>JournalInfo</code> object contains an abbreviation for a
 * journal's title, and optionally a country and optionally a unique
 * NLM identifier.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class JournalInfo {

    private final String mMedlineTA;
    private final String mCountry;
    private final String mNlmUniqueID;
    private final String mIssnLinking;

    JournalInfo(String medlineTA,
                String country,
                String nlmUniqueID,
                String issnLinking) {
        mMedlineTA = medlineTA;
        mCountry = country;
        mNlmUniqueID = nlmUniqueID;
        mIssnLinking = issnLinking;
    }

    /**
     * Return the standard MEDLINE title abbreviation for the journal.
     *
     * <P>The title abbreviation process is described in
     * more detail in:
     *
     * <blockquote>
     * <a href="http://www.nlm.nih.gov/pubs/factsheets/constructitle.html"
     *   >NLM Fact Sheet: MEDLINE Titles</a>
     * </blockquote>
     *
     * @return The title abbreviation for the journal.
     */
    public String medlineTA() {
        return mMedlineTA;
    }

    /**
     * Returns the country of publication of the journal, or the
     * empty (zero-length) string if none was provided.  The case
     * of countries varies.  The result may be <code>Unknown</code>
     * for unknown countries.
     *
     * <P>A list of valid country values may be found in the Z category
     * of Medical Subject Headings (MeSH) tree structures, available from:
     *
     * <blockquote>
     * <a href="http://www.nlm.nih.gov/mesh">MeSH Home Page</a>.
     *</blockquote>
     *
     * @return The country of pulbication of the journal.
     */
    public String country() {
        return mCountry;
    }

    /**
     * Returns the NLM unique identifier for the journal, or the
     * empty (zero length) string if none was provided.  The returned
     * value will be an alpha-numeric string.
     *
     * <P>The serial numbers returned are assigned in NLM's
     * Integrated Library System, LocatorPlus. More information is
     * available from:
     *
     * <blockquote>
     * <a href="http://locatorplus.gov/">NLM's Locator Plus Home Page</a>
     * </blockquote>
     *
     * @return The NLM unique identifier for the jjournal.
     */
    public String nlmUniqueID() {
        return mNlmUniqueID;
    }

    /**
     * Returns the unique ISSN (international standard serial number)
     * for a single source, independent of its medium, or the empty
     * (zero length) string if none was provided.  The returned value
     * will be a valid ISSN code (see the description in ISSN's own
     * publication <a href="http://www.issn.org/en/node/64">What is an
     * ISSN?</code>).
     *
     * @return The ISSN code for this source.
     */
    public String issnLinking() {
        return mIssnLinking;
    }

    /**
     * Returns a string-based representation of this journal information.
     *
     * @return A string-based representation of this journal information.
     */
    public String toString() {
        return "{"
            + "Medline TA=" + medlineTA()
            + " Country=" + country()
            + " NLM ID=" + nlmUniqueID()
            + " ISSN Linking=" + issnLinking()
            + "}";

    }

    // <!ELEMENT MedlineJournalInfo (Country?, MedlineTA, NlmUniqueID?, ISSNLinking?)>
    // <!ELEMENT Country (#PCDATA)>
    // <!ELEMENT MedlineTA (#PCDATA)>
    // <!ELEMENT NlmUniqueID (#PCDATA)>

    static class Handler extends DelegateHandler {
        private final TextAccumulatorHandler mMedlineTAHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mCountryHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mNlmUniqueIDHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mIssnLinkingHandler
            = new TextAccumulatorHandler();
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            setDelegate(MedlineCitationSet.MEDLINE_TA_ELT,mMedlineTAHandler);
            setDelegate(MedlineCitationSet.COUNTRY_ELT,mCountryHandler);
            setDelegate(MedlineCitationSet.NLM_UNIQUE_ID_ELT,
                        mNlmUniqueIDHandler);
            setDelegate(MedlineCitationSet.ISSN_LINKING_ELT,
                        mIssnLinkingHandler);
        }
        public void startDocument() throws SAXException {
            mMedlineTAHandler.reset();
            mCountryHandler.reset();
            mNlmUniqueIDHandler.reset();
            mIssnLinkingHandler.reset();
            super.startDocument();
        }
        public JournalInfo getJournalInfo() {
            return new JournalInfo(mMedlineTAHandler.getText(),
                                   mCountryHandler.getText(),
                                   mNlmUniqueIDHandler.getText(),
                                   mIssnLinkingHandler.getText());
        }
    }

}
