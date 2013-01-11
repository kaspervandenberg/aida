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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Static utility methods for iteration.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public class Iterators {

    // do not allow instances
    private Iterators() {
        /* no instances */
    }



    /**
     * An <code>Iterator.Array</code> iterates over the elements of an
     * array specified at construction time.  The array is not copied,
     * so any changes in the underlying array are reflected in the
     * iterator.
     *
     * <P><I>Implementation Note:</I> this class does not automatically
     * free references in the underlying array, because the array
     * may be used elswhere.  If reference freeing is critical here,
     * a call to <code>remove()</code> after ever <code>next()</code>
     * will free the references in the array.
     *
     * @author  Bob Carpenter
     * @version 3.0
     * @since   LingPipe1.0
     */
    public static class Array<E> implements Iterator<E> {

        /**
         * Array to iterate over.
         */
        private final E[] mMembers;

        /**
         * Current position of next element to return in array.
         */
        private int mPosition = 0;

        /**
         * Construct an array iterator from the specified array.
         *
         * @param members Array basis of the constructed iterator.
         */
        public Array(E[] members) {
            mMembers = members;
        }

        /**
         * Returns <code>true</code> if this iterator has more
         * elements.
         *
         * @return <code>true</code> if this iterator has more
         * elements.
         */
        public boolean hasNext() {
            return mPosition < mMembers.length;
        }

        /**
         * Returns the next element in the array.
         *
         * @return Next element in the array.
         * @throws NoSuchElementException If there are no more
         * elements left in the array to return.
         */
        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return mMembers[mPosition++];
        }

        /**
         * Sets position in underlying array corresponding to the most
         * recently returned token to <code>null</code>.
         *
         * @throws IllegalStateException If the <code>next</code>
         * method has not been called, or the <code>remove</code>
         * method has already been called after the last call to the
         * next method.
         */
        public void remove() {
            if (mPosition < 1)
                throw new IllegalStateException("Next not yet called.");
            if (mMembers[mPosition-1] == null)
                throw new IllegalStateException("Remove already called.");
            mMembers[mPosition-1] = null;
        }
    }

    /**
     * An <code>Iterators.ArraySlice</code> iterates over a slice of
     * an array specified by start index and length.
     *
     * @author  Bob Carpenter
     * @version 3.0
     * @since   LingPipe1.0
     */
    public static class ArraySlice<E> implements Iterator<E> {

        /**
         * The underlying objects represented in this array iterator.
         */
        private final E[] mObjects;

        /**
         * Index of next element to return.
         */
        private int mNext;

        /**
         * Index one past the index of the last element to return.
         */
        private final int mLast;

        /**
         * Construct an iterator over the specified array of objects
         * that begins at the object at the specified start index
         * and carries through to the other objects.
         *
         * @param objects Array of objects over which to iterate.
         * @param start Index of first object to return.
         */
        public ArraySlice(E[] objects,
                          int start, int length) {
            mObjects = objects;
            mNext = start;
            mLast = start + length;
        }

        /**
         * Returns <code>true</code> if there are more objects
         * to return.
         *
         * @return <code>true</code> if this iterator has more
         * elements.
         */
        public boolean hasNext() {
            return mNext < mLast;
        }

        /**
         * Returns the next object for this iterator, throwing
         * an exception if there are no more objects left.
         *
         * @return Next object from this iterator.
         * @throws NoSuchElementException If there are no more
         * elements to return.
         */
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();
            return mObjects[mNext++];
        }

        /**
         * Throws an <code>UnsupportedOperationException</code>.
         *
         * @throws UnsupportedOperationException If called.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * An <code>Iterator.Empty</code> is an iterator with no
     * objects.  The method {@link #hasNext()} always returns
     * <code>false</code>, while the methods {@link #next()}
     * and {@link #remove()} always throw exceptions.
     *
     * @author  Bob Carpenter
     * @version 3.0
     * @since   LingPipe3.0
     */
    public static class Empty<E> implements Iterator<E> {

        public Empty() {
            /* do nothing */
        }

        /**
         * Always returns <code>false</code>.
         *
         * @return <code>false</code>.
         */
        public boolean hasNext() {
            return false;
        }

        /**
         * Calling this method throws a no-such-element exception.
         *
         * @return Always throws an exception.
         * @throws NoSuchElementException Always.
         */
        public E next() {
            String msg = "No elements in empty iterator.";
            throw new NoSuchElementException(msg);
        }

        /**
         * Calling this method throws an illegal state exception.
         *
         * @throws IllegalStateException Always.
         */
        public void remove() {
            String msg = "No elements to remove in empty iterator.";
            throw new IllegalStateException(msg);
        }

    }

    /**
     * An <code>Iterator.Singleton</code> is an iterator over a
     * singleton object.
     *
     * @author  Bob Carpenter
     * @version 3.0
     * @since   LingPipe2.0
     */
    public static class Singleton<E> implements Iterator<E> {

        /**
         * The single object to return.
         */
        private E mMember;

        /**
         * <code>true</code> if the single element has not already
         * been returned.
         */
        private boolean mHasNext = true;

        /**
         * Construct a singleton iterator that returns the specified
         * object.
         *
         * @param member Single member over which to iterate.
         */
        public Singleton(E member) {
            mMember = member;
        }

        /**
         * Returns <code>true</code> if the single member has not
         * already been returned.
         *
         * @return <code>true</code> if the single member has not
         * already been returned.
         */
        public boolean hasNext() {
            return mHasNext;
        }

        /**
         * Returns the singleton member if it has not yet been
         * returned, otherwise throw an exception.
         *
         * @return Singleton member if it has not yet been returned.
         * @throws NoSuchElementException If the singleton member has
         * already been returned and no elements remain.
         */
        public E next() {
            if (!mHasNext)
                throw new NoSuchElementException();
            mHasNext = false;
            E result = mMember;
            mMember = null;
            return result;
        }

        /**
         * Throws an unsupported operation exception.
         *
         * @throws UnsupportedOperationException Whenver called.
         */
        public void remove() {
            String msg = "This iterator does not support remove()."
                + " class=" + this.getClass();
            throw new UnsupportedOperationException(msg);
        }
    }


    /**
     * An <code>Iterators.Pair</code> provides an iterator
     * over a sequence of exactly two elements.
     *
     * @author  Bob Carpenter
     * @version 3.0
     * @since   LingPipe2.0
     */
    public static class Pair<E> implements Iterator<E> {

        /**
         * Number of members returned so far.
         */
        private int mMembersReturned = 0;

        private E mMember1;
        private E mMember2;

        /**
         * Construct a pair iterator based on the containing
         * pair set.
         */
        public Pair(E member1, E member2) {
            mMember1 = member1;
            mMember2 = member2;
        }

        /**
         * Returns <code>true</code> if there are more elements
         * left to return.
         *
         * @return <code>true</code> if there are more elements
         * left to return.
         */
        public boolean hasNext() {
            return mMembersReturned < 2;
        }

        /**
         * Returns the next object in this pair iterator,
         * or throws an exception if there aren't any more.
         *
         * @return Next object in this pair iterator.
         */
        public E next() {
            if (mMembersReturned == 0) {
                ++mMembersReturned;
                E result1 = mMember1;
                mMember1 = null;
                return result1;
            }
            if (mMembersReturned == 1) {
                ++mMembersReturned;
                E result2 = mMember2;
                mMember2 = null;
                return result2;
            }
            throw new NoSuchElementException();
        }

        /**
         * Throws an unsupported operation exception.
         *
         * @throws UnsupportedOperationException Whenver called.
         */
        public void remove() {
            String msg = "This iterator does not support remove()."
                + " class=" + this.getClass();
            throw new UnsupportedOperationException(msg);
        }

    }


    /**
     * An <code>Iterators.Filter</code> filters the stream of objects
     * returned by another iterator by subjecting them to an acceptance
     * test.  Instances are constructed with a specified iterator to be
     * filtered; this filter is saved in this class and should not be
     * accessed by other methods.  Concrete subclasses must implement
     * {@link #accept(Object)}, which determines which of the elements
     * produced by the containediterator are returned.
     *
     * @author  Bob Carpenter
     * @version 3.0
     * @since   LingPipe1.0
     */
    public static abstract class Filter<E> implements Iterator<E> {

        private final Iterator<? extends E> mIterator;
        private boolean mFoundNext = false;
        private E mNext;

        /**
         * Construct a filtered iterator from the specified iterator.
         *
         * @param iterator Contained iterator to be filtered.
         */
        public Filter(Iterator<? extends E> iterator) {
            mIterator = iterator;
        }

        /**
         * Returns <code>true</code> for objects returned by the contained
         * iterator that should be returned by this iterator.
         *
         * @param x Object to test.
         * @return <code>true</code> if this object should be returned by
         * this iterator.
         */
        abstract public boolean accept(E x);

        /**
         * Returns <code>true</code> if calls to <code>next()</code> will
         * return a value.
         *
         * <P><i>Implementation note:</i> This iterator stores the next
         * element in a local variable after calls to this method.  Calls to
         * {@link #next()} remove this reference.
         *
         * @return <code>true</code> if calls to <code>next()</code> will
         * return a value.
         */
        public boolean hasNext() {
            if (mFoundNext) return true;
            while (mIterator.hasNext()) {
                E y = mIterator.next();
                if (accept(y)) {
                    mFoundNext = true;
                    mNext = y;
                    return true;
                }
            }
            return false;
        }


        /**
         * Returns the next value from the contained iterator that is accepted.
         * Acceptance is determined by the method {@link #accept(Object)}.
         *
         * @return Next object from the underlying iterator that passes
         * the acceptance test.
         * @throws NoSuchElementException If there are no more elements in
         * the underlying iterator that pass the acceptance test.
         */
        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();
            mFoundNext = false;
            E result = mNext;
            mNext = null;
            return result;
        }

        /**
         * This operation is not supported.
         *
         * <P><i>Implementation Note:</i> Because calls to {@link
         * #hasNext()} must iterate over the contained iterator to find
         * the next acceptable object to return, it is not guaranteed that
         * the last element returned by this iterator is the same as the
         * last element returned by the underlying iterator.  Thus the
         * underlying iterator can't be used to do the removal without
         * tripping the fail-fast behavior of {@link Iterator}.
         *
         * @throws UnsupportedOperationException Always.
         */
        public void remove() {
            String msg = "Cannot remove from a filtered iterator.";
            throw new UnsupportedOperationException(msg);
        }

    }


    /**
     * An <code>Iterator.Modifier</code> uses a single abstract method
     * to operate on the elements returned by an underlying iterator
     * to return modified objects.  The {@link #remove()} and {@link
     * #hasNext()} methods are simply passed to the underlying iterator.
     * This implements the filter pattern, which is known as a map in
     * functional programming.
     *
     * @author  Bob Carpenter
     * @version 3.0
     * @since   LingPipe2.1
     */
    public static abstract class Modifier<E> implements Iterator<E> {
        private final Iterator<? extends E> mIt;

        /**
         * Construct a modifier from an underlying iterator.
         *
         * @param it Underlying iterator.
         */
        public Modifier(Iterator<? extends E> it) {
            mIt = it;
        }

        /**
         * Remove the next element by delegating the call to the the
         * underlying iterator.
         */
        public void remove() {
            mIt.remove();
        }

        /**
         * Returns <code>true</code> if the underlying iterator has
         * more elements.  This method is simply delegated to the
         * underlying iterator.
         *
         * @return <code>true</code> if this iterator has more
         * elements.
         */
        public boolean hasNext() {
            return mIt.hasNext();
        }

        /**
         * Returns the next element for this iterator, which is
         * the object returned by the underlying iterator after
         * undergoing modification by {@link #modify(Object)}.
         *
         * @return The modified next element from the underlying
         * iterator.
         */
        public E next() {
            return modify(mIt.next());
        }

        /**
         * This abstract method is applied to objects returned by the
         * underlying iterator to create the object returned by this
         * iterator. This method is called once for each call to
         * {@link #next()}.
         *
         * @param next Object returned by underlying iterator.
         * @return Object returned by this iterator.
         */
        public abstract E modify(E next);

    }

    /**
     * An <code>Iterator.Buffered</code> uses a single method to return
     * objects, buffering the result and returning it as the next element
     * if it is non-<code>null</code>.  This class does not support
     * <code>null</code> return values for {@link #next()}.  The {@link
     * #remove()} operation is unsupported, but may be overridden.
     *
     * @author  Bob Carpenter
     * @version 3.0
     * @since   LingPipe1.0
     */
    public static abstract class Buffered<E> implements Iterator<E> {

        private E mNext;

        /**
         * Construct a buffered iterator.  This constructor does not
         * do anything.
         */
        protected Buffered() {
            // do nothing
        }

        /**
         * Returns the next object for this iterator, or <code>null</code>
         * if there are no more objects.
         *
         * @return Next object for this iterator.
         */
        protected abstract E bufferNext();

        /**
         * Returns <code>true</code> if the next call to {@link #next()}
         * will return a non-<code>null</code> value.
         *
         * @return <code>true</code> if the next call to {@link #next()}
         * will return a non-<code>null</code> value.
         */
        public boolean hasNext() {
            return mNext != null
                || (mNext = bufferNext()) != null;
        }

        /**
         * Returns the next object for this iterator.
         *
         * @return The next object for this iterator.
         * @throws NoSuchElementException If there are no more elements.
         */
        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();
            E result = mNext;
            mNext = null;
            return result;
        }

        /**
         * Throws an unsupported operation exception unless overridden by
         * a subclass.
         *
         * @throws UnsupportedOperationException Always.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }


    /**
     * An <code>Iterators.Sequence</code> iterates over the elements of an
     * ordered sequence of iterators in turn.  Each iterator is exhausted
     * before moving to the next.  These iterators may be supplied as a
     * pair of iterators, as an array of iterators, or as an iterator of
     * iterators.  The sequence iterator delegates calls to {@link
     * #next()} and {@link #remove()} to the relevant iterator in the
     * underlying sequence of iterators.  For <code>next()</code>, this is
     * the current underlying iterator.  For <code>remove()</code>, it's
     * the last iterator whose <code>next()</code> element was called, and
     * it throws an illegal state exception if there isn't one.
     *
     * <P><I>Implementation Note:</I> Because of the requirements of
     * {@link Iterator#remove()}, a reference to the last iterator is
     * kept, as well as to the current iterator.  Otherwise, the sequence
     * iterator will release resources as soon as possible.  If the
     * supplied sequence is an array, the elements will not be
     * automatically returned from that array; it is simply wrapped in an
     * instance of {@link Iterators.Array}.
     *
     * @author  Bob Carpenter
     * @version 3.0
     * @since   LingPipe1.0
     */
    public static class Sequence<E> implements Iterator<E> {

        private final Iterator<? extends Iterator<? extends E>> mIterators;
        private Iterator<? extends E> mCurrentIterator;
        private Iterator<? extends E> mLastIteratorNextCalled;

        /**
         * Construct a sequenc iterator that calls the pair of
         * iterators specified in turn.
         *
         * @param iterator1 First iterator.
         * @param iterator2 Second iterator.
         */
        public Sequence(Iterator<? extends E> iterator1,
                        Iterator<? extends E> iterator2) {
            this(toIteratorIterator(iterator1,iterator2));
        }


        /**
         * Construct a sequence iterator that calls the iterators in the
         * specified array in the order they are given in the array.
         *
         * @param iterators Sequence of iterators.
         */
        public Sequence(Iterator<? extends E>[] iterators) {
            this(new Iterators.Array<Iterator<? extends E>>(iterators));
        }

        /**
         * Construct a sequence iterator that calls the iterators returned
         * by the iterator of iterators specified.  If one of the elements
         * is not an iterator, <code>hasNext()</code> and
         * <code>next()</code> will throw a
         * <code>ClassCastException</code>.
         *
         * @param iteratorOfIterators Iterator of iterators.
         */
        public Sequence(Iterator<? extends Iterator<? extends E>>
                        iteratorOfIterators) {
            mIterators = iteratorOfIterators;
        }

        /**
         * Returns <code>true</code> if this iterator has another element.
         * This sequence of iterators has another element if it has
         * another iterator that has another element.
         *
         * @return <code>true</code> if this sequence of iterators
         * has another iterator with another element.
         * @throws ClassCastException If an object is returned by
         * the iterator of iterators specified at construction time
         * that is not an iterator.
         */
        public boolean hasNext() {
            if (mCurrentIterator == null)
                nextIterator(); // get started
            for (; mCurrentIterator != null; nextIterator())
                if (mCurrentIterator.hasNext())
                    return true;
            return false;
        }

        /**
         * Return the next element returned by the next iterator in the
         * iterator sequence.
         *
         * @return The next object in the iteration.
         * @throws ClassCastException If an object is returned by
         * the iterator of iterators specified at construction time
         * that is not an iterator.
         */
        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();
            mLastIteratorNextCalled = mCurrentIterator;
            return mCurrentIterator.next();
        }

        /**
         * Removes the last element returned by this iterator from the
         * collection underlying the iterator from which it was returned.
         * The method can only be called once per call to
         * <code>next()</code>.

         * @throws IllegalStateException If <code>next()</code> has not
         * yet been called, or <code>remove()</code> method has
         * been called after the last call to <code>next()</code>.
         */
        public void remove() {
            if (mLastIteratorNextCalled == null)
                throw new IllegalStateException("next() not yet called.");
            mLastIteratorNextCalled.remove();
            mLastIteratorNextCalled = null;
        }

        private void nextIterator() {
            // possible cast exception
            mCurrentIterator =
                mIterators.hasNext()
                ? mIterators.next()
                : null;
        }


        static <E> Iterator<? extends Iterator<? extends E>>
            toIteratorIterator(Iterator<? extends E> it1,
                               Iterator<? extends E> it2) {
                ArrayList<Iterator<? extends E>> list
                    = new ArrayList<Iterator<? extends E>>(2);
                list.add(it1);
                list.add(it2);
                return list.iterator();
        }

    }


}
