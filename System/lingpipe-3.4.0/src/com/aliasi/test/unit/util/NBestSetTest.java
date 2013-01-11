package com.aliasi.test.unit.util;


import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.util.NBestSet;

import java.util.Comparator;
import java.util.Iterator;


public class NBestSetTest extends BaseTestCase {

    public void testOne() {
        NBestSet s = new NBestSet(2);
        s.add(new Integer(1));
        assertEquals(1,s.size());
        s.add(new Integer(2));
        assertEquals(2,s.size());
        s.add(new Integer(3));
        assertEquals(2,s.size());
        assertEquals(new Integer(2),s.first());
        assertEquals(new Integer(3),s.last());
        Iterator it = s.iterator();
        assertEquals(new Integer(2),it.next());
        assertEquals(new Integer(3),it.next());
        assertFalse(it.hasNext());
        s.add(new Integer(1));
        it = s.iterator();
        assertEquals(new Integer(2),it.next());
        assertEquals(new Integer(3),it.next());
        assertFalse(it.hasNext());
        s.add(new Integer(7));
        it = s.iterator();
        assertEquals(new Integer(3),it.next());
        assertEquals(new Integer(7),it.next());
        assertFalse(it.hasNext());
    }


    public void testTwo() {
        Comparator comparator = new Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((Comparable)o2).compareTo(o1);
                }
            };
        NBestSet s = new NBestSet(3,comparator);
        s.add(new Integer(1));
        s.add(new Integer(3));
        s.add(new Integer(5));
        s.add(new Integer(100));
        assertEquals(3,s.size());
        assertEquals(new Integer(5),s.first());
        assertEquals(new Integer(1),s.last());
    }
}
