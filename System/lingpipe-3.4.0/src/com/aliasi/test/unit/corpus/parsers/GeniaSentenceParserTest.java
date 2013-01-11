package com.aliasi.test.unit.corpus.parsers;

import com.aliasi.corpus.parsers.GeniaSentenceParser;
import com.aliasi.corpus.ChunkHandler;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkFactory;

import com.aliasi.sentences.SentenceChunker;

import com.aliasi.test.unit.BaseTestCase;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class GeniaSentenceParserTest extends BaseTestCase {

    // test on a few lines from actual Genia corpus.
    public void testOne() throws Exception {
    String [] sents = new String[] {
        "Activation of the CD28 surface receptor provides a major costimulatory signal for T cell activation resulting in enhanced production of interleukin-2 (IL-2) and cell proliferation.",
        "In primary T lymphocytes we show that CD28 ligation leads to the rapid intracellular formation of reactive oxygen intermediates (ROIs) which are required for CD28-mediated activation of the NF-kappa B/CD28-responsive complex and IL-2 expression."
    };

    GeniaChunkHandler handler = new GeniaChunkHandler();
    GeniaSentenceParser parser = new GeniaSentenceParser(handler);
    parser.parse("src/com/aliasi/test/unit/corpus/parsers/GeniaSentenceParserData/test1.xml");
    handler.assertSentences(sents);
    }

    // empty abstract
    public void testTwo() throws Exception {
    String [] sents = new String[] {
    };

    GeniaChunkHandler handler = new GeniaChunkHandler();
    GeniaSentenceParser parser = new GeniaSentenceParser(handler);
    parser.parse("src/com/aliasi/test/unit/corpus/parsers/GeniaSentenceParserData/test2.xml");
    handler.assertSentences(sents);
    }


    // test on just one line from actual Genia corpus.
    public void testThree() throws Exception {
    String [] sents = new String[] {
        "These findings should be useful for therapeutic strategies and the development of immunosuppressants targeting the CD28 costimulatory pathway."
    };

    GeniaChunkHandler handler = new GeniaChunkHandler();
    GeniaSentenceParser parser = new GeniaSentenceParser(handler);
    parser.parse("src/com/aliasi/test/unit/corpus/parsers/GeniaSentenceParserData/test3.xml");
    handler.assertSentences(sents);
    }

    // abstract containing empty sentences
    public void testFour() throws Exception {
    String [] sents = new String[] {
    };

    GeniaChunkHandler handler = new GeniaChunkHandler();
    GeniaSentenceParser parser = new GeniaSentenceParser(handler);
    parser.parse("src/com/aliasi/test/unit/corpus/parsers/GeniaSentenceParserData/test4.xml");
    handler.assertSentences(sents);
    }

    // empty set
    public void testFive() throws Exception {
    String [] sents = new String[] {
    };

    GeniaChunkHandler handler = new GeniaChunkHandler();
    GeniaSentenceParser parser = new GeniaSentenceParser(handler);
    parser.parse("src/com/aliasi/test/unit/corpus/parsers/GeniaSentenceParserData/test5.xml");
    handler.assertSentences(sents);
    }


    class GeniaChunkHandler implements ChunkHandler {
    Chunking mChunking;

    public void handle(Chunking chunking) {
        mChunking = chunking;
    }

    public void assertSentences(String[] sents) {
        if (mChunking == null
        || mChunking.charSequence() == null 
        || mChunking.chunkSet() == null ) return;
        String refCharSequence = mChunking.charSequence().toString();
        Set refChunkSet = mChunking.chunkSet();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < sents.length; ++i) {
        if (i > 0) sb.append(" ");
        sb.append(sents[i]);
        }
        String respCharSequence = sb.toString();
        int offset = 0;
        LinkedHashSet respChunkSet = new LinkedHashSet(sents.length,1);
        for (int i = 0; i< sents.length; i++) {
        Chunk chunk = 
            ChunkFactory.
            createChunk(offset,offset+sents[i].length(),
                SentenceChunker.SENTENCE_CHUNK_TYPE);

        respChunkSet.add(chunk);
        offset += sents[i].length()+1;
        }
        assertEquals(refCharSequence,respCharSequence);
        assertEquals(refChunkSet.size(),respChunkSet.size());
        Iterator it1 = refChunkSet.iterator();
        Iterator it2 = respChunkSet.iterator();
        for (int i = 0 ; i < sents.length; ++i) {
        Chunk refChunk = (Chunk)it1.next();
        Chunk respChunk = (Chunk)it2.next();
        assertEquals(refChunk.start(),respChunk.start());
        assertEquals(refChunk.end(),respChunk.end());
        assertEquals(refChunk.type(),respChunk.type());
        }
        assertFalse(it1.hasNext());
        assertFalse(it2.hasNext());
    }
    }


}
