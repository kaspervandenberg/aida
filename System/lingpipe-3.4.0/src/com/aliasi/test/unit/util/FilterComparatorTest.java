package com.aliasi.test.unit.util;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.util.FilterComparator;

import java.util.Comparator;

public class FilterComparatorTest extends BaseTestCase {

    public void testOne() {
        FilterComparator fc = new FilterComparator(INTEGER_COMPARATOR);
        assertEquals(4,fc.compare(new Integer(7),new Integer(3)));
        assertEquals(0,fc.compare(new Integer(7),new Integer(7)));
        assertEquals(-4,fc.compare(new Integer(3),new Integer(7)));
    }

    public static final Comparator INTEGER_COMPARATOR
        = new Comparator() {
                public int compare(Object x1, Object x2) {
                    return ((Integer)x1).intValue()
                        - ((Integer)x2).intValue();
                }
            };


    public static final Comparator STRING_COMPARATOR
        = new Comparator() {
                public int compare(Object x1, Object x2) {
                    return x1.toString().compareTo(x2.toString());
                }
            };
}
