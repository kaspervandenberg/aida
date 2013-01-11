package com.aliasi.test.unit.matrix;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.matrix.ProximityMatrix;

public class ProximityMatrixTest extends BaseTestCase {

    public void testOne() {
        ProximityMatrix matrix = new ProximityMatrix(10);
        for (int i = 0; i < 10; ++i) {
            for (int j = i + 1; j < 10; ++j) {
                matrix.setValue(i,j,i*j);
            }
        }
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                if (i == j) assertEquals(0.0,matrix.value(i,j),0.0001);
                else assertEquals((double)(i*j),matrix.value(i,j),0.0001);
            }
        }
    }


    public void testExs() {
        try {
            new ProximityMatrix(-1);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }


        try {
            new ProximityMatrix(7).value(7,3);
            fail();
        } catch (IndexOutOfBoundsException e) {
            succeed();
        }


        try {
            new ProximityMatrix(7).value(3,7);
            fail();
        } catch (IndexOutOfBoundsException e) {
            succeed();
        }


        try {
            new ProximityMatrix(7).value(3,-1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            succeed();
        }


        try {
            new ProximityMatrix(7).value(-1,3);
            fail();
        } catch (IndexOutOfBoundsException e) {
            succeed();
        }


    }

}
