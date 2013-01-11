package com.aliasi.test.unit.stats;

import com.aliasi.stats.BernoulliDistribution;

import com.aliasi.test.unit.BaseTestCase;

public class BernoulliDistributionTest extends BaseTestCase {

    public void testOne() {
    BernoulliDistribution coin = new BernoulliDistribution() {
        public double successProbability() {
            return 0.50;
        }
        };
    
    assertEquals(0l,coin.minOutcome());
    assertEquals(1l,coin.maxOutcome());
    assertEquals(2,coin.numDimensions());
    assertEquals("failure",coin.label(0l));
    assertEquals("success",coin.label(1l));
    assertEquals(0.50,coin.successProbability(),0.0001);
    assertEquals(0.50,coin.probability(0l),0.0001);
    assertEquals(0.50,coin.probability(1l),0.0001);

    assertEquals(0.00,coin.cumulativeProbabilityLess(-1l),0.0001);
    assertEquals(0.50,coin.cumulativeProbabilityLess(0l),0.0001);
    assertEquals(1.00,coin.cumulativeProbabilityLess(1l),0.0001);
    assertEquals(1.00,coin.cumulativeProbabilityLess(2l),0.0001);
    assertEquals(1.00,coin.cumulativeProbabilityGreater(-1l),0.0001);
    assertEquals(1.00,coin.cumulativeProbabilityGreater(0l),0.0001);
    assertEquals(0.50,coin.cumulativeProbabilityGreater(1l),0.0001);
    assertEquals(0.00,coin.cumulativeProbabilityGreater(2l),0.0001);
    assertEquals(0.00,coin.cumulativeProbability(-2l,-1l),0.0001);
    assertEquals(0.00,coin.cumulativeProbability(3l,120l),0.0001);
    assertEquals(0.50,coin.cumulativeProbability(-1l,0l),0.0001);
    assertEquals(0.50,coin.cumulativeProbability(1l,1l),0.0001);
    assertEquals(0.50,coin.cumulativeProbability(1l,15l),0.0001);
    assertEquals(1.00,coin.cumulativeProbability(0l,1l),0.0001);
    assertEquals(1.00,coin.cumulativeProbability(0l,14l),0.0001);
    assertEquals(1.00,coin.cumulativeProbability(-12l,14l),0.0001);

    assertEquals(1.0,coin.entropy(),0.0001);
    assertEquals(0.5,coin.mean(),0.0001);
    assertEquals((0.5 * 0.5 + 0.5*0.5)/2.0,coin.variance(),0.0001);
    }
}
