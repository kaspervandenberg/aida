package com.aliasi.test.unit.stats;

import com.aliasi.stats.MultivariateDistribution;

import com.aliasi.test.unit.BaseTestCase;

public class MultivariateDistributionTest extends BaseTestCase {

    public void testOne() {
    Distro distro = new Distro();

    assertEquals(10,distro.numDimensions());

    assertEquals(0l,distro.minOutcome());
    assertEquals(9l,distro.maxOutcome());

    assertEquals(.50,distro.cumulativeProbabilityLess(4l),0.001);
    assertEquals(.00,distro.cumulativeProbabilityLess(-1l),0.001);
    assertEquals(1.00,distro.cumulativeProbabilityLess(9l),0.001);
    assertEquals(1.00,distro.cumulativeProbabilityLess(20l),0.001);

    assertEquals(.50,distro.cumulativeProbabilityGreater(5l),0.001);
    assertEquals(.00,distro.cumulativeProbabilityGreater(10l),0.001);
    assertEquals(1.00,distro.cumulativeProbabilityGreater(0l),0.001);
    assertEquals(1.00,distro.cumulativeProbabilityGreater(-20l),0.001);

    assertEquals(.50,distro.cumulativeProbability(1l,5l),0.001);
    assertEquals(.50,distro.cumulativeProbability(-3l,4l),0.001);
    assertEquals(.50,distro.cumulativeProbability(-3l,4l),0.001);
    assertEquals(.00,distro.cumulativeProbability(-3l,-4l),0.001);
    assertEquals(1.00,distro.cumulativeProbability(-3l,15l),0.001);
    assertEquals(1.00,distro.cumulativeProbability(0l,9l),0.001);

    assertEquals(.10,distro.probability(0l),0.0001);
    assertEquals(.10,distro.probability(5l),0.0001);
    assertEquals(.10,distro.probability(9l),0.0001);
    assertEquals(.00,distro.probability(17l),0.0001);

    assertEquals(com.aliasi.util.Math.log2(.10),
             distro.log2Probability(0l),0.0001);
    assertEquals(com.aliasi.util.Math.log2(.10),
             distro.log2Probability(5l),0.0001);
    assertEquals(com.aliasi.util.Math.log2(.10),
             distro.log2Probability(9l),0.0001);
    assertEquals(com.aliasi.util.Math.log2(.00),
             distro.log2Probability(17l),0.0001);

    double mean = (10.0*9.0/2.0)/10.0;
    double variance = 0.0;
    for (int i = 0; i < 10; ++i) {
        double diff = mean - (double)i;
        variance += diff*diff;
    }
    variance /= 10.0;
    assertEquals(mean,distro.mean(),0.0001);
    assertEquals(variance,distro.variance(),0.0001);

    double entropy = 0.0;
    for (int i = 0; i <= 9; ++i)
        entropy += -distro.probability(i) * distro.log2Probability(i);
    assertEquals(entropy,distro.entropy(),0.0001);

    assertEquals("1",distro.label(1l));
    assertEquals(1l,distro.outcome("1"));
    try {
        assertEquals("-1",distro.label(-1l));
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }

    assertEquals(-1l,distro.outcome("foo"));
    }

    public static class Distro extends MultivariateDistribution {
    public double probability(long outcome) {
        if (outcome < minOutcome() || outcome > maxOutcome()) return 0.0;
        return 1.0/10.0;
    }
    public int numDimensions() {
        return 10;
    }
    }



}

