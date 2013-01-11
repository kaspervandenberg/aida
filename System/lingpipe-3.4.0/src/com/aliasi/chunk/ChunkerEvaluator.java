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

package com.aliasi.chunk;

import com.aliasi.classify.ScoredPrecisionRecallEvaluation;

import com.aliasi.corpus.ChunkHandler;
import com.aliasi.corpus.ChunkTagHandlerAdapter;
import com.aliasi.corpus.TagHandler;


import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The <code>ChunkerEvaulator</code> class provides an evaluation
 * framework for chunkers.  An instance of this class is constructed
 * based on the chunker to be evaluated.  This class implements the
 * {@link ChunkHandler} interface in order to receive reference
 * chunkings.  Reference chunkings may be added directly using the
 * {@link #handle(Chunking)} or by passing this handler to an
 * appropriate parser.  Either way, the sequence is extracted from the
 * reference chunking, the contained chunker is used to generate
 * a response chunking, and then the reference and response chunkings
 * are added to a contained {@link ChunkingEvaluation} which maintains
 * a running score.  The method {@link #evaluation()} returns the
 * contained chunking evaluation, which may be inspected for partial
 * results at any time.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.1
 */
public class ChunkerEvaluator implements ChunkHandler, TagHandler {

    private final Chunker mChunker;

    private boolean mVerbose = false;

    // 1st-best
    private final ChunkingEvaluation mChunkingEvaluation;

    // n-best
    private final ObjectToCounterMap<Integer> mCorrectRanks 
        = new ObjectToCounterMap<Integer>();

    // conf eval
    private final ScoredPrecisionRecallEvaluation mConfEval
        = new ScoredPrecisionRecallEvaluation();

    // n-best
    int mMaxNBest = 64;
    int mMaxNBestPrint = 8;
    String mLastNBestCase = null;

    // conf eval
    int mConfMaxChunks = 128;
    String mLastConfidenceCase = null;


    /**
     * Construct an evaluator for the specified chunker.
     *
     * @param chunker Chunker to evaluate.
     */
    public ChunkerEvaluator(Chunker chunker) {
        mChunker = chunker;
        mChunkingEvaluation = new ChunkingEvaluation();
    }


    /**
     * Sets the verbosity level of this evaluator to the specified
     * value.  If the argument is <code>true</code>, calls to {@link
     * #handle(Chunking)} will print (to {@link System#out}) a report
     * for each chunking evaluation (first-best, n-best and
     * confidence).  
     *
     * <p>The reports that are written are also available as strings
     * programmatically through the methods {@link #lastNBestCaseReport()}, and
     * {@link #lastConfidenceCaseReport()},
     *
     * @param isVerbose <code>true</code> for standard output per
     * case.
     */
    public void setVerbose(boolean isVerbose) {
        mVerbose = isVerbose;
    }

    /**
     * Returns a string-based representation of the last evaluation
     * case and the first-best result.
     *
     * @return The first-best report for the last case handled.
     */
    public String lastFirstBestCaseReport() {
        return mChunkingEvaluation.mLastCase;
    }

    /**
     * Sets the maximum number of chunks extracted by a
     * confidence-based chunker for evaluation.
     *
     * @param n Number of chunks to extract with confidence.
     */
    public void setMaxConfidenceChunks(int n) {
        mConfMaxChunks = n;
    }
    
    /**
     * Returns a string-based representation of the last evaluation
     * case's confidence evaluation.  If there has not been an
     * evaluation case or the chunker being evaluated is not a
     * confidence-based chunker, this result will be
     * <code>null</code>.
     *
     * @return A string representation of the last case's confidence
     * evaluation.
     */
    public String lastConfidenceCaseReport() {
        return mLastConfidenceCase;
    }

    /**
     * Sets the maximum number of chunkings extracted by an n-best
     * chunker for evaluation.
     *
     * @param n Number of chunkings to evaluate in n-best chunking.
     */
    public void setMaxNBest(int n) {
        mMaxNBest = n;
    }

    /**
     * Sets the maximum number of chunkings that will be reported in a
     * case report.  That is, chunkings reported through a call to the
     * the {@link #lastNBestCaseReport()} method.
     *
     * @param n Number of n-best results to print in a case report.
     */
    public void setMaxNBestReport(int n) {
        mMaxNBestPrint = n;
    }

    /**
     * Returns a string-based representation of the last n-best
     * evaluation case.
     *
     * @return String representing the last n-best case evaluation.
     */
    public String lastNBestCaseReport() {
        return mLastNBestCase;
    }

    /**
     * Handle the specified reference chunking encoded in the standard
     * BIO tag chunking format.  If the whitespaces are <code>null</code>,
     * a single space character is used to separate tokens.
     *
     * <p>See {@link #handle(Chunking)} for
     * more information.  
     *
     * @param tokens Array of tokens.
     * @param whitespaces Array of whitespaces.
     * @param tags Array of tags.
     */
    public void handle(String[] tokens, String[] whitespaces, String[] tags) {
        ChunkTagHandlerAdapter adapter = new ChunkTagHandlerAdapter(this);
        adapter.handle(tokens,whitespaces,tags);
    }

    /**
     * Handle the specified reference chunking.  This involves
     * running the chunker being evaluated over the reference
     * chunking's sequence to create a response chunking, which
     * is then added with the reference chunking as a case to
     * the chunking evaluation.  
     *
     * <p>If the contained chunker returns <code>null</code> for
     * a given input, this method will fill in a chunking over
     * the appropriate sequence with no chunks for evaluation.
     *
     * @param referenceChunking The reference chunking case.
     */
    public void handle(Chunking referenceChunking) {
        CharSequence cSeq = referenceChunking.charSequence();


        // first-best
        Chunking firstBestChunking  = mChunker.chunk(cSeq);
        if (firstBestChunking == null)
            firstBestChunking = new ChunkingImpl(cSeq);
        mChunkingEvaluation.addCase(referenceChunking,firstBestChunking);

        if (mChunker instanceof NBestChunker) {
            NBestChunker nBestChunker = (NBestChunker) mChunker;
            char[] cs = Strings.toCharArray(cSeq);
            StringBuffer sb = new StringBuffer();

            sb.append(ChunkingEvaluation.formatHeader(13,referenceChunking));
            sb.append(" REF                 " + ChunkingEvaluation.formatChunks(referenceChunking));

            Iterator nBestIt = nBestChunker.nBest(cs,0,cs.length,mMaxNBest);
            int i;
            double score = Double.NEGATIVE_INFINITY;
            int foundRank = -1;
            for (i = 0; i < mMaxNBest && nBestIt.hasNext(); ++i) {
                ScoredObject so = (ScoredObject) nBestIt.next();
                score = so.score();
                Chunking responseChunking = (Chunking) so.getObject();
                if (i < mMaxNBestPrint) {
                    sb.append(Strings.decimalFormat(i,"#,##0",9));
                    sb.append(" ");
                    sb.append(Strings.decimalFormat(score,"#,##0",10));
                    sb.append(" ");
                    sb.append(ChunkingEvaluation.formatChunks(responseChunking));
                }
                if (responseChunking.equals(referenceChunking)) {
                    sb.append("  -----------\n");
                    foundRank = i;
                }
            }
            if (foundRank < 0) 
                sb.append("Correct Rank >=" + mMaxNBest + "\n\n");
            else
                sb.append("Correct Rank=" + foundRank + "\n\n");
            mCorrectRanks.increment(new Integer(foundRank));
        
            mLastNBestCase = sb.toString();
        }

        if (mChunker instanceof ConfidenceChunker) {
            ConfidenceChunker confChunker = (ConfidenceChunker) mChunker;
            char[] cs = Strings.toCharArray(cSeq);
            StringBuffer sb = new StringBuffer();
            Set refChunks = new HashSet();
            Iterator it =referenceChunking.chunkSet().iterator();
            while (it.hasNext()) {
                Chunk nextChunk = (Chunk) it.next();
                Chunk zeroChunk = toUnscoredChunk(nextChunk);
                refChunks.add(zeroChunk);
            }

            sb.append(ChunkingEvaluation.formatHeader(5,referenceChunking));
        
            Iterator nBestIt 
                = confChunker.nBestChunks(cs,0,cs.length,mConfMaxChunks);
            while (nBestIt.hasNext()) {
                Chunk nextChunk = (Chunk) nBestIt.next();
                double score = nextChunk.score();
                Chunk zeroedChunk = toUnscoredChunk(nextChunk);
                boolean correct = refChunks.contains(zeroedChunk);
                sb.append((correct ? "TRUE " : "false")
                          + " (" + nextChunk.start() + ", " + nextChunk.end() + ")"
                          + ": " + nextChunk.type()
                          + "  " + nextChunk.score() + "\n");
        
                mConfEval.addCase(correct,score);
            }
            mLastConfidenceCase = sb.toString();
        }
        report();
    }

    void report() {
        if (!mVerbose) return;
        System.out.println(mChunkingEvaluation.mLastCase);
        if (mChunker instanceof NBestChunker)
            System.out.println(mLastNBestCase);
        if (mChunker instanceof ConfidenceChunker)
            System.out.println(mLastConfidenceCase);
    }

    /**
     * Returns the scored precision-recall evaluation derived from a
     * confidence-based chunker.  If the chunker being evaluated is
     * not a confidence-based chunker, then this evaluation will be
     * empty.
     *
     * <p>This is the actual evaluation used by this class, so
     * changing it will affect this class's results.
     *
     * @return The scored precision/recall evaluation.
     */
    public ScoredPrecisionRecallEvaluation confidenceEvaluation() {
        return mConfEval;
    }


    /**
     * Return the first-best chunking evaluation.  
     *
     * <p>This is the actual evaluation used by this class, so
     * changing it will affect this class's results.
     *
     * @return The chunking evaluation.
     */
    public ChunkingEvaluation evaluation() {
        return mChunkingEvaluation;
    }

    /**
     * Returns the n-best evaluation in the form of a mapping from
     * ranks to the number of times the reference chunking was that
     * rank in the evaluation.  The ranks are instances of
     * <code>Integer</code>, with <code>-1</code> being the rank
     * assigned to cases in which the reference chunking was not
     * among the n-best results.
     *
     * <p>This is the actual counter used by this class, so
     * changing it will affect this class's results.
     *
     * <p>If the chunker being evaluated is not an n-best chunker,
     * then this evaluation will be empty.
     *
     * @return The n-best evaluation.
     */
    public ObjectToCounterMap<Integer> nBestEvaluation() {
        return mCorrectRanks;
    }

    /**
     * Returns a string-based representation of this evaluation.
     * It will include the first-best evaluation.  An n-best evaluation
     * and/or a confidence evaluation are included if defined.
     *
     * @return A string-based representation of this evaluator.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("FIRST-BEST EVAL\n");
        sb.append(evaluation().toString());
        if (mChunker instanceof NBestChunker) {
            sb.append("\n\nN-BEST EVAL (rank=count)\n");
            sb.append(nBestEvaluation().toString());
        }
        if (mChunker instanceof ConfidenceChunker) {
            sb.append("\n\nCONFIDENCE EVALUATION");
            sb.append(confidenceEvaluation().toString());
        }
        return sb.toString();
    }

    static Chunk toUnscoredChunk(Chunk c) {
        return ChunkFactory.createChunk(c.start(),
                                        c.end(),
                                        c.type());
    }


}
