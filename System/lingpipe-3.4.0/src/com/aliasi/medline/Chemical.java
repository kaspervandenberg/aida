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

/**
 * A <code>Chemical</code> indicates a chemical that is determined by
 * a name and registry number.  Registry numbers are not always
 * provided; those that are indicate their official numbering by the
 * Chemical Abstracts Service or by the Enzyme Nomenclature.  
 *
 * The Chemical Abstracts Service (CAS) database of chemicals is
 * available from:
 *
 * <blockquote>
 * <a href="http://www.cas.org/cgi-bin/regreport.pl"
 *   >CAS Substance Databases</a>
 * </blockquote>
 *
 * The Enzyme Nomenclature is available from:
 *
 * <blockquote>
 * <a href="http://www.chem.qmw.ac.uk/iubmb/enzyme/"
 *   >Enzyme Nomenclature Home Page</a>
 * </blockquote>
 *
 * The substance names are drawn from the MeSH vocabulary.  The MeSH
 * vocabulary is available from:
 * 
 * <blockquote>
 * <a href="http://www.nlm.nih.gov/mesh">MeSH Vocabulary Home Page</a>
 * </blockquote>
 * 
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class Chemical {

    private final String mRegistryNumber;
    private final String mNameOfSubstance;

    Chemical(String registryNumber,
             String nameOfSubstance) {
        mRegistryNumber = registryNumber;
        mNameOfSubstance = nameOfSubstance;
    }

    /**
     * Returns a string-based representation the registry number of
     * this chemical.  The value might be <code>0</code> (a single
     * zero), if no number is available.  Numbers that do occur are in
     * one of two formats.  The first is a unique
     * five to nine digit hyphenated number assigned by the Chemical
     * Abstracts Service (<i>e.g.</i> <code>69-93-2</code>). Enzymes
     * with names assigned by the Enzyme Nomenclature
     * (<i>e.g.</i> <code>EC 3.1.1.34</code>).
     *
     * @return The registry number for this chemical.
     */
    public String registryNumber() {
        return mRegistryNumber;
    }

    /**
     * Returns the name of this chemical, with vocabulary drawn
     * from MeSH.  Note that there is no other MeSH identifier provided
     * in MEDLINE records.
     *
     * @return The name of this substance.
     */
    public String nameOfSubstance() {
        return mNameOfSubstance;
    }

    /**
     * Returns a string-based representation of this chemical.
     *
     * @return A string-based representation of this chemical.
     */
    public String toString() {
        return '{'
            + "RegistryNumber=" + registryNumber()
            + " NameOfSubstance=" + nameOfSubstance()
            + '}';
    }

    // <!ELEMENT Chemical (RegistryNumber, NameOfSubstance)>
    // <!ELEMENT RegistryNumber (#PCDATA)>
    // <!ELEMENT NameOfSubstance (#PCDATA)>
    static class Handler extends DelegateHandler {
        private final TextAccumulatorHandler mRegistryHandler 
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mNameHandler 
            = new TextAccumulatorHandler();
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            setDelegate(MedlineCitationSet.REGISTRY_NUMBER_ELT,
                        mRegistryHandler);
            setDelegate(MedlineCitationSet.NAME_OF_SUBSTANCE_ELT,
                        mNameHandler);
        }
        public Chemical getChemical() {
            return new Chemical(mRegistryHandler.getText(),
                                mNameHandler.getText());
        }
    }

}
