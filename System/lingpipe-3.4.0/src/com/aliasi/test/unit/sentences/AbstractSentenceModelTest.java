package com.aliasi.test.unit.sentences;

import com.aliasi.sentences.AbstractSentenceModel;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.test.unit.BaseTestCase;

import java.util.ArrayList;
import java.util.Collection;


public class AbstractSentenceModelTest extends BaseTestCase {

    public void testReturns() {
    SentenceModel model = new TestModel();
    ArrayList listExpected = new ArrayList();
    listExpected.add(new Integer(4));
    ArrayList listFound = new ArrayList();
    model.boundaryIndices(new String[] { "a", "b", "c", "d" },
                  new String[] { "", "", "", "", "" },
                  0,5,listFound);
    assertEquals(listExpected,listFound);

    int[] indicesFound 
        = model.boundaryIndices(new String[] { "a", "b", "c", "d" },
                    new String[] { "", "", "", "", "" });
    assertEquals(1,indicesFound.length);
    assertEquals(3,indicesFound[0]);
    
    int[] indicesFound2 
        = ((AbstractSentenceModel) model)
        .boundaryIndices(new String[] { "a", "b", "c", "d" },
                 new String[] { "", "", "", "", "" },
                 2,2);
    assertEquals(1,indicesFound2.length);
    assertEquals(3,indicesFound2[0]);
    }

    public void testExceptions() {
    AbstractSentenceModel model = new TestModel();
    try {
        model.boundaryIndices(new String[] { "a", "b" },
                  new String[] { "", "" });
        fail();
    } catch (IllegalArgumentException e) {
        succeed();
    }

    try {
        model.boundaryIndices(new String[] { "a", "b" },
                  new String[] { "", "" },
                  0,2);
        fail();
    } catch (IllegalArgumentException e) {
        succeed();
    }

    try {
        model.boundaryIndices(new String[] { "a" },
                  new String[] { "", "" },
                  1,1);
        fail();
    } catch (IllegalArgumentException e) {
        succeed();
    }

    }


    static class TestModel extends AbstractSentenceModel {
    public void boundaryIndices(String[] tokens,
                    String[] whitespaces,
                    int start, int length,
                    Collection indices) {
        if (length > 0)
        indices.add(new Integer(start+length-1));
    }
    }

}