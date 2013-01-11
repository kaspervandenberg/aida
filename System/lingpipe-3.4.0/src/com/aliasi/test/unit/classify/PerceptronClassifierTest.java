package com.aliasi.test.unit.classify;

import com.aliasi.classify.BinaryLMClassifier;
import com.aliasi.classify.Classification;
import com.aliasi.classify.PerceptronClassifier;
import com.aliasi.classify.ScoredClassification;

import com.aliasi.corpus.ClassificationHandler;
import com.aliasi.corpus.Corpus;

import com.aliasi.matrix.PolynomialKernel;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenFeatureExtractor;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.SmallObjectToDoubleMap;
import com.aliasi.util.Strings;

import com.aliasi.test.unit.BaseTestCase;

import java.io.IOException;
import java.io.Serializable;

import java.util.Map;


public class PerceptronClassifierTest extends BaseTestCase {

    static final String ACC = BinaryLMClassifier.DEFAULT_ACCEPT_CATEGORY;
    static final String REJ = BinaryLMClassifier.DEFAULT_REJECT_CATEGORY;

    public void testOne() throws ClassNotFoundException, IOException {
        PerceptronClassifier<CharSequence> pc
            = create(INSTANCES1,CATS1,2,2);
        // System.out.println("\npc=" + pc + "\n");

        PerceptronClassifier<CharSequence> pc2
            = (PerceptronClassifier<CharSequence>)
            AbstractExternalizable.serializeDeserialize(pc);

        for (int i = 0; i < INSTANCES1.length; ++i) {
            // System.out.println("\nPC(" + INSTANCES1[i] + "=" + pc.classify(INSTANCES1[i]));
            // System.out.println("PC2=" + pc2.classify(INSTANCES1[i]));

            ScoredClassification c1 = pc.classify(INSTANCES1[i]);
            ScoredClassification c2 = pc2.classify(INSTANCES1[i]);

            assertEquals(CATS1[i],c1.bestCategory());
            assertEquals(CATS1[i],c2.bestCategory());
            assertEquals(SCORES1[i],c1.score(0),0.0001);
            assertEquals(-SCORES1[i],c1.score(1),0.0001);
            assertEquals(SCORES1[i],c2.score(0),0.0001);
            assertEquals(-SCORES1[i],c2.score(1),0.0001);
        }

    }

    static PerceptronClassifier<CharSequence> create(String[] instances,
                                                     String[] cats,
                                                     int degree,
                                                     int numIterations)
        throws IOException {

        return new PerceptronClassifier<CharSequence>(new TokenFeatureExtractor(IndoEuropeanTokenizerFactory.FACTORY),
                                                      new PolynomialKernel(degree),
                                                      new TestCorpus(instances,cats),
                                                      ACC,
                                                      numIterations);
    }

    static final String[] INSTANCES1
        = new String[] {
        "a a b",
        "a b b",
        "b b b"
    };
    static final String[] CATS1
        = new String[] {
        ACC,
        REJ,
        REJ
    };
    static double[] SCORES1
        = new double[] {
        91.0,
        30.0,
        149.0,
    };


    static final String[] INSTANCES2
        = new String[] {
        "a a b",
        "b b b",
        "a a a",
        "a b a",
        "b b a",
        "b a a",
        "b b a"
    };

    static final String[] CATS2
        = new String[] {
        ACC,
        REJ,
        ACC,
        ACC,
        REJ,
        ACC,
        REJ
    };

    static class TestFeatureExtractor
        implements FeatureExtractor<CharSequence>, Serializable {

        public Map<String,Double> features(CharSequence in) {
            ObjectToDoubleMap<String> map = new ObjectToDoubleMap<String>();
            char[] cs = Strings.toCharArray(in);
            Tokenizer tokenizer
                = IndoEuropeanTokenizerFactory.FACTORY.tokenizer(cs,0,cs.length);
            for (String token : tokenizer)
                map.increment(token,1.0);
            // System.out.println(in + "=" + map);
            return map;
        }

    }

    static class TestCorpus
        extends Corpus<ClassificationHandler<CharSequence,Classification>> {

        final String[] mInstances;
        final String[] mCats;

        TestCorpus(String[] instances, String[] cats) {
            if (instances.length != cats.length)
                throw new IllegalStateException("length diff");
            mInstances = instances;
            mCats = cats;
        }

        public void visitTrain(ClassificationHandler<CharSequence,Classification> handler) {
            for (int i = 0; i < mInstances.length; ++i) {
                Classification c = new Classification(mCats[i]);
                handler.handle(mInstances[i],c);
            }
        }

    }


}