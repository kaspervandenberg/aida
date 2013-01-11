package com.aliasi.test.unit.classify;

import com.aliasi.classify.TfIdfClassifierTrainer;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classifier;
import com.aliasi.classify.ScoredClassification;

import com.aliasi.corpus.ClassificationHandler;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Compilable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
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



public class TfIdfClassifierTrainerTest extends BaseTestCase {

    public void testOne() throws Exception {
        TokenFeatureExtractor featureExtractor
            = new TokenFeatureExtractor(new IndoEuropeanTokenizerFactory());

        TfIdfClassifierTrainer<CharSequence> trainer
            = new TfIdfClassifierTrainer<CharSequence>(featureExtractor);

        trainer.handle("a b b", new Classification("cat1"));
        trainer.handle("b c c c d", new Classification("cat2"));
        trainer.handle("c c c c c", new Classification("cat3"));

        double cat1_a = Math.sqrt(1.0) * Math.log(3.0/1.0);
        double cat1_b = Math.sqrt(2.0) * Math.log(3.0/2.0);
        double len1 = Math.sqrt(cat1_a * cat1_a + cat1_b * cat1_b);
        double cat1_a_n = cat1_a/len1;
        double cat1_b_n = cat1_b/len1;


        double cat2_b = Math.sqrt(1.0) * Math.log(3.0/2.0);
        double cat2_c = Math.sqrt(3.0) * Math.log(3.0/2.0);
        double cat2_d = Math.sqrt(1.0) * Math.log(3.0);
        double len2 = Math.sqrt(cat2_b*cat2_b + cat2_c*cat2_c + cat2_d*cat2_d);
        double cat2_b_n = cat2_b/len2;
        double cat2_c_n = cat2_c/len2;
        double cat2_d_n = cat2_d/len2;

        double cat3_c = Math.sqrt(5.0) * Math.log(3.0/2.0);
        double len3 = Math.sqrt(cat3_c * cat3_c);
        double cat3_c_n = cat3_c/len3; // = 1.0 :-)

        Classifier<String,ScoredClassification> classifier
            = (Classifier<String,ScoredClassification>)
            AbstractExternalizable.compile(trainer);

        ScoredClassification classification
            = classifier.classify("a b b");

        assertEquals("cat1",classification.bestCategory());

        assertEquals("cat1",classification.category(0));
        assertEquals(1.0,classification.score(0),0.001);
	
        assertEquals("cat2",classification.category(1));
        assertEquals(cat1_b_n * cat2_b_n,classification.score(1),0.05); // off by 0.01

        assertEquals("cat3",classification.category(2));
        assertEquals(0.0,classification.score(2),0.001);

	TfIdfClassifierTrainer<CharSequence> trainer2
            = (TfIdfClassifierTrainer<CharSequence>)
            AbstractExternalizable.serializeDeserialize(trainer);

        Classifier<String,ScoredClassification> classifier2
            = (Classifier<String,ScoredClassification>)
            AbstractExternalizable.compile(trainer2);

        assertEquals("cat1",classification.bestCategory());

        assertEquals("cat1",classification.category(0));
        assertEquals(1.0,classification.score(0),0.001);

        assertEquals("cat2",classification.category(1));
        assertEquals(cat1_b_n * cat2_b_n,classification.score(1),0.05); // off by 0.01

        assertEquals("cat3",classification.category(2));
        assertEquals(0.0,classification.score(2),0.001);


    }

}