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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A <code>ReversibleMTFListComparator</code> contains an ordered list
 * of comparators, any member of which can be reversed or moved to the
 * front of the list.  The result of comparison is defined to be the
 * first non-zero result from the contained comparators evaluated in
 * order, or zero if they all return zero.  A reversible move-to-front
 * list comparator is constructed with an array of comparators, and
 * will be initialized with them in the order supplied and in their
 * original polarity (not reversed).  At any point, a comparator may
 * be reversed using {@link #reverse(Comparator)}, or may be moved to
 * the front of the list with {@link #moveToFront(Comparator)}.
 *
 * <P>This class is useful for maintaining ordering on columns in
 * tables, where the ordering of sort on a column may be reversed,
 * and where sorts should be stable except for reordering the
 * column in focus.
 *
 * <P>To provide concurrent access to reversible move-to-front list
 * comparators, they must be concurrent-read/single-write (CRSW)
 * locked, where the methods {@link #moveToFront(Comparator)}, and
 * {@link #reverse(Comparator)} are writers, and the methods {@link
 * #compare(Object,Object)} and {@link #sort(Object[])} are readers.
 * Exclusive locking on all access suffices for safety, but the result
 * is not as live as CRSW synchronization.
 *
 * <P><i>Implementation Note:</i>&nbsp; The original comparators are
 * maintained in a hash map, so their equality and hashcode methods
 * must be stable after construction of a list comparator.  Having
 * comparators simply inherit implementations of {@link #hashCode()} and
 * {@link #equals(Object)} from {@link Object} suffices.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe1.0
 */
public class ReversibleMTFListComparator<E> implements Comparator<E> {

    /**
     * A mapping from the comparators provided in the constructor to
     * their reversible comparator containers.
     */
    private final HashMap<Comparator<? super E>,Comparator<? super E>> mComparatorToReversibleComparator 
	= new HashMap<Comparator<? super E>,Comparator<? super E>>();

    /**
     * The list of reversible comparators in order of application.
     */
    private final LinkedList<Comparator<? super E>> mComparatorList 
	= new LinkedList<Comparator<? super E>>();

    /**
     * Construct a reversible move-to-front comparator containing
     * reversible versions of the specified comparators.
     *
     * @param comparators Comparators to use, in order.
     */
    public ReversibleMTFListComparator(Comparator<? super E>[] comparators) {
        for (int i = 0; i < comparators.length; ++i) {
            ReversibleComparator<? super E> revComparator
                = new ReversibleComparator<E>(comparators[i]);
            mComparatorList.addLast(revComparator);
            mComparatorToReversibleComparator.put(comparators[i],
                                                  revComparator);
        }
    }

    /**
     * Moves the specified comparator to the front of the list of
     * comparators.
     *
     * @param comparator Comparator to move to the front of the list.
     * @throws IllegalArgumentException If the comparator specified is
     * not one of the comparators supplied at construction time.
     */
    public void moveToFront(Comparator comparator) {
        ReversibleComparator revComparator
            = getReversibleComparator(comparator);
        mComparatorList.remove(revComparator);
        mComparatorList.addFirst(revComparator);
    }

    /**
     * Reverse the ordering on the specified comparator.
     *
     * @param comparator Comparator whose order is reversed.
     * @throws IllegalArgumentException If the comparator specified is
     * not one of the comparators supplied at construction time.
     */
    public void reverse(Comparator comparator) {
        getReversibleComparator(comparator).toggleSortOrder();
    }

    /**
     * Returns the reversible comparator corresponding to the
     * specified comparator.
     *
     * @param comparator Comparator whose reversible counterpart is
     * returned.
     * @return The reversible counterpart of the specified comparator.
     * @throws IllegalArgumentException If the comparator specified
     * was not provided at construction time.
     */
    private ReversibleComparator
        getReversibleComparator(Comparator comparator) {

        ReversibleComparator revComparator = (ReversibleComparator)
            mComparatorToReversibleComparator.get(comparator);

        if (revComparator == null)
            throw new IllegalArgumentException("Unknown compartor="
                                               + comparator);

        return revComparator;
    }

    /**
     * Compares the specified objects using this list comparator,
     * returning a positive number if the first is greater than the
     * second, a negative number if the first is less than the second,
     * and zero if they are equal.  The contained comparators, either
     * reversed or in their original order, are evaluated in order
     * until one produces a non-zero result, which is then returned.
     * If the contained comparators all return zero, zero is returned
     * from this method.
     *
     * @param obj1 First object to compare.
     * @param obj2 Second object to compare.
     * @return A positive integer if the first object is greater than
     * the second, a negative integer if the second is greater than
     * the first, and zero if they are the same.
     */
    public int compare(E obj1, E obj2) {
        Iterator comparatorIterator = mComparatorList.iterator();
        while (comparatorIterator.hasNext()) {
            Comparator comparator = (Comparator) comparatorIterator.next();
            int result = comparator.compare(obj1,obj2);
            if (result != 0) return result; // return first non-equal result
        }
        return 0; // same by each comparator
    }

    /**
     * Sort the specified array of objects using this comparator.
     *
     * @param xs Array of objects to sort.
     */
    public <F extends E> void sort(F[] xs) {
        Arrays.<F>sort(xs,this);
    }

}
