package com.aliasi.test.unit.cluster;

import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.LeafDendrogram;
import com.aliasi.cluster.LinkDendrogram;
import com.aliasi.cluster.CompleteLinkClusterer;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.util.Distance;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompleteLinkClustererTest extends BaseTestCase {

    public void testTrue() {
        assertTrue(true);
    }

    public void testBoundaries() {
        // cut and paste from single link
        CompleteLinkClusterer<String> clusterer 
            = new CompleteLinkClusterer<String>(SingleLinkClustererTest.TEST_DISTANCE);

        Set<String> elts0 = new HashSet<String>();
        try {
            clusterer.hierarchicalCluster(elts0);
            fail();
        } catch (IllegalArgumentException iae) {
            succeed();
        }
        Set<Set<String>> clusters = clusterer.cluster(elts0);
        assertEquals(0,clusters.size());

        Set<String> elts1 = new HashSet<String>();
        elts1.add("A");
        Set<Set<String>> clustering = new HashSet<Set<String>>();
        clustering.add(elts1);
        assertEquals(clustering,clusterer.cluster(elts1));
        Dendrogram<String> dendro1 = clusterer.hierarchicalCluster(elts1);
        assertTrue(dendro1 instanceof LeafDendrogram);
        assertEquals(elts1,dendro1.memberSet());
        assertEquals(0.0,dendro1.score(),0.001);
    }


    public void testOne() {

        CompleteLinkClusterer<String> clusterer 
            = new CompleteLinkClusterer<String>(SingleLinkClustererTest.TEST_DISTANCE);

        Set<String> elts = new HashSet<String>();
        elts.add("A");
        elts.add("B");
        elts.add("C");
        elts.add("D");
        elts.add("E");
        Dendrogram<String> dendro = clusterer.hierarchicalCluster(elts);
        
        Set<String> a = new HashSet<String>();
        a.add("A");
        Set<String> b = new HashSet<String>();
        b.add("B");
        Set<String> c = new HashSet<String>();
        c.add("C");
        Set<String> d = new HashSet<String>();
        d.add("D");
        Set<String> e = new HashSet<String>();
        e.add("E");
        
        Set<String> ab = new HashSet<String>();
        ab.addAll(a);
        ab.addAll(b);
        
        Set<String> abc = new HashSet<String>();
        abc.addAll(ab);
        abc.addAll(c);
        
        Set<String> de = new HashSet<String>();
        de.addAll(d);
        de.addAll(e);

        Set<String> abcde = new HashSet<String>();
        abcde.addAll(abc);
        abcde.addAll(de);

        assertEquals(abcde,dendro.memberSet());
        
        Set<Set<String>> p1 = new HashSet<Set<String>>();
        p1.add(abcde);
        assertEquals(p1,dendro.partitionK(1));

        Set<Set<String>> p2 = new HashSet<Set<String>>();
        p2.add(abc);
        p2.add(de);
        assertEquals(p2,dendro.partitionK(2));

        Set<Set<String>> p3 = new HashSet<Set<String>>();
        p3.add(abc);
        p3.add(d);
        p3.add(e);
        assertEquals(p3,dendro.partitionK(3));
        
        Set<Set<String>> p4 = new HashSet<Set<String>>();
        p4.add(ab);
        p4.add(c);
        p4.add(d);
        p4.add(e);
        assertEquals(p4,dendro.partitionK(4));

        Set<Set<String>> p5 = new HashSet<Set<String>>();
        p5.add(a);
        p5.add(b);
        p5.add(c);
        p5.add(d);
        p5.add(e);
        assertEquals(p5,dendro.partitionK(5));

        assertEquals(9.0,dendro.score(),0.001);

        try {
            dendro.partitionK(0);
            fail();
        } catch (IllegalArgumentException iae) {
            succeed();
        }

        try {
            dendro.partitionK(6);
            fail();
        } catch (IllegalArgumentException iae) {
            succeed();
        }
    }


    public void testSix() {
        Integer[] ints = new Integer[10];
        for (int i = 0; i < ints.length; ++i)
            ints[i] = new Integer(i);
        
        SingleLinkClustererTest.FixedDistance<Integer> dist 
            = new SingleLinkClustererTest.FixedDistance<Integer>();
        double[][] vals = new double[][] 
            { { 13 },
              { 21,  9 },
              { 18, 19, 22 },
              {  4, 15, 20,  3 },
              {  8, 14, 12, 23,  5},
              {  7, 10, 11, 27, 24,  6 },
              { 28, 16, 17,  1,  2, 25, 26 } };
        for (int i = 0; i < vals.length; ++i)
            for (int j = 0; j < vals[i].length; ++j)
                dist.setVal(ints[i+1],ints[j],vals[i][j]);

        Set<Integer> elts = new HashSet<Integer>();
        for (int i = 0; i < 8; ++i)
            elts.add(ints[i]);

        CompleteLinkClusterer clusterer = new CompleteLinkClusterer(dist);
        Dendrogram dendrogram = clusterer.hierarchicalCluster(elts);

        Set<Integer> dtrs1 = new HashSet<Integer>();
        dtrs1.add(ints[0]);
        dtrs1.add(ints[5]);
        dtrs1.add(ints[6]);
        dtrs1.add(ints[1]);
        dtrs1.add(ints[2]);

        Set<Integer> dtrs2 = new HashSet<Integer>();
        dtrs2.add(ints[3]);
        dtrs2.add(ints[7]);
        dtrs2.add(ints[4]);

        assertTrue(dendrogram instanceof LinkDendrogram);
        LinkDendrogram linkDendro = (LinkDendrogram) dendrogram;
        Dendrogram dendro1 = linkDendro.dendrogram1();
        Dendrogram dendro2 = linkDendro.dendrogram2();
        assertTrue(dendro1.memberSet().equals(dtrs1) 
                   && dendro2.memberSet().equals(dtrs2)
                   || 
                   dendro2.memberSet().equals(dtrs1)
                   && dendro1.memberSet().equals(dtrs2));
        // just check top level
        // expect:     {{{0+{5+6}}+{1+2}}+{{3+7}+4}}
    }
}
