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

package com.aliasi.hmm;

import com.aliasi.corpus.TagHandler;

import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.util.Iterator;

/**
 * An <code>HmmEvaluator</code> provides an evaluation mechanism for
 * HMMs.  An evaluator is constructed with an HMM, the array of tags
 * used, and a limit on how far the n-best list is searched.  It
 * implements the {@link TagHandler} interface, which specifies the
 * {@link #handle(String[],String[],String[])} method.  This method
 * will receive reference taggins in the form of arrays of tokens,
 * whitespaces (which are ignored), and a reference tagging.  It then
 * runs an HMM decoder based on the specified HMM over the input to
 * produce responses in the form of a first-best response, a
 * confidence-based lattice response and an n-best iterator response.
 * At any point in the sequence of test cases, the evaluation returned
 * by {@link #evaluation()} may be inspected.
 * 
 * @author  Bob Carpenter
 * @version 2.4.1
 * @since   LingPipe2.1
 */
public class HmmEvaluator implements TagHandler {

    private final HmmDecoder mDecoder;
    private final HmmEvaluation mEvaluation;

    private String[] mFirstBest;
    private TagWordLattice mLattice;
    private Iterator mNBestIterator;
    private String[] mTokens;
    private String[] mReferenceTags;
    
    /**
     * Construct a scorer for hidden Markov model taggings from
     * the specified HMM.
     *
     * @param hmm Hidden Markov model to use for tagging.
     * @param tags Array of outcome tags in the specified model.
     * @param nBest Maximum number of n-best results to evaluate per case.
     */
    public HmmEvaluator(HiddenMarkovModel hmm, String[] tags, int nBest) {
	this(new HmmDecoder(hmm),
	     new HmmEvaluation(tags,nBest));
    }


    /**
     * Construct an evaluator for the specified hidden Markov model
     * decoder and specified evaluation.
     */
    public HmmEvaluator(HmmDecoder decoder, HmmEvaluation evaluation) {
	mDecoder = decoder;
	mEvaluation = evaluation;
    }

    /**
     * Handle the specified tokens and reference tags as a test case,
     * ignoring the whitespaces value.  The whitespaces value may be
     * <code>null</code>.  For each call to this method, the
     * HMM being evaluated will be run over the tokens to produce
     * first-best, lattice and n-best outputs, which are then
     * used to populate an HMM evaluation.
     *
     * @param tokens Tokens for evaluation.
     * @param whitespaces Ignored.
     * @param referenceTags Reference tags for evaluation.
     * @throws IllegalArgumentException If the token and tag arrays
     * are not the same length; whitespaces are not checked.
     */
    public void handle(String[] tokens, String[] whitespaces, 
                       String[] referenceTags) {
        if (tokens.length != referenceTags.length) {
            String msg = "Tokens and reference tags must be same length."
                + " Found tokens.length=" + tokens.length
                + " referenceTags.length=" + referenceTags.length;
            throw new IllegalArgumentException(msg);
        }
        mTokens = tokens;
        mReferenceTags = referenceTags;
        mFirstBest = mDecoder.firstBest(tokens);
        mLattice = mDecoder.lattice(tokens);
        mNBestIterator = mDecoder.nBest(tokens);
        mEvaluation.addCase(tokens,referenceTags,
                            mFirstBest,mLattice,mNBestIterator);
    }

    /**
     * Return a string-based representation of the results for the
     * last case handled by this evaluator.  The report contains information
     * on the tokenization and reference tags, first-best tags, n-best
     * rank and an extensive dump of confidences for indivdiual tags.
     *
     * <P>Note that cumulative results are available from the HMM
     * evaluation returned by {@link #evaluation()}.
     *
     * @return A string-based representation of the last case handled
     * by this evaluator.
     */
    public String lastCaseToString() {
        if (mTokens == null) 
            return "No cases handled yet.";
        StringBuffer sb = new StringBuffer();
        for (int tokenIndex = 0; tokenIndex < mTokens.length; ++tokenIndex) {
            sb.append(mEvaluation.knownTokenSet().contains(mTokens[tokenIndex])
                      ? "       "
                      : "    ?? ");
            sb.append(pad(mTokens[tokenIndex],20));
            sb.append(pad(mReferenceTags[tokenIndex],4));
            sb.append("  |  ");
            sb.append(pad(mFirstBest[tokenIndex],6));
            if (mReferenceTags[tokenIndex].equals(mFirstBest[tokenIndex]))
                sb.append("    ");
            else
                sb.append(" XX ");
            ScoredObject[] tagProbs 
                = mLattice.log2ConditionalTags(tokenIndex);
            for (int i = 0; i < 10 && i < tagProbs.length; ++i) {
                double log2CondProb = tagProbs[i].score();
                if (log2CondProb < -10.966) break;  //  ~ 0.001 rounded
                double conditionalProb = Math.pow(2.0,log2CondProb);
                String tag = (String) tagProbs[i].getObject();
                sb.append(" " + format(conditionalProb) 
                          + ":" + pad(tag,4));
            }
            sb.append('\n');
        }        
        sb.append("N-Best Rank=");
        int lastNBest = mEvaluation.lastNBest();
        if (lastNBest >= mEvaluation.maxNBest())
            sb.append(">=");
        sb.append(lastNBest);
        sb.append('\n');
        return sb.toString();
    }

    static String format(double x) {
        return Strings.decimalFormat(x,"#,##0.000",9);
    }

    static String pad(String in, int length) {
        if (in.length() > length) return in.substring(0,length-4) + "... ";
        if (in.length() == length) return in;
        StringBuffer sb = new StringBuffer(length);
        sb.append(in);
        while (sb.length() < length) sb.append(' ');
        return sb.toString();
    
    }

    /**
     * Returns the evaluation produced by this evaluator.  This
     * evaluation may be inspected at any time.  The returned
     * evaluation is the same evaluation contained in the evaluator,
     * so any changes to it will affect this evaluator.
     *
     * @return The evaluation for this evaluator.
     */
    public HmmEvaluation evaluation() {
        return mEvaluation;
    }


}
