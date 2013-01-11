package com.aliasi.test.unit.matrix;

import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.SparseFloatVector;
import com.aliasi.matrix.Vector;

import com.aliasi.test.unit.BaseTestCase;

import java.util.HashMap;
import java.util.Map;

public class SparseFloatVectorTest extends BaseTestCase {

    // public void testMultiply() {
    // Map<Integer,Float> map = new HashMap<Integer,Float>();
    // map.put(new Integer(3), new Float(3.0));
    // map.put(new Integer(5), new Float(5.0));
    //
    // double len = Math.sqrt(3.0*3.0 + 5.0*5.0);
    //
    // Vector vec = new SparseFloatVector(map);
    // Vector negVec = vec.multiply(-1);
    // Vector vec2 = vec.multiply(2);
    // assertEquals(len,vec.length(),0.0001);
    // assertEquals(len,negVec.length(),0.0001);
    // assertEquals(2*len,vec2.length(),0.0001);
    //
    // assertEquals(10.0,vec2.value(5));
    // assertEquals(6.0,vec2.value(3));
    // assertEquals(0.0,vec2.value(1));
    // }

    public void testSerialization() {
        Map<Integer,Float> map = new HashMap<Integer,Float>();

        assertFullSerialization(new SparseFloatVector(map));

        map.put(0,1.0f);
        assertFullSerialization(new SparseFloatVector(map));

        map.put(17,2.0f);
        assertFullSerialization(new SparseFloatVector(map));

        map.put(Integer.MAX_VALUE-1,3.0f);
        assertFullSerialization(new SparseFloatVector(map));
    }

    public void testZero() {
        Map<Integer,Float> map = new HashMap<Integer,Float>();
        Vector vec0 = new SparseFloatVector(map);
        assertEquals(0,vec0.numDimensions());
        assertEquals(0.0,vec0.length(),0.001);
        assertEquals(0.0,vec0.dotProduct(vec0));

        try {
            vec0.setValue(2,5.0);
            fail();
        } catch (UnsupportedOperationException e) {
            succeed();
        }

        try {
            vec0.value(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
            succeed();
        }

        assertFullEquals(vec0,vec0);
    }

    public void testOne() {
        Map<Integer,Float> map1 = new HashMap<Integer,Float>();
        map1.put(new Integer(3),new Float(5.0f));
        Vector vec1 = new SparseFloatVector(map1);

        assertFullEquals(vec1,vec1);
        assertEquals(4,vec1.numDimensions());
        assertEquals(5.0,vec1.length(),0.001);
        assertEquals(25.0,vec1.dotProduct(vec1));
        assertEquals(1.0,vec1.cosine(vec1));

        Map<Integer,Float> map2 = new HashMap<Integer,Float>();
        map2.put(new Integer(3),new Float(7.0f));
        map2.put(new Integer(1),new Float(9.0f));
        Vector vec2 = new SparseFloatVector(map2);
        assertFullEquals(vec2,vec2);
        assertFalse(vec1.equals(vec2));
        assertEquals(4,vec2.numDimensions());
        assertEquals(1.0,vec2.cosine(vec2));
        assertEquals(Math.sqrt(81.0 + 49.0), vec2.length(), 0.0001);
        assertEquals(35.0,vec1.dotProduct(vec2), 0.0001);
    }

    public void testTwo() {
        Map<Integer,Float> map1 = new HashMap<Integer,Float>();
        map1.put(new Integer(0),new Float(3.0f));
        map1.put(new Integer(1),new Float(5.0f));
        map1.put(new Integer(5),new Float(7.0f));
        Vector vec1 = new SparseFloatVector(map1);


        Map<Integer,Float> map2 = new HashMap<Integer,Float>();
        map2.put(new Integer(0), new Float(11.0f));
        map2.put(new Integer(1), new Float(13.0f));
        map2.put(new Integer(3), new Float(17.0f));
        Vector vec2 = new SparseFloatVector(map2,6);

        double len1 = Math.sqrt(3.0*3.0 + 5.0*5.0 + 7.0*7.0);
        double len2 = Math.sqrt(11.0*11.0 + 13.0*13.0 + 17.0*17.0);
        double product = 3.0*11.0 + 5.0*13.0;
        double cos = product / (len1 * len2);
        assertEquals(product,vec1.dotProduct(vec2),0.0001);
        assertEquals(cos,vec1.cosine(vec2),0.0001);
    }

    public void testMixed() {
        Map<Integer,Float> map1 = new HashMap<Integer,Float>();
        map1.put(new Integer(0),new Float(3.0f));
        map1.put(new Integer(1),new Float(5.0f));
        map1.put(new Integer(5),new Float(7.0f));
        Vector vec1 = new SparseFloatVector(map1);

        Vector vec3 = new DenseVector(new double[] { 3.0, 5.0, 0.0, 0.0, 0.0, 7.0 });
        assertFullEquals(vec1,vec3);
        // assertTrue(vec1.equals(vec3));
        // assertEquals(vec1.hashCode(), vec3.hashCode());
    }

    public void testNumber() {
        Map<Integer,Number> map1 = new HashMap<Integer,Number>();
        map1.put(new Integer(0), new Integer(1));
        map1.put(new Integer(2), new Double(3.0));

        Vector vec = new SparseFloatVector(map1);
        assertEquals(1.0,vec.value(0),0.0001);
        assertEquals(0.0,vec.value(1),0.0001);
        assertEquals(3.0,vec.value(2),0.0001);
    }

}