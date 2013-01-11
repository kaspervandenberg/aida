package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.test.unit.BaseTestCase;

public class NGramTokenizerFactoryTest extends BaseTestCase {

    public void test1() {
        char[] cs = "abcd".toCharArray();
        assertEqualsArray(new String[] { "ab", "bc", "cd", "abc", "bcd" },
                          new NGramTokenizerFactory(2,3)
              .tokenizer(cs,0,cs.length)
              .tokenize());
        assertEqualsArray(new String[] { "a", "b", "c", "d" },
                          new NGramTokenizerFactory(1,1)
              .tokenizer(cs,0,cs.length)
              .tokenize());
        try {
        new NGramTokenizerFactory(3,2);
        fail();
        } catch (IllegalArgumentException e) {
        succeed();
        }

        try {
        new NGramTokenizerFactory(-2,0);
        fail();
        } catch (IllegalArgumentException e) {
        succeed();
        }
    }

    public void test2() {
        char[] cs = "he".toCharArray();
        assertEqualsArray(new String[] { "he" },
                          new NGramTokenizerFactory(2,4)
              .tokenizer(cs,0,cs.length)
              .tokenize());
    }

    public void test3() {
    char[] cs = "abcd".toCharArray();
    Tokenizer tokenizer = new NGramTokenizerFactory(2,3).tokenizer(cs,0,cs.length);
    assertEquals(-1,tokenizer.lastTokenStartPosition());
    assertEquals("ab",tokenizer.nextToken());
    assertEquals(0,tokenizer.lastTokenStartPosition());
    assertEquals("bc",tokenizer.nextToken());
    assertEquals(1,tokenizer.lastTokenStartPosition());
    assertEquals("cd",tokenizer.nextToken());
    assertEquals(2,tokenizer.lastTokenStartPosition());
    assertEquals("abc",tokenizer.nextToken());
    assertEquals(0,tokenizer.lastTokenStartPosition());
    assertEquals("bcd",tokenizer.nextToken());
    assertEquals(1,tokenizer.lastTokenStartPosition());
    }

}
