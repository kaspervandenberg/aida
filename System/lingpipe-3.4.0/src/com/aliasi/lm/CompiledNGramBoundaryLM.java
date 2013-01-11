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

package com.aliasi.lm;

import java.io.ObjectInput;
import java.io.IOException;

import com.aliasi.util.Exceptions;

/**
 * A <code>CompiledNGramBoundaryLM</code> is constructed by reading
 * the serialized form of an instance of {@link NGramBoundaryLM}.
 *
 * <P>The serialization format is the boundary character followed by
 * the serialization of an n-gram process language model.
 */
public class CompiledNGramBoundaryLM
    implements LanguageModel.Sequence,
               LanguageModel.Conditional {

    private final char mBoundaryChar;
    private final char[] mBoundaryArray;
    private final CompiledNGramProcessLM mProcessLM;

    /**
     * Construct a compiled n-gram boundary langauge model from
     * the specified object input.  See the class documentation for
     * data format.
     *
     * @param objIn Object input from which to read the constructed
     * class.
     * @throws IOException If there is an error while reading the
     * data.
     */
    CompiledNGramBoundaryLM(ObjectInput objIn)
        throws IOException {
        mBoundaryChar = objIn.readChar();
        mBoundaryArray = new char[] { mBoundaryChar };
        try {
            mProcessLM = (CompiledNGramProcessLM) objIn.readObject();
        } catch (ClassNotFoundException e) {
            throw Exceptions.toIO("CompiledNGramBoundarLM(ObjectOutput)",e);
        }
    }



    /**
     * Returns the characters that have been observed for this
     * language model, including the special boundary character.
     *
     * @return The observed characters for this langauge model.
     */
    public char[] observedCharacters() {
        return mProcessLM.observedCharacters();
    }

    // ugly cut-and-paste from NGramBoundaryLM
    public double log2ConditionalEstimate(CharSequence cs) {
        if (cs.length() < 1) {
            String msg = "Conditional estimates require at least one character.";
            throw new IllegalArgumentException(msg);
        }
        char[] csBounded = NGramBoundaryLM.addBoundaries(cs,mBoundaryChar);
        return mProcessLM.log2ConditionalEstimate(csBounded,0,csBounded.length-1);
    }

    public double log2ConditionalEstimate(char[] cs, int start, int end) {
        if (end <= start) {
            String msg = "Conditional estimates require at least one character.";
            throw new IllegalArgumentException(msg);
        }
        char[] csBounded = NGramBoundaryLM.addBoundaries(cs,start,end,mBoundaryChar);
        return mProcessLM.log2ConditionalEstimate(csBounded,0,csBounded.length-1);
    }

    public double log2Estimate(CharSequence cs) {
        char[] csBounded = NGramBoundaryLM.addBoundaries(cs,mBoundaryChar);
        return mProcessLM.log2Estimate(csBounded,0,csBounded.length)
            - mProcessLM.log2Estimate(mBoundaryArray,0,1);
    }

    public double log2Estimate(char[] cs, int start, int end) {
        char[] csBounded = NGramBoundaryLM.addBoundaries(cs,start,end,mBoundaryChar);
        return mProcessLM.log2Estimate(csBounded,0,csBounded.length)
            - mProcessLM.log2Estimate(mBoundaryArray,0,1);
    }


}
