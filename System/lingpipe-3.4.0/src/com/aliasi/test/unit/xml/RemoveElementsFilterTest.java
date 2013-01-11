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

package com.aliasi.test.unit.xml;

import com.aliasi.test.unit.BaseTestCase;
import com.aliasi.test.unit.MockObjectHelper;

import com.aliasi.xml.RemoveElementsFilter;
import com.aliasi.xml.SimpleElementHandler;

import org.xml.sax.SAXException;

public class RemoveElementsFilterTest extends BaseTestCase {

    public void test1() throws SAXException {
        MockDefaultHandler handler = new MockDefaultHandler();
        MockObjectHelper helper = new MockObjectHelper();

        RemoveElementsFilter filter
            = new RemoveElementsFilter();
        filter.setHandler(handler);

        filter.startDocument();
        helper.add("startDocument");

        filter.startElement(null,"foo","foo",
                            SimpleElementHandler.EMPTY_ATTS);
        helper.add("startElement",null,"foo","foo",
                   MockDefaultHandler.EMPTY_ATTS_STRING);

        filter.endElement(null,"foo","foo");
        helper.add("endElement",null,"foo","foo");

        filter.endDocument();
        helper.add("endDocument");

        assertEquals(helper.getCalls(),handler.getCalls());
    }


    public void test2() throws SAXException {
        MockDefaultHandler handler = new MockDefaultHandler();
        MockObjectHelper helper = new MockObjectHelper();

        RemoveElementsFilter filter
            = new RemoveElementsFilter();
        filter.setHandler(handler);
        filter.removeElement("bar");

        filter.startDocument();
        helper.add("startDocument");

        filter.startElement(null,"foo","foo",
                            SimpleElementHandler.EMPTY_ATTS);
        helper.add("startElement",null,"foo","foo",
                   MockDefaultHandler.EMPTY_ATTS_STRING);

        filter.startElement(null,"bar","bar",
                            SimpleElementHandler.EMPTY_ATTS);
        filter.endElement(null,"bar","bar");

        filter.endElement(null,"foo","foo");
        helper.add("endElement",null,"foo","foo");

        filter.endDocument();
        helper.add("endDocument");

        assertEquals(helper.getCalls(),handler.getCalls());
    }



}
