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

import org.xml.sax.SAXException;

/**
 * A <code>PersonalNameSubject</code> is provided for citations
 * that contain a biographical note or obituary about a given
 * individual.  
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class PersonalNameSubject {

    private final Name mName;
    private final String mDatesAssociatedWithName;
    private final String mNameQualifier;
    private final String mOtherInformation;
    private final String mTitleAssociatedWithName;
    
    PersonalNameSubject(Name name,
                        String datesAssociatedWithName,
                        String nameQualifier,
                        String otherInformation,
                        String titleAssociatedWithName) {
        mName = name;
        mDatesAssociatedWithName = datesAssociatedWithName;
        mNameQualifier = nameQualifier;
        mOtherInformation = otherInformation;
        mTitleAssociatedWithName = titleAssociatedWithName;
    }

    /**
     * Returns the structured name for this subject.
     *
     * @return The name of this subject.
     */
    public Name name() {
        return mName;
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
     * Returns a string-based representation of this subject.
     *
     * @return A string-based representation of this subject.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        sb.append("Name=");
        sb.append(name());
        if (datesAssociatedWithName() != null 
            && datesAssociatedWithName().length() > 0) {
            sb.append(" Dates Associated with Name=");
            sb.append(datesAssociatedWithName());
        }
        if (nameQualifier() != null && nameQualifier().length() > 0) {
            sb.append(" Name Qualifer=");
            sb.append(nameQualifier());
        }
        if (otherInformation() != null && otherInformation().length() > 0) {
            sb.append(" Other Information=");
            sb.append(otherInformation());
        }
        if (titleAssociatedWithName() != null 
            && titleAssociatedWithName().length() > 0) {
            sb.append(" Title Associated with Name=");
            sb.append(titleAssociatedWithName());
        }
        sb.append('}');
        return sb.toString();
    }

    // <!ELEMENT PersonalNameSubject (%personal.name;, 
    //                                DatesAssociatedWithName?,
    //                                NameQualifier?, OtherInformation?,
    //                                TitleAssociatedWithName?)>
    // <!ENTITY % personal.name "(LastName,(ForeName|(FirstName,MiddleName?))?,
    //            Initials?,Suffix?)">
    // also used by author.name and Investigator
    static class Handler extends Name.Handler {
        private final TextAccumulatorHandler mDatesAssociatedWithNameHandler 
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mNameQualifierHandler 
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mOtherInformationHandler 
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mTitleAssociatedWithNameHandler 
            = new TextAccumulatorHandler();
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            setDelegate(MedlineCitationSet.DATES_ASSOCIATED_WITH_NAME_ELT,
                        mDatesAssociatedWithNameHandler);
            setDelegate(MedlineCitationSet.NAME_QUALIFIER_ELT,
                        mNameQualifierHandler);
            setDelegate(MedlineCitationSet.OTHER_INFORMATION_ELT,
                        mOtherInformationHandler);
            setDelegate(MedlineCitationSet.TITLE_ASSOCIATED_WITH_NAME_ELT,
                        mTitleAssociatedWithNameHandler);
        }
        public void reset() {
            mDatesAssociatedWithNameHandler.reset();
            mNameQualifierHandler.reset();
            mOtherInformationHandler.reset();
            mTitleAssociatedWithNameHandler.reset();
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public PersonalNameSubject getPersonalNameSubject() {
            return new PersonalNameSubject(getName(),
                                           mDatesAssociatedWithNameHandler
                                           .getText(),
                                           mNameQualifierHandler.getText(),
                                           mOtherInformationHandler
                                           .getText(),
                                           mTitleAssociatedWithNameHandler
                                           .getText());
        }
    }

}
