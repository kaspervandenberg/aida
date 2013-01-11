package com.aliasi.test.unit.util;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.util.Iterators;

import com.aliasi.test.unit.BaseTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.NoSuchElementException;



public class IteratorsTest extends BaseTestCase {

    public void testOne() {
        List xs = Arrays.asList(new String[0]);
        assertEqualsIterations(xs.iterator(),
                               new TrueIterator(xs.iterator()));
        assertEqualsIterations(xs.iterator(),
                               new FalseIterator(xs.iterator()));
        List ys = Arrays.asList(new String[] { "a", "b", "c" });
        assertEqualsIterations(ys.iterator(),
                               new TrueIterator(ys.iterator()));
        assertEqualsIterations(xs.iterator(),
                               new FalseIterator(ys.iterator()));
        List zs1 = Arrays.asList(new String[] { "a", "b", "c" });
        List zs1a = Arrays.asList(new String[] { "b", "c" });
        assertEqualsIterations(zs1a.iterator(),
                               new RemoveIterator(zs1.iterator(),"a"));
        List zs1b = Arrays.asList(new String[] { "a", "c" });
        assertEqualsIterations(zs1b.iterator(),
                               new RemoveIterator(zs1.iterator(),"b"));
        List zs1c = Arrays.asList(new String[] { "a", "b" });
        assertEqualsIterations(zs1c.iterator(),
                               new RemoveIterator(zs1.iterator(),"c"));


    }

