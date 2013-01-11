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

package com.aliasi.tokenizer;

/**
 * A <code>PorterStemmerFilterTokenizer</code> returns the stemmed
 * version of each token, as produced by {@link
 * PorterStemmer#stem(String)}.
 *
 * @author Bob Carpenter
 * @version 1.0.4
 * @since   LingPipe1.0
 */
public class PorterStemmerFilterTokenizer extends TokenFilterTokenizer {

    /**
     * Construct a Porter stemmer filter tokenizer containing
     * the specified tokenizer.
     *
     * @param tokenizer Contained tokenizer.
     */
    public PorterStemmerFilterTokenizer(Tokenizer tokenizer) {
        super(tokenizer);
    }

    /**
     * Returns the stemmed form of the specified token.
     *
     * @param token Token to stem.
     * @return Stemmed version of specified token.
     */
    public String filter(String token) {
        return PorterStemmer.stem(token);
    }

}



