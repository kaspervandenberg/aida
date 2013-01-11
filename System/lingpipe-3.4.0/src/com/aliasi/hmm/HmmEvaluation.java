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

import com.aliasi.classify.Classification;
import com.aliasi.classify.ClassifierEvaluator;
import com.aliasi.classify.JointClassification;

import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * An <code>HmmEvaluation</code> stores and reports the results for
 * evaluating hidden Markov models.  There are methods providing for
 * adding test cases (with results) and for various means of
 * reporting.  
 *
 * <P>The top-level {@link
 * #addCase(String[],String[],String[],TagWordLattice,Iterator)} adds
 * a complete case in the form of tokens, reference tags, first-best
 * response tags, a tag-word lattice of confidence estimates for tags,
 * and an iterator over the n-best list.  All of these are available
 * as outputs from an {@link HmmDecoder}.  If this method is used for
 * all cases, then all reports will be complete.  If it is not used
 * for all cases, then the results will not be complete.  For
 * instance, if {@link #addFirstBestCase(String[],String[],String[])}
 * is called directly, then it only adds results for the first-best
 * evaluation, and only the first-best evaluation results will be
 * relevant.
 *
 * <P>Results are available in the form of two different clasifier
 * evaluations.  The method {@link #firstBestEvaluation()} returns the
 * evaluation of the first-best results as a first-best classifier
 * evaluation on a token-by-token basis.  The method {@link
 * #confidenceEvaluation()} returns the confidence-based evaluation
 * in the form of a joint probability classifier evaluation.  
 *
 * <P>The results of the n-best decoder are available as a histogram
 * through {@link #nBestHistogram()}.  This histogram maps ranks to
 * the number of cases for which the correct result was of that rank
 * in the n-best list.  For instance, if the reference tagging was the
 * 7th-best result returned by the n-best iterator on three
 * occassions, then the n-best histogram maps the <code>Integer</code>
 * 7 to the count 3.
 *
 * <P>The method {@link #caseAccuracy()} returns the
 * percentage of cases for which the first-best answer has been
 * completely correct.  This makes most sense when the cases are
 * coherent units, such as sentences.
 *
 * <P>First-best accuracy for unknown words is available through the
 * method {@link #unknownTokenAccuracy()}.  The set of known tokens is
 * available through the method {@link #knownTokenSet()}.  This set
 * begins empty after construction.  Tokens may be added to this set
 * through the method {@link #addKnownToken(String)}.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.1
 */
public class HmmEvaluation {

    private final ClassifierEvaluator<String,Classification> mFirstBestEvaluation;
    private final ClassifierEvaluator<String,JointClassification> mLatticeEvaluation;
    private final ObjectToCounterMap mNBestHistogram;
    private final int mMaxNBest;

    private final Set mKnownTokenSet = new HashSet();

    private long mNumTokens = 0;

    private long mNumCases = 0;
    private long mNumCasesCorrect = 0;

    private long mNumUnknownTokens = 0;
    private long mNumUnknownTokensCorrect = 0;


    private int mLastNBest;
    
    /**
     * Construct a hidden Markov model evaluation with the specified
     * depth of n-best evaluation.  The n-best evaluation depth will
     * determine how many entries of the n-best results are searched
     * before giving up.  High values for the n-best number may cause
     * significant slowdowns in processing, especially for long input
     * strings.
     *
     * @param tags Possible state tags output by the HMM.
     * @param maxNBest Maximum n-best output to consider.
     */
    public HmmEvaluation(String[] tags, int maxNBest) {
        mFirstBestEvaluation = new ClassifierEvaluator<String,Classification>(null,tags);
        mLatticeEvaluation = new ClassifierEvaluator<String,JointClassification>(null,tags);
        mNBestHistogram = new ObjectToCounterMap<Integer>();
        mMaxNBest = maxNBest;
    }

    /**
     * Returns the number of cases making up this evaluation.
     */
    public long numCases() {
        return mNumCases;
    }

    /**
     * Returns the number of tokens making up this evaluation.
     */
    public long numTokens() {
        return mNumTokens;
    }

    /**
     * Returns the maximum n-best result searched.
     */
    public int maxNBest() {
        return mMaxNBest;
    }


    /**
     * Returns the classifier evaluation derived from the first-best
     * hypotheses.  This is a first-best classifier evaluation.
     *
     * @return This evaluation's classifier evaluation.
     */
    public ClassifierEvaluator<String,Classification> firstBestEvaluation() {
        return mFirstBestEvaluation;
    }

    /**
     * Returns the classifier evaluation derived from the tag-word
     * lattice confidence scoring.  The result is an evaluation with
     * scores and meaningful ranked outputs such as precision-recall
     * curves.
     *
     * @return The confidence evaluation for this HMM.
     */
    public ClassifierEvaluator<String,JointClassification> confidenceEvaluation() {
        return mLatticeEvaluation;
    }

    /**
     * Return the histogram of n-best ranks of the reference tagging
     * in the first-best responses.  The mapping is from
     * <code>Integer</code> objects representing ranks to counts of
     * the number of times the result of that rank was correct.  The
     * ranks will be greater than or equal to zero and less than the
     * value of {@link #maxNBest()}.  In addition, the count assigned
     * to {@link #maxNBest()} itself will return the count of all
     * cases that are greater than or equal to {@link #maxNBest()}.
     *
     * @return The n-best histogram for this evaluation.
     */
    public ObjectToCounterMap<Integer> nBestHistogram() {
        return mNBestHistogram;
    }

    /**
     * Adds a complete response case for evaluation, consisting
     * of the specified tokens, reference tags, first-best
     * response tags, lattice of forward-backward confidence-based
     * scores, and an iterator over the n-best list.  If any of the
     * last three values are <code>null</code>, then they will
     * not be added to the evaluations.
     *
     * @param tokens The tokens for the evaluation.
     * @param referenceTags The reference tagging.
     * @param responseTags The response tagging.
     * @throws IllegalArgumentException If the token and tag arrays
     * are not the same length, or if the lattice is not over the
     * specified token array.
     */
    public void addCase(String[] tokens, 
                        String[] referenceTags,    
                        String[] responseTags,
                        TagWordLattice lattice,
                        Iterator<ScoredObject<String[]>> nBestIterator) {
        addFirstBestCase(tokens,referenceTags,responseTags);
        addLatticeCase(tokens,referenceTags,lattice);
        addNBestCase(tokens,referenceTags,nBestIterator);
    }

    /**
     * Adds a first-best response case with the specified tokens,
     * reference tags, and first-best response tags.  Note that
     * this only adds information to the first-best evaluation,
     * not the n-best or lattice-based evaluations.
     *
     * @param tokens The tokens for the evaluation.
     * @param referenceTags The reference tagging.
     * @param responseTags The response tagging.
     * @throws IllegalArgumentException If the token, reference tag
     * and response tag arrays are not all the same length.
     */
    public void addFirstBestCase(String[] tokens,
                                 String[] referenceTags, 
                                 String[] responseTags) {
        verifyEqualLengths("tokens","referenceTags",tokens,referenceTags);
        verifyEqualLengths("tokens","responseTags",tokens,responseTags);
        mNumTokens += tokens.length;
        ++mNumCases;
        if (com.aliasi.util.Arrays.equals(referenceTags,responseTags))
            ++mNumCasesCorrect;
        for (int i = 0; i < tokens.length; ++i) {
            Classification result = new Classification(responseTags[i]);
            mFirstBestEvaluation.addClassification(referenceTags[i],result);
        }
        for (int i = 0; i < tokens.length; ++i) {
            if (knownTokenSet().contains(tokens[i])) continue;
            ++mNumUnknownTokens;
            if (referenceTags[i].equals(responseTags[i]))
                ++mNumUnknownTokensCorrect;
        }
    }

    /**
     * Returns the accuracy measured over entire cases.  This is the
     * number of evaluation cases that are completely correct divided
     * by the number of cases evaluated.  This number makes sense in
     * cases where the cases correspond to meaningful units such as
     * sentences.
     * 
     * @return The first-best complete case tagging accuracy.
     */
    public double caseAccuracy() {
        return ((double) mNumCasesCorrect)
            / (double) mNumCases;
    }

    /**
     * Returns the set of known tokens for this evaluation.
     * This set is immutable, but will reflect the current
     * set of known tokens.  
     *
     * @return The set of known tokens for this evaluation.
     */
    public Set knownTokenSet() {
        return Collections.unmodifiableSet(mKnownTokenSet);
    }

    /**
     * Adds the specified token to the set of known tokens.
     *
     * @param token Token to add to set of known tokens.
     */
    public void addKnownToken(String token) {
        mKnownTokenSet.add(token);
    }

    /**
     * Returns the first-best accuracy for unknown tokens.  Unknown
     * tokens are defined to be those not in the mutable set {@link
     * #knownTokenSet()} at the time the evaluation case was
     * added.
     *
     * @return The first-best unknown token accuracy.
     */
    public double unknownTokenAccuracy() {
        return ((double)mNumUnknownTokensCorrect)
            / (double)mNumUnknownTokens;
    }

    /**
     * Add a lattice-based response case with the specified tokens, 
     * reference tags and lattice.  Note that this only adds information
     * to the lattice evaluation, not the first-best or n-best evaluations.
     * 
     * @param tokens The tokens for the evaluation.
     * @param referenceTags The reference tagging.
     * @param lattice The response lattice.
     * @throws IllegalArgumentException If the token and reference tag
     * arrays are different lengths, or if the lattice tokens are not
     * the same as the tokens.
     */
    public void addLatticeCase(String[] tokens,
                               String[] referenceTags,
                               TagWordLattice lattice) {
        verifyEqualLengths("tokens","referenceTags",tokens,referenceTags);
        verifyEqual(tokens,lattice.tokens());
        for (int i = 0; i < tokens.length; ++i) {
            ScoredObject[] scoredTags = lattice.log2ConditionalTags(i);
            double[] log2JointProbs = new double[scoredTags.length];
            String[] responseNBestTags = new String[scoredTags.length];
            for (int j = 0; j < scoredTags.length; ++j) {
                log2JointProbs[j] = scoredTags[j].score();
                responseNBestTags[j] = scoredTags[j].getObject().toString();
            }
            JointClassification jc 
                = new JointClassification(responseNBestTags, log2JointProbs);
            mLatticeEvaluation.addClassification(referenceTags[i],jc);
        }
    }


    /**
     * Add an n-best response case with the specified tokens,
     * reference tags and n-best iterator.  Note that this only adds
     * information to the n-best evaluation, not the first-best or
     * confidence-based lattice evaluations.
     *
     * @param tokens The tokens for the evaluation.
     * @param referenceTags The reference tagging.
     * @param nBestIterator The n-best iterator.
     * @throws IllegalArgumentException If the token and reference tag
     * arrays are different lengths.
     */
    public void addNBestCase(String[] tokens,
                             String[] referenceTags,
                             Iterator<ScoredObject<String[]>> nBestIterator) {
        verifyEqualLengths("tokens","referenceTags",tokens,referenceTags);
        for (int i = 0; ((i < mMaxNBest) && nBestIterator.hasNext()); ++i) {
            ScoredObject response = (ScoredObject) nBestIterator.next();
            String[] responseTags = (String[]) response.getObject();
            if (com.aliasi.util.Arrays.equals(referenceTags,responseTags)) {
                mNBestHistogram.increment(new Integer(i));
                mLastNBest = i;
                return;
            }
        }
        mLastNBest = mMaxNBest;
        mNBestHistogram.increment(new Integer(mMaxNBest));
    }

    /**
     * Returns a terse, one-line report of the current state of this
     * evaluation.
     *
     * @return A string representation of the state of this
     * evaluation.
     */
    public String toString() {
        return "#Cases=" + mNumCases
            + "  #Toks=" + mNumTokens
            + "  Tok Acc=" 
            + format(mFirstBestEvaluation.confusionMatrix().totalAccuracy())
            + "  Case Acc="  + format(caseAccuracy())
            + "  Lattice Acc=" 
            + format(mLatticeEvaluation.confusionMatrix().totalAccuracy())
            + "  Unknown Toks=" + mNumUnknownTokens
            + "  Unknown Tok Acc=" + format(unknownTokenAccuracy())
            ;
    }

    
    int lastNBest() {
        return mLastNBest;
    }

    static String format(double x) {
        return Strings.decimalFormat(x,"0.000",5);
    }



    static void verifyEqualLengths(String name1, String name2, 
                                   String[] xs1, String[] xs2) {
        if (xs1.length == xs2.length) return;
        String msg = "Arrays " + name1 + " and " + name2 
            + " must be same length."
            + " Found " + name1 + ".length=" + xs1.length
            + " " + name2 + ".length=" + xs2.length;
        throw new IllegalArgumentException(msg);
    }

    static void verifyEqual(String[] tokens1, String[] tokens2) {
        if (tokens1.length != tokens2.length) {
            String msg = "Tokens must match lattice tokens."
                + " tokens.length=" + tokens1.length
                + " lattice.tokens().length=" + tokens2.length;
            throw new IllegalArgumentException(msg);
        }
        for (int i = 0; i < tokens1.length; ++i) {
            if (tokens1[i].equals(tokens2[i])) continue;
            String msg = "Tokens must match lattice tokens."
                + " tokens[" + i + "]=" + tokens1[i]
                + " != lattice.tokens()[" + i + "]=" + tokens2[i];
            throw new IllegalArgumentException(msg);
        }    
    }
    
}
