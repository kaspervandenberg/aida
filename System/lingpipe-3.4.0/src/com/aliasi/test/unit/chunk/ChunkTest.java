package com.aliasi.test.unit.chunk;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;

import com.aliasi.test.unit.BaseTestCase;

import java.util.Comparator;

public class ChunkTest extends BaseTestCase {


    public void testComparators() {
    Chunk[][] chunks = new Chunk[6][6];
    for (int i = 0; i < chunks.length; ++i)
        for (int j = i+1; j < chunks[i].length; ++j)
        chunks[i][j] = ChunkFactory.createChunk(i,j,"category");
    // starts later
    assertLess(Chunk.TEXT_ORDER_COMPARATOR,
            chunks[1][3],chunks[2][3]);
    assertLess(Chunk.TEXT_ORDER_COMPARATOR,
            chunks[1][3],chunks[3][4]);
    assertLess(Chunk.TEXT_ORDER_COMPARATOR,
            chunks[1][3],chunks[4][5]);
    // starts same, ends later
    assertLess(Chunk.TEXT_ORDER_COMPARATOR,
            chunks[1][3],chunks[1][4]);

    // starts later
    assertLess(Chunk.LONGEST_MATCH_ORDER_COMPARATOR,
            chunks[1][3],chunks[2][3]);
    assertLess(Chunk.LONGEST_MATCH_ORDER_COMPARATOR,
            chunks[1][3],chunks[3][4]);
    assertLess(Chunk.LONGEST_MATCH_ORDER_COMPARATOR,
            chunks[1][3],chunks[4][5]);
    // starts same, ends earlier
    assertLess(Chunk.LONGEST_MATCH_ORDER_COMPARATOR,
            chunks[1][4],chunks[1][3]);

    }

    public void assertLess(Comparator comp,
               Object o1, Object o2) {
    assertTrue(comp.compare(o1,o2) < 0);
    assertTrue(comp.compare(o2,o1) > 0);
    }

    public void assertSame(Comparator comp,
               Object o1, Object o2) {
    assertEquals(0,comp.compare(o1,o2));
    }

}
