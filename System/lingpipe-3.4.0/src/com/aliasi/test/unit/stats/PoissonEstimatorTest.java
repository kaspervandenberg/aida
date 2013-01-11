package com.aliasi.test.unit.stats;

import com.aliasi.stats.PoissonConstant;
import com.aliasi.stats.PoissonEstimator;

import com.aliasi.test.unit.BaseTestCase;

import java.io.*;

public class PoissonEstimatorTest extends BaseTestCase {

    public void testState() {
        PoissonEstimator dist = new PoissonEstimator();
        try {
            dist.mean();
            fail();
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
        try {
            dist.log2Probability(4);
            fail();
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    public void testAverage() throws IOException, ClassNotFoundException {
        PoissonEstimator dist = new PoissonEstimator();
        dist.train(1);
        dist.train(3);
        dist.train(2);
    PoissonDistributionTest.assertPoissonTwo(dist);

    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
    dist.compileTo(objOut);
    byte[] bytes = bytesOut.toByteArray();
    ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
    ObjectInputStream dataIn = new ObjectInputStream(bytesIn);
    PoissonConstant dist2 = (PoissonConstant) dataIn.readObject();
    
    PoissonDistributionTest.assertPoissonTwo(dist2);
    }

}
