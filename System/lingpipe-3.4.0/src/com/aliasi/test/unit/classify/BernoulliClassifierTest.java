package com.aliasi.test.unit.classify;

import com.aliasi.classify.BernoulliClassifier;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classifier;
import com.aliasi.classify.ScoredClassification;
import com.aliasi.classify.JointClassification;

import com.aliasi.corpus.ClassificationHandler;

import com.aliasi.matrix.EuclideanDistance;
import com.aliasi.matrix.Vector;

import com.aliasi.util.Distance;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Compilable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.Proximity;
import com.aliasi.util.ScoredObject;

import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.symbol.MapSymbolTable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.aliasi.test.unit.BaseTestCase;

public class BernoulliClassifierTest extends BaseTestCase {

    static final FeatureExtractor FEATURE_EXTRACTOR
        = new TokenFeatureExtractor(IndoEuropeanTokenizerFactory.FACTORY);

    public void testOne() {
        BernoulliClassifier classifier
            = new BernoulliClassifier(FEATURE_EXTRACTOR);

        classifier.handle("a b",new Classification("cat1"));
        classifier.handle("a",new Classification("cat1"));

        classifier.handle("a b",new Classification("cat2"));
        classifier.handle("b",new Classification("cat2"));

        JointClassification c = classifier.classify("a");

        assertEquals("cat1",c.bestCategory());
        assertEquals(0.75,c.conditionalProbability(0),0.0001);
        assertEquals(0.25,c.conditionalProbability(1),0.0001);


        assertEquals("cat2",classifier.classify("b").bestCategory());
        assertEquals(0.75,c.conditionalProbability(0),0.0001);
        assertEquals(0.25,c.conditionalProbability(1),0.0001);


        assertEquals("cat2",classifier.classify("b b").bestCategory());
        assertEquals(0.75,c.conditionalProbability(0),0.0001);
        assertEquals(0.25,c.conditionalProbability(1),0.0001);

        assertEquals("cat2",classifier.classify("b foo").bestCategory());
        assertEquals(0.75,c.conditionalProbability(0),0.0001);
        assertEquals(0.25,c.conditionalProbability(1),0.0001);

    }

}

