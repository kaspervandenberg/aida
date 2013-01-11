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

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.xml.SimpleElementHandler;
import com.aliasi.xml.TextContentFilter;

import com.aliasi.util.Collections;


import java.util.ArrayList;

import org.xml.sax.SAXException;

/**
 * A <code>SentenceAnnotateFilter</code> applies sentence-boundary
 * annotation to the text content of the specified elements.  An
 * instance is constructed with a sentence model and a tokenizer
 * factory.  Optionally, an array of elements to annotate may be
 * provided; if no array is specified, all text content is annotated.
 *
 * <p>The element <code>sent</code> is used to wrap sentences.  If the
 * filtered element contains only whitespace, it is not annotated.
 * There will be no whitespace characters at the start or end of a
 * sentence element's text content.  All inter-sentence whitespace is
 * retained, but included between sentence elements in the filtered
 * element's content.  For instance, the input
 * <code>&lt;p&gt;&nbsp;A&nbsp;b.&nbsp;&nbsp;C&nbsp;d.&nbsp;&lt;/p&gt;</code>
 * will yield
 * <code>&lt;p&gt;&nbsp;&lt;sent&gt;A&nbsp;b.&lt;/sent&gt;&nbsp;&nbsp;&lt;sent&gt;C&nbsp;d.&lt;/sent&gt;&nbsp;&lt;/p&gt;</code>.
 * Note that the text of a sentence element starts with the first
 * character of the first token and ends with the last character of
 * the last token.  Inter-sentential whitespace winds up as text
 * content outside of the sentence.  In this case, there is a single
 * whitespace before the first sentence, two spaces between the
 * sentences, and a single space after the second sentence.
 *
 * @author  Bob Carpenter
 * @version 1.0.3
 * @since   LingPipe1.0
 */
public class SentenceAnnotateFilter extends TextContentFilter {

    /**
     * <code>true</code> if every element is annotated.
     */
    private final boolean mAnnotateAll;

    /**
     * The model used for determining sentence boundaries within text.
     */
    private final SentenceModel mSentenceModel;

    /**
     * The tokenizer factory to use for tokenizing text before
     * determining sentence boundaries.
     */
    private final TokenizerFactory mTokenizerFactory;

    /**
     * Constructs a sentence annotation filter with the specified
     * sentence model and tokenizer factory.
     *
     * @param sentenceModel Sentence model to use for boundary detection.
     * @param tokenizerFactory Factory to produce tokenizers for text.
     */
    public SentenceAnnotateFilter(SentenceModel sentenceModel,
                                  TokenizerFactory tokenizerFactory) {
        mSentenceModel = sentenceModel;
        mTokenizerFactory = tokenizerFactory;
        mAnnotateAll = true;
    }

    /**
     * Constructs a sentence annotation filter with the specified
     * sentence model and tokenizer factory, and elements whose
     * text content should be annotated.
     *
     * @param sentenceModel Sentence model to use for boundary detection.
     * @param tokenizerFactory Factory to produce tokenizers for text.
     * @param elements List of elements to be annotated.
     */
    public SentenceAnnotateFilter(SentenceModel sentenceModel,
                                  TokenizerFactory tokenizerFactory,
                                  String[] elements) {
        mSentenceModel = sentenceModel;
        mTokenizerFactory = tokenizerFactory;
        mAnnotateAll = false;
        for (int i = 0; i < elements.length; ++i)
            filterElement(elements[i]);
    }

    /**
     * Annotates characters if all characters are being annotated,
     * otherwise annotates if in an annotated element, otherwise passing
     * characters directly to contained handler.  All boundary events
     * will be passed to the contained handler.
     *
     * @param cs Character array to filter.
     * @param start First character to filter.
     * @param length Number of characters to filter.
     * @throws SAXException If there is an exception thrown by the
     * contained handler.
     */
    public void characters(char[] cs, int start, int length)
        throws SAXException {

        if (mAnnotateAll) {
            filteredCharacters(cs,start,length);
        } else {
            super.characters(cs,start,length);
        }
    }

    /**
     * Performs sentence-boundary annotation of the specified
     * characters.  Markup and text SAX events are delegated to the
     * contained handler.
     *
     * @param cs Character array to annotate.
     * @param start First character to annotate.
     * @param length Number of characters to annotate.
     * @throws SAXException If there is an exception thrown by the
     * contained handler.
     */
    public void filteredCharacters(char[] cs, int start, int length)
        throws SAXException {

        Tokenizer tokenizer
            = mTokenizerFactory.tokenizer(cs,start,length);
        ArrayList tokenList = new ArrayList();
        ArrayList whitespaceList = new ArrayList();
        tokenizer.tokenize(tokenList,whitespaceList);
        String[] whitespaces = Collections.toStringArray(whitespaceList);
        String[] tokens = Collections.toStringArray(tokenList);
        SimpleElementHandler.characters(mHandler,whitespaces[0]);
        if (tokens.length == 0) return;
        int[] stops = mSentenceModel.boundaryIndices(tokens,whitespaces);

        int nextStopIndex = 0;
        startSimpleElement(SENTENCE_ELEMENT);
        for (int i = 0; i < tokens.length; ++i) {
            SimpleElementHandler.characters(mHandler,tokens[i]);
            if (i+1 < tokens.length
                && nextStopIndex < stops.length
                && i == stops[nextStopIndex]) {

                endSimpleElement(SENTENCE_ELEMENT);
                SimpleElementHandler.characters(mHandler,whitespaces[i+1]);
                startSimpleElement(SENTENCE_ELEMENT);
                ++nextStopIndex;
            } else if (i+1 < tokens.length) {
                    SimpleElementHandler.characters(mHandler,whitespaces[i+1]);
            }
        }
        endSimpleElement(SENTENCE_ELEMENT);
        SimpleElementHandler.characters(mHandler,whitespaces[tokens.length]);
     }

    /**
     * Element used to group sentences in sentence annotation,
     * namely <code>&quot;sent&quot;</code>.
     */
    public static final String SENTENCE_ELEMENT = "sent";

}
