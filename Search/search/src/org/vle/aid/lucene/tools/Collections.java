
package org.vle.aid.lucene.tools;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Static utility methods for processing collections.
 *
 */
public class Collections {

    /**
     * Forbid instance construction.
     */
    private Collections() { }

    /**
     * Returns <code>true</code> if the specified collection contains
     * exactly one member.
     *
     * @param c Collection to test.
     * @return <code>true</code> if the specified collection contains
     * exactly one member.
     */
    public static boolean isSingleton(Collection c) {
        return c.size() == 1;
    }

    /**
     * Returns the first member of the specified list.
     *
     * @param l List whose first member is returned.
     * @return First member of the specified list.
     * @throws IndexOutOfBoundsException If the list is empty.
     */
    public static Object getFirst(List l) {
        return l.get(0);
    }

    /**
     * Returns the first member of the specified set.
     *
     * @param s Set whose first member is returned.
     * @return First member of the specified set.
     * @throws NoSuchElementException If the set is empty.
     */
    public static Object getFirst(Set s) {
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
    public static boolean intersects(Set set1, Set set2) {
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
    public static void addAll(Collection c, Object[] xs) {
        for (int i = 0; i < xs.length; ++i)
            c.add(xs[i]);
    }

    /**
     * Returns the elements in the specified collection as an array.
     * The elements in the array will be ordered as by the
     * collection's iterator.
     *
     * @param c Collection to convert to an array.
     * @return Elements of specified collection as an array.
     */
    public static Object[] toArray(Collection c) {
        Object[] result = new Object[c.size()];
        toArray(c,result);
        return result;
    }

    /**
     * Writes the elements in the specified collection into the
     * specified array, beginning with the first position of the
     * array.  The elements in the array will be ordered as by the
     * collection's iterator.  The array may be longer than the
     * collection, in which case nothing is done to the remaining
     * members of the array.  If the collection has more elements
     * than the array will fit, only the first elements from the
     * collection are written.
     *
     * @param c Collection to convert to an array.
     * @param members Array to write collection into.
     */
    public static void toArray(Collection c, Object[] members) {
        Iterator it = c.iterator();
        for (int i = 0; it.hasNext() && i < members.length; ++i) {
            members[i] = it.next();
        }
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
    public static String setToString(Set s) {
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
    public static String listToString(List ls) {
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
    public static void listToStringBuffer(StringBuffer sb, List ls) {
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
    public static void setToStringBuffer(StringBuffer sb, Set c) {
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
    public static void elementsToStringBuffer(StringBuffer sb, Collection c) {
        Iterator it = c.iterator();
        for (int i = 0; it.hasNext(); ++i) {
            if (i > 0) sb.append(',');
            sb.append(it.next());
        }
    }

    /**
     * Returns <code>true</code> if the specified sets have a non-empty
     * intersection.
     *
     * @param s1 First set.
     * @param s2 Second set.
     * @return <code>true</code> if the sets have a non-empty intersection.
     */
    public static boolean hasIntersection(Set s1, Set s2) {
        if (s1.size() > s2.size()) return hasIntersection(s2,s1);
        Iterator elts = s1.iterator();
        while (elts.hasNext())
            if (s2.contains(elts.next())) return true;
        return false;
    }


}
