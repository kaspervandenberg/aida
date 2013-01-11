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

package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.util.Strings;

import com.aliasi.test.unit.BaseTestCase;

import java.io.IOException;

public class TokenizerTest extends BaseTestCase {

    public void testAbstractTokenizerStart() throws IOException {
        Tokenizer testTokenizer = new TestTokenizer();
        boolean threw = false;
        try {
            testTokenizer.lastTokenStartPosition();
        } catch (UnsupportedOperationException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    public void testAbstractTokenizerWhitespace() throws IOException {
        Tokenizer testTokenizer = new TestTokenizer();
        for (int i = 0; i < 20; ++i)
            assertEquals(Strings.SINGLE_SPACE_STRING,
                         testTokenizer.nextWhitespace());
        for (int i = 0; i < 20; ++i) testTokenizer.nextToken();
        assertEquals(Strings.SINGLE_SPACE_STRING,
                     testTokenizer.nextWhitespace());
    }

    public void testAbstractTokenizerNext() throws IOException {
        Tokenizer testTokenizer = new TestTokenizer();
        for (int i = 1; i <= 10; ++i) {
            assertEquals(String.valueOf(i),
                         testTokenizer.nextToken());
        }
        assertNull(testTokenizer.nextToken());
    }

    public void testAbstractTokenizerToArray() throws IOException {
        Tokenizer testTokenizer = new TestTokenizer();
        String[] answer = new String[10];
        for (int i = 0; i < 10; ++i)
            answer[i] = String.valueOf(i+1);
        assertEqualsArray(answer,
                          testTokenizer.tokenize());
    }

    public void testIterability() {
        Tokenizer tokenizer = new TestTokenizer();
        int count = 0;
        for (String token : tokenizer) {
            ++count;
            assertEquals(String.valueOf(count),token);
        }
        assertEquals(10,count);
    }

    private static class TestTokenizer extends Tokenizer {
        private int count = 0;
        public String nextToken() {
            return (count++ < 10)
                ? String.valueOf(count)
                : null;
        }
    }

}
