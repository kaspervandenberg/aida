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

import com.aliasi.util.Counter;
import com.aliasi.test.unit.BaseTestCase;

public class CounterTest extends BaseTestCase {
    
    public void testConstructor() {
    Counter counter = new Counter();
    assertNotNull(counter);
    
    Counter counter2 = new Counter(2);
    assertEquals(2,counter2.value());
    }

    public void testIncrement() {
    Counter counter = new Counter();
    assertEquals(0,counter.value());
    counter.increment();
    assertEquals(1,counter.value());
    counter.increment();
    assertEquals(2,counter.value());
    counter.increment(17);
    assertEquals(19,counter.value());
    }

    public void testSet() {
    Counter counter = new Counter();
    counter.set(5);
    assertEquals(5,counter.value());
    }

    public void testToString() {
    Counter counter = new Counter();
    assertEquals("0",counter.toString());
    }
}
