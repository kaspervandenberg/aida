package com.aliasi.test.unit.corpus;

import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.corpus.ChunkHandlerAdapter;

import com.aliasi.test.unit.BaseTestCase;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import java.util.ArrayList;
import java.util.List;

public class ChunkHandlerAdapterTest extends BaseTestCase {

    static TokenizerFactory TOK_FACTORY = new IndoEuropeanTokenizerFactory();

    public void testToArray() {
        String text = "John Smith is in Washington.";
        //             0123456789012345678901234567
        //             0         1         2
        ChunkingImpl chunking = new ChunkingImpl(text);
        chunking.add(ChunkFactory.createChunk(0,10,"PER"));
        chunking.add(ChunkFactory.createChunk(17,27,"LOC"));

        String[] tags = ChunkHandlerAdapter.toTags(chunking,
                                                   TOK_FACTORY);
        String[] expectedTags = new String[] {
            "B-PER", "I-PER", "O", "O", "B-LOC", "O"
        };
        assertEqualsArray(expectedTags,tags);

        ChunkingImpl chunkingReOrdered = new ChunkingImpl(text);
        chunkingReOrdered.add(ChunkFactory.createChunk(17,27,"LOC"));
        chunkingReOrdered.add(ChunkFactory.createChunk(0,10,"PER"));
        String[] tagsRe = ChunkHandlerAdapter.toTags(chunking,
                                                   TOK_FACTORY);
        assertEqualsArray(expectedTags,tagsRe);


        ChunkingImpl chunking2 = new ChunkingImpl("");
        String[] tags2 = ChunkHandlerAdapter.toTags(chunking2,
                                                    TOK_FACTORY);
        assertEqualsArray(new String[0],tags2);

        ChunkingImpl chunking3 = new ChunkingImpl("John");
        String[] tags3 = ChunkHandlerAdapter.toTags(chunking3,
                                                    TOK_FACTORY);
        assertEqualsArray(new String[] { "O" }, tags3);

        chunking3.add(ChunkFactory.createChunk(0,4,"PER"));
        String[] tags4 = ChunkHandlerAdapter.toTags(chunking3,
                                                    TOK_FACTORY);
        assertEqualsArray(new String[] { "B-PER" }, tags4);
    }

    public void testConsistentTokens() {
        assertRoundTrip("");
        assertRoundTrip(" ");
        assertRoundTrip("John J. Smith is in Washington.");
        assertRoundTrip("Foo\nBar.");

        assertConsistent(new String[] { }, new String[] { "" });
        assertConsistent(new String[] {
            "abc", "de", "fgh", "i"
        },
                         new String[] {
                             "", " ", " ", " ", ""
                         });
        assertInconsistent(new String[] { "ab", "cd" },
                           new String[] { " ", "", " "});
        assertInconsistent(new String[] { "a", "b" },
                           new String[] { " ", " " });
        assertInconsistent(new String[] { "ab", "cde", "fg" },
                           new String[] { "", " ", "", " "});
    }

