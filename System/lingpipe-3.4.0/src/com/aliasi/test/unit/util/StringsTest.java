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

import com.aliasi.util.Strings;

import com.aliasi.test.unit.BaseTestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.nio.CharBuffer;

public class StringsTest extends BaseTestCase {

    public void testHashCode() {
    testHashCode("");
    testHashCode("abc");
    testHashCode("xyz kdkdkpq984yuro8iuz");
    }

    void testHashCode(String input) {
    int expectedHash = input.hashCode();
    assertEquals(expectedHash,Strings.hashCode(input));

    StringBuffer sb = new StringBuffer();
    sb.append(input);
    assertEquals(expectedHash,Strings.hashCode(sb));

    char[] cs = input.toCharArray();
    CharBuffer buf = CharBuffer.wrap(cs);
    assertEquals(expectedHash,Strings.hashCode(buf));
    }

    public void testReverse() {
    assertReverse("","");
    assertReverse("a","a");
    assertReverse("ab","ba");
    assertReverse("abc","cba");
    }

    void assertReverse(String x, String xRev) {
    assertEquals(xRev,Strings.reverse(x));
    }

    public void testUTF8IsSupported() {
        boolean threw = false;
        try {
            new String(new byte[] { (byte)'a' },
                       Strings.UTF8);
        } catch (UnsupportedEncodingException e) {
            threw = true;
        }
        assertFalse(threw);
    }

    public void testAllLetters() {
        assertTrue(Strings.allLetters("abc".toCharArray()));
        assertTrue(Strings.allLetters("".toCharArray()));
        assertFalse(Strings.allLetters("abc1".toCharArray()));
    }

    public void testAllUpperCase() {
        assertTrue(Strings.allUpperCase("ABC".toCharArray()));
        assertTrue(Strings.allUpperCase("".toCharArray()));
        assertFalse(Strings.allUpperCase("ABC1".toCharArray()));
    }

    public void testCapitalized() {
        assertFalse(Strings.capitalized("".toCharArray()));
        assertTrue(Strings.capitalized("Abc".toCharArray()));
        assertFalse(Strings.capitalized("Abc1".toCharArray()));
    }

    public void testContainsDigits() {
        assertTrue(Strings.containsDigits("123".toCharArray()));
        assertFalse(Strings.containsDigits("".toCharArray()));
        assertTrue(Strings.containsDigits("abc1".toCharArray()));
        assertFalse(Strings.containsDigits("abc".toCharArray()));
    }

    public void testContainsLetter() {
        assertTrue(Strings.containsLetter("abc".toCharArray()));
        assertFalse(Strings.containsLetter("".toCharArray()));
        assertTrue(Strings.containsLetter("abc1".toCharArray()));
        assertFalse(Strings.containsLetter("123".toCharArray()));
    }

    public void testAllPunctuation() {
        assertTrue(Strings.allPunctuation(";..?!".toCharArray()));
        assertTrue(Strings.allPunctuation("".toCharArray()));
        assertFalse(Strings.allPunctuation("\".".toCharArray()));
    }

    public void testAllPunctuationString() {
        assertTrue(Strings.allPunctuation(";..?!"));
        assertTrue(Strings.allPunctuation(""));
        assertFalse(Strings.allPunctuation("\"."));
    }

    public void testAllSymbols() {
        assertTrue(Strings.allSymbols(";..?!".toCharArray()));
        assertTrue(Strings.allSymbols("".toCharArray()));
        assertTrue(Strings.allSymbols("\".".toCharArray()));
        assertFalse(Strings.allSymbols("$%^&*abc".toCharArray()));
    }

    public void testSave() {
        String s1 = "";
        String s2 = "abc def";
        assertFullEquals(s1,Strings.save(s1));
        assertFullEquals(s2,Strings.save(s2));
    }

    public void testContainsChar() {
        assertTrue(Strings.containsChar("abc",'a'));
        assertTrue(Strings.containsChar("abc",'b'));
        assertTrue(Strings.containsChar("abc",'c'));
        assertFalse(Strings.containsChar("abc",'d'));
        assertFalse(Strings.containsChar("",'a'));
    }

    public void testAllWhitespace() {
        assertTrue(Strings.allWhitespace(""));
        assertTrue(Strings.allWhitespace(" \n \t"));
        assertFalse(Strings.allWhitespace("  a  "));
    }

    public void testAllWhitespaceSB() {
        assertTrue(Strings.allWhitespace(new StringBuffer("")));
        assertTrue(Strings.allWhitespace(new StringBuffer(" \n \t")));
        assertFalse(Strings.allWhitespace(new StringBuffer("  a  ")));
    }

    public void testAllWhitespaceArray() {
        assertTrue(Strings.allWhitespace("".toCharArray(),0,0));
        assertTrue(Strings.allWhitespace(" \n \t ".toCharArray(),0,3));
        assertTrue(Strings.allWhitespace("     a  ".toCharArray(),1,2));
        assertFalse(Strings.allWhitespace("     a  ".toCharArray(),3,3));
    }


