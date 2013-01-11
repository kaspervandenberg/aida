package com.aliasi.test.unit.chunk;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.util.Strings;

import java.util.LinkedHashSet;
import java.util.Set;


public class ChunkingImplTest extends BaseTestCase {

    public void testHashCode() {
        ChunkingImpl c1 = new ChunkingImpl("foo bar");

        assertEquals(Strings.hashCode(c1.charSequence())
                     + 31 * c1.chunkSet().hashCode(),
                     c1.hashCode());

        c1.add(ChunkFactory.createChunk(0,3,"FOO"));
        assertEquals(Strings.hashCode(c1.charSequence())
                     + 31 * c1.chunkSet().hashCode(),
                     c1.hashCode());

        c1.add(ChunkFactory.createChunk(4,7,"BAR"));
        assertEquals(Strings.hashCode(c1.charSequence())
                     + 31 * c1.chunkSet().hashCode(),
                     c1.hashCode());
    }


    public void testEquals() {
        StringBuffer sb = new StringBuffer("foo bar");
        ChunkingImpl c1 = new ChunkingImpl(sb);
        ChunkingImpl c2 = new ChunkingImpl(sb.toString());
        assertFullEquals(c1,c2);

        c1.add(ChunkFactory.createChunk(0,3,"FOO"));
        assertFalse(c1.equals(c2));

        c2.add(ChunkFactory.createChunk(0,3,"FOO"));
        assertFullEquals(c1,c2);

        c1.add(ChunkFactory.createChunk(0,3,"FOO"));
        assertFullEquals(c1,c2);

        c2.add(ChunkFactory.createChunk(4,7,"BAR"));
        assertFalse(c1.equals(c2));

        c1.add(ChunkFactory.createChunk(4,7,"BAR"));
        assertFullEquals(c1,c2);
    }

    public void testSeq() {
        String seq = "span of text";
        Chunking c1 = new ChunkingImpl(seq);
        Chunking c2 = new ChunkingImpl(seq.toCharArray(),0,seq.length());
        assertEquals(seq,c1.charSequence());
        assertEquals(seq,c2.charSequence());
        assertEquals(c1.charSequence(),c2.charSequence());
    }

    public void testSet() {
        String seq = "012345";
        ChunkingImpl chunking = new ChunkingImpl(seq);
        Set set1 = new LinkedHashSet();
        assertEquals(set1,chunking.chunkSet());
    }

    public void testAdd() {
        String seq = "012345";
        ChunkingImpl chunking = new ChunkingImpl(seq);
        Chunk c1 = ChunkFactory.createChunk(0,1,"foo");
        chunking.add(c1);
        LinkedHashSet set1 = new LinkedHashSet();
        set1.add(c1);
        LinkedHashSet set2 = new LinkedHashSet(chunking.chunkSet());
        assertEquals(set1,set2);

    }

    public void testThrow() {
        try {
            String seq = "012345";
            ChunkingImpl chunking = new ChunkingImpl(seq);
            Chunk c1 = ChunkFactory.createChunk(0,101,"foo");
            chunking.add(c1);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            String seq = "012345";
            ChunkingImpl chunking = new ChunkingImpl(seq);
            Chunk c1 = ChunkFactory.createChunk(100,101,"foo");
            chunking.add(c1);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

    }


}
