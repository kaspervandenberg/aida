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

import com.aliasi.util.AbstractExternalizable;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import java.io.IOException;

public class IndoEuropeanTokenizerFactoryTest extends IndoEuropean {

    protected void assertTokenize(String input,
                        String[] whitespaces, String[] tokens,
                        int[] starts) {

        TokenizerFactory factory = new IndoEuropeanTokenizerFactory();

        assertEqualsArray(tokens,
                          factory.tokenizer(input.toCharArray(),
                                            0,input.length()).tokenize());

        Tokenizer tokenizer
            = factory.tokenizer(input.toCharArray(),0,input.length());
        for (int i = 0; i < starts.length; ++i) {
            String whitespace = tokenizer.nextWhitespace();
            String token = tokenizer.nextToken();
            assertEquals("Whitespace mismatch",whitespace,whitespaces[i]);
            assertEquals("Token mismatch",token,tokens[i]);
            assertEquals("Last token start position mismatch",
                         tokenizer.lastTokenStartPosition(),starts[i]);
        }
        assertEquals("Final whitespace mismatch",
                     whitespaces[whitespaces.length-1],
                     tokenizer.nextWhitespace());
        assertNull("Should return final null",
                   tokenizer.nextToken());

    }

    public void testCompilation() throws ClassNotFoundException, IOException {
        IndoEuropeanTokenizerFactory factory
            = new IndoEuropeanTokenizerFactory();
        TokenizerFactory compiledFactory
            = (TokenizerFactory) AbstractExternalizable.compile(factory);
        assertEquals(IndoEuropeanTokenizerFactory.FACTORY,
                     compiledFactory);
    }

}
