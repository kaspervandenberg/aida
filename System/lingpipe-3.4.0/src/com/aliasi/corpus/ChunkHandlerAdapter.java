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

package com.aliasi.corpus;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Strings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;

/**
 * A <code>ChunkHandlerAdapter</code> converts a BIO-coded tag handler
 * to a chunk handler.  The adapter handles chunkings by tokenizing
 * their character sequences and then using their chunk sets to
 * produce tags in the begin-in-out (BIO) tagging scheme.  For an
 * adapter from a chunk handler to a BIO-coded tag handler, see the
 * sister class {@link ChunkTagHandlerAdapter}.
 *
 * <P>The BIO tagging scheme marks each token as either beginning a
 * chunk (B), contininuing a chunk (I), or not in a chunk (O).  For
 * example, consider the following string (with character indices
 * annotated below it):
 *
 * <blockquote><pre>
 * John J. Smith lives in Washington.
 * 0123456789012345678901234567890123
 * 0         1         2         3
 * </pre></blockquote>
 *
 * with chunks of type <code>PERSON</code> spanning from character 0
 * (inclusive) to 13 (exclusive) and a chunk of type
 * <code>LOCATION</code> spanning from 23 to 33.  With the standard
 * <code>tokenizerIndoEuropeanTokenizerFactory</code> providing
 * tokenization, the tokens, whitespaces and their associated BIO tags
 * are:
 *
 * <blockquote><table border='1' cellpadding='5'>
 * <tr><td><i>Index</i></td>
 <td><i>Whitespace</i></td>
 *     <td><i>Token</i></td>
 *     <td><i>Tag</i></td></tr>
 * <tr><td>0</td> <td>&quot;&quot;</td> <td>John</td>  <td><code>B-PERSON</code></td></tr>
 * <tr><td>1</td> <td>&quot;&nbsp;&quot;</td> <td>J</td>  <td><code>I-PERSON</code></td></tr>
 * <tr><td>2</td> <td>&quot;&quot;</td> <td>.</td>  <td><code>I-PERSON</code></td></tr>
 * <tr><td>3</td> <td>&quot;&nbsp;&quot;</td> <td>Smith</td>  <td><code>I-PERSON</code></td></tr>
 * <tr><td>4</td> <td>&quot;&nbsp;&quot;</td> <td>lives</td>  <td><code>O</code></td></tr>
 * <tr><td>5</td> <td>&quot;&nbsp;&quot;</td> <td>in</td>  <td><code>O</code></td></tr>
 * <tr><td>6</td> <td>&quot;&nbsp;&quot;</td> <td>Washington</td>  <td><code>B-PERSON</code></td></tr>
 * <tr><td>7</td> <td>&quot;&quot;</td> <td>.</td>  <td><code>O</code></td></tr>
 * <tr><td>8</td> <td>&quot;&quot;</td> <td colspan='2'><i>n/a</i></td></tr>
 * </table></blockquote>
 *
 * As usual, the whitespaces with the same index as a token occur
 * before it.  Thus the two periods in the input do not have spaces
 * before them, but all other tokens do.  Further note there is one
 * additional whitespace following the last tag.  The tag
 * <code>B-PERSON</code> is assigned to the first token of the chunk,
 * with the subsequent tokens being assigned <code>I-PERSON</code>.
 * The tag &quot;out&quot; tag <code>O</code> is assigned to each
 * token that is not a substring of a chunk, including the final
 * period.
 *
 * <P>In order for this adaptation to be faithful, the chunks must be
 * consistent with the tokenizer.  Specifically, each chunk must start
 * on the first character of a token and end on the last character of
 * a token.  If the person chunk ended at character 14 (exclusive) to
 * include the space after the token <code>Smith</code>, it would no
 * longer be consistent with the tokenizer.  In the constructor or
 * using the flag setting method {@link
 * #setValidateTokenizer(boolean)}, the adapter may be configured to
 * raise exceptions if called upon to handle a chunking inconsistent
 * with its tokenizer.  The static method {@link
 * #consistentTokens(String[],String[],TokenizerFactory)} is also
 * provided to test if a given set of tokens and whitespaces is
 * consistent with a tokenizer factory.
 *
 * @author  Bob Carpenter
 * @version 3.1.2
 * @since   LingPipe2.1
 */
public class ChunkHandlerAdapter implements ChunkHandler {

