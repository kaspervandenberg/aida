package com.aliasi.test.unit.classify;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.classify.Classification;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.Classifier;
import com.aliasi.classify.ClassifierEvaluator;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.RankedClassification;
import com.aliasi.classify.ScoredClassification;
import com.aliasi.classify.ScoredPrecisionRecallEvaluation;

import java.util.HashMap;
import java.util.Map;

public class ClassifierEvaluatorTest extends BaseTestCase {

    private static final String[] CATS 
	= new String[] { "foo", "bar", "baz" };

    private static final String[] CATS2 
	= new String[] { "foo", "bar" };

    public void testAdaptation() {
	MockClassifier classifier = new MockClassifier();
	ClassifierEvaluator evaluator = new ClassifierEvaluator(classifier,
								CATS2);
	addJointCase(classifier,evaluator,"test0",
		     "foo",
		     "foo","bar","baz",
		     -1,-2,-3);

	assertEquals(-1.0,evaluator.averageLog2JointProbability("foo","foo"),
		     0.01);
	assertEquals(-1.0,evaluator.averageLog2JointProbabilityReference(),
		     0.01);
	// 0.57 = (1/2)/(1/2 + 1/4 + 1/8)
	assertEquals(0.57,evaluator.averageConditionalProbability("foo","foo"),
		     0.01);
    }

    public void testScored() {
	MockClassifier classifier = new MockClassifier();
	ClassifierEvaluator evaluator = new ClassifierEvaluator(classifier,
								CATS2);
	addScoredCase(classifier,evaluator,"obj0",
		      "foo",
		      "foo", "bar",
		      0.7, 0.3);
	addScoredCase(classifier,evaluator,"obj1",
		      "foo",
		      "foo", "bar",
		      0.8, 0.2);
	addScoredCase(classifier,evaluator,"obj2",
		      "foo",
		      "bar", "foo",
		      0.6, 0.4);
	addScoredCase(classifier,evaluator,"obj3",
		      "bar",
		      "bar", "foo",
		      0.7, 0.3);
	addScoredCase(classifier,evaluator,"obj4",
		      "bar",
		      "foo", "bar",
		      0.6, 0.4);

	assertEquals(0.63,
		     evaluator.averageScore("foo","foo"),
		     0.01);
	assertEquals(0.37,
		     evaluator.averageScore("foo","bar"),
		     0.01);

	assertEquals(0.45,
		     evaluator.averageScore("bar","foo"),
		     0.01);
	assertEquals(0.55,
		     evaluator.averageScore("bar","bar"),
		     0.01);
	assertEquals(0.60,
		     evaluator.averageScoreReference(),
		     0.01);
	try {
	    evaluator.averageScore("baz","baz");
	    fail();
	} catch (IllegalArgumentException e) {
	    succeed();
	}
    }

    public void testConditional() {
	MockClassifier classifier = new MockClassifier();
	ClassifierEvaluator evaluator = new ClassifierEvaluator(classifier,
								CATS2);
	addConditionalCase(classifier,evaluator,"obj0",
			   "foo",
			   "foo", "bar",
			   0.7, 0.3);
	addConditionalCase(classifier,evaluator,"obj1",
			   "foo",
			   "foo", "bar",
			   0.8, 0.2);
	addConditionalCase(classifier,evaluator,"obj2",
			   "foo",
			   "bar", "foo",
			   0.6, 0.4);
	addConditionalCase(classifier,evaluator,"obj3",
			   "bar",
			   "bar", "foo",
			   0.7, 0.3);
	addConditionalCase(classifier,evaluator,"obj4",
			   "bar",
			   "foo", "bar",
			   0.6, 0.4);

	assertEquals(0.63,
		     evaluator.averageConditionalProbability("foo","foo"),
		     0.01);
	assertEquals(0.37,
		     evaluator.averageConditionalProbability("foo","bar"),
		     0.01);

	assertEquals(0.45,
		     evaluator.averageConditionalProbability("bar","foo"),
		     0.01);
	assertEquals(0.55,
		     evaluator.averageConditionalProbability("bar","bar"),
		     0.01);

	assertEquals(0.60,
		     evaluator.averageConditionalProbabilityReference(),
		     0.01);
    
	try {
	    evaluator.averageConditionalProbability("baz","bar");
	    fail();
	} catch (IllegalArgumentException e) {
	    succeed();
	}
    }

