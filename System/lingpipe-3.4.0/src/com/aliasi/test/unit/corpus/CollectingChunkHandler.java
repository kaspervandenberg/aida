package com.aliasi.test.unit.corpus;

import com.aliasi.chunk.Chunking;

import com.aliasi.corpus.ChunkHandler;

import com.aliasi.test.unit.BaseTestCase;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class CollectingChunkHandler 
    extends BaseTestCase 
    implements ChunkHandler {

    List mChunkingList = new ArrayList();

    public void handle(Chunking chunking) {
    mChunkingList.add(chunking);
    }

    public void assertEquals(List chunkingList) {
    assertEquals(chunkingList,mChunkingList);
    }

    public void assertEquals(Chunking[] chunkings) {
    assertEquals(Arrays.asList(chunkings),mChunkingList);
    }

}