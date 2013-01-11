package com.aliasi.test.unit.corpus.parsers;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.classify.BinaryLMClassifier;
import com.aliasi.classify.Classification;

import com.aliasi.corpus.ClassificationHandler;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.Parser;
import com.aliasi.corpus.parsers.Reuters21578Parser;

import java.io.IOException;
import java.io.File;

import java.util.ArrayList;
import java.util.List;

public class Reuters21578ParserTest extends BaseTestCase {

    File TEST_FILE = new File("src/com/aliasi/test/unit/corpus/parsers/ReutersCorpus.sgm");

    public void testOne() throws IOException {
        try {
            new Reuters21578Parser("foo",true,false);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        // total of 5 training cases
        assertParse("acq",1,4,2,2);
        assertParse("rice",0,5,0,4);
    }

    void assertParse(String topic,
                    int trainAccept, int trainReject,
                    int testAccept, int testReject) throws IOException {

        Parser<ClassificationHandler<CharSequence,Classification>> trainParser
            = new Reuters21578Parser(topic,true,false);
        CollectHandler handlerTrain = new CollectHandler();
        trainParser.setHandler(handlerTrain);
        trainParser.parse(TEST_FILE);
        assertEquals(trainAccept,handlerTrain.mAcceptTexts.size());
        assertEquals(trainReject,handlerTrain.mRejectTexts.size());

        // total of 4 test cases
        Parser<ClassificationHandler<CharSequence,Classification>> testParser
            = new Reuters21578Parser(topic,false,true);
        CollectHandler handlerTest = new CollectHandler();
        testParser.setHandler(handlerTest);
        testParser.parse(TEST_FILE);
        assertEquals(testAccept,handlerTest.mAcceptTexts.size());
        assertEquals(testReject,handlerTest.mRejectTexts.size());

        Parser<ClassificationHandler<CharSequence,Classification>> trainParser2
            = new Reuters21578Parser(topic,true,false);
        Parser<ClassificationHandler<CharSequence,Classification>> testParser2
            = new Reuters21578Parser(topic,false,true);

        Corpus<ClassificationHandler<CharSequence,Classification>> corpus
            = Reuters21578Parser.corpus(topic,new File("src/com/aliasi/test/unit/corpus/parsers"));
        CollectHandler handlerTest2 = new CollectHandler();
        CollectHandler handlerTrain2 = new CollectHandler();
        corpus.visitCorpus(handlerTrain2,handlerTest2);

        assertEquals(trainAccept,handlerTrain2.mAcceptTexts.size());
        assertEquals(trainReject,handlerTrain2.mRejectTexts.size());
        assertEquals(testAccept,handlerTest2.mAcceptTexts.size());
        assertEquals(testReject,handlerTest2.mRejectTexts.size());

        CollectHandler allHandler = new CollectHandler();
        corpus.visitCorpus(allHandler);
        assertEquals(trainAccept + testAccept,allHandler.mAcceptTexts.size());
        assertEquals(trainReject + testReject,allHandler.mRejectTexts.size());

    }

    static class CollectHandler implements ClassificationHandler<CharSequence,Classification> {
        List<String> mAcceptTexts = new ArrayList<String>();
        List<String> mRejectTexts = new ArrayList<String>();
        public void handle(CharSequence cs, Classification classification) {
            if (BinaryLMClassifier.DEFAULT_ACCEPT_CATEGORY.equals(classification.bestCategory()))
                mAcceptTexts.add(cs.toString());
            else if (BinaryLMClassifier.DEFAULT_REJECT_CATEGORY.equals(classification.bestCategory()))
                mRejectTexts.add(cs.toString());
            else
                throw new IllegalStateException("bad category=" + classification);
        }
    }


}