package com.aliasi.test.unit.dict;

import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.test.unit.BaseTestCase;

public class ExactDictionaryChunkerTest extends BaseTestCase {

    TokenizerFactory TOKENIZER_FACTORY
        = new IndoEuropeanTokenizerFactory();

    public void testEmptyDictionary() {
	MapDictionary dictionary = new MapDictionary();
        ExactDictionaryChunker caseInsensitiveChunker
            = new ExactDictionaryChunker(dictionary,
                                         TOKENIZER_FACTORY,
                                         true,   // find all
                                         false); // not case sensitive
	caseInsensitiveChunker.chunk("John ran");
    }


    public void testCaseSensitivity() {
        MapDictionary dictionary = new MapDictionary();
        dictionary.addEntry(new DictionaryEntry("50 Cent","PERSON",1.0));
        dictionary.addEntry(new DictionaryEntry("xyz120 DVD Player","DB_ID_1232",1.0));

        String text = "50 Cent is worth more than 50 cent.";
        //             012345678901234567890123456789012345
        //             0         1         2         3

        Chunk capChunk = ChunkFactory.createChunk(0,7,"PERSON",1.0);
        Chunk lowChunk = ChunkFactory.createChunk(27,34,"PERSON",1.0);

        ExactDictionaryChunker caseInsensitiveChunker
            = new ExactDictionaryChunker(dictionary,
                                         TOKENIZER_FACTORY,
                                         true,   // find all
                                         false); // not case sensitive

        assertChunking(caseInsensitiveChunker,text,
                       new Chunk[] { lowChunk, capChunk });

        ExactDictionaryChunker caseSensitiveChunker
            = new ExactDictionaryChunker(dictionary,
                                         TOKENIZER_FACTORY,
                                         true,   // find all
                                         true); // is case sensitive

        assertChunking(caseSensitiveChunker,text,
                       new Chunk[] { capChunk });


    }

    public void testOverlapsCase() {
        MapDictionary dictionary = new MapDictionary();
        dictionary.addEntry(new DictionaryEntry("john smith","PER",7.0));
        dictionary.addEntry(new DictionaryEntry("smith and barney","ORG",3.0));
        dictionary.addEntry(new DictionaryEntry("smith","LOC",2.0));
        dictionary.addEntry(new DictionaryEntry("smith","PER",5.0));

        Chunk chunk_0_10_PER = ChunkFactory.createChunk(0,10,"PER",7.0);
        Chunk chunk_5_10_PER = ChunkFactory.createChunk(5,10,"PER",5.0);
        Chunk chunk_5_10_LOC = ChunkFactory.createChunk(5,10,"LOC",2.0);
        Chunk chunk_5_21_ORG = ChunkFactory.createChunk(5,21,"ORG",3.0);


        Chunk[] allChunks = new Chunk[] {
            chunk_0_10_PER,
            chunk_5_10_PER,
            chunk_5_10_LOC,
            chunk_5_21_ORG
        };

        Chunk[] casedChunks = new Chunk[] {
            chunk_5_10_PER,
            chunk_5_10_LOC,
        };

        Chunk[] singleChunks = new Chunk[] {
            chunk_0_10_PER
        };

        Chunk[] singleCaseChunks = new Chunk[] {
            chunk_5_10_PER
        };


        ExactDictionaryChunker chunker
            = new ExactDictionaryChunker(dictionary,TOKENIZER_FACTORY,
                                         true,true);
        assertChunking(chunker,"john smith and barney",allChunks);
        assertChunking(chunker,"JohN smith AND Barney",casedChunks);

        chunker
            = new ExactDictionaryChunker(dictionary,TOKENIZER_FACTORY,
                                         false,true);
        assertChunking(chunker,"john smith and barney",singleChunks);
        assertChunking(chunker,"JohN smith AND Barney",singleCaseChunks);

        chunker
            = new ExactDictionaryChunker(dictionary,TOKENIZER_FACTORY,
                                         true,false);
        assertChunking(chunker,"john smith and barney",allChunks);
        assertChunking(chunker,"JohN smith AND Barney",allChunks);

        chunker
            = new ExactDictionaryChunker(dictionary,TOKENIZER_FACTORY,
                                         false,false);
        assertChunking(chunker,"john smith and barney",singleChunks);
        assertChunking(chunker,"JohN smith AND Barney",singleChunks);
    }

    public void testBoundaries() {
        MapDictionary dictionary = new MapDictionary();
        dictionary.addEntry(new DictionaryEntry("john smith","PER",7.0));

        ExactDictionaryChunker chunker
            = new ExactDictionaryChunker(dictionary,TOKENIZER_FACTORY,
                                         true,true);

        Chunk[] noChunks = new Chunk[0];
        assertChunking(chunker,"john",noChunks);
        assertChunking(chunker,"smith john",noChunks);
        assertChunking(chunker,"john smith",
                       new Chunk[] { ChunkFactory.createChunk(0,10,"PER",7.0) });
        assertChunking(chunker,"john smith smith",
                       new Chunk[] { ChunkFactory.createChunk(0,10,"PER",7.0) });
        assertChunking(chunker,"john smith frank",
                       new Chunk[] { ChunkFactory.createChunk(0,10,"PER",7.0) });
        assertChunking(chunker,"then john smith",
                       new Chunk[] { ChunkFactory.createChunk(5,15,"PER",7.0) });
        assertChunking(chunker,"john john smith",
                       new Chunk[] { ChunkFactory.createChunk(5,15,"PER",7.0) });
        assertChunking(chunker,"john john smith smith",
                       new Chunk[] { ChunkFactory.createChunk(5,15,"PER",7.0) });
    }

    void assertChunking(ExactDictionaryChunker chunker, String in,
                        Chunk[] chunks) {
        Chunking chunking = chunker.chunk(in);

        ChunkingImpl chunkingExpected = new ChunkingImpl(in);
        for (int i = 0; i < chunks.length; ++i)
            chunkingExpected.add(chunks[i]);

        assertEquals(chunkingExpected,chunking);
    }

}
