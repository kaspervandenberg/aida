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

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.test.unit.BaseTestCase;

import java.util.HashSet;

public class MedlineSentenceModelTest extends BaseTestCase {

    public void testBoundaries() {
        assertBoundaries(new String[] { },
                         new String[] { "" },
                         new int[] { });
        assertBoundaries(new String[] { "." },
                         new String[] { "", ""},
                         new int[] { 0 });
        assertBoundaries(new String[] { "p53", "." },
                         new String[] { " ", " ", ""},
                         new int[] { 1 });

        assertBoundaries(new String[] { "p53", "proteins", "." },
                         new String[] { " ", " ", " ", ""},
                         new int[] { 2 });

        assertBoundaries(new String[] { "p", "-", "53", "proteins", ".", "A", "b" },
                         new String[] { "", "", "", " ", "", " ", " ", " "},
                         new int[] { 4, 6});

        assertBoundaries(new String[] { "Alpha", "bravo", ".", "abc", "def", "C"},
                         new String[] { "", " ", "", " ", "", " ", " "},
                         new int[] { 5 });

        assertBoundaries(new String[] { "(", "J", ".", "Child", "Dev", "P", ".", "53", ")", "." },
                         new String[] {    "", "", " ", " ", " ", "", " ", "", "", "", ""},
                         new int[] { 9 });

    assertBoundaries(new String[] { "Apple", ")", "bravo", ".", "Charlie", "Tango" },
                         new String[] { "", " ", " ", " ", " ", " ", ""},
                         new int[] { 3, 5 });

    assertBoundaries(new String[] { "Apple", "(", "Bravo", ".", "Charlie", ")", "Dog", "." },
                         new String[] { "", " ", " ", " ", " ", " ", " ", " ", " " },
                         new int[] { 7 });

    assertBoundaries(new String[] { "Apple", "Bravo", ".", "Charlie" },
                         new String[] { "", " ", "", "", ""},
                         new int[] { 3 });

    assertBoundaries(new String[] { "Apple", "(", "Bravo", ".", "Charlie" },
                         new String[] { "", " ", " ", "", " ", ""},
                         new int[] { 4 });

    assertBoundaries(new String[] { "Apple", "(", ")", "Bravo", ".", "Charlie", "Tango" },
                         new String[] { "", " ", " ", " ", "", " ", " ", ""},
                         new int[] { 4, 6 });
    }

    private void assertBoundaries(String[] tokens, String[] whitespaces,
                                  int[] boundaries) {

        SentenceModel model
            = new MedlineSentenceModel();

        assertBoundaries(model,tokens,whitespaces,0,tokens.length,
                         boundaries);

    /*
        String[] tokens2 = new String[tokens.length+10];
        String[] whitespaces2 = new String[whitespaces.length+10];
        Arrays.fill(tokens2,"boo");
        Arrays.fill(whitespaces2," ");
        for (int i = 0; i < tokens.length; ++i) {
            tokens2[i+5] = tokens[i];
            whitespaces2[i+5] = whitespaces[i];
        }
        int[] boundaries2 = new int[boundaries.length];
        for (int i = 0; i < boundaries.length; ++i)
            boundaries2[i] = boundaries[i]+5;

        assertBoundaries(model,tokens2,whitespaces2,5,tokens.length,
                         boundaries2);
    */

    }

    private void assertBoundaries(SentenceModel model,
                                  String[] tokens, String[] whitespaces,
                                  int start, int length,
                                  int[] boundaries) {

        // full API call
        HashSet boundariesSet = new HashSet();
    model.boundaryIndices(tokens,whitespaces,start,length,
                                  boundariesSet);
        assertEquals(boundaries.length,boundariesSet.size());
        for (int i = 0; i < boundaries.length; ++i)
            assertTrue(boundariesSet.contains(new Integer(boundaries[i])));

        // simple API call
        if (start == 0 && length == tokens.length) {
            int[] boundariesDerived
                = model.boundaryIndices(tokens,whitespaces);
            assertEqualsArray(boundaries,boundariesDerived);
        }
    }

}
