package com.aliasi.test.unit.util;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.util.BoundedPriorityQueue;

import java.util.Comparator;
import java.util.Iterator;

public class BoundedPriorityQueueTest extends BaseTestCase {

    public void testRemove() {
    BoundedPriorityQueue queue 
        = new BoundedPriorityQueue(new IntComparator(),4);
    queue.add(new Integer(1));
    queue.add(new Integer(55));
    queue.add(new Integer(233));
    assertEquals(3,queue.size());
    assertTrue(queue.remove(new Integer(55)));
    assertFalse(queue.remove(new Integer(10001)));
    assertEquals(2,queue.size());
    assertTrue(queue.contains(new Integer(1)));
    assertTrue(queue.contains(new Integer(233)));
    assertFalse(queue.contains(new Integer(55)));
    }
    
    public void testClear() {
    BoundedPriorityQueue queue 
        = new BoundedPriorityQueue(new IntComparator(),4);
    
    assertEquals(0,queue.size());
    queue.clear();
    assertEquals(0,queue.size());
    
    queue.add(new Integer(42));
    assertEquals(1,queue.size());

    queue.add(new Integer(42));
    assertEquals(1,queue.size());

    queue.add(new Integer(43));
    assertEquals(2,queue.size());

    queue.clear();
    assertEquals(0,queue.size());
    }

    public void testOne() {
    BoundedPriorityQueue queue 
        = new BoundedPriorityQueue(new IntComparator(),4);
    assertEquals(0,queue.size());
    Iterator it = queue.iterator();
    assertFalse(it.hasNext());
    assertNull(queue.peek());
    assertNull(queue.pop());
    assertTrue(queue.isEmpty());


    assertTrue(queue.add(new Integer(1)));
    assertTrue(queue.add(new Integer(3)));
    assertEquals(2,queue.size());
    it = queue.iterator();
    assertEquals(new Integer(3), it.next());
    assertEquals(new Integer(1), it.next());
    assertFalse(it.hasNext());

    assertEquals(new Integer(3),queue.peek());

    assertTrue(queue.add(new Integer(50)));
    assertTrue(queue.add(new Integer(20)));
    assertTrue(queue.add(new Integer(7)));
    assertFalse(queue.add(new Integer(0)));
    assertFalse(queue.add(new Integer(4))); // not bigger than smallest = 3 by ordering
    assertFalse(queue.add(new Integer(50)));

    assertEquals(4,queue.size());
    it = queue.iterator();
    assertEquals(new Integer(50), it.next());
    assertEquals(new Integer(20), it.next());
    assertEquals(new Integer(7), it.next());
    assertEquals(new Integer(3), it.next());
    assertFalse(it.hasNext());

    assertTrue(queue.add(new Integer(8)));

    assertEquals(new Integer(50),queue.pop());
    assertEquals(new Integer(20),queue.peek());
    
    }

    private static class IntComparator implements Comparator {
    public int compare(Object obj1, Object obj2) {
        Integer int1 = (Integer) obj1;
        Integer int2 = (Integer) obj2;
        return int1.intValue()/2 - int2.intValue();
    }
    }

}
