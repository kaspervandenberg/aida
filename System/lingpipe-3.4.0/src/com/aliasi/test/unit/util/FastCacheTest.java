package com.aliasi.test.unit.util;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.util.FastCache;

public class FastCacheTest extends BaseTestCase {

    public void testOne() {
        int numIts = 100000;
        FastCache cache = new FastCache(5000000,1.0);
        for (long i = 0; i < numIts; ++i)
            cache.put(new Long(i), new int[(int)(i % 10L)]);
        for (long i = 0; i < numIts; ++i)
            assertEquals((int)(i % 10L), 
                         ((int[]) cache.get(new Long(i))).length);
    }

    public void testRecover() {
        // shouldn't blow out memory
        int megabyte = 1000000;
        int numMegabytes = 100;
        FastCache cache = new FastCache(5000000,1.0);
        for (int i = 0; i < numMegabytes; ++i)
            cache.put(new Integer(i),
                      new int[megabyte]);
        succeed();
    }

    public void testConstrux() {
        try {
            new FastCache(0);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            new FastCache(1,0.0);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }
    }

    public void testPrune() {
        FastCache cache = new FastCache(150,0.5);
        int max = 10000;
        for (int i = 0; i < max; ++i)
            cache.put(new Integer(i),new Integer(i/2));
        assertTrue(cache.size() < 75);
    }

    public void testMulti() throws InterruptedException {
        FastCache cache = new FastCache(1000000,1.0);
        int numThreads = 2; // 16; // 128;
        int numEntries = 8; // 128; // 1024;
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; ++i) {
            threads[i] = new Thread(new TestCache(cache,numEntries));
            threads[i].start();
        }
        for (int i = 0; i < numThreads; ++i) {
            threads[i].join();
        }
        for (int i = 0; i < numEntries; ++i) {
            Integer val = (Integer) cache.get(new Integer(i));
            if (val == null) continue;
            assertEquals(val, new Integer(i/2));
        }
    }

    private static class TestCache implements Runnable {
        final FastCache mCache;
        int mNum;
        TestCache(FastCache cache, int num) {
            mCache = cache;
            mNum = num;
        }
        public void run() {
            for (int i = 0; i < mNum; ++i)
                mCache.put(new Integer(i), new Integer(i/2));
        }
    }


}
