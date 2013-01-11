package com.aliasi.test.unit.cluster;

import com.aliasi.cluster.LatentDirichletAllocation;

import com.aliasi.test.unit.BaseTestCase;

import java.util.Random;

public class LatentDirichletAllocationTest extends BaseTestCase {

    public void testTopicSampler() {
        double docTopicPrior = 0.1;
        double[][] topicWordProbs
            = new double[][] {
            // words = { river, bank, loan }
            { 0.70, .25, .05 },  // topic 0 = rivers
            { 0.05, .50, .45},  // topic 1 = finance
        };

        LatentDirichletAllocation lda
            = new LatentDirichletAllocation(docTopicPrior,
                                            topicWordProbs);

        int[] doc1 = new int[] { 0, 0, 1, 0, 1, 0 };
        int[] doc2 = new int[] { 2, 1, 1, 2, 2, 2, 1, 2, 2, 2 };
        int[] doc3 = new int[] { 2, 2, 2, 0, 0, 2, 2, 1, 1 };

        assertTopicSamples(doc1,lda);
        assertTopicSamples(doc2,lda);
        assertTopicSamples(doc3,lda);
    }

    void assertTopicSamples(int[] doc, LatentDirichletAllocation lda) {
        short[][] samples
            = lda.sampleTopics(doc,20,500,500,new Random());

        for (int i = 0; i < samples.length; ++i) {
            short[] sample = samples[i];
            for (int tok = 0; tok < sample.length; ++tok) {
                // System.out.printf(" %3d",sample[tok]);
            }
            // System.out.println();
        }
    }


    public void testGetters() {
        double docTopicPrior = 1.0;
        double[][] topicWordProbs
            = new double[][] {
            // words = { river, bank, loan }
            { 0.7, .25, .05 },  // topic 1 = rivers
            { 0.01, .30, .69},  // topic 2 = finance
        };

        LatentDirichletAllocation lda
            = new LatentDirichletAllocation(docTopicPrior,
                                            topicWordProbs);

        assertEquals(1.0,lda.documentTopicPrior(),0.0001);
        assertEquals(3, lda.numWords());
        assertEquals(2, lda.numTopics());
        for (int topic = 0; topic < lda.numTopics(); ++topic) {
            double[] topicWordProbsOut = lda.wordProbabilities(topic);
            for (int word = 0; word < lda.numWords(); ++word) {
                assertEquals(topicWordProbs[topic][word], topicWordProbsOut[word],
                             0.0001);
                assertEquals(topicWordProbs[topic][word],
                             lda.wordProbability(topic,word),
                             0.0001);
            }
        }
    }

    public void testExs() {
        double docTopicPrior = 1.0;
        double[][] topicWordProbs
            = new double[][] {
            // words = { river, bank, loan }
            { 0.7, .25, .05 },  // topic 1 = rivers
            { 0.01, .30, .69},  // topic 2 = finance
        };

        LatentDirichletAllocation lda
            = new LatentDirichletAllocation(docTopicPrior,
                                            topicWordProbs);

        try {
            new LatentDirichletAllocation(0,topicWordProbs);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            new LatentDirichletAllocation(-1.0,topicWordProbs);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            new LatentDirichletAllocation(Double.NaN,topicWordProbs);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            new LatentDirichletAllocation(Double.POSITIVE_INFINITY,topicWordProbs);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }


        try {
            new LatentDirichletAllocation(1.0, new double[0][2]);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            new LatentDirichletAllocation(1.0, new double[][] { { 0.0, 1.0 }, { -.2, 0.5 } });
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }


        try {
            new LatentDirichletAllocation(1.0, new double[][] { { 0.0, 1.0 }, { 0.5, 1.5 }, { 0.5, 0.5} });
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }


    }


}