package com.aliasi.test.unit.corpus.parsers;

import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.corpus.ChunkHandler;

import com.aliasi.corpus.parsers.GeneTagChunkParser;

import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import com.aliasi.test.unit.BaseTestCase;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class GeneTagChunkParserTest extends BaseTestCase {


    String FORMAT
        = "P00001606T0076|14 33|alkaline phosphatases"
        + "\n"
        + "P00001606T0076|37 50|5-nucleotidase"
        + "\n"
        + "P00015731A0090|36 52|carbonic anhydrase"
        + "\n";

    String S0 = "Comparison with alkaline phosphatases and 5-nucleotidase";
    //           01234567890123456789012345678901234567890123456789012345
    //                     1         2         3         4         5
    //           0123456789 0123 45678901 234567890123 4567 8901234567890
    //           0          1          2          3           4         5

    String S1 = "Pharmacologic aspects of neonatal hyperbilirubinemia.";

    String S2 = "Intravenous administration (25 mg/kg) of carbonic anhydrase inhibitors (acetazolamide, methazolamide, dichlorphenamide, sulthiame) induced an early important rise of cortical p O2, which is not dependent on increase of p O2 and p CO2 and decrease of pH in arterial blood.";
    //           0123456789012345678901234567890123456789012345678901234567890
    //                     1          2         3         4         5        6
    //           01234567890 12345678901234 567 890123 45 67890123 456789012345
    //                     1          2           3           4          5
    String SENT =
        "P00001606T0076"
        + "\n"
        + S0
        + "\n"
        + "P00008171T0000"
        + "\n"
        + S1
        + "\n"
        + "P00015731A0090"
        + "\n"
        + S2
        + "\n";

    String GENE = GeneTagChunkParser.GENE_CHUNK_TYPE;

    public void testOne() throws IOException {
        File formatFile = Files.createTempFile("GeneTagChunkParserTest.format");
        Files.writeStringToFile(FORMAT,formatFile,Strings.UTF8);
        ChunkAccumHandler handler = new ChunkAccumHandler();
        GeneTagChunkParser parser = new GeneTagChunkParser(formatFile,handler);
        parser.parseString(SENT);

        ChunkingImpl expectedChunking0 =  new ChunkingImpl(S0);
        expectedChunking0.add(ChunkFactory.createChunk(16,37,GENE));
        expectedChunking0.add(ChunkFactory.createChunk(42,56,GENE));

        ChunkingImpl expectedChunking1 =  new ChunkingImpl(S1);

        ChunkingImpl expectedChunking2 =  new ChunkingImpl(S2);
        expectedChunking2.add(ChunkFactory.createChunk(41,59,GENE));

        assertChunking(expectedChunking0,handler.mChunkingList,0);
        assertChunking(expectedChunking1,handler.mChunkingList,1);
        assertChunking(expectedChunking2,handler.mChunkingList,2);
    }

    void assertChunking(Chunking expected, List chunkList, int index) {
        Chunking found = (Chunking) chunkList.get(index);
        assertEquals(expected.charSequence().toString(),
                     found.charSequence().toString());
        assertEquals(expected.chunkSet(),
                     found.chunkSet());
    }

    static class ChunkAccumHandler implements ChunkHandler {
        ArrayList mChunkingList = new ArrayList();
        public void handle(Chunking chunking) {
            mChunkingList.add(chunking);
        }
    }
}