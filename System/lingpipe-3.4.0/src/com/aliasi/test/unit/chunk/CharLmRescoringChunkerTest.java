package com.aliasi.test.unit.chunk;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.chunk.CharLmRescoringChunker;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;

public class CharLmRescoringChunkerTest extends BaseTestCase {

    public void testChunkHandler() {
    TokenizerFactory factory
        = new IndoEuropeanTokenizerFactory();

    CharLmRescoringChunker chunkerEstimator
        = new CharLmRescoringChunker(factory,8,
                     5,128,5.0);

    
    String text1 = "John J. Smith lives in Washington.";
    //              0123456789012345678901234567890123
    //              0         1         2         3
    ChunkingImpl chunking1 = new ChunkingImpl(text1);
    Chunk chunk11 = ChunkFactory.createChunk(0,13,"PER");
    Chunk chunk12 = ChunkFactory.createChunk(23,33,"LOC");
    chunking1.add(chunk11);
    chunking1.add(chunk12);
    
    for (int i = 0; i < 10; ++i)
        chunkerEstimator.handle(chunking1);

    assertChunking(chunkerEstimator,chunking1);

    String text2 = "Washington is near John";
    //              01234567890123456789012
    //              0         1         2  
    ChunkingImpl chunking2 = new ChunkingImpl(text2);
    Chunk chunk21 = ChunkFactory.createChunk(0,10,"LOC");
    Chunk chunk22 = ChunkFactory.createChunk(19,23,"PER");
    chunking2.add(chunk21);
    chunking2.add(chunk22);
    
    for (int i = 0; i < 10; ++i)
        chunkerEstimator.handle(chunking2);
    
    assertChunking(chunkerEstimator,chunking2);
    
    
    String text3 = "Washington D.C. is near Frank Jones.";
    //              012345678901234567890123456789012345
    //              0         1         2         3
    ChunkingImpl chunking3 = new ChunkingImpl(text3);
    Chunk chunk31 = ChunkFactory.createChunk(0,15,"LOC");
    Chunk chunk32 = ChunkFactory.createChunk(24,36,"PER");
    chunking3.add(chunk31);
    chunking3.add(chunk32);

    for (int i = 0; i < 10; ++i)
        chunkerEstimator.handle(chunking3);

    assertChunking(chunkerEstimator,chunking3);

    }

    void assertChunking(CharLmRescoringChunker chunkerEstimator,
            Chunking expectedChunking) {
    CharSequence text = expectedChunking.charSequence();
    Chunking chunking = chunkerEstimator.chunk(text);
    assertEqualsChunking(expectedChunking,chunking);

    try { 
        Chunker chunker 
        = (Chunker) AbstractExternalizable.compile(chunkerEstimator);
        Chunking chunking2 = chunker.chunk(text);
        assertEqualsChunking(expectedChunking,chunking2);
    } catch (IOException e) {
        fail(e.toString());
    } catch (ClassNotFoundException e) {
        fail(e.toString());
    }
    }


    void assertEqualsChunking(Chunking expectedChunking, Chunking chunking) {
    assertEquals(expectedChunking.charSequence(),
             chunking.charSequence());
    assertEquals(expectedChunking.chunkSet(),
             chunking.chunkSet());
    }
}
