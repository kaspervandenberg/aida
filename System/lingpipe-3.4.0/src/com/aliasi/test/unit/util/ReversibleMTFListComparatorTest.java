package com.aliasi.test.unit.util;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.util.ReversibleMTFListComparator;

import java.util.Comparator;

public class ReversibleMTFListComparatorTest extends BaseTestCase {

    static Integer I3 = new Integer(3);
    static Integer I33 = new Integer(33);
    static Integer I8 = new Integer(8);
    static Integer I9 = new Integer(9);

    public void testOne() {
        ReversibleMTFListComparator rmlc
            = new ReversibleMTFListComparator(new Comparator[] {
                FilterComparatorTest.INTEGER_COMPARATOR,
                AD_HOC_COMPARATOR,
                FilterComparatorTest.STRING_COMPARATOR
            });
        Integer[] ints = new Integer[] { I3, I33, I9, I8 };
        rmlc.sort(ints);
        assertEqualsArray(new Integer[] { I3, I8, I9, I33 }, ints);

        rmlc.reverse(FilterComparatorTest.INTEGER_COMPARATOR);
        rmlc.sort(ints);
        assertEqualsArray(new Integer[] { I33, I9, I8, I3 }, ints);

        rmlc.moveToFront(FilterComparatorTest.STRING_COMPARATOR);
        rmlc.sort(ints);
        assertEqualsArray(new Integer[] { I3, I33, I8, I9}, ints);

        rmlc.reverse(FilterComparatorTest.STRING_COMPARATOR);
        rmlc.sort(ints);
        assertEqualsArray(new Integer[] { I9, I8, I33, I3}, ints);

        rmlc.moveToFront(AD_HOC_COMPARATOR);
        rmlc.sort(ints);
        assertEqualsArray(new Integer[] { I33, I9, I8, I3 }, ints);
    }

    private static final Comparator AD_HOC_COMPARATOR
        = new Comparator() {
                public int compare(Object x1, Object x2) {
                    if (x1.equals(I3)) return 1;
                    if (x2.equals(I3)) return -1;
                    if (x1.equals(I33)) return -1;
                    if (x2.equals(I33)) return 1;
                    return 0;
                }
            };


}
