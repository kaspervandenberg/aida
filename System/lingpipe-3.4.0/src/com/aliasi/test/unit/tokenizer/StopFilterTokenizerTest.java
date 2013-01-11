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

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.StopFilterTokenizer;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.test.unit.BaseTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StopFilterTokenizerTest extends BaseTestCase {

    TokenizerFactory SPACE_TF = new RegExTokenizerFactory("\\S+");

    public void test1() {
        assertStop("",new String[] { }, new String[] { "" });
        assertStop("  ",new String[] { }, new String[] { "  " });

        assertStop("x",new String[] { "x" }, new String[] { "", "" });
        assertStop("x  ",new String[] { "x" }, new String[] { "", "  " });
        assertStop(" x",new String[] { "x" }, new String[] { " ", "" });
        assertStop(" x  ",new String[] { "x" }, new String[] { " ", "  " });

        assertStop("abc",new String[] { }, new String[] { "" });
        assertStop(" abc",new String[] { }, new String[] { " " });
        assertStop("abc  ",new String[] { }, new String[] { "" });
        assertStop(" abc  ",new String[] { }, new String[] { " " });

        assertStop("abc abc",new String[] { }, new String[] { "" });
        assertStop(" abc abc",new String[] { }, new String[] { " " });
        assertStop("abc abc  ",new String[] { }, new String[] { "" });
        assertStop(" abc abc  ",new String[] { }, new String[] { " " });

        assertStop("abc  defg",new String[] { "defg" },
                   new String[] { "", "" });
        assertStop("abc  defg   ",new String[] { "defg" },
                   new String[] { "", "   " });
        assertStop(" abc  defg",new String[] { "defg" },
                   new String[] { " ", "" });
        assertStop(" abc  defg   ",new String[] { "defg" },
                   new String[] { " ", "   " });

        assertStop("defg  abc",new String[] { "defg" },
                   new String[] { "", "  " });
        assertStop("defg  abc   ",new String[] { "defg" },
                   new String[] { "", "  " });
        assertStop(" defg  abc",new String[] { "defg" },
                   new String[] { " ", "  " });
        assertStop(" defg  abc   ",new String[] { "defg" },
                   new String[] { " ", "  " });
    }

    void assertStop(String s, String[] toks, String[] whitespaces) {
        List expectedTokList = Arrays.asList(toks);
        List expectedWsList = Arrays.asList(whitespaces);
        ArrayList foundTokList = new ArrayList();
        ArrayList foundWsList = new ArrayList();
        
        Tokenizer innerTokenizer 
            = SPACE_TF.tokenizer(s.toCharArray(),0,s.length());
        Tokenizer tokenizer = new TestTokenizer(innerTokenizer);
        tokenizer.tokenize(foundTokList,foundWsList);
        assertEquals("toks",expectedTokList,foundTokList);
        assertEquals("whitespaces",expectedWsList,foundWsList);
    }

    static class TestTokenizer extends StopFilterTokenizer {
        TestTokenizer(Tokenizer tokenizer) {
            super(tokenizer);
        }
        public boolean stop(String token) {
            return token.length() == 3;
        }
    }

    

}
