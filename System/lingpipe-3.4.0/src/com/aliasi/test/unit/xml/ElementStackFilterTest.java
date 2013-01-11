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

import com.aliasi.xml.ElementStackFilter;
import com.aliasi.xml.SimpleElementHandler;

import java.util.EmptyStackException;

import org.xml.sax.SAXException;

public class ElementStackFilterTest extends BaseTestCase {

    public void test1() throws SAXException {
        MockDefaultHandler handler = new MockDefaultHandler();

        ElementStackFilter filter
            = new ElementStackFilter(handler);

        boolean threw = false;
        try {
            filter.currentElement();
        } catch (EmptyStackException e) {
            threw = true;
        }
        assertTrue(threw);
        threw = false;
        try {
            filter.currentAttributes();
        } catch (EmptyStackException e) {
            threw = true;
        }
        assertTrue(threw);

        filter.startDocument();

        threw = false;
        try {
            filter.currentElement();
        } catch (EmptyStackException e) {
            threw = true;
        }
        assertTrue(threw);
        threw = false;
        try {
            filter.currentAttributes();
        } catch (EmptyStackException e) {
            threw = true;
        }
        assertTrue(threw);

        filter.startElement(null,"foo","foo",
                            SimpleElementHandler.EMPTY_ATTS);
        assertEquals("foo",filter.currentElement());

        filter.characters("foobar");

        assertEquals("foo",filter.currentElement());
        assertEquals(SimpleElementHandler.EMPTY_ATTS,
                     filter.currentAttributes());

        filter.startElement(null,"bar","biz:baz",

                            SimpleElementHandler.EMPTY_ATTS);

        assertEquals("biz:baz",filter.currentElement());


        filter.endElement(null,"bar","biz:baz");
        filter.endElement(null,"foo","foo");
        filter.endDocument();

        threw = false;
        try {
            filter.currentElement();
        } catch (EmptyStackException e) {
            threw = true;
        }
        assertTrue(threw);
        threw = false;
        try {
            filter.currentAttributes();
        } catch (EmptyStackException e) {
            threw = true;
        }
        assertTrue(threw);

    }


}
