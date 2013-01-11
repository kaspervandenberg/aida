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
 * Provides a filtering comparator that supports inverting the order
 * of the comparison.  A reversible comparator is constructed from a
 * specified comparator as a filter comparator.  The ordering will
 * start out the same as the provided comparator.  Whenever the {@link
 * #toggleSortOrder()} method is called, the order of comparison will
 * be inverted.  Calls to {@link #setOriginalSortOrder()} set the
 * ordering to be that of the contained comparator, whereas
 * {@link #setReverseSortOrder()} sets the ordering to be the inverse
 * of that provided by the contained comparator.
 *
 * <P>To provide concurrent access to reversible comparators, they
 * must be concurrent-read/single-write locked, where the methods
 * {@link #toggleSortOrder()}, {@link #setOriginalSortOrder()}, and
 * {@link #setReverseSortOrder()} are writers, whereas the methods
 * {@link #compare(Object,Object)} is a reader.  Exclusive locking on
 * all methods suffices for safety.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe1.0
 */
public class ReversibleComparator<E> extends FilterComparator<E> {

    /**
     * <code>true</code> if the contained comparator should be
     * reversed.
     */
    private boolean mReverse = false;

    /**
     * Construct a reversible comparator from the specified comparator.
     * The initial ordering will be the same as the specified comparator.
     *
     * @param comparator The contained comparator in the reversible
     * comparator being constructed.
     */
    public ReversibleComparator(Comparator<? super E> comparator) {
        super(comparator);
    }

    /**
     * Return the result of comparing the specified objects using the
     * contained comparator or its inverse.  Which ordering is
     * specified by calls to {@link #toggleSortOrder()}, {@link
     * #setReverseSortOrder()}, and {@link #setOriginalSortOrder()}.
     *
     * @param x1 First object to compare.
     * @param x2 Second object to compare.
     * @return A positive integer if the first object is greater than
     * the second, a negative integer if the second is greater than
     * the first, and zero if they are the same.
     */
    public int compare(E x1, E x2) {
        return mReverse
            ? mComparator.compare(x2,x1)
            : mComparator.compare(x1,x2);
    }

    /**
     * Toggle the ordering on this comparator to be the inverse of
     * its former ordering.
     */
    public void toggleSortOrder() {
        mReverse = !mReverse;
    }

    /**
     * Resets this comparator to use the same ordering as the
     * contained comparator provided at construction time.
     */
    public void setOriginalSortOrder() {
        mReverse = false;
    }

    /**
     * Sets this comparator to use an ordering that is
     * the inverse of that provided at construction time.
     */
    public void setReverseSortOrder() {
        mReverse = true;
    }

}
