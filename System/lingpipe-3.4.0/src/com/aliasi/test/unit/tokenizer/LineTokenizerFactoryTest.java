package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.LineTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.test.unit.BaseTestCase;

import java.util.ArrayList;
import java.util.Arrays;

public class LineTokenizerFactoryTest extends BaseTestCase {


    public void testOne() {
        assertTokenizer("",
                        new String[] { },
                        new String[] { "" });

        assertTokenizer("abc",
                        new String[] { "abc" },
                        new String[] { "", "" });

        assertTokenizer("abc\n",
                        new String[] { "abc" },
                        new String[] { "", "\n" });

        assertTokenizer("  \n",
                        new String[] { "  " },
                        new String[] { "", "\n" });

        assertTokenizer("abc\n def ",
                        new String[] { "abc", " def " },
                        new String[] { "", "\n", "" });

        assertTokenizer("abc\r\ndef",
                        new String[] { "abc", "def" },
                        new String[] { "", "\r\n", "" });

        assertTokenizer("abc\rdef",
                        new String[] { "abc", "def" },
                        new String[] { "", "\r", "" });

        assertTokenizer("abc\u2029def",
                        new String[] { "abc", "def" },
                        new String[] { "", "\u2029", "" });


    }

    void assertTokenizer(String input,
                         String[] tokens,
                         String[] whitespaces) {
        LineTokenizerFactory tf = new LineTokenizerFactory();
        Tokenizer tokenizer = tf.tokenizer(input.toCharArray(),0,input.length());
        ArrayList<String> tokenList = new ArrayList<String>();
        ArrayList<String> whiteList = new ArrayList<String>();
        tokenizer.tokenize(tokenList,whiteList);
        assertEquals(Arrays.<String>asList(tokens),tokenList);
        if (whitespaces != null)
            assertEquals(Arrays.<String>asList(whitespaces),whiteList);
    }

}