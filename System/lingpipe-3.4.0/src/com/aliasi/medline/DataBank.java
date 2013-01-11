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
import java.util.Arrays;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>DataBank</code> represents a set of molecular
 * sequences registered in a particular database.  Each
 * data bank contains a name identifying the data bank
 * and a list of accession numbers uniquely identifying
 * particular sequences in that data bank.  The accession
 * numbers must be interpreted relative to a particular
 * data bank.
 *
 * <P>The following table lists the six data banks registering
 * molecular sequence data for MEDLINE.
 * 
 * <blockquote>
 * <table border='1' cellpadding='5'>
 * <tr><td><i>Abbreviation</i></td><td><i>Data Bank Name</i></td></tr>
 * <tr><td><code>GDB</code></td>
 *     <td>Johns Hopkins University Genome Data Bank</td></tr>
 * <tr><td><code>GENBANK</code></td>
 *     <td>GenBank Nucleic Acid Sequence Database</td></tr>
 * <tr><td><code>OMIM</code></td>
 *     <td>Mendelian Inheritance in Man (McKusick)</td></tr>
 * <tr><td><code>PDB</code></td>
 *     <td>Protein Data Bank</td></tr>
 * <tr><td><code>PIR</code></td>
 *     <td>Protein Identification Resource</td></tr>
 * <tr><td><code>SWISSPROT</code></td>
 *     <td>Protein Sequence Database</td></tr>
 * </table>
 * </blockquote>
 * 
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class DataBank {

    private final String mDataBankName;
    private final String[] mAccessionNumbers;

    DataBank(String dataBankname,
             String[] accessionNumbers) {
        mDataBankName = dataBankname;
        mAccessionNumbers = accessionNumbers;
    }

    /**
     * Returns the name of this data bank.  The value
     * will be one of the abbreviations drawn from the list
     * provided in the class documentation above.
     *
     * @return The name of this data bank.
     */
    public String dataBankName() {
        return mDataBankName;
    }
    
    /**
     * Returns the list of accession numbers for molecular
     * sequences in this database.  The format of the accession
     * numbers depends on the data bank.
     *
     * @return The  list of accession numbers for molecular
     * sequences in this database.
     */
    public String[] accessionNumbers() {
        return mAccessionNumbers;
    }

    /**
     * Returns a string-based representation of this data bank's
     * name and the list of accession numbers.
     *
     * @return A string-based representation of this data bank.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        sb.append("Data Bank Name=" + dataBankName());
        sb.append(" Accession Numbers=" 
                  + Arrays.asList(accessionNumbers()));
        sb.append('}');
        return sb.toString();
    }

    // <!ELEMENT DataBank (DataBankName, AccessionNumberList?)>
    // <!ELEMENT DataBankName (#PCDATA)>
    // <!ELEMENT AccessionNumberList (AccessionNumber+)>
    // <!ELEMENT AccessionNumber (#PCDATA)>
    static class Handler extends DelegateHandler {
        private final TextAccumulatorHandler mAccessionNumberHandler 
            = new TextAccumulatorHandler();
        private final TextAccumulatorHandler mDataBankNameHandler 
            = new TextAccumulatorHandler();
        private final ArrayList mAccessionNumberList = new ArrayList();
        private boolean mVisited;
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            setDelegate(MedlineCitationSet.DATA_BANK_NAME_ELT,
                        mDataBankNameHandler);
            setDelegate(MedlineCitationSet.ACCESSION_NUMBER_ELT,
                        mAccessionNumberHandler);
        }
        public void reset() {
            mVisited = false;
            mAccessionNumberList.clear();
            mDataBankNameHandler.reset();
        }
        public boolean visited() {
            return mVisited;
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(MedlineCitationSet.ACCESSION_NUMBER_ELT)) {
                mAccessionNumberList.add(mAccessionNumberHandler.getText());
            }
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
            mVisited = true;
        }
        public DataBank getDataBank() {
            String[] accessionNumbers = new String[mAccessionNumberList.size()];
            mAccessionNumberList.toArray(accessionNumbers);
            return new DataBank(mDataBankNameHandler.getText(),
                                accessionNumbers);
        }

    }

}
