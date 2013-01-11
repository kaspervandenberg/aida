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
 * A <code>StopFilterTokenizer</code> removes tokens from the token
 * stream if they meet conditions specified by concrete subclasses.
 * Subclasses must implement the {@link #stop(String)} method, which
 * determines whether a token should be removed from the token stream.
 * 
 * <p>If a token is removed, so is the whitespace immediately
 * following it.
 *
 * @author Bob Carpenter
 * @version 2.4.1
 * @since   LingPipe1.0
 */
public abstract class StopFilterTokenizer extends FilterTokenizer {

    /**
     * Construct a stop filter tokenizer from the specified
     * tokenizer.
     *
     * @param tokenizer Tokenizer from which to read tokens.
     */
    public StopFilterTokenizer(Tokenizer tokenizer) {
        super(tokenizer);
    }

    /**
     * Returns the next token that does not satisfy {@link
     * #stop(String)}, or <code>null</code> if there are no
     * more underlying tokens.
     *
     * @return Next token that is not stopped.
     */
    public String nextToken() {
        String token;
        while ((token = mTokenizer.nextToken()) != null
               && stop(token)) ;
        return token;
    }

    /**
     * Returns <code>true</code> if the specified token should be
     * filtered out.  The argument token will never be
     * <code>null</code>.
     *
     * @param token Token to test for removal.
     * @return <code>true</code> if the token should be removed.
     */
    abstract public boolean stop(String token);



}
