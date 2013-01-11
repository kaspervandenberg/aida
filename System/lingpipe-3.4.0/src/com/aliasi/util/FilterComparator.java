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

package com.aliasi.util;

import java.util.Comparator;

/**
 * Provides a generic filter for comparators, holding a reference to
 * a contained comparator and allowing it to be retrieved, and a comparison
 * operation that delegates to the contained comparator.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe1.0
 */
public class FilterComparator<E> implements Comparator<E> {

    /**
     * The comparator being filtered.
     */
    protected final Comparator<? super E> mComparator;

    /**
     * Construct a filter comparator containing the specified
     * comparator and delegating comparison to it.
     *
     * @param comparator The comparator being filtered.
     */
    public FilterComparator(Comparator<? super E> comparator) {
        mComparator = comparator;
    }

    /**
     * Returns the comparator being filtered, as provided to the
     * constructor.
     *
     * @return The comparator being filtered, as provided to the
     * constructor.
     */
    public Comparator<? super E> getFilteredComparator() {
        return mComparator;
    }

    /**
     * Return the result of comparing the specified objects using
     * the contained comparator.  Subclasses typically override
     * this implementation.
     *
     * @param x1 First object to compare.
     * @param x2 Second object to compare.
     * @return A positive integer if the first object is greater than
     * the second, a negative integer if the second is greater than
     * the first, and zero if they are the same.
     */
    public int compare(E x1, E x2) {
        return mComparator.compare(x1,x2);
    }

}