    private final TokenizerFactory mTokenizerFactory;
    private TagHandler mTagHandler;
    private boolean mValidateTokenizer;

    /**
     * Create a chunk handler based on the specified tag handler and
     * tokenizer factory.  The tag handler may be reset later using
     * {@link #setTagHandler(TagHandler)}.  The chunks handled
     * by this handler will be converted to BIO-encoded tag sequences
     *
     * @param tagHandler Tag handler.
     * @param tokenizerFactory Tokenizer factory.
     * @param validateTokenizer Whether or not to validate tokenizer.
     */
    public ChunkHandlerAdapter(TagHandler tagHandler,
                               TokenizerFactory tokenizerFactory,
                               boolean validateTokenizer) {
        this(tokenizerFactory,validateTokenizer);
        mTagHandler = tagHandler;
    }

    /**
     * Construct a chunk handler based on the specified tokenizer
     * factory and an initially null tag handler.  The tag handler may
     * be reset later using {@link #setTagHandler(TagHandler)}.
     *
     * @param tokenizerFactory Tokenizer factory.
     * @param validateTokenizer Whether or not to validate tokenizer.
     */
    public ChunkHandlerAdapter(TokenizerFactory tokenizerFactory,
                               boolean validateTokenizer) {
        mTokenizerFactory = tokenizerFactory;
        mValidateTokenizer = validateTokenizer;
    }

    /**
     * Set the tag handler to the specified value.
     *
     * @param tagHandler New tag handler for this class.
     */
    public void setTagHandler(TagHandler tagHandler) {
        mTagHandler = tagHandler;
    }

    /**
     * Sets the tokenizer validation status to the specified value.
     * If the value is set to <code>true</code>, then every chunking
     * is tested for whether or not it is consistent with the
     * specified tokenizer for this handler.
     *
     * @param validateTokenizer Whether or not to validate tokenizer.
     */
    public void setValidateTokenizer(boolean validateTokenizer) {
        mValidateTokenizer = validateTokenizer;
    }

    /**
     * Handle the specified chunking by converting it to a tagging
     * using the BIO scheme and contained tokenizer, then delegating
     * to the contained tag handler.
     *
     * @param chunking Chunking to handle.
     * @throws IllegalArgumentException If tokenizer consistency is
     * being validated and the tokenization is not consistent with the
     * specified chunking.
     */
    public void handle(Chunking chunking) {
        CharSequence cSeq = chunking.charSequence();
        char[] cs = Strings.toCharArray(cSeq);

        Set chunkSet = chunking.chunkSet();
        Chunk[] chunks = new Chunk[chunkSet.size()];
        chunkSet.toArray(chunks);
        Arrays.sort(chunks,Chunk.TEXT_ORDER_COMPARATOR);

        ArrayList tokenList = new ArrayList();
        ArrayList whiteList = new ArrayList();
        ArrayList tagList = new ArrayList();
        int pos = 0;
        for (Chunk nextChunk : chunks) {
            String type = nextChunk.type();
            int start = nextChunk.start();
            int end = nextChunk.end();
            outTag(cs,pos,start,tokenList,whiteList,tagList,mTokenizerFactory);
            chunkTag(cs,start,end,type,tokenList,whiteList,tagList,mTokenizerFactory);
            pos = end;
        }
        outTag(cs,pos,cSeq.length(),tokenList,whiteList,tagList,mTokenizerFactory);
        String[] toks = new String[tokenList.size()];
        tokenList.toArray(toks);
        String[] whites = new String[whiteList.size()];
        whiteList.toArray(whites);
        String[] tags = new String[tagList.size()];
        tagList.toArray(tags);
        if (mValidateTokenizer
            && !consistentTokens(toks,whites,mTokenizerFactory)) {
            String msg = "Tokens not consistent with tokenizer factory."
                + " Tokens=" + Arrays.asList(toks)
                + " Tokenization=" + tokenization(toks,whites)
                + " Factory class=" + mTokenizerFactory.getClass();
            throw new IllegalArgumentException(msg);
        }
        mTagHandler.handle(toks,whites,tags);
    }