    public void testJoint() {
	MockClassifier classifier = new MockClassifier();
	ClassifierEvaluator evaluator = new ClassifierEvaluator(classifier,
								CATS);
	addJointCase(classifier,evaluator,"obj0",
		     "foo",
		     "foo", "bar", "baz",
		     -1, -2, -3);
	addJointCase(classifier,evaluator,"obj1",
		     "foo",
		     "foo", "baz", "bar",
		     -2, -3, -5);
	addJointCase(classifier,evaluator,"obj2",
		     "foo",
		     "bar", "foo", "baz",
		     -3, -4, -7);
	addJointCase(classifier,evaluator,"obj3",
		     "bar",
		     "bar", "foo", "baz",
		     -1, -2, -3);
	addJointCase(classifier,evaluator,"obj4",
		     "bar",
		     "foo", "baz", "bar",
		     -1, -2, -3);

	assertEquals(-11.0/5.0,
		     evaluator.averageLog2JointProbabilityReference(),
		     0.01);

	assertEquals(-7.0/3.0,
		     evaluator.averageLog2JointProbability("foo","foo"),
		     0.01);
	assertEquals(-10.0/3.0,
		     evaluator.averageLog2JointProbability("foo","bar"),
		     0.01);
	assertEquals(-13.0/3.0,
		     evaluator.averageLog2JointProbability("foo","baz"),
		     0.01);

	assertEquals(-3.0/2.0,
		     evaluator.averageLog2JointProbability("bar","foo"),
		     0.01);
	assertEquals(-4.0/2.0,
		     evaluator.averageLog2JointProbability("bar","bar"),
		     0.01);
	assertTrue(Double.isNaN(evaluator.
				averageLog2JointProbability("baz","bar")));
    

    }