    public void testHandler() {
        CollectingTagHandler tagHandler = new CollectingTagHandler();
        ChunkHandlerAdapter chunkHandler
            = new ChunkHandlerAdapter(tagHandler,TOK_FACTORY,true);

        List tokenList = new ArrayList();
        List whiteList = new ArrayList();
        List tagList = new ArrayList();

        ChunkingImpl chunking1 = new ChunkingImpl("abc");
        chunkHandler.handle(chunking1);
        tokenList.add(new String[] { "abc" });
        whiteList.add(new String[] { "", "" });
        tagList.add(new String[] { "O" });
        assertTagging(tagHandler,tokenList,whiteList,tagList);

        ChunkingImpl chunking2 = new ChunkingImpl("John runs.");
        chunking2.add(ChunkFactory.createChunk(0,4,"PER"));
        chunkHandler.handle(chunking2);
        tokenList.add(new String[] { "John", "runs", "." });
        whiteList.add(new String[] { "", " ", "", "" });
        tagList.add(new String[] { "B-PER", "O", "O" });
        assertTagging(tagHandler,tokenList,whiteList,tagList);

        ChunkingImpl chunking3 = new ChunkingImpl("John Johnson");
        chunking3.add(ChunkFactory.createChunk(0,4,"PER"));
        chunking3.add(ChunkFactory.createChunk(5,12,"PER"));
        chunkHandler.handle(chunking3);
        tokenList.add(new String[] { "John", "Johnson" });
        whiteList.add(new String[] { "", " ", "" });
        tagList.add(new String[] { "B-PER", "B-PER" });
        assertTagging(tagHandler,tokenList,whiteList,tagList);

        ChunkingImpl chunking4 = new ChunkingImpl("John Johnson ran.");
        chunking4.add(ChunkFactory.createChunk(0,12,"PER"));
        chunkHandler.handle(chunking4);
        tokenList.add(new String[] { "John", "Johnson", "ran", "." });
        whiteList.add(new String[] { "", " ", " ", "", "" });
        tagList.add(new String[] { "B-PER", "I-PER", "O", "O" });
        assertTagging(tagHandler,tokenList,whiteList,tagList);

        ChunkingImpl chunking5 = new ChunkingImpl("John Johnson County");
        chunking5.add(ChunkFactory.createChunk(0,12,"PER"));
        chunking5.add(ChunkFactory.createChunk(13,19,"LOC"));
        chunkHandler.handle(chunking5);
        tokenList.add(new String[] { "John", "Johnson", "County" });
        whiteList.add(new String[] { "", " ", " ", "" });
        tagList.add(new String[] { "B-PER", "I-PER", "B-LOC" });
        assertTagging(tagHandler,tokenList,whiteList,tagList);

        ChunkingImpl chunking6 = new ChunkingImpl("Mr. John Johnson");
        chunking6.add(ChunkFactory.createChunk(4,16,"PER"));
        chunkHandler.handle(chunking6);
        tokenList.add(new String[] { "Mr", ".", "John", "Johnson" });
        whiteList.add(new String[] { "", "", " ", " ", "" });
        tagList.add(new String[] { "O", "O", "B-PER", "I-PER" });
        assertTagging(tagHandler,tokenList,whiteList,tagList);

        ChunkingImpl chunkingBad1 = new ChunkingImpl("Mr. John Johnson");
        chunkingBad1.add(ChunkFactory.createChunk(0,1,"PER"));
        try {
            chunkHandler.handle(chunkingBad1);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        ChunkingImpl chunkingBad2 = new ChunkingImpl("Mr. John Johnson");
        chunkingBad2.add(ChunkFactory.createChunk(1,2,"PER"));
        try {
            chunkHandler.handle(chunkingBad2);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        chunkHandler.setValidateTokenizer(false);
        chunkHandler.handle(chunkingBad2);

        chunkHandler.setValidateTokenizer(true);
        try {
            chunkHandler.handle(chunkingBad2);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }
    }

    void assertTagging(CollectingTagHandler tagHandler,
                       List tokenList, List whiteList, List tagList) {
        tagHandler.assertTokenList(tokenList);
        tagHandler.assertWhitespaceList(whiteList);
        tagHandler.assertTagList(tagList);
    }


    void assertRoundTrip(String in) {
        ArrayList tokList = new ArrayList();
        ArrayList whiteList = new ArrayList();
        char[] cs = in.toCharArray();
        TOK_FACTORY.tokenizer(cs,0,cs.length).tokenize(tokList,whiteList);
        String[] toks
            = (String[]) tokList.toArray(new String[tokList.size()]);
        String[] whites
            = (String[]) whiteList.toArray(new String[whiteList.size()]);
        assertConsistent(toks,whites);
    }

    void assertConsistent(String[] toks, String[] whites) {
        assertTrue(ChunkHandlerAdapter
                   .consistentTokens(toks,whites,TOK_FACTORY));
    }

    void assertInconsistent(String[] toks, String[] whites) {
        assertFalse(ChunkHandlerAdapter
                    .consistentTokens(toks,whites,TOK_FACTORY));
    }



}
