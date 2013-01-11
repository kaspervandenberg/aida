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

package com.aliasi.test.unit.sentences;

import com.aliasi.sentences.HeuristicSentenceModel;
import com.aliasi.sentences.SentenceAnnotateFilter;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.test.unit.BaseTestCase;
import com.aliasi.test.unit.MockObjectHelper;

import com.aliasi.test.unit.xml.MockDefaultHandler;

import com.aliasi.xml.GroupCharactersFilter;

import java.util.HashSet;

import org.xml.sax.SAXException;

public class SentenceAnnotateFilterTest extends BaseTestCase {

    public void testOne() throws SAXException {

        MockObjectHelper helper = new MockObjectHelper();

        SentenceModel model = getModel();
        TokenizerFactory tokenizerFactory = new IndoEuropeanTokenizerFactory();
        SentenceAnnotateFilter filter
            = new SentenceAnnotateFilter(model,tokenizerFactory,
                                         new String[] { "p" });
        filter.filterElement("p");
        MockDefaultHandler handler = new MockDefaultHandler();

        filter.setHandler(new GroupCharactersFilter(handler));
        filter.startDocument();
        helper.add("startDocument");

        filter.startSimpleElement("document");
        helper.add("startElement",null,"document","document","");

        filter.startSimpleElement("foo");
        helper.add("startElement",null,"foo","foo","");

        filter.characters("John ran.");
        helper.add("characters","John ran.");

        filter.endSimpleElement("foo");
        helper.add("endElement",null,"foo","foo");

        filter.startSimpleElement("p");
        helper.add("startElement",null,"p","p","");

        helper.add("startElement",null,"sent","sent","");

        filter.characters("John ran.");
        helper.add("characters","John ran.");

        helper.add("endElement",null,"sent","sent");

        filter.endSimpleElement("p");
        helper.add("endElement",null,"p","p");

        filter.endSimpleElement("document");
        helper.add("endElement",null,"document","document");

        filter.endDocument();
        helper.add("endDocument");
        Object[] helperCalls = helper.getCalls().toArray();
        Object[] handlerCalls = handler.getCalls().toArray();
        assertNotNull(helperCalls);
        assertNotNull(handlerCalls);
        assertEqualsArray(helperCalls,handlerCalls);
    }

    public void testTwo() throws SAXException {

        MockObjectHelper helper = new MockObjectHelper();

        SentenceModel model = getModel();
        TokenizerFactory tokenizerFactory = new IndoEuropeanTokenizerFactory();
        SentenceAnnotateFilter filter
            = new SentenceAnnotateFilter(model,tokenizerFactory,
                                         new String[] { "p" });
        filter.filterElement("p");
        MockDefaultHandler handler = new MockDefaultHandler();

        filter.setHandler(new GroupCharactersFilter(handler));
        filter.startDocument();
        helper.add("startDocument");

        filter.startSimpleElement("document");
        helper.add("startElement",null,"document","document","");

        filter.startSimpleElement("p");
        helper.add("startElement",null,"p","p","");

        filter.startSimpleElement("foo");
        helper.add("startElement",null,"foo","foo","");

        filter.characters("John ran.");
        helper.add("characters","John ran.");

        filter.endSimpleElement("foo");
        helper.add("endElement",null,"foo","foo");

        filter.endSimpleElement("p");
        helper.add("endElement",null,"p","p");

        filter.endSimpleElement("document");
        helper.add("endElement",null,"document","document");

        filter.endDocument();
        helper.add("endDocument");
        Object[] helperCalls = helper.getCalls().toArray();
        Object[] handlerCalls = handler.getCalls().toArray();
        assertNotNull(helperCalls);
        assertNotNull(handlerCalls);
        assertEqualsArray(helperCalls,handlerCalls);
    }

    public void testThree() throws SAXException {

        MockObjectHelper helper = new MockObjectHelper();

        SentenceModel model = getModel();
        TokenizerFactory tokenizerFactory = new IndoEuropeanTokenizerFactory();
        SentenceAnnotateFilter filter
            = new SentenceAnnotateFilter(model,tokenizerFactory);
        filter.filterElement("p");
        MockDefaultHandler handler = new MockDefaultHandler();

        filter.setHandler(new GroupCharactersFilter(handler));
        filter.startDocument();
        helper.add("startDocument");

        filter.startSimpleElement("document");
        helper.add("startElement",null,"document","document","");

        filter.startSimpleElement("p");
        helper.add("startElement",null,"p","p","");

        filter.characters("a b stop C d stop");
        helper.add("startElement",null,"sent","sent","");
        helper.add("characters","a b stop");
        helper.add("endElement",null,"sent","sent");
        helper.add("characters"," ");
        helper.add("startElement",null,"sent","sent","");
        helper.add("characters","C d stop");
        helper.add("endElement",null,"sent","sent");

        filter.endSimpleElement("p");
        helper.add("endElement",null,"p","p");

        filter.endSimpleElement("document");
        helper.add("endElement",null,"document","document");

        filter.endDocument();
        helper.add("endDocument");
        Object[] helperCalls = helper.getCalls().toArray();
        Object[] handlerCalls = handler.getCalls().toArray();
        assertNotNull(helperCalls);
        assertNotNull(handlerCalls);
        assertEqualsArray(helperCalls,handlerCalls);
    }

    public void testFour() throws SAXException {

        MockObjectHelper helper = new MockObjectHelper();

        SentenceModel model = getModel();
        TokenizerFactory tokenizerFactory = new IndoEuropeanTokenizerFactory();
        SentenceAnnotateFilter filter
            = new SentenceAnnotateFilter(model,tokenizerFactory,
                                         new String[] { "p" });
        MockDefaultHandler handler = new MockDefaultHandler();

        filter.setHandler(new GroupCharactersFilter(handler));
        filter.startDocument();
        helper.add("startDocument");

        filter.startSimpleElement("document");
        helper.add("startElement",null,"document","document","");

        filter.startSimpleElement("p");
        helper.add("startElement",null,"p","p","");

        filter.characters("   ");
        helper.add("characters","   ");

        filter.endSimpleElement("p");
        helper.add("endElement",null,"p","p");

        filter.endSimpleElement("document");
        helper.add("endElement",null,"document","document");

        filter.endDocument();
        helper.add("endDocument");
        Object[] helperCalls = helper.getCalls().toArray();
        Object[] handlerCalls = handler.getCalls().toArray();
        assertNotNull(helperCalls);
        assertNotNull(handlerCalls);
        assertEqualsArray(helperCalls,handlerCalls);
    }

    private static SentenceModel getModel() {
        HashSet stops = new HashSet();
        stops.add("stop");
        HashSet badPrevious = new HashSet();
        badPrevious.add("badPrevious");
        HashSet badFollowing = new HashSet();
        badFollowing.add("badFollowing");
        return new HeuristicSentenceModel(stops,badPrevious,badFollowing);
    }
}
