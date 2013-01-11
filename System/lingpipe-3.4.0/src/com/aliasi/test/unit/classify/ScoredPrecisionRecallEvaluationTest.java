package com.aliasi.test.unit.classify;

import com.aliasi.classify.ScoredPrecisionRecallEvaluation;

import com.aliasi.test.unit.BaseTestCase;

public class ScoredPrecisionRecallEvaluationTest extends BaseTestCase {
    
    
    public void testOne() {
    ScoredPrecisionRecallEvaluation eval 
        = new ScoredPrecisionRecallEvaluation();
    eval.addCase(false,-1.21);
    eval.addCase(true,-1.27);
    eval.addCase(false,-1.39);
    eval.addCase(true,-1.47);
    eval.addCase(true,-1.60);
    eval.addCase(false,-1.65);
    eval.addCase(false,-1.79);
    eval.addCase(false,-1.80);
    eval.addCase(true,-2.01);
    eval.addCase(false,-3.70);

    double[][] prCurve = eval.prCurve(false);
    assertEquals(4,prCurve.length);
    assertEqualsArray(new double[] { 0.25, 0.50 },
              prCurve[0], 0.01);
    assertEqualsArray(new double[] { 0.50, 0.50 },
              prCurve[1], 0.01);
    assertEqualsArray(new double[] { 0.75, 0.60 },
              prCurve[2], 0.01);
    assertEqualsArray(new double[] { 1.00, 0.44 },
              prCurve[3], 0.01);

    assertEquals(0.5,eval.reciprocalRank(),0.0005);
    
    assertEquals(0.0,eval.precisionAt(1),0.0005);
    assertEquals(0.5,eval.precisionAt(2),0.0005);
    assertEquals(0.6,eval.precisionAt(5),0.0005);
    assertTrue(Double.isNaN(eval.precisionAt(20)));

    double[][] interpolatedPrCurve = eval.prCurve(true);
    assertEquals(2,interpolatedPrCurve.length);
    assertEqualsArray(new double[] { 0.75, 0.60 },
              interpolatedPrCurve[0], 0.01);
    assertEqualsArray(new double[] { 1.00, 0.44 },
              interpolatedPrCurve[1], 0.01);


    assertEquals(0.51,eval.areaUnderPrCurve(false),0.01);
    assertEquals(0.56,eval.areaUnderPrCurve(true),0.01);

    assertEquals(0.51,eval.averagePrecision(),0.01);
    assertEquals(0.67,eval.maximumFMeasure(),0.01);
    assertEquals(0.60,eval.prBreakevenPoint(),0.01);


    double[][] rocCurve = eval.rocCurve(false);
    assertEquals(4,rocCurve.length);
    assertEqualsArray(new double[] { 0.25, 0.83 },
              rocCurve[0], 0.01);
    assertEqualsArray(new double[] { 0.50, 0.67 },
              rocCurve[1], 0.01);
    assertEqualsArray(new double[] { 0.75, 0.67 },
              rocCurve[2], 0.01);
    assertEqualsArray(new double[] { 1.00, 0.17 },
              rocCurve[3], 0.01);

    double[][] interpolatedRocCurve = eval.rocCurve(true);
    assertEquals(3,interpolatedRocCurve.length);
    assertEqualsArray(new double[] { 0.25, 0.83 },
              interpolatedRocCurve[0], 0.01);
    assertEqualsArray(new double[] { 0.75, 0.67 },
              interpolatedRocCurve[1], 0.01);
    assertEqualsArray(new double[] { 1.00, 0.17 },
              interpolatedRocCurve[2], 0.01);

    assertEquals(0.58,eval.areaUnderRocCurve(false),0.01);
    assertEquals(0.58,eval.areaUnderRocCurve(true),0.01);
    }


}
