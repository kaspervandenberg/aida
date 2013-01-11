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

package com.aliasi.coref.matchers;

import com.aliasi.coref.EnglishMentionFactory;
import com.aliasi.coref.Killer;
import com.aliasi.coref.Mention;
import com.aliasi.coref.MentionChain;

import com.aliasi.util.Collections;

import java.util.Set;

/**
 * Implements a killing function that defeats a match of a mention
 * against a mention chain with an incompatible honorific.
 *
 * @author  Bob Carpenter
 * @version 1.0
 * @since   LingPipe1.0
 */
public class HonorificConflictKiller implements Killer {

    /**
     * Construct a new honorific killer.
     */
    public HonorificConflictKiller() { 
        /* do nothing */
    }

    /**
     * Returns <code>true</code> if the specified mention
     * and mention chain have incompatible honorifics.  Honorifics
     * are determined to be honorific according to the
     */
    public boolean kill(Mention mention, MentionChain chain) {
        Set honorifics1 = mention.honorifics();
        Set honorifics2 = chain.honorifics();
        return honorifics1.size() > 0
            && honorifics2.size() > 0
            && honorificConflict(honorifics1,honorifics2);
    }

    private static boolean honorificConflict(Set honorifics1,
                                             Set honorifics2) {
        return male(honorifics1) && female(honorifics2)
            || female(honorifics1) && male(honorifics2);
    }

    private static boolean male(Set honorifics) {
        return
            Collections.intersects(honorifics,
                                   EnglishMentionFactory.MALE_HONORIFICS);
    }

    private static boolean female(Set honorifics) {
        return
            Collections.intersects(honorifics,
                                   EnglishMentionFactory.FEMALE_HONORIFICS);
    }

}
