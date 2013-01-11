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

package com.aliasi.stats;

import com.aliasi.util.AbstractExternalizable;

import java.util.ArrayList;
import java.util.HashMap;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * A <code>MultivariateEstimator</code> provides a maximum likelihood
 * estimator of a multivariate distribution based on training samples.
 * Training is carried out by incrementing outcomes through {@link
 * #train(String,long)}.  At any point, the distribution provides a
 * maximum likelihood estimator.
 *
 * <P>Simple additive smoothing can be achieved through the API by
 * initially incrementing counts for all possible outcomes by one.
 *
 * <h3>Compilation and Serialization</h3>
 *
 * <p>Serialization simply stores the current multivariate estimator
 * and reconstructs it exactly as is under deserialization (that is,
 * the class of the deserialized object is
 * <code>MultivariateEstimator</code>).  Compilation stores a more
 * efficient and compact version of the estimator, which deserializes
 * to a <code>MultivariateDistribution</code> rather than a
 * <code>MultivariateEstimator</code>.
 *
 * @author  Bob Carpenter
 * @version 3.1.3
 * @since   LingPipe2.0
 */
public class MultivariateEstimator
    extends MultivariateDistribution
    implements Serializable {

    static final long serialVersionUID = 1171641384366463097L;

    final HashMap mLabelToIndex;
    final ArrayList mIndexToLabel;
    final ArrayList mIndexToCount;

    long mTotalCount = 0l;
    int mNextIndex = 0;

    /**
     * Construct a multivariate estimator with no known outcomes
     * or counts.
     */
    public MultivariateEstimator() {
        this(new HashMap(), new ArrayList(), new ArrayList());
    }

    private MultivariateEstimator(HashMap labelToIndex,
                                  ArrayList indexToLabel,
                                  ArrayList indexToCount) {
        mLabelToIndex = labelToIndex;
        mIndexToLabel = indexToLabel;
        mIndexToCount = indexToCount;
    }

    static void checkLongAddInRange(long a, long b) {
        if (Long.MAX_VALUE - b < a) {
            String msg = "Long addition overflow."
                + " a=" + a
                + " b=" + b;
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Resets the count for the specified outcome label to zero.
     * Calling this method will also decrement the total count
     * for this estimator.
     *
     * @param outcomeLabel Label of outcome that is reset.
     * @throws IllegalArgumentException If the outcome label is not
     * known.
     */
    public void resetCount(String outcomeLabel) {
        Integer index = (Integer) mLabelToIndex.get(outcomeLabel);
        if (index == null) {
            String msg = "May only reset known outcomes."
                + " Found outcome=" + outcomeLabel;
            throw new IllegalArgumentException(msg);
        }
        long currentCount
            = ((Long) mIndexToCount.get(index.intValue())).longValue();
        mTotalCount -= currentCount;
        mIndexToCount.set(index.intValue(),
                          new Long(0));
    }

    /**
     * Increment counts in this estimator for the specified
     * outcome by the specified increment.
     *
     * @param outcomeLabel Label of sample outcome.
     * @param increment Amount to increment count for outcome.
     * @throws IllegalArgumentException If the result would
     * be a count higher than the maximum long value or if the
     * increment is less than one.
     */
    public void train(String outcomeLabel, long increment) {
        if (increment < 1) {
            String msg = "Increment must be positive."
                + " Found increment=" + increment;
            throw new IllegalArgumentException(msg);
        }
        mTotalCount += increment;
        Integer indexInteger
            = (Integer) mLabelToIndex.get(outcomeLabel);
        if (indexInteger == null) {
            int index = mNextIndex++;
            mLabelToIndex.put(outcomeLabel,new Integer(index));
            mIndexToLabel.add(index,outcomeLabel);
            mIndexToCount.add(index,new Long(increment));
            return;
        }
        int index = indexInteger.intValue();
        long currentCount = ((Long) mIndexToCount.get(index)).intValue();
        checkLongAddInRange(currentCount,increment);
        mIndexToCount.set(index,new Long(currentCount + increment));
    }

    /**
     * Return the outcome for the specified label.
     *
     * @param outcomeLabel Label whose outcome is returned.
     * @return The outcome for the specified label.
     */
    public long outcome(String outcomeLabel) {
        Integer outcome = (Integer) mLabelToIndex.get(outcomeLabel);
        return outcome == null ? -1l : outcome.longValue();
    }

    /**
     * Return the label for the specified outcome.
     *
     * @param outcome Outcome whose label is returned.
     * @return The label for the specified outcome.
     */
    public String label(long outcome) {
        if (outcome < 0l || outcome >= mNextIndex) {
            String msg = "Outcome must be between 0 and max."
                + " Max outcome=" + maxOutcome()
                + " Argument outcome=" + outcome;
            throw new IllegalArgumentException(msg);
        }
        return mIndexToLabel.get((int)outcome).toString();
    }

    /**
     * Returns the number of dimensions for this multivariate
     * estimator.
     *
     * @return The number of dimensions for this multivariate
     * estimator.
     */
    public int numDimensions() {
        return mIndexToLabel.size();
    }

    /**
     * Returns the multivariate probability estimate for the specified
     * outcome.
     *
     * @param outcome The outcome whose probability is returned.
     * @return The probability of the specified outcome.
     */
    public double probability(long outcome) {
        if (outcome < minOutcome() || outcome > maxOutcome())
            return 0.0;
        return ((double) getCount(outcome))
            / (double) trainingSampleCount();
    }

    /**
     * Returns the count in this estimator for the specified outcome.
     *
     * @param outcome The outcome whose probability is returned.
     * @return The probability of the specified outcome.
     * @throws IllegalArgumentException If the outcome is not between
     * zero and the maximum outcome inclusive.
     */
    public long getCount(long outcome) {
        checkOutcome(outcome);
        Long count = (Long) mIndexToCount.get((int)outcome);
        return count == null ? 0l : count.longValue();
    }

    /**
     * Returns the count for the specified outcome.
     *
     * @param outcomeLabel Label of specified outcome.
     * @return Count of outcome in this estimator.
     * @throws IllegalArgumentException If the
     */
    public long getCount(String outcomeLabel) {
        Integer index = (Integer) mLabelToIndex.get(outcomeLabel);
        if (index == null) {
            String msg = "May only count known outcomes by label."
                + " Found outcome=" + outcomeLabel;
            throw new IllegalArgumentException(msg);
        }
        return getCount(index.longValue());
    }

    /**
     * Returns the total count of training sample.
     *
     * @return The total count for this estimator.
     * @throws IllegalArgumentException If the outcome is not between
     * zero and the maximum outcome inclusive.
     */
    public long trainingSampleCount() {
        return mTotalCount;
    }

    /**
     * Writes a constant version of this estimator to the specified
     * object output.  The distribution read back in will be an
     * instance of {@link MultivariateConstant} with the same
     * distribution as the estimated distribution.
     *
     * @param objOut The object output to which this estimator is
     * compiled.
     * @throws IOException If there is an I/O exception writing to the
     * output.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }


    static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = 2913496935213914118L;
        final MultivariateEstimator mEstimator;
        public Externalizer() { mEstimator = null; }
        public Externalizer(MultivariateEstimator estimator) {
            mEstimator = estimator;
        }
        public void writeExternal(ObjectOutput out)
            throws IOException {

            String[] labels = new String[mEstimator.mIndexToLabel.size()];
            mEstimator.mIndexToLabel.toArray(labels);
            out.writeObject(labels);
            Long[] counts = new Long[mEstimator.mIndexToCount.size()];
            mEstimator.mIndexToCount.toArray(counts);
            double totalCount = mEstimator.mTotalCount;
            double[] ratios = new double[counts.length];
            for (int i = 0; i < ratios.length; ++i)
                ratios[i] = counts[i].doubleValue()/totalCount;
            out.writeObject(ratios);
        }
        public Object read(ObjectInput in)
            throws ClassNotFoundException, IOException {
            String[] labels = (String[]) in.readObject();
            double[] ratios = (double[]) in.readObject();
            return new MultivariateConstant(ratios,labels);
        }
    }


}
