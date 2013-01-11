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

import java.util.Set;

/**
 * A <code>StopListFilterTokenizer</code> is a stop-list-based stop
 * filter tokenizer that removes tokens from a tokenizer stream if
 * they are on a specified list of so-called ``stop'' tokens.
 *
 * @author Bob Carpenter
 * @version 1.0.3
 * @since   LingPipe1.0
 */
public class StopListFilterTokenizer extends StopFilterTokenizer {

    /**
     * The set of tokens to filter out.
     */
    private final Set mStopList;

    /**
     * Construct a stop-list-based stop filter tokenizer based
     * on the specified set of stop tokens.
     *
     * <p>Keeps a handle on the set, so that changes to the set will
     * affect this stop list.
     *
     * @param stopList Set of tokens to remove from token streams.
     */
    public StopListFilterTokenizer(Tokenizer tokenizer, Set stopList) {
        super(tokenizer);
        mStopList = stopList;
    }

    /**
     * Returns <code>true</code> if the specified token should
     * be ignored.
     *
     * @param token Token to test for removal.
     * @return <code>true</code> if the token should be removed from
     * the stream of tokens.
     */
    public boolean stop(String token) {
        return mStopList.contains(token);
    }

}
