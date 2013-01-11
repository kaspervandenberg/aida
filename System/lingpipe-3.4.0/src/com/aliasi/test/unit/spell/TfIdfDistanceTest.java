package com.aliasi.test.unit.spell;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.spell.TfIdfDistance;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import java.util.HashSet;

public class TfIdfDistanceTest extends BaseTestCase {

    public void testOne() {
        IndoEuropeanTokenizerFactory tokenizerFactory
            = new IndoEuropeanTokenizerFactory();
        TfIdfDistance distance 
            = new TfIdfDistance(tokenizerFactory);
        distance.trainIdf("abc de jj");
        distance.trainIdf("de jj");
        distance.trainIdf("abc de jj");
        distance.trainIdf("fg jj");

        HashSet expectedTerms = new HashSet();
        expectedTerms.add("abc");
        expectedTerms.add("de");
        expectedTerms.add("fg");
        expectedTerms.add("jj");

        assertEquals(expectedTerms,distance.termSet());
        assertEquals(4,distance.numDocuments());
        assertEquals(expectedTerms.size(),distance.numTerms());
        
        assertEquals(0,distance.docFrequency("k"));
        assertEquals(1,distance.docFrequency("fg"));
        assertEquals(2,distance.docFrequency("abc"));
        assertEquals(3,distance.docFrequency("de"));
        assertEquals(4,distance.docFrequency("jj"));

        assertEquals(0.0,distance.idf("k"));
        assertIdf(distance,"fg");
        assertIdf(distance,"abc");
        assertIdf(distance,"de");
        assertIdf(distance,"jj");

        assertDistance(1.0,"","abc",distance);
        assertDistance(1.0,"abc","de",distance);
        assertDistance(1.0,"abc jj","de jj",distance);
        
        assertDistance(0.0,"","",distance);
        assertDistance(0.0,"abc","abc",distance);
        assertDistance(0.0,"jj","jj",distance);
        assertDistance(0.0,"abc jj","abc jj",distance);
        assertDistance(0.0,"abc de jj","de abc",distance);
        assertDistance(0.0,"abc de abc jj","de abc abc",distance);
        assertDistance(0.0,"de abc de abc jj","de abc abc de",distance);

        double idf_fg = distance.idf("fg");
        double idf_abc = distance.idf("abc");
        double idf_de = distance.idf("de");

        // fg abc vs. fg de
        double expected 
            = 1.0 
            - idf_fg
            / Math.sqrt((idf_fg + idf_de) * (idf_fg + idf_abc));
        assertDistance(expected,"fg abc","fg de",distance);
        
    }

    void assertDistance(double expectedVal, String cs1, String cs2, 
                        TfIdfDistance distance) {
        assertEquals(expectedVal, distance.distance(cs1,cs2), 0.001);
        assertEquals(expectedVal, distance.distance(cs2,cs1), 0.001);
    }

    void assertIdf(TfIdfDistance distance, String cs) {
        assertEquals(Math.log(((double)distance.numDocuments())
                              / (double) distance.docFrequency(cs)),
                     distance.idf(cs), 0.001);
    }        


}
