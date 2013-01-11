package com.aliasi.test.unit.hmm;

import com.aliasi.hmm.HmmCharLmEstimator;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.hmm.TagWordLattice;

import com.aliasi.util.FastCache;
import com.aliasi.util.ScoredObject;


import com.aliasi.test.unit.BaseTestCase;

import java.util.Iterator;

public class HmmDecoderTest extends BaseTestCase {

    public void testCons() {
	HmmCharLmEstimator est = new HmmCharLmEstimator();


	String[] toks1 = new String[] { "John", "ran", "." };
	String[] tags1 = new String[] { "PN", "IV", "." };
	est.handle(toks1,null,tags1);

	String[] toks2 = new String[] { "Mary", "ran", "." };
	String[] tags2 = new String[] { "PN", "IV", "." };
	est.handle(toks2,null,tags2);

	String[] toks3 = new String[] { "Fred", "ran", "." };
	String[] tags3 = new String[] { "PN", "IV", "." };
	est.handle(toks3,null,tags3);

	String[] toks4 = new String[] { "John", "likes", "Mary", "." };
	String[] tags4 = new String[] { "PN", "TV", "PN", "." };
	est.handle(toks4,null,tags4);

	HmmDecoder decoder = new HmmDecoder(est);
	HmmDecoder decoderCached = new HmmDecoder(est,
						  new FastCache(1000),
						  new FastCache(1000));
    
	assertEqualsArray(tags4,
			  decoder.firstBest(toks4));
	for (int i = 0; i < 5; ++i) {
	    assertEqualsArray(tags4,
			      decoderCached.firstBest(toks4));
	}

	String[] empty = new String[0];
	assertEqualsArray(empty, decoder.firstBest(empty));

	for (int i = 0; i < 5; ++i)
	    assertEqualsArray(empty, decoderCached.firstBest(empty));
        
    }
    
    public void testLattice() {
	HmmCharLmEstimator est = new HmmCharLmEstimator();


	String[] toks1 = new String[] { "John", "ran", "." };
	String[] tags1 = new String[] { "PN", "IV", "." };
	est.handle(toks1,null,tags1);

	String[] toks2 = new String[] { "Mary", "ran", "." };
	String[] tags2 = new String[] { "PN", "IV", "." };
	est.handle(toks2,null,tags2);

	String[] toks3 = new String[] { "Fred", "ran", "." };
	String[] tags3 = new String[] { "PN", "IV", "." };
	est.handle(toks3,null,tags3);

	String[] toks4 = new String[] { "John", "likes", "Mary", "." };
	String[] tags4 = new String[] { "PN", "TV", "PN", "." };
	est.handle(toks4,null,tags4);

	HmmDecoder decoder = new HmmDecoder(est);
	HmmDecoder decoderCached = new HmmDecoder(est,new FastCache(1000),
						  new FastCache(1000));

	TagWordLattice lattice = decoder.lattice(toks4);
	decoderCached.lattice(toks4);
    
	String[] decodedTags = lattice.bestForwardBackward();
	assertEqualsArray(tags4,decodedTags);

	for (int i = 0; i < 5; ++i) {
	    String[] decodedTagsCached = lattice.bestForwardBackward();
	    assertEqualsArray(tags4,decodedTagsCached);
	}

	String[] empty = new String[0];
	lattice = decoder.lattice(empty);
	assertEqualsArray(empty, lattice.bestForwardBackward());

	for (int i = 0; i < 5; ++i) {
	    lattice = decoderCached.lattice(empty);
	    assertEqualsArray(empty, lattice.bestForwardBackward());
	}
              
    }

