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

import com.aliasi.util.SmallSet;
import com.aliasi.test.unit.BaseTestCase;

public class SmallSetTest extends BaseTestCase {

    private SmallSet setEmpty;
    private SmallSet setEmptyC;
    private SmallSet set1;
    private SmallSet set1C;
    private SmallSet set2;
    private SmallSet set3;
    private SmallSet set12;
    private SmallSet set123;

    public void setUp() {
        setEmpty = SmallSet.create();
        setEmptyC = SmallSet.create();
        set1 = SmallSet.create(new Integer(1));
        set1C = SmallSet.create(new Integer(1));
        set2 = SmallSet.create(new Integer(2));
        set3 = SmallSet.create(new Integer(3));
        set12 = SmallSet.create(new Integer(1), new Integer(2));
        set123 = SmallSet.create(new Integer[] {
            new Integer(1), new Integer(2), new Integer(3) });
        // just test creation on these
        SmallSet.create(new Integer[] {
            new Integer(1), new Integer(2), new Integer(3), new Integer(4) });
        SmallSet.create(new Integer[] {
            new Integer(1), new Integer(2), new Integer(3), new Integer(4), new Integer(5) });
    }

    public void testEmptySet() {
        assertEquals(setEmpty.size(),0);
        assertFullEquals(setEmpty,setEmptyC);
        assertFalse(setEmpty.contains(new Integer(0)));
        testUnion(setEmpty,setEmpty,setEmpty);
    }

    public void testSingleton() {
        assertEquals(set1.size(),1);
        assertFalse(set1.contains(new Integer(0)));
        assertTrue(set1.contains(new Integer(1)));
        assertFullEquals(set1,set1C);
        testUnion(set1,setEmpty,set1);
        assertEquals(set1,SmallSet.create(new Integer(1),new Integer(1)));
    }

    public void testPair() {
        assertEquals(set12.size(),2);
        assertFalse(set12.contains(new Integer(0)));
        assertTrue(set12.contains(new Integer(1)));
        assertTrue(set12.contains(new Integer(2)));
        testUnion(set1,set2,set12);
        testUnion(set1,set12,set12);
        testUnion(setEmpty,set12,set12);
    }

    public void testTriple() {
        assertEquals(set123.size(),3);
        assertTrue(set123.contains(new Integer(1)));
        assertTrue(set123.contains(new Integer(2)));
        assertTrue(set123.contains(new Integer(3)));
        assertFalse(set123.contains(new Integer(0)));
        testUnion(set12,set3,set123);
        testUnion(set123,setEmpty,set123);
        testUnion(set123,set1,set123);
        testUnion(set123,set12,set123);
        testUnion(set123,set123,set123);
    }

    public void testUnion(SmallSet set1, SmallSet set2, SmallSet result) {
        assertFullEquals(set1.union(set2),result);
        assertFullEquals(set2.union(set1),result);
    }

}
