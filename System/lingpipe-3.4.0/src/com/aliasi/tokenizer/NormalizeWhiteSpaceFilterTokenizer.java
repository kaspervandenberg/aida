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

import com.aliasi.util.Strings;

/**
 * A <code>NormalizeWhiteSpaceFilterTokenizer</code> reduces each
 * non-empty whitespace to a single space.
 *
 * @author Bob Carpenter
 * @version 1.0.3
 * @since   LingPipe1.0
 */
public class NormalizeWhiteSpaceFilterTokenizer extends FilterTokenizer {


    /**
     * Construct a filter tokenizer that normalizes whitespace,
     * using the specified contained tokenizer.
     *
     * @param tokenizer Contained tokenizer.
     */
    public NormalizeWhiteSpaceFilterTokenizer(Tokenizer tokenizer) {
        super(tokenizer);
    }

    /**
     * Returns the next whitespace, which will either be
     * the single space string {@link Strings#SINGLE_SPACE_STRING}
     * or the empty string {@link Strings#EMPTY_STRING}.
     *
     * @return Next whitespace.
     */
    public String nextWhitespace() {
        return mTokenizer.nextWhitespace().length() > 0
            ? Strings.SINGLE_SPACE_STRING
            : Strings.EMPTY_STRING;
    }

}
