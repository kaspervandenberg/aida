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

import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.test.unit.BaseTestCase;


public class IndoEuropeanSentenceModelTest extends BaseTestCase {

    public void testBoundaries() {
        assertBoundaries(new String[] { "John", "ran", "." },
                         new String[] { "", " ", "", "" },
                         new int[] { 2 });
        assertBoundaries(new String[] { "John", "ran", ".", "Hello" },
                         new String[] { "", " ", "", " ", ""},
                         new int[] { 2 });
        assertBoundaries(new String[] { "John", "ran", ".", "Hello", "?" },
                         new String[] { "", " ", "", " ", "", "" },
                         new int[] { 2, 4 });
        assertBoundaries(new String[] { "Mr", ".", "Smith", "ran", "." },
                         new String[] { "", "", " ", " ", "", "" },
                         new int[] { 4 });
        assertBoundaries(new String[] { "Mr", ".", "Smith", "ran", "." },
                         new String[] { "", "", " ", " ", "", "" },
                         new int[] { 4 });
        assertBoundaries(new String[] { "Johnson", ",", "etc", ".", "are", "OK", "." },
                         new String[] { "", "", " ", "", " ", " ", "", " " },
                         new int[] { });
        assertBoundaries(new String[] { "\"", "John", "ran", ".", "\"" },
                         new String[] { "", "", " ", "", "", "" },
                         new int[] { 4 });
        assertBoundaries(new String[] { "\"", "John", "ran", ".",
                                        "\"", "Bill" },
                         new String[] { "", "", " ", "", " ", "", "" },
                         new int[] { 3 });
        assertBoundaries(new String[] { "\"", "Hello", "world", ".",
                                        "What", "up", "?", "\"" },
                         new String[] { "", "", " ", "", " ", " ", "", "", "" },
                         new int[] { 3, 7 });
    }

    private void assertBoundaries(String[] tokens,
                                  String[] whitespaces,
                                  int[] boundaries) {
        assertEquals(tokens.length, whitespaces.length-1);
        SentenceModel model = new IndoEuropeanSentenceModel();
        assertEqualsArray(boundaries,
                          model.boundaryIndices(tokens,whitespaces));
    }

}
