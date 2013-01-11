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

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.test.unit.BaseTestCase;

import java.io.IOException;

public class RegExTokenizerFactoryTest extends BaseTestCase {

    public void testOne() throws IOException, ClassNotFoundException {
    
    RegExTokenizerFactory factory 
        = new RegExTokenizerFactory("[a-zA-Z]+|[0-9]+|\\S");
    char[] cs = "abc de 123. ".toCharArray();
    String[] whites = new String[] { "", " ", " ", "", " " };
    String[] toks = new String[] { "abc", "de", "123", "." };
    int[] starts = new int[] { 0, 4, 7, 10 };

    Tokenizer tokenizer = factory.tokenizer(cs,0,cs.length);
    assertTrue(tokenizer != null);

    assertTokenize(new String(cs),whites,toks,starts,factory);
    
    TokenizerFactory factory2
        = (TokenizerFactory) AbstractExternalizable.compile(factory);

    assertTokenize(new String(cs),whites,toks,starts,factory2);
    }


    protected void assertTokenize(String input, 
                  String[] whitespaces, String[] tokens, 
                  int[] starts,
                  TokenizerFactory factory) {

    assertEqualsArray(tokens,
              factory.tokenizer(input.toCharArray(),
                        0,input.length()).tokenize());

    Tokenizer tokenizer 
        = factory.tokenizer(input.toCharArray(),0,input.length());
    for (int i = 0; i < starts.length; ++i) {
        String whitespace = tokenizer.nextWhitespace();
        String token = tokenizer.nextToken();
        assertEquals("Whitespace mismatch",whitespaces[i],whitespace);
        assertEquals("Token mismatch",tokens[i],token);
        assertEquals("Last token start position mismatch",starts[i],
             tokenizer.lastTokenStartPosition());
    }
    assertEquals("Final whitespace mismatch",
             whitespaces[whitespaces.length-1],
             tokenizer.nextWhitespace());
    assertNull("Should return final null",
           tokenizer.nextToken());

    }

}
