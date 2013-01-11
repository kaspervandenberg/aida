package com.aliasi.test.unit.cluster;

import com.aliasi.cluster.Clusterer;
import com.aliasi.cluster.KMeansClusterer;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;

import com.aliasi.test.unit.BaseTestCase;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;



public class KMeansClustererTest extends BaseTestCase {

    static TokenFeatureExtractor FEATURE_EXTRACTOR
        = new TokenFeatureExtractor(IndoEuropeanTokenizerFactory.FACTORY);

    static String AA = "A A";
    static String AAA = "A A A";
    static String BBB = "B B B";
    static String CCC = "C C C";
    static String AAB = "A A B";

    public void testZero() {
        int numClusters = 0;
        int maxIterations = 10;
        try {
            KMeansClusterer<String> clusterer
                = new KMeansClusterer(FEATURE_EXTRACTOR,
                                      numClusters,
                                      maxIterations);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }


        numClusters = 10;
        maxIterations = -1;
        try {
            KMeansClusterer<String> clusterer
                = new KMeansClusterer(FEATURE_EXTRACTOR,
                                      numClusters,
                                      maxIterations);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }
    }

    public void testOne() {
        int numClusters = 1;
        int maxIterations = 10;
        KMeansClusterer<String> clusterer
            = new KMeansClusterer(FEATURE_EXTRACTOR,
                                  numClusters,
                                  maxIterations);

        assertCluster(clusterer,
                      new String[] { },
                      new String[][] { });

        assertCluster(clusterer,
                      new String[] { AAA },
                      new String[][] { { AAA } });

        assertCluster(clusterer,
                      new String[] { AAA, BBB },
                      new String[][] { { AAA, BBB } });

        assertCluster(clusterer,
                      new String[] { AAA, BBB, CCC },
                      new String[][] { { AAA, BBB, CCC } });
    }

    public void testTwo() {
        int numClusters = 2;
        int maxIterations = 10;
        KMeansClusterer<String> clusterer
            = new KMeansClusterer(FEATURE_EXTRACTOR,
                                  numClusters,
                                  maxIterations);

        assertCluster(clusterer,
                      new String[] { },
                      new String[][] { });

        assertCluster(clusterer,
                      new String[] { AAA },
                      new String[][] { { AAA } });

        assertCluster(clusterer,
                      new String[] { AAA, BBB },
                      new String[][] { { AAA }, { BBB } });

        // all inits produce same result in 2 iterations
        // { AAA, AAB }, { BBB }
        // { AAA, BBB }, { AAB }
        // { AAB, BBB }, { AAA }
        assertCluster(clusterer,
                      new String[] { AA, AAB, BBB },
                      new String[][] { { AA, AAB }, { BBB } });


    }

    public void testRandom() {
        Random random = new Random();
        int numInputs = 10; //random.nextInt(100) + 50;
        Set<String> inputSet = new HashSet<String>();
        for (int n = 0; n < numInputs; ++n) {
            StringBuilder sb = new StringBuilder();
            int length = random.nextInt(20) + 5;
            int numChars = 2; // random.nextInt(16);
            for (int i = 0; i < length; ++i) {
                if (i > 0) sb.append(' ');
                char c = (char) ( ((int)'a') + random.nextInt(numChars));
                sb.append(c);
            }
            inputSet.add(sb.toString());
        }


        int maxIterations = 10; // random.nextInt(50) + 50;
        int numClusters = 3; // random.nextInt(10) + 5;
        KMeansClusterer<String> clusterer
            = new KMeansClusterer(FEATURE_EXTRACTOR,
                                  numClusters,
                                  maxIterations);
        Set<Set<String>> clustering =
            clusterer.cluster(inputSet);

        assertCovers(clustering,inputSet);

    }

    void assertCovers(Set<Set<String>> clustering, Set<String> elts) {
        Set<String> clusteringElts = new HashSet<String>();
        for (Set<String> cluster : clustering) {
            assertTrue(cluster.size() > 0);
            clusteringElts.addAll(cluster);
        }
        assertEquals(elts,clusteringElts);
    }

    void assertCluster(Clusterer clusterer,
                       String[] inputs,
                       String[][] expectedClusters) {

        Set<Set<String>> expectedClustering
            = new HashSet<Set<String>>();
        for (int i = 0; i < expectedClusters.length; ++i)
            expectedClustering.add(toSet(expectedClusters[i]));

        Set<String> inputSet = toSet(inputs);
        Set<Set<String>> clustering = clusterer.cluster(inputSet);

        assertEquals(expectedClustering,clustering);
    }

    static Set<String> toSet(String[] xs) {
        Set<String> result = new HashSet<String>();
        for (int i = 0; i < xs.length; ++i)
            result.add(xs[i]);
        return result;
    }

}