    public void testAllDigits() {
        assertTrue(Strings.allDigits(""));
        assertTrue(Strings.allDigits("123"));
        assertFalse(Strings.allDigits("1.23"));
        assertFalse(Strings.allDigits("1ab"));
    }

    public void testAllDigitsArray() {
        assertTrue(Strings.allDigits("".toCharArray(),0,0));
        assertTrue(Strings.allDigits("123".toCharArray(),0,3));
        assertFalse(Strings.allDigits("1.23".toCharArray(),0,4));
        assertFalse(Strings.allDigits("1ab".toCharArray(),0,3));
    }

    public void testIsWhitespace() {
        assertTrue(Strings.isWhitespace((char)160));
        assertTrue(Strings.isWhitespace(' '));
        assertTrue(Strings.isWhitespace('\n'));
        assertFalse(Strings.isWhitespace('a'));
    }

    public void testIsPunctuation() {
        assertTrue(Strings.isPunctuation('!'));
        assertTrue(Strings.isPunctuation('?'));
        assertTrue(Strings.isPunctuation(';'));
        assertFalse(Strings.isPunctuation('"'));
        assertFalse(Strings.isPunctuation('a'));
    }

    public void testPower() {
        assertEquals("",Strings.power("abc",0));
        assertEquals("",Strings.power("",3));
        assertEquals("aaa",Strings.power("a",3));
    }

    public void testConcatenateObjectArray() {
        assertEquals("a b",Strings.concatenate(new Object[] { "a", "b" }));
        assertEquals("a b c",
                     Strings.concatenate(new Object[] { "a", "b", "c" }));
        assertEquals("",Strings.concatenate(new Object[] { }));
    }

    public void testConcatenateObjectArraySpacer() {
        assertEquals("a,b",
                     Strings.concatenate(new Object[] { "a", "b" },
                                         ","));
        assertEquals("abc",
                     Strings.concatenate(new Object[] { "a", "b", "c" },
                                         ""));
        assertEquals("",Strings.concatenate(new Object[] { }, " "));
    }

    public void testConcatenateObjectArrayStartSpacer() {
        assertEquals("a,b",
                     Strings.concatenate(new Object[] { "a", "b" },
                                         0, ","));
        assertEquals("abc",
                     Strings.concatenate(new Object[] { "e", "a", "b", "c" },
                                         1, ""));
        assertEquals("",Strings.concatenate(new Object[] { },
                                            15, " "));
    }

    public void testConcatenateObjectArrayStartEndSpacer() {
        assertEquals("a,b",
                     Strings.concatenate(new Object[] { "a", "b" },
                                         0, 2, ","));
        assertEquals("abc",
                     Strings.concatenate(new Object[] { "e", "a", "b",
                                                        "c", "f" },
                                         1, 4, ""));
    }

    public void testIndent() {
        StringBuffer sb = new StringBuffer();
        Strings.indent(sb,3);
        assertEquals(sb.toString(),"\n   ");
        sb = new StringBuffer();
        Strings.indent(sb,0);
        assertEquals(sb.toString(),"\n");
    }

    public void testFit() {
        assertEquals("",Strings.fit("",0));
        assertEquals("abc ",Strings.fit("abc",4));
        assertEquals("abc",Strings.fit("abcd",3));
    }

    public void testPadding() {
        assertEquals("",Strings.padding(0));
        assertEquals("  ",Strings.padding(2));
    }

    public void testPaddingSB() {
        StringBuffer sb = new StringBuffer();
        Strings.padding(sb,0);
        assertEquals("",sb.toString());
        sb = new StringBuffer();
        Strings.padding(sb,2);
        assertEquals("  ",sb.toString());
    }

    public void testFunctionArgs() {
        assertEquals("a()",Strings.functionArgs("a",new Object[] { }));
        assertEquals("a(1)",
                     Strings.functionArgs("a",
                                          new Object[] { new Integer(1) }));
        assertEquals("a(1,b)",
                     Strings.functionArgs("a",
                                          new Object[] { new Integer(1),
                                                         "b" }));
    }

    public void testArgsList() {
        assertEquals("()",Strings.functionArgsList(new Object[] { }));
        assertEquals("(a)", Strings.functionArgsList(new Object[] { "a" }));
        assertEquals("(a,b)", Strings.functionArgsList(new Object[] { "a",
                                                                      "b" }));
    }

    public void testTitleCase() {
        assertEquals("",Strings.titleCase(""));
        assertEquals("A",Strings.titleCase("a"));
        assertEquals("1ab",Strings.titleCase("1ab"));
        assertEquals("Abc",Strings.titleCase("abc"));
    }

    public void testConstants() {
        assertEquals(160,Strings.NBSP_CHAR);
        assertEquals('\n',Strings.NEWLINE_CHAR);
        assertEquals(' ',Strings.DEFAULT_SEPARATOR_CHAR);
        assertEquals(" ",Strings.DEFAULT_SEPARATOR_STRING);

    }

