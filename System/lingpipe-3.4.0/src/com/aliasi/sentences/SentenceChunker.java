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

package com.aliasi.sentences;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Strings;

import java.util.ArrayList;

/**
 * The <code>SentenceChunker</code> class uses a
 * <code>SentenceModel</code> to implement sentence detection through
 * the <code>chunk.Chunker</code> interface.  A sentence chunker is
 * constructed from a tokenizer factory and a sentence model.  The
 * tokenizer factory creates tokens that it sends to the sentence
 * model.  The types of the chunks produced are given by the
 * constant {@link #SENTENCE_CHUNK_TYPE}.
 *
 * <P>The tokens and whitespaces returned by the tokenizer are
 * concatenated to form the underlying text slice of the chunks
 * returned by the chunker.  Thus a tokenizer like the stop list
 * tokenizer or Porter stemmer tokenizer will create a character slice
 * that does not match the input.  A whitespace-normalizing tokenizer
 * filter can be used, for example, to produce normalized text for the
 * basis of the chunks.
 * 
 * @author  Mitzi Morris
 * @version 2.1
 * @since   LingPipe2.1
 */
public class SentenceChunker implements Chunker {

    private final TokenizerFactory mTokenizerFactory;
    private final SentenceModel mSentenceModel;

    /**
     * Construct a sentence chunker from the specified tokenizer
     * factory and sentence model.
     *
     * @param tf Tokenizer factory for chunker.
     * @param sm Sentence model for chunker.
     */
    public SentenceChunker(TokenizerFactory tf, SentenceModel sm) {
	mTokenizerFactory = tf;
	mSentenceModel = sm;
    }

    /**
     * Return the chunking derived from the underlying sentence model
     * over the tokenization of the specified character slice.
     * Iterating over the returned set is guaranteed to return the
     * sentence chunks in their original textual order.  
     *
     * <P><i>Warning:</i> As described in the class documentation
     * above, a tokenizer factory that produces tokenizers that do not
     * reproduce the original sequence may cause the underlying
     * character slice for the chunks to differ from the slice
     * provided as an argument.
     *
     * @param cSeq Character sequence underlying the slice.
     * @return The sentence chunking of the specified character
     * sequence.
     */
    public Chunking chunk(CharSequence cSeq) {
	char[] cs = Strings.toCharArray(cSeq);
	return chunk(cs,0,cs.length);
    }
    
    /**
     * Return the chunking derived from the underlying sentence model
     * over the tokenization of the specified character slice.  See
     * {@link #chunk(CharSequence)} for more information.
     *
     * @param cs Underlying character sequence.
     * @param start Index of first character in slice.
     * @param end Index of one past the last character in the slice.
     * @return The sentence chunking of the specified character slice.
     */
    public Chunking chunk(char[] cs, int start, int end) {
	ArrayList tokenList = new ArrayList();
	ArrayList whiteList = new ArrayList();

	Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,start,end-start);
	tokenizer.tokenize(tokenList,whiteList);

	ChunkingImpl chunking = new ChunkingImpl(cs,start,end);

	if (tokenList.size() == 0) return chunking;

	String[] tokens = new String[tokenList.size()];
	String[] whites = new String[whiteList.size()];
	tokenList.toArray(tokens);
	whiteList.toArray(whites);

	int[] tokenStarts = new int[tokens.length];
	int[] tokenEnds = new int[tokens.length];
    
	int pos = whites[0].length();
	for (int i = 0; i < tokens.length; ++i) {
	    tokenStarts[i] = pos;
	    pos += tokens[i].length();
	    tokenEnds[i] = pos;
	    pos += whites[i+1].length();
	}

	int[] sentenceBoundaries = mSentenceModel.boundaryIndices(tokens,whites);
	if (sentenceBoundaries.length < 1) return chunking;

	int nextSentStart = tokenStarts[0];
	for (int i = 0; i < sentenceBoundaries.length; ++i) {
	    int sentenceStart = nextSentStart;
	    int endTokIdx = sentenceBoundaries[i];
	    int sentenceEnd = tokenEnds[endTokIdx];
            Chunk chunk 
		= ChunkFactory.createChunk(sentenceStart,sentenceEnd,
					   SENTENCE_CHUNK_TYPE);
	    chunking.add(chunk);
            nextSentStart = sentenceEnd + whites[endTokIdx+1].length();
	}
	return chunking;
    }

    /**
     * The type assigned to sentence chunks, namely
     * <code>&quot;S&quot;</code>.
     */
    public static final String SENTENCE_CHUNK_TYPE = "S";

}


