package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.test.unit.BaseTestCase;

import java.util.ArrayList;
import java.util.Arrays;

public class ConstantTokenizerTest extends BaseTestCase {

    private final char[] EMPTY_CHARS = new char[0];

    public void testConstants() {
        String[] toks = new String[] { "John", "Smith", "rocks", "." };
        String[] whites = new String[] { "", " ", " ", " ", "" };
        TokenizerFactory tf
            = new ConstantTokenizerFactory(toks,whites);
        assertEqualsArray(toks,tf.tokenizer(EMPTY_CHARS,0,0).tokenize());

        Tokenizer t = tf.tokenizer(EMPTY_CHARS,0,0);
        ArrayList tokList = new ArrayList();
        ArrayList whiteList = new ArrayList();
        t.tokenize(tokList,whiteList);
        assertEquals(Arrays.asList(toks),tokList);
        assertEquals(Arrays.asList(whites),whiteList);
        assertNull(t.nextToken());
        assertEquals("",t.nextWhitespace());
        assertEquals("",t.nextWhitespace());
        assertNull(t.nextToken());
    }
}
