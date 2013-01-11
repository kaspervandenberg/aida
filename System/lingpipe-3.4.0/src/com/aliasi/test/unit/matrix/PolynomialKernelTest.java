package com.aliasi.test.unit.matrix;

import com.aliasi.matrix.PolynomialKernel;
import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.SparseFloatVector;
import com.aliasi.matrix.Vector;

import com.aliasi.util.AbstractExternalizable;

import com.aliasi.test.unit.BaseTestCase;

import java.util.Map;
import java.util.HashMap;

import java.io.IOException;

public class PolynomialKernelTest extends BaseTestCase {

    public void testOne() throws ClassNotFoundException, IOException {
        Vector v1 = new DenseVector(new double[] { -1, 2, 3 });
        Vector v2 = new DenseVector(new double[] { 5, -7, 9 });

        PolynomialKernel kernel1
            = new PolynomialKernel(3);

        PolynomialKernel kernel2
            = (PolynomialKernel)
            AbstractExternalizable
            .serializeDeserialize(kernel1);

        double dp = v1.dotProduct(v2);
        double expectedv1v2 = Math.pow(1.0 + dp, 3.0);
        assertEquals(expectedv1v2,
                     kernel1.proximity(v1,v2),
                     0.0001);
        assertEquals(expectedv1v2,
                     kernel2.proximity(v2,v1),
                     0.0001);


    }

}