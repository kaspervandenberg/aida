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

package com.aliasi.classify;

import com.aliasi.util.Math;

/**
 * A <code>ConditionalClassification</code> is a scored classification
 * which estimates conditional probabilities of categories given an
 * input.  By default, the scores are the conditional probabilities;
 * if the scores are different than the conditional probabilities,
 * they must be in the same order.  Both score and conditional
 * probability are tracked independently by the evaluators.  The
 * method {@link #conditionalProbability(int)} returns the conditional
 * probability based on rank while the superclass method {@link
 * #score(int)} returns the score by rank.
 *
 * <P>The conditional probabilities must sum to one over the set of
 * categories:
 *
 * <blockquote><code>
 * <big><big>&Sigma;</big></big><sub><sub>rank&lt;size()</sub></sub>
 * score(rank) = 1.0
 * </code></blockquote>
 *
 * <P>The constructors check that this criterion is satisfied to
 * within a specified arithmetic tolerance.  The convenience method
 * {@link com.aliasi.stats.Statistics#normalize(double[])} may be used
 * to normalize an array of probability ratios so that they will be an
 * acceptable input to this constructor, but note the warning in that
 * method's documentation concerning arithmetic precision.
 * 
 * @author Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class ConditionalClassification extends ScoredClassification {

    private final double[] mConditionalProbs;

    /**
     * Construct a conditional classification with the specified
     * categories and conditional probabilities which sum to one
     * within the default tolerance of <code>0.01</code>.  The
     * conditional probabilities are used as the scores. 
     *
     * @param categories Categories assigned by classification.
     * @param conditionalProbs Conditional probabilities of the
     * categories.
     * @throws IllegalArgumentException If the category and
     * probability arrays are of different lengths, if the
     * probabilities or scores are not in descending order, if any
     * probability is less than zero or greater than one, or if their
     * sum is not 1.0 plus or minus 0.01.
     */
    public ConditionalClassification(String[] categories,
                     double[] conditionalProbs) {
    this(categories,conditionalProbs,conditionalProbs,TOLERANCE);
    }
    
    /**
     * Construct a conditional classification with the specified
     * categories, scores and conditional probabilities which sum to
     * one within the default tolerance of <code>0.01</code>.  The
     * scores and conditional probs must be of the same length as the
     * categories and in descending numerical order.
     *
     * @param categories Categories assigned by classification.
     * @param scores Scores of the categories.
     * @param conditionalProbs Conditional probabilities of the
     * categories.
     * @throws IllegalArgumentException If the category and
     * probability arrays are of different lengths, if the
     * probabilities or scores are not in descending order, if any
     * probability is less than zero or greater than one, or if their
     * sum is not 1.0 plus or minus 0.01.
     */
    public ConditionalClassification(String[] categories,
                     double[] scores,
                     double[] conditionalProbs) {
    this(categories,scores,conditionalProbs,TOLERANCE);
    }

    /**
     * Construct a conditional classification with the specified
     * categories and conditional probabilities whose probabilities
     * sum to one within the specified tolerance.  By setting the
     * tolerance to <code>Double.POSITIVE_INFINITY</code>, there is
     * effectively no consistency requirement placed on the
     * conditional probabilities.
     *
     * @param categories Categories assigned by classification.
     * @param conditionalProbs Conditional probabilities of the
     * categories.
     * @param tolerance Tolerance within which the conditional probabilities
     * must sum to one.
     * @throws IllegalArgumentException If the category and
     * probability arrays are of different lengths, if the probabilities
     * are not in descending order, if any probability is less than
     * zero or greater than one, or if their sum is not 1.0 plus or
     * minus the tolerance, or if the tolerance is not a positive number.
     */
    public ConditionalClassification(String[] categories,
                     double[] conditionalProbs,
                     double tolerance) {
    this(categories,conditionalProbs,conditionalProbs,tolerance);
    }


    /**
     * Construct a conditional classification with the specified
     * categories and conditional probabilities whose probabilities
     * sum to one within the specified tolerance.  By setting the
     * tolerance to <code>Double.POSITIVE_INFINITY</code>, there is
     * effectively no consistency requirement placed on the
     * conditional probabilities.
     *
     * @param categories Categories assigned by classification.
     * @param scores Scores of the categories.
     * @param conditionalProbs Conditional probabilities of the
     * categories.
     * @param tolerance Tolerance within which the conditional probabilities
     * must sum to one.
     * @throws IllegalArgumentException If the category and
     * probability or score arrays are of different lengths, if the
     * probabilities or scores are not in descending order, if any
     * probability is less than zero or greater than one, or if their
     * sum is not 1.0 plus or minus the tolerance, or if the tolerance
     * is not a positive number.
     */
    public ConditionalClassification(String[] categories,
                     double[] scores,
                     double[] conditionalProbs,
                     double tolerance) {
    super(categories,scores);
    mConditionalProbs = conditionalProbs;
    if (tolerance < 0.0 || Double.isNaN(tolerance)) {
        String msg = "Tolerance must be a positive number."
        + " Found tolerance=" + tolerance;
        throw new IllegalArgumentException(msg);
    }
    for (int i = 0; i < conditionalProbs.length; ++i) {
        if (conditionalProbs[i] < 0.0 || conditionalProbs[i] > 1.0) {
        String msg = "Conditional probabilities must be "
            + " between 0.0 and 1.0."
            + " Found conditionalProbs[" + i + "]=" 
            + conditionalProbs[i];
        throw new IllegalArgumentException(msg);
        }
    }
    double sum = Math.sum(conditionalProbs);
    if (sum < (1.0-tolerance)  || sum > (1.0+tolerance)) {
        String msg = "Conditional probabilities must sum to 1.0."
        + " Acceptable tolerance=" + tolerance
        + " Found sum=" + sum;
        throw new IllegalArgumentException(msg);
    }
    }

    /**
     * Returns the conditional probability estimate for the category
     * at the specified rank.  Note that this method returns the same
     * result as {@link #score(int)}.
     *
     * @param rank Rank of category.
     * @return The conditional probability of the category at the
     * specified rank.
     * @throws IllegalArgumentException If the rank is out of range.
     */
    public double conditionalProbability(int rank) {
    if (rank < 0 || rank > (mConditionalProbs.length - 1)) {
        String msg = "Require rank in range 0.." 
        + (mConditionalProbs.length-1)
        + " Found rank=" + rank;
        throw new IllegalArgumentException(msg);
    }
    return mConditionalProbs[rank];
    }

    /**
     * Returns a string-based representation of this conditional
     * probability ranked classification.
     *
     * @return A string-based representation of this classification.
     */
    public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Rank  Category  Score  P(Category|Input)\n");
    for (int i = 0; i < size(); ++i)
        sb.append(i + "=" + category(i) + " " + score(i) 
              + " " + conditionalProbability(i) + '\n');
    return sb.toString();
    }


    private static final double TOLERANCE = 0.01;

}