    public void testSplit() {
        assertEqualsArray(new String[] { "" },
                          Strings.split("",' '));

        assertEqualsArray(new String[] { "a" },
                          Strings.split("a",' '));

        assertEqualsArray(new String[] { "a", "" },
                          Strings.split("a ",' '));

        assertEqualsArray(new String[] { "", "a" },
                          Strings.split(" a",' '));

        assertEqualsArray(new String[] { "", "a", "" },
                          Strings.split(" a ",' '));

        assertEqualsArray(new String[] { "", "aa", "" },
                          Strings.split(" aa ",' '));
        assertEqualsArray(new String[] { "a", "b" },
                          Strings.split("a b",' '));
        assertEqualsArray(new String[] { "a", "b", "c" },
                          Strings.split("a b c",' '));

        assertEqualsArray(new String[] { "aaa" },
                          Strings.split("aaa",' '));
        assertEqualsArray(new String[] { "aaa", "bb" },
                          Strings.split("aaa bb",' '));
        assertEqualsArray(new String[] { "aaa", "bb", "c" },
                          Strings.split("aaa bb c",' '));

    }


    public void testArrayIO() throws IOException {
        assertArrayIO(new String[] { });
        assertArrayIO(new String[] { "a" });
        assertArrayIO(new String[] { "a", "bcd", "efgh" });
    }

    private void assertArrayIO(String[] testCase) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(bytesOut);
        Strings.writeArrayTo(dataOut,testCase);
        byte[] bytes = bytesOut.toByteArray();
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        DataInputStream dataIn = new DataInputStream(bytesIn);
        String[] roundTripResult = Strings.readArrayFrom(dataIn);
        assertEqualsArray(testCase,roundTripResult);
    }

    public void testBytesToHex() {
        assertEquals("",Strings.bytesToHex(new byte[] { }));
        assertEquals("00",Strings.bytesToHex(new byte[] { (byte)0 }));
        assertEquals("0000",Strings.bytesToHex(new byte[] { (byte)0, (byte)0 }));
        assertEquals("ff0f",Strings.bytesToHex(new byte[] { (byte)-1, (byte)15 }));
    }

    public void testByteToHex() {
        assertEquals("ff",Strings.byteToHex((byte)-1));
        assertEquals("0f",Strings.byteToHex((byte)15));
    }

    public void testMsToString() {
        assertEquals(":00",Strings.msToString(0));
        assertEquals(":00",Strings.msToString(999));
        assertEquals(":01",Strings.msToString(1001));
        assertEquals(":32",Strings.msToString(32000));
        assertEquals("1:01",Strings.msToString(61000));
        assertEquals("3:12:03",Strings.msToString((60*60*3 + 60*12 + 3)*1000));
        assertEquals("33:00:00",Strings.msToString((60*60*33)*1000));
    }

    public void testDecimalFormat() {
        assertEquals("2,798.39",Strings.decimalFormat(2798.389,"#,##0.00",8));
        assertEquals(" 2,798.39",Strings.decimalFormat(2798.389,"#,##0.00",9));
        assertEquals("2798.39",Strings.decimalFormat(2798.389,"#0.00",7));
        assertEquals("  2798.39",Strings.decimalFormat(2798.389,"#0.00",9));
        assertEquals("2,798.39",Strings.decimalFormat(2798.389,"#,###.00",8));
        assertEquals(" 2798.39",Strings.decimalFormat(2798.389,"#.00",8));

        assertEquals(" .39",Strings.decimalFormat(0.39,"#.00",4));
        assertEquals("0.39",Strings.decimalFormat(0.39,"#0.00",4));
        assertEquals(" .39",Strings.decimalFormat(0.39,"#,###.00",4));
        assertEquals("0.39",Strings.decimalFormat(0.39,"#,##0.00",4));

        assertEquals("-0.39",Strings.decimalFormat(-0.39,"#,##0.00",5));

        assertEquals("???",Strings.decimalFormat(127.9,"#,##0.00",3));

        try {
            Strings.decimalFormat(1,"#,0,!",4);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            Strings.decimalFormat(1,"#,##0",-2);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }
    }

    public void testNormalizeWhitespace() {
    assertWhitespaceNormalized("abc","abc");
    assertWhitespaceNormalized("abc de fg"," abc de  \t fg\n\n");
    assertWhitespaceNormalized("a b"," a\tb\n");
    assertWhitespaceNormalized("a b","a\t\t\t b");
    assertWhitespaceNormalized("","");
    assertWhitespaceNormalized(""," ");
    }

    private void assertWhitespaceNormalized(String expected, String input) {
    StringBuffer sb = new StringBuffer();
    Strings.normalizeWhitespace(input,sb);
    assertEquals(expected,sb.toString());
    }

    public void testEqualsCharSeqs() {
    StringBuffer sb1 = new StringBuffer();
    StringBuffer sb2 = new StringBuffer();
    assertTrue(Strings.equalCharSequence(sb1,sb2));
    sb1.append("abc");
    assertFalse(Strings.equalCharSequence(sb1,sb2));
    assertTrue(Strings.equalCharSequence("abc",sb1));
    }

}
