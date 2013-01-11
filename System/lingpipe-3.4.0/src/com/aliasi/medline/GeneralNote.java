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
 * A <code>GeneralNote</code> represents supplemental or descriptive
 * information related to the record.  
 *
 * <P>Each note has an owner, the possible values
 * for which are the same as those of the owner for a 
 * citation, as enumerated in {@link MedlineCitation#owner()}.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class GeneralNote {

    private final String mOwner;
    private final String mNote;

    GeneralNote(String owner, String note) {
        mOwner = owner;
        mNote = note;
    }
    
    /**
     * Returns the owner of this note.  The value will be
     * one of the values enumerated in {@link MedlineCitation#owner()}.
     
     * @return The owner of this note.
     */
    public String owner() {
        return mOwner;
    }

    /**
     * Return the text of the note itself.
     *
     * @return The text of the note.
     */
    public String note() {
        return mNote;
    }

    /**
     * Return a string-based representation of this note.
     *
     * @return A string-based representation of this note.
     */
    public String toString() {
        return "Owner=" + owner() + " Note=" + note();
    }

    // <!ELEMENT GeneralNote (#PCDATA)>
    // <!ATTLIST GeneralNote
    //           Owner %Owner; "NLM">
    static class Handler extends TextAccumulatorHandler {
        private String mOwner;
        public GeneralNote getNote() {
            return new GeneralNote(mOwner,getText());
        }
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts)
            throws SAXException {
            if (qName.equals(MedlineCitationSet.GENERAL_NOTE_ELT)) {
                mOwner = atts.getValue(MedlineCitationSet.OWNER_ATT);
            } else {
                super.startElement(namespaceURI,localName,qName,atts);
            }
        }
    }
}
