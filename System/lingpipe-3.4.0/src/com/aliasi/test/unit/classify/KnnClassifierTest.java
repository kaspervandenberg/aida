package com.aliasi.test.unit.classify;

import com.aliasi.classify.KnnClassifier;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classifier;
import com.aliasi.classify.ScoredClassification;

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



public class KnnClassifierTest extends BaseTestCase {

    static final TokenFeatureExtractor FEATURE_EXTRACTOR
        = new TokenFeatureExtractor(new IndoEuropeanTokenizerFactory());

    static final Distance<Vector> DISTANCE
        = EuclideanDistance.DISTANCE;

    public void testOne() throws ClassNotFoundException, IOException {
        String[] train = new String[] {
            "a a b",
            "a b b"
        };
        String[] cats = new String[] {
            "A",
            "B"
        };

        KnnClassifier<String> classifier
            = new KnnClassifier<String>(FEATURE_EXTRACTOR, 1);
        for (int i = 0; i < train.length; ++i)
            classifier.handle(train[i],new Classification(cats[i]));

        ScoredClassification classification =
            classifier.classify("a a a a b b");
        assertEquals("A",classification.bestCategory());
        assertEquals("A",classification.category(0));
        assertEquals("B",classification.category(1));
        assertEquals(1.0,classification.score(0));
        assertEquals(0.0,classification.score(1));

        KnnClassifier<String> classifier2
            = (KnnClassifier<String>)
            AbstractExternalizable.serializeDeserialize(classifier);
        classifier2.classify("a a a a b b");
        assertEquals("A",classification.bestCategory());
        assertEquals("A",classification.category(0));
        assertEquals("B",classification.category(1));
        assertEquals(1.0,classification.score(0));
        assertEquals(0.0,classification.score(1));
    }

    public void testTwo() throws ClassNotFoundException, IOException {
        String[] train = new String[] {
            "a a b",
            "a b b",
            "a a a",
            "a a a a a b",
            "a a b b"
        };
        String[] cats = new String[] {
            "A",
            "B",
            "A",
            "A",
            "B"
        };

        KnnClassifier<String> classifier
            = new KnnClassifier<String>(FEATURE_EXTRACTOR, 3);
        for (int i = 0; i < train.length; ++i)
            classifier.handle(train[i],new Classification(cats[i]));

        ScoredClassification classification =
            classifier.classify("a a b");
        assertEquals("A",classification.bestCategory());
        assertEquals("A",classification.category(0));
        assertEquals("B",classification.category(1));
        assertEquals(2.0,classification.score(0));
        assertEquals(1.0,classification.score(1));

        KnnClassifier<String> classifier2
            = (KnnClassifier<String>)
            AbstractExternalizable.serializeDeserialize(classifier);
        classification = classifier2.classify("a a b");
        assertEquals("A",classification.bestCategory());
        assertEquals("A",classification.category(0));
        assertEquals("B",classification.category(1));
        assertEquals(2.0,classification.score(0));
        assertEquals(1.0,classification.score(1));

    }

    public void testThree() {
        String[] train = new String[] {
            "a a b",
            "a b b",
            "a a a",
            "b b b"
        };
        String[] cats = new String[] {
            "A",
            "B",
            "A",
            "B"
        };

        KnnClassifier<String> classifier
            = new KnnClassifier<String>(FEATURE_EXTRACTOR,
                                        Integer.MAX_VALUE,
                                        new TestProximity(),
                                        true);
        for (int i = 0; i < train.length; ++i)
            classifier.handle(train[i],new Classification(cats[i]));

        double prox01 = 1.0/(1.0 + Math.sqrt(sqrDiff(2,1) + sqrDiff(1,2)));
        double prox02 = 1.0/(1.0 + Math.sqrt(sqrDiff(2,3) + sqrDiff(1,0)));
        double prox03 = 1.0/(1.0 + Math.sqrt(sqrDiff(2,0) + sqrDiff(1,3)));
        double prox12 = 1.0/(1.0 + Math.sqrt(sqrDiff(1,3) + sqrDiff(1,0)));
        double prox13 = 1.0/(1.0 + Math.sqrt(sqrDiff(1,0) + sqrDiff(2,3)));
        double prox23 = 1.0/(1.0 + Math.sqrt(sqrDiff(3,0) + sqrDiff(0,3)));

        ScoredClassification[] classifications
            = new ScoredClassification[train.length];
        for (int i = 0; i < train.length; ++i)
            classifications[i] = classifier.classify(train[i]);
        for (int i = 0; i < train.length; ++i) {
            assertEquals(cats[i],classifications[i].bestCategory());
        }

    }

    static double sqrDiff(double x1, double x2) {
        double diff = x1 - x2;
        return diff * diff;
    }

    static class TestProximity
        implements Proximity<Vector>, Serializable {

        public double proximity(Vector v1, Vector v2) {
            return 1.0/(1.0 + DISTANCE.distance(v1,v2));
        }
    }

}
