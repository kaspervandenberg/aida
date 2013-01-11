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

package com.aliasi.test.unit.util;

import com.aliasi.util.Collections;

import com.aliasi.test.unit.BaseTestCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class CollectionsTest extends BaseTestCase {

    public void testIsSingleton() {
        HashSet set = new HashSet();
        assertFalse(Collections.isSingleton(set));
        set.add("a");
        assertTrue(Collections.isSingleton(set));
        set.add("b");
        assertFalse(Collections.isSingleton(set));
    }

    public void testGetFirstList() {
        ArrayList list = new ArrayList();
        boolean threw = false;
        try {
            Collections.getFirst(list);
        } catch (IndexOutOfBoundsException e) {
            threw = true;
        }
        assertTrue(threw);
        list.add("a");
        assertEquals("a",Collections.getFirst(list));
        list.add("b");
        assertEquals("a",Collections.getFirst(list));
    }

    public void testGetFirstSet() {
        HashSet set = new HashSet();
        boolean threw = false;
        try {
            Collections.getFirst(set);
        } catch (NoSuchElementException e) {
            threw = true;
        }
        assertTrue(threw);
        set.add("a");
        assertEquals("a",Collections.getFirst(set));
        set.add("b");
        assertTrue("a".equals(Collections.getFirst(set))
                   || "b".equals(Collections.getFirst(set)));
    }

    public void testNonEmptyIntersection() {
        HashSet set1 = new HashSet();
        HashSet set2 = new HashSet();
        assertFalse(Collections.intersects(set1,set2));
        set1.add("a");
        assertFalse(Collections.intersects(set1,set2));
        set2.add("b");
        assertFalse(Collections.intersects(set1,set2));
        set1.add("c");
        set2.add("c");
        assertTrue(Collections.intersects(set1,set2));
    }

    public void testAddAll() {
        HashSet set = new HashSet();
        assertEquals(0,set.size());
        Collections.addAll(set,new Object[] { });
        assertEquals(0,set.size());
        Collections.addAll(set,new Object[] { "a" });
        assertEquals(1,set.size());
        assertTrue(set.contains("a"));
        Collections.addAll(set,new Object[] { "b", "c" });
        assertEquals(3,set.size());
        assertTrue(set.contains("b"));
    }

    public void testToStringArray() {
        ArrayList list = new ArrayList();
        String[] zeroArray = new String[0];
        String[] oneArray = new String[1];
        String[] twoArray = new String[2];
        assertEqualsArray(new String[] { },
                          Collections.toStringArray(list));
        Collections.toStringArray(list,zeroArray);
        list.add("a");
        assertEqualsArray(new String[] { "a" },
                          Collections.toStringArray(list));
        Collections.toStringArray(list,oneArray);
        assertEqualsArray(new String[] { "a" },
                          oneArray);
        list.add("b");
        Collections.toStringArray(list,twoArray);
        assertEqualsArray(new String[] { "a", "b" },
                          Collections.toStringArray(list));
        assertEqualsArray(new String[] { "a", "b" },
                          twoArray);
        boolean threw = false;
        try {
            Collections.toStringArray(list,oneArray);
        } catch (IndexOutOfBoundsException e) {
            threw = false;
        }
        assertFalse(threw);
        assertEqualsArray(new String[] { "a" },
                          oneArray);
    }


    public void testToIntArray() {
        ArrayList list = new ArrayList();
        assertEqualsArray(Collections.toIntArray(list),
                          new int[] { });
        list.add(new Integer(1));
        assertEqualsArray(Collections.toIntArray(list),
                          new int[] { 1 });
        list.add(new Integer(2));
        assertEqualsArray(Collections.toIntArray(list),
                          new int[] { 1, 2 });

    }

    public void testIntersections() {
        HashSet s1 = new HashSet();
        HashSet s2 = new HashSet();
        assertFalse(Collections.intersects(s1,s2));
        s1.add("a");
        assertFalse(Collections.intersects(s1,s2));
        s2.add("a");
        assertTrue(Collections.intersects(s1,s2));

        HashSet s3 = new HashSet();
        s3.add(new Integer(3));
        s3.add(new Integer(5));
        s3.add(new Integer(7));
        HashSet s4 = new HashSet();
        s4.add(new Integer(2));
        s4.add(new Integer(4));
        s4.add(new Integer(5));
        s4.add(new Integer(9));
        assertTrue(Collections.intersects(s3,s4));
    }

    public void immutableSetTest() {
	HashSet<String> s = new HashSet<String>();
	Set<String> is1
	    = com.aliasi.util.Collections.<String>immutableSet(s);
	assertFullEquals(s,is1);

	s.add("abc");
	assertFalse(s.equals(is1));
	
	Set<String> is2
	    = com.aliasi.util.Collections.<String>immutableSet(s);
	assertFullEquals(s,is2);
	
	s.add("def");
	assertFalse(s.equals(is2));
	
	Set<String> is3
	    = com.aliasi.util.Collections.<String>immutableSet(s);
	assertFullEquals(s,is3);

	try {
	    is1.add("foo");
	    fail();
	} catch (UnsupportedOperationException e) {
	    succeed();
	}

	try {
	    is1.clear();
	    fail();
	} catch (UnsupportedOperationException e) {
	    succeed();
	}

	try {
	    Iterator<String> it = is2.iterator();
	    String s2 = it.next();
	    assertNotNull(s2);
	    it.remove();
	    fail();
	} catch (UnsupportedOperationException e) {
	    succeed();
	}

	try {
	    is1.remove("abc");
	    fail();
	} catch (UnsupportedOperationException e) {
	    succeed();
	}

	try {
	    is1.removeAll(new HashSet<String>());
	    fail();
	} catch (UnsupportedOperationException e) {
	    succeed();
	}

	try {
	    is1.retainAll(new HashSet<String>());
	    fail();
	} catch (UnsupportedOperationException e) {
	    succeed();
	}

    }

}
