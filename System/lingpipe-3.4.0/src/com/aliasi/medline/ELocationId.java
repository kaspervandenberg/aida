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

/**
 * An <code>ELocationId</code> represents the pagination type
 * information for an electronic publication.
 *
 * @author  Bob Carpenter
 * @version 3.3
 * @since   LingPipe2.0
 */
public class ELocationId {

    private final String mLocation;
    private final boolean mValid;
    private final String mEIdType;

    /**
     * Construct an electronic location identifier with the specified
     * text content, identifer type and validity flag.
     *
     * @param location Text content of the electronic location identifier
     * element.
     * @param eIdType The type of electronic identifier.
     * @param valid <code>true</code> if the location identifier was
     * flagged as validated in the citation.
     */
    public ELocationId(String location,
                       String eIdType,
                       boolean valid) {
        mLocation = location;
        mValid = valid;
        mEIdType = eIdType;
    }


    /**
     * Returns the location for this electronic location identifier.
     * This may be arbitrary text; it is the text content of the
     * electronic identifier element.
     */
    public String location() {
        return mLocation;
    }

    /**
     * Returns <code>true</code> if this electronic location
     * identifier was marked as validated in the citation.
     *
     * @return <code>true</code> if this electronic location is valid.
     */
    public boolean valid() {
        return mValid;
    }

    /**
     * Returns the type of electronic identifier for this electronic
     * location identifier.  The value returned will be one of either
     * <code>doi</code> or <code>pii</code>.
     *
     * @return The type for this identifier.
     */
    public String eIdType() {
        return mEIdType;
    }

    static class Handler extends TextAccumulatorHandler {
        boolean mValid;
        String mEIdType;
        public void startElement(String url, String name, String qName,
                                 Attributes atts) {
            if (qName.equals(MedlineCitationSet.E_LOCATION_ID_ELT)) {
                String validAttVal = atts.getValue(MedlineCitationSet.VALID_YN_ATT);
                mValid = (validAttVal == null)
                    || Boolean.parseBoolean(validAttVal);
                mEIdType = atts.getValue(MedlineCitationSet.E_ID_TYPE_ATT);
            }
        }
        public ELocationId getELocationId() {
            return new ELocationId(getText(),mEIdType,mValid);
        }
    }

}