    public void testOne() {
	MockClassifier classifier = new MockClassifier();
	ClassifierEvaluator evaluator = new ClassifierEvaluator(classifier,
								CATS);

	assertEqualsArray(CATS,evaluator.categories());

	addCase(classifier,evaluator,"obj0",
		"bar",
		"foo","bar","baz",
		-1.21,-1.4,-1.7);
	addCase(classifier,evaluator,"obj1",
		"foo",
		"foo","bar","baz",
		-1.27,-1.5,-1.9);
	addCase(classifier,evaluator,"obj2",
		"bar",
		"bar","foo","baz",
		-1.1,-1.39,-1.5);
	addCase(classifier,evaluator,"obj3",
		"foo",
		"foo","baz","bar",
		-1.47,-1.6,-2.5);
	addCase(classifier,evaluator,"obj4",
		"foo",
		"bar","foo","baz",
		-1.2,-1.6,-2.3);
	addCase(classifier,evaluator,"obj5",
		"baz",
		"baz","foo","bar",
		-1.1,-1.65,-3.13);
	addCase(classifier,evaluator,"obj6",
		"baz",
		"baz","bar","foo",
		-1.0,-1.1,-1.79);
	addCase(classifier,evaluator,"obj7",
		"bar",
		"bar","baz","foo",
		-1.2,-1.3,-1.8);
	addCase(classifier,evaluator,"obj8",
		"foo",
		"bar","baz","foo",
		-1.5,-1.6,-2.01);
	addCase(classifier,evaluator,"obj9",
		"bar",
		"baz","bar","foo",
		-1.5,-1.6,-3.7);
    

	assertEquals(2,
		     evaluator.rankCount("foo",0));
	assertEquals(1,
		     evaluator.rankCount("foo",1));
	assertEquals(1,
		     evaluator.rankCount("foo",2));
	assertEquals(2,
		     evaluator.rankCount("bar",0));
	assertEquals(2,
		     evaluator.rankCount("bar",1));
	assertEquals(0,
		     evaluator.rankCount("bar",2));
	assertEquals(2,
		     evaluator.rankCount("baz",0));
	assertEquals(0,
		     evaluator.rankCount("baz",1));
	assertEquals(0,
		     evaluator.rankCount("baz",2));

	double mrr 
	    = 1.0/2.0 
	    + 1.0/1.0
	    + 1.0/1.0
	    + 1.0/1.0
	    + 1.0/2.0
	    + 1.0/1.0
	    + 1.0/1.0
	    + 1.0/1.0
	    + 1.0/3.0
	    + 1.0/2.0;
	assertEquals(mrr/10.0,evaluator.meanReciprocalRank(),0.001);


	assertEquals(10,evaluator.numCases());

	assertEquals(0.5,evaluator.averageRankReference(),0.01);
	assertEquals(0.5,evaluator.averageRank("bar","bar"),0.01);
	assertEquals(0.0,evaluator.averageRank("baz","baz"),0.01);
	assertEquals(0.75,evaluator.averageRank("foo","foo"),0.01);

	ScoredPrecisionRecallEvaluation fooEval
	    = evaluator.scoredOneVersusAll("foo");

	double[][] prCurve = fooEval.prCurve(false);
	assertEquals(4,prCurve.length);
	assertEqualsArray(new double[] { 0.25, 0.50 },
			  prCurve[0], 0.01);
	assertEqualsArray(new double[] { 0.50, 0.50 },
			  prCurve[1], 0.01);
	assertEqualsArray(new double[] { 0.75, 0.60 },
			  prCurve[2], 0.01);
	assertEqualsArray(new double[] { 1.00, 0.44 },
			  prCurve[3], 0.01);

	assertEquals(0.51,fooEval.areaUnderPrCurve(false),0.01);

	assertEquals(0.51,fooEval.averagePrecision(),0.01);

	assertEquals(0.67,fooEval.maximumFMeasure(),0.01);

	assertEquals(0.60,fooEval.prBreakevenPoint(),0.01);

	try {
	    evaluator.scoredOneVersusAll("Foo"); // caps don't match
	    fail();
	} catch (IllegalArgumentException e) {
	    succeed();
	}

	double[][] interpolatedPrCurve = fooEval.prCurve(true);
	assertEquals(2,interpolatedPrCurve.length);
	assertEqualsArray(new double[] { 0.75, 0.60 },
			  interpolatedPrCurve[0], 0.01);
	assertEqualsArray(new double[] { 1.00, 0.44 },
			  interpolatedPrCurve[1], 0.01);

	assertEquals(0.56,fooEval.areaUnderPrCurve(true),0.01);



	double[][] rocCurve = fooEval.rocCurve(false);
	assertEquals(4,rocCurve.length);
	assertEqualsArray(new double[] { 0.25, 0.83 },
			  rocCurve[0], 0.01);
	assertEqualsArray(new double[] { 0.50, 0.67 },
			  rocCurve[1], 0.01);
	assertEqualsArray(new double[] { 0.75, 0.67 },
			  rocCurve[2], 0.01);
	assertEqualsArray(new double[] { 1.00, 0.17 },
			  rocCurve[3], 0.01);

	double[][] interpolatedRocCurve = fooEval.rocCurve(true);
	assertEquals(3,interpolatedRocCurve.length);
	assertEqualsArray(new double[] { 0.25, 0.83 },
			  interpolatedRocCurve[0], 0.01);
	assertEqualsArray(new double[] { 0.75, 0.67 },
			  interpolatedRocCurve[1], 0.01);
	assertEqualsArray(new double[] { 1.00, 0.17 },
			  interpolatedRocCurve[2], 0.01);

	assertEquals(0.51,fooEval.areaUnderPrCurve(false),0.01);
	assertEquals(0.56,fooEval.areaUnderPrCurve(true),0.01);

    }

