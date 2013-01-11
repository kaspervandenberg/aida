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

import com.aliasi.util.Arrays;

import com.aliasi.test.unit.BaseTestCase;

import java.util.Iterator;
import java.util.NoSuchElementException;

import java.util.HashSet;
import java.util.Set;

public class ArraysTest extends BaseTestCase {

    public void testPermute() {
        Integer[] xs = new Integer[0];
        Arrays.<Integer>permute(xs);

        xs = new Integer[1];
        xs[0] = new Integer(5);
        Arrays.<Integer>permute(xs);
        assertEquals(new Integer(5), xs[0]);

        xs = new Integer[2];
        xs[0] = new Integer(0);
        xs[1] = new Integer(1);
        Arrays.<Integer>permute(xs);
        assertTrue(( xs[0].equals(new Integer(0))
                     && xs[1].equals(new Integer(1)) )
                   ||
                   ( xs[0].equals(new Integer(1))
                     && xs[1].equals(new Integer(0))) );

        xs = new Integer[100];
        for (int i = 0; i < 100; ++i)
            xs[i] = new Integer(i);
        Arrays.<Integer>permute(xs);
        Set<Integer> resultSet = new HashSet<Integer>(200);
        for (int i = 0; i < xs.length; ++i) {
            int val = xs[i].intValue();
            assertTrue(0 <= val && val < 100);
            resultSet.add(xs[i]);
        }
        assertEquals(100,resultSet.size());

    }


    public void testReallocate() {
        int[] xs = new int[] { 1, 2, 3 };
        assertReallocate(xs,5);
        assertReallocate(xs,3);
        assertReallocate(xs,1);
        assertReallocate(xs,0);

        int[] zs = new int[] { };
        assertReallocate(zs,0);
        assertReallocate(zs,3);

    }

    void assertReallocate(int[] xs, int len) {
        int[] ys = Arrays.reallocate(xs,len);
        assertEquals(len,ys.length);
        for (int i = 0; i < xs.length && i < len; ++i)
            assertEquals(xs[i],ys[i]);
        for (int i = xs.length; i < ys.length; ++i)
            assertEquals(0,ys[i]);
    }

    public void testEquals() {
        String[] xs1 = new String[] { "a", "b", "c" };
        String[] xs2 = new String[] { "a", "b", "c" };
        String[] xs3 = new String[] { "a", "b" };
        assertTrue(Arrays.equals(xs1,xs2));
        assertFalse(Arrays.equals(xs2,xs3));
        assertTrue(Arrays.equals(new Object[0], new Object[0]));
    }

    public void testArrayToCSV2D() {
        assertCSV2D("",new String[][] { { "" } });
        assertCSV2D("a",new String[][] { { "a" } });
        assertCSV2D("a,b",new String[][] { { "a", "b" } });
        assertCSV2D("\\\n",new String[][] { { "\n" } });
        assertCSV2D("\n",new String[][] { { "" }, {""} });
        assertCSV2D("\n\n",new String[][] { { "" }, {""}, {""} });
        assertCSV2D("a,b\nc,d",
                    new String[][] {
                        { "a", "b" },
                        { "c", "d" }
                    });
        assertCSV2D("a,b\\\n\nc,d",
                    new String[][] {
                        { "a", "b\n" },
                        { "c", "d" }
                    });

    }

    void assertCSV2D(String expectedEncoding, String[][] expectedElts) {
        String encoding = Arrays.arrayToCSV(expectedElts);
        assertEquals(expectedEncoding + "=?=" + encoding,
                     expectedEncoding,encoding);
        String[][] elts = Arrays.csvToArray2D(expectedEncoding);
        assertEquals(expectedElts.length + "=?=" + elts.length,
                     expectedElts.length,elts.length);
        for (int i = 0; i < elts.length; ++i)
            assertEqualsArray(expectedElts[i],elts[i]);
    }

    public void testArraytoCSV() {
        assertCSV("",
                 new String[] {
                      ""
                  });
        assertCSV(",",
                 new String[] {
                      "", ""
                  });
        assertCSV(",,",
                 new String[] {
                      "","", ""
                  });
        assertCSV(",a",
                 new String[] {
                      "","a"
                  });
        assertCSV("a,",
                 new String[] {
                      "a",""
                  });
        assertCSV("a",
                 new String[] {
                      "a"
                  });
        assertCSV("abc,d",
                 new String[] {
                      "abc", "d"
                  });
        assertCSV("abc,d,e,,f",
                 new String[] {
                      "abc", "d", "e", "", "f"
                  });
        assertCSV("a\\,bc,d\\\\,e,,f",
                 new String[] {
                      "a,bc", "d\\", "e", "", "f"
                  });

        assertCSV("\\\n",
                 new String[] {
                      "\n"
                  });

    }

    void assertCSV(String expectedEncoding, String[] expectedElts) {
        String encoding = Arrays.arrayToCSV(expectedElts);
        assertEquals("decode(" + java.util.Arrays.asList(expectedElts) + ")=" + encoding,
                     expectedEncoding,encoding);

        String[] elts = Arrays.csvToArray(expectedEncoding);
        assertEqualsArray("decode(" + expectedEncoding + ")="
                          + java.util.Arrays.asList(elts),
                          expectedElts,elts);
    }


    public void testMemberObject() {
        assertFalse(Arrays.member("a",null));
        assertFalse(Arrays.member("a",new Object[] { "b", null }));
        assertFalse(Arrays.member("a",new Object[] { }));
        assertTrue(Arrays.member("a",new Object[] { "a" }));
        assertTrue(Arrays.member("a",new Object[] { null, "a" }));
    }

    public void testMemberChar() {
        assertFalse(Arrays.member('a',null));
        assertFalse(Arrays.member('a',new char[] { }));
        assertFalse(Arrays.member('a',new char[] { 'b', 'c' }));
        assertTrue(Arrays.member('a',new char[] { 'a' }));
        assertTrue(Arrays.member('a',new char[] { 'b', 'a' }));
    }

    public void testArrayToString() {
        assertEquals("[]",Arrays.arrayToString(new Object[] { }));
        assertEquals("[a]",Arrays.arrayToString(new Object[] { "a" }));
        assertEquals("[a,b]",Arrays.arrayToString(new Object[] { "a", "b" }));
    }

    public void testArrayToStringBuffer() {
        StringBuffer sb = new StringBuffer();
        Arrays.arrayToStringBuffer(sb,new Object[] { });
        assertEquals("[]",sb.toString());
        sb = new StringBuffer();
        Arrays.arrayToStringBuffer(sb,new Object[] { "a" });
        assertEquals("[a]",sb.toString());
        sb = new StringBuffer();
        Arrays.arrayToStringBuffer(sb,new Object[] { "a", "b" });
        assertEquals("[a,b]",sb.toString());
    }

    public void testConcatenate() {
        assertEqualsArray(new String[] { },
                          Arrays.concatenate(new String[] { },
                                             new String[] { }));
        assertEqualsArray(new String[] { "a" },
                          Arrays.concatenate(new String[] { "a" },
                                             new String[] { }));
        assertEqualsArray(new String[] { "b" },
                          Arrays.concatenate(new String[] { },
                                             new String[] { "b" }));
        assertEqualsArray(new String[] { "a","b","c","d" },
                          Arrays.concatenate(new String[] { "a", "b" },
                                             new String[] { "c", "d" }));


    }

}
