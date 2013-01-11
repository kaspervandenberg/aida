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

import com.aliasi.util.Iterators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>DataBankList</code> contains linkages of
 * molecular sequences mentioned in articles to their data bank
 * name and accession numbers.  Some of these references do
 * not appear directly in the articles, but have been added by NLM.
 * NLM includes all data bank information present in the articles.
 *
 * <P>Results are returned as an array of data banks, each data bank
 * containing linkages for a particular data source.  The returned
 * array may be of length zero if there are no linkages for the
 * record.  There are eight data banks that are currently used, as
 * described in the class documentation for {@link DataBank}.  The
 * list of data bank linkages may be incomplete; the method {@link
 * #complete()} may be used to test.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public class DataBankList implements Iterable<DataBank> {

    private final boolean mComplete;
    private final DataBank[] mDataBanks;

    DataBankList(boolean complete,
                 DataBank[] dataBanks) {
        mComplete = complete;
        mDataBanks = dataBanks;
    }

    /**
     * Returns an iterator over the data banks in this list.  This
     * method provides the implementation for
     * <code>Iterable&lt;DataBank&gt;</code>, which allows general
     * for-loops over data banks to be used.
     *
     * @return An iterator over the data banks in this list.
     */
    public Iterator<DataBank> iterator() {
        return new Iterators.Array<DataBank>(mDataBanks);
    }

    /**
     * Returns <code>true</code> if the list of databank numbers in
     * the citation is complete.  From 2000 onward, the lists include
     * every mention in the article.  In addition, NLM has added data
     * bank linkages for molecular sequences mentioned in the article
     * where the linkage was not present in the original article.
     *
     * @return <code>true</code> if the databank numbers in the
     * citation is complete.
     */
    public boolean complete() {
        return mComplete;
    }

    /**
     * Return the list of data bank linkages for molecular
     * sequences mentioned in this article.  
     *
     * @return The list of data bank linkages for molecular
     * sequences mentioned in this article.  
     */
    public DataBank[] dataBanks() {
        return mDataBanks;
    }

    /**
     * Returns a string-based representation of this data bank list.
     * 
     * @return A string-based representation of this data bank list.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        sb.append("Complete=");
        sb.append(complete());
        sb.append(" DataBanks=");
        sb.append(Arrays.asList(dataBanks()));
        sb.append('}');
        return sb.toString();
    }

    // <!ELEMENT DataBankList (DataBank+)>
    // <!ATTLIST DataBankList
    //           CompleteYN (Y | N) "Y">
    static class Handler extends DelegateHandler {
        private boolean mComplete;
        private final ArrayList mDataBankList = new ArrayList();
        private final DataBank.Handler mDataBankHandler;
        private boolean mVisited;
        public Handler(DelegatingHandler delegator) {
            super(delegator);
            mDataBankHandler = new DataBank.Handler(delegator);
            setDelegate(MedlineCitationSet.DATA_BANK_ELT,
                        mDataBankHandler);
        }
        public void reset() {
            mVisited = false;
            mDataBankList.clear();
            mDataBankHandler.reset();
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
            mVisited = true;
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(MedlineCitationSet.DATA_BANK_ELT)) {
                mDataBankList.add(mDataBankHandler.getDataBank());
            }
        }

        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts)
            throws SAXException {

            super.startElement(namespaceURI,localName,qName,atts);
            if (qName.equals(MedlineCitationSet.DATA_BANK_LIST_ELT)) {
                mComplete
                    = MedlineCitationSet
                    .YES_VALUE
                    .equals(atts.getValue(MedlineCitationSet.COMPLETE_YN_ATT));
            }

        }
        public DataBankList getDataBankList() {
            if (!mVisited) return null;
            DataBank[] dataBanks = new DataBank[mDataBankList.size()];
            mDataBankList.toArray(dataBanks);
            return new DataBankList(mComplete,dataBanks);
        }
    }

}
