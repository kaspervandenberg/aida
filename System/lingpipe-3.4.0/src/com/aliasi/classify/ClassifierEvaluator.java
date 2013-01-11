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

import com.aliasi.corpus.ClassificationHandler;

import com.aliasi.util.Collections;
import com.aliasi.util.Scored;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A <code>ClassifierEvaluator</code> provides an evaluation harness
 * for classifiers.  An evaluator is constructed from a classifier and
 * a complete list of the categories returned by the classifier.  Test
 * cases are then added using the {@link #addCase(String,Object)}
 * which accepts a string-based category and object to classify.  The
 * evaluator will run the classifier over the input object and collect
 * results over multiple cases. Depending on the classification types
 * returned by the classifier, various report statistics are
 * available.
 *
 * <P>An exhaustive set of evaluation metrics for first-best
 * classification results is accessbile as a confusion matrix through
 * the {@link #confusionMatrix()} method.  Confusion matrices provide
 * dozens of statistics on classification which can be computed from
 * first-best results; see {@link ConfusionMatrix} for more
 * information.
 * 
 * <P>Depending on the class of return results for the classifier
 * being evaluated, the following methods are supported:
 *
 * <blockquote>
 * <table border='1' cellpadding='5'>
 * <tr><td><i>Classifier Return Class</i></td>
 *     <td><i>Supported Methods</i></td></tr>
 * <tr><td><code>Classification</code></td>
 *     <td><table cellpadding='5'>
 *           <tr><td>{@link #confusionMatrix()}</td></tr>
 *         </table>
 *     </td></tr>
 * <tr><td><code>RankedClassification</code></td>
 *     <td><table cellpadding='5'>
 *           <tr><td>{@link #rankCount(String,int)}</td></tr>
 *           <tr><td>{@link #averageRankReference()}</td></tr>
 *           <tr><td>{@link #meanReciprocalRank()}</td></tr>
 *           <tr><td>{@link #averageRank(String,String)}</td></tr>
 *         </table>
 *     </td></tr>
 * <tr><td><code>ScoredClassification</code></td>
 *     <td><table cellpadding='5'>
 *           <tr><td>{@link #scoredOneVersusAll(String)}</td></tr>
 *           <tr><td>{@link #averageScore(String,String)}</td></tr>
 *           <tr><td>{@link #averageScoreReference()}</td></tr>
 *         </table>
 *     </td></tr>
 * <tr><td><code>ConditionalClassification</code></td>
 *     <td><table cellpadding='5'>
 *           <tr><td>{@link #averageConditionalProbability(String,String)}</td></tr>
 *           <tr><td>{@link #averageConditionalProbabilityReference()}</td></tr>
 *         </table>
 *     </td></tr>
 * <tr><td><code>JointClassification</code></td>
 *     <td><table cellpadding='5'>
 *           <tr><td>{@link #averageLog2JointProbability(String,String)}</td></tr>
 *           <tr><td>{@link #averageLog2JointProbabilityReference()}</td></tr>
 *         </table>
 *     </td></tr>
 * </table>
 * </blockquote>
 *
 * <P>If the input is a ranked classification and the reference
 * category does not appear at some rank in the classification,
 * results will be returned as if the reference category appeared in
 * the last possible rank in the ranked classification.  This
 * heuristic for scoring applies to all four methods listed for ranked
 * classifications in the table above.  As a consequence, the results
 * of {@link #averageRank(String,String)} might not be such as they
 * could be derived by a set of ranked classifications, because we are
 * assuming that all unlisted categories have the worst possible rank.
 *
 * <P>This class requires concurrent read and synchronous write
 * synchronization.  Reads are any of the statistics gathering methods
 * and write is just adding new test cases.
 *
 * @author  Bob Carpenter
 * @version 3.1.2
 * @since   LingPipe2.0
 */
public class ClassifierEvaluator<E,C extends Classification> 
    implements ClassificationHandler<E,Classification> {

    // Classification
    final Classifier<E,C> mClassifier;
    private final ConfusionMatrix mConfusionMatrix;
    private int mNumCases = 0;
    final String[] mCategories;
    final HashSet mCategorySet;

    // paired inputs and outputs
    final ArrayList mReferenceCategories = new ArrayList();
    final ArrayList mClassifications = new ArrayList();

    // RankedClassification
    private boolean mHasRanked = false;
    private final int[][] mRankCounts;

    // ScoredClassification
    private boolean mHasScored = false;
    private final ArrayList[] mScoreOutcomeLists;

    // ConditionalClassification
    private boolean mHasConditional = false;
    private final ArrayList[] mConditionalOutcomeLists;

    // JointClassification
    private boolean mHasJoint = false;

    /**
     * Construct a classifier evaluator for the specified classifier
     * that records results for the specified set of categories.
     *
     * <P>If the classifier evaluator is only going to be populated
     * using the {@link #addClassification(String,Classification)}
     * method, then the classifier may be null.
     *
     * @param classifier Classifier to evaluate.
     * @param categories Categories of the classifier.
     */
    public ClassifierEvaluator(Classifier<E,C> classifier, String[] categories) {
        // Classification
        mClassifier = classifier;
        mCategories = categories;
        mCategorySet = new HashSet();
        Collections.addAll(mCategorySet,categories);
        mConfusionMatrix = new ConfusionMatrix(categories);

        // RankedClassification
        int len = categories.length;
        mRankCounts = new int[len][len];
        for (int i = 0; i < len; ++i)
            for (int j = 0; j < len; ++j)
                mRankCounts[i][j] = 0;

        // Scored
        mScoreOutcomeLists = new ArrayList[numCategories()];
        for (int i = 0; i < mScoreOutcomeLists.length; ++i)
            mScoreOutcomeLists[i] = new ArrayList();

        // Conditional
        mConditionalOutcomeLists = new ArrayList[numCategories()];
        for (int i = 0; i < mConditionalOutcomeLists.length; ++i)
            mConditionalOutcomeLists[i] = new ArrayList();
    
        // Joint
    }

    /**
     * Returns the classifier for this evaluator.
     *
     * @return The classifier for this evaluator.
     */
    public Classifier<E,C> classifier() {
        return mClassifier;
    }

    /**
     * Returns the categories for which this evaluator stores
     * results.
     *
     * @return The categories for which this evaluator stores
     * results.
     */
    public String[] categories() {
        return mCategories;
    }

    /**
     * Adds a test case for the specified input with the specified
     * reference category.  This method runs the classifer over
     * the specified input.  It then stores the resulting classification
     * and reference category for collective reporting.
     *
     * <P>This method simply applies the classifier specified at
     * construction time to the specified input to produce a
     * classification which is forwarded to {@link
     * #addClassification(String,Classification)}.
     *
     * @param referenceCategory Correct category for object.
     * @param input Object being classified.
     * @throws IllegalArgumentException If the reference category is
     * not a category for this evaluator.
     */
    public void addCase(String referenceCategory, E input) {
        validateCategory(referenceCategory);
        Classification classification = mClassifier.classify(input);
        addClassification(referenceCategory,classification);
    }

    /**
     * This is a convenience implementation for the classification
     * handler interface.  It merely delegates to {@link
     * #addCase(String,Object)} by extracting the best category from
     * the specified classification.
     *
     * @param input Object being evaluated.
     * @param classification Reference classification of object.
     */
    public void handle(E input, Classification classification) {
	addCase(classification.bestCategory(),input);
    }


    /**
     * Returns the number of test cases which have been provided
     * to this evaluator.
     *
     * @return The number of test cases which have been provided
     * to this evaluator.
     */
    public int numCases() {
        return mNumCases;
    }

    /**
     * Returns the confusion matrix of first-best classification
     * result statistics for this evaluator.  See {@link
     * ConfusionMatrix} for details of the numerous available
     * evaluation metrics provided by confusion matrices.
     *
     * @return The confusion matrix for the test cases evaluated so far.
     */
    public ConfusionMatrix confusionMatrix() {
        return mConfusionMatrix;
    }

    /**
     * Returns the number of times that the reference category's
     * rank was the specified rank.  
     *
     * <P>For example, in the set of training samples and results
     * described in the method documentation for {@link
     * #averageRank(String,String)}, sample rank counts are as
     * follows:
     *
     * <blockquote><code>
     * rankCount(&quot;a&quot;,0) = 3
     * <br>rankCount(&quot;a&quot;,1) = 1
     * <br>rankCount(&quot;a&quot;,2) = 0
     * <br> &nbsp;
     * <br>rankCount(&quot;b&quot;,0) = 1
     * <br>rankCount(&quot;b&quot;,1) = 0
     * <br>rankCount(&quot;b&quot;,2) = 1
     * <br> &nbsp;
     * <br>rankCount(&quot;c&quot;,0) = 1
     * <br>rankCount(&quot;c&quot;,1) = 0
     * <br>rankCount(&quot;c&quot;,2) = 0
     * </code></blockquote>
     *
     * These results are typically presented as a bar graph histogram
     * per category.
     * 
     * @param referenceCategory Reference category.
     * @param rank Rank of count.
     * @return Number of times the reference category's ranking was
     * the specified rank.
     * @throws IllegalArgumentException If the category is unknown.
     */
    public int rankCount(String referenceCategory, int rank) {
        validateCategory(referenceCategory);
        int i = categoryToIndex(referenceCategory);
        return rankCount(i,rank);
    }


    /**
     * Returns the average over all test samples of the rank of
     * the the response that matches the reference category.
     *
     * <P>Using the example classifications shown in the method
     * documentation of {@link #averageRank(String,String)}:
     *
     * <blockquote><code>
     * averageRankReference() 
     * <br> = (0 + 0 + 0 + 1 + 0 + 2 + 0)/7 ~ 0.43
     * </code></blockquote>
     *
     * @return The average rank of the reference category in
     * all classification results.
     */
    public double averageRankReference() {
        double sum = 0.0;
        int count = 0;
        for (int i = 0; i < numCategories(); ++i) {
            for (int rank = 0; rank < numCategories(); ++rank) {
                int rankCount = mRankCounts[i][rank];
                if (rankCount == 0) continue; // just efficiency
                count += rankCount;
                sum += rank * rankCount;
            }
        }
        return sum / (double) count;
    }


    /**
     * Returns the average over all test samples of the reciprocal of
     * one plus the rank of the reference category in the response.
     * This represents counting from one, so if the first-best answer
     * is correct, the reciprocal rank is 1/1; if the second is
     * correct, 1/2; if the third, 1/3; and so on.  These individual
     * recirpocals are then averaged over cases.
     *
     * <P>Using the example classifications shown in the method
     * documentation of {@link #averageRank(String,String)}:
     *
     * <blockquote><code>
     * averageRankReference() 
     * <br> = (1/1 + 1/1 + 1/1 + 1/2 + 1/1 + 1/3 + 1/1)/7 ~ 0.83
     * </code></blockquote>
     *
     * @return The mean reciprocal rank of the reference category in
     * the result ranking.
     */
    public double meanReciprocalRank() {
        double sum = 0.0;
        int numCases = 0;
        for (int i = 0; i < numCategories(); ++i) {
            for (int rank = 0; rank < numCategories(); ++rank) {
                int rankCount = mRankCounts[i][rank];
                if (rankCount == 0) continue;  // just for efficiency
                numCases += rankCount;
                sum += ((double) rankCount) / (1.0 + (double) rank);
            }
        }
        return sum / (double) numCases;
    }


    /**
     * Returns the average conditional probability of the specified response
     * category for test cases with the specified reference category.  If
     * there are no cases matching the reference category, the result
     * is <code>Double.NaN</code>.  If the conditional classifiers'
     * results are properly normalized, the sum of the averages over
     * all categories will be 1.0.
     *
     * <P>Better classifiers return high values when the reference and
     * response categories are the same and lower values when they are
     * different.  The log value would be extremely volatile given the
     * extremely low and high conditional estimates of the language
     * model classifiers.
     * 
     *
     * @param refCategory Reference category.
     * @param responseCategory Response category.
     * @return Average conditional probability of response category in
     * cases for specified reference category.
     * @throws IllegalArgumentException If the either category is unknown.
     */
    public double averageConditionalProbability(String refCategory,
                                                String responseCategory) {
        validateCategory(refCategory);
        validateCategory(responseCategory);
        double sum = 0.0;
        int count = 0;
        for (int i = 0; i < mReferenceCategories.size(); ++i) {
            if (mReferenceCategories.get(i).equals(refCategory)) {
                ConditionalClassification c
                    = (ConditionalClassification) mClassifications.get(i);
                for (int rank = 0; rank < c.size(); ++rank) {
                    if (c.category(rank).equals(responseCategory)) {
                        sum += c.conditionalProbability(rank);
                        ++count;
                        break;
                    }
                }
            }
        }
        return sum / (double) count;
    }

    /**
     * Returns the average log (base 2) joint probability of the
     * response category for cases of the specified reference
     * category.  If there are no cases matching the reference
     * category, the result is <code>Double.NaN</code>.  
     *
     * <P>Better classifiers return high values when the reference
     * and response categories are the same and lower values
     * when they are different.  Unlike the conditional probability
     * values, joint probability averages are not particularly
     * useful because they are not normalized by input length.  For
     * the language model classifiers, the scores are normalized
     * by length, and provide a better cross-case view.
     *
     * @param refCategory Reference category.
     * @param responseCategory Response category.
     * @return Average log (base 2) conditional probability of
     * response category in cases for specified reference category.
     * @throws IllegalArgumentException If the either category is unknown.
     */
    public double averageLog2JointProbability(String refCategory,
                                              String responseCategory) {
        validateCategory(refCategory);
        validateCategory(responseCategory);
        double sum = 0.0;
        int count = 0;
        for (int i = 0; i < mReferenceCategories.size(); ++i) {
            if (mReferenceCategories.get(i).equals(refCategory)) {
                JointClassification c
                    = (JointClassification) mClassifications.get(i);
                for (int rank = 0; rank < c.size(); ++rank) {
                    if (c.category(rank).equals(responseCategory)) {
                        sum += c.jointLog2Probability(rank);
                        ++count;
                        break;
                    }
                }
            }
        }
        return sum / (double) count;
    }

    /**
     * Returns the average over all test cases of the score of the
     * response that matches the reference category.  Better
     * classifiers return higher values for this average.
     *
     * <P>Whether average scores make sense across training instances
     * depends on the classifier.
     *
     * @return The average score of the reference category in the
     * response.
     */
    public double averageScoreReference() {
        double sum = 0.0;
        for (int i = 0; i < mReferenceCategories.size(); ++i) {
            String refCategory = mReferenceCategories.get(i).toString();
            ScoredClassification c
                = (ScoredClassification) mClassifications.get(i);
            for (int rank = 0; rank < c.size(); ++rank) {
                if (c.category(rank).equals(refCategory)) {
                    sum += c.score(rank);
                    break;
                }
            }
        }
        return sum / (double) mReferenceCategories.size();
    }


    /**
     * Returns the average over all test cases of the conditional
     * probability of the response that matches the reference
     * category.  Better classifiers return higher values for this
     * average.
     *
     * <P>As a normalized value, the average conditional probability
     * always has a sensible interpretation across training instances.
     *
     * @return The average conditional probability of the reference
     * category in the response.
     */
    public double averageConditionalProbabilityReference() {
        double sum = 0.0;
        for (int i = 0; i < mReferenceCategories.size(); ++i) {
            String refCategory = mReferenceCategories.get(i).toString();
            ConditionalClassification c
                = (ConditionalClassification) mClassifications.get(i);
            for (int rank = 0; rank < c.size(); ++rank) {
                if (c.category(rank).equals(refCategory)) {
                    sum += c.conditionalProbability(rank);
                    break;
                }
            }
        }
        return sum / (double) mReferenceCategories.size();
    }



    /**
     * Returns the average over all test cases of the joint log (base
     * 2) probability of the response that matches the reference
     * category.  Better classifiers return higher values for this
     * average.
     *
     * <P>Whether average scores make sense across training instances
     * depends on the classifier.  For the language-model based
     * classifiers, the normalized score values are more reasonable
     * averages.
     *
     * @return The average joint log probability of the reference
     * category in the response.
     */
    public double averageLog2JointProbabilityReference() {
        double sum = 0.0;
        for (int i = 0; i < mReferenceCategories.size(); ++i) {
            String refCategory = mReferenceCategories.get(i).toString();
            JointClassification c
                = (JointClassification) mClassifications.get(i);
            for (int rank = 0; rank < c.size(); ++rank) {
                if (c.category(rank).equals(refCategory)) {
                    sum += c.jointLog2Probability(rank);
                    break;
                }
            }
        }
        return sum / (double) mReferenceCategories.size();
    }




    /**
     * Returns the average score of the specified response category
     * for test cases with the specified reference category.  If there
     * are no cases matching the reference category, the result is
     * <code>Double.NaN</code>.
     *
     * <P>Better classifiers return high values when the reference
     * and response categories are the same and lower values
     * when they are different.  Depending on the classifier, the
     * scores may or may not be meaningful as an average.
     *
     * @param refCategory Reference category.
     * @param responseCategory Response category.
     * @return Average score of response category in test cases for
     * specified reference category.
     * @throws IllegalArgumentException If the either category is unknown.
     */
    public double averageScore(String refCategory,
                               String responseCategory) {
        validateCategory(refCategory);
        validateCategory(responseCategory);
        double sum = 0.0;
        int count = 0;
        for (int i = 0; i < mReferenceCategories.size(); ++i) {
            if (mReferenceCategories.get(i).equals(refCategory)) {
                ScoredClassification c
                    = (ScoredClassification) mClassifications.get(i);
                for (int rank = 0; rank < c.size(); ++rank) {
                    if (c.category(rank).equals(responseCategory)) {
                        sum += c.score(rank);
                        ++count;
                        break;
                    }
                }
            }
        }
        return sum / (double) count;
    }

    /**
     * Returns the average rank of the specified response category for
     * test cases with the specified reference category.  If there are
     * no cases matching the reference category, the result is
     * <code>Double.NaN</code>.
     *
     * <P>Better classifiers return lower values when the reference
     * and response categories are the same and higher values
     * when they are different.  
     *
     * <P>For example, suppose there are three categories,
     * <code>a</code>, <code>b</code> and <code>c</code>.  Consider
     * the following seven test cases, with the specified ranked
     * results:
     *
     * <blockquote>
     * <table border='1' cellpadding='5'>
     * <tr><td><i>Test Case</i></td>
     *     <td><i>Reference</i></td>
     *     <td><i>Rank 0</i></td>
     *     <td><i>Rank 1</i></td>
     *     <td><i>Rank 2</i></td></tr>
     * <tr><td>0</td><td>a</td><td>a</td><td>b</td><td>c</td></tr>
     * <tr><td>1</td><td>a</td><td>a</td><td>c</td><td>b</td></tr>
     * <tr><td>2</td><td>a</td><td>a</td><td>b</td><td>c</td></tr>
     * <tr><td>3</td><td>a</td><td>b</td><td>a</td><td>c</td></tr>
     * <tr><td>4</td><td>b</td><td>b</td><td>a</td><td>c</td></tr>
     * <tr><td>5</td><td>b</td><td>a</td><td>c</td><td>b</td></tr>
     * <tr><td>6</td><td>c</td><td>c</td><td>b</td><td>a</td></tr>
     * </table>
     * </blockquote>
     *
     * for which:
     *
     * <blockquote><code>
     * averageRank(&quot;a&quot;,&quot;a&quot;) = (0 + 0 + 0 + 1)/4 = 0.25
     * <br>
     * averageRank(&quot;a&quot;,&quot;b&quot;) = (1 + 2 + 1 + 0)/4 = 1.00
     * <br>
     * averageRank(&quot;a&quot;,&quot;c&quot;) = (2 + 1 + 2 + 2)/4 = 1.75
     * <br>&nbsp;<br>
     * averageRank(&quot;b&quot;,&quot;a&quot;) = (1 + 0)/2 = 0.50
     * <br>
     * averageRank(&quot;b&quot;,&quot;b&quot;) = (0 + 2)/2 = 1.0
     * <br>
     * averageRank(&quot;b&quot;,&quot;c&quot;) = (2 + 1)/2 = 1.5
     * <br>&nbsp;<br>
     * averageRank(&quot;c&quot;,&quot;a&quot;) = (2)/1 = 2.0
     * <br>
     * averageRank(&quot;c&quot;,&quot;b&quot;) = (1)/1 = 1.0
     * <br>
     * averageRank(&quot;c&quot;,&quot;c&quot;) = (0)/1 = 0.0
     * </code></blockquote> 
     *
     * <p>If every ranked result is complete in assigning every
     * category to a rank, the sum of the average ranks will be one
     * less than the number of cases with the specified reference
     * value.  If categories are missing from ranked results, the
     * sums may possible be larger than one minus the number of test
     * cases.
     *
     * <p>Note that the confusion matrix is computed using only the
     * reference and first column of this matrix of results.
     *
     * @param refCategory Reference category.
     * @param responseCategory Response category.
     * @return Average rank of response category in test cases for
     * specified reference category.
     * @throws IllegalArgumentException If either category is unknown.
     */
    public double averageRank(String refCategory,
                              String responseCategory) {
        validateCategory(refCategory);
        validateCategory(responseCategory);
        double sum = 0.0;
        int count = 0;
        // iterate over all paired classifications and lists
        for (int i = 0; i < mReferenceCategories.size(); ++i) {
            if (mReferenceCategories.get(i).equals(refCategory)) {
                RankedClassification rankedClassification
                    = (RankedClassification) mClassifications.get(i);
                int rank = getRank(rankedClassification,responseCategory);
                sum += rank;
                ++count;
            }
        }
        return sum / (double) count;
    }

    int getRank(RankedClassification classification, String responseCategory) {
        for (int rank = 0; rank < classification.size(); ++rank)
            if (classification.category(rank).equals(responseCategory))
                return rank;
        // default to putting it in last rank
        return mCategories.length-1;
    }

    /**
     * Returns a scored precision-recall evaluation of the
     * classification of the specified reference category versus all
     * other categories using the classification scores.
     *
     * @param refCategory Reference category.
     * @return The scored one-versus-all precision-recall evaluatuion.
     * @throws IllegalArgumentException If the specified category
     * is unknown.
     */
    public ScoredPrecisionRecallEvaluation 
        scoredOneVersusAll(String refCategory) {

        validateCategory(refCategory);
        return scoredOneVersusAll(mScoreOutcomeLists,
                                  categoryToIndex(refCategory));
    }

    /**
     * Returns a scored precision-recall evaluation of the
     * classifcation of the specified reference category versus all
     * other categories using the conditional probability scores.
     * This method may only be called for evaluations that have
     * scores.
     *
     * @param refCategory Reference category.
     * @return The conditional one-versus-all precision-recall evaluatuion.
     * @throws IllegalArgumentException If the specified category
     * is unknown.
     */
    public ScoredPrecisionRecallEvaluation
        conditionalOneVersusAll(String refCategory) {

        validateCategory(refCategory);
        return scoredOneVersusAll(mConditionalOutcomeLists,
                                  categoryToIndex(refCategory));
    }

    /**
     * Returns the first-best one-versus-all precision-recall
     * evaluation of the classification of the specified reference
     * category versus all other categories.  This method may be
     * called for any evaluation.
     *
     * @param refCategory Reference category.
     * @return The first-best one-versus-all precision-recall
     * evaluatuion.
     * @throws IllegalArgumentException If the specified category
     * is unknown.
     */
    public PrecisionRecallEvaluation oneVersusAll(String refCategory) {
        validateCategory(refCategory);
        PrecisionRecallEvaluation prEval = new PrecisionRecallEvaluation();
        int numCases = mReferenceCategories.size();
        for (int i = 0; i < numCases; ++i) {
            Object caseRefCategory = mReferenceCategories.get(i);
            Classification response = (Classification) mClassifications.get(i);
            Object caseResponseCategory = response.bestCategory();
            boolean inRef = caseRefCategory.equals(refCategory);
            boolean inResp = caseResponseCategory.equals(refCategory);
            prEval.addCase(inRef,inResp);
        }
        return prEval;
    }

    private ScoredPrecisionRecallEvaluation 
        scoredOneVersusAll(ArrayList[] outcomeLists, int categoryIndex) {
        ScoredPrecisionRecallEvaluation eval
            = new ScoredPrecisionRecallEvaluation();
        List responseList = outcomeLists[categoryIndex];
        for (int i = 0; i < responseList.size(); ++i) {
            ScoreOutcome outcome = (ScoreOutcome) responseList.get(i);
            eval.addCase(outcome.mOutcome,outcome.mScore);
        }
        return eval;
    }



    /**
     * Returns a string-based representation of the classification
     * results.
     *
     * @return A string-based representation of the classification
     * results.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CLASSIFIER EVALUATION\n");
        mConfusionMatrix.toStringGlobal(sb);

        if (mHasRanked) {
            sb.append("Average Reference Rank=" 
                      + averageRankReference() + "\n");
        }

        if (mHasScored) {
            sb.append("Average Score Reference=" 
                      + averageScoreReference() + "\n");
        }

        if (mHasConditional) {
            sb.append("Average Conditional Probability Reference=" 
                      + averageConditionalProbabilityReference() + "\n");
        }

        if (mHasJoint) {
            sb.append("Average Log2 Joint Probability Reference=" 
                      + averageLog2JointProbabilityReference() + "\n");
        }
    
        sb.append("ONE VERSUS ALL EVALUATIONS BY CATEGORY\n");
        for (int i = 0; i < categories().length; ++i) {
            String category = categories()[i];
            sb.append("\nCATEGORY[" + i + "]=" + category + "\n");

            sb.append("First-Best Precision/Recall Evaluation\n");
            sb.append(oneVersusAll(category));
            sb.append("\n");

            if (mHasRanked) {
                sb.append("Rank Histogram=\n");
                appendCategoryLine(sb);
                for (int rank = 0; rank < numCategories(); ++rank) {
                    if (rank > 0) sb.append(',');
                    sb.append(mRankCounts[i][rank]);
                }
                sb.append("\n");

                sb.append("Average Rank Histogram=\n");
                appendCategoryLine(sb);
                for (int j = 0; j < numCategories(); ++j) {
                    if (j > 0) sb.append(',');
                    sb.append(averageRank(category,categories()[j]));
                }
                sb.append("\n");
            }

            if (mHasScored) {
                sb.append("Scored One Versus All\n");
                sb.append(scoredOneVersusAll(category).toString() + "\n");
                sb.append("Average Score Histogram=\n");
                appendCategoryLine(sb);
                for (int j = 0; j < numCategories(); ++j) {
                    if (j > 0) sb.append(',');
                    sb.append(averageScore(category,categories()[j]));
                }
                sb.append("\n");
            }
        
            if (mHasConditional) {
                sb.append("Conditional One Versus All\n");
                sb.append(conditionalOneVersusAll(category).toString() + "\n");
                sb.append("Average Conditional Probability Histogram=\n");
                appendCategoryLine(sb);
                for (int j = 0; j < numCategories(); ++j) {
                    if (j > 0) sb.append(',');
                    sb.append(averageConditionalProbability(category,
                                                            categories()[j]));
                }
                sb.append("\n");
            }

            if (mHasJoint) {
                sb.append("Average Joint Probability Histogram=\n");
                appendCategoryLine(sb);
                for (int j = 0; j < numCategories(); ++j) {
                    if (j > 0) sb.append(',');
                    sb.append(averageLog2JointProbability(category,
                                                          categories()[j]));
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    void appendCategoryLine(StringBuffer sb) {
        sb.append("  ");
        for (int i = 0; i < numCategories(); ++i) {
            if (i > 0) sb.append(',');
            sb.append(categories()[i]);
        }
        sb.append("\n  ");
    }

    private void validateCategory(String category) {
        if (mCategorySet.contains(category)) return;
        String msg = "Unknown category=" + category;
        throw new IllegalArgumentException(msg);
    }



    /**
     * NEEDS HEADERS AND ESCAPES.
     */
    void rankHistogramToCSV(StringBuffer sb) {
        for (int i = 0; i < numCategories(); ++i) {
            if (i > 0) sb.append('\n');
            for (int rank = 0; rank < numCategories(); ++rank) {
                if (rank > 0) sb.append(',');
                sb.append(mRankCounts[i][rank]);
            }
        }
    }



    double averageRankReference(int i) {
        double sum = 0.0;
        int count = 0;
        for (int rank = 0; rank < numCategories(); ++rank) {
            int rankCount = mRankCounts[i][rank];
            if (rankCount == 0) continue;
            count += rankCount;
            sum += rank * rankCount;
        }
        return sum / (double) count;
    }

    int categoryToIndex(String category) {
        int result = mConfusionMatrix.getIndex(category);
        if (result < 0) {
            String msg = "Unknown category=" + category;
            throw new IllegalArgumentException(msg);
        }
        return result;
    }

    
    int rankCount(int categoryIndex, int rank) {
        return mRankCounts[categoryIndex][rank];
    }

    /**
     * Adds the specified classification as a response for the specified
     * reference category.
     *
     * @param referenceCategory Reference category for case.
     * @param classification Response classification for case.
     */
    public void addClassification(String referenceCategory, 
                                  Classification classification) {

        mConfusionMatrix.increment(referenceCategory,
                                   classification.bestCategory());
        mReferenceCategories.add(referenceCategory);
        mClassifications.add(classification);
        ++mNumCases;
        if (classification instanceof RankedClassification) {
            mHasRanked = true;
            addRanking(referenceCategory,
                       (RankedClassification) classification);
        }
        if (classification instanceof ScoredClassification) {
            mHasScored = true;
            addScoring(referenceCategory,
                       (ScoredClassification) classification);
        }
        if (classification instanceof ConditionalClassification) {
            mHasConditional = true;
            addConditioning(referenceCategory,
                            (ConditionalClassification) classification);
        }
        if (classification instanceof JointClassification) {
            mHasJoint = true;
        }
    }

    final int numCategories() {
        return mConfusionMatrix.numCategories();
    }

    void addRanking(String refCategory, 
                    RankedClassification ranking) {
        updateRankHistogram(refCategory,ranking);
    }

    private void updateRankHistogram(String refCategory, 
                                     RankedClassification ranking) {
        int refCategoryIndex = categoryToIndex(refCategory);
        for (int rank = 0; rank < ranking.size(); ++rank) {
            String category = ranking.category(rank);
            if (category.equals(refCategory)) {
                ++ mRankCounts[refCategoryIndex][rank];
                return;
            }
        }
        // assume the reference has last rank 
        ++mRankCounts[refCategoryIndex][mCategories.length-1];
    }


    private void addScoring(String refCategory, ScoredClassification scoring) {
        for (int rank = 0; rank < numCategories(); ++rank) {
            double score = scoring.score(rank);
            String category = scoring.category(rank);
            int categoryIndex = categoryToIndex(category);
            boolean match = category.equals(refCategory);
            ScoreOutcome outcome = new ScoreOutcome(score,match,rank==0);
            mScoreOutcomeLists[categoryIndex].add(outcome);
        }
    }
    

    private void addConditioning(String refCategory, 
                                 ConditionalClassification scoring) {
        for (int rank = 0; rank < numCategories(); ++rank) {
            double score = scoring.conditionalProbability(rank);
            String category = scoring.category(rank);
            int categoryIndex = categoryToIndex(category);
            boolean match = category.equals(refCategory);
            ScoreOutcome outcome = new ScoreOutcome(score,match,rank==0);
            mConditionalOutcomeLists[categoryIndex].add(outcome);
        }
    }

    static class ScoreOutcome implements Scored {
        private final double mScore;
        private final boolean mOutcome;
        private final boolean mFirstBest;
        public ScoreOutcome(double score, boolean outcome, boolean firstBest) {
            mOutcome = outcome;
            mScore = score;
            mFirstBest = firstBest;
        }
        public double score() {
            return mScore;
        }
        public String toString() {
            return "(" + mScore + ": " + mOutcome + "firstBest=" + mFirstBest + ")";
        }
    }

}
