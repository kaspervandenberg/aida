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
import com.aliasi.xml.TextAccumulatorHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * An instance of <code>Author</code> represents an author
 * of a paper, including information about name, affiliation,
 * title, and various qualifiers and dates.
 *
 * <P>Some of the elements appearing within the <code>Author</code>
 * element in the DTD are not documented in NLM's description of
 * elements or alphabetical listing.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class Author {

    private final String mCollectiveName;
    private final Name mName;
    private final String mAffiliation;
    private final String mDatesAssociatedWithName;
    private final String mNameQualifier;
    private final String mOtherInformation;
    private final String mTitleAssociatedWithName;
    private final boolean mIsValid;

    Author(String collectiveName,
           Name name,
           String affiliation,
           String datesAssociatedWithName,
           String nameQualifier,
           String otherInformation,
           String titleAssociatedWithName,
           boolean isValid) {
        mCollectiveName = collectiveName;
        mName = name;
        mAffiliation = affiliation;
        mDatesAssociatedWithName = datesAssociatedWithName;
        mNameQualifier = nameQualifier;
        mOtherInformation = otherInformation;
        mTitleAssociatedWithName = titleAssociatedWithName;
        mIsValid = isValid;
    }

    /**
     * Returns <code>true</code> if the spelling of the authr names
     * has been validated.
     *
     * @return <code>true</code> if the spelling of the authr names
     * has been validated.
     */
    public boolean isValid() {
        return mIsValid;
    }

    /**
     * Returns <code>true</code> if this author is a collective.  If
     * this method returns <code>true</code> if and only if
     * <code>collectiveName()</code> returns a string of length greater
     * than zero.
     *
     * @return <code>true</code> if this author is a collective.
     */
    public boolean isCollective() {
        return mCollectiveName != null
            && mCollectiveName.length() > 0;
    }

    /**
     * Returns the collective or corporate name of this author.  If
     * the author is a collective, the collective name will be a
     * string of length greater than zero.  If it is not a collective,
     * it will be the zero-length string.  Note that before 2001,
     * corporate author information was indicated at the end of the
     * article title.  All names appear as they are in the journal,
     * though some non-English collective names are transliterated.
     *
     * @return The collective name of this author.
     */
    public String collectiveName() {
        return mCollectiveName;
    }

    /**
     * Returns the name of the author if the author is not a
     * collective, or <code>null</code> if the author is a collective.
     *
     * @return The name of this author if the author is not a
     * collective.
     */
    public Name name() {
        if (isCollective()) return null;
        return mName;
    }

    /**
     * Returns the affiliation for this author.  If the
     * result is of length zero, there was no affiliation provided
     * in the record.  Affiliations were first marked in 1988. The
     * format evolved over time, and includes information such as
     * institution name, city (and state and zip code in the United
     * States) and country, and later e-mail.
     *
     * <P> The first author's affiliation is repeated at the article
     * level; see {@link Article#affiliation()}.
     *
     * @return The affiliation for this author.
     */
    public String affiliation() {
        return mAffiliation;
    }

    /**
     * Returns a string-based representation of the dates
     * associated with this name.  There is no documentation for
     * this field in the element overview or in the alphabetical
     * lists supplied by NLM.  A result of length zero indicates
     * that no date associated with this name was supplied in
     * the record.
     *
     * @return The dates associated with this name.
     */
    public String datesAssociatedWithName() {
        return mDatesAssociatedWithName;
    }

    /**
     * Returns a string-based representation of the qualifier
     * associated with this name.  There is no documentation for
     * this field in the element overview or in the alphabetical
     * lists supplied by NLM.  A result of length zero indicates
     * that no name qualifier was supplied for this name in
     * the record.
     *
     * @return The dates associated with this name.
     */
    public String nameQualifier() {
        return mNameQualifier;
    }

    /**
     * Returns a string-based representation of the other information
     * associated with this name.  There is no documentation for this
     * field in the element overview or in the alphabetical lists
     * supplied by NLM.  A result of length zero indicates that no
     * other information was supplied for this name in the record.
     *
     * @return The other information associated with this name.
     */
    public String otherInformation() {
        return mOtherInformation;
    }

    /**
     * Returns a string-based representation of the title associated
     * with this name.  There is no documentation for this field in
     * the element overview or in the alphabetical lists supplied by
     * NLM.  A result of length zero indicates that no title
     * information was supplied for this name in the record.
     *
     * @return The title information associated with this name.
     */
    public String titleAssociatedWithName() {
        return mTitleAssociatedWithName;
    }

    /**
     * Returns a string-based representation of this author.
     *
     * @return A string-based representation of this author.
     */
    public String toString() {
        if (isCollective())
            return "Collective=" + collectiveName();
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        sb.append("Name=");
        sb.append(mName);
        if (affiliation().length() > 0) {
            sb.append(" Affiliation=");
            sb.append(affiliation());
        }
        if (datesAssociatedWithName().length() > 0) {
            sb.append(" Dates Associated with Name=");
            sb.append(datesAssociatedWithName());
        }
        if (nameQualifier().length() > 0) {
            sb.append(" Name Qualifier=");
            sb.append(nameQualifier());
        }
        if (otherInformation().length() > 0) {
            sb.append(" Other Information=");
            sb.append(otherInformation());
        }
        if (titleAssociatedWithName().length() > 0) {
            sb.append(" Title Associated with Name=");
            sb.append(titleAssociatedWithName());
        }
        sb.append('}');
        return sb.toString();
    }

    // <!ELEMENT Author ((%author.name;), Affiliation?,
    //                   DatesAssociatedWithName?,
    //                   NameQualifier?,OtherInformation?,
    //                   TitleAssociatedWithName?)>
    // <!ENTITY % author.name "(%personal.name; | CollectiveName)">
    // <!ENTITY % personal.name "(LastName,(ForeName|(FirstName,MiddleName?))?,
    //                            Initials?,Suffix?)">
    static class Handler extends Name.Handler {
        private final TextAccumulatorHandler mCollectiveNameHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mAffiliationHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mDatesAssociatedWithNameHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mNameQualifierHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mOtherInformationHandler
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mTitleAssociatedWithNameHandler
            = new TextAccumulatorHandler();
        boolean mIsValid;
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            setDelegate(MedlineCitationSet.COLLECTIVE_NAME_ELT,
                        mCollectiveNameHandler);
            setDelegate(MedlineCitationSet.AFFILIATION_ELT,
                        mAffiliationHandler);
            setDelegate(MedlineCitationSet.DATES_ASSOCIATED_WITH_NAME_ELT,
                        mDatesAssociatedWithNameHandler);
            setDelegate(MedlineCitationSet.NAME_QUALIFIER_ELT,
                        mNameQualifierHandler);
            setDelegate(MedlineCitationSet.OTHER_INFORMATION_ELT,
                        mOtherInformationHandler);
            setDelegate(MedlineCitationSet.TITLE_ASSOCIATED_WITH_NAME_HANDLER,
                        mTitleAssociatedWithNameHandler);
        }
        public void startElement(String x, String y, String qName, Attributes atts)
            throws SAXException {

            super.startElement(x,y,qName,atts);
            if (qName.equals(MedlineCitationSet.AUTHOR_ELT)) {
                String isValidString = atts.getValue(MedlineCitationSet.VALID_YN_ATT);
                mIsValid = MedlineCitationSet.YES_VALUE.equals(isValidString);
            }
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            mCollectiveNameHandler.reset();
            mAffiliationHandler.reset();
            mDatesAssociatedWithNameHandler.reset();
            mNameQualifierHandler.reset();
            mOtherInformationHandler.reset();
            mTitleAssociatedWithNameHandler.reset();
        }
        public Author getAuthor() {
            return new Author(mCollectiveNameHandler.getText(),
                              getName(),
                              mAffiliationHandler.getText(),
                              mDatesAssociatedWithNameHandler.getText(),
                              mNameQualifierHandler.getText(),
                              mOtherInformationHandler.getText(),
                              mTitleAssociatedWithNameHandler.getText(),
                              mIsValid);
        }
    }

}
