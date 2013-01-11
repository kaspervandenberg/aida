package com.aliasi.test.unit.util;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.util.ReversibleComparator;

public class ReversibleComparatorTest extends BaseTestCase {

    public void testOne() {
        ReversibleComparator rc
            = new ReversibleComparator(FilterComparatorTest.INTEGER_COMPARATOR);
        assertEquals(4,rc.compare(new Integer(7),new Integer(3)));
        assertEquals(0,rc.compare(new Integer(7),new Integer(7)));
        assertEquals(-4,rc.compare(new Integer(3),new Integer(7)));

        rc.toggleSortOrder();

        assertEquals(-4,rc.compare(new Integer(7),new Integer(3)));
        assertEquals(0,rc.compare(new Integer(7),new Integer(7)));
        assertEquals(4,rc.compare(new Integer(3),new Integer(7)));

        rc.toggleSortOrder();

        assertEquals(4,rc.compare(new Integer(7),new Integer(3)));
        assertEquals(0,rc.compare(new Integer(7),new Integer(7)));
        assertEquals(-4,rc.compare(new Integer(3),new Integer(7)));

        rc.toggleSortOrder();
        rc.setOriginalSortOrder();

        assertEquals(4,rc.compare(new Integer(7),new Integer(3)));
        assertEquals(0,rc.compare(new Integer(7),new Integer(7)));
        assertEquals(-4,rc.compare(new Integer(3),new Integer(7)));

        rc.setReverseSortOrder();

        assertEquals(-4,rc.compare(new Integer(7),new Integer(3)));
        assertEquals(0,rc.compare(new Integer(7),new Integer(7)));
        assertEquals(4,rc.compare(new Integer(3),new Integer(7)));

    }

}
