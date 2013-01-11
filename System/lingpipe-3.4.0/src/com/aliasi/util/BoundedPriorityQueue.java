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

import java.util.AbstractCollection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * A <code>BoundedPriorityQueue</code> implements a priority queue
 * with an upper bound on the number of elements.  If the queue is not
 * full, added elements are always added.  If the queue is full and
 * the added element is greater than the smallest element in the
 * queue, the smallest element is removed and the new element is
 * added.  If the queue is full and the added element is not greater
 * than the smallest element in the queue, the new element is not
 * added.
 *
 * <P>Bounded priority queues are the ideal data structure with which
 * to implement n-best accumulators.  A priority queue of bound
 * <code>n</code> can find the <code>n</code>-best elements of a
 * collection of <code>m</code> elements using <code>O(n)</code> space
 * and <code>O(m n log n)</code> time.  

 * <P>Bounded priority queues may also be used as the basis of a
 * search implementation with the bound implementing heuristic
 * n-best pruning.
 *
 * <P>Because bounded priority queues require a comparator and a
 * maximum size constraint, they do not comply with the recommendation
 * in the {@link java.util.Collection} interface in that they neither
 * implement a nullary constructor nor a constructor taking a single
 * collection.  Instead, they are constructed with a comparator and
 * a maximum size bound.
 *
 * <P><i>Implementation Note:</i> Priority queues are implemented on
 * top of {@link TreeSet} with element wrappers to adapt object
 * equality and the priority comparator.  Because tree sets implement
 * balanced trees, the priority queue operations,
 * <code>add(Object)</code>, <code>pop()</code> and
 * <code>peek()</code>, all require <code>O(log n)</code> time where
 * <code>n</code> is the size of the queue.  A standard heap-based
 * implementation of a queue implements peeks in constant time and
 * adds and pops in <code>O(log n)</code> time.  For our intended
 * applications, pops are more likely than peeks and we need access to
 * the worst element in the queue.  An upside-down ordered heap
 * implementation of priority queues implements bounded adds most
 * efficiently, but requires up to <code>O(n)</code> for a pop or peek
 * and an <code>O(n log n)</code> sort before iteration.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public class BoundedPriorityQueue<E>
    extends AbstractCollection<E> 
    implements PriorityQueue<E> {

    final TreeSet<Entry<E>> mQueue;
    private int mMaxSize;
    private final Comparator<? super E> mComparator;
    
    /**
     * Construct a bounded priority queue which uses the specified
     * comparator to order elements and allows up to the specified
     * maximum number of elements.
     *
     * @param comparator Comparator to order elements.
     * @param maxSize Maximum number of elements in the queue.
     * @throws IllegalArgumentException If the maximum size is less than 1.
     */
    public BoundedPriorityQueue(Comparator<? super E> comparator,
                                int maxSize) {
        if (maxSize < 1) {
            String msg = "Require maximum size >= 1."
                + " Found max size=" + maxSize;
            throw new IllegalArgumentException(msg);
        }
        mQueue = new TreeSet(new EntryComparator());
        mComparator = comparator;
        mMaxSize = maxSize;
    }

    public boolean isEmpty() {
        return mQueue.isEmpty();
    }
    
    public E peek() {
        if (isEmpty()) return null;
        return mQueue.first().mObject;
    }

    public E pop() {
        if (isEmpty()) return null;
        if (mQueue.isEmpty()) return null;
        Entry<E> entry = mQueue.first();
        mQueue.remove(entry);
        return entry.mObject;
    }
    
    /**
     * Removes the specified object from the priority queue.  Note
     * that the object is removed using identity conditions defined by
     * the comparator specified for this queue, not the natural
     * equality or comparator.
     *
     * @param obj Object to remove from priority queue.
     * @return <code>true</code> if the object was removed.
     * @throws ClassCastException If the specified object is not
     * compatible with this collection.
     */
    public boolean remove(Object obj) {
        return mQueue.remove(new Entry<E>((E)obj,-1));
    }


    /**
     * Sets the maximum size of this bounded priority queue to
     * the specified maximum size.  If there are more than the
     * specified number of elements in the queue, they are popped
     * one by one until the queue is of the maximum size.
     *
     * <p>Note that this operation is not thread safe and should not
     * be called concurrently with any other operations on this queue.
     * 
     * @param maxSize New maximum size for this queue.
     */
    public void setMaxSize(int maxSize) {
        mMaxSize = maxSize;
        while (mQueue.size() > maxSize()) 
            mQueue.remove(mQueue.last());
    }

    /**
     * Conditionally add the specified element to this queue.  If the
     * queue is smaller than its maximum size, the element is added.
     * If the queue is at its maximum size, the element is added only
     * if it is larger than the smallest element, in which case the
     * smallest element in the queue is removed.
     *
     * @param o Object to add to queue.
     * @return <code>true</code> if the object was added.
     */
    public boolean add(E o) {
        if (size() < mMaxSize) 
            return mQueue.add(new Entry(o));
        Entry<E> last = mQueue.last();
        E lastObj = last.mObject;
        if (mComparator.compare(o,lastObj) <= 0) 
            return false; // worst element better
        if (!mQueue.add(new Entry(o))) 
            return false; // already contain elt
        mQueue.remove(last);
        return true;
    }

    /**
     * Removes all elements from this queue.  The queue will be
     * empty after this call.
     */
    public void clear() {
        mQueue.clear();
    }

    /**
     * Returns the current number of elements in this
     * priority queue.
     *
     * @return Number of elements in this priority queue.
     */
    public int size() {
        return mQueue.size();
    }

    /**
     * Returns the maximum size allowed for this queue.  
     *
     * @return The maximum size allowed for this queue.  
     */
    public int maxSize() {
        return mMaxSize;
    }
    
    /**
     * Returns an iterator over the elements in this bounded priority
     * queue.  The elements are returned in order.  The returned
     * iterator implements a fail-fast deletion in the same way
     * as Java's collections framework.
     *
     * @return Ordered iterator over the elements in this queue.
     */
    public Iterator<E> iterator() {
        return new QueueIterator<E>(mQueue.iterator());
    }

    private class EntryComparator implements Comparator<Entry<E>> {
        public int compare(Entry<E> entry1, Entry<E> entry2) {
            // reverse normal so largest is "first"
            E eObj1 = entry1.mObject;
            E eObj2 = entry2.mObject;
            if (eObj1.equals(eObj2)) return 0;
            int comp = mComparator.compare(eObj1,eObj2);
            if (comp != 0) return -comp;
            // arbitrarily order by earliest
            return entry1.mId < entry2.mId ? 1 : -1;
        }
    }

    private static class Entry<E> {
        private final long mId;
        private final E mObject;
        public Entry(E object) {
            this(object,nextId());
        }
        public Entry(E object, long id) {
            mObject = object;
            mId = id;
        }
        private static synchronized long nextId() {
            return sNextId++;
        }
        private static long sNextId = 0;
        public String toString() {
            return "qEntry(" + mObject.toString() + "," + mId+ ")";
        }
    }

    private static class QueueIterator<E> implements Iterator<E> {
        private final Iterator<Entry<E>> mIterator;
        QueueIterator(Iterator<Entry<E>> iterator) {
            mIterator = iterator;
        }
        public boolean hasNext() {
            return mIterator.hasNext();
        }
        public E next() {
            return mIterator.next().mObject;
        }
        public void remove() {
            mIterator.remove();
        }
    }



}
