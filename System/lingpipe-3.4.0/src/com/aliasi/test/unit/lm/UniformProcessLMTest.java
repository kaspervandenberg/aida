package com.aliasi.test.unit.lm;

import com.aliasi.lm.UniformProcessLM;

import com.aliasi.test.unit.BaseTestCase;

import java.io.IOException;


public class UniformProcessLMTest extends BaseTestCase {

    public void testProcess() {
        UniformProcessLM lm
            = new UniformProcessLM(32);
        assertEquals(0.0,
                     lm.log2Estimate(new char[0],0,0),
                     0.005);
        assertEquals(0.0,
                     lm.log2Estimate(new char[] { 'a' },0,0),
                     0.005);
        assertEquals(-5.0,
                     lm.log2Estimate(new char[] { 'a' },0,1),
                     0.005);
        lm.train("foo");
        assertEquals(-5.0,
                     lm.log2Estimate(new char[] { 'a' },0,1),
                     0.005);
        assertEquals(-10.0,
                     lm.log2Estimate(new char[] { 'a', 'b' },0,2),
                     0.005);
    }

    public void testSerializable() throws ClassNotFoundException, IOException {
        UniformProcessLM lm
            = new UniformProcessLM(32);

        Object serDeser = UniformBoundaryLMTest.compileRead(lm);
        UniformProcessLM lmIO
            = (UniformProcessLM) serDeser;
        assertEquals(-0.0,
                     lmIO.log2Estimate(new char[0],0,0),
                     0.005);
        assertEquals(-5.0,
                     lmIO.log2Estimate(new char[] { 'a' },0,1),
                     0.005);
        assertEquals(-10.0,
                     lmIO.log2Estimate(new char[] { 'a', 'b' },0,2),
                     0.005);
    }

    public void testExs() {
        try {
            new UniformProcessLM(-1);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            new UniformProcessLM(Integer.MAX_VALUE);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            new UniformProcessLM(-1);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            new UniformProcessLM(Integer.MAX_VALUE);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

    }

}
