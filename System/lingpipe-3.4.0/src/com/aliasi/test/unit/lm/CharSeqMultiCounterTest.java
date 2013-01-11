package com.aliasi.test.unit.lm;

import com.aliasi.lm.CharSeqCounter;
import com.aliasi.lm.CharSeqMultiCounter;
import com.aliasi.lm.TrieCharSeqCounter;

import com.aliasi.test.unit.BaseTestCase;

public class CharSeqMultiCounterTest extends BaseTestCase {

    public void testEx() {
        CharSeqCounter[] counters0 = new CharSeqCounter[] { };
        try {
            new CharSeqMultiCounter(counters0);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        TrieCharSeqCounter counter1 = new TrieCharSeqCounter(3);
        CharSeqCounter[] counters1 = new CharSeqCounter[] { counter1 };
        try {
            new CharSeqMultiCounter(counters1);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    public void testOne() {
        TrieCharSeqCounter counter1 = new TrieCharSeqCounter(3);
        TrieCharSeqCounter counter2 = new TrieCharSeqCounter(5);
        assertCounters(counter1,counter2);
    }


    public void testReversed() {
        TrieCharSeqCounter counter1 = new TrieCharSeqCounter(3);
        TrieCharSeqCounter counter2 = new TrieCharSeqCounter(5);
        assertCounters(counter1,counter2);
    }




    public void assertCounters(TrieCharSeqCounter counter1,
                               TrieCharSeqCounter counter2) {


        CharSeqMultiCounter counter
            = new CharSeqMultiCounter(counter2,counter1);

        CharSeqMultiCounter counterA
            = new CharSeqMultiCounter(new CharSeqCounter[] { counter1,
                                                             counter2 });


        char[] cs1 = "acef".toCharArray();
        char[] cs2 = "bdgced".toCharArray();


        counter1.incrementSubstrings(cs1,0,4);
        assertEqualsArray(cs1,counter.charactersFollowing(cs1,0,0));
        assertEqualsArray(cs1,counterA.charactersFollowing(cs1,0,0));
        assertEqualsArray(new char[] { 'e' },
                          counter.charactersFollowing(cs1,0,2));

        assertEquals(0,counter.count(cs1,0,4));
        assertEquals(1,counter.count(cs1,0,3));
        assertEquals(1,counter.count(cs1,0,2));
        assertEquals(1,counter.count(cs1,0,1));
        assertEquals(4,counter.count(cs1,0,0));

        assertEquals(0,counterA.count(cs1,0,4));
        assertEquals(1,counterA.count(cs1,0,3));
        assertEquals(1,counterA.count(cs1,0,2));
        assertEquals(1,counterA.count(cs1,0,1));
        assertEquals(4,counterA.count(cs1,0,0));

        assertEquals(0,counter.extensionCount(cs1,0,3));
        assertEquals(1,counter.extensionCount(cs1,0,2));
        assertEquals(4,counter.extensionCount(cs1,0,0));

        assertEquals(0,counterA.extensionCount(cs1,0,3));
        assertEquals(1,counterA.extensionCount(cs1,0,2));
        assertEquals(4,counterA.extensionCount(cs1,0,0));

        assertEqualsArray(cs1,counter.observedCharacters());
        assertEqualsArray(cs1,counterA.observedCharacters());

        assertEquals(0,counter.numCharactersFollowing(cs1,0,3));
        assertEquals(1,counter.numCharactersFollowing(cs1,0,2));
        assertEquals(4,counter.numCharactersFollowing(cs1,0,0));

        assertEquals(0,counterA.numCharactersFollowing(cs1,0,3));
        assertEquals(1,counterA.numCharactersFollowing(cs1,0,2));
        assertEquals(4,counterA.numCharactersFollowing(cs1,0,0));

        counter2.incrementSubstrings(cs2,0,6);

        assertEquals(1,counter.count(cs2,0,5));
        assertEquals(1,counter.count(cs1,0,2));
        assertEquals(2,counter.count(cs2,3,5));
        assertEquals(2,counter.count(cs1,1,2));
        assertEquals(1,counter.count("ef".toCharArray(),0,2));
        assertEquals(1,counter.count("ed".toCharArray(),0,2));
        assertEquals(2,counter.count("e".toCharArray(),0,1));

        assertEquals(2,counter.extensionCount(new char[] { 'e' }, 0, 1));
        assertEquals(2,counter.extensionCount(cs1,1,2));
        assertEquals(2,counter.extensionCount(cs1,1,3));

        assertEquals(1,counterA.count(cs2,0,5));
        assertEquals(1,counterA.count(cs1,0,2));
        assertEquals(2,counterA.count(cs2,3,5));
        assertEquals(2,counterA.count(cs1,1,2));
        assertEquals(1,counterA.count("ef".toCharArray(),0,2));
        assertEquals(1,counterA.count("ed".toCharArray(),0,2));
        assertEquals(2,counterA.count("e".toCharArray(),0,1));

        assertEquals(2,counterA.extensionCount(new char[] { 'e' }, 0, 1));
        assertEquals(2,counterA.extensionCount(cs1,1,2));
        assertEquals(2,counterA.extensionCount(cs1,1,3));

        assertEqualsArray("abcdefg".toCharArray(),
                          counter.observedCharacters());

        assertEqualsArray("abcdefg".toCharArray(),
                          counterA.observedCharacters());

        counter2.incrementSubstrings("3317".toCharArray(),0,4);

        assertEqualsArray("137abcdefg".toCharArray(),
                          counter.observedCharacters());

        assertEqualsArray("137abcdefg".toCharArray(),
                          counterA.observedCharacters());

        counter1.incrementSubstrings("zyxw".toCharArray(),0,4);
        assertEqualsArray("137abcdefgwxyz".toCharArray(),
                          counter.observedCharacters());

        assertEqualsArray("137abcdefgwxyz".toCharArray(),
                          counterA.observedCharacters());


    }

}
