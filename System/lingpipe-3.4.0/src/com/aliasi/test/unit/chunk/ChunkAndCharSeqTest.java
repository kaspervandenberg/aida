package com.aliasi.test.unit.chunk;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkAndCharSeq;
import com.aliasi.chunk.ChunkFactory;

import com.aliasi.test.unit.BaseTestCase;

public class ChunkAndCharSeqTest extends BaseTestCase {

    public void testEquals() {
	String seq = "span of text";
	Chunk c1 = ChunkFactory.createChunk(0,1,"foo");
	ChunkAndCharSeq cacs1 = new ChunkAndCharSeq(c1,seq);
	assertEquals(seq,cacs1.charSequence());
	assertEquals(c1,cacs1.chunk());
	assertEquals(seq.subSequence(0,1),cacs1.span());
	ChunkAndCharSeq cacs2 = new ChunkAndCharSeq(c1,seq);
	assertEquals(cacs1,cacs2);
	assertFullEquals(cacs1,cacs1);
    }

    public void testContext() {
	String seq = "0123456789";
	Chunk c1 = ChunkFactory.createChunk(3,6,"foo");
	ChunkAndCharSeq cacs1 = new ChunkAndCharSeq(c1,seq);
	assertEquals(cacs1.span().toString(),"345");
	assertEquals(cacs1.spanStartContext(1).toString(),"23");
	assertEquals(cacs1.spanEndContext(1).toString(),"56");
    }


    public void testThrowConstructor() {
	try {
	    String seq = "012345";
	    Chunk c1 = ChunkFactory.createChunk(0,101,"foo");
	    new ChunkAndCharSeq(c1,seq);
	    fail();
	} catch (IllegalArgumentException e) {
	    succeed();
	}
    }

    public void testThrowContext() {
	try {
	    String seq = "012345";
	    Chunk c1 = ChunkFactory.createChunk(0,1,"foo");
	    ChunkAndCharSeq cacs1 = new ChunkAndCharSeq(c1,seq);
	    cacs1.spanStartContext(-9);
	    fail();
	} catch (IllegalArgumentException e) {
	    succeed();
	}
    }
}
