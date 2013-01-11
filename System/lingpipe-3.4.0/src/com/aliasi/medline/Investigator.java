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

/**
 * An <code>Investigator</code> represents a funded principal
 * investigator for the (United States) National Aeronautics and Space
 * Administration (NASA).  The information provided by an investigator
 * is similar to that provided by an {@link Author}.
 * 
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class Investigator {

    private final Name mName;
    private final String mAffiliation;

    Investigator(Name name, String affiliation) {
        mName = name;
        mAffiliation = affiliation;
    }

    /**
     * Returns the name of this investigator.
     *
     * @return The name of this investigator.
     */
    public Name name() {
        return mName;
    }

    /**
     * Returns the affiliation of this investigator, or the
     * empty (zero length) string if none was provided.
     *
     * @return The affiliation of this investigator.
     */
    public String affiliation() {
        return mAffiliation;
    }

    /**
     * Returns a string-based representation of this investigator.
     *
     * @return A string-based representation of this investigator.
     */
    public String toString() {
        return ("Name=" + name() 
        + " Affiliation=" + affiliation());
    }

    // <!ELEMENT Investigator (%personal.name;, Affiliation?)>
    // <!ELEMENT Affiliation (#PCDATA)>
    // personal.name see Name.Handler
    static class Handler extends Name.Handler {
        private final TextAccumulatorHandler mAffiliationHandler
            = new TextAccumulatorHandler();
        public Handler(DelegatingHandler delegator) {
        super(delegator);
            setDelegate(MedlineCitationSet.AFFILIATION_ELT,mAffiliationHandler);
        }
        public Investigator getInvestigator() {
            return new Investigator(getName(),mAffiliationHandler.getText());
        }
    }
}
