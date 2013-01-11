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

package com.aliasi.test.unit.util;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.util.Files;
import com.aliasi.util.Strings;
import com.aliasi.util.XML;

import com.aliasi.xml.SAXWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;

public class XMLTest extends BaseTestCase {

    public void testEscape() {
    assertEscape("","");
    assertEscape("foo","foo");
    assertEscape("f<o","f&lt;o");
    assertEscape("f>o","f&gt;o");
    assertEscape("f\"o","f&quot;o");
    assertEscape("f&o","f&amp;o");
    assertEscape("<oo","&lt;oo");
    assertEscape("fo<","fo&lt;");
    }

    public void testCharEscape() {
    assertCharEscape('a',"a");
    assertCharEscape('<',"&lt;");
    assertCharEscape('"',"&quot;");
    assertCharEscape('>',"&gt;");
    assertCharEscape('&',"&amp;");
    }

    public void testEscapeEntity() {
    StringBuffer sb = new StringBuffer();
    XML.escapeEntity("foo",sb);
    assertEquals("&foo;",sb.toString());
    }

    public void testFileFilters() throws IOException {
    File dir = Files.makeCleanDir(Files.TEMP_DIRECTORY,"foo");
    assertTrue(XML.XML_FILE_FILTER.accept(dir));
    assertFalse(XML.XML_FILE_ONLY_FILTER.accept(dir));
    File file = new File(dir,"foo.xml");
    assertTrue(XML.XML_FILE_FILTER.accept(file));
    assertTrue(XML.XML_FILE_ONLY_FILTER.accept(file));
    File fileBad = new File(dir,"bar.txt");
    assertFalse(XML.XML_FILE_FILTER.accept(fileBad));
    assertFalse(XML.XML_FILE_ONLY_FILTER.accept(fileBad));
    }

    public void testHandle() throws SAXException, IOException {
    assertHandled("<a/>");
    assertHandled("<a></a>","<a/>");
    assertHandled("<a>text</a>");
    assertHandled("<a foo=\"bar\">baz<b/><c>biz</c></a>");
    }
    
    private void assertHandled(String document) 
    throws SAXException, IOException {

    assertHandled(document,document);

    }

    private void assertHandled(String document, String resultDocExpected) 
    throws SAXException, IOException {

    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    SAXWriter writer = new SAXWriter(bytesOut,Strings.UTF8);
    XML.handle(document,writer);
    String resultDocument = new String(bytesOut.toByteArray(),
                       Strings.UTF8);
    assertEqualModPrefix(resultDocExpected,resultDocument);
    }

    private void assertEqualModPrefix(String document, String resultDocument) {
    String xmlPrefix = "<?xml version=_1.0_ encoding=_UTF-8_?>";
    int xmlPrefixLength = xmlPrefix.length();
    assertTrue(document + "\n!=\n" + resultDocument,
       document.equals(resultDocument)
       || (resultDocument.length() > xmlPrefixLength
           && document.equals(resultDocument.substring(xmlPrefixLength))));
    
    }


    private void assertCharEscape(char c, String escaped) {
    StringBuffer sb = new StringBuffer();
    XML.escape(c,sb);
    assertEquals(escaped,sb.toString());
    }

    private void assertEscape(String original, String escaped) {
    assertEquals(escaped,XML.escape(original));
    StringBuffer sb = new StringBuffer();
    XML.escape(original,sb);
    assertEquals(escaped,sb.toString());
    }


}
