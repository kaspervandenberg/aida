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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * An <code>NBestSet</code> is an ordered set with a maximum number of
 * elements specified at construction.  Adding to an n-best set of
 * non-maximum size proceeds as usual. When adding to an n-best set of
 * maximum size, if the specified item is greater than the minimum
 * element in the set, the minimum element is removed and the
 * specified item is added.  Other than the <code>add</code> methods,
 * all other methods work as in a <code>TreeSet</code>.  In
 * particular, iteration through {@link #iterator()} returns objects in
 * increasing order of size, {@link #first()} returns the smallest
 * element and {@link #last()} the largest element. For this class to
 * work properly, there must be a unique minimum element in the
 * collection at all times that is smaller than all other elements.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public class NBestSet<E> extends TreeSet<E> {
	
    // inherits serializability from TreeSet
    static final long serialVersionUID = 1877503923749415403L;

    private final int mMax;

    /**
     * Construct an n-best set with the specified maximum number of
     * elements, using the specified comparator to compare elements.
     *
     * @param max Maximum number of elements for constructed set.
     * @param comparator Comparator to use for element comparison.
     */
    public NBestSet(int max, Comparator<? super E> comparator) {
        super(comparator);
        mMax = max;
    }

    /**
     * Construct an n-best set with the specified maximum number of
     * elements, using the natural ordering of elements.
     *
     * @param max Maximum number of elements for constructed set.
     */
    public NBestSet(int max) {
        mMax = max;
    }

    /**
     * Add the specified object to this n-best set.  If the set is
     * not of maximum size, the object is added if the set did not
     * already contain the object.  If the set is of maximum size,
     * the specified object is compared with the mimimum (last)
     * element of the set.  If the specified element is greater, it is
     * added and the last element is removed; otherwise the element is
     * not added.  The return result is <code>true</code> if the set
     * is modified as a result of this operation.
     *
     * @param obj Object to add.
     * @return <code>true</code> if this set is modified as a result
     * of this operation.
     */
    public boolean add(E obj) {
        if (size() < mMax)
            return super.add(obj);
        E first = first();
        Comparator<? super E> comparator = comparator();
        int comparison = comparator == null
            ? ((Comparable)obj).compareTo((Comparable)first)
            : comparator.compare(obj,first);
        if (comparison <= 0) return false;
        remove(first);
        return super.add(obj);
    }

    /**
     * Add the elements of the collection one by one.  Each element is
     * added using {@link #add(Object)}.  The return result is
     * <code>true</code> if the set is modified as a result of this
     * operation.
     *
     * @param c Collection of elements to add.
     * @return <code>true</code> if this set is modified as a result
     * of this operation.
     */
    public boolean addAll(Collection<? extends E> c) {
        Iterator<? extends E> it = c.iterator();
        boolean changed = false;
        while (it.hasNext())
            changed = changed || add(it.next());
        return changed;
    }


}
