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
 * A <code>FilterTokenizer</code> contains a tokenizer to
 * which it delegates the tokenizer methods.
 *
 * @author  Bob Carpenter
 * @version 1.0.3
 * @since   LingPipe1.0
 */
public class FilterTokenizer extends Tokenizer {

    /**
     * The contained tokenizer.
     */
    protected Tokenizer mTokenizer;

    /**
     * Construct a filter tokenizer that contains the specified
     * tokenizer.
     *
     * @param tokenizer Contained tokenizer.
     */
    public FilterTokenizer(Tokenizer tokenizer) {
        mTokenizer = tokenizer;
    }

    /**
     * Sets the contained tokenizer to the specified tokenizer.
     *
     * @param tokenizer New contained tokenizer.
     */
    public void setTokenizer(Tokenizer tokenizer) {
        mTokenizer = tokenizer;
    }

    /**
     * Returns the next token from this tokenizer.  The
     * method is delegated to the contained tokenizer.
     *
     * @return Next token from this tokenizer.
     */
    public String nextToken() {
        return mTokenizer.nextToken();
    }

    /**
     * Returns the next white space from this tokenizer.  The method
     * is delegated to the contained tokenizer.
     *
     * @return Next white space from this tokenizer.
     */
    public String nextWhitespace() {
        return mTokenizer.nextWhitespace();
    }

    /**
     * Returns the starting index of the last token returned.  The
     * method is delegated to the contained tokenizer.
     *
     * @return Starting index of last token in sequence.
     */
    public int lastTokenStartPosition() {
        return mTokenizer.lastTokenStartPosition();
    }

}
