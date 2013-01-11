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

import com.aliasi.corpus.Handler;
import com.aliasi.corpus.Parser;
import com.aliasi.corpus.XMLParser;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import com.aliasi.util.XML;

import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;


/**
 * A <code>MedlineParser</code> is able to parse 2008 MEDLINE citations
 * from an input source.  The parser takes a visitor in the form of a
 * <code>MedlineHandler</code>, which processes the MEDLINE citations
 * as they are extracted by the parser.
 *
 * <P>The parser is able to store the entire raw XML form of the
 * original citation.  Whether it does so is controlled by a flag in
 * the constructor.
 *
 * <P>For more information, see the LingPipe tutorial on parsing MEDLINE.
 *
 * @author Bob Carpenter
 * @version 3.3.0
 * @since   LingPipe2.0
 */
public class MedlineParser
    extends XMLParser<MedlineHandler> {

    private final boolean mIncludeRawXML;

    /**
     * Construct a MEDLINE parser no initial handler.
     *
     * @param includeRawXML Set to <code>true</code> if the raw XML
     * will be available as part of the processed citation.
     */
    public MedlineParser(boolean includeRawXML) {
        this(null,includeRawXML);
    }

    /**
     * Construct a MEDLINE parser with the specified handler.
     *
     * @param handler Handler for the citations.
     * @param includeRawXML Set to <code>true</code> if the raw XML
     * will be available as part of the processed citation.
     */
    public MedlineParser(MedlineHandler handler,
                         boolean includeRawXML) {
        super(handler);
        mIncludeRawXML = includeRawXML;
    }

    /**
     * Parse the MEDLINE citations in the specified input source
     * and give them to the specified handler for processing.
     *
     * @deprecated Use <code>setHandler(MedlineHandler)</code> followed
     * by <code>parse(InputSource)</code>.
     * @param inSource Input source to parse.
     * @param handler MEDLINE handler for processing citations.
     */
    public void parse(InputSource inSource,
                      MedlineHandler handler)
        throws IOException, SAXException {

        setHandler(handler);
        parse(inSource);
    }

    protected DefaultHandler getXMLHandler() {
        return new VisitingHandler(getHandler(),
                                   mIncludeRawXML);
    }


    private static class DeletionHandler extends DelegateHandler {
        TextAccumulatorHandler mPMIDHandler
            = new TextAccumulatorHandler();
        Set mDeletions = new HashSet();
        public DeletionHandler(DelegatingHandler delegator) {
            super(delegator);
            setDelegate(MedlineCitationSet.PMID_ELT,
                        mPMIDHandler);
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            mPMIDHandler.reset();
            mDeletions = new HashSet();
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(MedlineCitationSet.PMID_ELT))
                mDeletions.add(mPMIDHandler.getText());
        }
        public Set deletionSet() {
            return mDeletions;
        }
    }

    private static class VisitingHandler extends DelegatingHandler {
        private final MedlineHandler mVisitor;
        private StringBuffer mXMLBuffer = new StringBuffer();
        private final boolean mSaveXML;
        private final DeletionHandler mDeletionHandler;
        public VisitingHandler(MedlineHandler visitor) {
            this(visitor,true);
        }
        public VisitingHandler(MedlineHandler visitor,
                               boolean saveXML) {
            mVisitor = visitor;
            mSaveXML = saveXML;
            setDelegate(MedlineCitationSet.MEDLINE_CITATION_ELT,
                        new MedlineCitation.Handler(this));
            mDeletionHandler = new DeletionHandler(this);
            setDelegate(MedlineCitationSet.DELETE_CITATION_ELT,mDeletionHandler);
        }
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts)
            throws SAXException {

            if (mSaveXML) {
                if (qName.equals(MedlineCitationSet.MEDLINE_CITATION_ELT)) {
                    mXMLBuffer = new StringBuffer();
                    mXMLBuffer.append("<MedlineCitation");
                    appendAttributes(atts);
                    mXMLBuffer.append('>');
                } else {
                    mXMLBuffer.append('<');
                    mXMLBuffer.append(qName);
                    appendAttributes(atts);
                    mXMLBuffer.append('>');
                }
            }
            super.startElement(namespaceURI,localName,qName,atts);
        }
        public void endElement(String namespaceURI, String localName,
                               String qName) throws SAXException {
            if (mSaveXML) {
                mXMLBuffer.append("</");
                mXMLBuffer.append(qName);
                mXMLBuffer.append('>');
            }
            super.endElement(namespaceURI,localName,qName);
        }
        public void characters(char[] ch, int start, int length)
            throws SAXException {

            if (mSaveXML) {
                appendCharacters(ch,start,length);
            }
            super.characters(ch,start,length);
        }
        public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException {

            if (!DTD_MAP.containsKey(publicId)) {
                System.out.println("Can't find local Entity="
                                   + publicId);
                System.out.println("Looking for systemId="
                                   + systemId);
                return super.resolveEntity(publicId,systemId);
            }
            String fileName = DTD_MAP.get(publicId).toString();
            InputStream in = this.getClass().getResourceAsStream(fileName);
            return new InputSource(in);
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(MedlineCitationSet.MEDLINE_CITATION_ELT)) {
                MedlineCitation.Handler citationHandler
                    = (MedlineCitation.Handler)handler;
                MedlineCitation citation = citationHandler.getCitation();
                if (mSaveXML)
                    citation.setXMLString(mXMLBuffer.toString());
                mVisitor.handle(citation);
            }
            if (qName.equals(MedlineCitationSet.DELETE_CITATION_ELT)) {
                Set deletedPmidSet = mDeletionHandler.deletionSet();
                Iterator it = deletedPmidSet.iterator();
                while (it.hasNext()) {
                    String pmid = it.next().toString();
                    mVisitor.delete(pmid);
                }
            }
        }
        private void appendAttributes(Attributes atts) {
            for (int i = 0; i < atts.getLength(); ++i) {
                mXMLBuffer.append(' ');
                mXMLBuffer.append(atts.getQName(i));
                mXMLBuffer.append("=\"");
                char[] cs = atts.getValue(i).toCharArray();
                appendCharacters(cs,0,cs.length);
                mXMLBuffer.append('"');
            }
        }
        private void appendCharacters(char[] cs, int start, int length) {
            for (int i = 0; i < length; ++i) {
                char c = cs[start+i];
                switch (c) {
                case '<':  { mXMLBuffer.append("&lt;"); break; }
                case '>':  { mXMLBuffer.append("&gt;"); break; }
                case '&':  { mXMLBuffer.append("&amp;"); break; }
                case '"':  { mXMLBuffer.append("&quot;"); break; }
                default:   { mXMLBuffer.append(c); }
                }
            }
        }
        private static final HashMap DTD_MAP
            = new HashMap();
        static {
            // "-//NLM//DTD Medline Citation, 1st January 2008//EN"
            // "-//NLM//DTD Medline Citation, 1st January 2008//EN",
            DTD_MAP.put(MedlineCitationSet.NLM_MEDLINE_DTD_NAME,
                        "nlmmedline_080101.dtd");

            // "-//NLM//DTD MedlineCitation, 1st January 2008//EN";
            DTD_MAP.put(MedlineCitationSet.NLM_MEDLINE_CITATION_DTD_NAME,
                        "nlmmedlinecitation_080101.dtd");

            // "-//NLM//DTD SharedCatCit, 1st January 2008//EN",
            DTD_MAP.put(MedlineCitationSet.NLM_SHARED_CAT_DTD_NAME,
                        "nlmsharedcatcit_080101.dtd");

            // "-//NLM//DTD Common, 1st January 2008//EN",
            DTD_MAP.put(MedlineCitationSet.NLM_COMMON_DTD_NAME,
                        "nlmcommon_080101.dtd");

        }

    }


}