    /**
     * Returns the array of tags for the specified chunking, relative
     * to the specified tokenizer factory.
     *
     * @param chunking Chunking to convert to tags.
     * @param factory Tokenizer factory for token generation.
     */
    public static String[] toTags(Chunking chunking,
                                  TokenizerFactory factory) {
        CharSequence cSeq = chunking.charSequence();
        char[] cs = Strings.toCharArray(cSeq);

        Set chunkSet = chunking.chunkSet();
        Chunk[] chunks = new Chunk[chunkSet.size()];
        chunkSet.toArray(chunks);
        Arrays.sort(chunks,Chunk.TEXT_ORDER_COMPARATOR);

        ArrayList tokenList = new ArrayList();
        ArrayList whiteList = new ArrayList();
        ArrayList tagList = new ArrayList();
        int pos = 0;
        for (Chunk nextChunk : chunks) {
            String type = nextChunk.type();
            int start = nextChunk.start();
            int end = nextChunk.end();
            outTag(cs,pos,start,tokenList,whiteList,tagList,factory);
            chunkTag(cs,start,end,type,tokenList,whiteList,tagList,factory);
            pos = end;
        }
        outTag(cs,pos,cSeq.length(),tokenList,whiteList,tagList,factory);

        String[] tags = new String[tagList.size()];
        tagList.toArray(tags);
        return tags;
    }

    /**
     * Returns <code>true</code> if the specified tokens and
     * whitespaces are consistent with the specified tokenizer
     * factory.  A tokenizer is consistent with the specified
     * tokens and whitespaces if running the tokenizer over
     * the concatenation of the tokens and whitespaces produces
     * the same tokens and whitespaces.
     *
     * @param toks Tokens to check.
     * @param whitespaces Whitespaces to check.
     * @param tokenizerFactory Factory to create tokenizers.
     * @return <code>true</code> if the tokenizer is consistent with
     * the tokens and whitespaces.
     */
    public static boolean consistentTokens(String[] toks,
                                           String[] whitespaces,
                                           TokenizerFactory tokenizerFactory) {
        if (toks.length+1 != whitespaces.length) return false;
        char[] cs = getChars(toks,whitespaces);
        Tokenizer tokenizer = tokenizerFactory.tokenizer(cs,0,cs.length);
        String nextWhitespace = tokenizer.nextWhitespace();
        if (!whitespaces[0].equals(nextWhitespace)) {
            return false;
        }
        for (int i = 0; i < toks.length; ++i) {
            String token = tokenizer.nextToken();
            if (token == null) {
                return false;
            }
            if (!toks[i].equals(token)) {
                return false;
            }
            nextWhitespace = tokenizer.nextWhitespace();
            if (!whitespaces[i+1].equals(nextWhitespace)) {
                return false;
            }
        }
        return true;
    }

    static void outTag(char[] cs, int start, int end,
                       ArrayList tokenList, ArrayList whiteList, ArrayList tagList,
                       TokenizerFactory factory) {
        Tokenizer tokenizer = factory.tokenizer(cs,start,end-start);
        whiteList.add(tokenizer.nextWhitespace());
        String nextToken;
        while ((nextToken = tokenizer.nextToken()) != null) {
            tokenList.add(nextToken);
            tagList.add(ChunkTagHandlerAdapter.OUT_TAG);
            whiteList.add(tokenizer.nextWhitespace());
        }

    }

    static void chunkTag(char[] cs, int start, int end, String type,
                         ArrayList tokenList, ArrayList whiteList, ArrayList tagList,
                         TokenizerFactory factory) {
        Tokenizer tokenizer = factory.tokenizer(cs,start,end-start);
        String firstToken = tokenizer.nextToken();
        tokenList.add(firstToken);
        tagList.add(ChunkTagHandlerAdapter.BEGIN_TAG_PREFIX + type);
        while (true) {
            String nextWhitespace = tokenizer.nextWhitespace();
            String nextToken = tokenizer.nextToken();
            if (nextToken == null) break;
            tokenList.add(nextToken);
            whiteList.add(nextWhitespace);
            tagList.add(ChunkTagHandlerAdapter.IN_TAG_PREFIX + type);
        }
    }


    List tokenization(String[] toks, String[] whitespaces) {
        ArrayList tokList = new ArrayList();
        ArrayList whiteList = new ArrayList();
        char[] cs = getChars(toks,whitespaces);
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
        tokenizer.tokenize(tokList,whiteList);
        return tokList;
    }

    static char[] getChars(String[] toks, String[] whitespaces) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < toks.length; ++i) {
            sb.append(whitespaces[i]);
            sb.append(toks[i]);
        }
        sb.append(whitespaces[whitespaces.length-1]);
        return Strings.toCharArray(sb);
    }

}