    public void testTwo() {
        List xs = Arrays.asList(new String[] { "a", "b" });
        TrueIterator it = new TrueIterator(xs.iterator());
        it.next();
        try {
            it.remove();
            fail("Remove should be unsupported.");
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
    }

    public void testThree() {
        List xs = Arrays.asList(new String[] { });
        TrueIterator it = new TrueIterator(xs.iterator());
        try {
            assertFalse(it.hasNext());
            it.next();
            // it.next();
            fail("Should not be a next.");
        } catch (NoSuchElementException e) {
            assertTrue(true);
        }
        List ys = Arrays.asList(new String[] { "a", "b" });
        TrueIterator it2 = new TrueIterator(ys.iterator());
        it2.next();
        it2.next();
        try {
            it2.next();
            fail("Should not be a next.");
        } catch (NoSuchElementException e) {
            assertTrue(true);
        }

    }

    static class TrueIterator extends Iterators.Filter {
        public TrueIterator(Iterator it) {
            super(it);
        }
        public boolean accept(Object x) {
            return true;
        }
    }

    static class FalseIterator extends Iterators.Filter {
        public FalseIterator(Iterator it) {
            super(it);
        }
        public boolean accept(Object x) {
            return false;
        }
    }

    static class RemoveIterator extends Iterators.Filter {
        private final String mX;
        public RemoveIterator(Iterator it, String x) {
            super(it);
            mX = x;
        }
        public boolean accept(Object x) {
            return !x.equals(mX);
        }
    }

    public void testBufferedOne() {
        List xs = Arrays.asList(new String[0]);
        Iterator it = new ListBufferedIterator(xs.iterator());
        assertEqualsIterations(xs.iterator(),it);

        List ys = Arrays.asList(new String[] { "a" });
        Iterator it2 = new ListBufferedIterator(ys.iterator());
        assertEqualsIterations(ys.iterator(),it2);
    }

    public void testBufferedTwo() {
        List xs = Arrays.asList(new String[0]);
        Iterator it = new ListBufferedIterator(xs.iterator());
        try {
            it.remove();
            fail("Should not be able to remove from a buffered iterator.");
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
    }

    public void testBufferedThree() {
        List xs = Arrays.asList(new String[0]);
        Iterator it = new ListBufferedIterator(xs.iterator());
        try {
            it.next();
            fail("Should not be a next element.");
        } catch (NoSuchElementException e) {
            assertTrue(true);
        }

        List ys = Arrays.asList(new String[] { "a", "b", "c" });
        Iterator it2 = new ListBufferedIterator(ys.iterator());
        it2.next();
        it2.next();
    it2.next();
        try {
        it2.next();
            fail("Should not be a next element.");
        } catch (NoSuchElementException e) {
            assertTrue(true);
        }

    }

    public static class ListBufferedIterator extends Iterators.Buffered {
        private final Iterator mIterator;
        public ListBufferedIterator(Iterator iterator) {
            mIterator = iterator;
        }
        public Object bufferNext() {
            if (!mIterator.hasNext()) return null;
            while (mIterator.hasNext()) {
                Object next = mIterator.next();
                if (next != null) return next;
            }
            return null;
        }
    }


    public void testSIOne() {
        HashSet s1 = new HashSet();
        Iterators.Sequence it = new Iterators.Sequence(new Iterator[] { 
                                                       s1.iterator()
                                                   });
        assertIteration(it,new Object[] { });
        assertIllegalState(it);

        HashSet s2 = new HashSet();
        Iterators.Sequence it2 = new Iterators.Sequence(new Iterator[] { 
                                                        s1.iterator(),
                                                        s2.iterator()
                                                    });
        assertIteration(it2,new Object[] { });
        assertIllegalState(it2);

        Iterators.Sequence it3 = new Iterators.Sequence(
                                                    s1.iterator(),
                                                    s2.iterator());
        assertIteration(it3,new Object[] { });
        assertIllegalState(it3);
    
    }

    public void testSITwo() {
        HashSet s1 = new HashSet();
        s1.add("a");
        Iterators.Sequence it = new Iterators.Sequence(new Iterator[] {
                                                       s1.iterator()
                                                   });
        assertIteration(it,new Object[] { "a" });
        it.remove();
        assertEquals(Collections.EMPTY_SET,s1);
        assertIllegalState(it);

        List xs = Arrays.asList(new String[] { "a", "b" });
        List ixs = Collections.unmodifiableList(xs); 
        Iterators.Sequence it2 = new Iterators.Sequence(new Iterator[] {
                                                        ixs.iterator()
                                                    });
        assertIteration(it2,new Object[] { "a", "b" });
        assertUnsupportedOperation(it2); // explicitly not modifiable

        ArrayList xs2 = new ArrayList();
        xs2.add("a");
        xs2.add("b");
        Iterators.Sequence it3 = new Iterators.Sequence(new Iterator[] {
                                                        xs2.iterator()
                                                    });
        assertIteration(it3,new Object[] { "a", "b" });
        it3.remove();
        ArrayList xs2expected = new ArrayList();
        xs2expected.add("a");
        assertEquals(xs2expected,xs2);

        ArrayList xs3 = new ArrayList();
        xs3.add("a");
        xs3.add("b");
        Iterators.Sequence it4 = new Iterators.Sequence(new Iterator[] {
                                                        xs3.iterator()
                                                    });
        it4.next();
        it4.remove();
        ArrayList xs3expected = new ArrayList();
        xs3expected.add("b");
        assertEquals(xs3expected,xs3);
    }

    public void testSIThree() {
        ArrayList ab = new ArrayList();
        ab.add("a");
        ab.add("b");
        ArrayList c = new ArrayList();
        c.add("c");
        ArrayList de = new ArrayList();
        de.add("d");
        de.add("e");
        Iterators.Sequence it = new Iterators.Sequence(new Iterator[] {
                                                       ab.iterator(), c.iterator(), de.iterator()
                                                   });

        assertIteration(it,new Object[] { "a", "b", "c", "d", "e" });
        
    }

    public void testSIFour() {
        ArrayList ab = new ArrayList();
        ab.add("a");
        ab.add("b");
        ArrayList empty = new ArrayList();
        ArrayList cde = new ArrayList();
        cde.add("c");
        cde.add("d");
        cde.add("e");

        assertIteration(new Iterators.Sequence(new Iterator[] {
                                                 empty.iterator(),
                                                 ab.iterator(), 
                                                 cde.iterator(),
                                             }),
                        new Object[] { "a", "b", "c", "d", "e" });
        assertIteration(new Iterators.Sequence(new Iterator[] {
                                                 ab.iterator(), 
                                                 empty.iterator(), 
                                                 cde.iterator()
                                             }),
                        new Object[] { "a", "b", "c", "d", "e" });

        assertIteration(new Iterators.Sequence(new Iterator[] {
                                                 ab.iterator(), 
                                                 cde.iterator(),
                                                 empty.iterator()
                                             }),
                        new Object[] { "a", "b", "c", "d", "e" });


        assertIteration(new Iterators.Sequence(new Iterator[] {
                                                 empty.iterator(),
                                                 empty.iterator(),
                                                 ab.iterator(), 
                                                 empty.iterator(),
                                                 empty.iterator(),
                                                 cde.iterator(),
                                                 empty.iterator(),
                                                 empty.iterator()
                                             }),
                        new Object[] { "a", "b", "c", "d", "e" });

        
    }

    public void testSIFive() {
        ArrayList ab = new ArrayList();
        ab.add("a");
        ab.add("b");
        ArrayList cd = new ArrayList();
        cd.add("c");
        cd.add("d");
        Iterators.Sequence it = new Iterators.Sequence(new Iterator[] {
                                                       ab.iterator(), cd.iterator()
                                                   });
        it.next();
        it.next();
        it.remove();
        ArrayList a = new ArrayList();
        a.add("a");
        assertEquals(a,ab);
        it.next();
        it.remove();
        ArrayList d = new ArrayList();
        d.add("d");
        assertEquals(d,cd);
    }

    public void testSISix() {
        ArrayList empty = new ArrayList();
        ArrayList ab = new ArrayList();
        ab.add("a");
        ab.add("b");
        ArrayList cd = new ArrayList();
        cd.add("c");
        cd.add("d");
        Iterators.Sequence it = new Iterators.Sequence(new Iterator[] {
                                                       ab.iterator(), empty.iterator(),
                                                       empty.iterator(), cd.iterator()
                                                   });
        it.next();
        it.next();
        it.remove();
        ArrayList a = new ArrayList();
        a.add("a");
        assertEquals(a,ab);
        it.next();
        it.remove();
        ArrayList d = new ArrayList();
        d.add("d");
        assertEquals(d,cd);
    }


    public void assertIteration(Iterator it,
                                Object[] values) {
        assertIllegalState(it); // illegal < start
        for (int i = 0; i < values.length; ++i) {
            if (!it.hasNext()) fail();
            assertEquals(it.next(),values[i]);
        }
        assertFalse(it.hasNext());
    }
    
    public void assertIllegalState(Iterator it) {
        boolean threw = false;
        try {
            it.remove();
        } catch (IllegalStateException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    public void assertUnsupportedOperation(Iterator it) {
        boolean threw = false;
        try {
            it.remove();
        } catch (UnsupportedOperationException e) {
            threw = true;
        }
        assertTrue(threw);
    }



    public void testArrayIterators() {
        assertIteration(new Integer[] { });
        assertIteration(new Integer[] { new Integer(0) });
        assertIteration(new Integer[] { new Integer(0),
                                        new Integer(1) });
        assertIteration(new Integer[] { new Integer(0),
                                        new Integer(1),
                                        new Integer(2)  });
        assertIteration(new Integer[] { new Integer(0),
                                        new Integer(1),
                                        new Integer(2),
                                        new Integer(3) });

        Object[] xs = new Object[] { "a", "b", "c" };
        assertNotNull(xs[1]);
        Iterator it = new Iterators.Array(xs);
        it.next();
        it.next();
        it.remove();
        assertNull(xs[1]);

        Object[] ys = new Object[] { "a" };
        assertNotNull(ys[0]);
        it = new Iterators.Array(ys);
        boolean threw = false;
        try {
            it.remove();
        } catch (IllegalStateException e) {
            threw = true;
        }
        assertTrue(threw);
        threw = false;
        it.next();
        it.remove();
        try {
            it.remove();
        } catch (IllegalStateException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    public void assertIteration(Object[] objs) {
        Iterator it = new Iterators.Array(objs);
        for (int i = 0; i < objs.length; ++i) {
            assertEquals(objs[i],it.next());
        }
        assertFalse(it.hasNext());
        boolean threw = false;
        try {
            it.next();
        } catch (NoSuchElementException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    public void testSliceIterator() {
        Object[] xs = new Object[] { };
        assertSliceIterator(xs,0,0);

        Object[] ys = new Object[] { "A" };
        assertSliceIterator(ys,0,1);

        Object[] zs = new Object[] { "A", "B", "C", "D" };
        assertSliceIterator(zs,0,4);
        assertSliceIterator(zs,2,2);
        assertSliceIterator(zs,0,2);
        assertSliceIterator(zs,2,1);
    }

    public void assertSliceIterator(Object[] objs, int start, int length) {
        Iterator it = new Iterators.ArraySlice(objs,start,length);
        for (int i = 0; i < length; ++i) {
            assertEquals(it.next(),objs[start+i]);
        }
        assertFalse(it.hasNext());

        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
            succeed();
        }
    }


}

