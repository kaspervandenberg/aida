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

package com.aliasi.coref;

/**
 * An implementation of the killing interface provides a way
 * of defeating a match between a mention and a mention chain.
 *
 * @author  Bob Carpenter
 * @version 1.0
 * @since   LingPipe1.0
 */
public interface Killer {

    /**
     * Return <code>true</code> if the match between mention and
     * mention chain should be excluded.
     *
     * @param mention Mention to match.
     * @param chain Mention chain to match.
     * @return <code>true</code> if the match should be prohibited.
     */
    boolean kill(Mention mention, MentionChain chain);
}

