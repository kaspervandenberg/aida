package com.aliasi.test.unit.classify;

import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.LMClassifier;

import com.aliasi.stats.MultivariateEstimator;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.File;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;


public class DynamicLMClassifierTest extends BaseTestCase {

    public void testCompileOneCategory() throws IOException,
						ClassNotFoundException {
	File dataFile 
	    = new File("src/com/aliasi/test/unit/classify/testFile1.txt");
	String data 
	    = Files.readFromFile(dataFile,Strings.UTF8);

        String[] categories = {"Foo", "Bar"};
        DynamicLMClassifier classifier
            = DynamicLMClassifier.createNGramBoundary(categories,32);

	classifier.train("Foo",data);


        LMClassifier compiledClassifier =
	    (LMClassifier) AbstractExternalizable.compile(classifier);
	assertTrue(null != compiledClassifier);
	
    }

    public void testOne() throws IOException, ClassNotFoundException {

        String[] categories = {"Foo","Bar"};
        DynamicLMClassifier classifier
            = DynamicLMClassifier.createNGramProcess(categories,2);


        String oneStr = "The rain in Spain falls mainly on the ground.";

        char[] oneChar = oneStr.toCharArray();
        classifier.train("Foo",oneChar,0,oneChar.length);

        String barStr = "The rain in Madrid is made of water.";

        char[] barChar = barStr.toCharArray();
        classifier.train("Bar",barChar,0,barChar.length);

        MultivariateEstimator est = classifier.categoryEstimator();
        assertEquals(2, est.getCount(est.outcome("Foo")));
        assertEquals(4, est.trainingSampleCount());

	assertEquals("Foo",
		     classifier.classify("falls mainly").bestCategory());
	assertEquals("Bar",
		     classifier.classify("Madrid is made of water").bestCategory());

	/*
	  classifier.resetLanguageModel("Foo",2,256);
	  assertEquals(est.getCount(est.outcome("Foo")),0);
	  assertEquals(est.trainingSampleCount(),1);
	*/

	LMClassifier compiledCassifier 
	    = (LMClassifier) AbstractExternalizable.compile(classifier);
    
	assertEquals("Foo",
		     compiledCassifier.classify("falls mainly").bestCategory());
	assertEquals("Bar",
		     compiledCassifier.classify("Madrid is made of water").bestCategory());
    }


}
