/*
 * LingPipe v. 2.0
 * Copyright (C) 2003-5 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://www.alias-i.com/lingpipe/licenseV1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.test.unit.sentences;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;

import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import java.util.Set;
import java.util.LinkedHashSet;

import com.aliasi.test.unit.BaseTestCase;


public class SentenceChunkerTest extends BaseTestCase {

    static final TokenizerFactory tf = new IndoEuropeanTokenizerFactory();
    static final SentenceModel sm = new IndoEuropeanSentenceModel();

    public void testSentenceChunks() {
    SentenceChunker sentenceChunker  = new SentenceChunker(tf,sm);

    // simple test
    String[] sents = new String[] {
        "John ran.",
        "He saw Susan."
    };
    String[] whitespaces = new String[] {
        "  ",
        " ",
        ""
    };
    assertChunks(sentenceChunker,sents,whitespaces);

    // single sentence
    sents = new String[] {
        "His temperature was 99.5 and rising."
    };
    whitespaces = new String[] {
        " ",
        ""
    };
    assertChunks(sentenceChunker,sents,whitespaces);

    // no sentences
    sents = new String[] {
    };
    whitespaces = new String[] {
        ""
    };
    assertChunks(sentenceChunker,sents,whitespaces);

    // sample medline data
    sents = new String[] {
        "Transcription of the nirIX gene cluster itself was controlled by NNR, a member of the family of FNR-like transcriptional activators.",
        "The NirI sequence corresponds to that of a membrane-bound protein with six transmembrane helices, a large periplasmic domain and cysteine-rich cytoplasmic domains that resemble the binding sites of [4Fe-4S] clusters in many ferredoxin-like proteins.",
        "An NNR binding sequence is located in the middle of the intergenic region between the nirI and nirS genes with its centre located at position -41.5 relative to the transcription start sites of both genes.",
        "In eight families we identified six novel MLH1 and two novel MSH2 mutations comprising one frame shift mutation (c.1420 del C), two missense mutations (L622H and R687W), two splice site mutations (c.1990-1 G>A and c.453+2 T>C and one nonsense mutation (K329X) in the MLH1 gene as well as two frame shift mutations (c.1979-1980 del AT and c.1704-1705 del AG) in the MSH2 gene."
    };

    whitespaces = new String[] {
        " ",
        "  ",
        "  ",
        "  ",
        ""
    };
    assertChunks(sentenceChunker,sents,whitespaces);

    }

    void assertChunks(SentenceChunker sentenceChunker, 
              String[] sents, String[] whitespaces) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < sents.length; ++i) {
        sb.append(whitespaces[i]);
        sb.append(sents[i]);
    }
    sb.append(whitespaces[sents.length]);
    String input = sb.toString();
    char[] cs = input.toCharArray();
    
    LinkedHashSet expectedChunks = new LinkedHashSet();
    int end = 0;
    int start = 0;
    for (int i = 0; i < sents.length; ++i) {
        start = end + whitespaces[i].length();
        end = start + sents[i].length();
        Chunk chunk 
        = ChunkFactory.createChunk(start,start+sents[i].length(),
                       SentenceChunker
                       .SENTENCE_CHUNK_TYPE);
        expectedChunks.add(chunk);
    }

    Set foundChunks 
        = sentenceChunker.chunk(cs,0,input.length()).chunkSet();
    assertEquals(expectedChunks,foundChunks);
    }


}
