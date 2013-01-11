package com.aliasi.test.unit.classify;

import com.aliasi.classify.BinaryLMClassifier;

import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.lm.NGramProcessLM;

import com.aliasi.test.unit.BaseTestCase;

public class BinaryLMClassifierTest extends BaseTestCase {

    public void testNeg() {
        NGramBoundaryLM lm = new NGramBoundaryLM(5);
        BinaryLMClassifier classifier
            = new BinaryLMClassifier(lm,10);
        classifier.train("true","Hello");
        classifier.train("false","Goodbye");
    }


    public void testOne() {
        NGramBoundaryLM lm = new NGramBoundaryLM(5);
        BinaryLMClassifier classifier
            = new BinaryLMClassifier(lm,10);

        for (int i = 0; i < 100; ++i)
            classifier.train("true","Kilroy was here.");

        // System.out.println("classification=" + classifier.classify("John Smith"));
    
        assertEquals("false",
                     classifier.classify("John Smith").bestCategory());

        assertEquals("true",
                     classifier.classify("Kilroy").bestCategory());
    

        try {
            classifier.resetCategory("true", new NGramBoundaryLM(5), 15);
            fail();
        } catch (UnsupportedOperationException e) {
            succeed();
        }
    }

    public void testTwo() {
        NGramProcessLM lm = new NGramProcessLM(5);
        BinaryLMClassifier classifier
            = new BinaryLMClassifier(lm,12);

        assertEquals("false",
                     classifier.classify("").bestCategory());
        assertEquals("false",
                     classifier.classify("abcdefghijklmnop").bestCategory());

    
    }

    public void testThree() {
        NGramProcessLM lm = new NGramProcessLM(5);
        BinaryLMClassifier classifier
            = new BinaryLMClassifier(lm,18);

        assertEquals("true",
                     classifier.classify("abcdefghijklmnop").bestCategory());

    }

}