    public void testNBest() {
	HmmCharLmEstimator est = new HmmCharLmEstimator();


	String[] toks1 = new String[] { "John", "ran", "." };
	String[] tags1 = new String[] { "PN", "IV", "." };
	est.handle(toks1,null,tags1);

	String[] toks2 = new String[] { "Mary", "ran", "." };
	String[] tags2 = new String[] { "PN", "IV", "." };
	est.handle(toks2,null,tags2);

	String[] toks3 = new String[] { "Fred", "ran", "." };
	String[] tags3 = new String[] { "PN", "IV", "." };
	est.handle(toks3,null,tags3);

	String[] toks4 = new String[] { "John", "likes", "Mary", "." };
	String[] tags4 = new String[] { "PN", "TV", "PN", "." };
	est.handle(toks4,null,tags4);

	HmmDecoder decoder = new HmmDecoder(est);
	HmmDecoder decoderCached = new HmmDecoder(est,
						  new FastCache(1000),
						  new FastCache(1000));


	Iterator nBest = decoder.nBest(toks4);
	ScoredObject best = (ScoredObject) nBest.next();
	String[] decodedTags = (String[]) best.getObject();
	assertEqualsArray(tags4,decodedTags);

	Iterator nBestC = decoderCached.nBest(toks4);
	ScoredObject bestC = (ScoredObject) nBestC.next();
	String[] decodedTagsC = (String[]) bestC.getObject();
	assertEqualsArray(tags4,decodedTagsC);

	String[] tags5 = new String[] { "A", "B", "C", "." };
	est.handle(toks4,null,tags5);

        nBest = decoder.nBest(toks4);
        double lastScore = Double.POSITIVE_INFINITY;
	for (int i = 0; nBest.hasNext(); ++i) {
	    ScoredObject nextBest = (ScoredObject) nBest.next();
            double score = nextBest.score();
            assertTrue(score < lastScore);
            lastScore = score;
	    String[] nextTags = (String[]) nextBest.getObject();
            assertEquals(4,nextTags.length);
	}

	HmmDecoder decoderCached2 = new HmmDecoder(est,
						   new FastCache(1000),
						   new FastCache(1000));
	for (int k = 0; k < 5; ++k) {
	    Iterator nBestC2 = decoderCached2.nBest(toks4);
	    double lastScoreC2 = Double.POSITIVE_INFINITY;
	    for (int i = 0; nBestC2.hasNext(); ++i) {
		ScoredObject nextBestC2 = (ScoredObject) nBestC2.next();
		double scoreC2 = nextBestC2.score();
		assertTrue(scoreC2 < lastScoreC2);
		lastScoreC2 = scoreC2;
		String[] nextTagsC2 = (String[]) nextBestC2.getObject();
		assertEquals(4,nextTagsC2.length);
	    }
	}

        ScoredObject firstBest = (ScoredObject) decoder.nBest(toks4).next();
        String[] yield = (String[]) firstBest.getObject();
        assertEqualsArray(decoder.firstBest(toks4),yield);

	for (int i = 0; i < 5; ++i) {
	    ScoredObject firstBestC2 
		= (ScoredObject) decoderCached2.nBest(toks4).next();
	    String[] yieldC2 = (String[]) firstBestC2.getObject();
	    assertEqualsArray(decoderCached2.firstBest(toks4),yieldC2);
	}

	String[] empty = new String[0];
	Iterator nBest2 = decoder.nBest(empty);
	nBest2.next();
	assertFalse(nBest2.hasNext());

	for (int i = 0; i < 5; ++i) {
	    Iterator nBestC2 = decoderCached2.nBest(empty);
	    nBestC2.next();
	    assertFalse(nBestC2.hasNext());
	}    
    }

    public void testNBestFull() {
	HmmCharLmEstimator est = new HmmCharLmEstimator();


	String[] toks1 = new String[] { "a", "b" };
	String[] tags1 = new String[] { "X", "Y" };
	est.handle(toks1,null,tags1);

	String[] toks2 = new String[] { "b", "a" };
	est.handle(toks2,null,tags1);

	String[] toks3 = new String[] { "a", "b" };
	String[] tags3 = new String[] { "Y", "X" };
	est.handle(toks3,null,tags3);

	String[] toks4 = new String[] { "b", "a" };
	String[] tags4 = new String[] { "Y", "X" };
	est.handle(toks4,null,tags4);

	String[] toks5 = new String[] { "a", "b" };
	String[] tags5 = new String[] { "X", "X" };
	est.handle(toks5,null,tags5);

	String[] toks6 = new String[] { "b", "a" };
	String[] tags6 = new String[] { "X", "X" };
	est.handle(toks6,null,tags6);

	String[] toks7 = new String[] { "a", "b" };
	String[] tags7 = new String[] { "Y", "Y" };
	est.handle(toks7,null,tags7);

	String[] toks8 = new String[] { "b", "a" };
	String[] tags8 = new String[] { "Y", "Y" };
	est.handle(toks8,null,tags8);

	HmmDecoder decoder = new HmmDecoder(est);
	assertNBestCount(decoder,new String[] { }, 1);
	assertNBestCount(decoder,new String[] { "a" }, 2);
	assertNBestCount(decoder,new String[] { "a", "a" }, 4);
	assertNBestCount(decoder,new String[] { "a", "a", "a" }, 8);
	assertNBestCount(decoder,new String[] { "a", "a", "a", "a" }, 16);

	HmmDecoder decoderCached = new HmmDecoder(est,
						  new FastCache(1000),
						  new FastCache(1000));
	for (int i = 0; i < 5; ++i) {
	    assertNBestCount(decoderCached,new String[] { }, 1);
	    assertNBestCount(decoderCached,new String[] { "a" }, 2);
	    assertNBestCount(decoderCached,new String[] { "a", "a" }, 4);
	    assertNBestCount(decoderCached,new String[] { "a", "a", "a" }, 8);
	    assertNBestCount(decoderCached,
			     new String[] { "a", "a", "a", "a" }, 16);
	}
    }

    void assertNBestCount(HmmDecoder decoder, String[] toks, int expCount) {
	Iterator nBest = decoder.nBest(toks);
	int count = 0;
	while (nBest.hasNext()) {
	    ++count;
	    nBest.next();
	}
	assertEquals(expCount,count);
    }
}
