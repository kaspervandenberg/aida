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

import com.aliasi.util.Scored;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * A <code>ScoredPrecisionRecallEvaluation</code> provides an evaluation
 * of possible precision-recall operating points and other summary statistics
 * The single method {@link #addCase(boolean,double)} is used to populate
 * the evaluation, with the first argument representing whether the response
 * was correct and the second the score that was assigned.  Note that
 * only positive reference cases are considered as part of this evaluation.
 *
 * <P>By way of example, consider the following table of cases, all of
 * which involve positive responses.  The cases are in rank order,
 * with their scores and whether they were correct listed.  
 *
 * <blockquote>
 * <table border='1' cellpadding='5'>
 * <tr><td><i>Rank</i></td>
 *     <td><i>Score</i></td>
 *     <td><i>Correct</i></td>
 *     <td><i>Rec</i></td>
 *     <td><i>Prec</i></td>
 *     <td><i>Rej Rec</i></td></tr>
 * <tr><td>0</td><td>-1.21</td><td>incorrect</td>
 *     <td>0.00</td>
 *     <td><small><code>NaN</code></small></td>
 *     <td>0.83</td></tr>
 * <tr bgcolor='#CCCCFF'><td>1</td><td>-1.27</td><td>correct</td>
 *     <td>0.25</td>
 *     <td>0.50</td>
 *     <td bgcolor='orange'>0.83</td></tr>
 * <tr><td>2</td><td>-1.39</td><td>incorrect</td>
 *     <td>0.25</td>
 *     <td>0.33</td>
 *     <td>0.67</td></tr>
 * <tr bgcolor='#CCCCFF'><td>3</td><td>-1.47</td><td>correct</td>
 *     <td>0.50</td>
 *     <td>0.50</td>
 *     <td>0.67</td></tr>
 * <tr bgcolor='#CCCCFF'><td>4</td><td>-1.60</td><td>correct</td>
 *     <td>0.75</td>
 *     <td bgcolor='yellow'>0.60</td>
 *     <td bgcolor='orange'>0.67</td></tr>
 * <tr><td>5</td><td>-1.65</td><td>incorrect</td>
 *     <td>0.75</td>
 *     <td>0.50</td>
 *     <td>0.50</td></tr>
 * <tr><td>6</td><td>-1.79</td><td>incorrect</td>
 *     <td>0.75</td>
 *     <td>0.43</td>
 *     <td>0.33</td></tr>
 * <tr><td>7</td><td>-1.80</td><td>incorrect</td>
 *     <td>0.75</td>
 *     <td>0.38</td>
 *     <td>0.17</td></tr>
 * <tr bgcolor='#CCCCFF'><td>8</td><td>-2.01</td><td>correct</td>
 *     <td>1.00</td>
 *     <td bgcolor='yellow'>0.44</td>
 *     <td bgcolor='orange'>0.17</td></tr>
 * <tr><td>9</td><td>-3.70</td><td>incorrect</td>
 *     <td>1.00</td>
 *     <td>0.40</td>
 *     <td>0.00</td></tr>
 * </table>
 * </blockquote>
 *
 * Note that there are four positive reference cases (blue
 * backgrounds) and six negative reference cases (clear backgrounds)
 * in this diagram.  By setting an acceptance threshold at the various
 * scores, the precision, recall, and rejection recall values listed
 * in the fourth through sixth columns are derived.  For instance,
 * after the rank 0 response, which is wrong, recall is
 * <code>0/4&nbsp;=&nbsp;0.0</code>, because we have retrieved none of
 * the four positive reference cases; similarly, rejection recall is
 * <code>5/6&nbsp;=&nbsp;0.83</code> because 5/6 of the negative
 * reference cases have been rejected.  After the rank 4 response,
 * recall is 3/4 and rejection recall is 2/6.  
 *
 * <P>The pairs of precision/recall values form the basis for the
 * precision-recall curve returned by {@link #prCurve(boolean)}, with
 * the argument indicating whether to perform precision interpolation.
 * For the above graph:
 *
 * <pre>
 *     <b>prCurve</b>(false) = { {0.25, 0.50}, 
 *                        {0.50, 0.50}, 
 *                        {0.75, 0.60}, 
 *                        {1.00, 0.44} }</pre>
 *
 * <P>The pairs of recall/rejection recall values form the basis for the
 * receiver operating characteristic (ROC) curve returned by {@link
 * #rocCurve(boolean)} with the boolean parameter again indicating
 * whether to perform precision interpolation.  For the above graph,
 * the result is:
 * 
 * <pre> 
 *     <b>rocCurve</b>(false) = { { 0.25, 0.83 },
 *                         { 0.50, 0.67 },
 *                         { 0.75, 0.67 },
 *                         { 1.00, 0.17 } }</pre>
 * 
 * Note that for both curves, only the rows corresponding to the
 * correct responses are considered, which are highlighted in blue.  
 * 
 * <P>Precision interpolation removes any operating point for which
 * there is a dominant operating point in both dimensions.
 * For the precision-recall curve, the points <code>(0.25,0.50)</code>
 * and <code>(0.50,0.50)</code> are dominated in both dimensions by
 * <code>(0.75,0.60)</code> and so are dropped; the resulting curve is:
 *
 * <pre>
 *     <b>prCurve</b>(true) =  { {0.75, 0.60}, 
 *                        {1.00, 0.44} }</pre>
 *
 * This is meant to be read
 * meant to be read as having a constant precision of 0.6 for all
 * recall values between 0 and 0.75 inclusive; thus the interpolation
 * increases values.  For the ROC curve, only the three points
 * highlighted in yellow are left:
 * 
 * <pre>
 *     <b>rocCurve</b>(true) = { { 0.25, 0.83 },
 *                        { 0.75, 0.67 },
 *                        { 1.00, 0.17 } }</pre>
 *
 * Note that the precision interpolated curves always provide strictly
 * decreasing precisions.
 *
 * <P>The area under the raw precision-recall and ROC curves, with
 * or without interpolation, is computed by the following methods:
 *
 * <pre>
 *     <b>areaUnderPrCurve</b>(false) = (0.25 - 0.00) * 0.50
 *                             + (0.50 - 0.25) * 0.50
 *                             + (0.75 - 0.50) * 0.60
 *                             + (1.00 - 0.75) * 0.44
 *     = 0.51
 * 
 *     <b>areaUnderPrCurve</b>(true) = (0.75 - 0.00) * 0.60
 *                            + (1.00 - 0.75) * 0.44 
 *     = 0.56</pre>
 *
 * The ROC areas are computed similarly to yield:
 * 
 * <pre>
 *     <b>areaUnderRocCurve</b>(false) = 0.58
 * 
 *     <b>areaUnderRocCurve</b>(true) = 0.58</pre>
 * 
 * Note that the precision-interpolated values are always higher.
 *
 * <P>For precision-recall curves, three additional summary statistics
 * are available.  The first provides an average over precision values
 * over the operating points on the uninterpolated precision-recall
 * curve.
 *
 * <pre>
 *      <b>averagePrecision</b>() = (0.50 + 0.50 + 0.60 + 0.44)/4.00 
 *      = 0.51</pre>
 *
 * The second merely returns the maximum F<sub><sub>&beta;</sub></sub>
 * measure for an actual operating point:
 *
 * <pre>
 *      <b>maximumFMeasure</b>() = <b>maximumFMeasure</b>(1) = 0.67</pre>
 *
 * Note that this statistic provides a post-hoc optimal setting for
 * F-measure.  Further note that it is based on actual operating
 * points, not interpolations between operating points.  The final
 * statistic is the so-called precision-recall breakeven point (BEP).
 * This is computed in the standard way by using the interpolated
 * precision-recall curve.  Because the two points of interest are
 * (0.0,0.6) and (0.75,0.6), the best point at which they are equal
 * is (0.6,0.6), and thus:
 *
 * <pre>
 *      <b>prBreakevenPoint</b>() = 0.6</pre>
 *
 * Note that this value will always be less than or equal to the
 * maximum F<sub><sub>1</sub></sub>-measure.
 *
 * <P>Given the precision-recall curve, it's possible to compute the
 * precision after any given number of results.  The method
 * <code>precisionAt(int)</code> will return the precision after
 * the specified number of results.  For instance, in the above
 * graph:
 *
 * <pre>
 *      <b>precisionAt(5)</b>() = 0.6
 *      <b>precisionAt(10)</b>() = 0.4</pre>
 *
 * Typically results are reported for 5, 10 and 100 when available
 * (counting from 1, not 0).  The other information-retrieval style
 * result we return is the reciprocal rank (RR), which is defined to
 * be <code>1/rank</code>, again counting from 1, not 0.  For
 * instance, for the graph above, the first correct answer is the
 * second, soRR is 1/2:
 *
 * <pre>
 *      <b>reciprocalRank()</b>() = 0.5</pre>
 * 
 *
 * @author  Bob Carpenter
 * @version 2.4
 * @since   LingPipe2.1
 */
public class ScoredPrecisionRecallEvaluation {

    private final Set mCases = new TreeSet(Scored.REVERSE_SCORE_COMPARATOR);
    private int mNegativeRef = 0;
    private int mPositiveRef = 0;

    /**
     * Construct a scored precision-recall evaluation.
     */
    public ScoredPrecisionRecallEvaluation() { 
        /* do nothing */
    }
    
    /**
     * Add a case with the specified correctness and response score.
     * Only positive response cases are considered, and the correct
     * flag is set to <code>true</code> if the reference was also
     * positive.  The score is just the response score.
     *
     * <P><b>Warning:</b> The scores should be sensibly comparable
     * across cases.  
     *
     * @param correct <code>true</code> if this case was correct.
     * @param score Score of response.
     */
    public void addCase(boolean correct, double score) {
        mCases.add(new Case(correct,score));
        if (correct) ++mPositiveRef;
        else ++mNegativeRef;
    }

    /**
     * Returns the set of recall/precision operating points according
     * to the scores of the cases.  The method {@link
     * #rocCurve(boolean)} returns the recall (sensitivity)
     * versus rejection recall (specificity) operating points, which
     * take the number of true negative classifications into account.
     *
     * Note that the recall values (the first component) are strictly
     * increasing, resulting in a well-defined function from recall
     * to precision. 
     *
     * <P>The second operation derives so-called &quot;interpolated
     * precision&quot; and is widely used for evaluating information
     * retrieval systems.  The interpolated precision of a given
     * recall point is defined to be the maximum precision for that
     * recall point and any higher recall point.  This ensures that
     * precision values are non-increasing with increased recall.  For
     * the example above, because 0.60 precision is found at 0.75
     * recall, the interpolated precision of all recall levels lower
     * than 0.75 is 0.60.  This method implements this interpolation by
     * only returning points that are not dominated by other points
     * that have both better precision and recall:
     *
     * 
     * In the diagram, these are the yellow highlighted precision
     * points. 
     *
     * <P>It is common to also see this graph completed with points
     * (0,1) and (1,0), but this function does <i>not</i> include these
     * limits.  The one hundred percent precision implied by the
     * first point is not necessarily achievable, whereas the
     * second point will be no better than the last point in the
     * return result.
     *
     * <P>Neither interpolated nor uninterpolated return values are
     * guaranteed to be convex.  Convex closure will skew results
     * upward in an even more unrealistic direction, especially if the
     * artificial completion point (0,1) is included.
     *
     * @param interpolate Set to <code>true</code> if the precisions
     * are interpolated through pruning dominated points.
     * @return The precision-recall curve for the specified category.
     */
    public double[][] prCurve(boolean interpolate) {
        PrecisionRecallEvaluation eval 
            = new PrecisionRecallEvaluation();
        ArrayList prList = new ArrayList();
        Iterator it = mCases.iterator();
        for (int i = 0; it.hasNext(); ++i) {
            Case cse = (Case) it.next();
            boolean correct = cse.mCorrect;
            eval.addCase(correct,true);
            if (correct) {
                double r = div(eval.truePositive(),mPositiveRef);
                double p = eval.precision();
                prList.add(new double[] { r, p });
            }
        }
        return interpolate(prList,interpolate);
    }

    /**
     * Returns the receiver operating characteristic (ROC) curve for
     * the cases ordered by score.  The ROC curve returns an array of
     * points making up a plot of sensitivity (recall) versus
     * specificity (rejection recall).  As usual, recall (sensitivity)
     * is <code>TP/(TP+FN)</code> and rejection recall (sensitivity)
     * is its negative dual <code>TN/(TN+FP)</code>, where
     * <code>TP</code> is the true positive count, <code>FP</code> the
     * false positive, <code>FN</code> the false negative and
     * <code>TN</code> the true negative counts.  Through sensitivity,
     * the ROC curve provides information about rejection.
     *
     * <P>The last column in the example in {@link
     * #prCurve(boolean)} provides the rejection recall rates
     * at each threshold. The resulting ROC curves for that example
     * are:
     *
     * As with the recall-precision curve, the parameter determines
     * whether or not to &quot;interpolate&quot; the rejection recall
     * values.  This is carried out as with the recall-precision curve
     * by only returning values which would not be interpolated.  In
     * general, without interpolation, the same rows of the table are
     * used as for the recall-precision curve, namely those at the end
     * of a run of true positives.  Interpolation may result in a
     * different set of recall points in the pruned answer set, as in
     * the example above.
     *
     * <P>Like the recall-precision curve method, this method does not
     * insert artificial end ponits of (0,1) and (1,0) into the graph.
     * As with the recall-precision curve, the final entry will have
     * recall equal to one.  
     *
     * <P>Neither interpolated nor uninterpolated return values are
     * guaranteed to be convex.  Convex closure will skew results
     * upward in an even more unrealistic direction, especially if the
     * artificial completion point (0,1) is included.
     * 
     * @param interpolate If <code>true</code>, any point with both
     * precision and recall lower than another point is eliminated
     * from the returned precision-recall curve.
     * @return The receiver operating characteristic curve for the
     * specified category.
     */
    public double[][] rocCurve(boolean interpolate) {
        PrecisionRecallEvaluation eval = new PrecisionRecallEvaluation();
        ArrayList prList = new ArrayList();
        Iterator it = mCases.iterator();
        for (int i = 0; it.hasNext(); ++i) {
            Case cse = (Case) it.next();
            boolean correct = cse.mCorrect;
            eval.addCase(correct,true);
            if (correct) {
                double r = div(eval.truePositive(), mPositiveRef);
                double rr 
                    = 1.0 - div(eval.falsePositive(), mNegativeRef);
                prList.add(new double[] { r, rr });
            }
        }
        return interpolate(prList,interpolate);
    }

    /**
     * Returns the maximum F<sub><sub>1</sub></sub>-measure for an
     * actual operating point on the uninterpolated precision-recall
     * curve.  This maximization is based on a post-hoc optimal
     * acceptance threshold.  This is derived from the pair on the
     * recall-precision curve yielding the highest value of the F
     * measure, <code>2*recall*precision/(recall+precision)</code>.
     *
     * <P>For the example in {@link #prCurve(boolean)}:
     *
     * <blockquote><code>
     * maximumFMeasure(&quot;foo&quot;) = 0.67
     * </code></blockquote>
     *
     * corresponding to recall=0.75 and precision=0.60.
     *
     * @return Maximum f-measure for the specified category.
     */
    public double maximumFMeasure() {
        return maximumFMeasure(1.0);
    }

    /**
     * Returns the maximum F<sub><sub>&beta;</sub></sub>-measure for
     * an actual operating point on the uninterpolated
     * precision-recall curve for a specified &beta;.  This
     * maximization is based on a post-hoc optimal acceptance
     * threshold.  This is derived from the pair on the
     * recall-precision curve yielding the highest value of the F
     * measure, <code>2*recall*precision/(recall+precision)</code>.
     *
     * <P>For the example in {@link #prCurve(boolean)}:
     *
     * <blockquote><code>
     * maximumFMeasure(&quot;foo&quot;) = 0.67
     * </code></blockquote>
     *
     * corresponding to recall=0.75 and precision=0.60.
     *
     * @return Maximum f-measure for the specified category.
     */
    public double maximumFMeasure(double beta) {
        double maxF = 0.0;
        double[][] pr = prCurve(false);
        for (int i = 0; i < pr.length; ++i) {
            double f = PrecisionRecallEvaluation.fMeasure(beta,pr[i][0],pr[i][1]);
            maxF = Math.max(maxF,f);
        }
        return maxF;
    }

    /**
     * Returns the breakeven point (BEP) for precision and recall
     * based on the interpolated precision.  This is the point where
     * the interpolated precision-recall curve has recall equal to
     * precision.  Because the interpolation is a step fucntion, the
     * result is different than if two points were linearly
     * interpolated.
     *
     * <P>For the example illustrated in {@link #prCurve(boolean)},
     * the breakeven point is 0.60.  This is because the interpolated
     * precision recall curve is flat from the implicit initial point
     * <code>(0.00,0.60)</code> to <code>(0.75,0.60)</code> and thus
     * the line between them has a breakeven point of <code>x = y =
     * 0.6</code>.
     *
     * <P>As an interpolation (equal precision and recall) of a
     * rounded up estimate (interpolated recall-precision curve), the
     * breakeven point is not necessarily an achievable operating
     * point.  Note that the recall-precision breakeven point will
     * always be smaller than the maximum F measure, which does
     * correspond to an observed operating point, because the
     * breakeven point always involves lowering the recall of the
     * first point on the curve with recall greater than precision to
     * match the precision.
     *
     * <P>This method will return <code>0.0</code> if the
     * precision-recall curve never crosses the diagonal.
     *
     * @return The interpolated recall-precision breakeven point.
     */
    public double prBreakevenPoint() {
        double[][] prCurve = prCurve(true);
        for (int i = 0; i < prCurve.length; ++i)
            if (prCurve[i][0] > prCurve[i][1])
                return prCurve[i][1];
        return 0.0;
    }

    /**
     * Returns point-wise average precision of points on the
     * uninterpolated precision-recall curve.  See {@link
     * #prCurve(boolean)} for a definition of the values on the curve.
     * 
     * <P>This method implements the standard information retrieval
     * definition, which only averages precision measurements from
     * correct responses.
     *
     * <P>For the example provided in {@link #prCurve(boolean)}, the
     * average precision is the average of precision values for the
     * correct responses (highlighted lines):
     * 
     *
     * <P>Although the reasoning is different, the average precision
     * returned is the same as the area under the uninterpolated
     * recall-precision graph.
     *
     * @return Pointwise average precision.
     */
    public double averagePrecision() {
        double[][] prCurve = prCurve(false);
        double sum = 0.0;
        for (int i = 0; i < prCurve.length; ++i)
            sum += prCurve[i][1]; 
        return sum/((double)prCurve.length);
    }

    /**
     * Returns the precision score achieved by returning the top
     * scoring documents up to the specified rank.  The
     * precision-recall curve is not interpolated for this
     * computation.  If there are not enough documents, the result
     * <code>Double.NaN</code> is returned.
     * 
     * @return The precision at the specified rank.
     */
    public double precisionAt(int rank) {
        if (mCases.size() < rank) return Double.NaN;
        int correctCount = 0;
        Iterator it = mCases.iterator();
        for (int i = 0; i < rank; ++i)
            if (((Case)it.next()).mCorrect)
                ++correctCount;
        return ((double) correctCount) / (double) rank;
    }


    /**
     * Returns the reciprocal rank (RR) for this evaluation.  The
     * reciprocal rank is defined to be the reciprocal of the rank at
     * which the first correct result is retrieved (counting from 1).
     * The return result will be between 1.0 for the first-best result
     * being correct and 0.0, for none of the results being correct.
     *
     * <P>Typically, the mean of the reciprocal ranks for a number
     * of evaluations is reported.
     *
     * @return The reciprocal rank.
     */
    public double reciprocalRank() {
        Iterator it = mCases.iterator();
        for (int i = 0; it.hasNext(); ++i) {
            Case cse = (Case) it.next();
            boolean correct = cse.mCorrect;
            if (correct)
                return 1.0 / (double) (i + 1);
        }
        return 0.0;
    }

    /**
     * Returns the area under the recall-precision curve with
     * interpolation as specified.  The recall-precision curve is
     * taken to be a step function for the purposes of this
     * calculation, and thus whether precision is interpolated, that
     * is, whether dominated entries are pruned, will affect the area.
     *
     * <P>For the example detailed in {@link
     * #prCurve(boolean)}, the areas without and with
     * interpolation are:
     *
     *
     * Interpolation will always result in an equal or greater area.
     *
     * <P>Note that the uninterpolated area under the recall-precision
     * curve is the same as the average precision value.
     *
     * @param interpolate Set to <code>true</code> to interpolate
     * the precision values.
     * @return The area under the specified precision-recall curve.
     */
    public double areaUnderPrCurve(boolean interpolate) {
        return areaUnder(prCurve(interpolate));
    }

    /**
     * Returns the area under the receiver operating characteristic
     * (ROC) curve.  The ROC curve is taken to be a step function for
     * the purposes of this calculation, and thus whether rejection
     * recall is interpolated, that is, whether dominated entries are,
     * will affect the area.
     *
     * Interpolation will always result in an equal or greater area.
     *
     * @param interpolate Set to <code>true</code> to interpolate
     * the rejection recall values.
     * @return The area under the ROC curve.
     */
    public double areaUnderRocCurve(boolean interpolate) {
        return areaUnder(rocCurve(interpolate));
    }

    /**
     * Returns a string-based representation of this scored precision
     * recall evaluation.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("  Area Under PR Curve (interpolated)=" 
                  + areaUnderPrCurve(true));
        sb.append("\n  Area Under PR Curve (uninterpolated)=" 
                  + areaUnderPrCurve(false));
        sb.append("\n  Area Under ROC Curve (interpolated)=" 
                  + areaUnderRocCurve(true));
        sb.append("\n  Area Under ROC Curve (uninterpolated)=" 
                  + areaUnderRocCurve(false));
        sb.append("\n  Average Precision=" + averagePrecision());
        sb.append("\n  Maximum F(1) Measure=" + maximumFMeasure());
        sb.append("\n  BEP (Precision-Recall break even point)=" 
                  + prBreakevenPoint());
        sb.append("\n  Reciprocal Rank=" + reciprocalRank());
        int[] ranks = new int[] { 5, 10, 25, 100, 500 };
        for (int i = 0; i < ranks.length && mCases.size() < ranks[i]; ++i)
            sb.append("\n  Precision at " + ranks[i]
                      + "=" + precisionAt(ranks[i]));
        return sb.toString();
    }

    static double div(double x, double y) {
        return x/y;
    }

    private static double[][] interpolate(ArrayList prList, 
                                          boolean interpolate) {
        if (!interpolate) {
            double[][] rps = new double[prList.size()][];
            prList.toArray(rps);
            return rps;
        }
        Collections.reverse(prList);
        LinkedList resultList = new LinkedList();
        Iterator it = prList.iterator();
        double maxP = Double.NEGATIVE_INFINITY;
        while (it.hasNext()) {
            double[] rp = (double[]) it.next();
            double p = rp[1];
            if (maxP < p) {
                maxP = p;
                resultList.addFirst(rp);
            }
        }
        double[][] rps = new double[resultList.size()][];
        resultList.toArray(rps);
        return rps;
    }

    private static double areaUnder(double[][] zeroOneStepFunction) {
        double area = 0.0;
        double lastX = 0.0;
        for (int i = 0; i < zeroOneStepFunction.length; ++i) {
            double x = zeroOneStepFunction[i][0];
            double height = zeroOneStepFunction[i][1];
            double width = x - lastX; 
            area += width * height; // step function
            lastX = x;
        }
        return area;
    }

    static class Case implements Scored {
        private final boolean mCorrect;
        private final double mScore;
        Case(boolean correct, double score) {
            mCorrect = correct;
            mScore = score;
        }
        public double score() {
            return mScore;
        }
        public String toString() {
            return mCorrect + " : " + mScore;
        }
    }

}
