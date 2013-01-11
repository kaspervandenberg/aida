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

import com.aliasi.tokenizer.CharacterTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.test.unit.BaseTestCase;

import java.io.IOException;

public class CharacterTokenizerFactoryTest extends BaseTestCase {

    public void testTokenize() {
    assertTokenize("abc", 
               new String[] { "a", "b", "c" });
    assertTokenize("",
               new String[] { });
    }

    void assertTokenize(String input, String[] tokens) {
    TokenizerFactory factory = new CharacterTokenizerFactory();
    Tokenizer tokenizer 
        = factory.tokenizer(input.toCharArray(),0,input.length());
    for (int i = 0; i < tokens.length; ++i) {
        assertEquals("",tokenizer.nextWhitespace());
        assertEquals(tokens[i],tokenizer.nextToken());
    }
    assertEquals("",tokenizer.nextWhitespace());
    }

    public void testCompilation() throws IOException, ClassNotFoundException {
    CharacterTokenizerFactory factory = new CharacterTokenizerFactory();
    TokenizerFactory compiledFactory 
        = (TokenizerFactory) AbstractExternalizable.compile(factory);
    assertEquals(CharacterTokenizerFactory.FACTORY,
             compiledFactory);
    }

}
