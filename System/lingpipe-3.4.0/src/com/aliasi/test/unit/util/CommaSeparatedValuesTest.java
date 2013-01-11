package com.aliasi.test.unit.util;

import com.aliasi.util.CommaSeparatedValues;

import com.aliasi.test.unit.BaseTestCase;

import java.io.*;

public class CommaSeparatedValuesTest extends BaseTestCase {

    public void testUnquoted() throws IOException {

        assertCsv("", new String[][] { });

        assertCsv("a",new String[][] { { "a" } });
        assertCsv("a,b",new String[][] { { "a", "b" } });
        assertCsv("a,b\nc",new String[][] { { "a", "b" },
                                            { "c"} });

        String input1 = "aa,b,c\nd,e,f";
        String[][] expected1 = {
            { "aa", "b", "c" },
            { "d", "e", "f" }
        };
        assertCsv(input1,expected1);

        String input2 = " aa ,b,  c\nd  ,e  ,   f";
        String[][] expected2 = expected1;
        assertCsv(input2,expected2);

        try {
            assertCsv("a\",b",null); // illegal quote
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        assertEqualsCsv("1997,Ford,E350",
                        "1997,   Ford   , E350");


    }

    public void testQuoted() throws IOException {
        assertEqualsCsv("1997,Ford,E350",
                        "\"1997\",Ford,E350");

        // from Wikipedia example
        String in = "1997,Ford,E350,\"ac, abs, moon\",3000.00"
            + "\n"
            + "1999,Chevy,\"Venture \"\"Extended Edition\"\"\",4900.00"
            + "\n"
            + "1996,Jeep,Grand Cherokee,\"MUST SELL!\nair, moon roof, loaded\",4799.00";
        String[][] expected = {
            {"1997", "Ford", "E350","ac, abs, moon","3000.00"},
            {"1999","Chevy","Venture \"Extended Edition\"","4900.00"},
            {"1996","Jeep","Grand Cherokee","MUST SELL!\nair, moon roof, loaded","4799.00"}
        };
        assertCsv(in,expected);

        try {
            assertCsv("\"abc",null); // premature quote end
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            assertCsv("\"abc\"  d,e",null); // unexpected chars after close quote
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

    }


    void assertEqualsCsv(String input1, String input2) {
        try {
            Reader reader1 = new CharArrayReader(input1.toCharArray());
            CommaSeparatedValues csv
                = new CommaSeparatedValues(reader1);
            String[][] expected = csv.getArray();
            assertCsv(input2,expected);
        } catch (IOException e) {
            fail("IOException=" + e);
            e.printStackTrace(System.out);
        }
    }


    void assertCsv(String input, String[][] expected) {
        try {
            Reader reader = new CharArrayReader(input.toCharArray());
            CommaSeparatedValues csv
                = new CommaSeparatedValues(reader);
            assertVals(csv,expected);
        } catch (IOException e) {
            fail("IOException=" + e);
            e.printStackTrace(System.out);
        }
    }

    void assertVals(CommaSeparatedValues csv,
                    String[][] expected) {

        String[][] found = csv.getArray();
        assertEquals(found.length,expected.length);
        for (int i = 0; i < expected.length; ++i)
            assertEqualsArray(expected[i],found[i]);
    }

}