package com.aliasi.test.unit.matrix;

import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.Vector;

import com.aliasi.test.unit.BaseTestCase;

import java.util.Arrays;
import java.util.List;

public class DenseVectorTest extends BaseTestCase {

    public void testVectorOps() {
        Vector v1 = new DenseVector(new double[] { 1, 1, 0});
        Vector v2 = new DenseVector(new double[] { 1, 0, 1});
        assertEquals(Math.sqrt(2),v1.length(),0.0001);
        assertEquals(Math.sqrt(2),v2.length(),0.0001);
        assertEquals(1.0,v1.dotProduct(v2),0.0001);
        assertEquals(1.0,v2.dotProduct(v1),0.0001);
        assertEquals(1.0/2.0,v1.cosine(v2),0.0001);
        assertEquals(1.0/2.0,v2.cosine(v1),0.0001);

        Vector v3 = new DenseVector(new double[] { 3, 5 });
        try {
            v1.dotProduct(v3);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            v3.dotProduct(v1);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            v1.cosine(v3);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            v3.cosine(v1);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }


    public void testSized() {
        Vector v = new DenseVector(2);
        assertEquals(2,v.numDimensions());
        assertEquals(0.0,v.value(0),0.001);

        v.setValue(0,5.0);
        assertEquals(5.0,v.value(0),0.001);

        v.setValue(1,2.0);
        assertEquals(2.0,v.value(1),0.0001);

    }

    public void testHashCode() {
        Vector v = new DenseVector(new double[] { 1, 2, 3 });
        List list = Arrays.asList(new Double[] {
            new Double(1),
            new Double(2),
            new Double(3) });
        assertEquals(list.hashCode(),v.hashCode());
    }

    public void testEquals() {
        Vector v1 = new DenseVector(new double[] { 1, 3, 7, 12 });
	Vector v2 = new DenseVector(4);
        v2.setValue(0,1);
        v2.setValue(1,3);
        v2.setValue(2,7);
        v2.setValue(3,12);
        assertFullEquals(v1,v2);
    }

    public void testAllocated() {
        Vector v = new DenseVector(new double[] { 1, 2, 3});
        assertEquals(3,v.numDimensions());
        assertEquals(2.0,v.value(1),0.0001);

        Vector v2 = new DenseVector(new double[] { 1, 2, 3});
        assertEquals(3,v2.numDimensions());
        assertEquals(2.0,v2.value(1),0.0001);

        Vector v3 = new DenseVector(2);
        assertEquals(2,v3.numDimensions());
        assertEquals(0.0,v3.value(1),0.0001);
    }


    public void testConstructorExs() {
        try {
            new DenseVector(new double[0]);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            new DenseVector(0);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }


    }



}
