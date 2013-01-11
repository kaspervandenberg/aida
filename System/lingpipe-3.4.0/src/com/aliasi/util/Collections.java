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

import java.lang.reflect.Array;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Static utility methods for processing collections.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe1.0
 */
public class Collections {

    /**
     * Forbid instance construction.
     */
    private Collections() { 
        /* no instances */
    }

    /**
     * Returns <code>true</code> if the specified collection contains
     * exactly one member.
     *
     * @param c Collection to test.
     * @return <code>true</code> if the specified collection contains
     * exactly one member.
     */
    public static boolean isSingleton(Collection<?> c) {
        return c.size() == 1;
    }

    /**
     * Returns the first member of the specified list.
     *
     * @param l List whose first member is returned.
     * @return First member of the specified list.
     * @throws IndexOutOfBoundsException If the list is empty.
     */
    public static <E> E getFirst(List<? extends E> l) {
        return l.get(0);
    }

    /**
     * Returns the first member of the specified set.
     *
     * @param s Set whose first member is returned.
     * @return First member of the specified set.
     * @throws NoSuchElementException If the set is empty.
     */
    public static <E> E getFirst(Set<? extends E> s) {
        return s.iterator().next();
    }

    /**
     * Returns <code>true</code> if the specified sets have at least one
     * element in common.
     *
     * @param set1 First set.
     * @param set2 Second set.
     * @return <code>true</code> if the specified sets have at least
     * one element in common.
     */
    public static boolean intersects(Set<?> set1, Set<?> set2) {
        Iterator set1Elements = set1.iterator();
        while (set1Elements.hasNext()) {
            Object element1 = set1Elements.next();
            if (set2.contains(element1)) return true;
        }
        return false;
    }

    /**
     * Adds the elements from the specified array to the
     * specified collection.
     *
     * @param c Collection to which objects are added.
     * @param xs Objects to add to the collection.
     */
    public static <E> void addAll(Collection<? super E> c, E[] xs) {
        for (int i = 0; i < xs.length; ++i)
            c.add(xs[i]);
    }


    /**
     * Returns the elements in the specified collection as an array
     * after converting each to a string.  The strings in the array
     * will be ordered as by the collection's iterator.
     *
     * @param c Collection to convert to an array.
     * @return Elements of specified collection as an array of
     * strings.
     */
    public static String[] toStringArray(Collection c) {
        String[] result = new String[c.size()];
        toStringArray(c,result);
        return result;
    }

    /**
     * Writes the elements in the specified collection into the
     * specified array as strings, beginning with the first position
     * of the array.  The elements in the array will be ordered as by
     * the collection's iterator.  The array may be longer than the
     * collection, in which case nothing is done to the remaining
     * members of the array.
     *
     * @param c Collection to convert to an array.
     * @param members String rray to write collection into.
     * @throws IndexOutOfBoundsException If the size of the collection
     * is greater than the length of the array.
     */
    public static void toStringArray(Collection c, String[] members) {
        Iterator it = c.iterator();
        for (int i = 0; it.hasNext() && i < members.length; ++i) {
            Object obj = it.next();
            members[i] = obj==null?"null":obj.toString();
        }
    }

    /**
     * Returns an array of <code>int</code> consisting
     * of the elements of the specified collection converted
     * to int.   The collection must only consist of <code>Integer</code>
     * values.
     *
     * @param cs Collection of integers to convert to ints.
     * @return Array of ints derived from collection of integers.
     */
    public static int[] toIntArray(Collection cs) {
        int[] result = new int[cs.size()];
        Iterator it = cs.iterator();
        for (int i = 0; it.hasNext(); ++i) {
            result[i] = ((Integer) it.next()).intValue();
        }
        return result;
    }

    /**
     * Returns a string-based representation of the specified set.
     *
     * @param s Set to convert to string.
     * @return String-based representation of the specified set.
     */
    public static String setToString(Set<?> s) {
        StringBuffer sb = new StringBuffer();
        setToStringBuffer(sb,s);
        return sb.toString();
    }

    /**
     * Returns a string-based representation of the specified list.
     *
     * @param ls List to convert to string.
     * @return String-based representation of the specified list.
     */
    public static String listToString(List<?> ls) {
        StringBuffer sb = new StringBuffer();
        listToStringBuffer(sb,ls);
        return sb.toString();
    }

    /**
     * Appends a string-based representation of the specified list
     * to the specified string buffer.
     *
     * @param sb String buffer to which the representation is appended.
     * @param ls List to append as a string.
     */
    public static void listToStringBuffer(StringBuffer sb, List<?> ls) {
        sb.append('<');
        elementsToStringBuffer(sb,ls);
        sb.append('>');
    }

    /**
     * Appends a string-based representation of the specified set
     * to the specified string buffer.
     *
     * @param sb String buffer to which the representation is appended.
     * @param c Set to append as a string.
     */
    public static void setToStringBuffer(StringBuffer sb, Set<?> c) {
        sb.append('{');
        elementsToStringBuffer(sb,c);
        sb.append('}');
    }

    /**
     * Appends a string-based representation of the specified colleciton
     * to the specified string buffer.
     *
     * @param sb String buffer to which the representation is appended.
     * @param c Collection to append as a string.
     */
    public static void elementsToStringBuffer(StringBuffer sb, 
					      Collection<?> c) {
        Iterator it = c.iterator();
        for (int i = 0; it.hasNext(); ++i) {
            if (i > 0) sb.append(',');
            sb.append(it.next());
        }
    }

    public static <E> Set<E> immutableSet(Collection<? extends E> elts) {
	return new ImmutableSet<E>(elts);
    }

    public static <E> Set<E> immutableSet(E[] elts) {
	return new ImmutableSet<E>(elts);
    }
    
    static class ImmutableSet<E> extends HashSet<E> {
	final int mHashCode;

	// copied from java.util.HashSet v1.5
	static final long serialVersionUID = -5024744406713321676L;

	ImmutableSet(Collection<? extends E> elts) {
	    for (E elt : elts) 
		add(elt);
	    mHashCode = super.hashCode();
	}
	ImmutableSet(E[] elts) {
	    for (int i = 0; i < elts.length; ++i)
		add(elts[i]);
	    mHashCode = super.hashCode();
	}
	public int hashCode() {
	    return mHashCode;
	}
	public Iterator<E> iterator() {
	    return java.util.Collections.<E>unmodifiableSet(this).iterator();
	}
	public boolean add(E o){
	    throw new UnsupportedOperationException();
        }
	public boolean remove(Object o) {
	    throw new UnsupportedOperationException();
        }
	public boolean addAll(Collection<? extends E> coll) {
	    throw new UnsupportedOperationException();
        }
	public boolean removeAll(Collection<?> coll) {
	    throw new UnsupportedOperationException();
        }
	public boolean retainAll(Collection<?> coll) {
	    throw new UnsupportedOperationException();
        }
	public void clear() {
	    throw new UnsupportedOperationException();
        }
    }


}
