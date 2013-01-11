package com.aliasi.test.unit.chunk;

import com.aliasi.chunk.RegExChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.ChunkingImpl;


import com.aliasi.test.unit.BaseTestCase;

public class RegExChunkerTest extends BaseTestCase {

    public void test1() {
	assertChunking(new RegExChunker("abc|de","typeA", -1.2),
		       "abcdef",
		       new Chunk[] { ChunkFactory.createChunk(0,3,"typeA",-1.2),
				     ChunkFactory.createChunk(3,5,"typeA",-1.2) 
		       });
	assertChunking(new RegExChunker("(abc|ab|a)","typeA", -1.2),
		       "abcdef",
		       new Chunk[] { ChunkFactory.createChunk(0,3,"typeA",-1.2)
		       });
	assertChunking(new RegExChunker("(a*ba*|b*ab*)","typeB",-12),
		       "aabaa bab",
		       new Chunk[] { ChunkFactory.createChunk(0,5,"typeB",-12),
				     ChunkFactory.createChunk(6,8,"typeB",-12),
				     ChunkFactory.createChunk(8,9,"typeB",-12) });
    }

    void assertChunking(Chunker chunker, String in,
			Chunk[] chunks) {
	ChunkingImpl chunking = new ChunkingImpl(in);
	for (int i = 0; i < chunks.length; ++i)
	    chunking.add(chunks[i]);
	assertEquals(chunking,chunker.chunk(in));
    }

}