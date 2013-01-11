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

package com.aliasi.lm;

import java.util.Stack;

class BitTrie {

    private final Stack mLastSymbolStack = new Stack();

    protected BitTrie() { 
        /* no op constructor */
    }

    long popValue() {
        return toLong(mLastSymbolStack.pop());
    }

    long pushValue(long n) {
        mLastSymbolStack.push(new Long(n));
        return n;
    }

    static long toLong(Object n) {
        return ((Long) n).longValue();
    }

    static void checkCount(long n) {
        if (n > 0) return;
        String msg = "All counts must be positive."
            + " Found count=" + n;
        throw new IllegalArgumentException(msg);
    }
    
}
