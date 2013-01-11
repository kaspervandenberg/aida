package com.aliasi.test.unit.chunk;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;

import com.aliasi.test.unit.BaseTestCase;

import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;

public class ChunkFactoryTest extends BaseTestCase {

    public void testTypeScore() {
    Chunk c1 = ChunkFactory.createChunk(4,7,"word",1.0);
    assertEquals(4,c1.start());
    assertEquals(7,c1.end());
    assertEquals(1.0,c1.score(),0.0001);
    assertEquals("word",c1.type());
    }

    public void testType() {
    Chunk c1 = ChunkFactory.createChunk(4,7,"word");
    assertEquals(4,c1.start());
    assertEquals(7,c1.end());
    assertEquals(ChunkFactory.DEFAULT_CHUNK_SCORE,c1.score(),0.0001);
    assertEquals("word",c1.type());
    }

    public void testScore() {
    Chunk c1 = ChunkFactory.createChunk(4,7,1.0);
    assertEquals(4,c1.start());
    assertEquals(7,c1.end());
    assertEquals(1.0,c1.score(),0.0001);
    assertEquals(ChunkFactory.DEFAULT_CHUNK_TYPE,c1.type());
    }

    public void testNone() {
    Chunk c1 = ChunkFactory.createChunk(4,7);
    assertEquals(4,c1.start());
    assertEquals(7,c1.end());
    assertEquals(ChunkFactory.DEFAULT_CHUNK_SCORE,c1.score(),0.0001);
    assertEquals(ChunkFactory.DEFAULT_CHUNK_TYPE,c1.type());
    }

    public void testThrow() {
    try {
        ChunkFactory.createChunk(4,2);
        fail();
    } catch (IllegalArgumentException e) {
        succeed();
    }

    try {
        ChunkFactory.createChunk(-1,3);
        fail();
    } catch (IllegalArgumentException e) {
        succeed();
    }
    }

    public void testOrder() {
    Chunk c1 = ChunkFactory.createChunk(0,1,"foo");
    Chunk c2 = ChunkFactory.createChunk(1,2,"bar");
    Chunk c3 = ChunkFactory.createChunk(2,3,"foo");
    Chunk[] chunks = new Chunk[] { c1, c2, c3 };
    TreeSet set = new TreeSet(Chunk.TEXT_ORDER_COMPARATOR);
    set.addAll(Arrays.asList(chunks));
    Iterator it = set.iterator();
    for (int i = 0; i < chunks.length; ++i)
        assertEquals(chunks[i],it.next());
    assertFalse(it.hasNext());
    }

    public void testOverlapOrder() {
    Chunk c1 = ChunkFactory.createChunk(0,1,"foo");
    Chunk c2 = ChunkFactory.createChunk(0,3,"foo");
    Chunk c3 = ChunkFactory.createChunk(2,4,"bar");
    Chunk[] chunks = new Chunk[] { c1, c2, c3 };
    TreeSet set = new TreeSet(Chunk.TEXT_ORDER_COMPARATOR);
    set.addAll(Arrays.asList(chunks));
    Iterator it = set.iterator();
    for (int i = 0; i < chunks.length; ++i)
        assertEquals(chunks[i],it.next());
    assertFalse(it.hasNext());
    }

    public void testEquality() {
    Chunk c1 = ChunkFactory.createChunk(0,2,"foo");
    Chunk c2 = ChunkFactory.createChunk(0,2,"foo");
    Chunk c3 = ChunkFactory.createChunk(0,2,"bar");
    Chunk c4 = ChunkFactory.createChunk(0,1,"foo");
    Chunk c5 = ChunkFactory.createChunk(1,2,"foo");
    assertFullEquals(c1,c1);
    assertFullEquals(c1,c2);
    assertNotEquals(c1,c3);
    assertNotEquals(c1,c4);
    assertNotEquals(c1,c5);
    }

}