    public void testRanked() {
	MapRankedClassifier classifier = new MapRankedClassifier();
	classifier.set(new Integer(1),
		       new String[] { "a", "b", "c" });
	classifier.set(new Integer(2),
		       new String[] { "c", "b", "a" });
	classifier.set(new Integer(3),
		       new String[] { "c", "b" });
	classifier.set(new Integer(4),
		       new String[] { "a" });
	classifier.set(new Integer(5),
		       new String[] { "b", "a" });
	ClassifierEvaluator evaluator
	    = new ClassifierEvaluator(classifier,
				      new String[] { "a", "b", "c" });
	evaluator.addCase("a", new Integer(1));
	evaluator.addCase("c", new Integer(2));
	evaluator.addCase("a", new Integer(3));
	evaluator.addCase("b", new Integer(4));
	evaluator.addCase("a", new Integer(5));
	
	assertNotNull(evaluator.confusionMatrix());
	assertEquals(5.0/5.0,evaluator.averageRankReference(),
		     0.001);
	assertEquals((1.0/1.0 + 1.0/1.0 + 1.0/3.0 + 1.0/3.0 + 1.0/2.0)/5.0,
		     evaluator.meanReciprocalRank(),
		     0.001);
	assertEquals(2.0/3.0,evaluator.averageRank("a","b"),
		     0.001);
	assertEquals(evaluator.rankCount("a",0),1);
	assertEquals(evaluator.rankCount("a",1),1);
	assertEquals(evaluator.rankCount("a",2),1);
	assertEquals(evaluator.rankCount("b",0),0);
	assertEquals(evaluator.rankCount("b",1),0);
	assertEquals(evaluator.rankCount("b",2),1);
	assertEquals(evaluator.rankCount("c",0),1);
	assertEquals(evaluator.rankCount("c",1),0);
	assertEquals(evaluator.rankCount("c",2),0);
	assertEquals(3.0/3.0,evaluator.averageRank("a","a"),0.001);
	assertEquals(2.0/3.0,evaluator.averageRank("a","b"),0.001);
	assertEquals(4.0/3.0,evaluator.averageRank("a","c"),0.001);
	assertEquals(0.0,evaluator.averageRank("b","a"),0.001);
	assertEquals(2.0,evaluator.averageRank("b","b"),0.001);
	assertEquals(2.0,evaluator.averageRank("b","c"),0.001);
	assertEquals(2.0,evaluator.averageRank("c","a"),0.001);
	assertEquals(1.0,evaluator.averageRank("c","b"),0.001);
	assertEquals(0.0,evaluator.averageRank("c","c"),0.001);

	assertNotNull(evaluator.toString());
    }

    static class MapRankedClassifier implements Classifier {
	final Map mClassifyMap = new HashMap();
	public void set(Object in, String[] cs) {
	    mClassifyMap.put(in,new RankedClassification(cs));
	}
	public Classification classify(Object in) {
	    return (RankedClassification) mClassifyMap.get(in);
	}
    }


    void addScoredCase(MockClassifier classifier,
		       ClassifierEvaluator evaluator,
		       String input,
		       String refCategory,
		       String cat1, String cat2,
		       double cond1, double cond2) {
	String[] cats = new String[] { cat1, cat2 };
	double[] conds = new double[] { cond1, cond2 };
	classifier.put(input,
		       new ScoredClassification(cats,conds));
	evaluator.addCase(refCategory,input);
    }
                    
    void addConditionalCase(MockClassifier classifier,
			    ClassifierEvaluator evaluator,
			    String input,
			    String refCategory,
			    String cat1, String cat2,
			    double cond1, double cond2) {
	String[] cats = new String[] { cat1, cat2 };
	double[] conds = new double[] { cond1, cond2 };
	classifier.put(input,
		       new ConditionalClassification(cats,conds));
	evaluator.addCase(refCategory,input);
    }

    void addJointCase(MockClassifier classifier,
		      ClassifierEvaluator evaluator,
		      String input,
		      String refCategory,
		      String cat1, String cat2, String cat3, 
		      double joint1, double joint2, double joint3) {
	String[] cats = new String[] { cat1, cat2, cat3 };
	double[] joints = new double[] { joint1, joint2, joint3 };
	classifier.put(input,
		       new JointClassification(cats,joints));
	evaluator.addCase(refCategory,input);
    }


    void addCase(MockClassifier classifier,
		 ClassifierEvaluator evaluator,
		 String input, String refCat,
		 String cat1, String cat2, String cat3,
		 double score1, double score2, double score3) {
	String[] cats = new String[] { cat1, cat2, cat3 };
	double[] scores = new double[] { score1, score2, score3 };
	classifier.put(input,
		       new ScoredClassification(cats,scores));
	evaluator.addCase(refCat,input);
    }

    static class MockClassifier implements Classifier {
	private final Map mObjectToClassification
	    = new HashMap();
	public Classification classify(Object input) {
	    return (Classification) mObjectToClassification.get(input);
	}
	public void put(Object input,
			Classification classification) {
	    mObjectToClassification.put(input,classification);
	}
    }
}
